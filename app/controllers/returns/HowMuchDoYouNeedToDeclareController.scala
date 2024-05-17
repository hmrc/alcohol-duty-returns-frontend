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
import forms.returns.HowMuchDoYouNeedToDeclareFormProvider

import javax.inject.Inject
import models.{AlcoholRegime, Mode, RateBand}
import navigation.ReturnsNavigator
import pages.returns.{HowMuchDoYouNeedToDeclarePage, WhatDoYouNeedToDeclarePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import connectors.CacheConnector
import models.returns.{DutyByTaxType, VolumesByTaxType}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.returns.HowMuchDoYouNeedToDeclareHelper
import views.html.returns.HowMuchDoYouNeedToDeclareView

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class HowMuchDoYouNeedToDeclareController @Inject() (
  override val messagesApi: MessagesApi,
  cacheConnector: CacheConnector,
  navigator: ReturnsNavigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: HowMuchDoYouNeedToDeclareFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: HowMuchDoYouNeedToDeclareView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode, regime: AlcoholRegime): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      request.userAnswers.getByKey(WhatDoYouNeedToDeclarePage, regime) match {
        case None            => Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
        case Some(rateBands) =>
          val howMuchDoYouNeedToDeclareHelper = HowMuchDoYouNeedToDeclareHelper(regime, rateBands)
          val preparedForm                    = request.userAnswers.getByKey(HowMuchDoYouNeedToDeclarePage, regime) match {
            case None        => form
            case Some(value) => form.fill(value.map(_.toVolumes))
          }

          Ok(view(preparedForm, regime, howMuchDoYouNeedToDeclareHelper, mode))
      }

    }

  def onSubmit(mode: Mode, regime: AlcoholRegime): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      request.userAnswers.getByKey(WhatDoYouNeedToDeclarePage, regime) match {
        case None            => Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
        case Some(rateBands) =>
          val howMuchDoYouNeedToDeclareHelper = HowMuchDoYouNeedToDeclareHelper(regime, rateBands)
          form
            .bindFromRequest()
            .fold(
              formWithErrors =>
                Future.successful(BadRequest(view(formWithErrors, regime, howMuchDoYouNeedToDeclareHelper, mode))),
              value =>
                for {
                  dutyByTaxTypes <- Future.fromTry(rateBandFromTaxType(value, rateBands))
                  updatedAnswers <-
                    Future.fromTry(request.userAnswers.setByKey(HowMuchDoYouNeedToDeclarePage, regime, dutyByTaxTypes))
                  _              <- cacheConnector.set(updatedAnswers)
                } yield Redirect(navigator.nextPage(HowMuchDoYouNeedToDeclarePage, mode, updatedAnswers))
            )
      }
    }

  private def rateBandFromTaxType(
    volumeByTaxType: Seq[VolumesByTaxType],
    rateBands: Set[RateBand]
  ): Try[Seq[DutyByTaxType]] = Try {
    volumeByTaxType.map { volumes =>
      val rateBand = rateBands
        .find(_.taxType == volumes.taxType)
        .getOrElse(throw new IllegalArgumentException(s"Invalid tax type: ${volumes.taxType}"))
      DutyByTaxType(
        taxType = volumes.taxType,
        totalLitres = volumes.totalLitres,
        pureAlcohol = volumes.pureAlcohol,
        dutyRate = rateBand.rate.getOrElse(
          throw new IllegalArgumentException(s"Rate not found for tax type: ${volumes.taxType}")
        )
      )
    }
  }
}
