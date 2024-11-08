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

import com.google.inject.{ImplementedBy, Inject, Singleton}
import connectors.AlcoholDutyCalculatorConnector
import models.UserAnswers
import models.adjustment.{AdjustmentEntry, AdjustmentType}
import models.adjustment.AdjustmentType.RepackagedDraughtProducts
import models.adjustment.AdjustmentTypes.fromAdjustmentType
import pages.adjustment.CurrentAdjustmentEntryPage
import play.api.Logging
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AdjustmentEntryServiceImpl @Inject() (
  alcoholDutyCalculatorConnector: AlcoholDutyCalculatorConnector
)(implicit ec: ExecutionContext)
    extends AdjustmentEntryService
    with Logging {

  override def createAdjustment(
    userAnswers: UserAnswers
  )(implicit hc: HeaderCarrier): Future[AdjustmentEntry] = {
    val adjustmentEntry = for {
      adjustmentEntry   <- userAnswers.get(CurrentAdjustmentEntryPage)
      pureAlcoholVolume <- adjustmentEntry.pureAlcoholVolume
      adjustmentType    <- adjustmentEntry.adjustmentType
    } yield {
      val rate          = adjustmentEntry.rate.getOrElse(throw getError(adjustmentEntry))
      val taxDutyFuture = alcoholDutyCalculatorConnector
        .calculateAdjustmentDuty(pureAlcoholVolume, rate, fromAdjustmentType(adjustmentType))
        .map { taxDuty =>
          adjustmentEntry.copy(duty = Some(taxDuty.duty))
        }
      repackagedAdjustmentDutyCalculation(adjustmentEntry, pureAlcoholVolume, adjustmentType, taxDutyFuture)
    }
    adjustmentEntry match {
      case Some(adjustmentEntry) => adjustmentEntry
      case _                     =>
        logger.warn("Couldn't fetch correct AdjustmentEntry from user answers")
        Future.failed(new Exception("Couldn't fetch correct AdjustmentEntry from user answers"))
    }
  }

  private def repackagedAdjustmentDutyCalculation(
    adjustmentEntry: AdjustmentEntry,
    pureAlcoholVolume: BigDecimal,
    adjustmentType: AdjustmentType,
    taxDutyFuture: Future[AdjustmentEntry]
  )(implicit hc: HeaderCarrier): Future[AdjustmentEntry] =
    adjustmentType match {
      case RepackagedDraughtProducts =>
        val repackagedRate = adjustmentEntry.repackagedRate.getOrElse(throw getRepackagedError(adjustmentEntry))
        for {
          updatedAdjustment <- taxDutyFuture
          repackagedTaxDuty <-
            alcoholDutyCalculatorConnector.calculateAdjustmentDuty(
              pureAlcoholVolume,
              repackagedRate,
              fromAdjustmentType(adjustmentType)
            )
          newDuty           <-
            alcoholDutyCalculatorConnector.calculateRepackagedDutyChange(
              repackagedTaxDuty.duty,
              updatedAdjustment.duty.getOrElse(
                throw new RuntimeException("Couldn't fetch adjustment duty from user answers")
              )
            )
        } yield updatedAdjustment.copy(
          repackagedDuty = Some(repackagedTaxDuty.duty),
          newDuty = Some(newDuty.duty)
        )
      case _                         => taxDutyFuture
    }

  private[services] def getError(adjustmentEntry: AdjustmentEntry): RuntimeException =
    (adjustmentEntry.rateBand.flatMap(_.rate), adjustmentEntry.sprDutyRate) match {
      case (Some(_), Some(_)) =>
        new RuntimeException("Failed to get rate, both tax rate and spr duty rate are defined.")
      case (None, None)       => new RuntimeException("Failed to get rate, neither tax rate nor spr duty rate are defined.")
      case (_, _)             => new RuntimeException("Failed to get rate.")
    }

  private[services] def getRepackagedError(adjustmentEntry: AdjustmentEntry): RuntimeException =
    (adjustmentEntry.repackagedRateBand.flatMap(_.rate), adjustmentEntry.repackagedSprDutyRate) match {
      case (Some(_), Some(_)) =>
        new RuntimeException(
          "Failed to get rate, both tax rate and spr duty rate are defined for repackaged draught products."
        )
      case (None, None)       =>
        new RuntimeException(
          "Failed to get rate, neither tax rate nor spr duty rate are defined for repackaged draught products."
        )
      case (_, _)             => new RuntimeException("Failed to get rate for repackaged draught products.")
    }

}

@ImplementedBy(classOf[AdjustmentEntryServiceImpl])
trait AdjustmentEntryService {
  def createAdjustment(
    userAnswers: UserAnswers
  )(implicit hc: HeaderCarrier): Future[AdjustmentEntry]
}
