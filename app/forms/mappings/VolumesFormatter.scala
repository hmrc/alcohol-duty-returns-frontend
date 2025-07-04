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

import cats.implicits.toBifunctorOps
import config.Constants
import config.Constants.MappingFields._
import models.declareDuty.VolumesByTaxType
import play.api.data.FormError
import play.api.data.format.Formatter

class VolumesFormatter(
  invalidKey: String,
  requiredKey: String,
  decimalPlacesKey: String,
  minimumValueKey: String,
  maximumValueKey: String,
  lessOrEqualKey: String,
  regimeName: String
) extends Formatter[VolumesByTaxType]
    with Formatters {

  private val taxTypeFormatter = stringFormatter(s"$requiredKey.$taxTypeField")

  private val volumeFormatter = new BigDecimalFieldFormatter(
    requiredKey,
    invalidKey,
    decimalPlacesKey,
    minimumValueKey,
    maximumValueKey,
    totalLitresField,
    maximumValue = Constants.volumeMaximumValue,
    minimumValue = Constants.volumeMinimumValue,
    exactDecimalPlacesRequired = true,
    args = Seq(regimeName)
  )

  private val pureAlcoholBigDecimalFormatter = new BigDecimalFieldFormatter(
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
    args = Seq(regimeName)
  )

  private def requiredFieldFormError(key: String, field: String, rateBandDescription: String): FormError =
    FormError(nameToId(s"$key.$field"), s"$requiredKey.$field", Seq(rateBandDescription, regimeName))

  private def formatVolume(
    key: String,
    data: Map[String, String]
  ): Either[Seq[FormError], VolumesByTaxType] = {
    val taxType     = taxTypeFormatter.bind(s"$key.$taxTypeField", data)
    val totalLitres = volumeFormatter.bind(s"$key.$totalLitresField", data)
    val pureAlcohol = pureAlcoholBigDecimalFormatter.bind(s"$key.$pureAlcoholField", data)

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

  private def checkValues(
    key: String,
    data: Map[String, String],
    rateBandDescription: String
  ): Either[Seq[FormError], VolumesByTaxType] =
    formatVolume(key, data).fold(
      errors => Left(errors),
      volumesByTaxType =>
        if (volumesByTaxType.totalLitres < volumesByTaxType.pureAlcohol) {
          Left(
            Seq(
              FormError(nameToId(s"$key.$pureAlcoholField"), lessOrEqualKey, Seq(rateBandDescription, regimeName))
            )
          )
        } else {
          Right(volumesByTaxType)
        }
    )

  override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], VolumesByTaxType] = {
    val rateBandDescription = data.getOrElse(
      s"$key.$rateBandDescriptionField",
      throw new IllegalArgumentException(s"Expected $key.$rateBandDescriptionField to be provided in the view")
    )
    validateField(taxTypeField, key, data, taxTypeFormatter, rateBandDescription).leftMap(_ =>
      throw new IllegalArgumentException(s"Expected $key.$taxTypeField to be provided in the view")
    )
    val totalLitresResult   = validateField(totalLitresField, key, data, volumeFormatter, rateBandDescription)
    val pureAlcoholResult   =
      validateField(pureAlcoholField, key, data, pureAlcoholBigDecimalFormatter, rateBandDescription)

    val allErrors =
      totalLitresResult.left.toSeq.flatten ++ pureAlcoholResult.left.toSeq.flatten
    if (allErrors.nonEmpty) {
      Left(allErrors)
    } else {
      checkValues(key, data, rateBandDescription)
    }
  }

  private def validateField[T](
    field: String,
    key: String,
    data: Map[String, String],
    formatter: Formatter[T],
    rateBandDescription: String
  ): Either[Seq[FormError], T] =
    data.get(s"$key.$field").filter(_.nonEmpty) match {
      case Some(_) => formatter.bind(s"$key.$field", data)
      case None    => Left(Seq(requiredFieldFormError(key, field, rateBandDescription)))
    }

  override def unbind(key: String, value: VolumesByTaxType): Map[String, String] =
    taxTypeFormatter.unbind(s"$key.$taxTypeField", value.taxType) ++
      volumeFormatter.unbind(s"$key.$totalLitresField", value.totalLitres) ++
      pureAlcoholBigDecimalFormatter.unbind(s"$key.$pureAlcoholField", value.pureAlcohol)
}
