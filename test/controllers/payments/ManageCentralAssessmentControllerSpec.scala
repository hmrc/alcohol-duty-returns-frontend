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

package controllers.payments

import base.SpecBase
import config.Constants.pastPaymentsSessionKey
import forms.payments.ManageCentralAssessmentFormProvider
import org.mockito.ArgumentMatchers.{any, argThat, eq as eqTo}
import org.mockito.Mockito.{times, verify, when}
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.Session
import play.api.test.Helpers._
import viewmodels.payments.CentralAssessmentHelper
import views.html.payments.ManageCentralAssessmentView

class ManageCentralAssessmentControllerSpec extends SpecBase {

  val formProvider = new ManageCentralAssessmentFormProvider()
  val form         = formProvider()

  lazy val manageCentralAssessmentRoute =
    controllers.payments.routes.ManageCentralAssessmentController.onPageLoad(chargeReference).url

  lazy val manageCentralAssessmentPostRoute =
    controllers.payments.routes.ManageCentralAssessmentController.onSubmit(chargeReference).url

  val sessionData = (pastPaymentsSessionKey, Json.toJson(openPaymentsData.outstandingPayments).toString)

  "ManageCentralAssessment Controller" - {
    "must return OK and the correct view for a GET" in {
      val mockCentralAssessmentHelper = mock[CentralAssessmentHelper]

      when(mockCentralAssessmentHelper.getCentralAssessmentChargeFromSession(any(), any())) thenReturn
        Some((outstandingCAPayment, 3))

      when(mockCentralAssessmentHelper.getCentralAssessmentViewModel(any())(any())) thenReturn
        centralAssessmentViewModel

      val application = applicationBuilder()
        .overrides(bind[CentralAssessmentHelper].toInstance(mockCentralAssessmentHelper))
        .build()

      running(application) {
        val request = FakeRequestNoPeriodKey(GET, manageCentralAssessmentRoute).withSession(sessionData)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ManageCentralAssessmentView]

        status(result)          mustEqual OK
        contentAsString(result) mustEqual view(form, centralAssessmentViewModel)(
          request,
          getMessages(application)
        ).toString

        verify(mockCentralAssessmentHelper, times(1)).getCentralAssessmentChargeFromSession(
          argThat[Session](_.data.get(sessionData._1).contains(sessionData._2)),
          eqTo(chargeReference)
        )
        verify(mockCentralAssessmentHelper, times(1))
          .getCentralAssessmentViewModel(eqTo(outstandingCAPayment))(any())
      }
    }

    "must redirect to Journey Recovery for a GET if the helper cannot get the charge from session data" in {
      val mockCentralAssessmentHelper = mock[CentralAssessmentHelper]

      when(mockCentralAssessmentHelper.getCentralAssessmentChargeFromSession(any(), any())) thenReturn None

      val application = applicationBuilder()
        .overrides(bind[CentralAssessmentHelper].toInstance(mockCentralAssessmentHelper))
        .build()

      running(application) {
        val request = FakeRequestWithoutSession(GET, manageCentralAssessmentRoute)

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url

        verify(mockCentralAssessmentHelper, times(1)).getCentralAssessmentChargeFromSession(
          argThat[Session](!_.data.contains(sessionData._1)),
          eqTo(chargeReference)
        )
        verify(mockCentralAssessmentHelper, times(0)).getCentralAssessmentViewModel(any())(any())
      }
    }

    "must redirect to the Check Your Returns page on submit" in {
      val mockCentralAssessmentHelper = mock[CentralAssessmentHelper]

      when(mockCentralAssessmentHelper.getCentralAssessmentChargeFromSession(any(), any())) thenReturn
        Some((outstandingCAPayment, 3))

      val application = applicationBuilder()
        .overrides(bind[CentralAssessmentHelper].toInstance(mockCentralAssessmentHelper))
        .build()

      running(application) {
        val request = FakeRequestNoPeriodKey(POST, manageCentralAssessmentPostRoute)
          .withSession(sessionData)
          .withFormUrlEncodedBody(("submitReturn", "true"))

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.returns.routes.ViewPastReturnsController.onPageLoad.url

        verify(mockCentralAssessmentHelper, times(1)).getCentralAssessmentChargeFromSession(
          argThat[Session](_.data.get(sessionData._1).contains(sessionData._2)),
          eqTo(chargeReference)
        )
        verify(mockCentralAssessmentHelper, times(0)).getCentralAssessmentViewModel(any())(any())
      }
    }

    "must redirect to the Pay central assessment charge page on submit" in {
      val mockCentralAssessmentHelper = mock[CentralAssessmentHelper]

      when(mockCentralAssessmentHelper.getCentralAssessmentChargeFromSession(any(), any())) thenReturn
        Some((outstandingCAPayment, 3))

      val application = applicationBuilder()
        .overrides(bind[CentralAssessmentHelper].toInstance(mockCentralAssessmentHelper))
        .build()

      running(application) {
        val request = FakeRequestNoPeriodKey(POST, manageCentralAssessmentPostRoute)
          .withSession(sessionData)
          .withFormUrlEncodedBody(("submitReturn", "false"))

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.payments.routes.PayCentralAssessmentController.onPageLoad(chargeReference).url

        verify(mockCentralAssessmentHelper, times(1)).getCentralAssessmentChargeFromSession(
          argThat[Session](_.data.get(sessionData._1).contains(sessionData._2)),
          eqTo(chargeReference)
        )
        verify(mockCentralAssessmentHelper, times(0)).getCentralAssessmentViewModel(any())(any())
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      val mockCentralAssessmentHelper = mock[CentralAssessmentHelper]

      when(mockCentralAssessmentHelper.getCentralAssessmentChargeFromSession(any(), any())) thenReturn
        Some((outstandingCAPayment, 3))

      when(mockCentralAssessmentHelper.getCentralAssessmentViewModel(any())(any())) thenReturn
        centralAssessmentViewModel

      val application = applicationBuilder()
        .overrides(bind[CentralAssessmentHelper].toInstance(mockCentralAssessmentHelper))
        .build()

      running(application) {
        val request = FakeRequestNoPeriodKey(POST, manageCentralAssessmentPostRoute)
          .withSession(sessionData)
          .withFormUrlEncodedBody(("submitReturn", ""))

        val boundForm = form.bind(Map("submitReturn" -> ""))

        val view = application.injector.instanceOf[ManageCentralAssessmentView]

        val result = route(application, request).value

        status(result)          mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, centralAssessmentViewModel)(
          request,
          getMessages(application)
        ).toString

        verify(mockCentralAssessmentHelper, times(1)).getCentralAssessmentChargeFromSession(
          argThat[Session](_.data.get(sessionData._1).contains(sessionData._2)),
          eqTo(chargeReference)
        )
        verify(mockCentralAssessmentHelper, times(1))
          .getCentralAssessmentViewModel(eqTo(outstandingCAPayment))(any())
      }
    }

    "must redirect to Journey Recovery for a POST if the helper cannot get the charge from session data" in {
      val mockCentralAssessmentHelper = mock[CentralAssessmentHelper]

      when(mockCentralAssessmentHelper.getCentralAssessmentChargeFromSession(any(), any())) thenReturn None

      val application = applicationBuilder()
        .overrides(bind[CentralAssessmentHelper].toInstance(mockCentralAssessmentHelper))
        .build()

      running(application) {
        val request = FakeRequestWithoutSession(POST, manageCentralAssessmentPostRoute)
          .withFormUrlEncodedBody(("submitReturn", "true"))

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url

        verify(mockCentralAssessmentHelper, times(1)).getCentralAssessmentChargeFromSession(
          argThat[Session](!_.data.contains(sessionData._1)),
          eqTo(chargeReference)
        )
        verify(mockCentralAssessmentHelper, times(0)).getCentralAssessmentViewModel(any())(any())
      }
    }
  }
}
