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

import controllers.actions._
import forms.declareDuty.TellUsAboutSingleSPRRateFormProvider

import javax.inject.Inject
import models.{AlcoholRegime, Mode}
import navigation.ReturnsNavigator
import pages.declareDuty.{TellUsAboutSingleSPRRatePage, WhatDoYouNeedToDeclarePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import connectors.UserAnswersConnector
import handlers.ADRServerException
import models.declareDuty.VolumeAndRateByTaxType
import play.api.Logging
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.declareDuty.CategoriesByRateTypeHelper
import views.html.declareDuty.TellUsAboutSingleSPRRateView

import scala.concurrent.{ExecutionContext, Future}

class TellUsAboutSingleSPRRateController @Inject() (
  override val messagesApi: MessagesApi,
  userAnswersConnector: UserAnswersConnector,
  navigator: ReturnsNavigator,
  identify: IdentifyWithEnrolmentAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: TellUsAboutSingleSPRRateFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: TellUsAboutSingleSPRRateView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging
    with ReturnController[Seq[VolumeAndRateByTaxType], TellUsAboutSingleSPRRatePage.type] {

  val currentPage = TellUsAboutSingleSPRRatePage

  def onPageLoad(mode: Mode, regime: AlcoholRegime): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      val form = formProvider(regime)
      request.userAnswers.getByKey(WhatDoYouNeedToDeclarePage, regime) match {
        case None            =>
          throw ADRServerException(s"Unable to retrieve WhatDoYouNeedToDeclarePage from UserAnswers for page load with regime: $regime $request")
        case Some(rateBands) =>
          val preparedForm               = request.userAnswers.getByKey(currentPage, regime) match {
            case None        => form
            case Some(value) => form.fill(value)
          }
          val categoriesByRateTypeHelper = CategoriesByRateTypeHelper.rateBandCategories(rateBands)
          Ok(view(preparedForm, regime, categoriesByRateTypeHelper, mode))
      }
    }

  def onSubmit(mode: Mode, regime: AlcoholRegime): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      request.userAnswers.getByKey(WhatDoYouNeedToDeclarePage, regime) match {
        case None            =>
          throw ADRServerException(s"Unable to retrieve WhatDoYouNeedToDeclarePage from user answer for page submission with regime: $regime $request")
        case Some(rateBands) =>
          formProvider(regime)
            .bindFromRequest()
            .fold(
              formWithErrors =>
                Future.successful(
                  BadRequest(
                    view(formWithErrors, regime, CategoriesByRateTypeHelper.rateBandCategories(rateBands), mode)
                  )
                ),
              value => {
                val hasChanged = hasValueChanged(value, regime)
                for {
                  updatedAnswers <-
                    Future.fromTry(request.userAnswers.setByKey(currentPage, regime, value))
                  _              <- userAnswersConnector.set(updatedAnswers)
                } yield Redirect(
                  navigator
                    .nextPageWithRegime(currentPage, mode, updatedAnswers, regime, hasChanged)
                )
              }
            )
      }
    }
}
