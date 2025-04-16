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
import forms.declareDuty.HowMuchDoYouNeedToDeclareFormProvider
import models.{AlcoholRegime, NormalMode}
import navigation.ReturnsNavigator
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import org.scalacheck.Arbitrary._
import pages.declareDuty.{HowMuchDoYouNeedToDeclarePage, WhatDoYouNeedToDeclarePage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.Helpers._
import uk.gov.hmrc.http.HttpResponse
import viewmodels.declareDuty.CategoriesByRateTypeHelper
import views.html.declareDuty.HowMuchDoYouNeedToDeclareView

import scala.concurrent.Future

class HowMuchDoYouNeedToDeclareControllerSpec extends SpecBase {

  def onwardRoute = Call("GET", "/foo")

  val regime = arbitrary[AlcoholRegime].sample.value

  val formProvider = new HowMuchDoYouNeedToDeclareFormProvider()

  lazy val howMuchDoYouNeedToDeclareRoute =
    routes.HowMuchDoYouNeedToDeclareController.onPageLoad(NormalMode, regime).url

  val rateBands               = genListOfRateBandForRegime(regime).sample.value.toSet
  val volumeAndRateByTaxTypes = arbitraryVolumeAndRateByTaxType(
    rateBands.toSeq
  ).arbitrary.sample.value

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

        val expectedReturnSummaryList =
          CategoriesByRateTypeHelper.rateBandCategories(rateBands, regime)(getMessages(application))
        val form                      = formProvider(regime)(getMessages(application))

        status(result)          mustEqual OK
        contentAsString(result) mustEqual view(form, regime, expectedReturnSummaryList, NormalMode)(
          request,
          getMessages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val application = applicationBuilder(userAnswers = Some(filledUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, howMuchDoYouNeedToDeclareRoute)

        val view = application.injector.instanceOf[HowMuchDoYouNeedToDeclareView]

        val result = route(application, request).value

        val expectedReturnSummaryList =
          CategoriesByRateTypeHelper.rateBandCategories(rateBands, regime)(getMessages(application))
        val form                      = formProvider(regime)(getMessages(application))
        status(result)          mustEqual OK
        contentAsString(result) mustEqual view(
          form.fill(volumeAndRateByTaxTypes.map(_.toVolumes)),
          regime,
          expectedReturnSummaryList,
          NormalMode
        )(
          request,
          getMessages(application)
        ).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {
      val mockUserAnswersConnector = mock[UserAnswersConnector]
      val mockReturnsNavigator     = mock[ReturnsNavigator]

      when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])
      when(
        mockReturnsNavigator.nextPageWithRegime(eqTo(HowMuchDoYouNeedToDeclarePage), any(), any(), any(), any(), any())
      ) thenReturn onwardRoute

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[ReturnsNavigator].toInstance(mockReturnsNavigator),
            bind[UserAnswersConnector].toInstance(mockUserAnswersConnector)
          )
          .build()

      running(application) {

        val validValues = volumeAndRateByTaxTypes.map(_.toVolumes).zipWithIndex.flatMap { case (value, index) =>
          Seq(
            s"volumes[$index].rateBandDescription" -> rateBandDescription,
            s"volumes[$index].taxType"             -> value.taxType,
            s"volumes[$index].totalLitres"         -> value.totalLitres.toString,
            s"volumes[$index].pureAlcohol"         -> value.pureAlcohol.toString
          )
        }

        val request =
          FakeRequest(POST, howMuchDoYouNeedToDeclareRoute)
            .withFormUrlEncodedBody(validValues: _*)

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url

        verify(mockUserAnswersConnector, times(1)).set(any())(any())
        verify(mockReturnsNavigator, times(1))
          .nextPageWithRegime(
            eqTo(HowMuchDoYouNeedToDeclarePage),
            eqTo(NormalMode),
            any(),
            any(),
            eqTo(false),
            eqTo(None)
          )
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val formData = Seq(
        "volumes[0].rateBandDescription" -> rateBandDescription,
        "volumes[0].taxType"             -> "311",
        "volumes[0].totalLitres"         -> "invalid value"
      )

      running(application) {
        val request =
          FakeRequest(POST, howMuchDoYouNeedToDeclareRoute)
            .withFormUrlEncodedBody(formData: _*)

        val form = formProvider(regime)(getMessages(application))

        val boundForm = form.bind(formData.toMap)

        val view = application.injector.instanceOf[HowMuchDoYouNeedToDeclareView]

        val result = route(application, request).value

        val summaryList = CategoriesByRateTypeHelper.rateBandCategories(rateBands, regime)(getMessages(application))

        status(result)          mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, regime, summaryList, NormalMode)(
          request,
          getMessages(application)
        ).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, howMuchDoYouNeedToDeclareRoute)

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a GET if empty user data is provided" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, howMuchDoYouNeedToDeclareRoute)

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
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

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if empty user data is provided" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, howMuchDoYouNeedToDeclareRoute)
            .withFormUrlEncodedBody(("field1", "value 1"), ("field2", "value 2"))

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if incoherent data is found" in {

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {

        val validValues = volumeAndRateByTaxTypes.map(_.toVolumes).zipWithIndex.flatMap { case (value, index) =>
          Seq(
            s"volumes[$index].taxType"     -> "taxTypeNotInList",
            s"volumes[$index].totalLitres" -> value.totalLitres.toString,
            s"volumes[$index].pureAlcohol" -> value.pureAlcohol.toString
          )
        }

        val request =
          FakeRequest(POST, howMuchDoYouNeedToDeclareRoute)
            .withFormUrlEncodedBody(validValues: _*)

        route(application, request).value
        the[Exception] thrownBy status(
          route(application, request).value
        ) must have message "Expected volumes[0].rateBandDescription to be provided in the view"
      }
    }

    "must throw an exception for a POST if missing taxType" in {
      val mockUserAnswersConnector = mock[UserAnswersConnector]
      val mockReturnsNavigator     = mock[ReturnsNavigator]

      when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[ReturnsNavigator].toInstance(mockReturnsNavigator),
            bind[UserAnswersConnector].toInstance(mockUserAnswersConnector)
          )
          .build()

      running(application) {

        val validValues = volumeAndRateByTaxTypes.map(_.toVolumes).zipWithIndex.flatMap { case (value, index) =>
          Seq(
            s"volumes[$index].rateBandDescription" -> rateBandDescription,
            s"volumes[$index].totalLitres"         -> value.totalLitres.toString,
            s"volumes[$index].pureAlcohol"         -> value.pureAlcohol.toString
          )
        }

        val request =
          FakeRequest(POST, howMuchDoYouNeedToDeclareRoute)
            .withFormUrlEncodedBody(validValues: _*)

        route(application, request).value
        the[Exception] thrownBy status(
          route(application, request).value
        ) must have message "Expected volumes[0].taxType to be provided in the view"
      }
    }
  }
}
