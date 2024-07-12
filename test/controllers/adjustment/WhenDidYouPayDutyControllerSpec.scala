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
import forms.adjustment.WhenDidYouPayDutyFormProvider
import models.NormalMode
import navigation.{AdjustmentNavigator, FakeAdjustmentNavigator}
import org.mockito.ArgumentMatchers.any
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.Helpers._
import connectors.CacheConnector
import models.adjustment.AdjustmentType.Spoilt
import models.adjustment.{AdjustmentEntry, AdjustmentType}
import pages.adjustment.CurrentAdjustmentEntryPage
import uk.gov.hmrc.http.HttpResponse
import views.html.adjustment.WhenDidYouPayDutyView

import java.time.YearMonth
import scala.concurrent.Future

class WhenDidYouPayDutyControllerSpec extends SpecBase {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new WhenDidYouPayDutyFormProvider()
  val form         = formProvider()

  lazy val whenDidYouPayDutyRoute = routes.WhenDidYouPayDutyController.onPageLoad(NormalMode).url

  val adjustmentType = AdjustmentType.Overdeclaration
  val period         = YearMonth.of(2024, 1)

  val validEmptyUserAnswers = emptyUserAnswers
    .set(CurrentAdjustmentEntryPage, AdjustmentEntry(adjustmentType = Some(AdjustmentType.Overdeclaration)))
    .success
    .value

  val userAnswers = emptyUserAnswers
    .set(
      CurrentAdjustmentEntryPage,
      AdjustmentEntry(
        adjustmentType = Some(adjustmentType),
        period = Some(period)
      )
    )
    .success
    .value

  "WhenDidYouPayDuty Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(validEmptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, whenDidYouPayDutyRoute)

        val view = application.injector.instanceOf[WhenDidYouPayDutyView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, adjustmentType)(
          request,
          messages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, whenDidYouPayDutyRoute)

        val view = application.injector.instanceOf[WhenDidYouPayDutyView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(period), NormalMode, adjustmentType)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockCacheConnector = mock[CacheConnector]

      when(mockCacheConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])

      val application =
        applicationBuilder(userAnswers = Some(validEmptyUserAnswers))
          .overrides(
            bind[AdjustmentNavigator].toInstance(new FakeAdjustmentNavigator(onwardRoute, hasValueChanged = true)),
            bind[CacheConnector].toInstance(mockCacheConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, whenDidYouPayDutyRoute)
            .withFormUrlEncodedBody(
              ("when-did-you-pay-duty-input.month", "1"),
              ("when-did-you-pay-duty-input.year", "2024")
            )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must redirect to the next page when valid data is submitted and the user answers are empty" in {

      val mockCacheConnector = mock[CacheConnector]

      when(mockCacheConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[AdjustmentNavigator].toInstance(new FakeAdjustmentNavigator(onwardRoute, hasValueChanged = true)),
            bind[CacheConnector].toInstance(mockCacheConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, whenDidYouPayDutyRoute)
            .withFormUrlEncodedBody(
              ("when-did-you-pay-duty-input.month", "1"),
              ("when-did-you-pay-duty-input.year", "2024")
            )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must redirect to the next page when the same data is submitted" in {
      val userAnswers =
        emptyUserAnswers
          .set(
            CurrentAdjustmentEntryPage,
            AdjustmentEntry(
              adjustmentType = Some(Spoilt),
              period = Some(YearMonth.of(2024, 1))
            )
          )
          .success
          .value

      val mockCacheConnector = mock[CacheConnector]

      when(mockCacheConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[AdjustmentNavigator].toInstance(new FakeAdjustmentNavigator(onwardRoute, hasValueChanged = false)),
            bind[CacheConnector].toInstance(mockCacheConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, whenDidYouPayDutyRoute)
            .withFormUrlEncodedBody(
              ("when-did-you-pay-duty-input.month", "1"),
              ("when-did-you-pay-duty-input.year", "2024")
            )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(validEmptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, whenDidYouPayDutyRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[WhenDidYouPayDutyView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, adjustmentType)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, whenDidYouPayDutyRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a GET if userAnswers are empty" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, whenDidYouPayDutyRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, whenDidYouPayDutyRoute)
            .withFormUrlEncodedBody(("month", "value 1"), ("year", "value 2"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }

  "must redirect to Journey Recovery for a POST with invalid data and empty user answers" in {

    val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

    running(application) {
      val request =
        FakeRequest(POST, whenDidYouPayDutyRoute)
          .withFormUrlEncodedBody(("month", "value 1"), ("year", "value 2"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
    }
  }

  "throw an exception for a GET if adjustmentType is not defined and period is defined" in {
    val adjustmentEntry     = AdjustmentEntry(
      period = Some(period)
    )
    val previousUserAnswers = emptyUserAnswers.set(CurrentAdjustmentEntryPage, adjustmentEntry).success.value

    val application = applicationBuilder(userAnswers = Some(previousUserAnswers)).build()

    running(application) {
      val request = FakeRequest(GET, whenDidYouPayDutyRoute)

      val result = route(application, request).value

      whenReady(result.failed) { exception =>
        exception mustBe a[RuntimeException]
        exception.getMessage mustEqual "Couldn't fetch adjustment type value from cache"
      }
    }
  }
  "throw an exception for a GET if adjustmentType is not defined" in {
    val adjustmentEntry     = AdjustmentEntry(
      totalLitresVolume = Some(BigDecimal(0))
    )
    val previousUserAnswers = emptyUserAnswers.set(CurrentAdjustmentEntryPage, adjustmentEntry).success.value

    val application = applicationBuilder(userAnswers = Some(previousUserAnswers)).build()

    running(application) {
      val request = FakeRequest(GET, whenDidYouPayDutyRoute)

      val result = route(application, request).value

      whenReady(result.failed) { exception =>
        exception mustBe a[RuntimeException]
        exception.getMessage mustEqual "Couldn't fetch adjustment type value from cache"
      }
    }
  }

//  "must throw an exception for a POST if adjustmentType is not defined" in {
//    val adjustmentEntry     = AdjustmentEntry(
//      period = Some(period)
//    )
//    val previousUserAnswers = emptyUserAnswers.set(CurrentAdjustmentEntryPage, adjustmentEntry).success.value
//    val application         = applicationBuilder(userAnswers = Some(previousUserAnswers)).build()
//
//    running(application) {
//      val request =
//        FakeRequest(POST, whenDidYouPayDutyRoute)
//          .withFormUrlEncodedBody(
//            ("when-did-you-pay-duty-input.month", "1"),
//            ("when-did-you-pay-duty-input.year", "2024")
//          )
//
//      val result = route(application, request).value
//      whenReady(result.failed) { exception =>
//        exception mustBe a[RuntimeException]
//        exception.getMessage mustEqual "Couldn't fetch adjustment type value from cache"
//      }
//    }
//  }
}
