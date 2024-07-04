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
import models.adjustment.AdjustmentEntry
import models.adjustment.AdjustmentType.RepackagedDraughtProducts
import models.adjustment.AdjustmentTypes.fromAdjustmentType
import pages.adjustment.CurrentAdjustmentEntryPage
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AdjustmentEntryServiceImpl @Inject() (
  alcoholDutyCalculatorConnector: AlcoholDutyCalculatorConnector
) extends AdjustmentEntryService {

  override def createAdjustment(
    userAnswers: UserAnswers
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[AdjustmentEntry] = {

    val adjustmentEntry   =
      userAnswers
        .get(CurrentAdjustmentEntryPage)
        .getOrElse(throw new RuntimeException("Can't fetch adjustment entry from cache"))
    val pureAlcoholVolume =
      adjustmentEntry.pureAlcoholVolume.getOrElse(
        throw new RuntimeException("Can't fetch pure alcohol volume from cache")
      )
    val rate              = adjustmentEntry.rate.getOrElse(throw getError(adjustmentEntry))
    val adjustmentType    =
      adjustmentEntry.adjustmentType.getOrElse(throw new RuntimeException("Couldn't fetch adjustmentType from cache"))

    val taxDutyFuture           =
      alcoholDutyCalculatorConnector.calculateTaxDuty(pureAlcoholVolume, rate, fromAdjustmentType(adjustmentType)).map {
        taxDuty =>
          adjustmentEntry.copy(duty = Some(taxDuty.duty))
      }
    val updatedAdjustmentFuture = adjustmentType match {
      case RepackagedDraughtProducts =>
        val repackagedRate = adjustmentEntry.repackagedRate.getOrElse(throw getRepackagedError(adjustmentEntry))
        for {
          updatedAdjustment <- taxDutyFuture
          repackagedTaxDuty <-
            alcoholDutyCalculatorConnector.calculateTaxDuty(
              pureAlcoholVolume,
              repackagedRate,
              fromAdjustmentType(adjustmentType)
            )
          newDuty           <- alcoholDutyCalculatorConnector.calculateAdjustmentTaxDuty(
                                 repackagedTaxDuty.duty,
                                 updatedAdjustment.duty.getOrElse(BigDecimal(0))
                               )
        } yield updatedAdjustment.copy(
          repackagedDuty = Some(repackagedTaxDuty.duty),
          newDuty = Some(newDuty.duty)
        )
      case _                         => taxDutyFuture
    }
    updatedAdjustmentFuture
  }

  private def getError(adjustmentEntry: AdjustmentEntry): RuntimeException =
    (adjustmentEntry.rateBand.flatMap(_.rate), adjustmentEntry.sprDutyRate) match {
      case (Some(_), Some(_)) =>
        new RuntimeException("Failed to get rate, both tax rate and spr duty rate are defined.")
      case (None, None)       => new RuntimeException("Failed to get rate, neither tax rate nor spr duty rate are defined.")
      case (_, _)             => new RuntimeException("Failed to get rate.")
    }

  private def getRepackagedError(adjustmentEntry: AdjustmentEntry): RuntimeException =
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
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[AdjustmentEntry]
}
