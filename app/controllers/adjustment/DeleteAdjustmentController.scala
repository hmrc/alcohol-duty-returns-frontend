/*
 * Copyright 2024 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers.adjustment

import connectors.{AlcoholDutyCalculatorConnector, CacheConnector}
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import forms.adjustment.DeleteAdjustmentFormProvider
import models.UserAnswers
import models.adjustment.{AdjustmentDuty, AdjustmentType}
import models.adjustment.AdjustmentType.{Overdeclaration, Underdeclaration}
import navigation.AdjustmentNavigator
import pages.QuestionPage
import pages.adjustment.{AdjustmentEntryListPage, DeleteAdjustmentPage, OverDeclarationReasonPage, OverDeclarationTotalPage, UnderDeclarationReasonPage, UnderDeclarationTotalPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.adjustment.DeleteAdjustmentView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class DeleteAdjustmentController @Inject() (
  override val messagesApi: MessagesApi,
  cacheConnector: CacheConnector,
  navigator: AdjustmentNavigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: DeleteAdjustmentFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: DeleteAdjustmentView,
  alcoholDutyCalculatorConnector: AlcoholDutyCalculatorConnector
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(index: Int): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val preparedForm = request.userAnswers.get(DeleteAdjustmentPage).fold(form)(value => form.fill(value))
    Ok(view(preparedForm, index))
  }

  def onSubmit(index: Int): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, index))),
          value =>
            if (value) {
              for {
                updatedAnswers                        <- Future.fromTry(request.userAnswers.removeBySeqIndex(AdjustmentEntryListPage, index))
                userAnswersWithUpdatedOverUnderReason <- updateOverUnderDeclarationReasons(updatedAnswers)
                _                                     <- cacheConnector.set(userAnswersWithUpdatedOverUnderReason)
              } yield Redirect(controllers.adjustment.routes.AdjustmentListController.onPageLoad())
            } else {
              Future.successful(Redirect(controllers.adjustment.routes.AdjustmentListController.onPageLoad()))
            }
        )
  }

  private def updateOverUnderDeclarationReasons(
    userAnswers: UserAnswers
  )(implicit hc: HeaderCarrier): Future[UserAnswers] = {
    val underDeclarationDuties = getDutiesByAdjustmentType(Underdeclaration, userAnswers)
    val overDeclarationDuties  = getDutiesByAdjustmentType(Overdeclaration, userAnswers)

    val underDeclarationTotalFuture = calculateTotalDuty(underDeclarationDuties)
    val overDeclarationTotalFuture  = calculateTotalDuty(overDeclarationDuties)

    for {
      underDeclarationTotal           <- underDeclarationTotalFuture
      overDeclarationTotal            <- overDeclarationTotalFuture
      userAnswersWithUnderDeclaration <-
        Future.fromTry(userAnswers.set(UnderDeclarationTotalPage, underDeclarationTotal.duty))
      updatedUserAnswers              <-
        Future.fromTry(userAnswersWithUnderDeclaration.set(OverDeclarationTotalPage, overDeclarationTotal.duty))
      answersRemovingOverDeclaration  <-
        Future.fromTry(
          clearIfBelowThreshold(OverDeclarationReasonPage, overDeclarationTotal.duty.abs, updatedUserAnswers)
        )
      finalUserAnswers                <-
        Future.fromTry(
          clearIfBelowThreshold(UnderDeclarationReasonPage, underDeclarationTotal.duty, answersRemovingOverDeclaration)
        )
    } yield finalUserAnswers
  }

  private def getDutiesByAdjustmentType(adjustmentType: AdjustmentType, userAnswers: UserAnswers): Seq[BigDecimal] =
    userAnswers
      .get(AdjustmentEntryListPage)
      .toSeq
      .flatten
      .filter(_.adjustmentType.contains(adjustmentType))
      .flatMap(_.duty)

  private def calculateTotalDuty(duties: Seq[BigDecimal])(implicit hc: HeaderCarrier): Future[AdjustmentDuty] =
    if (duties.nonEmpty) {
      alcoholDutyCalculatorConnector.calculateTotalAdjustment(duties)
    } else {
      Future.successful(AdjustmentDuty(BigDecimal(0)))
    }

  private def clearIfBelowThreshold(
    reasonPage: QuestionPage[_],
    total: BigDecimal,
    userAnswers: UserAnswers
  ): Try[UserAnswers] =
    if (total.abs < 1000) userAnswers.remove(reasonPage) else Try(userAnswers)

}
