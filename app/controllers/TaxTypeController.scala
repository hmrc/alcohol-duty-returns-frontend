/*
 * Copyright 2023 HM Revenue & Customs
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

package controllers

import connectors.{AlcoholDutyCalculatorConnector, CacheConnector}
import controllers.actions._
import forms.TaxTypeFormProvider
import models.AlcoholRegime.{Beer, Cider, Spirits, Wine}
import models.requests.DataRequest
import models.{AlcoholByVolume, AlcoholRegime, Mode, RateBand, RateType}
import navigation.ProductEntryNavigator
import pages.{AlcoholByVolumeQuestionPage, DraughtReliefQuestionPage, SmallProducerReliefQuestionPage, TaxTypePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.TaxTypePageViewModel
import views.html.TaxTypeView

import java.time.YearMonth
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TaxTypeController @Inject() (
  override val messagesApi: MessagesApi,
  cacheConnector: CacheConnector,
  alcoholDutyCalculatorConnector: AlcoholDutyCalculatorConnector,
  navigator: ProductEntryNavigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: TaxTypeFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: TaxTypeView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val preparedForm = request.userAnswers.get(TaxTypePage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      val (
        abv: AlcoholByVolume,
        eligibleForDraughtRelief: Boolean,
        eligibleForSmallProducerRelief: Boolean,
        rateType: RateType,
        ratePeriod: YearMonth,
        approvedAlcoholRegimes: Set[AlcoholRegime]
      ) = rateParameters(request)

      alcoholDutyCalculatorConnector.rates(rateType, abv, ratePeriod, approvedAlcoholRegimes).map {
        rates: Seq[RateBand] =>
          Ok(
            view(
              preparedForm,
              mode,
              TaxTypePageViewModel(abv, eligibleForDraughtRelief, eligibleForSmallProducerRelief, rates)
            )
          )
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => {
            val (
              abv: AlcoholByVolume,
              eligibleForDraughtRelief: Boolean,
              eligibleForSmallProducerRelief: Boolean,
              rateType: RateType,
              ratePeriod: YearMonth,
              approvedAlcoholRegimes: Set[AlcoholRegime]
            ) = rateParameters(request)

            alcoholDutyCalculatorConnector.rates(rateType, abv, ratePeriod, approvedAlcoholRegimes).map {
              rates: Seq[RateBand] =>
                BadRequest(
                  view(
                    formWithErrors,
                    mode,
                    TaxTypePageViewModel(abv, eligibleForDraughtRelief, eligibleForSmallProducerRelief, rates)
                  )
                )

            }
          },
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(TaxTypePage, value))
              _              <- cacheConnector.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(TaxTypePage, mode, updatedAnswers))
        )
  }

  private def rateParameters(
    request: DataRequest[AnyContent]
  ): (AlcoholByVolume, Boolean, Boolean, RateType, YearMonth, Set[AlcoholRegime]) = {
    val abv: AlcoholByVolume                    = AlcoholByVolume(
      request.userAnswers
        .get(AlcoholByVolumeQuestionPage)
        .getOrElse(throw new RuntimeException("Couldn't fetch abv value from cache"))
    )
    val eligibleForDraughtRelief: Boolean       = request.userAnswers
      .get(DraughtReliefQuestionPage)
      .getOrElse(throw new RuntimeException("Couldn't fetch eligibleForDraughtRelief value from cache"))
    val eligibleForSmallProducerRelief: Boolean = request.userAnswers
      .get(SmallProducerReliefQuestionPage)
      .getOrElse(throw new RuntimeException("Couldn't fetch eligibleForSmallProducerRelief value from cache"))

    val rateType: RateType = RateType(eligibleForDraughtRelief, eligibleForSmallProducerRelief)

    //hardcoded for now, will need to get this from obligation period
    val ratePeriod: YearMonth = YearMonth.of(2024, 1)

    //hardcoded for now, will need to get this from subscription data
    val approvedAlcoholRegimes: Set[AlcoholRegime] = Set(Beer, Wine, Cider, Spirits)
    (abv, eligibleForDraughtRelief, eligibleForSmallProducerRelief, rateType, ratePeriod, approvedAlcoholRegimes)
  }
}
