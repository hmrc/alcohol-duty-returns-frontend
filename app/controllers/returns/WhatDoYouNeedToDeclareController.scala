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

import controllers.actions._
import forms.returns.WhatDoYouNeedToDeclareFormProvider

import javax.inject.Inject
import models.{AlcoholByVolume, AlcoholRegime, Mode, RateBand, RateType}
import navigation.ReturnsNavigator
import pages.returns.WhatDoYouNeedToDeclarePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import connectors.CacheConnector
import models.AlcoholRegime.Beer
import uk.gov.hmrc.govukfrontend.views.viewmodels.checkboxes.CheckboxItem
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.returns.TaxBandsViewModel
import views.html.returns.WhatDoYouNeedToDeclareView

import scala.concurrent.{ExecutionContext, Future}

class WhatDoYouNeedToDeclareController @Inject() (
  override val messagesApi: MessagesApi,
  cacheConnector: CacheConnector,
  navigator: ReturnsNavigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: WhatDoYouNeedToDeclareFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: WhatDoYouNeedToDeclareView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode, regime: AlcoholRegime): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      // TODO: check if user is authorised for this regime

      val taxBandsViewModel = TaxBandsViewModel(bands)
      // TODO: add call for tax bands
      val preparedForm      = request.userAnswers.get(WhatDoYouNeedToDeclarePage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, regime, taxBandsViewModel, mode))
    }

  def onSubmit(mode: Mode, regime: AlcoholRegime): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      val taxBandsViewModel = TaxBandsViewModel(bands)
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, regime, taxBandsViewModel, mode))),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(WhatDoYouNeedToDeclarePage, value))
              _              <- cacheConnector.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(WhatDoYouNeedToDeclarePage, mode, updatedAnswers))
        )
    }
  private val bands                                                   = Seq(
    RateBand(
      "311",
      "some band",
      RateType.Core,
      Set(Beer),
      AlcoholByVolume(1.2),
      AlcoholByVolume(3.4),
      Some(BigDecimal(10))
    ),
    RateBand(
      "321",
      "some band",
      RateType.Core,
      Set(Beer),
      AlcoholByVolume(3.5),
      AlcoholByVolume(8.4),
      Some(BigDecimal(10))
    ),
    RateBand(
      "341",
      "some band",
      RateType.Core,
      Set(Beer),
      AlcoholByVolume(22),
      AlcoholByVolume(100),
      Some(BigDecimal(10))
    ),
    RateBand(
      "351",
      "some band",
      RateType.DraughtRelief,
      Set(Beer),
      AlcoholByVolume(1.2),
      AlcoholByVolume(3.4),
      Some(BigDecimal(10))
    ),
    RateBand(
      "361",
      "some band",
      RateType.SmallProducerRelief,
      Set(Beer),
      AlcoholByVolume(1.2),
      AlcoholByVolume(3.4),
      Some(BigDecimal(10))
    ),
    RateBand(
      "371",
      "some band",
      RateType.DraughtAndSmallProducerRelief,
      Set(Beer),
      AlcoholByVolume(1.2),
      AlcoholByVolume(3.4),
      Some(BigDecimal(10))
    )
  )
}
