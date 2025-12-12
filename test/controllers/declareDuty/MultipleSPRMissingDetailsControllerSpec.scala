/*
 * Copyright 2025 HM Revenue & Customs
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
import forms.declareDuty.MultipleSPRMissingDetailsFormProvider
import models.NormalMode
import navigation.ReturnsNavigator
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{reset, times, verify, when}
import pages.declareDuty.*
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.Helpers.*
import uk.gov.hmrc.http.HttpResponse
import viewmodels.declareDuty.MissingSPRRateBandHelper
import views.html.declareDuty.MultipleSPRMissingDetailsView

import scala.concurrent.Future

class MultipleSPRMissingDetailsControllerSpec extends SpecBase {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new MultipleSPRMissingDetailsFormProvider()
  val form         = formProvider()
  val regime       = regimeGen.sample.value

  lazy val multipleSPRMissingDetailsRoute =
    controllers.declareDuty.routes.MultipleSPRMissingDetailsController.onPageLoad(regime).url

  val declaredNonSPRRateBands = MultipleSPRMissingDetails.declaredNonSPRRateBands(regime)
  val declaredSPRRateBands    = MultipleSPRMissingDetails.declaredSPRRateBands(regime)
  val allDeclaredRateBands    = declaredNonSPRRateBands ++ declaredSPRRateBands

  val multipleSPRListItems            = Seq(
    volumeAndRateByTaxType2,
    volumeAndRateByTaxType3,
    volumeAndRateByTaxType4.copy(taxType = "127"),
    volumeAndRateByTaxType4.copy(taxType = "128")
  )
  val multipleSPRListWithMissingItems = Seq(
    volumeAndRateByTaxType2,
    volumeAndRateByTaxType3
  )

  val missingSPRRateBands = MultipleSPRMissingDetails.missingSPRRateBands(regime)

  val missingRateBandDescriptions = MultipleSPRMissingDetails.missingRateBandDescriptions(regime)

  val userAnswersWithMissingRateBands = emptyUserAnswers
    .setByKey(WhatDoYouNeedToDeclarePage, regime, allDeclaredRateBands)
    .success
    .value
    .setByKey(DoYouHaveMultipleSPRDutyRatesPage, regime, true)
    .success
    .value
    .setByKey(MultipleSPRListPage, regime, multipleSPRListWithMissingItems)
    .success
    .value

  val mockMissingSPRRateBandHelper = mock[MissingSPRRateBandHelper]
  val mockUserAnswersConnector     = mock[UserAnswersConnector]
  val mockReturnsNavigator         = mock[ReturnsNavigator]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockMissingSPRRateBandHelper, mockUserAnswersConnector, mockReturnsNavigator)

    when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])
    when(
      mockReturnsNavigator.nextPageWithRegime(eqTo(MultipleSPRMissingDetailsPage), any(), any(), any(), any(), any())
    ) thenReturn onwardRoute
  }

  "MultipleSPRMissingDetails Controller" - {
    "must return OK and the correct view for a GET" in {
      when(
        mockMissingSPRRateBandHelper.findMissingSPRRateBands(eqTo(regime), eqTo(userAnswersWithMissingRateBands))
      ) thenReturn Some(missingSPRRateBands)

      when(
        mockMissingSPRRateBandHelper.getMissingRateBandDescriptions(eqTo(regime), eqTo(missingSPRRateBands))(any())
      ) thenReturn missingRateBandDescriptions

      val application = applicationBuilder(userAnswers = Some(userAnswersWithMissingRateBands))
        .overrides(bind[MissingSPRRateBandHelper].toInstance(mockMissingSPRRateBandHelper))
        .build()

      running(application) {
        val request = FakeRequest(GET, multipleSPRMissingDetailsRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[MultipleSPRMissingDetailsView]

        status(result)          mustEqual OK
        contentAsString(result) mustEqual view(form, regime, missingRateBandDescriptions)(
          request,
          getMessages(application)
        ).toString

        verify(mockMissingSPRRateBandHelper, times(1))
          .findMissingSPRRateBands(eqTo(regime), eqTo(userAnswersWithMissingRateBands))
        verify(mockMissingSPRRateBandHelper, times(1))
          .getMissingRateBandDescriptions(eqTo(regime), eqTo(missingSPRRateBands))(any())
      }
    }

    "must redirect to the next page when valid data is submitted" in {
      when(
        mockMissingSPRRateBandHelper.findMissingSPRRateBands(eqTo(regime), eqTo(userAnswersWithMissingRateBands))
      ) thenReturn Some(missingSPRRateBands)

      val application = applicationBuilder(userAnswers = Some(userAnswersWithMissingRateBands))
        .overrides(
          bind[ReturnsNavigator].toInstance(mockReturnsNavigator),
          bind[UserAnswersConnector].toInstance(mockUserAnswersConnector),
          bind[MissingSPRRateBandHelper].toInstance(mockMissingSPRRateBandHelper)
        )
        .build()

      running(application) {
        val request =
          FakeRequest(POST, multipleSPRMissingDetailsRoute)
            .withFormUrlEncodedBody(("addDeclarationDetails", "true"))

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url

        verify(mockMissingSPRRateBandHelper, times(1))
          .findMissingSPRRateBands(eqTo(regime), eqTo(userAnswersWithMissingRateBands))
        verify(mockUserAnswersConnector, times(1)).set(any())(any())
        verify(mockReturnsNavigator, times(1)).nextPageWithRegime(
          eqTo(MultipleSPRMissingDetailsPage),
          eqTo(NormalMode),
          any(),
          eqTo(regime),
          eqTo(false),
          eqTo(null)
        )
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      when(
        mockMissingSPRRateBandHelper.findMissingSPRRateBands(eqTo(regime), eqTo(userAnswersWithMissingRateBands))
      ) thenReturn Some(missingSPRRateBands)

      when(
        mockMissingSPRRateBandHelper.getMissingRateBandDescriptions(eqTo(regime), eqTo(missingSPRRateBands))(any())
      ) thenReturn missingRateBandDescriptions

      val application = applicationBuilder(userAnswers = Some(userAnswersWithMissingRateBands))
        .overrides(bind[MissingSPRRateBandHelper].toInstance(mockMissingSPRRateBandHelper))
        .build()

      running(application) {
        val request =
          FakeRequest(POST, multipleSPRMissingDetailsRoute)
            .withFormUrlEncodedBody(("addDeclarationDetails", ""))

        val boundForm = form.bind(Map("addDeclarationDetails" -> ""))

        val view = application.injector.instanceOf[MultipleSPRMissingDetailsView]

        val result = route(application, request).value

        status(result)          mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, regime, missingRateBandDescriptions)(
          request,
          getMessages(application)
        ).toString

        verify(mockMissingSPRRateBandHelper, times(1))
          .findMissingSPRRateBands(eqTo(regime), eqTo(userAnswersWithMissingRateBands))
        verify(mockMissingSPRRateBandHelper, times(1))
          .getMissingRateBandDescriptions(eqTo(regime), eqTo(missingSPRRateBands))(any())
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, multipleSPRMissingDetailsRoute)

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a GET if user answers do not contain the required data" in {
      val userAnswersWithoutDeclarations =
        userAnswersWithMissingRateBands.removeByKey(WhatDoYouNeedToDeclarePage, regime).success.value

      when(
        mockMissingSPRRateBandHelper.findMissingSPRRateBands(eqTo(regime), eqTo(userAnswersWithoutDeclarations))
      ) thenReturn None

      val application = applicationBuilder(userAnswers = Some(userAnswersWithoutDeclarations))
        .overrides(bind[MissingSPRRateBandHelper].toInstance(mockMissingSPRRateBandHelper))
        .build()

      running(application) {
        val request = FakeRequest(GET, multipleSPRMissingDetailsRoute)

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url

        verify(mockMissingSPRRateBandHelper, times(1))
          .findMissingSPRRateBands(eqTo(regime), eqTo(userAnswersWithoutDeclarations))
      }
    }

    "must redirect to Journey Recovery for a GET if there are no missing rate bands" in {
      val userAnswersWithoutMissingRateBands =
        userAnswersWithMissingRateBands.setByKey(MultipleSPRListPage, regime, multipleSPRListItems).success.value

      when(
        mockMissingSPRRateBandHelper.findMissingSPRRateBands(eqTo(regime), eqTo(userAnswersWithoutMissingRateBands))
      ) thenReturn Some(Set.empty)

      val application = applicationBuilder(userAnswers = Some(userAnswersWithoutMissingRateBands))
        .overrides(bind[MissingSPRRateBandHelper].toInstance(mockMissingSPRRateBandHelper))
        .build()

      running(application) {
        val request = FakeRequest(GET, multipleSPRMissingDetailsRoute)

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url

        verify(mockMissingSPRRateBandHelper, times(1))
          .findMissingSPRRateBands(eqTo(regime), eqTo(userAnswersWithoutMissingRateBands))
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, multipleSPRMissingDetailsRoute)
            .withFormUrlEncodedBody(("addDeclarationDetails", "true"))

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if user answers do not contain the required data" in {
      val userAnswersWithoutMultipleSPRList =
        userAnswersWithMissingRateBands.removeByKey(MultipleSPRListPage, regime).success.value

      when(
        mockMissingSPRRateBandHelper.findMissingSPRRateBands(eqTo(regime), eqTo(userAnswersWithoutMultipleSPRList))
      ) thenReturn None

      val application = applicationBuilder(userAnswers = Some(userAnswersWithoutMultipleSPRList))
        .overrides(bind[MissingSPRRateBandHelper].toInstance(mockMissingSPRRateBandHelper))
        .build()

      running(application) {
        val request =
          FakeRequest(POST, multipleSPRMissingDetailsRoute)
            .withFormUrlEncodedBody(("addDeclarationDetails", "true"))

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url

        verify(mockMissingSPRRateBandHelper, times(1))
          .findMissingSPRRateBands(eqTo(regime), eqTo(userAnswersWithoutMultipleSPRList))
      }
    }

    "must redirect to Journey Recovery for a POST if there are no missing rate bands" in {
      val userAnswersWithoutMissingRateBands =
        userAnswersWithMissingRateBands.setByKey(MultipleSPRListPage, regime, multipleSPRListItems).success.value

      when(
        mockMissingSPRRateBandHelper.findMissingSPRRateBands(eqTo(regime), eqTo(userAnswersWithoutMissingRateBands))
      ) thenReturn Some(Set.empty)

      val application = applicationBuilder(userAnswers = Some(userAnswersWithoutMissingRateBands))
        .overrides(bind[MissingSPRRateBandHelper].toInstance(mockMissingSPRRateBandHelper))
        .build()

      running(application) {
        val request =
          FakeRequest(POST, multipleSPRMissingDetailsRoute)
            .withFormUrlEncodedBody(("addDeclarationDetails", "true"))

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url

        verify(mockMissingSPRRateBandHelper, times(1))
          .findMissingSPRRateBands(eqTo(regime), eqTo(userAnswersWithoutMissingRateBands))
      }
    }
  }
}
