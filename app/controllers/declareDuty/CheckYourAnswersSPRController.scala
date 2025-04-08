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

package controllers.declareDuty

import connectors.UserAnswersConnector
import controllers.actions._
import models.declareDuty.VolumeAndRateByTaxType
import models.{AlcoholRegime, UserAnswers}
import pages.declareDuty.{MultipleSPRListPage, TellUsAboutMultipleSPRRatePage}
import play.api.Logging

import javax.inject.Inject
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.declareDuty.TellUsAboutMultipleSPRRateSummary
import views.html.declareDuty.CheckYourAnswersSPRView

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class CheckYourAnswersSPRController @Inject() (
  override val messagesApi: MessagesApi,
  userAnswersConnector: UserAnswersConnector,
  identify: IdentifyWithEnrolmentAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  tellUsAboutMultipleSPRRateSummary: TellUsAboutMultipleSPRRateSummary,
  view: CheckYourAnswersSPRView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(regime: AlcoholRegime, index: Option[Int]): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      tellUsAboutMultipleSPRRateSummary.rows(regime, request.userAnswers, index) match {
        case Seq() =>
          logger.warn("No SPR summary list items found")
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
        case rows  =>
          Ok(view(regime, SummaryList(rows), index))
      }
    }

  def onSubmit(regime: AlcoholRegime, index: Option[Int]): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      request.userAnswers.getByKey(TellUsAboutMultipleSPRRatePage, regime) match {
        case None               =>
          logger.warn("Impossible to retrieve TellUsAboutMultipleSPRRatePage from userAnswers")
          Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
        case Some(sprRateEntry) =>
          for {
            updatedUserAnswers <- Future.fromTry(updateUserAnswer(request.userAnswers, regime, sprRateEntry, index))
            cleanedUserAnswers <- Future.fromTry(updatedUserAnswers.removeByKey(TellUsAboutMultipleSPRRatePage, regime))
            _                  <- userAnswersConnector.set(cleanedUserAnswers)
          } yield Redirect(controllers.declareDuty.routes.MultipleSPRListController.onPageLoad(regime))
      }
    }

  def updateUserAnswer(
    userAnswers: UserAnswers,
    regime: AlcoholRegime,
    sprRateEntry: VolumeAndRateByTaxType,
    index: Option[Int]
  ): Try[UserAnswers] =
    index match {
      case Some(i) => userAnswers.setByKeyAndIndex(MultipleSPRListPage, regime, sprRateEntry, i)
      case None    => userAnswers.addToSeqByKey(MultipleSPRListPage, regime, sprRateEntry)
    }

}
