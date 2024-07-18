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
import models.{AlcoholRegime, CheckMode, Mode, NormalMode, UserAnswers}
import navigation.ReturnsNavigator
import pages.returns.{MultipleSPRListPage, TellUsAboutMultipleSPRRatePage, WhatDoYouNeedToDeclarePage}
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import connectors.CacheConnector
import models.returns.VolumeAndRateByTaxType
import play.api.Logging
import play.api.data.Form
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.returns.TellUsAboutMultipleSPRRateHelper
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
    with I18nSupport
    with Logging {

  def onPageLoad(mode: Mode, regime: AlcoholRegime, index: Option[Int]): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      val result = for {
        rateBands <- request.userAnswers.getByKey(WhatDoYouNeedToDeclarePage, regime)
        form      <- prepareForm(request.userAnswers, regime, mode, index)
      } yield Ok(view(form, mode, regime, TellUsAboutMultipleSPRRateHelper.radioItems(rateBands), index))

      result.getOrElse(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
    }

  def onSubmit(mode: Mode, regime: AlcoholRegime, index: Option[Int]): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      request.userAnswers.getByKey(WhatDoYouNeedToDeclarePage, regime) match {
        case None            =>
          Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
        case Some(rateBands) =>
          formProvider(regime)
            .bindFromRequest()
            .fold(
              formWithErrors =>
                Future.successful(
                  BadRequest(
                    view(
                      formWithErrors,
                      mode,
                      regime,
                      TellUsAboutMultipleSPRRateHelper.radioItems(rateBands),
                      index
                    )
                  )
                ),
              value => {
                val hasChanged = hasValueChanged(regime, index, request.userAnswers, mode, value)
                for {
                  updatedAnswers <-
                    Future.fromTry(request.userAnswers.setByKey(TellUsAboutMultipleSPRRatePage, regime, value))
                  _              <- cacheConnector.set(updatedAnswers)
                } yield Redirect(
                  navigator
                    .nextPageWithRegime(TellUsAboutMultipleSPRRatePage, mode, updatedAnswers, regime, hasChanged, index)
                )
              }
            )
      }
    }

  private def hasValueChanged(
    regime: AlcoholRegime,
    index: Option[Int],
    userAnswers: UserAnswers,
    mode: Mode,
    value: VolumeAndRateByTaxType
  ) =
    (mode, index) match {
      case (CheckMode, _)        => true
      case (NormalMode, Some(i)) => hasValueChangedAtIndex(userAnswers, regime, i, value)
      case _                     => false
    }

  private def hasValueChangedAtIndex(
    userAnswers: UserAnswers,
    regime: AlcoholRegime,
    i: Int,
    value: VolumeAndRateByTaxType
  ) =
    userAnswers.getByKeyAndIndex(MultipleSPRListPage, regime, i) match {
      case Some(existingValue) => existingValue != value
      case None                => false
    }

  private def prepareForm(
    userAnswers: UserAnswers,
    regime: AlcoholRegime,
    mode: Mode,
    index: Option[Int]
  )(implicit messages: Messages): Option[Form[_]] =
    (mode, index) match {
      case (NormalMode, Some(i)) => fillPreviousAnswersWithIndex(userAnswers, regime, i)
      case _                     => fillPreviousAnswers(userAnswers, regime)
    }

  private def fillPreviousAnswersWithIndex(answers: UserAnswers, regime: AlcoholRegime, i: Int)(implicit
    messages: Messages
  ): Option[Form[_]] =
    answers.getByKeyAndIndex(MultipleSPRListPage, regime, i) match {
      case Some(value) => Some(formProvider(regime).fill(value))
      case None        =>
        logger.warn(s"Failed to retrieve SPR list entry for regime $regime at index $i")
        None
    }

  private def fillPreviousAnswers(answers: UserAnswers, regime: AlcoholRegime)(implicit
    messages: Messages
  ): Option[Form[_]] = {
    val form = formProvider(regime)
    answers.getByKey(TellUsAboutMultipleSPRRatePage, regime) match {
      case Some(value) => Some(form.fill(value))
      case None        => Some(form)
    }
  }
}
