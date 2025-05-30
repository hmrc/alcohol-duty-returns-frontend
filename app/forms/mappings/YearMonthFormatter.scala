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

import play.api.data.FormError
import play.api.data.format.Formatter

import java.time.YearMonth

class YearMonthFormatter(
  invalidKey: String,
  allRequiredKey: String,
  requiredKey: String,
  invalidYear: String,
  args: Seq[String]
) extends Formatter[YearMonth]
    with Formatters {

  private val fieldKeys: List[String] = List("month", "year")

  private def verifyMonth(key: String, month: Int): Either[Seq[FormError], Int] =
    if (month >= 1 && month <= 12) Right(month) else Left(Seq(FormError(s"$key.month", s"$invalidKey.month", args)))

  private def verifyYear(key: String, year: Int): Either[Seq[FormError], Int] =
    if (year >= 1000 && year <= 9999) {
      Right(year)
    } else if ((year < 1000 && year >= 0) || year > 9999) {
      Left(Seq(FormError(s"$key.year", s"$invalidYear.year", args)))
    } else {
      Left(Seq(FormError(s"$key.year", s"$invalidKey.year", args)))
    }

  private val monthIntFormatter = intFormatter(
    requiredKey = s"$requiredKey.month",
    wholeNumberKey = s"$invalidKey.month",
    nonNumericKey = s"$invalidKey.month",
    args
  )

  private val yearIntFormatter = intFormatter(
    requiredKey = s"$requiredKey.year",
    wholeNumberKey = s"$invalidKey.year",
    nonNumericKey = s"$invalidKey.year",
    args
  )

  private def formatDate(key: String, data: Map[String, String]): Either[Seq[FormError], YearMonth] = {
    val month: Either[Seq[FormError], Int] = monthIntFormatter.bind(s"$key.month", data) match {
      case Right(value)   => verifyMonth(key, value)
      case Left(errorSeq) => Left(setErrorKey(s"$key.month", errorSeq))
    }

    val year = yearIntFormatter.bind(s"$key.year", data) match {
      case Right(value)   => verifyYear(key, value)
      case Left(errorSeq) => Left(setErrorKey(s"$key.year", errorSeq))
    }

    (month, year) match {
      case (Right(m), Right(y))    => Right(YearMonth.of(y, m))
      case (monthError, yearError) => Left(monthError.left.getOrElse(Seq.empty) ++ yearError.left.getOrElse(Seq.empty))
    }
  }

  private def setErrorKey(key: String, errors: Seq[FormError]): Seq[FormError] =
    errors.map(error => error.copy(key = key, args = args))

  override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], YearMonth] = {
    val fields = fieldKeys.map { field =>
      field -> data.get(s"$key.$field").filter(_.nonEmpty)
    }.toMap

    lazy val missingFields = fields
      .withFilter(_._2.isEmpty)
      .map(_._1)
      .toList

    fields.count(_._2.isDefined) match {
      case 2 =>
        formatDate(key, data)
      case 1 =>
        Left(missingFields.map(field => FormError(s"$key.$field", s"$requiredKey.$field", args)))
      case _ =>
        Left(List(FormError(key, allRequiredKey, args)))
    }
  }

  override def unbind(key: String, value: YearMonth): Map[String, String] =
    Map(
      s"$key.month" -> value.getMonthValue.toString,
      s"$key.year"  -> value.getYear.toString
    )
}
