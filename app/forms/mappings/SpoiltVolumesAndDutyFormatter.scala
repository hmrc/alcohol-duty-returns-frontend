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
import models.adjustment.SpoiltVolumeWithDuty
import play.api.data.FormError
import play.api.data.format.Formatter

class SpoiltVolumesAndDutyFormatter(
  invalidKey: String,
  requiredKey: String,
  decimalPlacesKey: String,
  minimumValueKey: String,
  maximumValueKey: String,
  inconsistentKey: String,
  args: Seq[String]
) extends Formatter[SpoiltVolumeWithDuty]
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
    exactDecimalPlacesRequired = true,
    args = args
  )

  private def dutyFormatter(fieldKey: String) = new BigDecimalFieldFormatter(
    requiredKey,
    invalidKey,
    decimalPlacesKey,
    minimumValueKey,
    maximumValueKey,
    fieldKey,
    maximumValue = Constants.spoiltDutyMaximumValue,
    minimumValue = Constants.spoiltDutyMinimumValue,
    args = args
  )

  private def requiredFieldFormError(key: String, field: String): FormError =
    FormError(nameToId(s"${key}_$field"), s"$requiredKey.$field", args)

  private def formatVolume(key: String, data: Map[String, String]): Either[Seq[FormError], SpoiltVolumeWithDuty] = {
    val totalLitres = volumeFormatter("totalLitresVolume").bind(s"$key.totalLitresVolume", data)
    val pureAlcohol = pureAlcoholVolumeFormatter("pureAlcoholVolume").bind(s"$key.pureAlcoholVolume", data)
    val duty        = dutyFormatter("duty").bind(s"$key.duty", data)

    (totalLitres, pureAlcohol, duty) match {
      case (Right(totalLitresValue), Right(pureAlcoholValue), Right(duty)) =>
        Right(SpoiltVolumeWithDuty(totalLitresValue, pureAlcoholValue, duty))
      case (totalLitresError, pureAlcoholError, duty)                      =>
        Left(
          totalLitresError.left.getOrElse(Seq.empty)
            ++ pureAlcoholError.left.getOrElse(Seq.empty)
            ++ duty.left.getOrElse(Seq.empty)
        )
    }
  }

  private def checkValues(key: String, data: Map[String, String]): Either[Seq[FormError], SpoiltVolumeWithDuty] =
    formatVolume(key, data).fold(
      errors => Left(errors),
      volumes =>
        if (volumes.totalLitresVolume < volumes.pureAlcoholVolume) {
          Left(Seq(FormError(nameToId(s"$key.pureAlcoholVolume"), inconsistentKey, args)))
        } else {
          Right(volumes)
        }
    )

  override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], SpoiltVolumeWithDuty] = {
    val totalLitresVolumeResult = validateField("totalLitresVolume", key, data, volumeFormatter)
    val pureAlcoholVolumeResult = validateField("pureAlcoholVolume", key, data, pureAlcoholVolumeFormatter)
    val dutyResult              = validateField("duty", key, data, dutyFormatter)
    val allErrors               =
      totalLitresVolumeResult.left.toSeq.flatten ++ pureAlcoholVolumeResult.left.toSeq.flatten ++ dutyResult.left.toSeq.flatten
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

  override def unbind(key: String, value: SpoiltVolumeWithDuty): Map[String, String] =
    Map(
      s"$key.totalLitresVolume" -> value.totalLitresVolume.toString,
      s"$key.pureAlcoholVolume" -> value.pureAlcoholVolume.toString,
      s"$key.duty"              -> value.duty.toString
    )
}
