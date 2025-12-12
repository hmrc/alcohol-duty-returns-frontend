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
import models.ErrorModel
import models.declareDuty.{AlcoholDuty, DutyByTaxType}
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{times, verify, when}
import pages.declareDuty.{DoYouHaveMultipleSPRDutyRatesPage, DutyCalculationPage, HowMuchDoYouNeedToDeclarePage, WhatDoYouNeedToDeclarePage}
import play.api.inject.bind
import play.api.test.Helpers.*
import uk.gov.hmrc.http.HttpResponse
import viewmodels.declareDuty.{CheckYourAnswersSummaryListHelper, DutyCalculationHelper}
import views.html.declareDuty.DutyCalculationView

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
      val mockCheckYourAnswersHelper = mock[CheckYourAnswersSummaryListHelper]
      val mockUserAnswersConnector   = mock[UserAnswersConnector]

      when(mockCheckYourAnswersHelper.checkDeclarationDetailsArePresent(any(), any())) thenReturn Right(rateBands)
      when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[AlcoholDutyCalculatorConnector].toInstance(calculatorMock),
          bind[CheckYourAnswersSummaryListHelper].toInstance(mockCheckYourAnswersHelper),
          bind[UserAnswersConnector].toInstance(mockUserAnswersConnector)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.declareDuty.routes.DutyCalculationController.onPageLoad(regime).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[DutyCalculationView]

        val tableViewModel =
          DutyCalculationHelper
            .dutyDueTableViewModel(alcoholDuty, userAnswers, regime)(getMessages(application))
            .toOption
            .get

        status(result)          mustEqual OK
        contentAsString(result) mustEqual view(regime, tableViewModel, alcoholDuty.totalDuty)(
          request,
          getMessages(application)
        ).toString

        verify(mockCheckYourAnswersHelper, times(1)).checkDeclarationDetailsArePresent(eqTo(regime), eqTo(userAnswers))
      }
    }

    "must return OK and the correct view for a GET if Yes is selected for add Multiple SPR option" in {
      val updatedUserAnswers = userAnswers.setByKey(DoYouHaveMultipleSPRDutyRatesPage, regime, true).success.value

      val mockCheckYourAnswersHelper = mock[CheckYourAnswersSummaryListHelper]
      val mockUserAnswersConnector   = mock[UserAnswersConnector]

      when(mockCheckYourAnswersHelper.checkDeclarationDetailsArePresent(any(), any())) thenReturn Right(rateBands)
      when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])

      val application = applicationBuilder(userAnswers = Some(updatedUserAnswers))
        .overrides(
          bind[AlcoholDutyCalculatorConnector].toInstance(calculatorMock),
          bind[CheckYourAnswersSummaryListHelper].toInstance(mockCheckYourAnswersHelper),
          bind[UserAnswersConnector].toInstance(mockUserAnswersConnector)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.declareDuty.routes.DutyCalculationController.onPageLoad(regime).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[DutyCalculationView]

        val tableViewModel =
          DutyCalculationHelper
            .dutyDueTableViewModel(alcoholDuty, updatedUserAnswers, regime)(getMessages(application))
            .toOption
            .get

        status(result)          mustEqual OK
        contentAsString(result) mustEqual view(regime, tableViewModel, alcoholDuty.totalDuty)(
          request,
          getMessages(application)
        ).toString

        verify(mockCheckYourAnswersHelper, times(1))
          .checkDeclarationDetailsArePresent(eqTo(regime), eqTo(updatedUserAnswers))
      }
    }

    "must redirect to Journey Recovery for a GET if user answers do not exist" in {
      val mockCheckYourAnswersHelper = mock[CheckYourAnswersSummaryListHelper]
      val mockUserAnswersConnector   = mock[UserAnswersConnector]

      when(mockCheckYourAnswersHelper.checkDeclarationDetailsArePresent(any(), any())) thenReturn Right(rateBands)
      when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])

      val application = applicationBuilder(userAnswers = None)
        .overrides(
          bind[AlcoholDutyCalculatorConnector].toInstance(calculatorMock),
          bind[CheckYourAnswersSummaryListHelper].toInstance(mockCheckYourAnswersHelper),
          bind[UserAnswersConnector].toInstance(mockUserAnswersConnector)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.declareDuty.routes.DutyCalculationController.onPageLoad(regime).url)

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url

        verify(mockCheckYourAnswersHelper, times(0)).checkDeclarationDetailsArePresent(any(), any())
      }
    }

    "must redirect to Journey Recovery for a GET if the user answers do not contain required declaration details" in {
      val mockCheckYourAnswersHelper = mock[CheckYourAnswersSummaryListHelper]
      val mockUserAnswersConnector   = mock[UserAnswersConnector]

      when(mockCheckYourAnswersHelper.checkDeclarationDetailsArePresent(any(), any())) thenReturn
        Left(ErrorModel(BAD_REQUEST, "Error from helper"))
      when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[AlcoholDutyCalculatorConnector].toInstance(calculatorMock),
          bind[CheckYourAnswersSummaryListHelper].toInstance(mockCheckYourAnswersHelper),
          bind[UserAnswersConnector].toInstance(mockUserAnswersConnector)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.declareDuty.routes.DutyCalculationController.onPageLoad(regime).url)

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url

        verify(mockCheckYourAnswersHelper, times(1)).checkDeclarationDetailsArePresent(eqTo(regime), eqTo(userAnswers))
      }
    }

    "must redirect to the Task List for a POST if user answers contain duty data" in {

      val updatedUserAnswers = userAnswers.setByKey(DutyCalculationPage, regime, alcoholDuty).success.value

      val mockUserAnswersConnector = mock[UserAnswersConnector]

      when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])

      val application = applicationBuilder(userAnswers = Some(updatedUserAnswers))
        .overrides(
          bind[AlcoholDutyCalculatorConnector].toInstance(calculatorMock),
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

    "must redirect to Journey Recovery for a POST if user answers do not contain duty data" in {

      val mockUserAnswersConnector = mock[UserAnswersConnector]

      when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[AlcoholDutyCalculatorConnector].toInstance(calculatorMock),
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

    "must redirect to Journey Recovery for a POST if user answers do not exist" in {

      val mockUserAnswersConnector = mock[UserAnswersConnector]

      when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])

      val application = applicationBuilder(userAnswers = None)
        .overrides(
          bind[AlcoholDutyCalculatorConnector].toInstance(calculatorMock),
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
}
