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
import config.Constants.periodKeySessionKey
import config.FrontendAppConfig
import forms.adjustment.WhenDidYouPayDutyFormProvider
import models.NormalMode
import navigation.{AdjustmentNavigator, FakeAdjustmentNavigator}
import org.mockito.ArgumentMatchers.any
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.Helpers._
import connectors.UserAnswersConnector
import models.adjustment.AdjustmentType.Spoilt
import models.adjustment.{AdjustmentEntry, AdjustmentType}
import pages.adjustment.CurrentAdjustmentEntryPage
import uk.gov.hmrc.http.HttpResponse
import views.html.adjustment.WhenDidYouPayDutyView

import scala.concurrent.Future

class WhenDidYouPayDutyControllerSpec extends SpecBase {

  def onwardRoute = Call("GET", "/foo")

  val formProvider          = new WhenDidYouPayDutyFormProvider()
  val returnPeriodYearMonth = returnPeriod.period
  val form                  = formProvider(returnPeriodYearMonth)

  lazy val whenDidYouPayDutyRoute = routes.WhenDidYouPayDutyController.onPageLoad(NormalMode).url

  val adjustmentType = AdjustmentType.Overdeclaration
  val validPeriod    = returnPeriodYearMonth.minusMonths(1)

  val validEmptyUserAnswers = emptyUserAnswers
    .set(CurrentAdjustmentEntryPage, AdjustmentEntry(adjustmentType = Some(AdjustmentType.Overdeclaration)))
    .success
    .value

  val userAnswers = emptyUserAnswers
    .set(
      CurrentAdjustmentEntryPage,
      AdjustmentEntry(
        adjustmentType = Some(adjustmentType),
        period = Some(validPeriod)
      )
    )
    .success
    .value

  val monthString = validPeriod.getMonthValue.toString
  val yearString  = validPeriod.getYear.toString

  "WhenDidYouPayDuty Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(validEmptyUserAnswers)).build()

      running(application) {
        val appConfig = application.injector.instanceOf[FrontendAppConfig]

        val request = FakeRequest(GET, whenDidYouPayDutyRoute)

        val view = application.injector.instanceOf[WhenDidYouPayDutyView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, adjustmentType, appConfig.exciseEnquiriesUrl)(
          request,
          getMessages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val appConfig = application.injector.instanceOf[FrontendAppConfig]

        val request = FakeRequest(GET, whenDidYouPayDutyRoute)

        val view = application.injector.instanceOf[WhenDidYouPayDutyView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form.fill(validPeriod),
          NormalMode,
          adjustmentType,
          appConfig.exciseEnquiriesUrl
        )(
          request,
          getMessages(application)
        ).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {
      val mockUserAnswersConnector = mock[UserAnswersConnector]

      when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])

      val application =
        applicationBuilder(userAnswers = Some(validEmptyUserAnswers))
          .overrides(
            bind[AdjustmentNavigator].toInstance(new FakeAdjustmentNavigator(onwardRoute, hasValueChanged = true)),
            bind[UserAnswersConnector].toInstance(mockUserAnswersConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, whenDidYouPayDutyRoute)
            .withSession((periodKeySessionKey, periodKey))
            .withFormUrlEncodedBody(
              ("when-did-you-pay-duty-input.month", monthString),
              ("when-did-you-pay-duty-input.year", yearString)
            )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must redirect to the next page when valid data is submitted and the user answers are empty" in {

      val mockUserAnswersConnector = mock[UserAnswersConnector]

      when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[AdjustmentNavigator].toInstance(new FakeAdjustmentNavigator(onwardRoute, hasValueChanged = true)),
            bind[UserAnswersConnector].toInstance(mockUserAnswersConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, whenDidYouPayDutyRoute)
            .withFormUrlEncodedBody(
              ("when-did-you-pay-duty-input.month", monthString),
              ("when-did-you-pay-duty-input.year", yearString)
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
              period = Some(validPeriod)
            )
          )
          .success
          .value

      val mockUserAnswersConnector = mock[UserAnswersConnector]

      when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[AdjustmentNavigator].toInstance(new FakeAdjustmentNavigator(onwardRoute, hasValueChanged = false)),
            bind[UserAnswersConnector].toInstance(mockUserAnswersConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, whenDidYouPayDutyRoute)
            .withFormUrlEncodedBody(
              ("when-did-you-pay-duty-input.month", monthString),
              ("when-did-you-pay-duty-input.year", yearString)
            )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(validEmptyUserAnswers)).build()

      running(application) {
        val appConfig = application.injector.instanceOf[FrontendAppConfig]

        val request =
          FakeRequest(POST, whenDidYouPayDutyRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[WhenDidYouPayDutyView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, adjustmentType, appConfig.exciseEnquiriesUrl)(
          request,
          getMessages(application)
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
}
