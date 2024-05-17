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
import models.{AlcoholRegime, Mode, RateBand, ReturnPeriod, UserAnswers}
import navigation.ReturnsNavigator
import pages.returns.{RateBandsPage, WhatDoYouNeedToDeclarePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import connectors.{AlcoholDutyCalculatorConnector, CacheConnector}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.returns.TaxBandsViewModel
import views.html.returns.WhatDoYouNeedToDeclareView

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class WhatDoYouNeedToDeclareController @Inject() (
  override val messagesApi: MessagesApi,
  cacheConnector: CacheConnector,
  calculatorConnector: AlcoholDutyCalculatorConnector,
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
    (identify andThen getData andThen requireData).async { implicit request =>
      getRateBands(request.userAnswers, request.returnPeriod, request.regimes, regime).map { rateBands: Seq[RateBand] =>
        val taxBandsViewModel = TaxBandsViewModel(rateBands)
        val preparedForm      = request.userAnswers.getByKey(WhatDoYouNeedToDeclarePage, regime) match {
          case None        => form
          case Some(value) => form.fill(value.map(_.taxType))
        }

        Ok(view(preparedForm, regime, taxBandsViewModel, mode))
      }
    }

  def onSubmit(mode: Mode, regime: AlcoholRegime): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      getRateBands(request.userAnswers, request.returnPeriod, request.regimes, regime).flatMap {
        rateBands: Seq[RateBand] =>
          val taxBandsViewModel = TaxBandsViewModel(rateBands)
          form
            .bindFromRequest()
            .fold(
              formWithErrors => Future.successful(BadRequest(view(formWithErrors, regime, taxBandsViewModel, mode))),
              value =>
                for {
                  rateBands      <- Future.fromTry(rateBandFromTaxType(value, rateBands))
                  updatedAnswers <-
                    Future.fromTry(request.userAnswers.setByKey(WhatDoYouNeedToDeclarePage, regime, rateBands))
                  _              <- cacheConnector.set(updatedAnswers)
                } yield Redirect(navigator.nextPageWithRegime(WhatDoYouNeedToDeclarePage, mode, updatedAnswers, regime))
            )
      }
    }

  private def getRateBands(
    userAnswers: UserAnswers,
    returnPeriod: ReturnPeriod,
    approvedAlcoholRegimes: Seq[AlcoholRegime],
    selectedRegime: AlcoholRegime
  )(implicit hc: HeaderCarrier): Future[Seq[RateBand]] =
    userAnswers.get(RateBandsPage) match {
      case Some(rateBands) => Future.successful(rateBands.filter(_.alcoholRegime.contains(selectedRegime)))
      case None            =>
        for {
          rateBands      <- calculatorConnector.rateBandByRegime(returnPeriod.period, approvedAlcoholRegimes)
          updatedAnswers <- Future.fromTry(userAnswers.set(RateBandsPage, rateBands))
          _              <- cacheConnector.set(updatedAnswers)
        } yield rateBands.filter(_.alcoholRegime.contains(selectedRegime))
    }

  private def rateBandFromTaxType(rateBandTaxTypes: Set[String], rateBands: Seq[RateBand]): Try[Set[RateBand]] = Try {
    rateBandTaxTypes.map { taxType =>
      rateBands.find(_.taxType == taxType).getOrElse(throw new IllegalArgumentException(s"Invalid tax type: $taxType"))
    }
  }
}