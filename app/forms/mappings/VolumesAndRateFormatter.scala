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
import models.declareDuty.VolumeAndRateByTaxType
import play.api.data.FormError
import play.api.data.format.Formatter

class VolumesAndRateFormatter(
  invalidKey: String,
  requiredKey: String,
  decimalPlacesKey: String,
  minimumValueKey: String,
  maximumValueKey: String,
  lessOrEqualKey: String,
  regimeName: String,
  regimeNameSoftMutation: String
) extends Formatter[VolumeAndRateByTaxType]
    with Formatters {

  private val taxTypeFormatter = stringFormatter(s"$requiredKey.$taxTypeField")

  private val volumeFormatter = new BigDecimalFieldFormatterWithRatebandRecap(
    requiredKey,
    invalidKey,
    decimalPlacesKey,
    minimumValueKey,
    maximumValueKey,
    totalLitresField,
    maximumValue = Constants.volumeMaximumValue,
    minimumValue = Constants.volumeMinimumValue,
    args = Seq(regimeName, regimeNameSoftMutation)
  )

  private val pureAlcoholVolumeFormatter: BigDecimalFieldFormatter = new BigDecimalFieldFormatterWithRatebandRecap(
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
    args = Seq(regimeName, regimeNameSoftMutation)
  )

  private val dutyRateFormatter = new BigDecimalFieldFormatterWithRatebandRecap(
    requiredKey,
    invalidKey,
    decimalPlacesKey,
    minimumValueKey,
    maximumValueKey,
    dutyRateField,
    maximumValue = Constants.dutyMaximumValue,
    minimumValue = Constants.dutyMinimumValue,
    args = Seq(regimeName, regimeNameSoftMutation)
  )

  private def requiredFieldFormError(key: String, field: String, allArgs: Seq[String]): FormError =
    FormError(nameToId(s"$key.$field"), s"$requiredKey.$field", allArgs)

  private def formatVolume(
    key: String,
    data: Map[String, String]
  ): Either[Seq[FormError], VolumeAndRateByTaxType] = {
    val taxType     = taxTypeFormatter.bind(s"$key.$taxTypeField", data)
    val totalLitres = volumeFormatter.bind(s"$key.$totalLitresField", data)
    val pureAlcohol = pureAlcoholVolumeFormatter.bind(s"$key.$pureAlcoholField", data)
    val dutyRate    = dutyRateFormatter.bind(s"$key.$dutyRateField", data)

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

  private def checkValues(
    key: String,
    data: Map[String, String],
    allArgs: Seq[String]
  ): Either[Seq[FormError], VolumeAndRateByTaxType] =
    formatVolume(key, data).fold(
      errors => Left(errors),
      dutyByTaxType =>
        if (dutyByTaxType.totalLitres < dutyByTaxType.pureAlcohol) {
          Left(
            Seq(
              FormError(nameToId(s"$key.$pureAlcoholField"), lessOrEqualKey, allArgs)
            )
          )
        } else {
          Right(dutyByTaxType)
        }
    )

  override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], VolumeAndRateByTaxType] = {
    // The orElse fallbacks are to support MultipleSPR for which we don't know the rateBand on view creation
    val rateBandRecap             = data
      .getOrElse(
        s"$key.$rateBandRecapField",
        regimeName
      )
    val rateBandRecapSoftMutation = data
      .getOrElse(
        s"$key.$rateBandRecapSoftMutationField",
        regimeNameSoftMutation
      )

    val allArgs = Seq(rateBandRecap, rateBandRecapSoftMutation, regimeName, regimeNameSoftMutation)

    val taxTypeResult     = validateField(taxTypeField, key, data, taxTypeFormatter, allArgs)
    val totalLitresResult = validateField(totalLitresField, key, data, volumeFormatter, allArgs)
    val pureAlcoholResult = validateField(pureAlcoholField, key, data, pureAlcoholVolumeFormatter, allArgs)
    val dutyRateResult    = validateField(dutyRateField, key, data, dutyRateFormatter, allArgs)
    val allErrors         =
      taxTypeResult.left.toSeq.flatten ++ totalLitresResult.left.toSeq.flatten ++ pureAlcoholResult.left.toSeq.flatten ++ dutyRateResult.left.toSeq.flatten
    if (allErrors.nonEmpty) {
      Left(allErrors)
    } else {
      checkValues(key, data, allArgs)
    }
  }

  private def validateField[T](
    field: String,
    key: String,
    data: Map[String, String],
    formatter: Formatter[T],
    allArgs: Seq[String]
  ): Either[Seq[FormError], T] =
    data.get(s"$key.$field").filter(_.nonEmpty) match {
      case Some(_) => formatter.bind(s"$key.$field", data)
      case None    => Left(Seq(requiredFieldFormError(key, field, allArgs)))
    }

  override def unbind(key: String, value: VolumeAndRateByTaxType): Map[String, String] =
    taxTypeFormatter.unbind(s"$key.$taxTypeField", value.taxType) ++
      volumeFormatter.unbind(s"$key.$totalLitresField", value.totalLitres) ++
      pureAlcoholVolumeFormatter.unbind(s"$key.$pureAlcoholField", value.pureAlcohol) ++
      dutyRateFormatter.unbind(s"$key.$dutyRateField", value.dutyRate)
}
