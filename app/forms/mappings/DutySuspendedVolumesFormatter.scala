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
import models.AlcoholRegime
import models.dutySuspended.{DutySuspendedRegimeSpecificKey, DutySuspendedVolume}
import play.api.data.FormError
import play.api.data.format.Formatter

class DutySuspendedVolumesFormatter(
  invalidKey: String,
  requiredKey: String,
  decimalPlacesKey: String,
  minimumValueKey: String,
  maximumValueKey: String,
  inconsistentKey: String,
  inconsistentSignKey: String,
  zeroTotalLitresKey: String,
  args: Seq[String],
  regime: AlcoholRegime
) extends Formatter[DutySuspendedVolume]
    with Formatters {

  private def volumeFormatter(fieldKey: String) = new BigDecimalFieldFormatter(
    requiredKey,
    invalidKey,
    decimalPlacesKey,
    minimumValueKey,
    maximumValueKey,
    fieldKey,
    maximumValue = Constants.dutySuspendedVolumeMaximumValue,
    minimumValue = Constants.dutySuspendedVolumeMinimumValue,
    args = args
  )

  private def pureAlcoholFormatter(fieldKey: String) = new BigDecimalFieldFormatter(
    requiredKey,
    invalidKey,
    decimalPlacesKey,
    minimumValueKey,
    maximumValueKey,
    fieldKey,
    decimalPlaces = Constants.lpaMaximumDecimalPlaces,
    maximumValue = Constants.dutySuspendedLpaMaximumValue,
    minimumValue = Constants.dutySuspendedLpaMinimumValue,
    args = args
  )

  private val totalVolumeKey = DutySuspendedRegimeSpecificKey.totalVolumeKey(regime)
  private val pureAlcoholKey = DutySuspendedRegimeSpecificKey.pureAlcoholKey(regime)

  private def requiredFieldFormError(key: String, field: String): FormError =
    FormError(nameToId(s"${key}_$field"), s"$requiredKey.$field", args)

  private def formatVolume(key: String, data: Map[String, String]): Either[Seq[FormError], DutySuspendedVolume] = {
    val totalLitres = volumeFormatter(totalVolumeKey).bind(s"$key.$totalVolumeKey", data)
    val pureAlcohol = pureAlcoholFormatter(pureAlcoholKey).bind(s"$key.$pureAlcoholKey", data)

    (totalLitres, pureAlcohol) match {
      case (Right(totalLitresValue), Right(pureAlcoholValue)) =>
        Right(DutySuspendedVolume(totalLitresValue, pureAlcoholValue))
      case (totalLitresError, pureAlcoholError)               =>
        Left(
          totalLitresError.left.getOrElse(Seq.empty)
            ++ pureAlcoholError.left.getOrElse(Seq.empty)
        )
    }
  }

  private def checkValues(key: String, data: Map[String, String]): Either[Seq[FormError], DutySuspendedVolume] =
    formatVolume(key, data).fold(
      errors => Left(errors),
      volumes =>
        if (volumes.total < volumes.pureAlcohol) {
          Left(Seq(FormError(nameToId(s"$key.$pureAlcoholKey"), inconsistentKey, args)))
        } else if ((volumes.total > 0) && (volumes.pureAlcohol < 0)) {
          Left(Seq(FormError(nameToId(s"$key.$pureAlcoholKey"), inconsistentSignKey, args)))
        } else if (volumes.total == 0 && volumes.pureAlcohol < 0) {
          Left(Seq(FormError(nameToId(s"$key.$pureAlcoholKey"), zeroTotalLitresKey, args)))
        } else {
          Right(volumes)
        }
    )

  override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], DutySuspendedVolume] = {
    val totalLitresResult = validateField(totalVolumeKey, key, data, volumeFormatter)
    val pureAlcoholResult = validateField(pureAlcoholKey, key, data, pureAlcoholFormatter)
    val allErrors         = totalLitresResult.left.toSeq.flatten ++ pureAlcoholResult.left.toSeq.flatten
    if (allErrors.nonEmpty) {
      Left(allErrors)
    } else {
      checkValues(key, data)
    }
  }

  private def validateField(
    field: String,
    key: String,
    data: Map[String, String],
    formatter: String => BigDecimalFieldFormatter
  ): Either[Seq[FormError], BigDecimal] =
    data.get(s"$key.$field").filter(_.nonEmpty) match {
      case Some(_) => formatter(field).bind(s"$key.$field", data)
      case None    => Left(Seq(requiredFieldFormError(key, field)))
    }

  override def unbind(key: String, value: DutySuspendedVolume): Map[String, String] =
    Map(
      s"$key.$totalVolumeKey" -> value.total.toString,
      s"$key.$pureAlcoholKey" -> value.pureAlcohol.toString
    )
}
