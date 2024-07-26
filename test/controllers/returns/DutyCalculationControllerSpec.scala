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

import base.SpecBase
import connectors.{AlcoholDutyCalculatorConnector, CacheConnector}
import models.returns.{AlcoholDuty, DutyByTaxType}
import org.mockito.ArgumentMatchers.any
import pages.returns.{DoYouHaveMultipleSPRDutyRatesPage, DutyCalculationPage, HowMuchDoYouNeedToDeclarePage, WhatDoYouNeedToDeclarePage}
import play.api.inject.bind
import play.api.test.Helpers._
import uk.gov.hmrc.http.HttpResponse
import viewmodels.returns.DutyCalculationHelper
import views.html.returns.DutyCalculationView

import scala.concurrent.Future

class DutyCalculationControllerSpec extends SpecBase {
  val calculatorMock  = mock[AlcoholDutyCalculatorConnector]
  val regime          = regimeGen.sample.value
  val rateBands       = genListOfRateBandForRegime(regime).sample.value.toSet
  val volumesAndRates = arbitraryVolumeAndRateByTaxType(
    rateBands.toSeq
  ).arbitrary.sample.value

  val userAnswers = emptyUserAnswers
    .setByKey(WhatDoYouNeedToDeclarePage, regime, rateBands)
    .success
    .value
    .setByKey(DoYouHaveMultipleSPRDutyRatesPage, regime, false)
    .success
    .value
    .setByKey(HowMuchDoYouNeedToDeclarePage, regime, volumesAndRates)
    .success
    .value

  val dutiesByTaxType = volumesAndRates.map { volumeAndRate =>
    val totalDuty = volumeAndRate.dutyRate * volumeAndRate.pureAlcohol
    DutyByTaxType(
      taxType = volumeAndRate.taxType,
      totalLitres = volumeAndRate.totalLitres,
      pureAlcohol = volumeAndRate.pureAlcohol,
      dutyRate = volumeAndRate.dutyRate,
      dutyDue = totalDuty
    )
  }

  val alcoholDuty = AlcoholDuty(
    dutiesByTaxType = dutiesByTaxType,
    totalDuty = dutiesByTaxType.map(_.dutyDue).sum
  )

  when(calculatorMock.calculateTotalDuty(any())(any())).thenReturn(Future.successful(alcoholDuty))

  "DutyCalculation Controller" - {

    "must return OK and the correct view for a GET if No is selected for add Multiple SPR option" in {

      val mockCacheConnector = mock[CacheConnector]

      when(mockCacheConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[AlcoholDutyCalculatorConnector].toInstance(calculatorMock),
          bind[CacheConnector].toInstance(mockCacheConnector)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.returns.routes.DutyCalculationController.onPageLoad(regime).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[DutyCalculationView]

        val tableViewModel =
          DutyCalculationHelper
            .dutyDueTableViewModel(alcoholDuty, userAnswers, regime)(getMessages(application))
            .toOption
            .get

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(regime, tableViewModel, alcoholDuty.totalDuty)(
          request,
          getMessages(application)
        ).toString
      }
    }

    "must return OK and the correct view for a GET if Yes is selected for add Multiple SPR option" in {

      val updatedUserAnswer = userAnswers.setByKey(DoYouHaveMultipleSPRDutyRatesPage, regime, true).success.value

      val mockCacheConnector = mock[CacheConnector]

      when(mockCacheConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])

      val application = applicationBuilder(userAnswers = Some(updatedUserAnswer))
        .overrides(
          bind[AlcoholDutyCalculatorConnector].toInstance(calculatorMock),
          bind[CacheConnector].toInstance(mockCacheConnector)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.returns.routes.DutyCalculationController.onPageLoad(regime).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[DutyCalculationView]

        val tableViewModel =
          DutyCalculationHelper
            .dutyDueTableViewModel(alcoholDuty, updatedUserAnswer, regime)(getMessages(application))
            .toOption
            .get

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(regime, tableViewModel, alcoholDuty.totalDuty)(
          request,
          getMessages(application)
        ).toString
      }
    }

    "must redirect in the Journey Recovery screen if the user answers are empty" in {

      val mockCacheConnector = mock[CacheConnector]

      when(mockCacheConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[AlcoholDutyCalculatorConnector].toInstance(calculatorMock),
          bind[CacheConnector].toInstance(mockCacheConnector)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.returns.routes.DutyCalculationController.onPageLoad(regime).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to the MultipleSPRList page if there is data for a POST" in {

      val updatedUserAnswers = userAnswers.setByKey(DutyCalculationPage, regime, alcoholDuty).success.value

      val mockCacheConnector = mock[CacheConnector]

      when(mockCacheConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])

      val application = applicationBuilder(userAnswers = Some(updatedUserAnswers))
        .overrides(
          bind[AlcoholDutyCalculatorConnector].toInstance(calculatorMock),
          bind[CacheConnector].toInstance(mockCacheConnector)
        )
        .build()

      running(application) {
        val request = FakeRequest(POST, controllers.returns.routes.DutyCalculationController.onSubmit(regime).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.TaskListController.onPageLoad.url
      }
    }

    "must redirect to the Journey Recover page if there is no data for a POST" in {

      val mockCacheConnector = mock[CacheConnector]

      when(mockCacheConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[AlcoholDutyCalculatorConnector].toInstance(calculatorMock),
          bind[CacheConnector].toInstance(mockCacheConnector)
        )
        .build()

      running(application) {
        val request = FakeRequest(POST, controllers.returns.routes.DutyCalculationController.onSubmit(regime).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
