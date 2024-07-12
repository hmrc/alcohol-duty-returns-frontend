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
import forms.adjustment.AdjustmentSmallProducerReliefDutyRateFormProvider
import models.{ABVRange, AlcoholByVolume, AlcoholRegime, AlcoholType, NormalMode, RangeDetailsByRegime, RateBand, RateType}
import navigation.{AdjustmentNavigator, FakeAdjustmentNavigator}
import org.mockito.ArgumentMatchers.any
import pages.adjustment.CurrentAdjustmentEntryPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.Helpers._
import connectors.CacheConnector
import models.AlcoholRegime.Beer
import models.adjustment.AdjustmentEntry
import models.adjustment.AdjustmentType.Spoilt
import uk.gov.hmrc.http.HttpResponse
import views.html.adjustment.AdjustmentSmallProducerReliefDutyRateView

import java.time.YearMonth
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

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, Spoilt)(request, messages(application)).toString
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

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(validAnswer), NormalMode, Spoilt)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

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
          FakeRequest(POST, adjustmentSmallProducerReliefDutyRateRoute)
            .withFormUrlEncodedBody(("adjustment-small-producer-relief-duty-rate-input", validAnswer.toString))

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
              rateBand = Some(rateBand),
              repackagedSprDutyRate = Some(validAnswer)
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
          FakeRequest(POST, adjustmentSmallProducerReliefDutyRateRoute)
            .withFormUrlEncodedBody(("adjustment-small-producer-relief-duty-rate-input", validAnswer.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
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

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, Spoilt)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, adjustmentSmallProducerReliefDutyRateRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
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

    "throw an exception for a GET if adjustmentType is not defined and repackagedSprDutyRate is defined" in {
      val adjustmentEntry     = AdjustmentEntry(
        repackagedSprDutyRate = Some(validAnswer)
      )
      val previousUserAnswers = emptyUserAnswers.set(CurrentAdjustmentEntryPage, adjustmentEntry).success.value

      val application = applicationBuilder(userAnswers = Some(previousUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, adjustmentSmallProducerReliefDutyRateRoute)

        val result = route(application, request).value

        whenReady(result.failed) { exception =>
          exception mustBe a[RuntimeException]
          exception.getMessage mustEqual "Couldn't fetch adjustment type value from cache"
        }
      }
    }
    "throw an exception for a GET if adjustmentType is not defined" in {
      val adjustmentEntry     = AdjustmentEntry(
        period = Some(YearMonth.of(2024, 1))
      )
      val previousUserAnswers = emptyUserAnswers.set(CurrentAdjustmentEntryPage, adjustmentEntry).success.value

      val application = applicationBuilder(userAnswers = Some(previousUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, adjustmentSmallProducerReliefDutyRateRoute)

        val result = route(application, request).value

        whenReady(result.failed) { exception =>
          exception mustBe a[RuntimeException]
          exception.getMessage mustEqual "Couldn't fetch adjustment type value from cache"
        }
      }
    }
    "must throw an exception for a POST if adjustmentType is not defined" in {
      val adjustmentEntry     = AdjustmentEntry(
        period = Some(YearMonth.of(2024, 1))
      )
      val previousUserAnswers = emptyUserAnswers.set(CurrentAdjustmentEntryPage, adjustmentEntry).success.value
      val application         = applicationBuilder(userAnswers = Some(previousUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, adjustmentSmallProducerReliefDutyRateRoute)
            .withFormUrlEncodedBody(("value", validAnswer.toString))

        val result = route(application, request).value
        whenReady(result.failed) { exception =>
          exception mustBe a[RuntimeException]
          exception.getMessage mustEqual "Couldn't fetch adjustment type value from cache"
        }
      }
    }

  }
}
