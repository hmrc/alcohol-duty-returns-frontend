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
import forms.returns.TellUsAboutMultipleSPRRateFormProvider

import javax.inject.Inject
import models.{AlcoholRegime, Mode, RateBand}
import navigation.ReturnsNavigator
import pages.returns.{TellUsAboutMultipleSPRRatePage, WhatDoYouNeedToDeclarePage}
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import connectors.CacheConnector
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.returns.CategoriesByRateTypeHelper
import views.html.returns.TellUsAboutMultipleSPRRateView

import scala.concurrent.{ExecutionContext, Future}

class TellUsAboutMultipleSPRRateController @Inject() (
  override val messagesApi: MessagesApi,
  cacheConnector: CacheConnector,
  navigator: ReturnsNavigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: TellUsAboutMultipleSPRRateFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: TellUsAboutMultipleSPRRateView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(mode: Mode, regime: AlcoholRegime): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      val form = formProvider(regime)
      request.userAnswers.getByKey(WhatDoYouNeedToDeclarePage, regime) match {
        case None            => Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
        case Some(rateBands) =>
          val preparedForm = request.userAnswers.getByKey(TellUsAboutMultipleSPRRatePage, regime) match {
            case None        => form
            case Some(value) => form.fill(value)
          }
          Ok(view(preparedForm, mode, regime, radioItems(regime, rateBands)))
      }
    }

  def onSubmit(mode: Mode, regime: AlcoholRegime): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      request.userAnswers.getByKey(WhatDoYouNeedToDeclarePage, regime) match {
        case None            => Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
        case Some(rateBands) =>
          formProvider(regime)
            .bindFromRequest()
            .fold(
              formWithErrors =>
                Future.successful(BadRequest(view(formWithErrors, mode, regime, radioItems(regime, rateBands)))),
              value =>
                for {
                  updatedAnswers <-
                    Future.fromTry(request.userAnswers.setByKey(TellUsAboutMultipleSPRRatePage, regime, value))
                  _              <- cacheConnector.set(updatedAnswers)
                } yield Redirect(navigator.nextPage(TellUsAboutMultipleSPRRatePage, mode, updatedAnswers))
            )
      }
    }

  private def radioItems(regime: AlcoholRegime, rateBands: Set[RateBand])(implicit
    messages: Messages
  ): Seq[RadioItem] = {
    val categoryViewModels                = CategoriesByRateTypeHelper(regime, rateBands)
    val smallProducerRadioItems           = categoryViewModels.smallProducer
      .map { category =>
        RadioItem(content = Text(category.category), value = Some(category.id))
      }
      .sortBy(_.id)
    val draughtAndSmallProducerRadioItems = categoryViewModels.draughtAndSmallProducer
      .map { category =>
        RadioItem(content = Text(category.category), value = Some(category.id))
      }
      .sortBy(_.id)

    smallProducerRadioItems ++ draughtAndSmallProducerRadioItems
  }
}
