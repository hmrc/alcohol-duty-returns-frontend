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
import play.api.test.Helpers._
import uk.gov.hmrc.http.HttpResponse
import views.html.adjustment.AdjustmentDutyDueView
import models.{ABVRange, AlcoholByVolume, AlcoholRegime, AlcoholType, RangeDetailsByRegime, RateBand, RateType}
import models.adjustment.AdjustmentEntry
import models.adjustment.AdjustmentType.{Overdeclaration, RepackagedDraughtProducts}
import org.mockito.ArgumentMatchers.any
import play.api.inject.bind
import services.adjustment.AdjustmentEntryService
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import viewmodels.checkAnswers.adjustment.{AdjustmentDutyDueViewModel, AdjustmentDutyDueViewModelCreator}

import java.time.YearMonth
import scala.concurrent.Future

class AdjustmentDutyDueControllerSpec extends SpecBase {
  "AdjustmentDutyDue Controller" - {
    "must return OK and the correct view for a GET when not RepackagedDraughtProducts" in new SetUp {
      when(mockAdjustmentEntryService.createAdjustment(any())(any())) thenReturn Future.successful(adjustmentEntry)
      when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[AdjustmentEntryService].toInstance(mockAdjustmentEntryService),
          bind[UserAnswersConnector].toInstance(mockUserAnswersConnector),
          bind[AdjustmentDutyDueViewModelCreator].toInstance(mockAdjustmentDutyDueViewModelCreator)
        )
        .build()

      when(mockAdjustmentDutyDueViewModelCreator(any, any, any, any, any, any, any)(any))
        .thenReturn(adjustmentDutyDueViewModel)

      running(application) {
        val request = FakeRequest(GET, adjustmentDutyDueRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AdjustmentDutyDueView]

        status(result)          mustEqual OK
        contentAsString(result) mustEqual view(
          adjustmentDutyDueViewModel,
          Overdeclaration
        )(
          request,
          getMessages(application)
        ).toString
      }

      verify(mockAdjustmentDutyDueViewModelCreator, times(1))(
        Overdeclaration,
        dutyDue,
        BigDecimal(0),
        pureAlcoholVolume,
        rate,
        BigDecimal(0),
        BigDecimal(0)
      )(getMessages(application))
    }

    "must return OK and the correct view for a GET when RepackagedDraughtProducts" in new SetUp {
      when(mockAdjustmentEntryService.createAdjustment(any())(any())) thenReturn Future.successful(
        repackagedAdjustmentEntry
      )
      when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[AdjustmentEntryService].toInstance(mockAdjustmentEntryService),
          bind[UserAnswersConnector].toInstance(mockUserAnswersConnector),
          bind[AdjustmentDutyDueViewModelCreator].toInstance(mockAdjustmentDutyDueViewModelCreator)
        )
        .build()

      when(mockAdjustmentDutyDueViewModelCreator(any, any, any, any, any, any, any)(any))
        .thenReturn(adjustmentDutyDueViewModel)

      running(application) {
        val request = FakeRequest(GET, adjustmentDutyDueRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AdjustmentDutyDueView]

        status(result)          mustEqual OK
        contentAsString(result) mustEqual view(
          adjustmentDutyDueViewModel,
          RepackagedDraughtProducts
        )(
          request,
          getMessages(application)
        ).toString
      }

      verify(mockAdjustmentDutyDueViewModelCreator, times(1))(
        RepackagedDraughtProducts,
        dutyDue,
        newDuty,
        pureAlcoholVolume,
        rate,
        repackagedRate,
        repackagedDuty
      )(getMessages(application))
    }

    "must redirect to Journey Recovery if no existing data is found" in new SetUp {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(GET, adjustmentDutyDueRoute)

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    Seq(
      ((entry: AdjustmentEntry) => entry.copy(pureAlcoholVolume = None), "pureAlcoholVolume"),
      ((entry: AdjustmentEntry) => entry.copy(duty = None), "duty")
    ).foreach { case (clearAdjustmentEntry, msg) =>
      s"must redirect to Journey Recovery for a GET if adjustment entry does not contain $msg" in new SetUp {
        val adjustmentEntryWithMissingValue = clearAdjustmentEntry(adjustmentEntry)
        when(mockAdjustmentEntryService.createAdjustment(any())(any())) thenReturn Future.successful(
          adjustmentEntryWithMissingValue
        )
        when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .overrides(
              bind[AdjustmentEntryService].toInstance(mockAdjustmentEntryService),
              bind[UserAnswersConnector].toInstance(mockUserAnswersConnector)
            )
            .build()

        running(application) {
          val request = FakeRequest(GET, adjustmentDutyDueRoute)

          val result = route(application, request).value

          status(result)                 mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }
  }

  class SetUp {
    val dutyDue            = BigDecimal(34.2)
    val rate               = BigDecimal(9.27)
    val repackagedRate     = BigDecimal(10.27)
    val pureAlcoholVolume  = BigDecimal(3.69)
    val volume             = BigDecimal(10)
    val repackagedDuty     = BigDecimal(33.2)
    val newDuty            = BigDecimal(1)
    val rateBand           = RateBand(
      "310",
      "some band",
      RateType.DraughtRelief,
      Some(rate),
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
    val repackagedRateBand = RateBand(
      "311",
      "some repackaged band",
      RateType.DraughtRelief,
      Some(repackagedRate),
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

    val adjustmentEntry = AdjustmentEntry(
      pureAlcoholVolume = Some(pureAlcoholVolume),
      totalLitresVolume = Some(volume),
      rateBand = Some(rateBand),
      duty = Some(dutyDue),
      adjustmentType = Some(Overdeclaration),
      period = Some(YearMonth.of(24, 1))
    )

    val repackagedAdjustmentEntry = AdjustmentEntry(
      pureAlcoholVolume = Some(pureAlcoholVolume),
      totalLitresVolume = Some(volume),
      rateBand = Some(rateBand),
      duty = Some(dutyDue),
      newDuty = Some(newDuty),
      adjustmentType = Some(RepackagedDraughtProducts),
      period = Some(YearMonth.of(24, 1)),
      repackagedDuty = Some(repackagedDuty),
      repackagedRateBand = Some(repackagedRateBand)
    )

    val mockAdjustmentEntryService            = mock[AdjustmentEntryService]
    val mockUserAnswersConnector              = mock[UserAnswersConnector]
    val mockAdjustmentDutyDueViewModelCreator = mock[AdjustmentDutyDueViewModelCreator]

    val adjustmentDutyDueRoute = controllers.adjustment.routes.AdjustmentDutyDueController.onPageLoad().url

    val adjustmentDutyDueViewModel =
      AdjustmentDutyDueViewModel(Overdeclaration, BigDecimal(34.2),
        Seq(Text("you declared 3.6900 litres of pure alcohol (LPA)"),
            Text("the duty rate is £9.27 per litre of pure alcohol"),
            Text("the duty is £34.20 (duty rate multiplied by the litres of pure alcohol")
        )
      )

      /*
      val repackagedDutyDueViewModel = {
        AdjustmentDutyDueViewModel(RepackagedDraughtProducts, BigDecimal(34.2),
          Seq(Text("you declared 3.6900 litres of pure alcohol (LPA)"),
            Text("the duty rate is £9.27 per litre of pure alcohol"),
            Text("the duty is £34.20 (duty rate multiplied by the litres of pure alcohol")
          )
        ) */

    val repackagedAdjustmentDutyDueViewModel =
      AdjustmentDutyDueViewModel(RepackagedDraughtProducts, BigDecimal(1),
        Seq(
          Text("the reduced duty rate was £9.27 per litre of pure alcohol"),
          Text("you declared 3.6900 litres of pure alcohol (LPA)"),
          Text("you originally paid £34.20"),
          Text("repackaged draught products are not eligible for reduced rates"),
          Text("the standard duty rate is £10.27 per litre of pure alcohol"),
          Text("the new duty value at the standard duty rate is £33.20"),
          Text("the duty to pay is £1.00: the new duty value minus the old duty value")
        )
      )
  }
}
