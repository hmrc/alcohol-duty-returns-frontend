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
import forms.adjustment.AdjustmentTaxTypeFormProvider
import models.{ABVRange, AlcoholByVolume, AlcoholRegime, AlcoholType, NormalMode, RangeDetailsByRegime, RateBand, RateType}
import navigation.{AdjustmentNavigator, FakeAdjustmentNavigator}
import org.mockito.ArgumentMatchers.any
import pages.adjustment.CurrentAdjustmentEntryPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.Helpers._
import connectors.{AlcoholDutyCalculatorConnector, CacheConnector}
import models.RateType.Core
import models.adjustment.AdjustmentType.{RepackagedDraughtProducts, Spoilt}
import models.adjustment.AdjustmentEntry
import uk.gov.hmrc.http.HttpResponse
import views.html.adjustment.AdjustmentTaxTypeView

import java.time.YearMonth
import scala.concurrent.Future

class AdjustmentTaxTypeControllerSpec extends SpecBase {
  val formProvider = new AdjustmentTaxTypeFormProvider()
  val form         = formProvider()
  val period       = YearMonth.of(2024, 1)

  def onwardRoute = Call("GET", "/foo")

  val validAnswer = 310

  lazy val adjustmentTaxTypeRoute             =
    controllers.adjustment.routes.AdjustmentTaxTypeController.onPageLoad(NormalMode).url
  lazy val mockAlcoholDutyCalculatorConnector = mock[AlcoholDutyCalculatorConnector]
  val spoilt                                  = Spoilt.toString
  val adjustmentEntry                         = AdjustmentEntry(adjustmentType = Some(Spoilt), period = Some(period))
  val userAnswers                             = emptyUserAnswers.set(CurrentAdjustmentEntryPage, adjustmentEntry).success.value
  val taxCode                                 = "310"
  val alcoholRegime                           = AlcoholRegime.Beer
  val rate                                    = Some(BigDecimal(10.99))

  val rateBand           = RateBand(
    taxCode,
    "some band",
    RateType.DraughtRelief,
    rate,
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
  val application        = applicationBuilder(userAnswers = Some(userAnswers)).build()
  val mockCacheConnector = mock[CacheConnector]

  "AdjustmentTaxType Controller" - {

    "must return OK and the correct view for a GET" in {
      running(application) {
        val request = FakeRequest(GET, adjustmentTaxTypeRoute)
        val result  = route(application, request).value
        status(result) mustEqual OK
        val view    = application.injector.instanceOf[AdjustmentTaxTypeView]
        contentAsString(result) mustEqual view(form, NormalMode, Spoilt)(request, getMessages(app)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val previousUserAnswers =
        userAnswers.set(CurrentAdjustmentEntryPage, adjustmentEntry.copy(rateBand = Some(rateBand))).success.value

      val application = applicationBuilder(userAnswers = Some(previousUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, adjustmentTaxTypeRoute)
        val result  = route(application, request).value

        status(result) mustEqual OK
        val view = application.injector.instanceOf[AdjustmentTaxTypeView]
        contentAsString(result) mustEqual view(form.fill(validAnswer), NormalMode, Spoilt)(
          request,
          getMessages(app)
        ).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {
      when(mockAlcoholDutyCalculatorConnector.rateBand(any(), any())(any())) thenReturn Future.successful(
        Some(rateBand)
      )

      when(mockCacheConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[AdjustmentNavigator].toInstance(new FakeAdjustmentNavigator(onwardRoute, hasValueChanged = true)),
            bind[AlcoholDutyCalculatorConnector].toInstance(mockAlcoholDutyCalculatorConnector),
            bind[CacheConnector].toInstance(mockCacheConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, adjustmentTaxTypeRoute)
            .withFormUrlEncodedBody(("adjustment-tax-type-input", validAnswer.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must redirect to the next page when the same data is submitted" in {
      when(mockAlcoholDutyCalculatorConnector.rateBand(any(), any())(any())) thenReturn Future.successful(
        Some(rateBand)
      )
      val newUserAnswers =
        emptyUserAnswers
          .set(
            CurrentAdjustmentEntryPage,
            adjustmentEntry.copy(rateBand = Some(rateBand))
          )
          .success
          .value

      when(mockCacheConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])

      val application =
        applicationBuilder(userAnswers = Some(newUserAnswers))
          .overrides(
            bind[AdjustmentNavigator].toInstance(new FakeAdjustmentNavigator(onwardRoute, hasValueChanged = false)),
            bind[AlcoholDutyCalculatorConnector].toInstance(mockAlcoholDutyCalculatorConnector),
            bind[CacheConnector].toInstance(mockCacheConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, adjustmentTaxTypeRoute)
            .withFormUrlEncodedBody(("adjustment-tax-type-input", validAnswer.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      when(mockAlcoholDutyCalculatorConnector.rateBand(any(), any())(any())) thenReturn Future.successful(
        Some(rateBand)
      )

      when(mockCacheConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[AdjustmentNavigator].toInstance(new FakeAdjustmentNavigator(onwardRoute, hasValueChanged = true)),
            bind[AlcoholDutyCalculatorConnector].toInstance(mockAlcoholDutyCalculatorConnector),
            bind[CacheConnector].toInstance(mockCacheConnector)
          )
          .build()
      running(application) {
        val request =
          FakeRequest(POST, adjustmentTaxTypeRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val result = route(application, request).value
        val view   = application.injector.instanceOf[AdjustmentTaxTypeView]
        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, Spoilt)(request, getMessages(app)).toString
      }
    }

    "must return a Bad Request and errors when invalid tax type is submitted" in {
      when(mockAlcoholDutyCalculatorConnector.rateBand(any(), any())(any())) thenReturn Future.successful(
        None
      )

      when(mockCacheConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[AdjustmentNavigator].toInstance(new FakeAdjustmentNavigator(onwardRoute, hasValueChanged = false)),
            bind[AlcoholDutyCalculatorConnector].toInstance(mockAlcoholDutyCalculatorConnector),
            bind[CacheConnector].toInstance(mockCacheConnector)
          )
          .build()
      running(application) {
        val request =
          FakeRequest(POST, adjustmentTaxTypeRoute)
            .withFormUrlEncodedBody(("adjustment-tax-type-input", validAnswer.toString))

        val result = route(application, request).value
        val view   = application.injector.instanceOf[AdjustmentTaxTypeView]
        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(
          formProvider()
            .withError("adjustment-tax-type-input", "adjustmentTaxType.error.invalid")
            .fill(validAnswer),
          NormalMode,
          Spoilt
        )(request, getMessages(app)).toString
      }
    }

    "must return a Bad Request and errors when it's a repackaged adjustment and Draught Relief tax type code is submitted" in {
      val coreRateBand = rateBand.copy(rateType = Core)
      when(mockAlcoholDutyCalculatorConnector.rateBand(any(), any())(any())) thenReturn Future.successful(
        Some(coreRateBand)
      )

      when(mockCacheConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])
      val adjustmentEntry = AdjustmentEntry(
        adjustmentType = Some(RepackagedDraughtProducts),
        period = Some(period),
        rateBand = Some(coreRateBand)
      )
      val userAnswers     = emptyUserAnswers.set(CurrentAdjustmentEntryPage, adjustmentEntry).success.value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[AdjustmentNavigator].toInstance(new FakeAdjustmentNavigator(onwardRoute, hasValueChanged = false)),
            bind[AlcoholDutyCalculatorConnector].toInstance(mockAlcoholDutyCalculatorConnector),
            bind[CacheConnector].toInstance(mockCacheConnector)
          )
          .build()
      running(application) {
        val request =
          FakeRequest(POST, adjustmentTaxTypeRoute)
            .withFormUrlEncodedBody(("adjustment-tax-type-input", validAnswer.toString))

        val result = route(application, request).value
        val view   = application.injector.instanceOf[AdjustmentTaxTypeView]
        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(
          formProvider()
            .withError("adjustment-tax-type-input", "adjustmentTaxType.error.notDraught")
            .fill(validAnswer),
          NormalMode,
          RepackagedDraughtProducts
        )(request, getMessages(app)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, adjustmentTaxTypeRoute)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, adjustmentTaxTypeRoute)
            .withFormUrlEncodedBody(("value", validAnswer.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }

}
