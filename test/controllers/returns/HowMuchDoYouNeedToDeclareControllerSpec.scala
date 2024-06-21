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
import forms.returns.HowMuchDoYouNeedToDeclareFormProvider
import models.{AlcoholRegimeName, NormalMode}
import navigation.{FakeReturnsNavigator, ReturnsNavigator}
import org.mockito.ArgumentMatchers.any
import pages.returns.{HowMuchDoYouNeedToDeclarePage, WhatDoYouNeedToDeclarePage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.Helpers._
import connectors.CacheConnector
import org.scalacheck.Arbitrary._
import uk.gov.hmrc.http.HttpResponse
import viewmodels.checkAnswers.returns.CategoriesByRateTypeHelper
import views.html.returns.HowMuchDoYouNeedToDeclareView

import scala.concurrent.Future

class HowMuchDoYouNeedToDeclareControllerSpec extends SpecBase {

  def onwardRoute = Call("GET", "/foo")

  val regime = arbitrary[AlcoholRegimeName].sample.value

  val formProvider = new HowMuchDoYouNeedToDeclareFormProvider()

  lazy val howMuchDoYouNeedToDeclareRoute =
    routes.HowMuchDoYouNeedToDeclareController.onPageLoad(NormalMode, regime).url

  val rateBands               = genListOfRateBandForRegime(regime).sample.value.toSet
  val volumeAndRateByTaxTypes = rateBands.map(genVolumeAndRateByTaxTypeRateBand(_).arbitrary.sample.value).toSeq

  val userAnswers = emptyUserAnswers
    .setByKey(WhatDoYouNeedToDeclarePage, regime, rateBands)
    .success
    .value

  val filledUserAnswers = userAnswers
    .setByKey(HowMuchDoYouNeedToDeclarePage, regime, volumeAndRateByTaxTypes)
    .success
    .value

  "HowMuchDoYouNeedToDeclare Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, howMuchDoYouNeedToDeclareRoute)

        val view = application.injector.instanceOf[HowMuchDoYouNeedToDeclareView]

        val result = route(application, request).value

        val expectedReturnSummaryList = CategoriesByRateTypeHelper.rateBandCategories(rateBands)(messages(application))
        val form                      = formProvider(regime)(messages(application))

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, regime, expectedReturnSummaryList, NormalMode)(
          request,
          messages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val application = applicationBuilder(userAnswers = Some(filledUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, howMuchDoYouNeedToDeclareRoute)

        val view = application.injector.instanceOf[HowMuchDoYouNeedToDeclareView]

        val result = route(application, request).value

        val expectedReturnSummaryList = CategoriesByRateTypeHelper.rateBandCategories(rateBands)(messages(application))
        val form                      = formProvider(regime)(messages(application))
        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form.fill(volumeAndRateByTaxTypes.map(_.toVolumes)),
          regime,
          expectedReturnSummaryList,
          NormalMode
        )(
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

        val validValues = volumeAndRateByTaxTypes.map(_.toVolumes).zipWithIndex.flatMap { case (value, index) =>
          Seq(
            s"volumes[$index].taxType"     -> value.taxType,
            s"volumes[$index].totalLitres" -> value.totalLitres.toString,
            s"volumes[$index].pureAlcohol" -> value.pureAlcohol.toString
          )
        }

        val request =
          FakeRequest(POST, howMuchDoYouNeedToDeclareRoute)
            .withFormUrlEncodedBody(validValues: _*)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, howMuchDoYouNeedToDeclareRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val form = formProvider(regime)(messages(application))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[HowMuchDoYouNeedToDeclareView]

        val result = route(application, request).value

        val summaryList = CategoriesByRateTypeHelper.rateBandCategories(rateBands)(messages(application))

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, regime, summaryList, NormalMode)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, howMuchDoYouNeedToDeclareRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, howMuchDoYouNeedToDeclareRoute)
            .withFormUrlEncodedBody(("field1", "value 1"), ("field2", "value 2"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
