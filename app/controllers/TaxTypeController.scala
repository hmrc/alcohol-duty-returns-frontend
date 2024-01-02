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
import models.{Mode, RatePeriod}
import navigation.ProductEntryNavigator
import pages.TaxTypePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.TaxTypePageViewModel
import views.html.TaxTypeView

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

      alcoholDutyCalculatorConnector.rates().map { rates: Seq[RatePeriod] =>
        TaxTypePageViewModel(request.userAnswers, rates) match {
          case None     => Redirect(routes.JourneyRecoveryController.onPageLoad())
          case Some(vm) => Ok(view(preparedForm, mode, vm))
        }
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors =>
            alcoholDutyCalculatorConnector.rates().map { rates: Seq[RatePeriod] =>
              TaxTypePageViewModel(request.userAnswers, rates) match {
                case None     => Redirect(routes.JourneyRecoveryController.onPageLoad())
                case Some(vm) => BadRequest(view(formWithErrors, mode, vm))
              }
            },
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(TaxTypePage, value))
              _              <- cacheConnector.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(TaxTypePage, mode, updatedAnswers))
        )
  }
}
