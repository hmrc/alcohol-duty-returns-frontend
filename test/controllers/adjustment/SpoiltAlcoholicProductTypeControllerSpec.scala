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
import forms.adjustment.AlcoholicProductTypeFormProvider
import models.{NormalMode, ReturnPeriod}
import navigation.AdjustmentNavigator
import org.mockito.ArgumentMatchers.any
import pages.adjustment.CurrentAdjustmentEntryPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.Helpers._
import connectors.UserAnswersConnector
import models.AlcoholRegime.{Beer, Cider}
import models.adjustment.{AdjustmentEntry, AdjustmentType}
import org.mockito.ArgumentMatchersSugar.eqTo
import uk.gov.hmrc.http.HttpResponse
import viewmodels.checkAnswers.adjustment.SpoiltAlcoholicProductTypeHelper
import views.html.adjustment.SpoiltAlcoholicProductTypeView

import scala.concurrent.Future

class SpoiltAlcoholicProductTypeControllerSpec extends SpecBase {
  "SpoiltAlcoholicProductType Controller" - {
    "must return OK and the empty form passed to the view for a GET when the question hasn't been answered yet" in new SetUp {
      val userAnswers = emptyUserAnswers
        .set(CurrentAdjustmentEntryPage, AdjustmentEntry(adjustmentType = Some(AdjustmentType.Spoilt)))
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, spoiltAlcoholicProductTypeRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[SpoiltAlcoholicProductTypeView]

        status(result)          mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, userAnswers.regimes)(
          request,
          getMessages(application)
        ).toString
      }
    }

    "must populate the view with the filled in form for a GET when the question has previously been answered" in new SetUp {
      val userAnswers = emptyUserAnswers
        .set(
          CurrentAdjustmentEntryPage,
          AdjustmentEntry(adjustmentType = Some(AdjustmentType.Spoilt), spoiltRegime = Some(regime))
        )
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, spoiltAlcoholicProductTypeRoute)

        val view = application.injector.instanceOf[SpoiltAlcoholicProductTypeView]

        val result = route(application, request).value

        status(result)          mustEqual OK
        contentAsString(result) mustEqual view(form.fill(alcoholType), NormalMode, userAnswers.regimes)(
          request,
          getMessages(application)
        ).toString
      }
    }

    "must redirect to the next page when valid data is submitted when no adjustmentEntry is found" in new SetUp {
      val adjustmentTypeAfterUpdate = AdjustmentEntry(
        spoiltRegime = Some(regime),
        rateBand = Some(rateBand),
        period = Some(ReturnPeriod.fromPeriodKeyOrThrow(emptyUserAnswers.returnId.periodKey).period.minusMonths(1))
      )

      val userAnswersAfterUpdate = emptyUserAnswers
        .set(CurrentAdjustmentEntryPage, adjustmentTypeAfterUpdate)
        .success
        .value

      when(mockUserAnswersConnector.set(any())(any())).thenReturn(Future.successful(HttpResponse(OK)))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[AdjustmentNavigator].toInstance(mockAdjustmentNavigator),
            bind[UserAnswersConnector].toInstance(mockUserAnswersConnector),
            bind[SpoiltAlcoholicProductTypeHelper].toInstance(mockSpoiltAlcoholicProductTypeHelper)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, spoiltAlcoholicProductTypeRoute)
            .withFormUrlEncodedBody(("alcoholic-product-type-value", regime.entryName))

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url

        verify(mockUserAnswersConnector, times(1)).set(eqTo(userAnswersAfterUpdate))(any())
        verify(mockAdjustmentNavigator, times(1)).nextPage(any(), any(), eqTo(userAnswersAfterUpdate), eqTo(true))
      }
    }

    "must redirect to the next page when valid data is submitted for the first time" in new SetUp {
      val adjustmentTypeWithDataToClear = AdjustmentEntry(
        adjustmentType = Some(AdjustmentType.Spoilt),
        totalLitresVolume = Some(BigDecimal(123.45)),
        pureAlcoholVolume = Some(BigDecimal(45.6789)),
        sprDutyRate = Some(BigDecimal(1.23)),
        duty = Some(BigDecimal(3.45)),
        repackagedRateBand = Some(draughtReliefRateBand),
        repackagedDuty = Some(BigDecimal(1.23)),
        repackagedSprDutyRate = Some(BigDecimal(4.56)),
        newDuty = Some(BigDecimal(6.78))
      )

      val userAnswers = emptyUserAnswers
        .set(CurrentAdjustmentEntryPage, adjustmentTypeWithDataToClear)
        .success
        .value

      val adjustmentTypeAfterUpdate = AdjustmentEntry(
        adjustmentType = Some(AdjustmentType.Spoilt),
        spoiltRegime = Some(regime),
        rateBand = Some(rateBand),
        period = Some(ReturnPeriod.fromPeriodKeyOrThrow(emptyUserAnswers.returnId.periodKey).period.minusMonths(1))
      )

      val userAnswersAfterUpdate = emptyUserAnswers
        .set(CurrentAdjustmentEntryPage, adjustmentTypeAfterUpdate)
        .success
        .value

      when(mockUserAnswersConnector.set(any())(any())).thenReturn(Future.successful(HttpResponse(OK)))

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[AdjustmentNavigator].toInstance(mockAdjustmentNavigator),
            bind[UserAnswersConnector].toInstance(mockUserAnswersConnector),
            bind[SpoiltAlcoholicProductTypeHelper].toInstance(mockSpoiltAlcoholicProductTypeHelper)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, spoiltAlcoholicProductTypeRoute)
            .withFormUrlEncodedBody(("alcoholic-product-type-value", regime.entryName))

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url

        verify(mockUserAnswersConnector, times(1)).set(eqTo(userAnswersAfterUpdate))(any())
        verify(mockAdjustmentNavigator, times(1)).nextPage(any(), any(), eqTo(userAnswersAfterUpdate), eqTo(true))
      }
    }

    "must redirect to the next page when valid data is submitted again but the regime hasn't changed" in new SetUp {
      val adjustmentTypeWithData = AdjustmentEntry(
        adjustmentType = Some(AdjustmentType.Spoilt),
        period = Some(ReturnPeriod.fromPeriodKeyOrThrow(emptyUserAnswers.returnId.periodKey).period.minusMonths(1)),
        spoiltRegime = Some(regime),
        rateBand = Some(rateBand),
        totalLitresVolume = Some(BigDecimal(123.45)),
        pureAlcoholVolume = Some(BigDecimal(45.6789)),
        sprDutyRate = Some(BigDecimal(1.23)),
        duty = Some(BigDecimal(3.45)),
        repackagedRateBand = Some(smallProducerReliefRateBand),
        repackagedDuty = Some(BigDecimal(1.23)),
        repackagedSprDutyRate = Some(BigDecimal(4.56)),
        newDuty = Some(BigDecimal(6.78))
      )

      val userAnswers = emptyUserAnswers
        .set(CurrentAdjustmentEntryPage, adjustmentTypeWithData)
        .success
        .value

      when(mockUserAnswersConnector.set(any())(any())).thenReturn(Future.successful(HttpResponse(OK)))

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[AdjustmentNavigator].toInstance(mockAdjustmentNavigator),
            bind[UserAnswersConnector].toInstance(mockUserAnswersConnector),
            bind[SpoiltAlcoholicProductTypeHelper].toInstance(mockSpoiltAlcoholicProductTypeHelper)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, spoiltAlcoholicProductTypeRoute)
            .withFormUrlEncodedBody(("alcoholic-product-type-value", regime.entryName))

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url

        verify(mockUserAnswersConnector, times(1)).set(eqTo(userAnswers))(any())
        verify(mockAdjustmentNavigator, times(1)).nextPage(any(), any(), eqTo(userAnswers), eqTo(false))
      }
    }

    "must redirect to the next page when valid data is submitted again and the regime has changed" in new SetUp {
      val adjustmentTypeWithData = AdjustmentEntry(
        adjustmentType = Some(AdjustmentType.Spoilt),
        period = Some(ReturnPeriod.fromPeriodKeyOrThrow(emptyUserAnswers.returnId.periodKey).period.minusMonths(1)),
        spoiltRegime = Some(oldRegime),
        rateBand = Some(rateBand),
        totalLitresVolume = Some(BigDecimal(123.45)),
        pureAlcoholVolume = Some(BigDecimal(45.6789)),
        sprDutyRate = Some(BigDecimal(1.23)),
        duty = Some(BigDecimal(3.45)),
        repackagedRateBand = Some(smallProducerReliefRateBand),
        repackagedDuty = Some(BigDecimal(1.23)),
        repackagedSprDutyRate = Some(BigDecimal(4.56)),
        newDuty = Some(BigDecimal(6.78))
      )

      val userAnswers = emptyUserAnswers
        .set(CurrentAdjustmentEntryPage, adjustmentTypeWithData)
        .success
        .value

      val adjustmentTypeWithUpdatedRegime = AdjustmentEntry(
        adjustmentType = Some(AdjustmentType.Spoilt),
        spoiltRegime = Some(regime),
        rateBand = Some(rateBand),
        period = Some(ReturnPeriod.fromPeriodKeyOrThrow(emptyUserAnswers.returnId.periodKey).period.minusMonths(1))
      )

      val userAnswersAfterUpdate = emptyUserAnswers
        .set(CurrentAdjustmentEntryPage, adjustmentTypeWithUpdatedRegime)
        .success
        .value

      when(mockUserAnswersConnector.set(any())(any())).thenReturn(Future.successful(HttpResponse(OK)))

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[AdjustmentNavigator].toInstance(mockAdjustmentNavigator),
            bind[UserAnswersConnector].toInstance(mockUserAnswersConnector),
            bind[SpoiltAlcoholicProductTypeHelper].toInstance(mockSpoiltAlcoholicProductTypeHelper)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, spoiltAlcoholicProductTypeRoute)
            .withFormUrlEncodedBody(("alcoholic-product-type-value", regime.entryName))

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url

        verify(mockUserAnswersConnector, times(1)).set(eqTo(userAnswersAfterUpdate))(any())
        verify(mockAdjustmentNavigator, times(1)).nextPage(any(), any(), eqTo(userAnswersAfterUpdate), eqTo(true))
      }
    }

    "must return a BAD_REQUEST and errors when invalid data is submitted" in new SetUp {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, spoiltAlcoholicProductTypeRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[SpoiltAlcoholicProductTypeView]

        val result = route(application, request).value

        status(result)          mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, emptyUserAnswers.regimes)(
          request,
          getMessages(application)
        ).toString
      }
    }

    "must redirect to Journey Recovery for a POST if an invalid regime is submitted" in new SetUp {
      val userAnswers =
        emptyUserAnswers.set(CurrentAdjustmentEntryPage, AdjustmentEntry(spoiltRegime = Some(Beer))).success.value

      when(mockUserAnswersConnector.set(any())(any())).thenReturn(Future.successful(HttpResponse(OK)))

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[AdjustmentNavigator].toInstance(mockAdjustmentNavigator),
          bind[UserAnswersConnector].toInstance(mockUserAnswersConnector)
        )
        .build()

      running(application) {
        val request =
          FakeRequest(POST, spoiltAlcoholicProductTypeRoute)
            .withFormUrlEncodedBody(("alcoholic-product-type-value", "invalidRegime"))

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in new SetUp {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, spoiltAlcoholicProductTypeRoute)

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "redirect to Journey Recovery for a POST if no existing data is found" in new SetUp {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, spoiltAlcoholicProductTypeRoute)
            .withFormUrlEncodedBody(("alcoholic-product-type-value", alcoholType))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }

  class SetUp {
    val onwardRoute = Call("GET", "/foo")

    val spoiltAlcoholicProductTypeRoute = routes.SpoiltAlcoholicProductTypeController.onPageLoad(NormalMode).url

    val formProvider = new AlcoholicProductTypeFormProvider()
    val form         = formProvider()
    val alcoholType  = "Beer"

    val mockSpoiltAlcoholicProductTypeHelper = mock[SpoiltAlcoholicProductTypeHelper]
    val mockAdjustmentNavigator              = mock[AdjustmentNavigator]
    val mockUserAnswersConnector             = mock[UserAnswersConnector]

    val regime    = Beer
    val oldRegime = Cider
    val rateBand  = smallProducerReliefRateBand

    when(mockSpoiltAlcoholicProductTypeHelper.createRateBandFromRegime(eqTo(regime))(any())).thenReturn(rateBand)
    when(mockAdjustmentNavigator.nextPage(any(), any(), any(), any())).thenReturn(onwardRoute)
  }
}
