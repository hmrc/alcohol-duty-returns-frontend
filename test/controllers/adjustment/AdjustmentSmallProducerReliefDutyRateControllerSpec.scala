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
import cats.data.NonEmptySeq
import connectors.UserAnswersConnector
import forms.adjustment.AdjustmentSmallProducerReliefDutyRateFormProvider
import models.AlcoholRegime.Beer
import models.adjustment.AdjustmentEntry
import models.adjustment.AdjustmentType.Spoilt
import models.{ABVRange, AlcoholByVolume, AlcoholRegime, AlcoholType, NormalMode, RangeDetailsByRegime, RateBand, RateType}
import navigation.AdjustmentNavigator
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import pages.adjustment.{AdjustmentSmallProducerReliefDutyRatePage, CurrentAdjustmentEntryPage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.Helpers._
import uk.gov.hmrc.http.HttpResponse
import views.html.adjustment.AdjustmentSmallProducerReliefDutyRateView

import scala.concurrent.Future

class AdjustmentSmallProducerReliefDutyRateControllerSpec extends SpecBase {

  val formProvider = new AdjustmentSmallProducerReliefDutyRateFormProvider()
  val form         = formProvider()

  def onwardRoute = Call("GET", "/foo")

  val validAnswer                                     = BigDecimal(2.00)
  val adjustmentEntry                                 = AdjustmentEntry(adjustmentType = Some(Spoilt))
  val userAnswers                                     = emptyUserAnswers.set(CurrentAdjustmentEntryPage, adjustmentEntry).success.value
  lazy val adjustmentSmallProducerReliefDutyRateRoute =
    controllers.adjustment.routes.AdjustmentSmallProducerReliefDutyRateController.onPageLoad(NormalMode).url
  val regime                                          = Beer
  val rateBand                                        = RateBand(
    "310",
    "some band",
    RateType.DraughtRelief,
    Some(BigDecimal(10.99)),
    Set(
      RangeDetailsByRegime(
        AlcoholRegime.Beer,
        NonEmptySeq.one(
          ABVRange(
            AlcoholType.Beer,
            AlcoholByVolume(0.1),
            AlcoholByVolume(5.8)
          )
        )
      )
    )
  )

  "AdjustmentSmallProducerReliefDutyRate Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, adjustmentSmallProducerReliefDutyRateRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AdjustmentSmallProducerReliefDutyRateView]

        status(result)          mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, Spoilt)(request, getMessages(app)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val adjustmentEntry = AdjustmentEntry(adjustmentType = Some(Spoilt), repackagedSprDutyRate = Some(validAnswer))

      val previousUserAnswers = userAnswers.set(CurrentAdjustmentEntryPage, adjustmentEntry).success.value

      val application = applicationBuilder(userAnswers = Some(previousUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, adjustmentSmallProducerReliefDutyRateRoute)

        val view = application.injector.instanceOf[AdjustmentSmallProducerReliefDutyRateView]

        val result = route(application, request).value

        status(result)          mustEqual OK
        contentAsString(result) mustEqual view(form.fill(validAnswer), NormalMode, Spoilt)(
          request,
          getMessages(app)
        ).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockUserAnswersConnector = mock[UserAnswersConnector]
      val mockAdjustmentNavigator  = mock[AdjustmentNavigator]

      when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])
      when(
        mockAdjustmentNavigator.nextPage(eqTo(AdjustmentSmallProducerReliefDutyRatePage), any(), any(), any())
      ) thenReturn onwardRoute

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[AdjustmentNavigator].toInstance(mockAdjustmentNavigator),
            bind[UserAnswersConnector].toInstance(mockUserAnswersConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, adjustmentSmallProducerReliefDutyRateRoute)
            .withFormUrlEncodedBody(("adjustment-small-producer-relief-duty-rate-input", validAnswer.toString))

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url

        verify(mockUserAnswersConnector, times(1)).set(any())(any())
        verify(mockAdjustmentNavigator, times(1))
          .nextPage(eqTo(AdjustmentSmallProducerReliefDutyRatePage), eqTo(NormalMode), any(), eqTo(Some(true)))
      }
    }

    "must redirect to the next page when the same data is submitted" in {
      val userAnswers =
        emptyUserAnswers
          .set(
            CurrentAdjustmentEntryPage,
            AdjustmentEntry(
              adjustmentType = Some(Spoilt),
              rateBand = Some(rateBand),
              repackagedSprDutyRate = Some(validAnswer)
            )
          )
          .success
          .value

      val mockUserAnswersConnector = mock[UserAnswersConnector]
      val mockAdjustmentNavigator  = mock[AdjustmentNavigator]

      when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])
      when(
        mockAdjustmentNavigator.nextPage(eqTo(AdjustmentSmallProducerReliefDutyRatePage), any(), any(), any())
      ) thenReturn onwardRoute

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[AdjustmentNavigator].toInstance(mockAdjustmentNavigator),
            bind[UserAnswersConnector].toInstance(mockUserAnswersConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, adjustmentSmallProducerReliefDutyRateRoute)
            .withFormUrlEncodedBody(("adjustment-small-producer-relief-duty-rate-input", validAnswer.toString))

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url

        verify(mockUserAnswersConnector, times(1)).set(any())(any())
        verify(mockAdjustmentNavigator, times(1))
          .nextPage(eqTo(AdjustmentSmallProducerReliefDutyRatePage), eqTo(NormalMode), any(), eqTo(Some(false)))
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, adjustmentSmallProducerReliefDutyRateRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[AdjustmentSmallProducerReliefDutyRateView]

        val result = route(application, request).value

        status(result)          mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, Spoilt)(request, getMessages(app)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, adjustmentSmallProducerReliefDutyRateRoute)

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, adjustmentSmallProducerReliefDutyRateRoute)
            .withFormUrlEncodedBody(("value", validAnswer.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
