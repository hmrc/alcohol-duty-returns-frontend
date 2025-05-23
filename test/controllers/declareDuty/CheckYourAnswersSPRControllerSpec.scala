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
import connectors.UserAnswersConnector
import org.mockito.ArgumentMatchers.any
import pages.declareDuty.{TellUsAboutMultipleSPRRatePage, WhatDoYouNeedToDeclarePage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.Helpers._
import uk.gov.hmrc.http.HttpResponse
import viewmodels.declareDuty.CheckYourAnswersSPRSummaryListHelper
import views.html.declareDuty.CheckYourAnswersSPRView

import scala.concurrent.Future

class CheckYourAnswersSPRControllerSpec extends SpecBase {

  def onwardRoute = Call("GET", "/foo")

  val regime                  = regimeGen.sample.value
  val rateBands               = genListOfRateBandForRegimeWithSPR(regime).sample.value.toSet
  val volumeAndRateByTaxTypes = genVolumeAndRateByTaxTypeRateBand(rateBands.head).arbitrary.sample.value

  val userAnswers = emptyUserAnswers
    .setByKey(WhatDoYouNeedToDeclarePage, regime, rateBands)
    .success
    .value
    .setByKey(TellUsAboutMultipleSPRRatePage, regime, volumeAndRateByTaxTypes)
    .success
    .value

  "CheckYourAnswerSPR Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request =
          FakeRequest(GET, controllers.declareDuty.routes.CheckYourAnswersSPRController.onPageLoad(regime).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CheckYourAnswersSPRView]

        val summaryList =
          CheckYourAnswersSPRSummaryListHelper.summaryList(regime, userAnswers, None)(getMessages(application)).get

        status(result)          mustEqual OK
        contentAsString(result) mustEqual view(regime, summaryList, None)(request, getMessages(application)).toString
      }
    }

    "must redirect to the Journey Recovery page if there is no data for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(GET, controllers.declareDuty.routes.CheckYourAnswersSPRController.onPageLoad(regime).url)

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to the Journey Recovery page if there is no data for a POST" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, controllers.declareDuty.routes.CheckYourAnswersSPRController.onSubmit(regime).url)

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to the MultipleSPRList page if there is data for a POST" in {

      val mockUserAnswersConnector = mock[UserAnswersConnector]

      when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[UserAnswersConnector].toInstance(mockUserAnswersConnector)
        )
        .build()

      running(application) {
        val request =
          FakeRequest(POST, controllers.declareDuty.routes.CheckYourAnswersSPRController.onSubmit(regime).url)

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.declareDuty.routes.MultipleSPRListController
          .onPageLoad(regime)
          .url

        verify(mockUserAnswersConnector, times(1)).set(any())(any())
      }
    }

    "must redirect to the MultipleSPRList page if there is data for a POST with an index" in {

      val index = Some(0)

      val mockUserAnswersConnector = mock[UserAnswersConnector]

      when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[UserAnswersConnector].toInstance(mockUserAnswersConnector)
        )
        .build()

      running(application) {
        val request =
          FakeRequest(POST, controllers.declareDuty.routes.CheckYourAnswersSPRController.onSubmit(regime, index).url)

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.declareDuty.routes.MultipleSPRListController
          .onPageLoad(regime)
          .url

        verify(mockUserAnswersConnector, times(1)).set(any())(any())
      }
    }
  }
}
