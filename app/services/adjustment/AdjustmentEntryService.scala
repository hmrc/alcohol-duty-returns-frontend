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
import models.adjustment.AdjustmentType.{RepackagedDraughtProducts, Underdeclaration}
import pages.adjustment.CurrentAdjustmentEntryPage
import uk.gov.hmrc.http.HeaderCarrier
import viewmodels.checkAnswers.adjustment.AdjustmentTypeHelper

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

    for {
      taxDuty <- alcoholDutyCalculatorConnector.calculateTaxDuty(pureAlcoholVolume, rate)
    } yield {
      val adjustmentType = AdjustmentTypeHelper.getAdjustmentTypeValue(adjustmentEntry)
      val dutyValue      = checkDutyValue(taxDuty.duty, adjustmentType)
      adjustmentEntry.copy(
        duty = Some(dutyValue)
      )
    }
  }

  private def getError(adjustmentEntry: AdjustmentEntry): RuntimeException =
    (adjustmentEntry.taxRate, adjustmentEntry.sprDutyRate) match {
      case (Some(_), Some(_)) =>
        new RuntimeException("Failed to get rate, both tax rate and spr duty rate are defined.")
      case (None, None)       => new RuntimeException("Failed to get rate, neither tax rate nor spr duty rate are defined.")
      case (_, _)             => new RuntimeException("Failed to get rate.")
    }

  private def checkDutyValue(duty: BigDecimal, adjustmentType: String): BigDecimal =
    if (adjustmentType.equals(Underdeclaration.toString) || adjustmentType.equals(RepackagedDraughtProducts.toString)) {
      duty
    } else {
      duty * -1
    }
}

@ImplementedBy(classOf[AdjustmentEntryServiceImpl])
trait AdjustmentEntryService {
  def createAdjustment(
    userAnswers: UserAnswers
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[AdjustmentEntry]
}
