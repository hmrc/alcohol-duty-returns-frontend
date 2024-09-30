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
import play.api.data.FormError
import play.api.data.format.Formatter

import scala.util.control.Exception.nonFatalCatch

class BigDecimalFieldFormatter(
  requiredKey: String,
  invalidKey: String,
  decimalPlacesKey: String,
  minimumValueKey: String,
  maximumValueKey: String,
  fieldKey: String,
  args: Seq[String] = Seq.empty,
  decimalPlaces: Int = Constants.maximumDecimalPlaces,
  maximumValue: BigDecimal = BigDecimal(999999999.99),
  minimumValue: BigDecimal = BigDecimal(0.01)
) extends Formatter[BigDecimal]
    with Formatters {

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
