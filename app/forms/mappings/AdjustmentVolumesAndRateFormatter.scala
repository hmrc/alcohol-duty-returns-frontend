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

package forms.mappings

import config.Constants.MappingFields._
import models.adjustment.AdjustmentVolumeWithSPR
import play.api.data.FormError
import play.api.data.format.Formatter

class AdjustmentVolumesAndRateFormatter(
  lessOrEqualKey: String,
  volumeFormatter: BigDecimalFieldFormatter,
  pureAlcoholVolumeFormatter: BigDecimalFieldFormatter,
  sprDutyRateFormatter: BigDecimalFieldFormatter,
  args: Seq[String]
) extends Formatter[AdjustmentVolumeWithSPR]
    with Formatters {

  private def bindFormatters(
    key: String,
    data: Map[String, String]
  ): Either[Seq[FormError], AdjustmentVolumeWithSPR] = {
    val totalLitresEither = bindField(key, totalLitresField, volumeFormatter, data)
    val pureAlcoholEither = bindField(key, pureAlcoholField, pureAlcoholVolumeFormatter, data)
    val sprDutyRateEither = bindField(key, sprDutyRateField, sprDutyRateFormatter, data)
    (totalLitresEither, pureAlcoholEither, sprDutyRateEither) match {
      case (Right(totalLitres), Right(pureAlcohol), Right(sprDutyRate)) =>
        Right(AdjustmentVolumeWithSPR(totalLitres, pureAlcohol, sprDutyRate))
      case _                                                            =>
        Left(Seq(totalLitresEither, pureAlcoholEither, sprDutyRateEither).collect { case Left(error) =>
          error
        }.flatten)
    }
  }

  private def checkPureAlcoholIsNotGreaterThanTotalLitres(
    key: String,
    adjustmentVolumeWithSPR: AdjustmentVolumeWithSPR
  ): Either[Seq[FormError], AdjustmentVolumeWithSPR] =
    if (adjustmentVolumeWithSPR.totalLitresVolume < adjustmentVolumeWithSPR.pureAlcoholVolume) {
      Left(
        Seq(
          FormError(nameToId(s"$key.$pureAlcoholField"), lessOrEqualKey, args)
        )
      )
    } else {
      Right(
        AdjustmentVolumeWithSPR(
          adjustmentVolumeWithSPR.totalLitresVolume,
          adjustmentVolumeWithSPR.pureAlcoholVolume,
          adjustmentVolumeWithSPR.sprDutyRate
        )
      )
    }

  override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], AdjustmentVolumeWithSPR] =
    for {
      adjustmentVolumeWithSPR <- bindFormatters(key, data)
      _                       <- checkPureAlcoholIsNotGreaterThanTotalLitres(key, adjustmentVolumeWithSPR)
    } yield adjustmentVolumeWithSPR

  override def unbind(key: String, value: AdjustmentVolumeWithSPR): Map[String, String] =
    volumeFormatter.unbind(s"$key.$totalLitresField", value.totalLitresVolume) ++
      pureAlcoholVolumeFormatter.unbind(s"$key.$pureAlcoholField", value.pureAlcoholVolume) ++
      sprDutyRateFormatter.unbind(s"$key.$sprDutyRateField", value.sprDutyRate)
}
