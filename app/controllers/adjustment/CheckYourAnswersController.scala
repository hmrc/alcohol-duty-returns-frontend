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

import connectors.UserAnswersConnector
import controllers.actions._
import models.UserAnswers
import models.adjustment.AdjustmentEntry
import pages.adjustment.{AdjustmentEntryListPage, CurrentAdjustmentEntryPage}
import play.api.Logging

import javax.inject.Inject
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Request, Result}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.adjustment.CheckYourAnswersSummaryListHelper
import views.html.adjustment.CheckYourAnswersView

import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  userAnswersConnector: UserAnswersConnector,
  identify: IdentifyWithEnrolmentAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view: CheckYourAnswersView,
  checkYourAnswersSummaryListHelper: CheckYourAnswersSummaryListHelper
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(index: Option[Int] = None): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val result = for {
        adjustmentEntry <- getAdjustmentEntry(request.userAnswers, index)
        summaryList     <- checkYourAnswersSummaryListHelper.currentAdjustmentEntrySummaryList(adjustmentEntry)
      } yield setCurrentAdjustmentEntry(request.userAnswers, adjustmentEntry, summaryList)

      result.getOrElse {
        logger.warn("Couldn't create the summaryList from user answers")
        Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
      }
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    request.userAnswers.get(CurrentAdjustmentEntryPage) match {
      case Some(adjustmentEntry) if adjustmentEntry.isComplete =>
        for {
          updatedAnswers <- saveAdjustmentEntry(request.userAnswers, adjustmentEntry)
          cleanedAnswers <- Future.fromTry(updatedAnswers.remove(CurrentAdjustmentEntryPage))
          _              <- userAnswersConnector.set(cleanedAnswers)
        } yield Redirect(
          controllers.adjustment.routes.AdjustmentListController.onPageLoad(1)
        )
      case Some(_)                                             =>
        logger.warn("Adjustment Entry not completed")
        Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
      case _                                                   =>
        logger.warn("Couldn't fetch currentAdjustmentEntry from user answers")
        Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
    }
  }

  private def getAdjustmentEntry(answers: UserAnswers, index: Option[Int]): Option[AdjustmentEntry] = {
    val ae = index match {
      case Some(i) => answers.getByIndex(AdjustmentEntryListPage, i)
      case None    => answers.get(CurrentAdjustmentEntryPage)
    }
    ae.map(ae => if (ae.index.isDefined) ae else ae.copy(index = index))
  }

  private def saveAdjustmentEntry(
    answers: UserAnswers,
    adjustmentEntry: AdjustmentEntry
  ): Future[UserAnswers] =
    adjustmentEntry.index match {
      case Some(i) => Future.fromTry(answers.setByIndex(AdjustmentEntryListPage, i, adjustmentEntry.copy(index = None)))
      case None    => Future.fromTry(answers.addToSeq(AdjustmentEntryListPage, adjustmentEntry))
    }

  private def setCurrentAdjustmentEntry(
    userAnswers: UserAnswers,
    adjustmentEntry: AdjustmentEntry,
    summaryList: SummaryList
  )(implicit
    request: Request[_]
  ): Future[Result] =
    for {
      updateUserAnswers <- Future.fromTry(userAnswers.set(CurrentAdjustmentEntryPage, adjustmentEntry))
      _                 <- userAnswersConnector.set(updateUserAnswers)
    } yield Ok(
      view(
        summaryList
      )
    )

}
