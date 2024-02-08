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

package controllers.productEntry

import connectors.{AlcoholDutyCalculatorConnector, CacheConnector}
import controllers.actions._
import forms.productEntry.TaxTypeFormProvider
import models.AlcoholRegime.{Beer, Cider, OtherFermentedProduct, Spirits, Wine}
import models.productEntry.{ProductEntry, TaxType}
import models.requests.DataRequest
import models.{AlcoholByVolume, AlcoholRegime, Mode, RateBand, RateType}
import navigation.ProductEntryNavigator
import pages.productEntry.{CurrentProductEntryPage, TaxTypePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.TaxTypePageViewModel
import views.html.productEntry.TaxTypeView

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
        case Some(value) => form.fill(value.taxCode)
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
      val (
        abv: AlcoholByVolume,
        eligibleForDraughtRelief: Boolean,
        eligibleForSmallProducerRelief: Boolean,
        rateType: RateType,
        ratePeriod: YearMonth,
        approvedAlcoholRegimes: Set[AlcoholRegime]
      ) = rateParameters(request)

      val rates = alcoholDutyCalculatorConnector.rates(rateType, abv, ratePeriod, approvedAlcoholRegimes)
      form
        .bindFromRequest()
        .fold(
          formWithErrors =>
            rates.map { rates: Seq[RateBand] =>
              BadRequest(
                view(
                  formWithErrors,
                  mode,
                  TaxTypePageViewModel(abv, eligibleForDraughtRelief, eligibleForSmallProducerRelief, rates)
                )
              )

            },
          value => {
            val product = request.userAnswers.get(CurrentProductEntryPage).getOrElse(ProductEntry())
            for {
              taxType        <- taxTypeFromValue(value, rates)
              updatedAnswers <-
                Future.fromTry(
                  request.userAnswers.set(
                    CurrentProductEntryPage,
                    product.copy(taxCode = Some(taxType.code), taxRate = taxType.taxRate, regime = Some(taxType.regime))
                  )
                )
              _              <- cacheConnector.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(TaxTypePage, mode, updatedAnswers))
          }
        )
  }

  private def rateParameters(
    request: DataRequest[AnyContent]
  ): (AlcoholByVolume, Boolean, Boolean, RateType, YearMonth, Set[AlcoholRegime]) = {
    val productEntry: ProductEntry     = request.userAnswers
      .get(CurrentProductEntryPage)
      .getOrElse(throw new RuntimeException("Couldn't fetch currentProductEntry value from cache"))
    val abv                            = productEntry.abv.getOrElse(throw new RuntimeException("Couldn't fetch abv value from cache"))
    val eligibleForDraughtRelief       = productEntry.draughtRelief.getOrElse(
      throw new RuntimeException("Couldn't fetch eligibleForDraughtRelief value from cache")
    )
    val eligibleForSmallProducerRelief = productEntry.smallProducerRelief.getOrElse(
      throw new RuntimeException("Couldn't fetch eligibleForSmallProducerRelief value from cache")
    )

    val rateType: RateType = RateType(eligibleForDraughtRelief, eligibleForSmallProducerRelief)

    //hardcoded for now, will need to get this from obligation period
    val ratePeriod: YearMonth = YearMonth.of(2024, 1)

    //hardcoded for now, will need to get this from subscription data
    val approvedAlcoholRegimes: Set[AlcoholRegime] = Set(Beer, Wine, Cider, Spirits, OtherFermentedProduct)
    (abv, eligibleForDraughtRelief, eligibleForSmallProducerRelief, rateType, ratePeriod, approvedAlcoholRegimes)
  }

  private def taxTypeFromValue(value: String, rates: Future[Seq[RateBand]]): Future[TaxType] =
    for {
      Array(code, regimeStr) <- value.split("_") match {
                                  case Array(code: String, regime: String) => Future.successful(Array(code, regime))
                                  case _                                   => Future.failed(new RuntimeException("Couldn't parse tax type code"))
                                }
      regime: AlcoholRegime  <- AlcoholRegime.fromString(regimeStr) match {
                                  case Some(regime) => Future.successful(regime)
                                  case _            => Future.failed(new RuntimeException("Couldn't parse alcohol regime"))
                                }
      rate                   <- getRate(code, regime, rates)
    } yield TaxType(code, regime, rate)

  private def getRate(code: String, regime: AlcoholRegime, future: Future[Seq[RateBand]]): Future[Option[BigDecimal]] =
    future.map { rates: Seq[RateBand] =>
      rates
        .find(rateBand => code == rateBand.taxType && rateBand.alcoholRegime.contains(regime))
        .flatMap(rateBand => rateBand.rate)
    }

}
