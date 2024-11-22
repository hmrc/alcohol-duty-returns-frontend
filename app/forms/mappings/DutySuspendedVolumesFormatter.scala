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
import models.dutySuspended.DutySuspendedVolume
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
  zeroLPAKey: String,
  args: Seq[String]
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

  private def pureAlcoholBigDecimalFormatter(fieldKey: String) = new BigDecimalFieldFormatter(
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

  private val NUMBER_OF_FIELDS = 2
  private val totalVolumeKey = "totalLitresVolume"
  private val pureAlcoholKey = "pureAlcoholVolume"
  private val fieldKeys: List[String] = List(totalVolumeKey, pureAlcoholKey)

  private def requiredFieldFormError(key: String, field: String): FormError =
    FormError(nameToId(s"${key}_$field"), s"$requiredKey.$field", args)

  private def formatVolume(key: String, data: Map[String, String]): Either[Seq[FormError], DutySuspendedVolume] = {
    val totalLitres = volumeFormatter(totalVolumeKey).bind(s"$key.$totalVolumeKey", data)
    val pureAlcohol = pureAlcoholBigDecimalFormatter(pureAlcoholKey).bind(s"$key.$pureAlcoholKey", data)

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
        if (volumes.totalLitresVolume < volumes.pureAlcoholVolume) {
          Left(Seq(FormError(nameToId(s"$key.$pureAlcoholKey"), inconsistentKey, args)))
        }
        else if ((volumes.totalLitresVolume > 0) && (volumes.pureAlcoholVolume < 0)) {
           Left(Seq(FormError(nameToId(s"$key.$pureAlcoholKey"), inconsistentSignKey, args)))
        }
        else if (volumes.totalLitresVolume != 0 && volumes.pureAlcoholVolume == 0) {
          Left(Seq(FormError(nameToId(s"$key.$pureAlcoholKey"), zeroLPAKey, args)))
        } else if (volumes.totalLitresVolume == 0 && volumes.pureAlcoholVolume < 0) {
          Left(Seq(FormError(nameToId(s"$key.$pureAlcoholKey"), zeroTotalLitresKey, args)))
        } else {
          Right(volumes)
        }
    )

  override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], DutySuspendedVolume] = {
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

  override def unbind(key: String, value: DutySuspendedVolume): Map[String, String] =
    Map(
      s"$key.$totalVolumeKey" -> value.totalLitresVolume.toString,
      s"$key.$pureAlcoholKey" -> value.pureAlcoholVolume.toString
    )
}
