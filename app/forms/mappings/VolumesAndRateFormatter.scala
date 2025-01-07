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
import models.declareDuty.VolumeAndRateByTaxType
import play.api.data.FormError
import play.api.data.format.Formatter
import cats.implicits._

class VolumesAndRateFormatter(
  invalidKey: String,
  lessOrEqualKey: String,
  taxTypeFormatter: Formatter[String],
  volumeFormatter: BigDecimalFieldFormatter,
  pureAlcoholVolumeFormatter: BigDecimalFieldFormatter,
  sprDutyRateFormatter: BigDecimalFieldFormatter,
  args: Seq[String]
) extends Formatter[VolumeAndRateByTaxType]
    with Formatters {

  private def validateVolumesAndRate(
    key: String,
    data: Map[String, String]
  ): Either[Seq[FormError], VolumeAndRateByTaxType] = {
    val fieldsWithFormatters = Seq(
      taxTypeField     -> taxTypeFormatter,
      totalLitresField -> volumeFormatter,
      pureAlcoholField -> pureAlcoholVolumeFormatter,
      sprDutyRateField -> sprDutyRateFormatter
    )
    val results              = fieldsWithFormatters.map { case (fieldKey, formatter) =>
      bindField(key, fieldKey, formatter, data)
    }
    val errors               = results.collect { case Left(error) => error }.flatten
    if (errors.nonEmpty) {
      Left(errors)
    } else {
      results.sequence match {
        case Right(Seq(taxType: String, totalLitres: BigDecimal, pureAlcohol: BigDecimal, sprDutyRate: BigDecimal)) =>
          if (totalLitres < pureAlcohol) {
            Left(
              Seq(
                FormError(nameToId(s"$key.$pureAlcoholField"), lessOrEqualKey, args)
              )
            )
          } else {
            Right(VolumeAndRateByTaxType(taxType, totalLitres, pureAlcohol, sprDutyRate))
          }
        case _                                                                                                      =>
          Left(Seq(FormError(nameToId(s"$key"), invalidKey, args))) //unreachable code
      }
    }
  }

  override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], VolumeAndRateByTaxType] =
    validateVolumesAndRate(key, data)

  override def unbind(key: String, value: VolumeAndRateByTaxType): Map[String, String] =//check if they all are working the same
    taxTypeFormatter.unbind(s"$key.$taxTypeField", value.taxType) ++
      volumeFormatter.unbind(s"$key.$totalLitresField", value.totalLitres) ++
      pureAlcoholVolumeFormatter.unbind(s"$key.$pureAlcoholField", value.pureAlcohol) ++
      sprDutyRateFormatter.unbind(s"$key.$sprDutyRateField", value.dutyRate)
}
