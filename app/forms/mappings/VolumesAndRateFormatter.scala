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
    val taxTypeResult     = validateField("taxType", key, data, stringFormatter("taxType"))
    val totalLitresResult = validateField("totalLitres", key, data, volumeFormatter("totalLitres"))
    val pureAlcoholResult = validateField("pureAlcohol", key, data, pureAlcoholVolumeFormatter("pureAlcohol"))
    val dutyRateResult    = validateField("dutyRate", key, data, dutyRateFormatter("dutyRate"))
    val allErrors         =
      totalLitresResult.left.toSeq.flatten ++ pureAlcoholResult.left.toSeq.flatten ++ dutyRateResult.left.toSeq.flatten ++ taxTypeResult.left.toSeq.flatten
    if (allErrors.nonEmpty) {
      Left(allErrors)
    } else {
      checkValues(key, data)
    }
  }

  private[mappings] def validateField[T](
    field: String,
    key: String,
    data: Map[String, String],
    formatter: Formatter[T]
  ): Either[Seq[FormError], T] =
    data.get(s"$key.$field").filter(_.nonEmpty) match {
      case Some(_) => formatter.bind(s"$key.$field", data)
      case None    => Left(Seq(requiredFieldFormError(key, field)))
    }

  override def unbind(key: String, value: VolumeAndRateByTaxType): Map[String, String] =
    Map(
      s"$key.taxType"     -> value.taxType,
      s"$key.totalLitres" -> value.totalLitres.toString,
      s"$key.pureAlcohol" -> value.pureAlcohol.toString,
      s"$key.dutyRate"    -> value.dutyRate.toString
    )

}
