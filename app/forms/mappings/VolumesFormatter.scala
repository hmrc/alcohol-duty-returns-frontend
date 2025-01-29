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

import config.Constants.MappingFields._
import models.declareDuty.VolumesByTaxType
import play.api.data.FormError
import play.api.data.format.Formatter

class VolumesFormatter(
  lessOrEqualKey: String,
  taxTypeFormatter: Formatter[String],
  volumeFormatter: BigDecimalFieldFormatter,
  pureAlcoholVolumeFormatter: BigDecimalFieldFormatter,
  args: Seq[String]
) extends Formatter[VolumesByTaxType]
    with Formatters {

  private def bindFormatters(
    key: String,
    data: Map[String, String]
  ): Either[Seq[FormError], VolumesByTaxType] = {
    val taxTypeEither     = bindField(key, taxTypeField, taxTypeFormatter, data)
    val totalLitresEither = bindField(key, totalLitresField, volumeFormatter, data)
    val pureAlcoholEither = bindField(key, pureAlcoholField, pureAlcoholVolumeFormatter, data)
    (taxTypeEither, totalLitresEither, pureAlcoholEither) match {
      case (Right(taxType), Right(totalLitres), Right(pureAlcohol)) =>
        Right(VolumesByTaxType(taxType, totalLitres, pureAlcohol))
      case _                                                        =>
        Left(Seq(taxTypeEither, totalLitresEither, pureAlcoholEither).collect { case Left(error) =>
          error
        }.flatten)
    }
  }

  private def checkPureAlcoholIsNotGreaterThanTotalLitres(
    key: String,
    volumeAndRateByTaxType: VolumesByTaxType
  ): Either[Seq[FormError], VolumesByTaxType] =
    if (volumeAndRateByTaxType.totalLitres < volumeAndRateByTaxType.pureAlcohol) {
      Left(
        Seq(
          FormError(nameToId(s"$key.$pureAlcoholField"), lessOrEqualKey, args)
        )
      )
    } else {
      Right(
        VolumesByTaxType(
          volumeAndRateByTaxType.taxType,
          volumeAndRateByTaxType.totalLitres,
          volumeAndRateByTaxType.pureAlcohol
        )
      )
    }

  override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], VolumesByTaxType] =
    for {
      volumesByTaxType <- bindFormatters(key, data)
      _                <- checkPureAlcoholIsNotGreaterThanTotalLitres(key, volumesByTaxType)
    } yield volumesByTaxType

  override def unbind(key: String, value: VolumesByTaxType): Map[String, String] =
    taxTypeFormatter.unbind(s"$key.$taxTypeField", value.taxType) ++
      volumeFormatter.unbind(s"$key.$totalLitresField", value.totalLitres) ++
      pureAlcoholVolumeFormatter.unbind(s"$key.$pureAlcoholField", value.pureAlcohol)
}
