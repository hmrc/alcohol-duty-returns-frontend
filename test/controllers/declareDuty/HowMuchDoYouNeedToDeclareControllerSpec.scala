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
import viewmodels.declareDuty.{CategoriesByRateTypeHelper, CategoriesByRateTypeViewModel, CategoryViewModel, RateBandDescription}
import views.html.declareDuty.HowMuchDoYouNeedToDeclareView

import scala.concurrent.Future

class HowMuchDoYouNeedToDeclareControllerSpec extends SpecBase {
  "HowMuchDoYouNeedToDeclare Controller" - {
    "must return OK and the correct view for a GET" in new SetUp {
      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[CategoriesByRateTypeHelper].toInstance(mockCategoriesByRateTypeHelper)
        )
        .build()

      when(mockCategoriesByRateTypeHelper.rateBandCategories(any(), any())(any())).thenReturn(returnSummaryList)

      running(application) {
        val request = FakeRequest(GET, howMuchDoYouNeedToDeclareRoute)

        val view = application.injector.instanceOf[HowMuchDoYouNeedToDeclareView]

        val result = route(application, request).value

        val form = formProvider(regime)(getMessages(application))

        status(result)          mustEqual OK
        contentAsString(result) mustEqual view(form, regime, returnSummaryList, NormalMode)(
          request,
          getMessages(application)
        ).toString
      }

      verify(mockCategoriesByRateTypeHelper, times(1)).rateBandCategories(rateBands, regime)(getMessages(application))
    }

    "must populate the view correctly on a GET when the question has previously been answered" in new SetUp {
      val application = applicationBuilder(userAnswers = Some(filledUserAnswers))
        .overrides(
          bind[CategoriesByRateTypeHelper].toInstance(mockCategoriesByRateTypeHelper)
        )
        .build()

      when(mockCategoriesByRateTypeHelper.rateBandCategories(any(), any())(any())).thenReturn(returnSummaryList)

      running(application) {
        val request = FakeRequest(GET, howMuchDoYouNeedToDeclareRoute)

        val view = application.injector.instanceOf[HowMuchDoYouNeedToDeclareView]

        val result = route(application, request).value

        val form = formProvider(regime)(getMessages(application))
        status(result)          mustEqual OK
        contentAsString(result) mustEqual view(
          form.fill(volumeAndRateByTaxTypes.map(_.toVolumes)),
          regime,
          returnSummaryList,
          NormalMode
        )(
          request,
          getMessages(application)
        ).toString
      }

      verify(mockCategoriesByRateTypeHelper, times(1)).rateBandCategories(rateBands, regime)(getMessages(application))
    }

    "must redirect to the next page when valid data is submitted" in new SetUp {
      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[ReturnsNavigator].toInstance(mockReturnsNavigator),
            bind[UserAnswersConnector].toInstance(mockUserAnswersConnector)
          )
          .build()

      when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])
      when(
        mockReturnsNavigator.nextPageWithRegime(eqTo(HowMuchDoYouNeedToDeclarePage), any(), any(), any(), any(), any())
      ) thenReturn onwardRoute

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

    "must return a Bad Request and errors when invalid data is submitted" in new SetUp {
      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[CategoriesByRateTypeHelper].toInstance(mockCategoriesByRateTypeHelper)
        )
        .build()

      when(mockCategoriesByRateTypeHelper.rateBandCategories(any(), any())(any())).thenReturn(returnSummaryList)

      running(application) {
        val request =
          FakeRequest(POST, howMuchDoYouNeedToDeclareRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val form = formProvider(regime)(getMessages(application))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[HowMuchDoYouNeedToDeclareView]

        val result = route(application, request).value

        status(result)          mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, regime, returnSummaryList, NormalMode)(
          request,
          getMessages(application)
        ).toString
      }

      verify(mockCategoriesByRateTypeHelper, times(1)).rateBandCategories(rateBands, regime)(getMessages(application))
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in new SetUp {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, howMuchDoYouNeedToDeclareRoute)

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a GET if empty user data is provided" in new SetUp {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, howMuchDoYouNeedToDeclareRoute)

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in new SetUp {
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

    "must redirect to Journey Recovery for a POST if empty user data is provided" in new SetUp {
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

    "must redirect to Journey Recovery for a POST if incoherent data is found" in new SetUp {
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
        ) must have message "Failed to find rate band for tax type"
      }
    }
  }

  class SetUp {
    def onwardRoute = Call("GET", "/foo")

    val mockCategoriesByRateTypeHelper = mock[CategoriesByRateTypeHelper]
    val mockUserAnswersConnector       = mock[UserAnswersConnector]
    val mockReturnsNavigator           = mock[ReturnsNavigator]

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

    val returnSummaryList = CategoriesByRateTypeViewModel(
      core = Seq.empty,
      draught = rateBands.map(rateBand =>
        CategoryViewModel(
          taxTypeCode = rateBand.taxTypeCode,
          description = RateBandDescription.toDescription(rateBand, Some(regime))(getMessages(app))
        )
      ).toSeq,
      smallProducer = Seq.empty,
      draughtAndSmallProducer = Seq.empty
    )
  }
}
