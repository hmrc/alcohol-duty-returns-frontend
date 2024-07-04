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
import models.returns.{VolumeAndRateByTaxType, VolumesByTaxType}
import play.api.Logging
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.returns.CategoriesByRateTypeHelper
import views.html.returns.HowMuchDoYouNeedToDeclareView

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

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
    with I18nSupport
    with Logging {

  def onPageLoad(mode: Mode, regime: AlcoholRegime): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      val form = formProvider(regime)
      request.userAnswers.getByKey(WhatDoYouNeedToDeclarePage, regime) match {
        case None            => Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
        case Some(rateBands) =>
          val howMuchDoYouNeedToDeclareHelper = CategoriesByRateTypeHelper.rateBandCategories(rateBands)
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
          val howMuchDoYouNeedToDeclareHelper = CategoriesByRateTypeHelper.rateBandCategories(rateBands)
          formProvider(regime)
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
                } yield Redirect(
                  navigator.nextPageWithRegime(HowMuchDoYouNeedToDeclarePage, mode, updatedAnswers, regime)
                )
            )
      }
    }

  private def rateBandFromTaxType(
    volumesByTaxType: Seq[VolumesByTaxType],
    rateBands: Set[RateBand]
  ): Try[Seq[VolumeAndRateByTaxType]] =
    volumesByTaxType
      .map { volumes =>
        for {
          rateBand <- rateBands.find(_.taxTypeCode == volumes.taxType)
          dutyRate <- rateBand.rate
        } yield VolumeAndRateByTaxType(
          taxType = volumes.taxType,
          totalLitres = volumes.totalLitres,
          pureAlcohol = volumes.pureAlcohol,
          dutyRate = dutyRate
        )
      }
      .foldLeft(Success(Seq.empty): Try[Seq[VolumeAndRateByTaxType]]) {
        case (acc, Some(value)) => acc.map(_ :+ value)
        case (_, None)          =>
          logger.warn(s"Failed to find rate band for tax type")
          Failure(new Exception("Failed to find rate band for tax type"))
      }
}
