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
import forms.adjustment.AdjustmentTaxTypeFormProvider
import models.RateType.Core
import models.adjustment.AdjustmentEntry
import models.adjustment.AdjustmentType.{RepackagedDraughtProducts, Spoilt}
import models.{ABVRange, AlcoholByVolume, AlcoholRegime, AlcoholType, NormalMode, RangeDetailsByRegime, RateBand, RateType}
import navigation.AdjustmentNavigator
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import pages.adjustment.{AdjustmentTaxTypePage, CurrentAdjustmentEntryPage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.Helpers._
import uk.gov.hmrc.http.HttpResponse
import views.html.adjustment.AdjustmentTaxTypeView
import viewmodels.adjustment.AdjustmentTaxTypeViewModel

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
  lazy val mockUserAnswersConnector           = mock[UserAnswersConnector]
  lazy val mockAdjustmentNavigator            = mock[AdjustmentNavigator]

  val adjustmentEntry           = AdjustmentEntry(adjustmentType = Some(Spoilt), period = Some(period))
  val adjustmentEntryRepackaged =
    AdjustmentEntry(adjustmentType = Some(RepackagedDraughtProducts), period = Some(period))
  val userAnswers               = emptyUserAnswers.set(CurrentAdjustmentEntryPage, adjustmentEntry).success.value
  val userAnswersRepackaged     = emptyUserAnswers.set(CurrentAdjustmentEntryPage, adjustmentEntryRepackaged).success.value

  val alcoholRegime = AlcoholRegime.Beer
  val rate          = Some(BigDecimal(10.99))

  val rateBand = RateBand(
    "310",
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

  val rateBandRepackaged = RateBand(
    "351",
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
    ),
    repackagedTaxTypeCode = Some("311")
  )

  val rateBandRepackagedInvalid = RateBand(
    "351",
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
    ),
    repackagedTaxTypeCode = None
  )

  val rateBandRepackagedFull = RateBand(
    "311",
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

  val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockAlcoholDutyCalculatorConnector)
    reset(mockUserAnswersConnector)
    reset(mockAdjustmentNavigator)
  }

  "AdjustmentTaxType Controller" - {

    "must return OK and the correct view for a GET" in {
      running(application) {
        val request = FakeRequest(GET, adjustmentTaxTypeRoute)
        val result  = route(application, request).value
        val view    = application.injector.instanceOf[AdjustmentTaxTypeView]

        status(result)          mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, AdjustmentTaxTypeViewModel(Spoilt))(
          request,
          getMessages(app)
        ).toString
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
        contentAsString(result) mustEqual view(form.fill(validAnswer), NormalMode, AdjustmentTaxTypeViewModel(Spoilt))(
          request,
          getMessages(app)
        ).toString
      }
    }

    "For non repackaged" - {
      "must redirect to the correct page when valid data is submitted" in {
        when(mockAlcoholDutyCalculatorConnector.rateBand(any(), any())(any())) thenReturn Future.successful(
          Some(rateBand)
        )
        when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])
        when(mockAdjustmentNavigator.nextPage(eqTo(AdjustmentTaxTypePage), any(), any(), any())) thenReturn onwardRoute

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
            FakeRequest(POST, adjustmentTaxTypeRoute)
              .withFormUrlEncodedBody(("adjustment-tax-type-input", validAnswer.toString))

          val result = route(application, request).value

          status(result)                 mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url

          verify(mockAlcoholDutyCalculatorConnector, times(1)).rateBand(any(), any())(any())
          verify(mockUserAnswersConnector, times(1)).set(any())(any())
          verify(mockAdjustmentNavigator, times(1))
            .nextPage(eqTo(AdjustmentTaxTypePage), eqTo(NormalMode), any(), eqTo(Some(true)))
        }
      }

      "must redirect to the correct page when the same data is submitted" in {
        when(mockAlcoholDutyCalculatorConnector.rateBand(any(), any())(any())) thenReturn Future.successful(
          Some(rateBand)
        )
        when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])
        when(mockAdjustmentNavigator.nextPage(eqTo(AdjustmentTaxTypePage), any(), any(), any())) thenReturn onwardRoute

        val newUserAnswers =
          emptyUserAnswers
            .set(
              CurrentAdjustmentEntryPage,
              adjustmentEntry.copy(rateBand = Some(rateBand))
            )
            .success
            .value

        val application =
          applicationBuilder(userAnswers = Some(newUserAnswers))
            .overrides(
              bind[AdjustmentNavigator].toInstance(mockAdjustmentNavigator),
              bind[AlcoholDutyCalculatorConnector].toInstance(mockAlcoholDutyCalculatorConnector),
              bind[UserAnswersConnector].toInstance(mockUserAnswersConnector)
            )
            .build()

        running(application) {
          val request =
            FakeRequest(POST, adjustmentTaxTypeRoute)
              .withFormUrlEncodedBody(("adjustment-tax-type-input", validAnswer.toString))

          val result = route(application, request).value

          status(result)                 mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url

          verify(mockAlcoholDutyCalculatorConnector, times(1)).rateBand(any(), any())(any())
          verify(mockUserAnswersConnector, times(1)).set(any())(any())
          verify(mockAdjustmentNavigator, times(1))
            .nextPage(eqTo(AdjustmentTaxTypePage), eqTo(NormalMode), any(), eqTo(Some(false)))
        }
      }
    }

    "For repackaged" - {
      "must redirect to the correct page when valid data is submitted" in {
        when(
          mockAlcoholDutyCalculatorConnector.rateBand(any(), any())(any())
        ) thenReturn (
          Future.successful(Some(rateBandRepackaged)),
          Future.successful(Some(rateBandRepackagedFull))
        )

        when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])
        when(mockAdjustmentNavigator.nextPage(eqTo(AdjustmentTaxTypePage), any(), any(), any())) thenReturn onwardRoute

        val application =
          applicationBuilder(userAnswers = Some(userAnswersRepackaged))
            .overrides(
              bind[AdjustmentNavigator].toInstance(mockAdjustmentNavigator),
              bind[AlcoholDutyCalculatorConnector].toInstance(mockAlcoholDutyCalculatorConnector),
              bind[UserAnswersConnector].toInstance(mockUserAnswersConnector)
            )
            .build()

        running(application) {
          val request =
            FakeRequest(POST, adjustmentTaxTypeRoute)
              .withFormUrlEncodedBody(("adjustment-tax-type-input", validAnswer.toString))

          val result = route(application, request).value

          status(result)                 mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url

          verify(mockAlcoholDutyCalculatorConnector, times(2)).rateBand(any(), any())(any())
          verify(mockUserAnswersConnector, times(1)).set(any())(any())
          verify(mockAdjustmentNavigator, times(1))
            .nextPage(eqTo(AdjustmentTaxTypePage), eqTo(NormalMode), any(), eqTo(Some(true)))
        }
      }

      "must redirect to the correct page when the same data is submitted" in {
        when(
          mockAlcoholDutyCalculatorConnector.rateBand(any(), any())(any())
        ) thenReturn (
          Future.successful(Some(rateBandRepackaged)),
          Future.successful(Some(rateBandRepackagedFull))
        )

        when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])
        when(mockAdjustmentNavigator.nextPage(eqTo(AdjustmentTaxTypePage), any(), any(), any())) thenReturn onwardRoute

        val newUserAnswers =
          userAnswersRepackaged
            .set(
              CurrentAdjustmentEntryPage,
              adjustmentEntryRepackaged.copy(rateBand = Some(rateBand))
            )
            .success
            .value

        val application =
          applicationBuilder(userAnswers = Some(newUserAnswers))
            .overrides(
              bind[AdjustmentNavigator].toInstance(mockAdjustmentNavigator),
              bind[AlcoholDutyCalculatorConnector].toInstance(mockAlcoholDutyCalculatorConnector),
              bind[UserAnswersConnector].toInstance(mockUserAnswersConnector)
            )
            .build()

        running(application) {
          val request =
            FakeRequest(POST, adjustmentTaxTypeRoute)
              .withFormUrlEncodedBody(("adjustment-tax-type-input", validAnswer.toString))

          val result = route(application, request).value

          status(result)                 mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url

          verify(mockAlcoholDutyCalculatorConnector, times(2)).rateBand(any(), any())(any())
          verify(mockUserAnswersConnector, times(1)).set(any())(any())
          verify(mockAdjustmentNavigator, times(1))
            .nextPage(eqTo(AdjustmentTaxTypePage), eqTo(NormalMode), any(), eqTo(Some(false)))
        }
      }

      "must go to journey recovery if no linked repackaged tax code type" in {
        when(
          mockAlcoholDutyCalculatorConnector.rateBand(any(), any())(any())
        ) thenReturn
          Future.successful(Some(rateBandRepackagedInvalid))

        when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])
        when(mockAdjustmentNavigator.nextPage(eqTo(AdjustmentTaxTypePage), any(), any(), any())) thenReturn onwardRoute

        val application =
          applicationBuilder(userAnswers = Some(userAnswersRepackaged))
            .overrides(
              bind[AdjustmentNavigator].toInstance(mockAdjustmentNavigator),
              bind[AlcoholDutyCalculatorConnector].toInstance(mockAlcoholDutyCalculatorConnector),
              bind[UserAnswersConnector].toInstance(mockUserAnswersConnector)
            )
            .build()

        running(application) {
          val request =
            FakeRequest(POST, adjustmentTaxTypeRoute)
              .withFormUrlEncodedBody(("adjustment-tax-type-input", validAnswer.toString))

          val result = route(application, request).value
          status(result)                 mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must go to journey recovery if no linked repackaged rate band" in {
        when(
          mockAlcoholDutyCalculatorConnector.rateBand(any(), any())(any())
        ) thenReturn (
          Future.successful(Some(rateBandRepackaged)),
          Future.successful(None)
        )

        when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])
        when(mockAdjustmentNavigator.nextPage(eqTo(AdjustmentTaxTypePage), any(), any(), any())) thenReturn onwardRoute

        val application =
          applicationBuilder(userAnswers = Some(userAnswersRepackaged))
            .overrides(
              bind[AdjustmentNavigator].toInstance(mockAdjustmentNavigator),
              bind[AlcoholDutyCalculatorConnector].toInstance(mockAlcoholDutyCalculatorConnector),
              bind[UserAnswersConnector].toInstance(mockUserAnswersConnector)
            )
            .build()

        running(application) {
          val request =
            FakeRequest(POST, adjustmentTaxTypeRoute)
              .withFormUrlEncodedBody(("adjustment-tax-type-input", validAnswer.toString))

          val result = route(application, request).value
          status(result)                 mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, adjustmentTaxTypeRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val result = route(application, request).value
        val view   = application.injector.instanceOf[AdjustmentTaxTypeView]

        status(result)          mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, AdjustmentTaxTypeViewModel(Spoilt))(
          request,
          getMessages(app)
        ).toString
      }
    }

    "must return a Bad Request and errors when invalid tax type is submitted" in {
      when(mockAlcoholDutyCalculatorConnector.rateBand(any(), any())(any())) thenReturn Future.successful(
        None
      )

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[AlcoholDutyCalculatorConnector].toInstance(mockAlcoholDutyCalculatorConnector)
        )
        .build()

      running(application) {
        val request =
          FakeRequest(POST, adjustmentTaxTypeRoute)
            .withFormUrlEncodedBody(("adjustment-tax-type-input", validAnswer.toString))

        val result = route(application, request).value
        val view   = application.injector.instanceOf[AdjustmentTaxTypeView]

        status(result)          mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(
          formProvider()
            .withError("adjustment-tax-type-input", "adjustmentTaxType.error.invalid")
            .fill(validAnswer),
          NormalMode,
          AdjustmentTaxTypeViewModel(Spoilt)
        )(request, getMessages(app)).toString

        verify(mockAlcoholDutyCalculatorConnector, times(1)).rateBand(any(), any())(any())
      }
    }

    "must return a Bad Request and errors when it's a repackaged adjustment and Draught Relief tax type code is submitted" in {
      val coreRateBand = rateBand.copy(rateType = Core)
      when(mockAlcoholDutyCalculatorConnector.rateBand(any(), any())(any())) thenReturn Future.successful(
        Some(coreRateBand)
      )

      val adjustmentEntry = AdjustmentEntry(
        adjustmentType = Some(RepackagedDraughtProducts),
        period = Some(period),
        rateBand = Some(coreRateBand)
      )
      val userAnswers     = emptyUserAnswers.set(CurrentAdjustmentEntryPage, adjustmentEntry).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[AlcoholDutyCalculatorConnector].toInstance(mockAlcoholDutyCalculatorConnector)
        )
        .build()

      running(application) {
        val request =
          FakeRequest(POST, adjustmentTaxTypeRoute)
            .withFormUrlEncodedBody(("adjustment-tax-type-input", validAnswer.toString))

        val result = route(application, request).value
        val view   = application.injector.instanceOf[AdjustmentTaxTypeView]

        status(result)          mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(
          formProvider()
            .withError("adjustment-tax-type-input", "adjustmentTaxType.error.notDraught")
            .fill(validAnswer),
          NormalMode,
          AdjustmentTaxTypeViewModel(RepackagedDraughtProducts)
        )(request, getMessages(app)).toString

        verify(mockAlcoholDutyCalculatorConnector, times(1)).rateBand(any(), any())(any())
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, adjustmentTaxTypeRoute)
        val result  = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
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

    "must redirect to Journey Recovery currentAdjustmentEntry returns None" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(POST, adjustmentTaxTypeRoute)
          .withFormUrlEncodedBody(("adjustment-tax-type-input", validAnswer.toString))

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
        val request = FakeRequest(POST, adjustmentTaxTypeRoute)
          .withFormUrlEncodedBody(("adjustment-tax-type-input", validAnswer.toString))
        val result  = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
