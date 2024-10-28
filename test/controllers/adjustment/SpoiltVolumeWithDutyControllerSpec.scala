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
import forms.adjustment.SpoiltVolumeWithDutyFormProvider
import models.AlcoholRegime.Beer
import models.adjustment.AdjustmentType.Spoilt
import models.adjustment.{AdjustmentEntry, SpoiltVolumeWithDuty}
import models.{ABVRange, AlcoholByVolume, AlcoholType, NormalMode, RangeDetailsByRegime, RateBand, RateType}
import navigation.{AdjustmentNavigator, FakeAdjustmentNavigator}
import org.mockito.ArgumentMatchers.any
import pages.adjustment.CurrentAdjustmentEntryPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.Helpers._
import uk.gov.hmrc.http.HttpResponse
import views.html.adjustment.SpoiltVolumeWithDutyView

import java.time.YearMonth
import scala.concurrent.Future

class SpoiltVolumeWithDutyControllerSpec extends SpecBase {
  val formProvider = new SpoiltVolumeWithDutyFormProvider()
  val regime       = Beer
  val form         = formProvider(regime)(getMessages(app))
  def onwardRoute  = Call("GET", "/foo")

  val validTotalLitres = BigDecimal(10.23)
  val validPureAlcohol = BigDecimal(9.23)
  val validDuty        = BigDecimal(2)
  val period           = YearMonth.of(2024, 1)

  lazy val spoiltVolumeWithDutyRoute =
    controllers.adjustment.routes.SpoiltVolumeWithDutyController.onPageLoad(NormalMode).url
  val rateBand                       = RateBand(
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
  val adjustmentEntry                = AdjustmentEntry(
    adjustmentType = Some(Spoilt),
    period = Some(period),
    rateBand = Some(rateBand)
  )
  val userAnswers                    = emptyUserAnswers.set(CurrentAdjustmentEntryPage, adjustmentEntry).success.value
  val userAnswersWithoutRegimes      =
    emptyUserAnswers.set(CurrentAdjustmentEntryPage, adjustmentEntry.copy(rateBand = None)).success.value
  val rateBandContent                = "Beer between 0.1% and 5.8% ABV (tax type code 310)"

  "SpoiltVolumeWithDuty Controller" - {
    "must return OK and the correct view for a GET" in {
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, spoiltVolumeWithDutyRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[SpoiltVolumeWithDutyView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, regime)(
          request,
          getMessages(app)
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val updatedAdjustmentEntry =
        adjustmentEntry.copy(
          totalLitresVolume = Some(validTotalLitres),
          pureAlcoholVolume = Some(validPureAlcohol),
          duty = Some(validDuty)
        )

      val previousUserAnswers = userAnswers.set(CurrentAdjustmentEntryPage, updatedAdjustmentEntry).success.value

      val application = applicationBuilder(userAnswers = Some(previousUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, spoiltVolumeWithDutyRoute)

        val view = application.injector.instanceOf[SpoiltVolumeWithDutyView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form.fill(SpoiltVolumeWithDuty(validTotalLitres, validPureAlcohol, validDuty)),
          NormalMode,
          regime
        )(
          request,
          getMessages(app)
        ).toString
      }
    }

    "must redirect to journey recovery page if unable to get regimes for a GET" in {
      val application = applicationBuilder(userAnswers = Some(userAnswersWithoutRegimes)).build()

      running(application) {
        val request = FakeRequest(GET, spoiltVolumeWithDutyRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
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
          FakeRequest(POST, spoiltVolumeWithDutyRoute)
            .withFormUrlEncodedBody(
              ("volumes.totalLitresVolume", validTotalLitres.toString()),
              ("volumes.pureAlcoholVolume", validPureAlcohol.toString()),
              ("volumes.duty", validDuty.toString())
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
              duty = Some(validDuty),
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
            bind[AdjustmentNavigator].toInstance(new FakeAdjustmentNavigator(onwardRoute, hasValueChanged = true)),
            bind[CacheConnector].toInstance(mockCacheConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, spoiltVolumeWithDutyRoute)
            .withFormUrlEncodedBody(
              ("volumes.totalLitresVolume", validTotalLitres.toString()),
              ("volumes.pureAlcoholVolume", validPureAlcohol.toString()),
              ("volumes.duty", validDuty.toString())
            )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must redirect to the journey recovery page if unable to get regimes when data is submitted" in {
      val userAnswers =
        emptyUserAnswers
          .set(
            CurrentAdjustmentEntryPage,
            AdjustmentEntry(
              totalLitresVolume = Some(validTotalLitres),
              pureAlcoholVolume = Some(validPureAlcohol),
              duty = Some(validDuty),
              adjustmentType = Some(Spoilt),
              period = Some(period),
              rateBand = None
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
          FakeRequest(POST, spoiltVolumeWithDutyRoute)
            .withFormUrlEncodedBody(
              ("volumes.totalLitresVolume", validTotalLitres.toString()),
              ("volumes.pureAlcoholVolume", validPureAlcohol.toString()),
              ("volumes.duty", validDuty.toString())
            )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val form    = formProvider(regime)(getMessages(app))
        val request =
          FakeRequest(POST, spoiltVolumeWithDutyRoute)
            .withFormUrlEncodedBody(
              ("volumes.totalLitresVolume", "invalid value"),
              ("volumes.pureAlcoholVolume", "invalid value"),
              ("volumes.duty", "invalid value")
            )

        val boundForm = form.bind(
          Map(
            "volumes.totalLitresVolume" -> "invalid value",
            "volumes.pureAlcoholVolume" -> "invalid value",
            "volumes.duty"              -> "invalid value"
          )
        )

        val view = application.injector.instanceOf[SpoiltVolumeWithDutyView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, regime)(
          request,
          getMessages(app)
        ).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, spoiltVolumeWithDutyRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to journey recovery page if CurrentAdjustmentEntryPage returns None on a GET" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
      running(application) {
        val request = FakeRequest(GET, spoiltVolumeWithDutyRoute)
        val result  = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, spoiltVolumeWithDutyRoute)
            .withFormUrlEncodedBody(("field1", "value 1"), ("field2", "value 2"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
