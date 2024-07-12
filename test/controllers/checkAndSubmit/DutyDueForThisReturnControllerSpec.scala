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

package controllers.checkAndSubmit

import base.SpecBase
import connectors.{AlcoholDutyCalculatorConnector, CacheConnector}
import models.AlcoholRegime.{Beer, Cider}
import models.returns.{AlcoholDuty, DutyByTaxType}
import org.mockito.ArgumentMatchers.any
import pages.returns.{AlcoholDutyPage, DeclareAlcoholDutyQuestionPage}
import play.api.inject.bind
import play.api.test.Helpers._
import uk.gov.hmrc.http.HttpResponse
import viewmodels.checkAnswers.checkAndSubmit.DutyDueForThisReturnHelper
import views.html.checkAndSubmit.DutyDueForThisReturnView

import scala.concurrent.Future

class DutyDueForThisReturnControllerSpec extends SpecBase {

  val calculatorMock  = mock[AlcoholDutyCalculatorConnector]
  val regime          = regimeGen.sample.value
  val rateBands       = genListOfRateBandForRegime(regime).sample.value.toSet
  val volumesAndRates = arbitraryVolumeAndRateByTaxType(
    rateBands.toSeq
  ).arbitrary.sample.value

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

  val filledUserAnswers = emptyUserAnswers
    .set(DeclareAlcoholDutyQuestionPage, true)
    .success
    .value
    .setByKey(AlcoholDutyPage, Beer, alcoholDuty)
    .success
    .value
    .setByKey(AlcoholDutyPage, Cider, alcoholDuty)
    .success
    .value

  val userAnswers = emptyUserAnswers
    .set(DeclareAlcoholDutyQuestionPage, false)
    .success
    .value

  val totalValue = BigDecimal(10.23)
  val nilValue   = 0.00

  "DutyDueForThisReturn Controller" - {

    "must return OK and the correct view for a GET if Yes is selected and there is alcohol to declare" in {

      val application = applicationBuilder(userAnswers = Some(filledUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(GET, controllers.checkAndSubmit.routes.DutyDueForThisReturnController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[DutyDueForThisReturnView]

        val table = DutyDueForThisReturnHelper
          .dutyDueByRegime(filledUserAnswers)(messages(application))

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(table, totalValue)(request, messages(application)).toString
      }
    }

    "must return OK and the correct view for a GET if No is selected and there is no alcohol to declare" in {

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request =
          FakeRequest(GET, controllers.checkAndSubmit.routes.DutyDueForThisReturnController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[DutyDueForThisReturnView]

        val table = DutyDueForThisReturnHelper
          .dutyDueByRegime(userAnswers)(messages(application))

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(table, nilValue)(request, messages(application)).toString
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
  }
}
