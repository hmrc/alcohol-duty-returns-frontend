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

import scala.util.control.Exception.nonFatalCatch

class VolumesFormatter(
  invalidKey: String,
  allRequiredKey: String,
  requiredKey: String,
  decimalPlacesKey: String,
  minimumValueKey: String,
  maximumValueKey: String,
  args: Seq[String]
) extends Formatter[VolumesByTaxType]
    with Formatters {

  val decimalPlaces: Int       = 2
  val minimumValue: BigDecimal = BigDecimal(0.01)
  val maximumValue: BigDecimal = BigDecimal(999999999.99)

  val fieldKeys: List[String] = List("taxType", "totalLitres", "pureAlcohol")

  private def formatVolume(key: String, data: Map[String, String]): Either[Seq[FormError], VolumesByTaxType] = {

    val taxType     = stringFormatter(s"$requiredKey.taxType").bind(s"$key.taxType", data)
    val totalLitres = volumeFormatter("totalLitres").bind(s"$key.totalLitres", data)
    val pureAlcohol = volumeFormatter("pureAlcohol").bind(s"$key.pureAlcohol", data)

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
      case 3     => formatVolume(key, data)
      case 2 | 1 =>
        Left(
          missingFields.map { field =>
            FormError(s"${nameToId(key)}.$field", s"$requiredKey.$field", missingFields ++ args)
          }
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

  def volumeFormatter(
    fieldKey: String,
    args: Seq[String] = Seq.empty
  ): Formatter[BigDecimal] =
    new Formatter[BigDecimal] {
      val decimalRegexp = s"""^[+-]?[0-9]*(\\.[0-9]{0,$decimalPlaces})?$$"""

      private val baseFormatter = stringFormatter(s"$requiredKey.$fieldKey", args)

      override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], BigDecimal] =
        baseFormatter
          .bind(key, data)
          .map(_.replace(",", ""))
          .flatMap { s =>
            nonFatalCatch
              .either(BigDecimal(s))
              .left
              .map(_ => Seq(FormError(nameToId(key), s"$invalidKey.$fieldKey", args)))
              .flatMap {
                case res if res < minimumValue                    =>
                  Left(Seq(FormError(nameToId(key), s"$minimumValueKey.$fieldKey", args)))
                case res if res > maximumValue                    =>
                  Left(Seq(FormError(nameToId(key), s"$maximumValueKey.$fieldKey", args)))
                case res if res.toString().matches(decimalRegexp) =>
                  Right(res)
                case _                                            =>
                  Left(Seq(FormError(nameToId(key), s"$decimalPlacesKey.$fieldKey", args)))
              }
          }

      override def unbind(key: String, value: BigDecimal): Map[String, String] =
        baseFormatter.unbind(key, value.toString)
    }
}
