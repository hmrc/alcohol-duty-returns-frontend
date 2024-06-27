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

import forms.mappings.BigDecimalFieldFormatter.nameToId
import models.adjustment.AdjustmentVolumeWithSPR
import play.api.data.FormError
import play.api.data.format.Formatter

class AdjustmentVolumesAndRateFormatter(
                               invalidKey: String,
                               allRequiredKey: String,
                               requiredKey: String,
                               decimalPlacesKey: String,
                               minimumValueKey: String,
                               maximumValueKey: String,
                               inconsistentKey: String,
                               args: Seq[String]
                             ) extends Formatter[AdjustmentVolumeWithSPR]
  with Formatters {

  def volumeFormatter(fieldKey: String) = new BigDecimalFieldFormatter(
    requiredKey,
    invalidKey,
    decimalPlacesKey,
    minimumValueKey,
    maximumValueKey,
    fieldKey,
    minimumValue = BigDecimal(0.00),//what's this for
    args = args
  )

  def dutyRateFormatter(fieldKey: String) = new BigDecimalFieldFormatter(
    requiredKey,
    invalidKey,
    decimalPlacesKey,
    minimumValueKey,
    maximumValueKey,
    fieldKey,
    minimumValue = BigDecimal(0.0),
    args = args
  )

  val NUMBER_OF_FIELDS        = 3
  val fieldKeys: List[String] = List("totalLitersVolume", "pureAlcoholVolume", "sprDutyRate")

  def requiredFieldFormError(key: String, field: String): FormError =
    FormError(nameToId(s"${key}_$field"), s"$requiredKey.$field", args)

  def requiredAllFieldsFormError(key: String): FormError =
    FormError(nameToId(key), allRequiredKey, args)

  def formatVolume(key: String, data: Map[String, String]): Either[Seq[FormError], AdjustmentVolumeWithSPR] = {
    val totalLitres = volumeFormatter("totalLitersVolume").bind(s"$key.totalLitersVolume", data)
    val pureAlcohol = volumeFormatter("pureAlcoholVolume").bind(s"$key.pureAlcoholVolume", data)
    val sprDutyRate    = dutyRateFormatter("sprDutyRate").bind(s"$key.sprDutyRate", data)

    (totalLitres, pureAlcohol, sprDutyRate) match {
      case (Right(totalLitresValue), Right(pureAlcoholValue), Right(sprDutyRate)) =>
        Right(AdjustmentVolumeWithSPR(totalLitresValue, pureAlcoholValue, sprDutyRate))
      case (totalLitresError, pureAlcoholError, sprDutyRateError)                        =>
        Left(
          totalLitresError.left.getOrElse(Seq.empty)
            ++ pureAlcoholError.left.getOrElse(Seq.empty)
            ++ sprDutyRateError.left.getOrElse(Seq.empty)
        )
    }
  }

  def checkValues(key: String, data: Map[String, String]): Either[Seq[FormError], AdjustmentVolumeWithSPR] =
    formatVolume(key, data).fold(
      errors => Left(errors),
      volumes =>
        if (volumes.totalLitersVolume < volumes.pureAlcoholVolume) {
          Left(Seq(FormError(nameToId(s"$key.pureAlcoholVolume"), inconsistentKey)))
        } else {
          Right(volumes)
        }
    )

  override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], AdjustmentVolumeWithSPR] = {
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

  override def unbind(key: String, value: AdjustmentVolumeWithSPR): Map[String, String] =
    Map(
      s"$key.totalLitersVolume" -> value.totalLitersVolume.toString,
      s"$key.pureAlcoholVolume" -> value.pureAlcoholVolume.toString,
      s"$key.sprDutyRate"    -> value.sprDutyRate.toString
    )

}
