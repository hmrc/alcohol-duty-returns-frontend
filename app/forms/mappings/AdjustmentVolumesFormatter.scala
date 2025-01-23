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
import models.adjustment.AdjustmentVolume
import play.api.data.FormError
import play.api.data.format.Formatter

class AdjustmentVolumesFormatter(
  lessOrEqualKey: String,
  volumeFormatter: BigDecimalFieldFormatter,
  pureAlcoholVolumeFormatter: BigDecimalFieldFormatter,
  args: Seq[String]
) extends Formatter[AdjustmentVolume]
    with Formatters {

  private def bindFormatters(
    key: String,
    data: Map[String, String]
  ): Either[Seq[FormError], AdjustmentVolume] = {
    val totalLitresEither = bindField(key, totalLitresField, volumeFormatter, data)
    val pureAlcoholEither = bindField(key, pureAlcoholField, pureAlcoholVolumeFormatter, data)
    (totalLitresEither, pureAlcoholEither) match {
      case (Right(totalLitres), Right(pureAlcohol)) =>
        Right(AdjustmentVolume(totalLitres, pureAlcohol))
      case _                                        =>
        Left(Seq(totalLitresEither, pureAlcoholEither).collect { case Left(error) =>
          error
        }.flatten)
    }
  }

  private def checkPureAlcoholIsNotGreaterThanTotalLitres(
    key: String,
    adjustmentVolumeWithSPR: AdjustmentVolume
  ): Either[Seq[FormError], AdjustmentVolume] =
    if (adjustmentVolumeWithSPR.totalLitresVolume < adjustmentVolumeWithSPR.pureAlcoholVolume) {
      Left(
        Seq(
          FormError(nameToId(s"$key.$pureAlcoholField"), lessOrEqualKey, args)
        )
      )
    } else {
      Right(
        AdjustmentVolume(
          adjustmentVolumeWithSPR.totalLitresVolume,
          adjustmentVolumeWithSPR.pureAlcoholVolume
        )
      )
    }

  override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], AdjustmentVolume] =
    for {
      adjustmentVolume <- bindFormatters(key, data)
      _                <- checkPureAlcoholIsNotGreaterThanTotalLitres(key, adjustmentVolume)
    } yield adjustmentVolume

  override def unbind(key: String, value: AdjustmentVolume): Map[String, String] =
    volumeFormatter.unbind(s"$key.$totalLitresField", value.totalLitresVolume) ++
      pureAlcoholVolumeFormatter.unbind(s"$key.$pureAlcoholField", value.pureAlcoholVolume)
}
