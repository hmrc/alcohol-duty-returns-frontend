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

import models.adjustment.AdjustmentVolume
import play.api.data.FormError
import play.api.data.format.Formatter

class AdjustmentVolumesFormatter(
  invalidKey: String,
  allRequiredKey: String,
  requiredKey: String,
  decimalPlacesKey: String,
  minimumValueKey: String,
  maximumValueKey: String,
  inconsistentKey: String,
  args: Seq[String]
) extends Formatter[AdjustmentVolume]
    with Formatters {

  def bigDecimalFormatter(fieldKey: String) = new BigDecimalFieldFormatter(
    requiredKey,
    invalidKey,
    decimalPlacesKey,
    minimumValueKey,
    maximumValueKey,
    fieldKey,
    args
  )

  def pureAlcoholBigDecimalFormatter(fieldKey: String) = new BigDecimalFieldFormatter(
    requiredKey,
    invalidKey,
    decimalPlacesKey,
    minimumValueKey,
    maximumValueKey,
    fieldKey,
    args,
    decimalPlaces = 4,
    maximumValue = BigDecimal(999999999.9999),
    minimumValue = BigDecimal(0.0001)
  )

  val NUMBER_OF_FIELDS = 2

  val fieldKeys: List[String] = List("totalLitresVolume", "pureAlcoholVolume")

  def requiredFieldFormError(key: String, field: String): FormError =
    FormError(nameToId(s"${key}_$field"), s"$requiredKey.$field", args)

  def requiredAllFieldsFormError(key: String): FormError =
    FormError(key, allRequiredKey, args)

  def formatVolume(key: String, data: Map[String, String]): Either[Seq[FormError], AdjustmentVolume] = {

    val totalLitres = bigDecimalFormatter("totalLitresVolume").bind(s"$key.totalLitresVolume", data)
    val pureAlcohol = pureAlcoholBigDecimalFormatter("pureAlcoholVolume").bind(s"$key.pureAlcoholVolume", data)

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

  def checkValues(key: String, data: Map[String, String]): Either[Seq[FormError], AdjustmentVolume] =
    formatVolume(key, data).fold(
      errors => Left(errors),
      volumes =>
        if (volumes.totalLitresVolume < volumes.pureAlcoholVolume) {
          Left(Seq(FormError(nameToId(s"$key.pureAlcoholVolume"), inconsistentKey, args)))
        } else {
          Right(volumes)
        }
    )

  override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], AdjustmentVolume] = {
    val fields = fieldKeys.map { field =>
      field -> data.get(s"$key.$field").filter(_.nonEmpty)
    }.toMap

    lazy val missingFields = fields
      .withFilter(_._2.isEmpty)
      .map(_._1)
      .toList

    fields.count(_._2.isDefined) match {
      case NUMBER_OF_FIELDS                             =>
        checkValues(key, data)
      case size if size < NUMBER_OF_FIELDS && size >= 0 =>
        Left(
          missingFields.map { field =>
            requiredFieldFormError(key, field)
          }
        )
      case _                                            =>
        Left(List(requiredAllFieldsFormError(key)))
    }
  }

  override def unbind(key: String, value: AdjustmentVolume): Map[String, String] =
    Map(
      s"$key.totalLitresVolume" -> value.totalLitresVolume.toString,
      s"$key.pureAlcoholVolume" -> value.pureAlcoholVolume.toString
    )
}
