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

package services.adjustmentEntry

import com.google.inject.{ImplementedBy, Inject, Singleton}
import connectors.AlcoholDutyCalculatorConnector
import models.UserAnswers
import models.adjustment.AdjustmentEntry
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

    val adjustmentEntry =
      userAnswers
        .get(CurrentAdjustmentEntryPage)
        .getOrElse(throw new RuntimeException("Can't fetch adjustment entry from cache"))
    val abv             = adjustmentEntry.abv.getOrElse(throw new RuntimeException("Can't fetch ABV from cache"))
    val volume          = adjustmentEntry.volume.getOrElse(throw new RuntimeException("Can't fetch volume from cache"))
    val rate            = adjustmentEntry.rate.getOrElse(throw getError(adjustmentEntry))

    for {
      taxDuty <- alcoholDutyCalculatorConnector.calculateTaxDuty(abv, volume, rate)
    } yield adjustmentEntry.copy(
      pureAlcoholVolume = Some(taxDuty.pureAlcoholVolume),
      duty = Some(taxDuty.duty)
    )
  }

  private def getError(adjustmentEntry: AdjustmentEntry): RuntimeException =
    (adjustmentEntry.taxRate, adjustmentEntry.sprDutyRate) match {
      case (Some(_), Some(_)) =>
        new RuntimeException("Failed to get rate, both tax rate and spr duty rate are defined.")
      case (None, None)       => new RuntimeException("Failed to get rate, neither tax rate nor spr duty rate are defined.")
      case (_, _)             => new RuntimeException("Failed to get rate.")
    }
}

@ImplementedBy(classOf[AdjustmentEntryServiceImpl])
trait AdjustmentEntryService {
  def createAdjustment(
    userAnswers: UserAnswers
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[AdjustmentEntry]
}
