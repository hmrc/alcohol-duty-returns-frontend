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
import models.returns.VolumesByTaxType
import play.api.data.FormError
import play.api.data.format.Formatter

class VolumesFormatter(
  invalidKey: String,
  allRequiredKey: String,
  requiredKey: String,
  decimalPlacesKey: String,
  minimumValueKey: String,
  maximumValueKey: String,
  moreOrEqualKey: String,
  lessOrEqualKey: String,
  args: Seq[String]
) extends Formatter[VolumesByTaxType]
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

  val NUMBER_OF_FIELDS = 3

  val fieldKeys: List[String] = List("taxType", "totalLitres", "pureAlcohol")

  def requiredFieldFormError(key: String, field: String): FormError =
    FormError(nameToId(s"${key}_$field"), s"$requiredKey.$field", args)

  def requiredAllFieldsFormError(key: String): FormError =
    FormError(key, allRequiredKey, args)

  def formatVolume(key: String, data: Map[String, String]): Either[Seq[FormError], VolumesByTaxType] = {

    val taxType     = stringFormatter(s"$requiredKey.taxType").bind(s"$key.taxType", data)
    val totalLitres = bigDecimalFormatter("totalLitres").bind(s"$key.totalLitres", data)
    val pureAlcohol = bigDecimalFormatter("pureAlcohol").bind(s"$key.pureAlcohol", data)

    (taxType, totalLitres, pureAlcohol) match {
      case (Right(taxTypeValue), Right(totalLitresValue), Right(pureAlcoholValue)) =>
        Right(VolumesByTaxType(taxTypeValue, totalLitresValue, pureAlcoholValue))
      case (taxTypeError, totalLitresError, pureAlcoholError)                      =>
        Left(
          taxTypeError.left.getOrElse(Seq.empty)
            ++ totalLitresError.left.getOrElse(Seq.empty)
            ++ pureAlcoholError.left.getOrElse(Seq.empty)
        )
    }
  }

  def checkValues(key: String, data: Map[String, String]): Either[Seq[FormError], VolumesByTaxType] =
    formatVolume(key, data).fold(
      errors => Left(errors),
      volumesByTaxType =>
        if (volumesByTaxType.totalLitres < volumesByTaxType.pureAlcohol) {
          Left(
            Seq(
              FormError(nameToId(s"$key.totalLitres"), moreOrEqualKey, args),
              FormError(nameToId(s"$key.pureAlcohol"), lessOrEqualKey, args)
            )
          )
        } else {
          Right(volumesByTaxType)
        }
    )

  override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], VolumesByTaxType] = {
    val fields = fieldKeys.map { field =>
      field -> data.get(s"$key.$field").filter(_.nonEmpty)
    }.toMap

    lazy val missingFields = fields
      .withFilter(_._2.isEmpty)
      .map(_._1)
      .toList

    fields.count(_._2.isDefined) match {
      case NUMBER_OF_FIELDS                            =>
        checkValues(key, data)
      case size if size < NUMBER_OF_FIELDS && size > 0 =>
        Left(
          missingFields.map { field =>
            requiredFieldFormError(key, field)
          }
        )
      case _                                           =>
        Left(List(requiredAllFieldsFormError(key)))
    }
  }

  override def unbind(key: String, value: VolumesByTaxType): Map[String, String] =
    Map(
      s"$key.taxType"     -> value.taxType,
      s"$key.totalLitres" -> value.totalLitres.toString,
      s"$key.pureAlcohol" -> value.pureAlcohol.toString
    )
}
