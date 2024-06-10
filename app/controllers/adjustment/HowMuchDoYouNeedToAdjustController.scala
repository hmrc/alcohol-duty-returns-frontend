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

import controllers.actions._
import forms.adjustment.HowMuchDoYouNeedToAdjustFormProvider

import javax.inject.Inject
import models.Mode
import navigation.AdjustmentNavigator
import pages.adjustment.{CurrentAdjustmentEntryPage, HowMuchDoYouNeedToAdjustPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import connectors.CacheConnector
import models.RateType.{DraughtAndSmallProducerRelief, SmallProducerRelief}
import models.adjustment.{AdjustmentEntry, HowMuchDoYouNeedToAdjust}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.adjustment.AdjustmentTypeHelper
import views.html.adjustment.HowMuchDoYouNeedToAdjustView

import scala.concurrent.{ExecutionContext, Future}

class HowMuchDoYouNeedToAdjustController @Inject() (
  override val messagesApi: MessagesApi,
  cacheConnector: CacheConnector,
  navigator: AdjustmentNavigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: HowMuchDoYouNeedToAdjustFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: HowMuchDoYouNeedToAdjustView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val (preparedForm, adjustmentEntry) = request.userAnswers.get(CurrentAdjustmentEntryPage) match {
      case None                  => (form, AdjustmentEntry())
      case Some(adjustmentEntry) =>
        val filledForm = (for {
          totalLitresVolume <- adjustmentEntry.totalLitresVolume
          pureAlcoholVolume <- adjustmentEntry.pureAlcoholVolume
        } yield form.fill(HowMuchDoYouNeedToAdjust(totalLitresVolume, pureAlcoholVolume, adjustmentEntry.sprDutyRate)))
          .getOrElse(form)
        (filledForm, adjustmentEntry)
    }

    Ok(
      view(
        preparedForm,
        mode,
        AdjustmentTypeHelper.getAdjustmentTypeValue(adjustmentEntry),
        checkSprApplicable(adjustmentEntry)
      )
    )
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors =>
            Future.successful(
              BadRequest(view(formWithErrors, mode, AdjustmentTypeHelper.getAdjustmentTypeValue(AdjustmentEntry())))
            ),
          value => {
            val adjustment = request.userAnswers.get(CurrentAdjustmentEntryPage).getOrElse(AdjustmentEntry())
            for {
              updatedAnswers <- Future.fromTry(
                                  request.userAnswers.set(
                                    CurrentAdjustmentEntryPage,
                                    adjustment.copy(
                                      totalLitresVolume = Some(value.totalLitersVolume),
                                      pureAlcoholVolume = Some(value.pureAlcoholVolume),
                                      sprDutyRate = value.sprDutyRate
                                    )
                                  )
                                )
              _              <- cacheConnector.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(HowMuchDoYouNeedToAdjustPage, mode, updatedAnswers))
          }
        )
  }

  private def checkSprApplicable(adjustmentEntry: AdjustmentEntry): Boolean =
    adjustmentEntry.rateType match {
      case Some(DraughtAndSmallProducerRelief) => true
      case Some(SmallProducerRelief)           => true
      case _                                   => false
    }
}
