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

package services.adjustment

import base.SpecBase
import cats.data.NonEmptySeq
import connectors.AlcoholDutyCalculatorConnector
import pages.adjustment._
import models.{ABVRange, AlcoholByVolume, AlcoholRegime, AlcoholType, RangeDetailsByRegime, RateBand, RateType}
import models.adjustment.{AdjustmentDuty, AdjustmentEntry}
import models.adjustment.AdjustmentType.{Drawback, Overdeclaration, RepackagedDraughtProducts, Spoilt, Underdeclaration}
import org.mockito.ArgumentMatchers.any
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class AdjustmentEntryServiceSpec extends SpecBase {

  "AdjustmentEntryService" - {

    implicit val hc: HeaderCarrier = HeaderCarrier()

    val rateBand = RateBand(
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

    val adjustmentEntry = AdjustmentEntry(
      adjustmentType = Some(Underdeclaration),
      totalLitresVolume = Some(BigDecimal(1)),
      pureAlcoholVolume = Some(BigDecimal(1)),
      rateBand = Some(rateBand),
      repackagedRateBand = Some(rateBand)
    )

    "must create an adjustment entry when TaxType contains rate" in {
      val userAnswerWithRate = emptyUserAnswers
        .set(CurrentAdjustmentEntryPage, adjustmentEntry)
        .success
        .value

      val mockConnector = mock[AlcoholDutyCalculatorConnector]
      when(mockConnector.calculateAdjustmentDuty(any(), any(), any())(any()))
        .thenReturn(Future.successful(AdjustmentDuty(BigDecimal(1))))

      val service = new AdjustmentEntryServiceImpl(mockConnector)

      val result = service.createAdjustment(userAnswerWithRate).futureValue

      result                   mustBe a[AdjustmentEntry]
      result.rate              mustBe Some(BigDecimal(10.99))
      result.totalLitresVolume mustBe Some(BigDecimal(1))
      result.pureAlcoholVolume mustBe Some(BigDecimal(1))
      result.duty              mustBe Some(BigDecimal(1))
    }

    "must create an adjustment entry when TaxType does not contain rate but the Small Producer Relief duty rate is present" in {
      val updatedAdjustmentEntry = adjustmentEntry.copy(
        adjustmentType = Some(Overdeclaration),
        rateBand = Some(rateBand.copy(rate = None)),
        sprDutyRate = Some(BigDecimal(2))
      )

      val userAnswerWithRate = emptyUserAnswers
        .set(CurrentAdjustmentEntryPage, updatedAdjustmentEntry)
        .success
        .value

      val mockConnector = mock[AlcoholDutyCalculatorConnector]
      when(mockConnector.calculateAdjustmentDuty(any(), any(), any())(any()))
        .thenReturn(Future.successful(AdjustmentDuty(BigDecimal(1))))

      val service = new AdjustmentEntryServiceImpl(mockConnector)

      val result = service.createAdjustment(userAnswerWithRate).futureValue

      result                   mustBe a[AdjustmentEntry]
      result.rate              mustBe Some(BigDecimal(2))
      result.totalLitresVolume mustBe Some(BigDecimal(1))
      result.pureAlcoholVolume mustBe Some(BigDecimal(1))
      result.duty              mustBe Some(BigDecimal(1))
    }

    "must create an adjustment entry for RepackagedDraughtProducts" in {
      val userAnswerWithRate = emptyUserAnswers
        .set(
          CurrentAdjustmentEntryPage,
          adjustmentEntry.copy(
            adjustmentType = Some(RepackagedDraughtProducts),
            repackagedDuty = Some(BigDecimal(1)),
            newDuty = Some(BigDecimal(2))
          )
        )
        .success
        .value
      val mockConnector      = mock[AlcoholDutyCalculatorConnector]
      when(mockConnector.calculateAdjustmentDuty(any(), any(), any())(any()))
        .thenReturn(Future.successful(AdjustmentDuty(BigDecimal(1))))
      when(mockConnector.calculateRepackagedDutyChange(any(), any())(any()))
        .thenReturn(Future.successful(AdjustmentDuty(BigDecimal(2))))
      val service            = new AdjustmentEntryServiceImpl(mockConnector)
      val result             = service.createAdjustment(userAnswerWithRate).futureValue
      result                   mustBe a[AdjustmentEntry]
      result.rate              mustBe Some(BigDecimal(10.99))
      result.totalLitresVolume mustBe Some(BigDecimal(1))
      result.pureAlcoholVolume mustBe Some(BigDecimal(1))
      result.duty              mustBe Some(BigDecimal(1))
      result.repackagedDuty    mustBe Some(BigDecimal(1))
      result.newDuty           mustBe Some(BigDecimal(2))
    }

    "must create an adjustment entry for Spoilt" in {
      val userAnswerWithRate = emptyUserAnswers
        .set(
          CurrentAdjustmentEntryPage,
          adjustmentEntry.copy(
            adjustmentType = Some(Spoilt)
          )
        )
        .success
        .value
      val mockConnector      = mock[AlcoholDutyCalculatorConnector]
      when(mockConnector.calculateAdjustmentDuty(any(), any(), any())(any()))
        .thenReturn(Future.successful(AdjustmentDuty(BigDecimal(1))))
      val service            = new AdjustmentEntryServiceImpl(mockConnector)
      val result             = service.createAdjustment(userAnswerWithRate).futureValue
      result                   mustBe a[AdjustmentEntry]
      result.rate              mustBe Some(BigDecimal(10.99))
      result.totalLitresVolume mustBe Some(BigDecimal(1))
      result.pureAlcoholVolume mustBe Some(BigDecimal(1))
      result.duty              mustBe Some(BigDecimal(1))
    }

    "must create an adjustment entry for Drawback" in {
      val userAnswerWithRate = emptyUserAnswers
        .set(
          CurrentAdjustmentEntryPage,
          adjustmentEntry.copy(
            adjustmentType = Some(Drawback)
          )
        )
        .success
        .value
      val mockConnector      = mock[AlcoholDutyCalculatorConnector]
      when(mockConnector.calculateAdjustmentDuty(any(), any(), any())(any()))
        .thenReturn(Future.successful(AdjustmentDuty(BigDecimal(1))))
      val service            = new AdjustmentEntryServiceImpl(mockConnector)
      val result             = service.createAdjustment(userAnswerWithRate).futureValue
      result                   mustBe a[AdjustmentEntry]
      result.rate              mustBe Some(BigDecimal(10.99))
      result.totalLitresVolume mustBe Some(BigDecimal(1))
      result.pureAlcoholVolume mustBe Some(BigDecimal(1))
      result.duty              mustBe Some(BigDecimal(1))
    }

    "must throw an Exception" - {
      "if both, TaxType and SmallProducerReliefDuty contain rate" in {
        val updatedAdjustmentEntry = adjustmentEntry.copy(
          rateBand = Some(rateBand),
          sprDutyRate = Some(BigDecimal(2))
        )

        val userAnswerWithRate = emptyUserAnswers
          .set(CurrentAdjustmentEntryPage, updatedAdjustmentEntry)
          .success
          .value

        val mockConnector = mock[AlcoholDutyCalculatorConnector]
        when(mockConnector.calculateAdjustmentDuty(any(), any(), any())(any()))
          .thenReturn(Future.successful(AdjustmentDuty(BigDecimal(1))))

        val service = new AdjustmentEntryServiceImpl(mockConnector)

        val exception = intercept[RuntimeException] {
          service.createAdjustment(userAnswerWithRate).futureValue
        }

        exception.getLocalizedMessage must include("Failed to get rate, both tax rate and spr duty rate are defined.")
      }

      "if pureAlcoholVolume value is not defined" in {
        val updatedAdjustmentEntry = AdjustmentEntry(
          adjustmentType = Some(Underdeclaration)
        )
        val userAnswerWithRate     = emptyUserAnswers
          .set(CurrentAdjustmentEntryPage, updatedAdjustmentEntry)
          .success
          .value
        val mockConnector          = mock[AlcoholDutyCalculatorConnector]
        when(mockConnector.calculateAdjustmentDuty(any(), any(), any())(any()))
          .thenReturn(Future.successful(AdjustmentDuty(BigDecimal(1))))
        val service                = new AdjustmentEntryServiceImpl(mockConnector)
        val exception              = intercept[RuntimeException] {
          service.createAdjustment(userAnswerWithRate).futureValue
        }
        exception.getLocalizedMessage must include(
          "Couldn't fetch correct AdjustmentEntry from user answers."
        )
      }

      "if neither TaxType or SmallProducerReliefDutyRate contain rate" in {
        val updatedAdjustmentEntry = adjustmentEntry.copy(
          rateBand = Some(rateBand.copy(rate = None)),
          sprDutyRate = None
        )
        val userAnswerWithRate     = emptyUserAnswers
          .set(CurrentAdjustmentEntryPage, updatedAdjustmentEntry)
          .success
          .value

        val mockConnector = mock[AlcoholDutyCalculatorConnector]
        when(mockConnector.calculateAdjustmentDuty(any(), any(), any())(any()))
          .thenReturn(Future.successful(AdjustmentDuty(BigDecimal(1))))

        val service = new AdjustmentEntryServiceImpl(mockConnector)

        val exception = intercept[RuntimeException] {
          service.createAdjustment(userAnswerWithRate).futureValue
        }

        exception.getLocalizedMessage must include(
          "Failed to get rate, neither tax rate nor spr duty rate are defined."
        )
      }
    }

    "if both TaxType and SmallProducerReliefDuty contain rate for RepackagedDraughtProducts" in {
      val updatedAdjustmentEntry = adjustmentEntry.copy(
        adjustmentType = Some(RepackagedDraughtProducts),
        repackagedRateBand = Some(rateBand),
        repackagedSprDutyRate = Some(BigDecimal(2)),
        repackagedDuty = Some(BigDecimal(1)),
        newDuty = Some(BigDecimal(2))
      )
      val userAnswerWithRate     = emptyUserAnswers
        .set(CurrentAdjustmentEntryPage, updatedAdjustmentEntry)
        .success
        .value
      val mockConnector          = mock[AlcoholDutyCalculatorConnector]
      when(mockConnector.calculateAdjustmentDuty(any(), any(), any())(any()))
        .thenReturn(Future.successful(AdjustmentDuty(BigDecimal(1))))
      when(mockConnector.calculateRepackagedDutyChange(any(), any())(any()))
        .thenReturn(Future.successful(AdjustmentDuty(BigDecimal(2))))
      val service                = new AdjustmentEntryServiceImpl(mockConnector)
      val exception              = intercept[RuntimeException] {
        service.createAdjustment(userAnswerWithRate).futureValue
      }
      exception.getLocalizedMessage must include(
        "Failed to get rate, both tax rate and spr duty rate are defined for repackaged draught products."
      )
    }

    "if neither TaxType or SmallProducerReliefDutyRate contain rate for RepackagedDraughtProducts" in {
      val updatedAdjustmentEntry = adjustmentEntry.copy(
        adjustmentType = Some(RepackagedDraughtProducts),
        repackagedRateBand = Some(rateBand.copy(rate = None)),
        repackagedSprDutyRate = None
      )
      val userAnswerWithRate     = emptyUserAnswers
        .set(CurrentAdjustmentEntryPage, updatedAdjustmentEntry)
        .success
        .value
      val mockConnector          = mock[AlcoholDutyCalculatorConnector]
      when(mockConnector.calculateAdjustmentDuty(any(), any(), any())(any()))
        .thenReturn(Future.successful(AdjustmentDuty(BigDecimal(1))))
      val service                = new AdjustmentEntryServiceImpl(mockConnector)
      val exception              = intercept[RuntimeException] {
        service.createAdjustment(userAnswerWithRate).futureValue
      }
      exception.getLocalizedMessage must include(
        "Failed to get rate, neither tax rate nor spr duty rate are defined for repackaged draught products."
      )
    }

    "for getError default case" in {
      val adjustmentEntry = AdjustmentEntry(
        rateBand = Some(rateBand.copy(rate = None)),
        sprDutyRate = Some(BigDecimal(1))
      )

      val service = new AdjustmentEntryServiceImpl(mock[AlcoholDutyCalculatorConnector])

      val exception = service.getError(adjustmentEntry)

      exception.getMessage must include("Failed to get rate.")
    }

    "for getRepackagedError default case" in {
      val adjustmentEntry = AdjustmentEntry(
        adjustmentType = Some(Spoilt),
        repackagedSprDutyRate = Some(BigDecimal(1))
      )

      val service = new AdjustmentEntryServiceImpl(mock[AlcoholDutyCalculatorConnector])

      val exception = service.getRepackagedError(adjustmentEntry)

      exception.getMessage must include("Failed to get rate for repackaged draught products.")
    }
  }
}
