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

class YearMonthFormatter(invalidKey: String, allRequiredKey: String, requiredKey: String, args: Seq[String])
    extends Formatter[YearMonth]
    with Formatters {

  val fieldKeys: List[String] = List("month", "year")

  def verifyMonth(key: String, month: Int): Either[Seq[FormError], Int] =
    if (month >= 1 && month <= 12) Right(month) else Left(Seq(FormError(s"$key.month", s"$invalidKey.month", args)))
  def verifyYear(key: String, year: Int): Either[Seq[FormError], Int]   =
    if (year >= 1000 && year <= 9999) Right(year) else Left(Seq(FormError(s"$key.year", s"$invalidKey.year", args)))

  val monthIntFormatter = intFormatter(
    requiredKey = s"$requiredKey.month",
    wholeNumberKey = s"$invalidKey.month",
    nonNumericKey = s"$invalidKey.month",
    args
  )

  val yearIntFormatter = intFormatter(
    requiredKey = s"$requiredKey.year",
    wholeNumberKey = s"$invalidKey.year",
    nonNumericKey = s"$invalidKey.year",
    args
  )

  private def formatDate(key: String, data: Map[String, String]): Either[Seq[FormError], YearMonth] = {

    val month: Either[Seq[FormError], Int] = monthIntFormatter.bind(s"$key.month", data) match {
      case Right(value)   => verifyMonth(key, value)
      case Left(errorSeq) => Left(errorSeq)
    }

    val year = yearIntFormatter.bind(s"$key.year", data) match {
      case Right(value)   => verifyYear(key, value)
      case Left(errorSeq) => Left(errorSeq)
    }

    (month, year) match {
      case (Right(m), Right(y))    => Right(YearMonth.of(y, m))
      case (monthError, yearError) => Left(monthError.left.getOrElse(Seq.empty) ++ yearError.left.getOrElse(Seq.empty))
    }
  }

  override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], YearMonth] = {
    val fields = fieldKeys.map { field =>
      field -> data.get(s"$key.$field").filter(_.nonEmpty)
    }.toMap

    if (fields.count(_._2.isDefined) > 0) {
      formatDate(key, data)
    } else {
      Left(List(FormError(key, allRequiredKey, args)))
    }
  }

  override def unbind(key: String, value: YearMonth): Map[String, String] =
    Map(
      s"$key.month" -> value.getMonthValue.toString,
      s"$key.year"  -> value.getYear.toString
    )
}
