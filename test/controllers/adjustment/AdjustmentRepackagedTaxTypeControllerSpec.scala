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
import connectors.{AlcoholDutyCalculatorConnector, UserAnswersConnector}
import forms.adjustment.AdjustmentRepackagedTaxTypeFormProvider
import models.adjustment.AdjustmentEntry
import models.adjustment.AdjustmentType.Spoilt
import models.{ABVRange, AlcoholByVolume, AlcoholRegime, AlcoholType, NormalMode, RangeDetailsByRegime, RateBand, RateType}
import navigation.AdjustmentNavigator
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import pages.adjustment.{AdjustmentRepackagedTaxTypePage, CurrentAdjustmentEntryPage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.Helpers._
import uk.gov.hmrc.http.HttpResponse
import views.html.adjustment.AdjustmentRepackagedTaxTypeView

import java.time.YearMonth
import scala.concurrent.Future

class AdjustmentRepackagedTaxTypeControllerSpec extends SpecBase {

  val formProvider    = new AdjustmentRepackagedTaxTypeFormProvider()
  val form            = formProvider()
  val period          = YearMonth.of(2024, 1)
  val adjustmentEntry = AdjustmentEntry(adjustmentType = Some(Spoilt), period = Some(period))
  val userAnswers     = emptyUserAnswers.set(CurrentAdjustmentEntryPage, adjustmentEntry).success.value
  val taxCode         = "361"
  val rate            = Some(BigDecimal(10.99))
  val rateBand        = RateBand(
    taxCode,
    "some band",
    RateType.Core,
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
  def onwardRoute     = Call("GET", "/foo")

  val validAnswer = 361

  lazy val adjustmentRepackagedTaxTypeRoute   =
    controllers.adjustment.routes.AdjustmentRepackagedTaxTypeController.onPageLoad(NormalMode).url
  lazy val mockAlcoholDutyCalculatorConnector = mock[AlcoholDutyCalculatorConnector]
  lazy val mockUserAnswersConnector           = mock[UserAnswersConnector]
  lazy val mockAdjustmentNavigator            = mock[AdjustmentNavigator]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockAlcoholDutyCalculatorConnector)
    reset(mockUserAnswersConnector)
    reset(mockAdjustmentNavigator)
  }

  "AdjustmentRepackagedTaxType Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, adjustmentRepackagedTaxTypeRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AdjustmentRepackagedTaxTypeView]

        status(result)          mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, Spoilt)(request, getMessages(app)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val adjustmentEntry = AdjustmentEntry(adjustmentType = Some(Spoilt), repackagedRateBand = Some(rateBand))

      val previousUserAnswers = userAnswers.set(CurrentAdjustmentEntryPage, adjustmentEntry).success.value

      val application = applicationBuilder(userAnswers = Some(previousUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, adjustmentRepackagedTaxTypeRoute)

        val view = application.injector.instanceOf[AdjustmentRepackagedTaxTypeView]

        val result = route(application, request).value

        status(result)          mustEqual OK
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
      when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])
      when(
        mockAdjustmentNavigator.nextPage(eqTo(AdjustmentRepackagedTaxTypePage), any(), any(), any())
      ) thenReturn onwardRoute

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[AdjustmentNavigator].toInstance(mockAdjustmentNavigator),
            bind[AlcoholDutyCalculatorConnector].toInstance(mockAlcoholDutyCalculatorConnector),
            bind[UserAnswersConnector].toInstance(mockUserAnswersConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, adjustmentRepackagedTaxTypeRoute)
            .withFormUrlEncodedBody(("new-tax-type-code", validAnswer.toString))

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url

        verify(mockAlcoholDutyCalculatorConnector, times(1)).rateBand(any(), any())(any())
        verify(mockUserAnswersConnector, times(1)).set(any())(any())
        verify(mockAdjustmentNavigator, times(1))
          .nextPage(eqTo(AdjustmentRepackagedTaxTypePage), eqTo(NormalMode), any(), eqTo(Some(true)))
      }
    }

    "must redirect to the next page when the same data is submitted" in {
      when(mockAlcoholDutyCalculatorConnector.rateBand(any(), any())(any())) thenReturn Future.successful(
        Some(rateBand)
      )
      when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])
      when(
        mockAdjustmentNavigator.nextPage(eqTo(AdjustmentRepackagedTaxTypePage), any(), any(), any())
      ) thenReturn onwardRoute

      val userAnswers =
        emptyUserAnswers
          .set(
            CurrentAdjustmentEntryPage,
            AdjustmentEntry(
              adjustmentType = Some(Spoilt),
              rateBand = Some(rateBand),
              repackagedRateBand = Some(rateBand),
              period = Some(period)
            )
          )
          .success
          .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[AdjustmentNavigator].toInstance(mockAdjustmentNavigator),
            bind[AlcoholDutyCalculatorConnector].toInstance(mockAlcoholDutyCalculatorConnector),
            bind[UserAnswersConnector].toInstance(mockUserAnswersConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, adjustmentRepackagedTaxTypeRoute)
            .withFormUrlEncodedBody(("new-tax-type-code", validAnswer.toString))

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url

        verify(mockAlcoholDutyCalculatorConnector, times(1)).rateBand(any(), any())(any())
        verify(mockUserAnswersConnector, times(1)).set(any())(any())
        verify(mockAdjustmentNavigator, times(1))
          .nextPage(eqTo(AdjustmentRepackagedTaxTypePage), eqTo(NormalMode), any(), eqTo(Some(false)))
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, adjustmentRepackagedTaxTypeRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[AdjustmentRepackagedTaxTypeView]

        val result = route(application, request).value

        status(result)          mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, Spoilt)(request, getMessages(app)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, adjustmentRepackagedTaxTypeRoute)

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, adjustmentRepackagedTaxTypeRoute)
            .withFormUrlEncodedBody(("value", validAnswer.toString))

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must handle non-draught rate band response error" in {
      when(mockAlcoholDutyCalculatorConnector.rateBand(any(), any())(any())) thenReturn Future.successful(
        Some(rateBand.copy(rateType = RateType.DraughtRelief))
      )

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[AlcoholDutyCalculatorConnector].toInstance(mockAlcoholDutyCalculatorConnector)
        )
        .build()

      running(application) {
        val request = FakeRequest(POST, adjustmentRepackagedTaxTypeRoute)
          .withFormUrlEncodedBody(("new-tax-type-code", validAnswer.toString))
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[AdjustmentRepackagedTaxTypeView]

        status(result)          mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(
          formProvider()
            .withError("new-tax-type-code", "adjustmentRepackagedTaxType.error.nonDraught")
            .fill(validAnswer),
          NormalMode,
          Spoilt
        )(request, getMessages(app)).toString

        verify(mockAlcoholDutyCalculatorConnector, times(1)).rateBand(any(), any())(any())
      }
    }

    "must handle invalid rate band response error" in {
      when(mockAlcoholDutyCalculatorConnector.rateBand(any(), any())(any())) thenReturn Future.successful(None)

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[AlcoholDutyCalculatorConnector].toInstance(mockAlcoholDutyCalculatorConnector)
        )
        .build()

      running(application) {
        val request = FakeRequest(POST, adjustmentRepackagedTaxTypeRoute)
          .withFormUrlEncodedBody(("new-tax-type-code", validAnswer.toString))
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[AdjustmentRepackagedTaxTypeView]

        status(result)          mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(
          formProvider()
            .withError("new-tax-type-code", "adjustmentRepackagedTaxType.error.invalid")
            .fill(validAnswer),
          NormalMode,
          Spoilt
        )(request, getMessages(app)).toString

        verify(mockAlcoholDutyCalculatorConnector, times(1)).rateBand(any(), any())(any())
      }
    }

    "must redirect to Journey Recovery currentAdjustmentEntry returns None" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(POST, adjustmentRepackagedTaxTypeRoute)
          .withFormUrlEncodedBody(("new-tax-type-code", validAnswer.toString))

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery when adjustment period is missing" in {
      val adjustmentEntry = AdjustmentEntry(adjustmentType = Some(Spoilt))
      val userAnswers     = emptyUserAnswers.set(CurrentAdjustmentEntryPage, adjustmentEntry).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(POST, adjustmentRepackagedTaxTypeRoute)
          .withFormUrlEncodedBody(("new-tax-type-code", validAnswer.toString))
        val result  = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
