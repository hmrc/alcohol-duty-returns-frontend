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
import forms.declareDuty.MultipleSPRListFormProvider
import navigation.{FakeReturnsNavigator, ReturnsNavigator}
import org.mockito.ArgumentMatchers.any
import pages.declareDuty.{DoYouWantToAddMultipleSPRToListPage, MultipleSPRListPage, WhatDoYouNeedToDeclarePage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.Helpers._
import connectors.UserAnswersConnector
import uk.gov.hmrc.http.HttpResponse
import viewmodels.declareDuty.MultipleSPRListHelper
import views.html.declareDuty.MultipleSPRListView

import scala.concurrent.Future

class MultipleSPRListControllerSpec extends SpecBase {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new MultipleSPRListFormProvider()
  val form         = formProvider()

  val regime = regimeGen.sample.value

  val rateBands               = genListOfRateBandForRegimeWithSPR(regime).sample.value.toSet
  val volumeAndRateByTaxTypes = rateBands.map(genVolumeAndRateByTaxTypeRateBand(_).arbitrary.sample.value).toSeq

  val userAnswers = emptyUserAnswers
    .setByKey(WhatDoYouNeedToDeclarePage, regime, rateBands)
    .success
    .value
    .setByKey(MultipleSPRListPage, regime, volumeAndRateByTaxTypes)
    .success
    .value

  lazy val multipleSPRListRoute = controllers.declareDuty.routes.MultipleSPRListController.onPageLoad(regime).url

  "MultipleSPRList Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, multipleSPRListRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[MultipleSPRListView]

        val sprTable = MultipleSPRListHelper
          .sprTableViewModel(userAnswers, regime)(getMessages(application))
          .getOrElse(fail())

        status(result)          mustEqual OK
        contentAsString(result) mustEqual view(form, regime, sprTable)(request, getMessages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val filledUserAnswers = userAnswers.setByKey(DoYouWantToAddMultipleSPRToListPage, regime, true).success.value

      val application = applicationBuilder(userAnswers = Some(filledUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, multipleSPRListRoute)

        val view = application.injector.instanceOf[MultipleSPRListView]

        val result = route(application, request).value

        val sprTable = MultipleSPRListHelper
          .sprTableViewModel(userAnswers, regime)(getMessages(application))
          .getOrElse(fail())

        status(result)          mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), regime, sprTable)(
          request,
          getMessages(application)
        ).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockUserAnswersConnector = mock[UserAnswersConnector]

      when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[ReturnsNavigator].toInstance(new FakeReturnsNavigator(onwardRoute)),
            bind[UserAnswersConnector].toInstance(mockUserAnswersConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, multipleSPRListRoute)
            .withFormUrlEncodedBody(("multipleSPRList-yesNoValue", "true"))

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, multipleSPRListRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[MultipleSPRListView]

        val result = route(application, request).value

        val sprTable = MultipleSPRListHelper
          .sprTableViewModel(userAnswers, regime)(getMessages(application))
          .getOrElse(fail())

        status(result)          mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, regime, sprTable)(request, getMessages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, multipleSPRListRoute)

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a GET if empty user data is provided" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, multipleSPRListRoute)

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, multipleSPRListRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if empty user data is provided" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, multipleSPRListRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }

  "must return an error if tax types in MultipleSPRListPage are not found in rate bands" in {

    val unmatchedRateBand = genRateBandForRegimeWithSPR(regime).sample.value
    val unmatchedTaxType  = genVolumeAndRateByTaxTypeRateBand(unmatchedRateBand).arbitrary.sample.value

    val rateBands = genListOfRateBandForRegimeWithSPR(regime).sample.value.toSet

    val incompleteUserAnswers = emptyUserAnswers
      .setByKey(WhatDoYouNeedToDeclarePage, regime, rateBands)
      .success
      .value
      .setByKey(MultipleSPRListPage, regime, Seq(unmatchedTaxType))
      .success
      .value

    val application = applicationBuilder(userAnswers = Some(incompleteUserAnswers)).build()

    running(application) {
      val request = FakeRequest(GET, multipleSPRListRoute)

      val result = route(application, request).value

      val sprTable = MultipleSPRListHelper
        .sprTableViewModel(incompleteUserAnswers, regime)(getMessages(application))

      sprTable mustBe Left(s"Tax types not found: ${unmatchedTaxType.taxType}")

      status(result)                 mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
    }
  }
}
