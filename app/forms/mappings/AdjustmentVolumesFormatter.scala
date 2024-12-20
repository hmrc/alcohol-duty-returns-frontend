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
import models.adjustment.AdjustmentVolume
import play.api.data.FormError
import play.api.data.format.Formatter

class AdjustmentVolumesFormatter(
  invalidKey: String,
  requiredKey: String,
  decimalPlacesKey: String,
  minimumValueKey: String,
  maximumValueKey: String,
  inconsistentKey: String,
  args: Seq[String]
) extends Formatter[AdjustmentVolume]
    with Formatters {

  private val totalLitresVolumeFormatter = new BigDecimalFieldFormatter(
    requiredKey,
    invalidKey,
    decimalPlacesKey,
    minimumValueKey,
    maximumValueKey,
    totalLitresVolumeField,
    maximumValue = Constants.volumeMaximumValue,
    minimumValue = Constants.volumeMinimumValue,
    args = args
  )

  private val pureAlcoholVolumeFormatter: BigDecimalFieldFormatter          = new BigDecimalFieldFormatter(
    requiredKey,
    invalidKey,
    decimalPlacesKey,
    minimumValueKey,
    maximumValueKey,
    pureAlcoholVolumeField,
    decimalPlaces = Constants.lpaMaximumDecimalPlaces,
    maximumValue = Constants.lpaMaximumValue,
    minimumValue = Constants.lpaMinimumValue,
    exactDecimalPlacesRequired = true,
    args = args
  )
  private def requiredFieldFormError(key: String, field: String): FormError =
    FormError(nameToId(s"$key.$field"), s"$requiredKey.$field", args)

  private def formatVolume(key: String, data: Map[String, String]): Either[Seq[FormError], AdjustmentVolume] = {
    val totalLitres = totalLitresVolumeFormatter.bind(s"$key.$totalLitresVolumeField", data)
    val pureAlcohol = pureAlcoholVolumeFormatter.bind(s"$key.$pureAlcoholVolumeField", data)

    (totalLitres, pureAlcohol) match {
      case (Right(totalLitresValue), Right(pureAlcoholValue)) =>
        Right(AdjustmentVolume(totalLitresValue, pureAlcoholValue))
      case (totalLitresError, pureAlcoholError)               =>
        Left(
          totalLitresError.left.getOrElse(Seq.empty)
            ++ pureAlcoholError.left.getOrElse(Seq.empty)
        )
    }
  }

  private def checkValues(key: String, data: Map[String, String]): Either[Seq[FormError], AdjustmentVolume] =
    formatVolume(key, data).fold(
      errors => Left(errors),
      volumes =>
        if (volumes.totalLitresVolume < volumes.pureAlcoholVolume) {
          Left(Seq(FormError(nameToId(s"$key.$pureAlcoholVolumeField"), inconsistentKey, args)))
        } else {
          Right(volumes)
        }
    )

  override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], AdjustmentVolume] = {
    val totalLitresResult = validateField(totalLitresVolumeField, key, data, totalLitresVolumeFormatter)
    val pureAlcoholResult = validateField(pureAlcoholVolumeField, key, data, pureAlcoholVolumeFormatter)
    val allErrors         = totalLitresResult.left.toSeq.flatten ++ pureAlcoholResult.left.toSeq.flatten
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

  override def unbind(key: String, value: AdjustmentVolume): Map[String, String] =
    totalLitresVolumeFormatter.unbind(s"$key.$totalLitresVolumeField", value.totalLitresVolume) ++
      pureAlcoholVolumeFormatter.unbind(s"$key.$pureAlcoholVolumeField", value.pureAlcoholVolume)
}
