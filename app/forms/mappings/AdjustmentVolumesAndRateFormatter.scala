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

import config.Constants
import config.Constants.MappingFields._
import models.adjustment.AdjustmentVolumeWithSPR
import play.api.data.FormError
import play.api.data.format.Formatter

class AdjustmentVolumesAndRateFormatter(
  invalidKey: String,
  requiredKey: String,
  decimalPlacesKey: String,
  minimumValueKey: String,
  maximumValueKey: String,
  lessOrEqualKey: String,
  args: Seq[String]
) extends Formatter[AdjustmentVolumeWithSPR]
    with Formatters {

  private val volumeFormatter = new BigDecimalFieldFormatter(
    requiredKey,
    invalidKey,
    decimalPlacesKey,
    minimumValueKey,
    maximumValueKey,
    totalLitresField,
    maximumValue = Constants.volumeMaximumValue,
    minimumValue = Constants.volumeMinimumValue,
    args = args
  )

  private val pureAlcoholVolumeFormatter: BigDecimalFieldFormatter = new BigDecimalFieldFormatter(
    requiredKey,
    invalidKey,
    decimalPlacesKey,
    minimumValueKey,
    maximumValueKey,
    pureAlcoholField,
    decimalPlaces = Constants.lpaMaximumDecimalPlaces,
    maximumValue = Constants.lpaMaximumValue,
    minimumValue = Constants.lpaMinimumValue,
    exactDecimalPlacesRequired = true,
    args = args
  )

  private val sprDutyRateFormatter = new BigDecimalFieldFormatter(
    requiredKey,
    invalidKey,
    decimalPlacesKey,
    minimumValueKey,
    maximumValueKey,
    sprDutyRateField,
    maximumValue = Constants.dutyMaximumValue,
    minimumValue = Constants.dutyMinimumValue,
    args = args
  )

  private def validateVolumesAndRate(
    key: String,
    data: Map[String, String]
  ): Either[Seq[FormError], AdjustmentVolumeWithSPR] = {
    val fields  = Seq(
      (totalLitresField, volumeFormatter),
      (pureAlcoholField, pureAlcoholVolumeFormatter),
      (sprDutyRateField, sprDutyRateFormatter)
    )
    val results = fields.map { case (fieldsKey, formatter) =>
      bindField(key, fieldsKey, formatter, data)
    }
    val errors  = results.collect { case Left(error) => error }.flatten

    if (errors.nonEmpty) {
      Left(errors)
    } else {
      val Seq(totalLitresResult, pureAlcoholResult, sprDutyRateResult) = results
      (totalLitresResult, pureAlcoholResult, sprDutyRateResult) match {
        case (
              Right(totalLitres: BigDecimal),
              Right(pureAlcohol: BigDecimal),
              Right(sprDutyRate: BigDecimal)
            ) =>
          if (totalLitres < pureAlcohol) {
            Left(
              Seq(
                FormError(nameToId(s"$key.$pureAlcoholField"), lessOrEqualKey, args)
              )
            )

          } else {
            Right(AdjustmentVolumeWithSPR(totalLitres, pureAlcohol, sprDutyRate))
          }
        case _ =>
          Left(Seq(FormError(nameToId(s"$key"), invalidKey, args)))
      }
    }
  }

  override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], AdjustmentVolumeWithSPR] =
    validateVolumesAndRate(key, data)

  override def unbind(key: String, value: AdjustmentVolumeWithSPR): Map[String, String] =
    volumeFormatter.unbind(s"$key.$totalLitresField", value.totalLitresVolume) ++
      pureAlcoholVolumeFormatter.unbind(s"$key.$pureAlcoholField", value.pureAlcoholVolume) ++
      sprDutyRateFormatter.unbind(s"$key.$sprDutyRateField", value.sprDutyRate)
}
