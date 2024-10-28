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
import models.declareDuty.VolumeAndRateByTaxType
import play.api.data.FormError
import play.api.data.format.Formatter

class VolumesAndRateFormatter(
  invalidKey: String,
  requiredKey: String,
  decimalPlacesKey: String,
  minimumValueKey: String,
  maximumValueKey: String,
  moreOrEqualKey: String,
  lessOrEqualKey: String,
  args: Seq[String]
) extends Formatter[VolumeAndRateByTaxType]
    with Formatters {

  private def volumeFormatter(fieldKey: String) = new BigDecimalFieldFormatter(
    requiredKey,
    invalidKey,
    decimalPlacesKey,
    minimumValueKey,
    maximumValueKey,
    fieldKey,
    maximumValue = Constants.volumeMaximumValue,
    minimumValue = Constants.volumeMinimumValue,
    args = args
  )

  private def pureAlcoholVolumeFormatter(fieldKey: String) = new BigDecimalFieldFormatter(
    requiredKey,
    invalidKey,
    decimalPlacesKey,
    minimumValueKey,
    maximumValueKey,
    fieldKey,
    decimalPlaces = Constants.lpaMaximumDecimalPlaces,
    maximumValue = Constants.lpaMaximumValue,
    minimumValue = Constants.lpaMinimumValue,
    args = args
  )

  private def dutyRateFormatter(fieldKey: String) = new BigDecimalFieldFormatter(
    requiredKey,
    invalidKey,
    decimalPlacesKey,
    minimumValueKey,
    maximumValueKey,
    fieldKey,
    maximumValue = Constants.dutyMaximumValue,
    minimumValue = Constants.dutyMinimumValue,
    args = args
  )

  private val NUMBER_OF_FIELDS        = 4
  private val fieldKeys: List[String] = List("taxType", "totalLitres", "pureAlcohol", "dutyRate")

  private def requiredFieldFormError(key: String, field: String): FormError =
    FormError(nameToId(s"${key}_$field"), s"$requiredKey.$field", args)

  private def formatVolume(key: String, data: Map[String, String]): Either[Seq[FormError], VolumeAndRateByTaxType] = {
    val taxType     = stringFormatter(s"$requiredKey.taxType").bind(s"$key.taxType", data)
    val totalLitres = volumeFormatter("totalLitres").bind(s"$key.totalLitres", data)
    val pureAlcohol = pureAlcoholVolumeFormatter("pureAlcohol").bind(s"$key.pureAlcohol", data)
    val dutyRate    = dutyRateFormatter("dutyRate").bind(s"$key.dutyRate", data)

    (taxType, totalLitres, pureAlcohol, dutyRate) match {
      case (Right(taxTypeValue), Right(totalLitresValue), Right(pureAlcoholValue), Right(dutyRate)) =>
        Right(VolumeAndRateByTaxType(taxTypeValue, totalLitresValue, pureAlcoholValue, dutyRate))
      case (taxTypeError, totalLitresError, pureAlcoholError, dutyRateError)                        =>
        Left(
          taxTypeError.left.getOrElse(Seq.empty)
            ++ totalLitresError.left.getOrElse(Seq.empty)
            ++ pureAlcoholError.left.getOrElse(Seq.empty)
            ++ dutyRateError.left.getOrElse(Seq.empty)
        )
    }
  }

  private def checkValues(key: String, data: Map[String, String]): Either[Seq[FormError], VolumeAndRateByTaxType] =
    formatVolume(key, data).fold(
      errors => Left(errors),
      dutyByTaxType =>
        if (dutyByTaxType.totalLitres < dutyByTaxType.pureAlcohol) {
          Left(
            Seq(
              FormError(nameToId(s"$key.totalLitres"), moreOrEqualKey, args),
              FormError(nameToId(s"$key.pureAlcohol"), lessOrEqualKey, args)
            )
          )
        } else {
          Right(dutyByTaxType)
        }
    )

  override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], VolumeAndRateByTaxType] = {
    val fields = fieldKeys.map { field =>
      field -> data.get(s"$key.$field").filter(_.nonEmpty)
    }.toMap

    lazy val missingFields = fields
      .withFilter(_._2.isEmpty)
      .map(_._1)
      .toList

    fields.count(_._2.isDefined) match {
      case NUMBER_OF_FIELDS =>
        checkValues(key, data)
      case _                =>
        Left(
          missingFields.map { field =>
            requiredFieldFormError(key, field)
          }
        )
    }
  }

  override def unbind(key: String, value: VolumeAndRateByTaxType): Map[String, String] =
    Map(
      s"$key.taxType"     -> value.taxType,
      s"$key.totalLitres" -> value.totalLitres.toString,
      s"$key.pureAlcohol" -> value.pureAlcohol.toString,
      s"$key.dutyRate"    -> value.dutyRate.toString
    )

}
