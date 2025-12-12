/*
 * Copyright 2023 HM Revenue & Customs
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

package forms.behaviours

import java.time.{LocalDate, YearMonth}
import java.time.format.DateTimeFormatter
import org.scalacheck.Gen
import play.api.data.{Form, FormError}

class DateBehaviours extends FieldBehaviours {

  def dateField(form: Form[_], key: String, validData: Gen[LocalDate]): Unit =
    "bind valid data" in
      forAll(validData -> "valid date") { date =>
        val data = Map(
          s"$key.day"   -> date.getDayOfMonth.toString,
          s"$key.month" -> date.getMonthValue.toString,
          s"$key.year"  -> date.getYear.toString
        )

        val result = form.bind(data)

        result.value.value mustEqual date
        result.errors         mustBe empty
      }

  def dateFieldWithMax(form: Form[_], key: String, max: LocalDate, formError: FormError): Unit =
    s"fail to bind a date greater than ${max.format(DateTimeFormatter.ISO_LOCAL_DATE)}" in {

      val generator = datesBetween(max.plusDays(1), max.plusYears(10))

      forAll(generator -> "invalid dates") { date =>
        val data = Map(
          s"$key.day"   -> date.getDayOfMonth.toString,
          s"$key.month" -> date.getMonthValue.toString,
          s"$key.year"  -> date.getYear.toString
        )

        val result = form.bind(data)

        result.errors must contain only formError
      }
    }

  def dateFieldWithMin(form: Form[_], key: String, min: LocalDate, formError: FormError): Unit =
    s"fail to bind a date earlier than ${min.format(DateTimeFormatter.ISO_LOCAL_DATE)}" in {

      val generator = datesBetween(min.minusYears(10), min.minusDays(1))

      forAll(generator -> "invalid dates") { date =>
        val data = Map(
          s"$key.day"   -> date.getDayOfMonth.toString,
          s"$key.month" -> date.getMonthValue.toString,
          s"$key.year"  -> date.getYear.toString
        )

        val result = form.bind(data)

        result.errors must contain only formError
      }
    }

  def mandatoryDateField(form: Form[_], key: String, requiredAllKey: String, errorArgs: Seq[String] = Seq.empty): Unit =
    "fail to bind an empty date" in {

      val result = form.bind(Map.empty[String, String])

      result.errors must contain only FormError(key, requiredAllKey, errorArgs)
    }

  def yearMonthMandatoryMonthField(form: Form[_], key: String, formError: FormError): Unit =
    s"fail to bind an empty month" in {

      val data = Map(
        s"$key.month" -> "",
        s"$key.year"  -> "2024"
      )

      val result = form.bind(data)

      result.errors must contain only formError
    }

  def yearMonthMandatoryYearField(form: Form[_], key: String, formError: FormError): Unit =
    s"fail to bind an empty year" in {

      val data = Map(
        s"$key.month" -> "1",
        s"$key.year"  -> ""
      )

      val result = form.bind(data)

      result.errors must contain only formError
    }

  def yearMonthField(form: Form[_], key: String, validData: Gen[YearMonth]): Unit =
    "bind valid data" in
      forAll(validData -> "valid date") { date =>
        val data = Map(
          s"$key.month" -> date.getMonthValue.toString,
          s"$key.year"  -> date.getYear.toString
        )

        val result = form.bind(data)

        result(key).errors mustBe empty
      }

  def yearMonthFieldInFuture(form: Form[_], key: String, returnPeriod: YearMonth, formError: FormError): Unit =
    "fail to bind a date in the future" in {

      val futureDate = returnPeriod.plusMonths(1)

      val data = Map(
        s"$key.month" -> futureDate.getMonthValue.toString,
        s"$key.year"  -> futureDate.getYear.toString
      )

      val result = form.bind(data)

      result.errors must contain only formError
    }

  def yearMonthFieldForCurrentPeriod(form: Form[_], key: String, returnPeriod: YearMonth, formError: FormError): Unit =
    "fail to bind a date in the future" in {

      val futureDate = returnPeriod

      val data = Map(
        s"$key.month" -> futureDate.getMonthValue.toString,
        s"$key.year"  -> futureDate.getYear.toString
      )

      val result = form.bind(data)

      result.errors must contain only formError
    }

  def yearMonthFieldWithMin(form: Form[_], key: String, minYearMonth: YearMonth, formError: FormError): Unit =
    "fail to bind a date earlier than the minimum" in {

      val data = Map(
        s"$key.month" -> minYearMonth.getMonthValue.toString,
        s"$key.year"  -> minYearMonth.getYear.toString
      )

      val result = form.bind(data)

      result.errors must contain only formError
    }

  def yearMonthWithMonthOutOfMinRange(
    form: Form[_],
    key: String,
    validData: Gen[YearMonth],
    formError: FormError
  ): Unit =
    "fail to bind a date with a month value below 1" in
      forAll(validData -> "valid date") { date =>
        val data = Map(
          s"$key.month" -> "0",
          s"$key.year"  -> date.getYear.toString
        )

        val result = form.bind(data)

        result.errors must contain only formError
      }

  def yearMonthWithMonthOutOfMaxRange(
    form: Form[_],
    key: String,
    validData: Gen[YearMonth],
    formError: FormError
  ): Unit =
    "fail to bind a date with a month value greater than 12" in
      forAll(validData -> "valid date") { date =>
        val data = Map(
          s"$key.month" -> "13",
          s"$key.year"  -> date.getYear.toString
        )

        val result = form.bind(data)

        result.errors must contain only formError
      }

  def yearMonthInvalidMonth(form: Form[_], key: String, validData: Gen[YearMonth], formError: FormError): Unit =
    "fail to bind a date with an invalid month" in
      forAll(validData -> "valid date") { date =>
        val data = Map(
          s"$key.month" -> "aaa",
          s"$key.year"  -> date.getYear.toString
        )

        val result = form.bind(data)

        result.errors must contain only formError
      }

  def yearMonthInvalidYear(form: Form[_], key: String, validData: Gen[YearMonth], formError: FormError): Unit =
    "fail to bind a date with an invalid year" in
      forAll(validData -> "valid date") { date =>
        val data = Map(
          s"$key.month" -> date.getMonthValue.toString,
          s"$key.year"  -> "abcd"
        )

        val result = form.bind(data)

        result.errors must contain only formError
      }

  def yearMonthWithYearOutOfMinRange(
    form: Form[_],
    key: String,
    validData: Gen[YearMonth],
    formError: FormError
  ): Unit =
    "fail to bind a date with a year value with less than 4 digits" in
      forAll(validData -> "valid date") { date =>
        val data = Map(
          s"$key.month" -> date.getMonthValue.toString,
          s"$key.year"  -> "999"
        )

        val result = form.bind(data)

        result.errors must contain only formError
      }

  def yearMonthWithYearOutOfMaxRange(
    form: Form[_],
    key: String,
    validData: Gen[YearMonth],
    formError: FormError
  ): Unit =
    "fail to bind a date with a year value with more than 4 digits" in
      forAll(validData -> "valid date") { date =>
        val data = Map(
          s"$key.month" -> date.getMonthValue.toString,
          s"$key.year"  -> "10000"
        )

        val result = form.bind(data)

        result.errors must contain only formError
      }
}
