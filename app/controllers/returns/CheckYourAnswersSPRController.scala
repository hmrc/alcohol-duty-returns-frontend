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

package controllers.returns

import connectors.CacheConnector
import controllers.actions._
import models.returns.VolumeAndRateByTaxType
import models.{AlcoholRegime, UserAnswers}
import pages.returns.{MultipleSPRListPage, TellUsAboutMultipleSPRRatePage}
import play.api.Logging

import javax.inject.Inject
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.returns.CheckYourAnswersSPRSummaryListHelper
import views.html.returns.CheckYourAnswersSPRView

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class CheckYourAnswersSPRController @Inject() (
  override val messagesApi: MessagesApi,
  cacheConnector: CacheConnector,
  identify: IdentifyWithEnrolmentAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view: CheckYourAnswersSPRView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(regime: AlcoholRegime, index: Option[Int]): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      CheckYourAnswersSPRSummaryListHelper.summaryList(regime, request.userAnswers, index) match {
        case None              =>
          logger.warn("Impossible to retrieve summary list")
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
        case Some(summaryList) =>
          Ok(view(regime, summaryList, index))
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
            _                  <- cacheConnector.set(cleanedUserAnswers)
          } yield Redirect(controllers.returns.routes.MultipleSPRListController.onPageLoad(regime))
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
