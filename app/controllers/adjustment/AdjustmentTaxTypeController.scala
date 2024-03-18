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
import forms.adjustment.AdjustmentTaxTypeFormProvider

import javax.inject.Inject
import models.Mode
import navigation.AdjustmentNavigator
import pages.adjustment.AdjustmentTaxTypePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import connectors.CacheConnector
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.adjustment.AdjustmentTaxTypeView

import java.time.YearMonth
import scala.concurrent.{ExecutionContext, Future}

class AdjustmentTaxTypeController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       cacheConnector: CacheConnector,
                                       navigator: AdjustmentNavigator,
                                       identify: IdentifierAction,
                                       getData: DataRetrievalAction,
                                       requireData: DataRequiredAction,
                                       formProvider: AdjustmentTaxTypeFormProvider,
                                       val controllerComponents: MessagesControllerComponents,
                                       view: AdjustmentTaxTypeView
                                      )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      val preparedForm = request.userAnswers.get(AdjustmentTaxTypePage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode))),

        value =>
          for {
            validFlag      <- fetchTaxTypeValidity(value)
            updatedAnswers <- Future.fromTry(request.userAnswers.set(AdjustmentTaxTypePage, value))
            _              <- cacheConnector.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(AdjustmentTaxTypePage, mode, updatedAnswers))
      )
  }

  def fetchTaxTypeValidity(taxCode: Int): (Future[Boolean]) ={
    //hardcoded for now, will need to get this from obligation period
    val ratePeriod: YearMonth = YearMonth.of(2024, 1)
    for {
      validFlag <- alcoholDutyCalculatorConnector.validateTaxType(taxCode, ratePeriod)
    } yield validFlag
  }
}
