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

import connectors.{AlcoholDutyCalculatorConnector, UserAnswersConnector}
import controllers.actions._
import forms.declareDuty.WhatDoYouNeedToDeclareFormProvider
import models.requests.DataRequest
import models.{AlcoholRegime, Mode, RateBand, ReturnPeriod, UserAnswers}
import navigation.ReturnsNavigator
import pages.QuestionPage
import pages.declareDuty.{WhatDoYouNeedToDeclarePage, nextPages}
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.declareDuty.TaxBandsViewModel
import views.html.declareDuty.WhatDoYouNeedToDeclareView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class WhatDoYouNeedToDeclareController @Inject() (
  override val messagesApi: MessagesApi,
  userAnswersConnector: UserAnswersConnector,
  calculatorConnector: AlcoholDutyCalculatorConnector,
  navigator: ReturnsNavigator,
  identify: IdentifyWithEnrolmentAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: WhatDoYouNeedToDeclareFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: WhatDoYouNeedToDeclareView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with ReturnController[Set[RateBand], WhatDoYouNeedToDeclarePage.type]
    with Logging {

  val currentPage = WhatDoYouNeedToDeclarePage

  def onPageLoad(mode: Mode, regime: AlcoholRegime): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      val form = formProvider(regime)

      getRateBands(request.userAnswers, request.returnPeriod, regime).map { rateBands =>
        val taxBandsViewModel = TaxBandsViewModel(rateBands, regime)
        val preparedForm      = request.userAnswers.getByKey(currentPage, regime) match {
          case None        => form
          case Some(value) => form.fill(value.map(_.taxTypeCode))
        }

        Ok(view(preparedForm, regime, taxBandsViewModel, mode))
      }
    }

  def onSubmit(mode: Mode, regime: AlcoholRegime): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      getRateBands(request.userAnswers, request.returnPeriod, regime)
        .flatMap { rateBands: Seq[RateBand] =>
          formProvider(regime)
            .bindFromRequest()
            .fold(
              formWithErrors =>
                Future.successful(BadRequest(view(formWithErrors, regime, TaxBandsViewModel(rateBands, regime), mode))),
              value =>
                for {
                  selectedRateBands         <- Future.fromTry(rateBandFromTaxType(value, rateBands))
                  updatedAnswers            <- Future.fromTry(request.userAnswers.setByKey(currentPage, regime, selectedRateBands))
                  (pagesToClear, hasChanged) = changedValue(selectedRateBands, regime)
                  clearedAnswers            <- Future.fromTry(updatedAnswers.removePagesByKey(pagesToClear, regime))
                  _                         <- userAnswersConnector.set(clearedAnswers)
                } yield Redirect(
                  navigator
                    .nextPageWithRegime(currentPage, mode, updatedAnswers, regime, hasChanged)
                )
            )
        }
        .recover(
          { case e =>
            logger.warn("Alcohol Type unselected", e)
            Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
          }
        )
    }

  private def getRateBands(
    userAnswers: UserAnswers,
    returnPeriod: ReturnPeriod,
    selectedRegime: AlcoholRegime
  )(implicit hc: HeaderCarrier): Future[Seq[RateBand]] =
    calculatorConnector
      .rateBandByRegime(returnPeriod.period, userAnswers.regimes.regimes.toSeq)
      .map(rateBands => rateBands.filter(_.rangeDetails.map(_.alcoholRegime).contains(selectedRegime)))

  private def rateBandFromTaxType(rateBandTaxTypes: Set[String], rateBands: Seq[RateBand]): Try[Set[RateBand]] = Try {
    rateBandTaxTypes.map { taxType =>
      rateBands
        .find(_.taxTypeCode == taxType)
        .getOrElse(throw new IllegalArgumentException(s"Invalid tax type: $taxType"))
    }
  }

  private def changedValue(newRateBands: Set[RateBand], regime: AlcoholRegime)(implicit
    request: DataRequest[AnyContent]
  ): (Seq[_ <: QuestionPage[Map[AlcoholRegime, _]]], Boolean) =
    request.userAnswers.getByKey(currentPage, regime) match {
      case Some(oldRateBands) if oldRateBands != newRateBands =>
        (nextPages(currentPage), true)
      case _                                                  =>
        (Seq.empty, false)
    }
}
