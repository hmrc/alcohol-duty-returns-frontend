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

import connectors.UserAnswersConnector
import controllers.actions._
import forms.declareDuty.MultipleSPRListFormProvider
import models.{AlcoholRegime, NormalMode, RateBand, UserAnswers}
import navigation.ReturnsNavigator
import pages.declareDuty.{DoYouWantToAddMultipleSPRToListPage, MissingRateBandsPage, MultipleSPRListPage, TellUsAboutMultipleSPRRatePage, WhatDoYouNeedToDeclarePage}
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.declareDuty.MultipleSPRListHelper
import views.html.declareDuty.MultipleSPRListView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Success, Try}

class MultipleSPRListController @Inject() (
  override val messagesApi: MessagesApi,
  userAnswersConnector: UserAnswersConnector,
  navigator: ReturnsNavigator,
  identify: IdentifyWithEnrolmentAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: MultipleSPRListFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: MultipleSPRListView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  val form = formProvider()

  def onPageLoad(regime: AlcoholRegime): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      MultipleSPRListHelper
        .sprTableViewModel(request.userAnswers, regime)
        .fold(
          error => {
            logger.warn(s"Failed to create SPR table: $error")
            Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
          },
          sprTable => Ok(view(form, regime, sprTable))
        )
    }

  def onSubmit(regime: AlcoholRegime): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors =>
            MultipleSPRListHelper
              .sprTableViewModel(request.userAnswers, regime)
              .fold(
                error => {
                  logger.warn(s"Failed to create SPR table: $error")
                  Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
                },
                sprTable => Future.successful(BadRequest(view(formWithErrors, regime, sprTable)))
              ),
          value =>
            for {
              updatedUserAnswers              <-
                Future.fromTry(request.userAnswers.setByKey(DoYouWantToAddMultipleSPRToListPage, regime, value))
              userAnswersWithMissingRateBands <-
                Future.fromTry(updateMissingRateBands(value, updatedUserAnswers, regime))
              cleanedUserAnswers              <-
                Future.fromTry(userAnswersWithMissingRateBands.removeByKey(TellUsAboutMultipleSPRRatePage, regime))
              _                               <- userAnswersConnector.set(cleanedUserAnswers)
            } yield Redirect(
              navigator.nextPageWithRegime(DoYouWantToAddMultipleSPRToListPage, NormalMode, cleanedUserAnswers, regime)
            )
        )
    }

  private def updateMissingRateBands(
    selectedYes: Boolean,
    userAnswers: UserAnswers,
    regime: AlcoholRegime
  ): Try[UserAnswers] =
    if (selectedYes) { Success(userAnswers) }
    else {
      val selectedSPRRateBands: Set[RateBand] =
        userAnswers.getByKey(WhatDoYouNeedToDeclarePage, regime).map(_.filter(_.rateType.isSPR)).getOrElse(Set.empty)
      val declaredSPRTaxTypes: Seq[String]    =
        userAnswers.getByKey(MultipleSPRListPage, regime).map(_.map(_.taxType)).getOrElse(Seq.empty)
      val missingSPRRateBands: Set[RateBand]  =
        selectedSPRRateBands.filter(rateBand => !declaredSPRTaxTypes.contains(rateBand.taxTypeCode))

      userAnswers.setByKey(MissingRateBandsPage, regime, missingSPRRateBands)
    }
}
