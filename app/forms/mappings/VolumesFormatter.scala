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

import models.returns.VolumesByTaxType
import play.api.data.FormError
import play.api.data.format.Formatter

class VolumesFormatter(
  invalidKey: String,
  allRequiredKey: String,
  requiredKey: String,
  decimalPlacesKey: String,
  args: Seq[String]
) extends Formatter[VolumesByTaxType]
    with Formatters {

  val fieldKeys: List[String] = List("taxType", "totalLitres", "pureAlcohol")

  private val totalLitresBigDecimalFormatter = bigDecimalFormatter(
    decimalPlaces = 2,
    requiredKey = s"$requiredKey.totalLitres",
    nonNumericKey = s"$invalidKey.totalLitres",
    decimalPlacesKey = s"$decimalPlacesKey.totalLitres",
    args
  )

  private val pureAlcoholBigDecimalFormatter = bigDecimalFormatter(
    decimalPlaces = 2,
    requiredKey = s"$requiredKey.pureAlcohol",
    nonNumericKey = s"$invalidKey.pureAlcohol",
    decimalPlacesKey = s"$decimalPlacesKey.pureAlcohol",
    args
  )

  private val taxTypeStringFormatter = stringFormatter(
    errorKey = s"$requiredKey.taxType",
    args
  )

  private def formatVolume(key: String, data: Map[String, String]): Either[Seq[FormError], VolumesByTaxType] = {

    val taxType                                         = taxTypeStringFormatter.bind(s"$key.taxType", data)
    val totalLitres: Either[Seq[FormError], BigDecimal] = totalLitresBigDecimalFormatter.bind(s"$key.totalLitres", data)
    val pureAlcohol                                     = pureAlcoholBigDecimalFormatter.bind(s"$key.pureAlcohol", data)

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

  override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], VolumesByTaxType] = {

    val fields = fieldKeys.map { field =>
      field -> data.get(s"$key.$field").filter(_.nonEmpty)
    }.toMap

    lazy val missingFields = fields
      .withFilter(_._2.isEmpty)
      .map(_._1)
      .toList

    fields.count(_._2.isDefined) match {
      case 3     =>
        formatVolume(key, data).left.map {
          _.map(_.copy(key = key, args = args))
        }
      case 2 | 1 =>
        Left(
          missingFields.map(field =>
            FormError(s"${nameToId(key)}.$field", s"$requiredKey.$field", missingFields ++ args)
          )
        )
      case _     =>
        Left(List(FormError(key, allRequiredKey, args)))
    }
  }

  private def nameToId(name: String): String = name.replace("[", "_").replace("]", "")

  override def unbind(key: String, value: VolumesByTaxType): Map[String, String] =
    Map(
      s"$key.taxType"     -> value.taxType,
      s"$key.totalLitres" -> value.totalLitres.toString,
      s"$key.pureAlcohol" -> value.pureAlcohol.toString
    )

}
