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

package controllers.adjustment

import base.SpecBase
import connectors.UserAnswersConnector
import forms.adjustment.AdjustmentTypeFormProvider
import models.AlcoholRegime.Beer
import models.adjustment.AdjustmentType.Spoilt
import models.adjustment.{AdjustmentEntry, AdjustmentType}
import models.{AlcoholRegimes, NormalMode}
import navigation.AdjustmentNavigator
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import pages.adjustment.{AdjustmentTypePage, CurrentAdjustmentEntryPage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.Helpers._
import uk.gov.hmrc.http.HttpResponse
import views.html.adjustment.AdjustmentTypeView

import scala.concurrent.Future

class AdjustmentTypeControllerSpec extends SpecBase {

  def onwardRoute = Call("GET", "/foo")

  lazy val adjustmentTypeRoute = routes.AdjustmentTypeController.onPageLoad(NormalMode).url

  val formProvider = new AdjustmentTypeFormProvider()
  val form         = formProvider()

  "AdjustmentType Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, adjustmentTypeRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AdjustmentTypeView]

        status(result)          mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode)(request, getMessages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val spoilt      = AdjustmentType.values.head
      val userAnswers =
        emptyUserAnswers
          .set(CurrentAdjustmentEntryPage, AdjustmentEntry(adjustmentType = Some(spoilt)))
          .success
          .value
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, adjustmentTypeRoute)

        val view = application.injector.instanceOf[AdjustmentTypeView]

        val result = route(application, request).value

        status(result)          mustEqual OK
        contentAsString(result) mustEqual view(form.fill(AdjustmentType.values.head), NormalMode)(
          request,
          getMessages(application)
        ).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockUserAnswersConnector = mock[UserAnswersConnector]
      val mockAdjustmentNavigator  = mock[AdjustmentNavigator]

      when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])
      when(mockAdjustmentNavigator.nextPage(eqTo(AdjustmentTypePage), any(), any(), any())) thenReturn onwardRoute

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[AdjustmentNavigator].toInstance(mockAdjustmentNavigator),
            bind[UserAnswersConnector].toInstance(mockUserAnswersConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, adjustmentTypeRoute)
            .withFormUrlEncodedBody(("adjustment-type-value", AdjustmentType.values.head.toString))

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url

        verify(mockUserAnswersConnector, times(1)).set(any())(any())
        verify(mockAdjustmentNavigator, times(1))
          .nextPage(eqTo(AdjustmentTypePage), eqTo(NormalMode), any(), eqTo(Some(true)))
      }
    }

    "must redirect to the next page when the same data is submitted" in {
      val userAnswers =
        emptyUserAnswers
          .set(
            CurrentAdjustmentEntryPage,
            AdjustmentEntry(
              adjustmentType = Some(Spoilt)
            )
          )
          .success
          .value

      val mockUserAnswersConnector = mock[UserAnswersConnector]
      val mockAdjustmentNavigator  = mock[AdjustmentNavigator]

      when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])
      when(mockAdjustmentNavigator.nextPage(eqTo(AdjustmentTypePage), any(), any(), any())) thenReturn onwardRoute

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[AdjustmentNavigator].toInstance(mockAdjustmentNavigator),
            bind[UserAnswersConnector].toInstance(mockUserAnswersConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, adjustmentTypeRoute)
            .withFormUrlEncodedBody(("adjustment-type-value", Spoilt.toString))

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url

        verify(mockUserAnswersConnector, times(1)).set(any())(any())
        verify(mockAdjustmentNavigator, times(1))
          .nextPage(eqTo(AdjustmentTypePage), eqTo(NormalMode), any(), eqTo(Some(false)))
      }
    }

    "must redirect to the next page when valid data is submitted and user is approved for a single regime when the same data is submitted" in {

      val userAnswers = emptyUserAnswers
        .copy(regimes = AlcoholRegimes(Set(Beer)))
        .set(
          CurrentAdjustmentEntryPage,
          AdjustmentEntry(
            adjustmentType = Some(Spoilt)
          )
        )
        .success
        .value

      val mockUserAnswersConnector = mock[UserAnswersConnector]
      val mockAdjustmentNavigator  = mock[AdjustmentNavigator]

      when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])
      when(mockAdjustmentNavigator.nextPage(eqTo(AdjustmentTypePage), any(), any(), any())) thenReturn onwardRoute

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[AdjustmentNavigator].toInstance(mockAdjustmentNavigator),
            bind[UserAnswersConnector].toInstance(mockUserAnswersConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, adjustmentTypeRoute)
            .withFormUrlEncodedBody(("adjustment-type-value", Spoilt.toString))

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url

        verify(mockUserAnswersConnector, times(1)).set(any())(any())
        verify(mockAdjustmentNavigator, times(1))
          .nextPage(eqTo(AdjustmentTypePage), eqTo(NormalMode), any(), eqTo(Some(false)))
      }
    }

    "must redirect to the next page when valid data is submitted and user is approved for a single regime when CurrentAdjustmentEntryPage is empty" in {

      val userAnswers = emptyUserAnswers.copy(regimes = AlcoholRegimes(Set(Beer)))

      val mockUserAnswersConnector = mock[UserAnswersConnector]
      val mockAdjustmentNavigator  = mock[AdjustmentNavigator]

      when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])
      when(mockAdjustmentNavigator.nextPage(eqTo(AdjustmentTypePage), any(), any(), any())) thenReturn onwardRoute

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[AdjustmentNavigator].toInstance(mockAdjustmentNavigator),
            bind[UserAnswersConnector].toInstance(mockUserAnswersConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, adjustmentTypeRoute)
            .withFormUrlEncodedBody(("adjustment-type-value", Spoilt.toString))

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url

        verify(mockUserAnswersConnector, times(1)).set(any())(any())
        verify(mockAdjustmentNavigator, times(1))
          .nextPage(eqTo(AdjustmentTypePage), eqTo(NormalMode), any(), eqTo(Some(true)))
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, adjustmentTypeRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[AdjustmentTypeView]

        val result = route(application, request).value

        status(result)          mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, getMessages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, adjustmentTypeRoute)

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, adjustmentTypeRoute)
            .withFormUrlEncodedBody(("value", AdjustmentType.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
