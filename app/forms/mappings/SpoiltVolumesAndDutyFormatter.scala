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

  private val dutyFormatter = new BigDecimalFieldFormatter(
    requiredKey,
    invalidKey,
    decimalPlacesKey,
    minimumValueKey,
    maximumValueKey,
    dutyField,
    maximumValue = Constants.spoiltDutyMaximumValue,
    minimumValue = Constants.spoiltDutyMinimumValue,
    args = args
  )

  private def requiredFieldFormError(key: String, field: String): FormError =
    FormError(nameToId(s"$key.$field"), s"$requiredKey.$field", args)

  private def formatVolume(key: String, data: Map[String, String]): Either[Seq[FormError], SpoiltVolumeWithDuty] = {
    val totalLitres = volumeFormatter.bind(s"$key.$totalLitresField", data)
    val pureAlcohol = pureAlcoholVolumeFormatter.bind(s"$key.$pureAlcoholField", data)
    val dutyRate    = dutyFormatter.bind(s"$key.$dutyField", data)

    (totalLitres, pureAlcohol, dutyRate) match {
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
          Left(Seq(FormError(nameToId(s"$key.$pureAlcoholField"), inconsistentKey, args)))
        } else {
          Right(volumes)
        }
    )

  override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], SpoiltVolumeWithDuty] = {
    val totalLitresResult = validateField(totalLitresField, key, data, volumeFormatter)
    val pureAlcoholResult = validateField(pureAlcoholField, key, data, pureAlcoholVolumeFormatter)
    val dutyRateResult    = validateField(dutyField, key, data, dutyFormatter)
    val allErrors         =
      totalLitresResult.left.toSeq.flatten ++ pureAlcoholResult.left.toSeq.flatten ++ dutyRateResult.left.toSeq.flatten
    if (allErrors.nonEmpty) {
      Left(allErrors)
    } else {
      checkValues(key, data)
    }
  }

  private def validateField[T](
    field: String,
    key: String,
    data: Map[String, String],
    formatter: Formatter[T]
  ): Either[Seq[FormError], T] =
    data.get(s"$key.$field").filter(_.nonEmpty) match {
      case Some(_) => formatter.bind(s"$key.$field", data)
      case None    => Left(Seq(requiredFieldFormError(key, field)))
    }

  override def unbind(key: String, value: SpoiltVolumeWithDuty): Map[String, String] =
    volumeFormatter.unbind(s"$key.$totalLitresField", value.totalLitresVolume) ++
      pureAlcoholVolumeFormatter.unbind(s"$key.$pureAlcoholField", value.pureAlcoholVolume) ++
      dutyFormatter.unbind(s"$key.$dutyField", value.duty)
}
