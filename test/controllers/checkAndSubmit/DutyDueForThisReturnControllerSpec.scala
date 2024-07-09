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
import connectors.AlcoholDutyCalculatorConnector
import models.AlcoholRegime.Beer
import pages.returns.{DoYouHaveMultipleSPRDutyRatesPage, HowMuchDoYouNeedToDeclarePage, WhatDoYouNeedToDeclarePage}
import play.api.test.Helpers._
import viewmodels.checkAnswers.checkAndSubmit.DutyDueForThisReturnHelper
import views.html.checkAndSubmit.DutyDueForThisReturnView

class DutyDueForThisReturnControllerSpec extends SpecBase {

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

  "DutyDueForThisReturn Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(GET, controllers.checkAndSubmit.routes.DutyDueForThisReturnController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[DutyDueForThisReturnView]

        val table = DutyDueForThisReturnHelper
          .dutyDueByRegime(userAnswers, Beer)(messages(application))

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(table)(request, messages(application)).toString
      }
    }
  }
}
