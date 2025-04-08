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

import base.SpecBase
import connectors.{AlcoholDutyCalculatorConnector, UserAnswersConnector}
import models.declareDuty.{AlcoholDuty, DutyByTaxType}
import org.mockito.ArgumentMatchers.any
import pages.declareDuty.{DoYouHaveMultipleSPRDutyRatesPage, DutyCalculationPage, HowMuchDoYouNeedToDeclarePage, WhatDoYouNeedToDeclarePage}
import play.api.inject.bind
import play.api.test.Helpers._
import uk.gov.hmrc.http.HttpResponse
import viewmodels.declareDuty.DutyCalculationHelper
import views.html.declareDuty.DutyCalculationView

import scala.concurrent.Future

class DutyCalculationControllerSpec extends SpecBase {
  "DutyCalculationController" - {
    "must return OK and the correct view for a GET if No is selected for add Multiple SPR option" in new SetUp {
      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[AlcoholDutyCalculatorConnector].toInstance(mockAlcoholDutyCalculatorConnector),
          bind[UserAnswersConnector].toInstance(mockUserAnswersConnector),
          bind[DutyCalculationHelper].toInstance(mockDutyCalculationHelper)
        )
        .build()

      val tableViewModel =
        new DutyCalculationHelper()
          .dutyDueTableViewModel(alcoholDuty, userAnswers, regime)(getMessages(application))
          .toOption
          .get

      when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])
      when(mockAlcoholDutyCalculatorConnector.calculateTotalDuty(any())(any()))
        .thenReturn(Future.successful(alcoholDuty))
      when(mockDutyCalculationHelper.dutyDueTableViewModel(any(), any(), any())(any()))
        .thenReturn(Right(tableViewModel))

      running(application) {
        val request = FakeRequest(GET, controllers.declareDuty.routes.DutyCalculationController.onPageLoad(regime).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[DutyCalculationView]

        status(result)          mustEqual OK
        contentAsString(result) mustEqual view(regime, tableViewModel, alcoholDuty.totalDuty)(
          request,
          getMessages(application)
        ).toString
      }

      verify(mockDutyCalculationHelper, times(1)).dutyDueTableViewModel(alcoholDuty, userAnswers, regime)(
        getMessages(application)
      )
    }

    "must return OK and the correct view for a GET if Yes is selected for add Multiple SPR option" in new SetUp {
      val updatedUserAnswers = userAnswers.setByKey(DoYouHaveMultipleSPRDutyRatesPage, regime, true).success.value

      val application = applicationBuilder(userAnswers = Some(updatedUserAnswers))
        .overrides(
          bind[AlcoholDutyCalculatorConnector].toInstance(mockAlcoholDutyCalculatorConnector),
          bind[UserAnswersConnector].toInstance(mockUserAnswersConnector),
          bind[DutyCalculationHelper].toInstance(mockDutyCalculationHelper)
        )
        .build()

      val tableViewModel =
        new DutyCalculationHelper()
          .dutyDueTableViewModel(alcoholDuty, updatedUserAnswers, regime)(getMessages(application))
          .toOption
          .get

      when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])
      when(mockAlcoholDutyCalculatorConnector.calculateTotalDuty(any())(any()))
        .thenReturn(Future.successful(alcoholDuty))
      when(mockDutyCalculationHelper.dutyDueTableViewModel(any(), any(), any())(any()))
        .thenReturn(Right(tableViewModel))

      running(application) {
        val request = FakeRequest(GET, controllers.declareDuty.routes.DutyCalculationController.onPageLoad(regime).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[DutyCalculationView]

        status(result)          mustEqual OK
        contentAsString(result) mustEqual view(regime, tableViewModel, alcoholDuty.totalDuty)(
          request,
          getMessages(application)
        ).toString
      }

      verify(mockDutyCalculationHelper, times(1)).dutyDueTableViewModel(alcoholDuty, updatedUserAnswers, regime)(
        getMessages(application)
      )
    }

    "must redirect in the Journey Recovery screen if unable to get the view model" in new SetUp {
      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[AlcoholDutyCalculatorConnector].toInstance(mockAlcoholDutyCalculatorConnector),
          bind[UserAnswersConnector].toInstance(mockUserAnswersConnector),
          bind[DutyCalculationHelper].toInstance(mockDutyCalculationHelper)
        )
        .build()

      when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])
      when(mockAlcoholDutyCalculatorConnector.calculateTotalDuty(any())(any()))
        .thenReturn(Future.successful(alcoholDuty))
      when(mockDutyCalculationHelper.dutyDueTableViewModel(any(), any(), any())(any())).thenReturn(Left("Error!"))

      running(application) {
        val request = FakeRequest(GET, controllers.declareDuty.routes.DutyCalculationController.onPageLoad(regime).url)

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }

      verify(mockDutyCalculationHelper, times(1)).dutyDueTableViewModel(alcoholDuty, userAnswers, regime)(
        getMessages(application)
      )
    }

    "must redirect in the Journey Recovery screen if the user answers are empty" in new SetUp {
      when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[AlcoholDutyCalculatorConnector].toInstance(mockAlcoholDutyCalculatorConnector),
          bind[UserAnswersConnector].toInstance(mockUserAnswersConnector)
        )
        .build()

      when(mockAlcoholDutyCalculatorConnector.calculateTotalDuty(any())(any()))
        .thenReturn(Future.successful(alcoholDuty))

      running(application) {
        val request = FakeRequest(GET, controllers.declareDuty.routes.DutyCalculationController.onPageLoad(regime).url)

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to the MultipleSPRList page if there is data for a POST" in new SetUp {
      val updatedUserAnswers = userAnswers.setByKey(DutyCalculationPage, regime, alcoholDuty).success.value

      when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])

      val application = applicationBuilder(userAnswers = Some(updatedUserAnswers))
        .overrides(
          bind[UserAnswersConnector].toInstance(mockUserAnswersConnector)
        )
        .build()

      running(application) {
        val request = FakeRequest(POST, controllers.declareDuty.routes.DutyCalculationController.onSubmit(regime).url)

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.TaskListController.onPageLoad.url
      }
    }

    "must redirect to the Journey Recover page if there is no data for a POST" in new SetUp {
      when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[UserAnswersConnector].toInstance(mockUserAnswersConnector)
        )
        .build()

      running(application) {
        val request = FakeRequest(POST, controllers.declareDuty.routes.DutyCalculationController.onSubmit(regime).url)

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }

  class SetUp {
    val mockUserAnswersConnector           = mock[UserAnswersConnector]
    val mockAlcoholDutyCalculatorConnector = mock[AlcoholDutyCalculatorConnector]
    val mockDutyCalculationHelper          = mock[DutyCalculationHelper]

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
  }
}
