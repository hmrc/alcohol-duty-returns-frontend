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
import config.FrontendAppConfig
import connectors.UserAnswersConnector
import forms.adjustment.AdjustmentReturnPeriodFormProvider
import models.NormalMode
import models.adjustment.AdjustmentType.Spoilt
import models.adjustment.{AdjustmentEntry, AdjustmentType}
import navigation.AdjustmentNavigator
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import pages.adjustment.{AdjustmentReturnPeriodPage, CurrentAdjustmentEntryPage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.Helpers._
import uk.gov.hmrc.http.HttpResponse
import views.html.adjustment.AdjustmentReturnPeriodView
import viewmodels.adjustment.{AdjustmentReturnPeriodHelper, AdjustmentReturnPeriodViewModel}

import scala.concurrent.Future

class AdjustmentReturnPeriodControllerSpec extends SpecBase {

  def onwardRoute = Call("GET", "/foo")

  val formProvider          = new AdjustmentReturnPeriodFormProvider()
  val returnPeriodYearMonth = returnPeriod.period
  val form                  = formProvider(returnPeriodYearMonth)

  lazy val adjustmentReturnPeriodRoute = routes.AdjustmentReturnPeriodController.onPageLoad(NormalMode).url

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

  "AdjustmentReturnPeriod Controller" - {

    "must return OK and the correct view for a GET" in {
      val mockHelper = mock[AdjustmentReturnPeriodHelper]

      when(mockHelper.createViewModel(any()))
        .thenReturn(AdjustmentReturnPeriodViewModel(adjustmentType, appConfig.exciseEnquiriesUrl))

      val application = applicationBuilder(userAnswers = Some(validEmptyUserAnswers))
        .overrides(bind[AdjustmentReturnPeriodHelper].toInstance(mockHelper))
        .build()

      running(application) {
        val appConfig = application.injector.instanceOf[FrontendAppConfig]

        val request = FakeRequest(GET, adjustmentReturnPeriodRoute)

        val view = application.injector.instanceOf[AdjustmentReturnPeriodView]

        val result = route(application, request).value

        status(result)          mustEqual OK
        contentAsString(result) mustEqual view(
          form,
          NormalMode,
          AdjustmentReturnPeriodViewModel(adjustmentType, appConfig.exciseEnquiriesUrl)
        )(
          request,
          getMessages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val mockHelper = mock[AdjustmentReturnPeriodHelper]

      when(mockHelper.createViewModel(any()))
        .thenReturn(AdjustmentReturnPeriodViewModel(adjustmentType, appConfig.exciseEnquiriesUrl))

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(bind[AdjustmentReturnPeriodHelper].toInstance(mockHelper))
        .build()

      running(application) {
        val appConfig = application.injector.instanceOf[FrontendAppConfig]

        val request = FakeRequest(GET, adjustmentReturnPeriodRoute)

        val view = application.injector.instanceOf[AdjustmentReturnPeriodView]

        val result = route(application, request).value

        status(result)          mustEqual OK
        contentAsString(result) mustEqual view(
          form.fill(validPeriod),
          NormalMode,
          AdjustmentReturnPeriodViewModel(adjustmentType, appConfig.exciseEnquiriesUrl)
        )(
          request,
          getMessages(application)
        ).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {
      val mockUserAnswersConnector = mock[UserAnswersConnector]
      val mockAdjustmentNavigator  = mock[AdjustmentNavigator]
      val mockHelper               = mock[AdjustmentReturnPeriodHelper]

      when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])
      when(
        mockAdjustmentNavigator.nextPage(eqTo(AdjustmentReturnPeriodPage), any(), any(), any())
      ) thenReturn onwardRoute
      when(mockHelper.createViewModel(any()))
        .thenReturn(AdjustmentReturnPeriodViewModel(adjustmentType, appConfig.exciseEnquiriesUrl))

      val application =
        applicationBuilder(userAnswers = Some(validEmptyUserAnswers))
          .overrides(
            bind[AdjustmentNavigator].toInstance(mockAdjustmentNavigator),
            bind[UserAnswersConnector].toInstance(mockUserAnswersConnector),
            bind[AdjustmentReturnPeriodHelper].toInstance(mockHelper)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, adjustmentReturnPeriodRoute)
            .withFormUrlEncodedBody(
              ("adjustment-return-period-input.month", monthString),
              ("adjustment-return-period-input.year", yearString)
            )

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url

        verify(mockUserAnswersConnector, times(1)).set(any())(any())
        verify(mockAdjustmentNavigator, times(1))
          .nextPage(eqTo(AdjustmentReturnPeriodPage), eqTo(NormalMode), any(), eqTo(Some(true)))
      }
    }

    "must redirect to the next page when valid data is submitted and the user answers are empty" in {

      val mockUserAnswersConnector = mock[UserAnswersConnector]
      val mockAdjustmentNavigator  = mock[AdjustmentNavigator]
      val mockHelper               = mock[AdjustmentReturnPeriodHelper]

      when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])
      when(
        mockAdjustmentNavigator.nextPage(eqTo(AdjustmentReturnPeriodPage), any(), any(), any())
      ) thenReturn onwardRoute
      when(mockHelper.createViewModel(any()))
        .thenReturn(AdjustmentReturnPeriodViewModel(adjustmentType, appConfig.exciseEnquiriesUrl))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[AdjustmentNavigator].toInstance(mockAdjustmentNavigator),
            bind[UserAnswersConnector].toInstance(mockUserAnswersConnector),
            bind[AdjustmentReturnPeriodHelper].toInstance(mockHelper)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, adjustmentReturnPeriodRoute)
            .withFormUrlEncodedBody(
              ("adjustment-return-period-input.month", monthString),
              ("adjustment-return-period-input.year", yearString)
            )

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url

        verify(mockUserAnswersConnector, times(1)).set(any())(any())
        verify(mockAdjustmentNavigator, times(1))
          .nextPage(eqTo(AdjustmentReturnPeriodPage), eqTo(NormalMode), any(), eqTo(Some(true)))
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
      val mockAdjustmentNavigator  = mock[AdjustmentNavigator]
      val mockHelper               = mock[AdjustmentReturnPeriodHelper]

      when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])
      when(
        mockAdjustmentNavigator.nextPage(eqTo(AdjustmentReturnPeriodPage), any(), any(), any())
      ) thenReturn onwardRoute
      when(mockHelper.createViewModel(any()))
        .thenReturn(AdjustmentReturnPeriodViewModel(adjustmentType, appConfig.exciseEnquiriesUrl))

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[AdjustmentNavigator].toInstance(mockAdjustmentNavigator),
            bind[UserAnswersConnector].toInstance(mockUserAnswersConnector),
            bind[AdjustmentReturnPeriodHelper].toInstance(mockHelper)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, adjustmentReturnPeriodRoute)
            .withFormUrlEncodedBody(
              ("adjustment-return-period-input.month", monthString),
              ("adjustment-return-period-input.year", yearString)
            )

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url

        verify(mockUserAnswersConnector, times(1)).set(any())(any())
        verify(mockAdjustmentNavigator, times(1))
          .nextPage(eqTo(AdjustmentReturnPeriodPage), eqTo(NormalMode), any(), eqTo(Some(false)))
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      val mockHelper = mock[AdjustmentReturnPeriodHelper]
      when(mockHelper.createViewModel(any()))
        .thenReturn(AdjustmentReturnPeriodViewModel(adjustmentType, appConfig.exciseEnquiriesUrl))

      val application = applicationBuilder(userAnswers = Some(validEmptyUserAnswers))
        .overrides(bind[AdjustmentReturnPeriodHelper].toInstance(mockHelper))
        .build()

      running(application) {
        val appConfig = application.injector.instanceOf[FrontendAppConfig]

        val request =
          FakeRequest(POST, adjustmentReturnPeriodRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[AdjustmentReturnPeriodView]

        val result = route(application, request).value

        status(result)          mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(
          boundForm,
          NormalMode,
          AdjustmentReturnPeriodViewModel(adjustmentType, appConfig.exciseEnquiriesUrl)
        )(
          request,
          getMessages(application)
        ).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {
      val mockHelper = mock[AdjustmentReturnPeriodHelper]

      val application = applicationBuilder(userAnswers = None)
        .overrides(bind[AdjustmentReturnPeriodHelper].toInstance(mockHelper))
        .build()

      running(application) {
        val request = FakeRequest(GET, adjustmentReturnPeriodRoute)

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a GET if userAnswers are empty" in {
      val mockHelper = mock[AdjustmentReturnPeriodHelper]

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[AdjustmentReturnPeriodHelper].toInstance(mockHelper))
        .build()

      running(application) {
        val request = FakeRequest(GET, adjustmentReturnPeriodRoute)

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {
      val mockHelper = mock[AdjustmentReturnPeriodHelper]

      val application = applicationBuilder(userAnswers = None)
        .overrides(bind[AdjustmentReturnPeriodHelper].toInstance(mockHelper))
        .build()

      running(application) {
        val request =
          FakeRequest(POST, adjustmentReturnPeriodRoute)
            .withFormUrlEncodedBody(("month", "value 1"), ("year", "value 2"))

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }

  "must redirect to Journey Recovery for a POST with invalid data and empty user answers" in {
    val mockHelper = mock[AdjustmentReturnPeriodHelper]

    val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
      .overrides(bind[AdjustmentReturnPeriodHelper].toInstance(mockHelper))
      .build()

    running(application) {
      val request =
        FakeRequest(POST, adjustmentReturnPeriodRoute)
          .withFormUrlEncodedBody(("month", "value 1"), ("year", "value 2"))

      val result = route(application, request).value

      status(result)                 mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
    }
  }
}
