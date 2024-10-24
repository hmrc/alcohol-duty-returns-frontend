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
import models.AlcoholRegime
import org.scalacheck.Arbitrary._
import pages.declareDuty.{HowMuchDoYouNeedToDeclarePage, WhatDoYouNeedToDeclarePage}
import play.api.test.Helpers._
import viewmodels.declareDuty.CheckYourAnswersSummaryListHelper
import views.html.declareDuty.CheckYourAnswersView

class CheckYourAnswersControllerSpec extends SpecBase {

  "CheckYourAnswers Controller" - {

    val regime = arbitrary[AlcoholRegime].sample.value

    val rateBands      = arbitraryRateBandList(regime).arbitrary.sample.value.toSet
    val dutyByTaxTypes = rateBands.map(genVolumeAndRateByTaxTypeRateBand(_).arbitrary.sample.value).toSeq

    val userAnswers = emptyUserAnswers
      .setByKey(WhatDoYouNeedToDeclarePage, regime, rateBands)
      .success
      .value
      .setByKey(HowMuchDoYouNeedToDeclarePage, regime, dutyByTaxTypes)
      .success
      .value

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.declareDuty.routes.CheckYourAnswersController.onPageLoad(regime).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CheckYourAnswersView]

        val expectedReturnSummaryList =
          CheckYourAnswersSummaryListHelper.createSummaryList(regime, userAnswers)(getMessages(application)).get

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(regime, expectedReturnSummaryList)(
          request,
          getMessages(application)
        ).toString
      }
    }

    "must return an exception empty dutyByTaxTypes" in {
      val userAnswers = emptyUserAnswers
        .setByKey(WhatDoYouNeedToDeclarePage, regime, rateBands)
        .success
        .value
        .setByKey(HowMuchDoYouNeedToDeclarePage, regime, Seq.empty)
        .success
        .value
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val exception = intercept[IllegalArgumentException] {
          CheckYourAnswersSummaryListHelper.createSummaryList(regime, userAnswers)(getMessages(application)).get
        }
        exception.getMessage.startsWith("Invalid tax type:") mustBe true
      }
    }

    "must redirect to the Journey Recovery page when there is no data" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, controllers.declareDuty.routes.CheckYourAnswersController.onPageLoad(regime).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to the Journey Recovery page when there is an empty user-answer" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.declareDuty.routes.CheckYourAnswersController.onPageLoad(regime).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
