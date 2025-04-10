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
import forms.declareDuty.TellUsAboutSingleSPRRateFormProvider
import models.declareDuty.VolumeAndRateByTaxType
import models.{AlcoholRegime, NormalMode, RateBand}
import navigation.ReturnsNavigator
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import pages.declareDuty.{TellUsAboutSingleSPRRatePage, WhatDoYouNeedToDeclarePage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.Helpers._
import uk.gov.hmrc.http.HttpResponse
import viewmodels.declareDuty.CategoriesByRateTypeHelper
import views.html.declareDuty.TellUsAboutSingleSPRRateView

import scala.concurrent.Future

class TellUsAboutSingleSPRRateControllerSpec extends SpecBase {

  def onwardRoute = Call("GET", "/foo")

  val regime: AlcoholRegime = regimeGen.sample.value

  val formProvider = new TellUsAboutSingleSPRRateFormProvider()

  lazy val tellUsAboutSingleSPRRateRoute = routes.TellUsAboutSingleSPRRateController.onPageLoad(NormalMode, regime).url

  val rateBands: Seq[RateBand]                            = genListOfRateBandForRegimeWithSPR(regime).sample.value
  val volumeAndRateByTaxType: Seq[VolumeAndRateByTaxType] = arbitraryVolumeAndRateByTaxType(
    rateBands
  ).arbitrary.sample.value

  val userAnswers       = emptyUserAnswers.setByKey(WhatDoYouNeedToDeclarePage, regime, rateBands.toSet).success.value
  val filledUserAnswers =
    userAnswers.setByKey(TellUsAboutSingleSPRRatePage, regime, volumeAndRateByTaxType).success.value

  "TellUsAboutSingleSPRRate Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, tellUsAboutSingleSPRRateRoute)

        val view = application.injector.instanceOf[TellUsAboutSingleSPRRateView]

        val result = route(application, request).value

        val categoriesByRateTypeViewModel =
          CategoriesByRateTypeHelper.rateBandCategories(rateBands.toSet, regime)(getMessages(application))

        val form = formProvider(regime)(getMessages(application))

        status(result)          mustEqual OK
        contentAsString(result) mustEqual view(form, regime, categoriesByRateTypeViewModel, NormalMode)(
          request,
          getMessages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val application = applicationBuilder(userAnswers = Some(filledUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, tellUsAboutSingleSPRRateRoute)

        val view = application.injector.instanceOf[TellUsAboutSingleSPRRateView]

        val result                        = route(application, request).value
        val categoriesByRateTypeViewModel =
          CategoriesByRateTypeHelper.rateBandCategories(rateBands.toSet, regime)(getMessages(application))

        val form = formProvider(regime)(getMessages(application))

        status(result)          mustEqual OK
        contentAsString(result) mustEqual view(
          form.fill(volumeAndRateByTaxType),
          regime,
          categoriesByRateTypeViewModel,
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
        mockReturnsNavigator.nextPageWithRegime(eqTo(TellUsAboutSingleSPRRatePage), any(), any(), any(), any(), any())
      ) thenReturn onwardRoute

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[ReturnsNavigator].toInstance(mockReturnsNavigator),
            bind[UserAnswersConnector].toInstance(mockUserAnswersConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, tellUsAboutSingleSPRRateRoute)
            .withFormUrlEncodedBody(
              "volumesWithRate[0].rateBandDescription" -> rateBandDescription,
              "volumesWithRate[0].totalLitres"         -> "1000",
              "volumesWithRate[0].pureAlcohol"         -> "500.0000",
              "volumesWithRate[0].dutyRate"            -> "10",
              "volumesWithRate[0].taxType"             -> "371"
            )

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url

        verify(mockUserAnswersConnector, times(1)).set(any())(any())
        verify(mockReturnsNavigator, times(1))
          .nextPageWithRegime(
            eqTo(TellUsAboutSingleSPRRatePage),
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
        "volumesWithRate[0].rateBandDescription" -> rateBandDescription,
        "volumesWithRate[0].taxType"             -> "311",
        "volumesWithRate[0].totalLitres"         -> "invalid value"
      )

      running(application) {
        val request =
          FakeRequest(POST, tellUsAboutSingleSPRRateRoute)
            .withFormUrlEncodedBody(formData: _*)

        val form = formProvider(regime)(getMessages(application))

        val boundForm = form.bind(formData.toMap)

        val view = application.injector.instanceOf[TellUsAboutSingleSPRRateView]

        val result = route(application, request).value

        val categoriesByRateTypeViewModel =
          CategoriesByRateTypeHelper.rateBandCategories(rateBands.toSet, regime)(getMessages(application))

        status(result)          mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, regime, categoriesByRateTypeViewModel, NormalMode)(
          request,
          getMessages(application)
        ).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, tellUsAboutSingleSPRRateRoute)

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, tellUsAboutSingleSPRRateRoute)
            .withFormUrlEncodedBody(
              "volumesWithRate[0].rateBandDescription" -> rateBandDescription,
              "volumesWithRate[0].taxType"             -> "371",
              "volumesWithRate[0].totalLitres"         -> "1000",
              "volumesWithRate[0].pureAlcohol"         -> "500.0000",
              "volumesWithRate[0].dutyRate"            -> "10"
            )

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must throw an exception for a POST if missing rateBandDescription" in {
      val mockUserAnswersConnector = mock[UserAnswersConnector]

      when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[ReturnsNavigator].toInstance(new FakeReturnsNavigator(onwardRoute, Some(false))),
            bind[UserAnswersConnector].toInstance(mockUserAnswersConnector)
          )
          .build()

      running(application) {

        val request =
          FakeRequest(POST, tellUsAboutSingleSPRRateRoute)
            .withFormUrlEncodedBody(
              "volumesWithRate[0].taxType"     -> "371",
              "volumesWithRate[0].totalLitres" -> "1000",
              "volumesWithRate[0].pureAlcohol" -> "500.0000",
              "volumesWithRate[0].dutyRate"    -> "10"
            )

        route(application, request).value
        the[Exception] thrownBy status(
          route(application, request).value
        ) must have message "Expected volumesWithRate[0].rateBandDescription to be provided in the view"
      }
    }

    "must throw an exception for a POST if missing taxType" in {
      val mockUserAnswersConnector = mock[UserAnswersConnector]

      when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[ReturnsNavigator].toInstance(new FakeReturnsNavigator(onwardRoute, Some(false))),
            bind[UserAnswersConnector].toInstance(mockUserAnswersConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, tellUsAboutSingleSPRRateRoute)
            .withFormUrlEncodedBody(
              "volumesWithRate[0].rateBandDescription" -> rateBandDescription,
              "volumesWithRate[0].totalLitres"         -> "1000",
              "volumesWithRate[0].pureAlcohol"         -> "500.0000",
              "volumesWithRate[0].dutyRate"            -> "10"
            )

        route(application, request).value
        the[Exception] thrownBy status(
          route(application, request).value
        ) must have message "Expected volumesWithRate[0].taxType to be provided in the view"
      }
    }
  }
}
