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

import cats.data.NonEmptySeq
import config.FrontendAppConfig
import controllers.actions._
import forms.adjustment.AlcoholicProductTypeFormProvider

import javax.inject.Inject
import models.{ABVRange, AlcoholByVolume, AlcoholRegime, AlcoholType, Mode, RangeDetailsByRegime, RateBand}
import navigation.AdjustmentNavigator
import pages.adjustment.{AlcoholicProductTypePage, CurrentAdjustmentEntryPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import connectors.CacheConnector
import models.AlcoholRegime.{Beer, Cider, Spirits, Wine}
import models.RateType.Core
import models.adjustment.AdjustmentEntry
import play.api.Logging
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.adjustment.AlcoholicProductTypeView

import java.time.YearMonth
import scala.concurrent.{ExecutionContext, Future}

class AlcoholicProductTypeController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       cacheConnector: CacheConnector,
                                       navigator: AdjustmentNavigator,
                                       identify: IdentifyWithEnrolmentAction,
                                       getData: DataRetrievalAction,
                                       requireData: DataRequiredAction,
                                       formProvider: AlcoholicProductTypeFormProvider,
                                       val controllerComponents: MessagesControllerComponents,
                                       view: AlcoholicProductTypeView,
                                       appConfig: FrontendAppConfig
                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Logging {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val spoiltRegimeOpt = request.userAnswers.get(CurrentAdjustmentEntryPage).flatMap(_.spoiltRegime)
      val preparedForm = spoiltRegimeOpt match {
        case None => form
        case Some(value) => form.fill(value.toString)
      }

      Ok(view(preparedForm, mode, request.userAnswers.regimes))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode, request.userAnswers.regimes))),

        value =>{
          val regime : AlcoholRegime = AlcoholRegime.fromString(value).getOrElse(Beer)//change
          val rateBand = createRateBandFromSelectedOption(regime)
          val adjustment                      = request.userAnswers.get(CurrentAdjustmentEntryPage).getOrElse(AdjustmentEntry())
          for {
            //updatedAnswers <- Future.fromTry(request.userAnswers.set(AlcoholicProductTypePage, regime))
            updatedAnswers <- Future.fromTry(request.userAnswers.set(CurrentAdjustmentEntryPage, adjustment.copy(spoiltRegime = Some(regime), rateBand = Some(rateBand), period = Some(YearMonth.of(2024, 1)))))//change to the correct period
            _              <- cacheConnector.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(AlcoholicProductTypePage, mode, updatedAnswers))
        }
      )

  }

  private def createRateBandFromSelectedOption(regime : AlcoholRegime): RateBand = {

    val (taxTypeCode, description, alcoholType) = regime match {
      case Beer => (appConfig.spoiltBeerTaxTypeCode,"Beer", AlcoholType.Beer)
      case Cider => (appConfig.spoiltCiderTaxTypeCode, "Cider",AlcoholType.Cider)
      case Wine => (appConfig.spoiltWineTaxTypeCode, "Wine", AlcoholType.Wine)
      case Spirits => (appConfig.spoiltSpiritsTaxTypeCode, "Spirits", AlcoholType.Spirits)
      case _ => (appConfig.spoiltOtherFermentedProductsTaxTypeCode, "Other fertmented products", AlcoholType.OtherFermentedProduct)
    }

    val rate : BigDecimal = appConfig.spoiltRate

    RateBand(taxTypeCode,description,Core,Some(rate),Set(RangeDetailsByRegime(regime, NonEmptySeq.one(ABVRange(alcoholType,AlcoholByVolume(0),AlcoholByVolume(0))))))
    //fetch description from messages, confirm rateType, and other rateband details
  }
}
