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
import connectors.CacheConnector
import forms.adjustment.AdjustmentVolumeWithSPRFormProvider
import models.AlcoholRegime.Beer
import models.adjustment.AdjustmentType.Spoilt
import models.adjustment.{AdjustmentEntry, AdjustmentVolumeWithSPR}
import models.{ABVRange, AlcoholByVolume, AlcoholType, NormalMode, RangeDetailsByRegime, RateBand, RateType}
import navigation.{AdjustmentNavigator, FakeAdjustmentNavigator}
import org.mockito.ArgumentMatchers.any
import pages.adjustment.CurrentAdjustmentEntryPage
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.Helpers._
import uk.gov.hmrc.http.HttpResponse
import views.html.adjustment.AdjustmentVolumeWithSPRView

import java.time.YearMonth
import scala.concurrent.Future

class AdjustmentVolumeWithSPRControllerSpec extends SpecBase {
  val formProvider = new AdjustmentVolumeWithSPRFormProvider()
  val regime       = Beer
  val messages     = mock[Messages]
  val form         = formProvider(regime)(messages)
  def onwardRoute  = Call("GET", "/foo")

  val validTotalLitres = BigDecimal(10.23)
  val validPureAlcohol = BigDecimal(9.23)
  val validSPRDutyRate = BigDecimal(2)
  val period           = YearMonth.of(2024, 1)

  lazy val adjustmentVolumeWithSPRRoute =
    controllers.adjustment.routes.AdjustmentVolumeWithSPRController.onPageLoad(NormalMode).url
  val rateBand                          = RateBand(
    "310",
    "some band",
    RateType.DraughtRelief,
    Some(BigDecimal(10.99)),
    Set(
      RangeDetailsByRegime(
        regime,
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
  val adjustmentEntry                   = AdjustmentEntry(
    adjustmentType = Some(Spoilt),
    period = Some(period),
    rateBand = Some(rateBand)
  )
  val userAnswers                       = emptyUserAnswers.set(CurrentAdjustmentEntryPage, adjustmentEntry).success.value

  val rateBandContent = "Beer between 0.1% and 5.8% ABV (tax type code 310)"

  "AdjustmentVolumeWithSPR Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, adjustmentVolumeWithSPRRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AdjustmentVolumeWithSPRView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, Spoilt, regime, rateBandContent)(
          request,
          messages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val updatedAdjustmentEntry =
        adjustmentEntry.copy(
          totalLitresVolume = Some(validTotalLitres),
          pureAlcoholVolume = Some(validPureAlcohol),
          sprDutyRate = Some(validSPRDutyRate)
        )

      val previousUserAnswers = userAnswers.set(CurrentAdjustmentEntryPage, updatedAdjustmentEntry).success.value

      val application = applicationBuilder(userAnswers = Some(previousUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, adjustmentVolumeWithSPRRoute)

        val view = application.injector.instanceOf[AdjustmentVolumeWithSPRView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form.fill(AdjustmentVolumeWithSPR(validTotalLitres, validPureAlcohol, validSPRDutyRate)),
          NormalMode,
          Spoilt,
          regime,
          rateBandContent
        )(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockCacheConnector = mock[CacheConnector]

      when(mockCacheConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[AdjustmentNavigator].toInstance(new FakeAdjustmentNavigator(onwardRoute, hasValueChanged = true)),
            bind[CacheConnector].toInstance(mockCacheConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, adjustmentVolumeWithSPRRoute)
            .withFormUrlEncodedBody(
              ("volumes.totalLitresVolume", validTotalLitres.toString()),
              ("volumes.pureAlcoholVolume", validPureAlcohol.toString()),
              ("volumes.sprDutyRate", validSPRDutyRate.toString())
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
              totalLitresVolume = Some(validTotalLitres),
              pureAlcoholVolume = Some(validPureAlcohol),
              sprDutyRate = Some(validSPRDutyRate),
              adjustmentType = Some(Spoilt),
              period = Some(period),
              rateBand = Some(rateBand)
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
          FakeRequest(POST, adjustmentVolumeWithSPRRoute)
            .withFormUrlEncodedBody(
              ("volumes.totalLitresVolume", validTotalLitres.toString()),
              ("volumes.pureAlcoholVolume", validPureAlcohol.toString()),
              ("volumes.sprDutyRate", validSPRDutyRate.toString())
            )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val form    = formProvider(regime)(messages(application))
        val request =
          FakeRequest(POST, adjustmentVolumeWithSPRRoute)
            .withFormUrlEncodedBody(
              ("volumes.totalLitresVolume", "invalid value"),
              ("volumes.pureAlcoholVolume", "invalid value"),
              ("volumes.sprDutyRate", "invalid value")
            )

        val boundForm = form.bind(
          Map(
            "volumes.totalLitresVolume" -> "invalid value",
            "volumes.pureAlcoholVolume" -> "invalid value",
            "volumes.sprDutyRate"       -> "invalid value"
          )
        )

        val view = application.injector.instanceOf[AdjustmentVolumeWithSPRView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, Spoilt, regime, rateBandContent)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, adjustmentVolumeWithSPRRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, adjustmentVolumeWithSPRRoute)
            .withFormUrlEncodedBody(("field1", "value 1"), ("field2", "value 2"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
    "throw an exception for a GET if rateBand is not defined " in {
      val adjustmentEntry     = AdjustmentEntry(
        adjustmentType = Some(Spoilt)
      )
      val previousUserAnswers = emptyUserAnswers.set(CurrentAdjustmentEntryPage, adjustmentEntry).success.value

      val application = applicationBuilder(userAnswers = Some(previousUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, adjustmentVolumeWithSPRRoute)

        val result = route(application, request).value

        whenReady(result.failed) { exception =>
          exception mustBe a[RuntimeException]
          exception.getMessage mustEqual "Couldn't fetch regime value from cache"
        }
      }
    }
    "throw an exception for a GET if adjustmentType is not defined" in {
      val adjustmentEntry     = AdjustmentEntry(
        period = Some(YearMonth.of(2024, 1)),
        rateBand = Some(rateBand)
      )
      val previousUserAnswers = emptyUserAnswers.set(CurrentAdjustmentEntryPage, adjustmentEntry).success.value

      val application = applicationBuilder(userAnswers = Some(previousUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, adjustmentVolumeWithSPRRoute)

        val result = route(application, request).value

        whenReady(result.failed) { exception =>
          exception mustBe a[RuntimeException]
          exception.getMessage mustEqual "Couldn't fetch adjustment type value from cache"
        }
      }
    }

    "must throw an exception for a POST if adjustmentType is not defined" in {
      val adjustmentEntry     = AdjustmentEntry(
        rateBand = Some(rateBand)
      )
      val previousUserAnswers = emptyUserAnswers.set(CurrentAdjustmentEntryPage, adjustmentEntry).success.value
      val application         = applicationBuilder(userAnswers = Some(previousUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, adjustmentVolumeWithSPRRoute)
            .withFormUrlEncodedBody(
              ("volumes.totalLitresVolume", validTotalLitres.toString()),
              ("volumes.pureAlcoholVolume", validPureAlcohol.toString()),
              ("volumes.sprDutyRate", validSPRDutyRate.toString())
            )

        val result = route(application, request).value
        whenReady(result.failed) { exception =>
          exception mustBe a[RuntimeException]
          exception.getMessage mustEqual "Couldn't fetch adjustment type value from cache"
        }
      }
    }
  }
}
