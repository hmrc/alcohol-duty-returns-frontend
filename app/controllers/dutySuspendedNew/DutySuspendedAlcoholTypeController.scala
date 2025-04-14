/*
 * Copyright 2025 HM Revenue & Customs
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

package controllers.dutySuspendedNew

import connectors.UserAnswersConnector
import controllers.actions._
import forms.dutySuspendedNew.DutySuspendedAlcoholTypeFormProvider
import models.AlcoholRegime._
import models.{AlcoholRegime, Mode}
import navigation.DutySuspendedNavigator
import pages.dutySuspendedNew.DutySuspendedAlcoholTypePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.dutySuspendedNew.DutySuspendedAlcoholTypeView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DutySuspendedAlcoholTypeController @Inject() (
  override val messagesApi: MessagesApi,
  userAnswersConnector: UserAnswersConnector,
  navigator: DutySuspendedNavigator,
  identify: IdentifyWithEnrolmentAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  checkDSDNewJourneyToggle: CheckDSDNewJourneyToggleAction,
  formProvider: DutySuspendedAlcoholTypeFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: DutySuspendedAlcoholTypeView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen checkDSDNewJourneyToggle) { implicit request =>
      val preparedForm = request.userAnswers.get(DutySuspendedAlcoholTypePage) match {
        case None        => form
        case Some(value) => form.fill(value.map(_.entryName))
      }

      Ok(view(preparedForm, mode, request.userAnswers.regimes))
    }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen checkDSDNewJourneyToggle).async { implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, request.userAnswers.regimes))),
          value => {
            val alcoholRegimes: Set[AlcoholRegime] = value.flatMap(AlcoholRegime.fromString)
//            val regimesToRemove: Set[AlcoholRegime] = request.userAnswers.regimes.regimes.diff(alcoholRegimes)
            val regimesToAdd: Set[AlcoholRegime]   = alcoholRegimes.diff(request.userAnswers.regimes.regimes)
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(DutySuspendedAlcoholTypePage, alcoholRegimes))
              _              <- userAnswersConnector.set(updatedAnswers)
            } yield Redirect(
              navigator.nextPage(DutySuspendedAlcoholTypePage, mode, updatedAnswers, Some(regimesToAdd.nonEmpty))
            )
          }
        )
    }

//  private def clearSubmittedQuantitiesForRemovedRegimes(
//    userAnswers: UserAnswers,
//    regimesToRemove: Set[AlcoholRegime]
//  ): Try[UserAnswers] =
//    regimesToRemove.foldLeft(Try(userAnswers)) { (currentUserAnswers, regime) =>
//      currentUserAnswers.flatMap(currentAnswers => currentAnswers.removeByKey(DutySuspendedQuantitiesPage, regime))
//    }
//
//  private def clearCalculatedQuantitiesForRemovedRegimes(
//    userAnswers: UserAnswers,
//    regimesToRemove: Set[AlcoholRegime]
//  ): Try[UserAnswers] =
//    for {
//      removedBeer    <- removeCalculatedBeer(userAnswers, regimesToRemove)
//      removedCider   <- removeCalculatedCider(removedBeer, regimesToRemove)
//      removedWine    <- removeCalculatedWine(removedCider, regimesToRemove)
//      removedSpirits <- removeCalculatedSpirits(removedWine, regimesToRemove)
//      removedOFP     <- removeCalculatedOFP(removedSpirits, regimesToRemove)
//    } yield removedOFP
//
//  private def removeCalculatedBeer(userAnswers: UserAnswers, regimesToRemove: Set[AlcoholRegime]): Try[UserAnswers] =
//    if (regimesToRemove.contains(Beer)) userAnswers.remove(DutySuspendedBeerPage) else Success(userAnswers)
//
//  private def removeCalculatedCider(userAnswers: UserAnswers, regimesToRemove: Set[AlcoholRegime]): Try[UserAnswers] =
//    if (regimesToRemove.contains(Cider)) userAnswers.remove(DutySuspendedCiderPage) else Success(userAnswers)
//
//  private def removeCalculatedWine(userAnswers: UserAnswers, regimesToRemove: Set[AlcoholRegime]): Try[UserAnswers] =
//    if (regimesToRemove.contains(Wine)) userAnswers.remove(DutySuspendedWinePage) else Success(userAnswers)
//
//  private def removeCalculatedSpirits(userAnswers: UserAnswers, regimesToRemove: Set[AlcoholRegime]): Try[UserAnswers] =
//    if (regimesToRemove.contains(Spirits)) userAnswers.remove(DutySuspendedSpiritsPage) else Success(userAnswers)
//
//  private def removeCalculatedOFP(userAnswers: UserAnswers, regimesToRemove: Set[AlcoholRegime]): Try[UserAnswers] =
//    if (regimesToRemove.contains(OtherFermentedProduct)) userAnswers.remove(DutySuspendedOtherFermentedPage)
//    else Success(userAnswers)
}
