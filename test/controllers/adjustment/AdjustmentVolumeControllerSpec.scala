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
import forms.adjustment.AdjustmentVolumeFormProvider
import models.{ABVRange, AlcoholByVolume, AlcoholRegime, AlcoholType, NormalMode, RangeDetailsByRegime, RateBand, RateType}
import navigation.{AdjustmentNavigator, FakeAdjustmentNavigator}
import org.mockito.ArgumentMatchers.any
import pages.adjustment.CurrentAdjustmentEntryPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.Helpers._
import connectors.UserAnswersConnector
import models.AlcoholRegime.Beer
import models.adjustment.{AdjustmentEntry, AdjustmentVolume}
import models.adjustment.AdjustmentType.Underdeclaration
import uk.gov.hmrc.http.HttpResponse
import views.html.adjustment.AdjustmentVolumeView

import java.time.YearMonth
import scala.concurrent.Future

class AdjustmentVolumeControllerSpec extends SpecBase {
  val formProvider = new AdjustmentVolumeFormProvider()
  val regime       = Beer
  val form         = formProvider()
  def onwardRoute  = Call("GET", "/foo")

  val validTotalLitres = BigDecimal(10.23)
  val validPureAlcohol = BigDecimal("9.2300")

  lazy val adjustmentVolumeRoute       = controllers.adjustment.routes.AdjustmentVolumeController.onPageLoad(NormalMode).url
  val rateBand                         = RateBand(
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
  val period                           = YearMonth.of(2024, 1)
  val adjustmentEntry                  = AdjustmentEntry(
    adjustmentType = Some(Underdeclaration),
    rateBand = Some(rateBand),
    period = Some(period)
  )
  val userAnswers                      = emptyUserAnswers.set(CurrentAdjustmentEntryPage, adjustmentEntry).success.value
  val userAnswersWithoutRegimes        =
    emptyUserAnswers.set(CurrentAdjustmentEntryPage, adjustmentEntry.copy(rateBand = None)).success.value
  val userAnswersWithoutAdjustmentType =
    emptyUserAnswers.set(CurrentAdjustmentEntryPage, adjustmentEntry.copy(adjustmentType = None)).success.value

  val rateBandContent = "Draught beer between 0.1% and 5.8% ABV (tax type code 310)"

  "AdjustmentVolume Controller" - {
    "must return OK and the correct view for a GET" in {
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, adjustmentVolumeRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AdjustmentVolumeView]

        status(result)          mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, Underdeclaration, regime, rateBandContent)(
          request,
          getMessages(app)
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val updatedAdjustmentEntry =
        adjustmentEntry.copy(totalLitresVolume = Some(validTotalLitres), pureAlcoholVolume = Some(validPureAlcohol))

      val previousUserAnswers = userAnswers.set(CurrentAdjustmentEntryPage, updatedAdjustmentEntry).success.value

      val application = applicationBuilder(userAnswers = Some(previousUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, adjustmentVolumeRoute)

        val view = application.injector.instanceOf[AdjustmentVolumeView]

        val result = route(application, request).value

        status(result)          mustEqual OK
        contentAsString(result) mustEqual view(
          form.fill(AdjustmentVolume(validTotalLitres, validPureAlcohol)),
          NormalMode,
          Underdeclaration,
          regime,
          rateBandContent
        )(
          request,
          getMessages(app)
        ).toString
      }
    }

    "must redirect to journey recovery page if unable to get regimes for a GET" in {
      val application = applicationBuilder(userAnswers = Some(userAnswersWithoutRegimes)).build()

      running(application) {
        val request = FakeRequest(GET, adjustmentVolumeRoute)

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to journey recovery page if unable to get CurrentAdjustmentEntryPage for a GET" in {
      val application = applicationBuilder(userAnswers = Some(userAnswersWithoutAdjustmentType)).build()

      running(application) {
        val request = FakeRequest(GET, adjustmentVolumeRoute)

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to the next page when valid data is submitted" in {
      val mockUserAnswersConnector = mock[UserAnswersConnector]

      when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[AdjustmentNavigator]
              .toInstance(new FakeAdjustmentNavigator(onwardRoute, hasValueChanged = Some(true))),
            bind[UserAnswersConnector].toInstance(mockUserAnswersConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, adjustmentVolumeRoute)
            .withFormUrlEncodedBody(
              ("volumes.totalLitresVolume", validTotalLitres.toString()),
              ("volumes.pureAlcoholVolume", validPureAlcohol.toString())
            )

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
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
              adjustmentType = Some(Underdeclaration),
              period = Some(period),
              rateBand = Some(rateBand)
            )
          )
          .success
          .value

      val mockUserAnswersConnector = mock[UserAnswersConnector]

      when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[AdjustmentNavigator]
              .toInstance(new FakeAdjustmentNavigator(onwardRoute, hasValueChanged = Some(false))),
            bind[UserAnswersConnector].toInstance(mockUserAnswersConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, adjustmentVolumeRoute)
            .withFormUrlEncodedBody(
              ("volumes.totalLitresVolume", validTotalLitres.toString()),
              ("volumes.pureAlcoholVolume", validPureAlcohol.toString())
            )

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must redirect to journey recovery page if unable to get regimes when data is submitted" in {
      val userAnswers =
        emptyUserAnswers
          .set(
            CurrentAdjustmentEntryPage,
            AdjustmentEntry(
              totalLitresVolume = Some(validTotalLitres),
              pureAlcoholVolume = Some(validPureAlcohol),
              adjustmentType = Some(Underdeclaration),
              period = Some(period),
              rateBand = None
            )
          )
          .success
          .value

      val mockUserAnswersConnector = mock[UserAnswersConnector]

      when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[AdjustmentNavigator]
              .toInstance(new FakeAdjustmentNavigator(onwardRoute, hasValueChanged = Some(false))),
            bind[UserAnswersConnector].toInstance(mockUserAnswersConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, adjustmentVolumeRoute)
            .withFormUrlEncodedBody(
              ("volumes.totalLitresVolume", validTotalLitres.toString()),
              ("volumes.pureAlcoholVolume", validPureAlcohol.toString())
            )

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val form    = formProvider()
        val request =
          FakeRequest(POST, adjustmentVolumeRoute)
            .withFormUrlEncodedBody(
              ("volumes.totalLitresVolume", "invalid value"),
              ("volumes.pureAlcoholVolume", "invalid value")
            )

        val boundForm =
          form.bind(Map("volumes.totalLitresVolume" -> "invalid value", "volumes.pureAlcoholVolume" -> "invalid value"))

        val view      = application.injector.instanceOf[AdjustmentVolumeView]

        val result = route(application, request).value

        status(result)          mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, Underdeclaration, regime, rateBandContent)(
          request,
          getMessages(app)
        ).toString
      }
    }

    "must redirect to journey recovery page when invalid data is submitted and unable to get the adjustment type" in {
      val application = applicationBuilder(userAnswers = Some(userAnswersWithoutAdjustmentType)).build()

      running(application) {
        val request =
          FakeRequest(POST, adjustmentVolumeRoute)
            .withFormUrlEncodedBody(
              ("volumes.totalLitresVolume", "invalid value"),
              ("volumes.pureAlcoholVolume", "invalid value")
            )

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, adjustmentVolumeRoute)

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, adjustmentVolumeRoute)
            .withFormUrlEncodedBody(("field1", "value 1"), ("field2", "value 2"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
