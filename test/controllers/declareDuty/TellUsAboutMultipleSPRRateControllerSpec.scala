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
import forms.declareDuty.TellUsAboutMultipleSPRRateFormProvider
import models.declareDuty.VolumeAndRateByTaxType
import models.{CheckMode, NormalMode}
import navigation.ReturnsNavigator
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import pages.declareDuty.{MultipleSPRListPage, TellUsAboutMultipleSPRRatePage, WhatDoYouNeedToDeclarePage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.Helpers._
import uk.gov.hmrc.http.HttpResponse
import viewmodels.declareDuty.TellUsAboutMultipleSPRRateHelper
import views.html.declareDuty.TellUsAboutMultipleSPRRateView

import scala.concurrent.Future

class TellUsAboutMultipleSPRRateControllerSpec extends SpecBase {

  val regime = regimeGen.sample.value

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new TellUsAboutMultipleSPRRateFormProvider()

  lazy val tellUsAboutMultipleSPRRateRoute =
    routes.TellUsAboutMultipleSPRRateController.onPageLoad(NormalMode, regime).url

  val rateBands              = genListOfRateBandForRegimeWithSPR(regime).sample.value.toSet
  val volumeAndRateByTaxType = genVolumeAndRateByTaxTypeRateBand(rateBands.head).arbitrary.sample.value

  val userAnswers       = emptyUserAnswers.setByKey(WhatDoYouNeedToDeclarePage, regime, rateBands).success.value
  val filledUserAnswers =
    userAnswers.setByKey(TellUsAboutMultipleSPRRatePage, regime, volumeAndRateByTaxType).success.value

  "TellUsAboutMultipleSPRRate Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, tellUsAboutMultipleSPRRateRoute)

        val view = application.injector.instanceOf[TellUsAboutMultipleSPRRateView]

        val result = route(application, request).value

        val rateBandRadioButton =
          TellUsAboutMultipleSPRRateHelper.radioItems(rateBands, regime)(getMessages(application))

        val form = formProvider()

        status(result)          mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, regime, rateBandRadioButton, None)(
          request,
          getMessages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val application = applicationBuilder(userAnswers = Some(filledUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, tellUsAboutMultipleSPRRateRoute)

        val view = application.injector.instanceOf[TellUsAboutMultipleSPRRateView]

        val result = route(application, request).value

        val rateBandRadioButton =
          TellUsAboutMultipleSPRRateHelper.radioItems(rateBands, regime)(getMessages(application))

        val form = formProvider()

        status(result)          mustEqual OK
        contentAsString(result) mustEqual view(
          form.fill(volumeAndRateByTaxType),
          NormalMode,
          regime,
          rateBandRadioButton,
          None
        )(
          request,
          getMessages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET when an index is provided" in {

      val index = 0

      val tellUsAboutMultipleSPRRateRoute =
        routes.TellUsAboutMultipleSPRRateController.onPageLoad(NormalMode, regime, Some(index)).url

      val filledUserAnswers =
        userAnswers.setByKey(MultipleSPRListPage, regime, Seq(volumeAndRateByTaxType)).success.value

      val application = applicationBuilder(userAnswers = Some(filledUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, tellUsAboutMultipleSPRRateRoute)

        val view = application.injector.instanceOf[TellUsAboutMultipleSPRRateView]

        val result = route(application, request).value

        val rateBandRadioButton =
          TellUsAboutMultipleSPRRateHelper.radioItems(rateBands, regime)(getMessages(application))

        val form = formProvider()

        status(result)          mustEqual OK
        contentAsString(result) mustEqual view(
          form.fill(volumeAndRateByTaxType),
          NormalMode,
          regime,
          rateBandRadioButton,
          Some(index)
        )(
          request,
          getMessages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET when an index is provided in Check mode" in {

      val index = 0

      val tellUsAboutMultipleSPRRateRoute =
        routes.TellUsAboutMultipleSPRRateController.onPageLoad(CheckMode, regime, Some(index)).url

      val application = applicationBuilder(userAnswers = Some(filledUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, tellUsAboutMultipleSPRRateRoute)

        val view = application.injector.instanceOf[TellUsAboutMultipleSPRRateView]

        val result = route(application, request).value

        val rateBandRadioButton =
          TellUsAboutMultipleSPRRateHelper.radioItems(rateBands, regime)(getMessages(application))

        val form = formProvider()

        status(result)          mustEqual OK
        contentAsString(result) mustEqual view(
          form.fill(volumeAndRateByTaxType),
          CheckMode,
          regime,
          rateBandRadioButton,
          Some(index)
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
        mockReturnsNavigator.nextPageWithRegime(eqTo(TellUsAboutMultipleSPRRatePage), any(), any(), any(), any(), any())
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
          FakeRequest(POST, tellUsAboutMultipleSPRRateRoute)
            .withFormUrlEncodedBody(
              "volumesWithRate.totalLitres" -> "1000.00",
              "volumesWithRate.pureAlcohol" -> "500.0000",
              "volumesWithRate.dutyRate"    -> "10",
              "volumesWithRate.taxType"     -> "371"
            )

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url

        verify(mockUserAnswersConnector, times(1)).set(any())(any())
        verify(mockReturnsNavigator, times(1))
          .nextPageWithRegime(
            eqTo(TellUsAboutMultipleSPRRatePage),
            eqTo(NormalMode),
            any(),
            any(),
            eqTo(false),
            eqTo(None)
          )
      }
    }

    "must redirect to the next page when valid data is submitted and index is provided" in {

      val index = 0

      val tellUsAboutMultipleSPRRateRoute =
        routes.TellUsAboutMultipleSPRRateController.onPageLoad(NormalMode, regime, Some(index)).url

      val volumeAndRateByTaxType = VolumeAndRateByTaxType(
        totalLitres = 1000,
        pureAlcohol = 500,
        dutyRate = 10,
        taxType = rateBands.head.taxTypeCode
      )

      val filledUserAnswers =
        userAnswers.setByKey(MultipleSPRListPage, regime, Seq(volumeAndRateByTaxType)).success.value

      val mockUserAnswersConnector = mock[UserAnswersConnector]
      val mockReturnsNavigator     = mock[ReturnsNavigator]

      when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])
      when(
        mockReturnsNavigator.nextPageWithRegime(eqTo(TellUsAboutMultipleSPRRatePage), any(), any(), any(), any(), any())
      ) thenReturn onwardRoute

      val application =
        applicationBuilder(userAnswers = Some(filledUserAnswers))
          .overrides(
            bind[ReturnsNavigator].toInstance(mockReturnsNavigator),
            bind[UserAnswersConnector].toInstance(mockUserAnswersConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, tellUsAboutMultipleSPRRateRoute)
            .withFormUrlEncodedBody(
              "volumesWithRate.totalLitres" -> "10000.00",
              "volumesWithRate.pureAlcohol" -> "5000.0000",
              "volumesWithRate.dutyRate"    -> "100",
              "volumesWithRate.taxType"     -> volumeAndRateByTaxType.taxType
            )

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url

        verify(mockUserAnswersConnector, times(1)).set(any())(any())
        verify(mockReturnsNavigator, times(1))
          .nextPageWithRegime(
            eqTo(TellUsAboutMultipleSPRRatePage),
            eqTo(NormalMode),
            any(),
            any(),
            eqTo(true),
            eqTo(Some(index))
          )
      }
    }

    "must redirect to the next page when valid data is submitted, index is provided and the mode is CheckMode" in {

      val index = 0

      val tellUsAboutMultipleSPRRateRoute =
        routes.TellUsAboutMultipleSPRRateController.onPageLoad(CheckMode, regime, Some(index)).url

      val volumeAndRateByTaxType = VolumeAndRateByTaxType(
        totalLitres = 1000,
        pureAlcohol = 500,
        dutyRate = 10,
        taxType = rateBands.head.taxTypeCode
      )

      val filledUserAnswers =
        userAnswers.setByKey(MultipleSPRListPage, regime, Seq(volumeAndRateByTaxType)).success.value

      val mockUserAnswersConnector = mock[UserAnswersConnector]
      val mockReturnsNavigator     = mock[ReturnsNavigator]

      when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])
      when(
        mockReturnsNavigator.nextPageWithRegime(eqTo(TellUsAboutMultipleSPRRatePage), any(), any(), any(), any(), any())
      ) thenReturn onwardRoute

      val application =
        applicationBuilder(userAnswers = Some(filledUserAnswers))
          .overrides(
            bind[ReturnsNavigator].toInstance(mockReturnsNavigator),
            bind[UserAnswersConnector].toInstance(mockUserAnswersConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, tellUsAboutMultipleSPRRateRoute)
            .withFormUrlEncodedBody(
              "volumesWithRate.totalLitres" -> "10000.00",
              "volumesWithRate.pureAlcohol" -> "5000.0000",
              "volumesWithRate.dutyRate"    -> "100",
              "volumesWithRate.taxType"     -> volumeAndRateByTaxType.taxType
            )

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url

        verify(mockUserAnswersConnector, times(1)).set(any())(any())
        verify(mockReturnsNavigator, times(1))
          .nextPageWithRegime(
            eqTo(TellUsAboutMultipleSPRRatePage),
            eqTo(CheckMode),
            any(),
            any(),
            eqTo(true),
            eqTo(Some(index))
          )
      }
    }

    "must redirect to the next page when valid data is submitted, index is provided and no value has changed" in {

      val index = 0

      val tellUsAboutMultipleSPRRateRoute =
        routes.TellUsAboutMultipleSPRRateController.onPageLoad(NormalMode, regime, Some(index)).url

      val volumeAndRateByTaxType = VolumeAndRateByTaxType(
        totalLitres = 1000.12,
        pureAlcohol = 500.1234,
        dutyRate = 10,
        taxType = rateBands.head.taxTypeCode
      )

      val filledUserAnswers =
        userAnswers.setByKey(MultipleSPRListPage, regime, Seq(volumeAndRateByTaxType)).success.value

      val mockUserAnswersConnector = mock[UserAnswersConnector]
      val mockReturnsNavigator     = mock[ReturnsNavigator]

      when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])
      when(
        mockReturnsNavigator.nextPageWithRegime(eqTo(TellUsAboutMultipleSPRRatePage), any(), any(), any(), any(), any())
      ) thenReturn onwardRoute

      val application =
        applicationBuilder(userAnswers = Some(filledUserAnswers))
          .overrides(
            bind[ReturnsNavigator].toInstance(mockReturnsNavigator),
            bind[UserAnswersConnector].toInstance(mockUserAnswersConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, tellUsAboutMultipleSPRRateRoute)
            .withFormUrlEncodedBody(
              "volumesWithRate.totalLitres" -> volumeAndRateByTaxType.totalLitres.toString(),
              "volumesWithRate.pureAlcohol" -> volumeAndRateByTaxType.pureAlcohol.toString(),
              "volumesWithRate.dutyRate"    -> volumeAndRateByTaxType.dutyRate.toString(),
              "volumesWithRate.taxType"     -> volumeAndRateByTaxType.taxType
            )

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url

        verify(mockUserAnswersConnector, times(1)).set(any())(any())
        verify(mockReturnsNavigator, times(1))
          .nextPageWithRegime(
            eqTo(TellUsAboutMultipleSPRRatePage),
            eqTo(NormalMode),
            any(),
            any(),
            eqTo(false),
            eqTo(Some(index))
          )
      }
    }

    "must redirect to the next page when valid data is submitted, index is provided and no value is found the Multiple SPR list" in {

      val index = 0

      val tellUsAboutMultipleSPRRateRoute =
        routes.TellUsAboutMultipleSPRRateController.onPageLoad(NormalMode, regime, Some(index)).url

      val volumeAndRateByTaxType = VolumeAndRateByTaxType(
        totalLitres = 1000.12,
        pureAlcohol = 500.1234,
        dutyRate = 10,
        taxType = rateBands.head.taxTypeCode
      )

      val mockUserAnswersConnector = mock[UserAnswersConnector]
      val mockReturnsNavigator     = mock[ReturnsNavigator]

      when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])
      when(
        mockReturnsNavigator.nextPageWithRegime(eqTo(TellUsAboutMultipleSPRRatePage), any(), any(), any(), any(), any())
      ) thenReturn onwardRoute

      val application =
        applicationBuilder(userAnswers = Some(filledUserAnswers))
          .overrides(
            bind[ReturnsNavigator].toInstance(mockReturnsNavigator),
            bind[UserAnswersConnector].toInstance(mockUserAnswersConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, tellUsAboutMultipleSPRRateRoute)
            .withFormUrlEncodedBody(
              "volumesWithRate.totalLitres" -> volumeAndRateByTaxType.totalLitres.toString(),
              "volumesWithRate.pureAlcohol" -> volumeAndRateByTaxType.pureAlcohol.toString(),
              "volumesWithRate.dutyRate"    -> volumeAndRateByTaxType.dutyRate.toString(),
              "volumesWithRate.taxType"     -> volumeAndRateByTaxType.taxType
            )

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url

        verify(mockUserAnswersConnector, times(1)).set(any())(any())
        verify(mockReturnsNavigator, times(1))
          .nextPageWithRegime(
            eqTo(TellUsAboutMultipleSPRRatePage),
            eqTo(NormalMode),
            any(),
            any(),
            eqTo(false),
            eqTo(Some(index))
          )
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, tellUsAboutMultipleSPRRateRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val form = formProvider()

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[TellUsAboutMultipleSPRRateView]

        val result = route(application, request).value

        val rateBandRadioButton =
          TellUsAboutMultipleSPRRateHelper.radioItems(rateBands, regime)(getMessages(application))

        status(result)          mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, regime, rateBandRadioButton, None)(
          request,
          getMessages(application)
        ).toString
      }
    }

    "must redirect to Journey Recovery for a GET if the index is referring to a not existing spr entry" in {

      val index = 1

      val tellUsAboutMultipleSPRRateRoute =
        routes.TellUsAboutMultipleSPRRateController.onPageLoad(NormalMode, regime, Some(index)).url

      val filledUserAnswers =
        userAnswers.setByKey(MultipleSPRListPage, regime, Seq(volumeAndRateByTaxType)).success.value

      val application = applicationBuilder(userAnswers = Some(filledUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, tellUsAboutMultipleSPRRateRoute)

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, tellUsAboutMultipleSPRRateRoute)

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, tellUsAboutMultipleSPRRateRoute)
            .withFormUrlEncodedBody(
              "volumesWithRate[0].totalLitres" -> "1000",
              "volumesWithRate[0].pureAlcohol" -> "500",
              "volumesWithRate[0].dutyRate"    -> "10",
              "volumesWithRate[0].taxType"     -> "371"
            )

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
