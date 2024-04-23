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

import models.productEntry.AllRatesPage
import play.api.data.FormError
import play.api.data.format.Formatter

import java.time.YearMonth

class DoubleInputFormatter(invalidKey: String, allRequiredKey: String, requiredKey: String, args: Seq[String])
    extends Formatter[AllRatesPage]
    with Formatters {

  val fieldKeys: List[String] = List("month", "year")

  private val baseFormatter = stringFormatter(requiredKey, args)

  private def formatDate(key: String, data: Map[String, String]): Either[Seq[FormError], AllRatesPage] = {

    val month: Either[Seq[FormError], String] = baseFormatter.bind(s"$key.month", data) match {
      case Right(value)   => Right(value)
      case Left(errorSeq) => Left(setErrorKey(key, errorSeq))
    }

    val year = baseFormatter.bind(s"$key.year", data) match {
      case Right(value)   => Right(value)
      case Left(errorSeq) => Left(setErrorKey(key, errorSeq))
    }

    (month, year) match {
      case (Right(m), Right(y))    => Right(AllRatesPage(m, y))
      case (monthError, yearError) => Left(monthError.left.getOrElse(Seq.empty) ++ yearError.left.getOrElse(Seq.empty))
    }

  }

  def setErrorKey(key: String, errors: Seq[FormError]): Seq[FormError] =
    errors.map(error => error.copy(key = key, args = args))

  override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], AllRatesPage] = {
    val fields = fieldKeys.map { field =>
      field -> data.get(s"$key.$field").filter(_.nonEmpty)
    }.toMap

    lazy val missingFields = fields
      .withFilter(_._2.isEmpty)
      .map(_._1)
      .toList

    fields.count(_._2.isDefined) match {
      case 2 =>
        formatDate(key, data).left.map {
          _.map(_.copy(key = key, args = args))
        }
      case 1 =>
        Left(missingFields.map(field => FormError(s"$key.$field", requiredKey, missingFields ++ args)))
      case _ =>
        Left(List(FormError(key, allRequiredKey, args)))
    }
  }

  override def unbind(key: String, value: AllRatesPage): Map[String, String] =
    Map(
      s"$key.month" -> value.bulkVolume,
      s"$key.year"  -> value.pureAlcoholVolume
    )
}
