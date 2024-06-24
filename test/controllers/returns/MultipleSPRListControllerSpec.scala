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
import forms.returns.MultipleSPRListFormProvider
import navigation.{FakeReturnsNavigator, ReturnsNavigator}
import org.mockito.ArgumentMatchers.any
import pages.returns.{DoYouWantToAddMultipleSPRToListPage, MultipleSPRListPage, WhatDoYouNeedToDeclarePage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.Helpers._
import connectors.CacheConnector
import uk.gov.hmrc.http.HttpResponse
import viewmodels.checkAnswers.returns.MultipleSPRListHelper
import views.html.returns.MultipleSPRListView

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

  lazy val multipleSPRListRoute = controllers.returns.routes.MultipleSPRListController.onPageLoad(regime).url

  "MultipleSPRList Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, multipleSPRListRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[MultipleSPRListView]

        val sprTable = MultipleSPRListHelper
          .sprTableViewModel(userAnswers, regime)(messages(application))
          .getOrElse(fail())

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, regime, sprTable)(request, messages(application)).toString
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
          .sprTableViewModel(userAnswers, regime)(messages(application))
          .getOrElse(fail())

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), regime, sprTable)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockCacheConnector = mock[CacheConnector]

      when(mockCacheConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[ReturnsNavigator].toInstance(new FakeReturnsNavigator(onwardRoute)),
            bind[CacheConnector].toInstance(mockCacheConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, multipleSPRListRoute)
            .withFormUrlEncodedBody(("multipleSPRList-yesNoValue", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
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
          .sprTableViewModel(userAnswers, regime)(messages(application))
          .getOrElse(fail())

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, regime, sprTable)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, multipleSPRListRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
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

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
