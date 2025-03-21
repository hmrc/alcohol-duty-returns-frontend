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

package forms.mappings

import base.SpecBase
import play.api.data.{Form, FormError}
import models.Enumerable
import models.adjustment.{AdjustmentVolume, AdjustmentVolumeWithSPR, SpoiltVolumeWithDuty}
import models.declareDuty.{VolumeAndRateByTaxType, VolumesByTaxType}

import java.time.YearMonth

object MappingsSpec {

  sealed trait Foo
  case object Bar extends Foo
  case object Baz extends Foo

  object Foo {

    val values: Set[Foo] = Set(Bar, Baz)

    implicit val fooEnumerable: Enumerable[Foo] =
      Enumerable(values.toSeq.map(v => v.toString -> v): _*)
  }
}

class MappingsSpec extends SpecBase with Mappings {

  val regimeName = "beer"

  import MappingsSpec._

  "text" - {

    val testForm: Form[String] =
      Form(
        "value" -> text()
      )

    "must bind a valid string" in {
      val result = testForm.bind(Map("value" -> "foobar"))
      result.get mustEqual "foobar"
    }

    "must not bind an empty string" in {
      val result = testForm.bind(Map("value" -> ""))
      result.errors must contain(FormError("value", "error.required"))
    }

    "must not bind a string of whitespace only" in {
      val result = testForm.bind(Map("value" -> " \t"))
      result.errors must contain(FormError("value", "error.required"))
    }

    "must not bind an empty map" in {
      val result = testForm.bind(Map.empty[String, String])
      result.errors must contain(FormError("value", "error.required"))
    }

    "must return a custom error message" in {
      val form   = Form("value" -> text("custom.error"))
      val result = form.bind(Map("value" -> ""))
      result.errors must contain(FormError("value", "custom.error"))
    }

    "must unbind a valid value" in {
      val result = testForm.fill("foobar")
      result.apply("value").value.value mustEqual "foobar"
    }
  }

  "boolean" - {

    val testForm: Form[Boolean] =
      Form(
        "value" -> boolean()
      )

    "must bind true" in {
      val result = testForm.bind(Map("value" -> "true"))
      result.get mustEqual true
    }

    "must bind false" in {
      val result = testForm.bind(Map("value" -> "false"))
      result.get mustEqual false
    }

    "must not bind a non-boolean" in {
      val result = testForm.bind(Map("value" -> "not a boolean"))
      result.errors must contain(FormError("value", "error.boolean"))
    }

    "must not bind an empty value" in {
      val result = testForm.bind(Map("value" -> ""))
      result.errors must contain(FormError("value", "error.required"))
    }

    "must not bind an empty map" in {
      val result = testForm.bind(Map.empty[String, String])
      result.errors must contain(FormError("value", "error.required"))
    }

    "must unbind" in {
      val result = testForm.fill(true)
      result.apply("value").value.value mustEqual "true"
    }
  }

  "int" - {

    val testForm: Form[Int] =
      Form(
        "value" -> int()
      )

    "must bind a valid integer" in {
      val result = testForm.bind(Map("value" -> "1"))
      result.get mustEqual 1
    }

    "must not bind an empty value" in {
      val result = testForm.bind(Map("value" -> ""))
      result.errors must contain(FormError("value", "error.required"))
    }

    "must not bind an empty map" in {
      val result = testForm.bind(Map.empty[String, String])
      result.errors must contain(FormError("value", "error.required"))
    }

    "must unbind a valid value" in {
      val result = testForm.fill(123)
      result.apply("value").value.value mustEqual "123"
    }
  }

  "enumerable" - {

    val testForm = Form(
      "value" -> enumerable[Foo]()
    )

    "must bind a valid option" in {
      val result = testForm.bind(Map("value" -> "Bar"))
      result.get mustEqual Bar
    }

    "must not bind an invalid option" in {
      val result = testForm.bind(Map("value" -> "Not Bar"))
      result.errors must contain(FormError("value", "error.invalid"))
    }

    "must not bind an empty map" in {
      val result = testForm.bind(Map.empty[String, String])
      result.errors must contain(FormError("value", "error.required"))
    }
  }

  "bigDecimal" - {

    val testForm: Form[BigDecimal] =
      Form(
        "value" -> bigDecimal()
      )

    "must bind a valid bigDecimal" - {
      "without decimal points" in {
        val result = testForm.bind(Map("value" -> "1"))
        result.get mustEqual BigDecimal(1)
      }
      "with decimal points" in {
        val result = testForm.bind(Map("value" -> "1.1"))
        result.get mustEqual BigDecimal(1.1)
      }
      "with commas" in {
        val result = testForm.bind(Map("value" -> "1,001.1"))
        result.get mustEqual BigDecimal(1001.1)
      }
      "with minus" in {
        val result = testForm.bind(Map("value" -> "-102.12"))
        result.get mustEqual BigDecimal(-102.12)
      }
    }

    "must not bind an empty value" in {
      val result = testForm.bind(Map("value" -> ""))
      result.errors must contain(FormError("value", "error.required"))
    }

    "must not bind a non numeric value" in {
      val result = testForm.bind(Map("value" -> "abc"))
      result.errors must contain(FormError("value", "error.nonNumeric"))
    }

    "must not bind a non numeric value with multiple dots" in {
      val result = testForm.bind(Map("value" -> "10.12.1"))
      result.errors must contain(FormError("value", "error.nonNumeric"))
    }

    "must not bind a non numeric value with 3 decimal digits" in {
      val result = testForm.bind(Map("value" -> "1.349"))
      result.errors must contain(FormError("value", "error.decimalPlaces"))
    }

  }

  "yearMonth" - {
    val mapping =
      yearMonth("invalid", "allRequired", "error.required", "invalidYear")

    val testForm: Form[YearMonth] =
      Form("value" -> mapping)

    "must bind a valid yearMonth" in {
      val result = testForm.bind(Map("value.month" -> "1", "value.year" -> "2024"))
      result.get mustEqual YearMonth.of(2024, 1)
    }

    "must not bind an empty" - {
      "month" in {
        val result = testForm.bind(Map("value.month" -> "", "value.year" -> "2024"))
        result.errors mustBe Seq(FormError("value.month", "error.required", Seq("month")))
      }

      "year" in {
        val result = testForm.bind(Map("value.month" -> "1", "value.year" -> ""))
        result.errors mustBe Seq(FormError("value.year", "error.required", Seq("year")))
      }

      "month and year" in {
        val result = testForm.bind(Map("value.month" -> "", "value.year" -> ""))
        result.errors mustBe Seq(FormError("value", "allRequired", Seq()))
      }
    }

    "must not bind a non-numeric value" - {
      "of month" in {
        val result = testForm.bind(Map("value.month" -> "a", "value.year" -> "2024"))
        result.errors mustBe Seq(FormError("value.month", "invalid.month", Seq()))
      }

      "of year" in {
        val result = testForm.bind(Map("value.month" -> "1", "value.year" -> "a"))
        result.errors mustBe Seq(FormError("value.year", "invalid.year", Seq()))
      }
    }

    "must not bind" - {
      "a month value too small" in {
        val result = testForm.bind(Map("value.month" -> "0", "value.year" -> "2024"))
        result.errors mustBe Seq(FormError("value.month", "invalid.month", Seq()))
      }

      "a month value too large" in {
        val result = testForm.bind(Map("value.month" -> "13", "value.year" -> "2024"))
        result.errors mustBe Seq(FormError("value.month", "invalid.month", Seq()))
      }

      "a year value too small" in {
        val result = testForm.bind(Map("value.month" -> "1", "value.year" -> "-1"))
        result.errors mustBe Seq(FormError("value.year", "invalid.year", Seq()))
      }

      "a year value not of four digits, when 0" in {
        val result = testForm.bind(Map("value.month" -> "1", "value.year" -> "0"))
        result.errors mustBe Seq(FormError("value.year", "invalidYear.year", Seq()))
      }

      "a year value not of four digits, when 999" in {
        val result = testForm.bind(Map("value.month" -> "1", "value.year" -> "999"))
        result.errors mustBe Seq(FormError("value.year", "invalidYear.year", Seq()))
      }

      "a year value of five digits" in {
        val result = testForm.bind(Map("value.month" -> "1", "value.year" -> "99999"))
        result.errors mustBe Seq(FormError("value.year", "invalidYear.year", Seq()))
      }
    }

    "can unbind" in {
      val result = mapping.unbind(YearMonth.of(2024, 1))
      result.get(".month") mustBe Some("1")
      result.get(".year")  mustBe Some("2024")
    }
  }

  "adjustmentVolumesWithRate" - {
    val mapping = adjustmentVolumesWithRate(
      "invalid",
      "required",
      "decimalPlaces",
      "minimumValue",
      "maximumValue",
      "inconsistent",
      Seq.empty
    )

    val testForm: Form[AdjustmentVolumeWithSPR] = Form("value" -> mapping)

    "must bind a valid AdjustmentVolumeWithSPR" in {
      val result = testForm.bind(
        Map(
          "value.totalLitresVolume" -> "1234.45",
          "value.pureAlcoholVolume" -> "12.3456",
          "value.sprDutyRate"       -> "5.31"
        )
      )
      result.get mustBe AdjustmentVolumeWithSPR(BigDecimal(1234.45), BigDecimal(12.3456), BigDecimal(5.31))
    }

    "must not bind when missing" - {
      "totalLitresVolume" in {
        val result = testForm.bind(
          Map("value.pureAlcoholVolume" -> "12.3456", "value.sprDutyRate" -> "5.31")
        )
        result.errors mustBe Seq(FormError("value_totalLitresVolume", "required.totalLitresVolume", Seq.empty))
      }

      "pureAlcoholVolume" in {
        val result = testForm.bind(
          Map("value.totalLitresVolume" -> "1234.45", "value.sprDutyRate" -> "5.31")
        )
        result.errors mustBe Seq(FormError("value_pureAlcoholVolume", "required.pureAlcoholVolume", Seq.empty))
      }

      "sprDutyRate" in {
        val result = testForm.bind(
          Map("value.totalLitresVolume" -> "1234.45", "value.pureAlcoholVolume" -> "12.3456")
        )
        result.errors mustBe Seq(FormError("value_sprDutyRate", "required.sprDutyRate", Seq.empty))
      }
    }

    "must not bind an empty" - {
      "totalLitresVolume" in {
        val result = testForm.bind(
          Map("value.totalLitresVolume" -> "", "value.pureAlcoholVolume" -> "12.3456", "value.sprDutyRate" -> "5.31")
        )
        result.errors mustBe Seq(FormError("value_totalLitresVolume", "required.totalLitresVolume", Seq.empty))
      }

      "pureAlcoholVolume" in {
        val result = testForm.bind(
          Map("value.totalLitresVolume" -> "1234.45", "value.pureAlcoholVolume" -> "", "value.sprDutyRate" -> "5.31")
        )
        result.errors mustBe Seq(FormError("value_pureAlcoholVolume", "required.pureAlcoholVolume", Seq.empty))
      }

      "sprDutyRate" in {
        val result = testForm.bind(
          Map("value.totalLitresVolume" -> "1234.45", "value.pureAlcoholVolume" -> "12.3456", "value.sprDutyRate" -> "")
        )
        result.errors mustBe Seq(FormError("value_sprDutyRate", "required.sprDutyRate", Seq.empty))
      }
    }

    "must not bind non-numeric value" - {
      "totalLitresVolume" in {
        val result = testForm.bind(
          Map("value.totalLitresVolume" -> "a", "value.pureAlcoholVolume" -> "12.3456", "value.sprDutyRate" -> "5.31")
        )
        result.errors mustBe Seq(FormError("value_totalLitresVolume", "invalid.totalLitresVolume", Seq.empty))
      }

      "pureAlcoholVolume" in {
        val result = testForm.bind(
          Map("value.totalLitresVolume" -> "1234.45", "value.pureAlcoholVolume" -> "a", "value.sprDutyRate" -> "5.31")
        )
        result.errors mustBe Seq(FormError("value_pureAlcoholVolume", "invalid.pureAlcoholVolume", Seq.empty))
      }

      "sprDutyRate" in {
        val result = testForm.bind(
          Map(
            "value.totalLitresVolume" -> "1234.45",
            "value.pureAlcoholVolume" -> "12.3456",
            "value.sprDutyRate"       -> "a"
          )
        )
        result.errors mustBe Seq(FormError("value_sprDutyRate", "invalid.sprDutyRate", Seq.empty))
      }
    }

    "must not bind values with too many decimal places" - {
      "totalLitresVolume" in {
        val result = testForm.bind(
          Map(
            "value.totalLitresVolume" -> "1234.456",
            "value.pureAlcoholVolume" -> "12.3456",
            "value.sprDutyRate"       -> "5.31"
          )
        )
        result.errors mustBe Seq(FormError("value_totalLitresVolume", "decimalPlaces.totalLitresVolume", Seq.empty))
      }

      "pureAlcoholVolume" in {
        val result = testForm.bind(
          Map(
            "value.totalLitresVolume" -> "1234.45",
            "value.pureAlcoholVolume" -> "12.34567",
            "value.sprDutyRate"       -> "5.31"
          )
        )
        result.errors mustBe Seq(FormError("value_pureAlcoholVolume", "decimalPlaces.pureAlcoholVolume", Seq.empty))
      }

      "sprDutyRate" in {
        val result = testForm.bind(
          Map(
            "value.totalLitresVolume" -> "1234.45",
            "value.pureAlcoholVolume" -> "12.3456",
            "value.sprDutyRate"       -> "5.312"
          )
        )
        result.errors mustBe Seq(FormError("value_sprDutyRate", "decimalPlaces.sprDutyRate", Seq.empty))
      }
    }

    "must not bind pureAlcoholVolume values" - {
      "with less than 4 decimal places" in {
        val result = testForm.bind(
          Map(
            "value.totalLitresVolume" -> "1234.45",
            "value.pureAlcoholVolume" -> "12.345",
            "value.sprDutyRate"       -> "5.31"
          )
        )
        result.errors mustBe Seq(FormError("value_pureAlcoholVolume", "decimalPlaces.pureAlcoholVolume", Seq.empty))
      }

      "with no decimal places and trailing decimal point" in {
        val result = testForm.bind(
          Map(
            "value.totalLitresVolume" -> "1234.45",
            "value.pureAlcoholVolume" -> "12.",
            "value.sprDutyRate"       -> "5.31"
          )
        )
        result.errors mustBe Seq(FormError("value_pureAlcoholVolume", "decimalPlaces.pureAlcoholVolume", Seq.empty))
      }

      "with no decimal places" in {
        val result = testForm.bind(
          Map(
            "value.totalLitresVolume" -> "1234.45",
            "value.pureAlcoholVolume" -> "12",
            "value.sprDutyRate"       -> "5.31"
          )
        )
        result.errors mustBe Seq(FormError("value_pureAlcoholVolume", "decimalPlaces.pureAlcoholVolume", Seq.empty))
      }
    }

    "must bind the smallest values" - {
      "totalLitresVolume" in {
        val result = testForm.bind(
          Map("value.totalLitresVolume" -> "0.01", "value.pureAlcoholVolume" -> "0.0001", "value.sprDutyRate" -> "0")
        )
        result.errors mustBe Seq.empty
      }
    }

    "must not bind values that are too small" - {
      "totalLitresVolume" in {
        val result = testForm.bind(
          Map("value.totalLitresVolume" -> "0", "value.pureAlcoholVolume" -> "0.0001", "value.sprDutyRate" -> "0")
        )
        result.errors mustBe Seq(FormError("value_totalLitresVolume", "minimumValue.totalLitresVolume", Seq.empty))
      }

      "pureAlcoholVolume" in {
        val result = testForm.bind(
          Map("value.totalLitresVolume" -> "0.01", "value.pureAlcoholVolume" -> "0.0000", "value.sprDutyRate" -> "0")
        )
        result.errors mustBe Seq(FormError("value_pureAlcoholVolume", "minimumValue.pureAlcoholVolume", Seq.empty))
      }

      "sprDutyRate" in {
        val result = testForm.bind(
          Map(
            "value.totalLitresVolume" -> "0.01",
            "value.pureAlcoholVolume" -> "0.0001",
            "value.sprDutyRate"       -> "-0.01"
          )
        )
        result.errors mustBe Seq(FormError("value_sprDutyRate", "minimumValue.sprDutyRate", Seq.empty))
      }
    }

    "must bind the largest values" - {
      "totalLitresVolume" in {
        val result = testForm.bind(
          Map(
            "value.totalLitresVolume" -> "999999999.99",
            "value.pureAlcoholVolume" -> "999999999.9999",
            "value.sprDutyRate"       -> "999999999.99"
          )
        )
        result.errors mustBe Seq(
          FormError("value_pureAlcoholVolume", "inconsistent", Seq.empty)
        ) // As pure alcohol volume exceeds total litres, but has already passed max check
      }
    }

    "must not bind values that are too large" - {
      "totalLitresVolume" in {
        val result = testForm.bind(
          Map(
            "value.totalLitresVolume" -> "1000000000",
            "value.pureAlcoholVolume" -> "999999999.9999",
            "value.sprDutyRate"       -> "999999999.99"
          )
        )
        result.errors mustBe Seq(FormError("value_totalLitresVolume", "maximumValue.totalLitresVolume", Seq.empty))
      }

      "pureAlcoholVolume" in {
        val result = testForm.bind(
          Map(
            "value.totalLitresVolume" -> "999999999.99",
            "value.pureAlcoholVolume" -> "1000000000.0000",
            "value.sprDutyRate"       -> "999999999.99"
          )
        )
        result.errors mustBe Seq(FormError("value_pureAlcoholVolume", "maximumValue.pureAlcoholVolume", Seq.empty))
      }

      "sprDutyRate" in {
        val result = testForm.bind(
          Map(
            "value.totalLitresVolume" -> "999999999.99",
            "value.pureAlcoholVolume" -> "999999999.9999",
            "value.sprDutyRate"       -> "1000000000"
          )
        )
        result.errors mustBe Seq(FormError("value_sprDutyRate", "maximumValue.sprDutyRate", Seq.empty))
      }
    }

    "when totalLitresVolume compared to pureAlcoholVolume" - {
      "is greater than or equal to it must bind" in {
        val result = testForm.bind(
          Map(
            "value.totalLitresVolume" -> "12.46",
            "value.pureAlcoholVolume" -> "12.4500",
            "value.sprDutyRate"       -> "5.31"
          )
        )
        result.errors mustBe Seq.empty

        val result2 = testForm.bind(
          Map(
            "value.totalLitresVolume" -> "12.45",
            "value.pureAlcoholVolume" -> "12.4500",
            "value.sprDutyRate"       -> "5.31"
          )
        )
        result2.errors mustBe Seq.empty
      }

      "is less than it must not bind" in {
        val result = testForm.bind(
          Map(
            "value.totalLitresVolume" -> "12.44",
            "value.pureAlcoholVolume" -> "12.4500",
            "value.sprDutyRate"       -> "5.31"
          )
        )
        result.errors mustBe Seq(FormError("value_pureAlcoholVolume", "inconsistent", Seq.empty))
      }
    }

    "can unbind" in {
      val result = mapping.unbind(AdjustmentVolumeWithSPR(BigDecimal(1234.45), BigDecimal(12.3456), BigDecimal(5.31)))
      result.get(".totalLitresVolume") mustBe Some("1234.45")
      result.get(".pureAlcoholVolume") mustBe Some("12.3456")
      result.get(".sprDutyRate")       mustBe Some("5.31")
    }
  }

  "adjustmentVolumes" - {
    val mapping = adjustmentVolumes(
      "invalid",
      "required",
      "decimalPlaces",
      "minimumValue",
      "maximumValue",
      "inconsistent",
      Seq.empty
    )

    val testForm: Form[AdjustmentVolume] = Form("value" -> mapping)

    "must bind a valid AdjustmentVolume" in {
      val result = testForm.bind(Map("value.totalLitresVolume" -> "1234.45", "value.pureAlcoholVolume" -> "12.3456"))
      result.get mustBe AdjustmentVolume(BigDecimal(1234.45), BigDecimal(12.3456))
    }

    "must not bind when missing" - {
      "totalLitresVolume" in {
        val result = testForm.bind(Map("value.pureAlcoholVolume" -> "12.3456"))
        result.errors mustBe Seq(FormError("value_totalLitresVolume", "required.totalLitresVolume", Seq.empty))
      }

      "pureAlcoholVolume" in {
        val result = testForm.bind(Map("value.totalLitresVolume" -> "1234.45"))
        result.errors mustBe Seq(FormError("value_pureAlcoholVolume", "required.pureAlcoholVolume", Seq.empty))
      }
    }

    "must not bind an empty" - {
      "totalLitresVolume" in {
        val result = testForm.bind(Map("value.totalLitresVolume" -> "", "value.pureAlcoholVolume" -> "12.3456"))
        result.errors mustBe Seq(FormError("value_totalLitresVolume", "required.totalLitresVolume", Seq.empty))
      }

      "pureAlcoholVolume" in {
        val result = testForm.bind(Map("value.totalLitresVolume" -> "1234.45", "value.pureAlcoholVolume" -> ""))
        result.errors mustBe Seq(FormError("value_pureAlcoholVolume", "required.pureAlcoholVolume", Seq.empty))
      }
    }

    "must not bind non-numeric value" - {
      "totalLitresVolume" in {
        val result = testForm.bind(Map("value.totalLitresVolume" -> "a", "value.pureAlcoholVolume" -> "12.3456"))
        result.errors mustBe Seq(FormError("value_totalLitresVolume", "invalid.totalLitresVolume", Seq.empty))
      }

      "pureAlcoholVolume" in {
        val result = testForm.bind(Map("value.totalLitresVolume" -> "1234.45", "value.pureAlcoholVolume" -> "a"))
        result.errors mustBe Seq(FormError("value_pureAlcoholVolume", "invalid.pureAlcoholVolume", Seq.empty))
      }
    }

    "must not bind values with too many decimal places" - {
      "totalLitresVolume" in {
        val result = testForm.bind(Map("value.totalLitresVolume" -> "1234.456", "value.pureAlcoholVolume" -> "12.3456"))
        result.errors mustBe Seq(FormError("value_totalLitresVolume", "decimalPlaces.totalLitresVolume", Seq.empty))
      }

      "pureAlcoholVolume" in {
        val result = testForm.bind(Map("value.totalLitresVolume" -> "1234.45", "value.pureAlcoholVolume" -> "12.34567"))
        result.errors mustBe Seq(FormError("value_pureAlcoholVolume", "decimalPlaces.pureAlcoholVolume", Seq.empty))
      }
    }

    "must not bind pureAlcoholVolume values" - {
      "with less than 4 decimal places" in {
        val result = testForm.bind(
          Map(
            "value.totalLitresVolume" -> "1234.45",
            "value.pureAlcoholVolume" -> "12.345",
            "value.dutyRate"          -> "5.31"
          )
        )
        result.errors mustBe Seq(FormError("value_pureAlcoholVolume", "decimalPlaces.pureAlcoholVolume", Seq.empty))
      }

      "with no decimal places and trailing decimal point" in {
        val result = testForm.bind(
          Map(
            "value.totalLitresVolume" -> "1234.45",
            "value.pureAlcoholVolume" -> "12.",
            "value.dutyRate"          -> "5.31"
          )
        )
        result.errors mustBe Seq(FormError("value_pureAlcoholVolume", "decimalPlaces.pureAlcoholVolume", Seq.empty))
      }

      "with no decimal places" in {
        val result = testForm.bind(
          Map(
            "value.totalLitresVolume" -> "1234.45",
            "value.pureAlcoholVolume" -> "12",
            "value.dutyRate"          -> "5.31"
          )
        )
        result.errors mustBe Seq(FormError("value_pureAlcoholVolume", "decimalPlaces.pureAlcoholVolume", Seq.empty))
      }
    }

    "must bind the smallest values" - {
      "totalLitresVolume" in {
        val result = testForm.bind(Map("value.totalLitresVolume" -> "0.01", "value.pureAlcoholVolume" -> "0.0001"))
        result.errors mustBe Seq.empty
      }
    }

    "must not bind values that are too small" - {
      "totalLitresVolume" in {
        val result = testForm.bind(Map("value.totalLitresVolume" -> "0", "value.pureAlcoholVolume" -> "0.0001"))
        result.errors mustBe Seq(FormError("value_totalLitresVolume", "minimumValue.totalLitresVolume", Seq.empty))
      }

      "pureAlcoholVolume" in {
        val result = testForm.bind(Map("value.totalLitresVolume" -> "0.01", "value.pureAlcoholVolume" -> "0.0000"))
        result.errors mustBe Seq(FormError("value_pureAlcoholVolume", "minimumValue.pureAlcoholVolume", Seq.empty))
      }
    }

    "must bind the largest values" - {
      "totalLitresVolume" in {
        val result =
          testForm.bind(Map("value.totalLitresVolume" -> "999999999.99", "value.pureAlcoholVolume" -> "999999999.9999"))
        result.errors mustBe Seq(
          FormError("value_pureAlcoholVolume", "inconsistent", Seq.empty)
        ) // As pure alcohol volume exceeds total litres, but has already passed max check
      }
    }

    "must not bind values that are too large" - {
      "totalLitresVolume" in {
        val result =
          testForm.bind(Map("value.totalLitresVolume" -> "1000000000", "value.pureAlcoholVolume" -> "999999999.9999"))
        result.errors mustBe Seq(FormError("value_totalLitresVolume", "maximumValue.totalLitresVolume", Seq.empty))
      }

      "pureAlcoholVolume" in {
        val result =
          testForm.bind(
            Map("value.totalLitresVolume" -> "999999999.99", "value.pureAlcoholVolume" -> "1000000000.0000")
          )
        result.errors mustBe Seq(FormError("value_pureAlcoholVolume", "maximumValue.pureAlcoholVolume", Seq.empty))
      }
    }

    "when totalLitresVolume compared to pureAlcoholVolume" - {
      "is greater than or equal to it must bind" in {
        val result = testForm.bind(Map("value.totalLitresVolume" -> "12.46", "value.pureAlcoholVolume" -> "12.4500"))
        result.errors mustBe Seq.empty

        val result2 = testForm.bind(Map("value.totalLitresVolume" -> "12.45", "value.pureAlcoholVolume" -> "12.4500"))
        result2.errors mustBe Seq.empty
      }

      "is less than it must not bind" in {
        val result = testForm.bind(Map("value.totalLitresVolume" -> "12.44", "value.pureAlcoholVolume" -> "12.4500"))
        result.errors mustBe Seq(FormError("value_pureAlcoholVolume", "inconsistent", Seq.empty))
      }
    }

    "can unbind" in {
      val result = mapping.unbind(AdjustmentVolume(BigDecimal(1234.45), BigDecimal(12.3456)))
      result.get(".totalLitresVolume") mustBe Some("1234.45")
      result.get(".pureAlcoholVolume") mustBe Some("12.3456")
    }
  }

  "volumesWithRate" - {
    val mapping = volumesWithRate(
      "invalid",
      "required",
      "decimalPlaces",
      "minimumValue",
      "maximumValue",
      "lessOrEqual",
      regimeName
    )

    val testForm: Form[VolumeAndRateByTaxType] = Form("value" -> mapping)

    "must bind a valid VolumeAndRateByTaxType" in {
      val result = testForm.bind(
        Map(
          "value.rateBandDescription" -> rateBandDescription,
          "value.taxType"             -> "123",
          "value.totalLitres"         -> "1234.45",
          "value.pureAlcohol"         -> "12.3456",
          "value.dutyRate"            -> "5.31"
        )
      )
      result.get mustBe VolumeAndRateByTaxType(
        "123",
        BigDecimal(1234.45),
        BigDecimal(12.3456),
        BigDecimal(5.31)
      )
    }

    "must fallback to regime when missing rateBandDescription" - {
      val result = testForm.bind(
        Map(
          "value.taxType"     -> "123",
          "value.totalLitres" -> "1234.45",
          "value.pureAlcohol" -> "12.3456",
          "value.dutyRate"    -> "5.31"
        )
      )
      result.get mustBe
        VolumeAndRateByTaxType(
          "123",
          BigDecimal(1234.45),
          BigDecimal(12.3456),
          BigDecimal(5.31)
        )
    }

    "must not bind when missing" - {
      "taxType" in {
        val result = testForm.bind(
          Map(
            "value.rateBandDescription" -> rateBandDescription,
            "value.totalLitres"         -> "1234.45",
            "value.pureAlcohol"         -> "12.3456",
            "value.dutyRate"            -> "5.31"
          )
        )
        result.errors mustBe Seq(FormError("value_taxType", "required.taxType", Seq(rateBandDescription, regimeName)))
      }

      "totalLitres" in {
        val result = testForm.bind(
          Map(
            "value.rateBandDescription" -> rateBandDescription,
            "value.taxType"             -> "123",
            "value.pureAlcohol"         -> "12.3456",
            "value.dutyRate"            -> "5.31"
          )
        )
        result.errors mustBe Seq(
          FormError("value_totalLitres", "required.totalLitres", Seq(rateBandDescription, regimeName))
        )
      }

      "pureAlcohol" in {
        val result = testForm.bind(
          Map(
            "value.rateBandDescription" -> rateBandDescription,
            "value.taxType"             -> "123",
            "value.totalLitres"         -> "1234.45",
            "value.dutyRate"            -> "5.31"
          )
        )
        result.errors mustBe Seq(
          FormError("value_pureAlcohol", "required.pureAlcohol", Seq(rateBandDescription, regimeName))
        )
      }

      "dutyRate" in {
        val result = testForm.bind(
          Map(
            "value.rateBandDescription" -> rateBandDescription,
            "value.taxType"             -> "123",
            "value.totalLitres"         -> "1234.45",
            "value.pureAlcohol"         -> "12.3456"
          )
        )
        result.errors mustBe Seq(FormError("value_dutyRate", "required.dutyRate", Seq(rateBandDescription, regimeName)))
      }
    }

    "must not bind an empty" - {
      "taxType" in {
        val result = testForm.bind(
          Map(
            "value.rateBandDescription" -> rateBandDescription,
            "value.taxType"             -> "",
            "value.totalLitres"         -> "1234.45",
            "value.pureAlcohol"         -> "12.3456",
            "value.dutyRate"            -> "5.31"
          )
        )
        result.errors mustBe Seq(FormError("value_taxType", "required.taxType", Seq(rateBandDescription, regimeName)))
      }

      "totalLitres" in {
        val result = testForm.bind(
          Map(
            "value.rateBandDescription" -> rateBandDescription,
            "value.taxType"             -> "123",
            "value.totalLitres"         -> "",
            "value.pureAlcohol"         -> "12.3456",
            "value.dutyRate"            -> "5.31"
          )
        )
        result.errors mustBe Seq(
          FormError("value_totalLitres", "required.totalLitres", Seq(rateBandDescription, regimeName))
        )
      }

      "pureAlcohol" in {
        val result = testForm.bind(
          Map(
            "value.rateBandDescription" -> rateBandDescription,
            "value.taxType"             -> "123",
            "value.totalLitres"         -> "1234.45",
            "value.pureAlcohol"         -> "",
            "value.dutyRate"            -> "5.31"
          )
        )
        result.errors mustBe Seq(
          FormError("value_pureAlcohol", "required.pureAlcohol", Seq(rateBandDescription, regimeName))
        )
      }

      "dutyRate" in {
        val result = testForm.bind(
          Map(
            "value.rateBandDescription" -> rateBandDescription,
            "value.taxType"             -> "123",
            "value.totalLitres"         -> "1234.45",
            "value.pureAlcohol"         -> "12.3456",
            "value.dutyRate"            -> ""
          )
        )
        result.errors mustBe Seq(FormError("value_dutyRate", "required.dutyRate", Seq(rateBandDescription, regimeName)))
      }
    }

    "must not bind non-numeric value" - {
      "totalLitres" in {
        val result = testForm.bind(
          Map(
            "value.rateBandDescription" -> rateBandDescription,
            "value.taxType"             -> "123",
            "value.totalLitres"         -> "a",
            "value.pureAlcohol"         -> "12.3456",
            "value.dutyRate"            -> "5.31"
          )
        )
        result.errors mustBe Seq(
          FormError("value_totalLitres", "invalid.totalLitres", Seq(rateBandDescription, regimeName))
        )
      }

      "pureAlcohol" in {
        val result = testForm.bind(
          Map(
            "value.rateBandDescription" -> rateBandDescription,
            "value.taxType"             -> "123",
            "value.totalLitres"         -> "1234.45",
            "value.pureAlcohol"         -> "a",
            "value.dutyRate"            -> "5.31"
          )
        )
        result.errors mustBe Seq(
          FormError("value_pureAlcohol", "invalid.pureAlcohol", Seq(rateBandDescription, regimeName))
        )
      }

      "dutyRate" in {
        val result = testForm.bind(
          Map(
            "value.rateBandDescription" -> rateBandDescription,
            "value.taxType"             -> "123",
            "value.totalLitres"         -> "1234.45",
            "value.pureAlcohol"         -> "12.3456",
            "value.dutyRate"            -> "a"
          )
        )
        result.errors mustBe Seq(FormError("value_dutyRate", "invalid.dutyRate", Seq(rateBandDescription, regimeName)))
      }
    }

    "must not bind values with too many decimal places" - {
      "totalLitres" in {
        val result = testForm.bind(
          Map(
            "value.rateBandDescription" -> rateBandDescription,
            "value.taxType"             -> "123",
            "value.totalLitres"         -> "1234.456",
            "value.pureAlcohol"         -> "12.3456",
            "value.dutyRate"            -> "5.31"
          )
        )
        result.errors mustBe Seq(
          FormError("value_totalLitres", "decimalPlaces.totalLitres", Seq(rateBandDescription, regimeName))
        )
      }

      "pureAlcohol" in {
        val result = testForm.bind(
          Map(
            "value.rateBandDescription" -> rateBandDescription,
            "value.taxType"             -> "123",
            "value.totalLitres"         -> "1234.45",
            "value.pureAlcohol"         -> "12.34567",
            "value.dutyRate"            -> "5.31"
          )
        )
        result.errors mustBe Seq(
          FormError("value_pureAlcohol", "decimalPlaces.pureAlcohol", Seq(rateBandDescription, regimeName))
        )
      }

      "dutyRate" in {
        val result = testForm.bind(
          Map(
            "value.rateBandDescription" -> rateBandDescription,
            "value.taxType"             -> "123",
            "value.totalLitres"         -> "1234.45",
            "value.pureAlcohol"         -> "12.3456",
            "value.dutyRate"            -> "5.312"
          )
        )
        result.errors mustBe Seq(
          FormError("value_dutyRate", "decimalPlaces.dutyRate", Seq(rateBandDescription, regimeName))
        )
      }
    }

    "must not bind pureAlcohol values" - {
      "with less than 4 decimal places" in {
        val result = testForm.bind(
          Map(
            "value.rateBandDescription" -> rateBandDescription,
            "value.taxType"             -> "123",
            "value.totalLitres"         -> "1234.45",
            "value.pureAlcohol"         -> "12.345",
            "value.dutyRate"            -> "5.31"
          )
        )
        result.errors mustBe Seq(
          FormError("value_pureAlcohol", "decimalPlaces.pureAlcohol", Seq(rateBandDescription, regimeName))
        )
      }

      "with no decimal places and trailing decimal point" in {
        val result = testForm.bind(
          Map(
            "value.rateBandDescription" -> rateBandDescription,
            "value.taxType"             -> "123",
            "value.totalLitres"         -> "1234.45",
            "value.pureAlcohol"         -> "12.",
            "value.dutyRate"            -> "5.31"
          )
        )
        result.errors mustBe Seq(
          FormError("value_pureAlcohol", "decimalPlaces.pureAlcohol", Seq(rateBandDescription, regimeName))
        )
      }

      "with no decimal places" in {
        val result = testForm.bind(
          Map(
            "value.rateBandDescription" -> rateBandDescription,
            "value.taxType"             -> "123",
            "value.totalLitres"         -> "1234.45",
            "value.pureAlcohol"         -> "12",
            "value.dutyRate"            -> "5.31"
          )
        )
        result.errors mustBe Seq(
          FormError("value_pureAlcohol", "decimalPlaces.pureAlcohol", Seq(rateBandDescription, regimeName))
        )
      }
    }

    "must bind the smallest values" - {
      "totalLitres" in {
        val result = testForm.bind(
          Map(
            "value.rateBandDescription" -> rateBandDescription,
            "value.taxType"             -> "123",
            "value.totalLitres"         -> "0.01",
            "value.pureAlcohol"         -> "0.0001",
            "value.dutyRate"            -> "0"
          )
        )
        result.errors mustBe Seq.empty
      }
    }

    "must not bind values that are too small" - {
      "totalLitres" in {
        val result = testForm.bind(
          Map(
            "value.rateBandDescription" -> rateBandDescription,
            "value.taxType"             -> "123",
            "value.totalLitres"         -> "0",
            "value.pureAlcohol"         -> "0.0001",
            "value.dutyRate"            -> "0"
          )
        )
        result.errors mustBe Seq(
          FormError("value_totalLitres", "minimumValue.totalLitres", Seq(rateBandDescription, regimeName))
        )
      }

      "pureAlcohol" in {
        val result = testForm.bind(
          Map(
            "value.rateBandDescription" -> rateBandDescription,
            "value.taxType"             -> "123",
            "value.totalLitres"         -> "0.01",
            "value.pureAlcohol"         -> "0.0000",
            "value.dutyRate"            -> "0"
          )
        )
        result.errors mustBe Seq(
          FormError("value_pureAlcohol", "minimumValue.pureAlcohol", Seq(rateBandDescription, regimeName))
        )
      }

      "dutyRate" in {
        val result = testForm.bind(
          Map(
            "value.rateBandDescription" -> rateBandDescription,
            "value.taxType"             -> "123",
            "value.totalLitres"         -> "0.01",
            "value.pureAlcohol"         -> "0.0001",
            "value.dutyRate"            -> "-0.01"
          )
        )
        result.errors mustBe Seq(
          FormError("value_dutyRate", "minimumValue.dutyRate", Seq(rateBandDescription, regimeName))
        )
      }
    }

    "must bind the largest values" - {
      "totalLitres" in {
        val result = testForm.bind(
          Map(
            "value.rateBandDescription" -> rateBandDescription,
            "value.taxType"             -> "123",
            "value.totalLitres"         -> "999999999.99",
            "value.pureAlcohol"         -> "999999999.9999",
            "value.dutyRate"            -> "999999999.99"
          )
        )
        result.errors mustBe Seq(
          FormError("value_pureAlcohol", "lessOrEqual", Seq(rateBandDescription, regimeName))
        ) // As pure alcohol volume exceeds total litres, but has already passed max check
      }
    }

    "must not bind values that are too large" - {
      "totalLitres" in {
        val result = testForm.bind(
          Map(
            "value.rateBandDescription" -> rateBandDescription,
            "value.taxType"             -> "123",
            "value.totalLitres"         -> "1000000000",
            "value.pureAlcohol"         -> "999999999.9999",
            "value.dutyRate"            -> "999999999.99"
          )
        )
        result.errors mustBe Seq(
          FormError("value_totalLitres", "maximumValue.totalLitres", Seq(rateBandDescription, regimeName))
        )
      }

      "pureAlcohol" in {
        val result = testForm.bind(
          Map(
            "value.rateBandDescription" -> rateBandDescription,
            "value.taxType"             -> "123",
            "value.totalLitres"         -> "999999999.99",
            "value.pureAlcohol"         -> "1000000000.0000",
            "value.dutyRate"            -> "999999999.99"
          )
        )
        result.errors mustBe Seq(
          FormError("value_pureAlcohol", "maximumValue.pureAlcohol", Seq(rateBandDescription, regimeName))
        )
      }

      "dutyRate" in {
        val result = testForm.bind(
          Map(
            "value.rateBandDescription" -> rateBandDescription,
            "value.taxType"             -> "123",
            "value.totalLitres"         -> "999999999.99",
            "value.pureAlcohol"         -> "999999999.9999",
            "value.dutyRate"            -> "1000000000"
          )
        )
        result.errors mustBe Seq(
          FormError("value_dutyRate", "maximumValue.dutyRate", Seq(rateBandDescription, regimeName))
        )
      }
    }

    "when totalLitres compared to pureAlcohol" - {
      "is greater than or equal to it must bind" in {
        val result = testForm.bind(
          Map(
            "value.rateBandDescription" -> rateBandDescription,
            "value.taxType"             -> "123",
            "value.totalLitres"         -> "12.46",
            "value.pureAlcohol"         -> "12.4500",
            "value.dutyRate"            -> "5.31"
          )
        )
        result.errors mustBe Seq.empty

        val result2 = testForm.bind(
          Map(
            "value.rateBandDescription" -> rateBandDescription,
            "value.taxType"             -> "123",
            "value.totalLitres"         -> "12.45",
            "value.pureAlcohol"         -> "12.4500",
            "value.dutyRate"            -> "5.31"
          )
        )
        result2.errors mustBe Seq.empty
      }

      "is less than it must not bind" in {
        val result = testForm.bind(
          Map(
            "value.rateBandDescription" -> rateBandDescription,
            "value.taxType"             -> "123",
            "value.totalLitres"         -> "12.44",
            "value.pureAlcohol"         -> "12.4500",
            "value.dutyRate"            -> "5.31"
          )
        )
        result.errors mustBe Seq(
          FormError("value_pureAlcohol", "lessOrEqual", Seq(rateBandDescription, regimeName))
        )
      }
    }

    "can unbind" in {
      val result =
        mapping.unbind(
          VolumeAndRateByTaxType("123", BigDecimal(1234.45), BigDecimal(12.3456), BigDecimal(5.31))
        )
      result.get(".taxType")     mustBe Some("123")
      result.get(".totalLitres") mustBe Some("1234.45")
      result.get(".pureAlcohol") mustBe Some("12.3456")
      result.get(".dutyRate")    mustBe Some("5.31")
    }
  }

  "volumes" - {
    val mapping = volumes(
      "invalid",
      "required",
      "decimalPlaces",
      "minimumValue",
      "maximumValue",
      "lessOrEqual",
      regimeName
    )

    val testForm: Form[VolumesByTaxType] = Form("value" -> mapping)

    "must bind a valid VolumesByTaxType" in {
      val result =
        testForm.bind(
          Map(
            "value.rateBandDescription" -> rateBandDescription,
            "value.taxType"             -> "123",
            "value.totalLitres"         -> "1234.45",
            "value.pureAlcohol"         -> "12.3456"
          )
        )
      result.get mustBe VolumesByTaxType("123", BigDecimal(1234.45), BigDecimal(12.3456))
    }

    "must fallback to regime when missing rateBandDescription" - {
      val result = testForm.bind(
        Map(
          "value.taxType"     -> "123",
          "value.totalLitres" -> "1234.45",
          "value.pureAlcohol" -> "12.3456"
        )
      )
      result.get mustBe
        VolumesByTaxType(
          "123",
          BigDecimal(1234.45),
          BigDecimal(12.3456)
        )
    }

    "must not bind when missing" - {
      "taxType" in {
        val result =
          testForm.bind(
            Map(
              "value.rateBandDescription" -> rateBandDescription,
              "value.totalLitres"         -> "1234.45",
              "value.pureAlcohol"         -> "12.3456"
            )
          )
        result.errors mustBe Seq(FormError("value_taxType", "required.taxType", Seq(rateBandDescription, regimeName)))
      }

      "totalLitres" in {
        val result =
          testForm.bind(
            Map(
              "value.rateBandDescription" -> rateBandDescription,
              "value.taxType"             -> "123",
              "value.pureAlcohol"         -> "12.3456"
            )
          )
        result.errors mustBe Seq(
          FormError("value_totalLitres", "required.totalLitres", Seq(rateBandDescription, regimeName))
        )
      }

      "pureAlcohol" in {
        val result =
          testForm.bind(
            Map(
              "value.rateBandDescription" -> rateBandDescription,
              "value.taxType"             -> "123",
              "value.totalLitres"         -> "1234.45"
            )
          )
        result.errors mustBe Seq(
          FormError("value_pureAlcohol", "required.pureAlcohol", Seq(rateBandDescription, regimeName))
        )
      }
    }

    "must not bind an empty" - {
      "taxType" in {
        val result =
          testForm.bind(
            Map(
              "value.rateBandDescription" -> rateBandDescription,
              "value.taxType"             -> "",
              "value.totalLitres"         -> "1234.45",
              "value.pureAlcohol"         -> "12.3456"
            )
          )
        result.errors mustBe Seq(FormError("value_taxType", "required.taxType", Seq(rateBandDescription, regimeName)))
      }

      "totalLitres" in {
        val result =
          testForm.bind(
            Map(
              "value.rateBandDescription" -> rateBandDescription,
              "value.taxType"             -> "123",
              "value.totalLitres"         -> "",
              "value.pureAlcohol"         -> "12.3456"
            )
          )
        result.errors mustBe Seq(
          FormError("value_totalLitres", "required.totalLitres", Seq(rateBandDescription, regimeName))
        )
      }

      "pureAlcohol" in {
        val result =
          testForm.bind(
            Map(
              "value.rateBandDescription" -> rateBandDescription,
              "value.taxType"             -> "123",
              "value.totalLitres"         -> "1234.45",
              "value.pureAlcohol"         -> ""
            )
          )
        result.errors mustBe Seq(
          FormError("value_pureAlcohol", "required.pureAlcohol", Seq(rateBandDescription, regimeName))
        )
      }
    }

    "must not bind non-numeric value" - {
      "totalLitres" in {
        val result =
          testForm.bind(
            Map(
              "value.rateBandDescription" -> rateBandDescription,
              "value.taxType"             -> "123",
              "value.totalLitres"         -> "a",
              "value.pureAlcohol"         -> "12.3456"
            )
          )
        result.errors mustBe Seq(
          FormError("value_totalLitres", "invalid.totalLitres", Seq(rateBandDescription, regimeName))
        )
      }

      "pureAlcohol" in {
        val result =
          testForm.bind(
            Map(
              "value.rateBandDescription" -> rateBandDescription,
              "value.taxType"             -> "123",
              "value.totalLitres"         -> "1234.45",
              "value.pureAlcohol"         -> "a"
            )
          )
        result.errors mustBe Seq(
          FormError("value_pureAlcohol", "invalid.pureAlcohol", Seq(rateBandDescription, regimeName))
        )
      }
    }

    "must not bind values with too many decimal places" - {
      "totalLitres" in {
        val result = testForm.bind(
          Map(
            "value.rateBandDescription" -> rateBandDescription,
            "value.taxType"             -> "123",
            "value.totalLitres"         -> "1234.456",
            "value.pureAlcohol"         -> "12.3456"
          )
        )
        result.errors mustBe Seq(
          FormError("value_totalLitres", "decimalPlaces.totalLitres", Seq(rateBandDescription, regimeName))
        )
      }

      "pureAlcohol" in {
        val result = testForm.bind(
          Map(
            "value.rateBandDescription" -> rateBandDescription,
            "value.taxType"             -> "123",
            "value.totalLitres"         -> "1234.45",
            "value.pureAlcohol"         -> "12.34567"
          )
        )
        result.errors mustBe Seq(
          FormError("value_pureAlcohol", "decimalPlaces.pureAlcohol", Seq(rateBandDescription, regimeName))
        )
      }
    }

    "must not bind pureAlcohol values" - {
      "with less than 4 decimal places" in {
        val result = testForm.bind(
          Map(
            "value.rateBandDescription" -> rateBandDescription,
            "value.taxType"             -> "123",
            "value.totalLitres"         -> "1234.45",
            "value.pureAlcohol"         -> "12.345",
            "value.dutyRate"            -> "5.31"
          )
        )
        result.errors mustBe Seq(
          FormError("value_pureAlcohol", "decimalPlaces.pureAlcohol", Seq(rateBandDescription, regimeName))
        )
      }

      "with no decimal places and trailing decimal point" in {
        val result = testForm.bind(
          Map(
            "value.rateBandDescription" -> rateBandDescription,
            "value.taxType"             -> "123",
            "value.totalLitres"         -> "1234.45",
            "value.pureAlcohol"         -> "12.",
            "value.dutyRate"            -> "5.31"
          )
        )
        result.errors mustBe Seq(
          FormError("value_pureAlcohol", "decimalPlaces.pureAlcohol", Seq(rateBandDescription, regimeName))
        )
      }

      "with no decimal places" in {
        val result = testForm.bind(
          Map(
            "value.rateBandDescription" -> rateBandDescription,
            "value.taxType"             -> "123",
            "value.totalLitres"         -> "1234.45",
            "value.pureAlcohol"         -> "12",
            "value.dutyRate"            -> "5.31"
          )
        )
        result.errors mustBe Seq(
          FormError("value_pureAlcohol", "decimalPlaces.pureAlcohol", Seq(rateBandDescription, regimeName))
        )
      }
    }

    "must bind the smallest values" - {
      "totalLitres" in {
        val result =
          testForm.bind(
            Map(
              "value.rateBandDescription" -> rateBandDescription,
              "value.taxType"             -> "123",
              "value.totalLitres"         -> "0.01",
              "value.pureAlcohol"         -> "0.0001"
            )
          )
        result.errors mustBe Seq.empty
      }
    }

    "must not bind values that are too small" - {
      "totalLitres" in {
        val result =
          testForm.bind(
            Map(
              "value.rateBandDescription" -> rateBandDescription,
              "value.taxType"             -> "123",
              "value.totalLitres"         -> "0",
              "value.pureAlcohol"         -> "0.0001"
            )
          )
        result.errors mustBe Seq(
          FormError("value_totalLitres", "minimumValue.totalLitres", Seq(rateBandDescription, regimeName))
        )
      }

      "pureAlcohol" in {
        val result =
          testForm.bind(
            Map(
              "value.rateBandDescription" -> rateBandDescription,
              "value.taxType"             -> "123",
              "value.totalLitres"         -> "0.01",
              "value.pureAlcohol"         -> "0.0000"
            )
          )
        result.errors mustBe Seq(
          FormError("value_pureAlcohol", "minimumValue.pureAlcohol", Seq(rateBandDescription, regimeName))
        )
      }
    }

    "must bind the largest values" - {
      "totalLitres" in {
        val result = testForm.bind(
          Map(
            "value.rateBandDescription" -> rateBandDescription,
            "value.taxType"             -> "123",
            "value.totalLitres"         -> "999999999.99",
            "value.pureAlcohol"         -> "999999999.9999"
          )
        )
        result.errors mustBe Seq(
          FormError("value_pureAlcohol", "lessOrEqual", Seq(rateBandDescription, regimeName))
        ) // As pure alcohol volume exceeds total litres, but has already passed max check
      }
    }

    "must not bind values that are too large" - {
      "totalLitres" in {
        val result = testForm.bind(
          Map(
            "value.rateBandDescription" -> rateBandDescription,
            "value.taxType"             -> "123",
            "value.totalLitres"         -> "1000000000",
            "value.pureAlcohol"         -> "999999999.9999"
          )
        )
        result.errors mustBe Seq(
          FormError("value_totalLitres", "maximumValue.totalLitres", Seq(rateBandDescription, regimeName))
        )
      }

      "pureAlcohol" in {
        val result = testForm.bind(
          Map(
            "value.rateBandDescription" -> rateBandDescription,
            "value.taxType"             -> "123",
            "value.totalLitres"         -> "999999999.99",
            "value.pureAlcohol"         -> "1000000000.0000"
          )
        )
        result.errors mustBe Seq(
          FormError("value_pureAlcohol", "maximumValue.pureAlcohol", Seq(rateBandDescription, regimeName))
        )
      }
    }

    "when totalLitres compared to pureAlcohol" - {
      "is greater than or equal to it must bind" in {
        val result =
          testForm.bind(
            Map(
              "value.rateBandDescription" -> rateBandDescription,
              "value.taxType"             -> "123",
              "value.totalLitres"         -> "12.46",
              "value.pureAlcohol"         -> "12.4500"
            )
          )
        result.errors mustBe Seq.empty

        val result2 =
          testForm.bind(
            Map(
              "value.rateBandDescription" -> rateBandDescription,
              "value.taxType"             -> "123",
              "value.totalLitres"         -> "12.45",
              "value.pureAlcohol"         -> "12.4500"
            )
          )
        result2.errors mustBe Seq.empty
      }

      "is less than it must not bind" in {
        val result =
          testForm.bind(
            Map(
              "value.rateBandDescription" -> rateBandDescription,
              "value.taxType"             -> "123",
              "value.totalLitres"         -> "12.44",
              "value.pureAlcohol"         -> "12.4567"
            )
          )
        result.errors mustBe Seq(
          FormError("value_pureAlcohol", "lessOrEqual", Seq(rateBandDescription, regimeName))
        )
      }
    }

    "can unbind" in {
      val result = mapping.unbind(VolumesByTaxType("123", BigDecimal(1234.45), BigDecimal(12.3456)))
      result.get(".taxType")     mustBe Some("123")
      result.get(".totalLitres") mustBe Some("1234.45")
      result.get(".pureAlcohol") mustBe Some("12.3456")
    }
  }

  "spoiltVolumes" - {
    val mapping = spoiltVolumesWithDuty(
      "invalid",
      "required",
      "decimalPlaces",
      "minimumValue",
      "maximumValue",
      "inconsistentKey",
      Seq.empty
    )

    val testForm: Form[SpoiltVolumeWithDuty] = Form("value" -> mapping)

    "must bind a valid SpoiltVolumeWithDuty" in {
      val result = testForm.bind(
        Map(
          "value.totalLitresVolume" -> "1234.45",
          "value.pureAlcoholVolume" -> "12.3456",
          "value.duty"              -> "5.31"
        )
      )
      result.get mustBe SpoiltVolumeWithDuty(BigDecimal(1234.45), BigDecimal(12.3456), BigDecimal(5.31))
    }

    "must not bind when missing" - {
      "totalLitresVolume" in {
        val result = testForm.bind(
          Map(
            "value.pureAlcoholVolume" -> "12.3456",
            "value.duty"              -> "5.31"
          )
        )
        result.errors mustBe Seq(FormError("value_totalLitresVolume", "required.totalLitresVolume", Seq.empty))
      }

      "pureAlcohol" in {
        val result = testForm.bind(
          Map(
            "value.totalLitresVolume" -> "1234.45",
            "value.duty"              -> "5.31"
          )
        )
        result.errors mustBe Seq(FormError("value_pureAlcoholVolume", "required.pureAlcoholVolume", Seq.empty))
      }

      "duty" in {
        val result = testForm.bind(
          Map(
            "value.totalLitresVolume" -> "1234.45",
            "value.pureAlcoholVolume" -> "12.3456"
          )
        )
        result.errors mustBe Seq(FormError("value_duty", "required.duty", Seq.empty))
      }
    }

    "must not bind an empty" - {
      "totalLitresVolume" in {
        val result = testForm.bind(
          Map(
            "value.totalLitresVolume" -> "",
            "value.pureAlcoholVolume" -> "12.3456",
            "value.duty"              -> "5.31"
          )
        )
        result.errors mustBe Seq(FormError("value_totalLitresVolume", "required.totalLitresVolume", Seq.empty))
      }

      "pureAlcohol" in {
        val result = testForm.bind(
          Map(
            "value.totalLitresVolume" -> "1234.45",
            "value.pureAlcoholVolume" -> "",
            "value.duty"              -> "5.31"
          )
        )
        result.errors mustBe Seq(FormError("value_pureAlcoholVolume", "required.pureAlcoholVolume", Seq.empty))
      }

      "duty" in {
        val result = testForm.bind(
          Map(
            "value.totalLitresVolume" -> "1234.45",
            "value.pureAlcoholVolume" -> "12.3456",
            "value.duty"              -> ""
          )
        )
        result.errors mustBe Seq(FormError("value_duty", "required.duty", Seq.empty))
      }
    }

    "must not bind non-numeric value" - {
      "totalLitresVolume" in {
        val result = testForm.bind(
          Map(
            "value.totalLitresVolume" -> "a",
            "value.pureAlcoholVolume" -> "12.3456",
            "value.duty"              -> "5.31"
          )
        )
        result.errors mustBe Seq(FormError("value_totalLitresVolume", "invalid.totalLitresVolume", Seq.empty))
      }

      "pureAlcoholVolume" in {
        val result = testForm.bind(
          Map(
            "value.totalLitresVolume" -> "1234.45",
            "value.pureAlcoholVolume" -> "a",
            "value.duty"              -> "5.31"
          )
        )
        result.errors mustBe Seq(FormError("value_pureAlcoholVolume", "invalid.pureAlcoholVolume", Seq.empty))
      }

      "duty" in {
        val result = testForm.bind(
          Map(
            "value.totalLitresVolume" -> "1234.45",
            "value.pureAlcoholVolume" -> "12.3456",
            "value.duty"              -> "a"
          )
        )
        result.errors mustBe Seq(FormError("value_duty", "invalid.duty", Seq.empty))
      }
    }

    "must not bind values with too many decimal places" - {
      "totalLitresVolume" in {
        val result = testForm.bind(
          Map(
            "value.totalLitresVolume" -> "1234.456",
            "value.pureAlcoholVolume" -> "12.3456",
            "value.duty"              -> "5.31"
          )
        )
        result.errors mustBe Seq(FormError("value_totalLitresVolume", "decimalPlaces.totalLitresVolume", Seq.empty))
      }

      "pureAlcoholVolume" in {
        val result = testForm.bind(
          Map(
            "value.totalLitresVolume" -> "1234.45",
            "value.pureAlcoholVolume" -> "12.34567",
            "value.duty"              -> "5.31"
          )
        )
        result.errors mustBe Seq(FormError("value_pureAlcoholVolume", "decimalPlaces.pureAlcoholVolume", Seq.empty))
      }

      "duty" in {
        val result = testForm.bind(
          Map(
            "value.totalLitresVolume" -> "1234.45",
            "value.pureAlcoholVolume" -> "12.3456",
            "value.duty"              -> "5.312"
          )
        )
        result.errors mustBe Seq(FormError("value_duty", "decimalPlaces.duty", Seq.empty))
      }
    }

    "must not bind pureAlcoholVolume values" - {
      "with less than 4 decimal places" in {
        val result = testForm.bind(
          Map(
            "value.totalLitresVolume" -> "1234.45",
            "value.pureAlcoholVolume" -> "12.345",
            "value.duty"              -> "5.31"
          )
        )
        result.errors mustBe Seq(FormError("value_pureAlcoholVolume", "decimalPlaces.pureAlcoholVolume", Seq.empty))
      }

      "with no decimal places and trailing decimal point" in {
        val result = testForm.bind(
          Map(
            "value.totalLitresVolume" -> "1234.45",
            "value.pureAlcoholVolume" -> "12.",
            "value.duty"              -> "5.31"
          )
        )
        result.errors mustBe Seq(FormError("value_pureAlcoholVolume", "decimalPlaces.pureAlcoholVolume", Seq.empty))
      }

      "with no decimal places" in {
        val result = testForm.bind(
          Map(
            "value.totalLitresVolume" -> "1234.45",
            "value.pureAlcoholVolume" -> "12",
            "value.duty"              -> "5.31"
          )
        )
        result.errors mustBe Seq(FormError("value_pureAlcoholVolume", "decimalPlaces.pureAlcoholVolume", Seq.empty))
      }
    }

    "must bind the smallest values" - {
      "totalLitresVolume" in {
        val result = testForm.bind(
          Map(
            "value.totalLitresVolume" -> "0.01",
            "value.pureAlcoholVolume" -> "0.0001",
            "value.duty"              -> "0.01"
          )
        )
        result.errors mustBe Seq.empty
      }
    }

    "must not bind values that are too small" - {
      "totalLitresVolume" in {
        val result = testForm.bind(
          Map(
            "value.totalLitresVolume" -> "0",
            "value.pureAlcoholVolume" -> "0.0001",
            "value.duty"              -> "0.01"
          )
        )
        result.errors mustBe Seq(FormError("value_totalLitresVolume", "minimumValue.totalLitresVolume", Seq.empty))
      }

      "pureAlcoholVolume" in {
        val result = testForm.bind(
          Map(
            "value.totalLitresVolume" -> "0.01",
            "value.pureAlcoholVolume" -> "0.0000",
            "value.duty"              -> "0.01"
          )
        )
        result.errors mustBe Seq(FormError("value_pureAlcoholVolume", "minimumValue.pureAlcoholVolume", Seq.empty))
      }

      "duty" in {
        val result = testForm.bind(
          Map(
            "value.totalLitresVolume" -> "0.01",
            "value.pureAlcoholVolume" -> "0.0001",
            "value.duty"              -> "0"
          )
        )
        result.errors mustBe Seq(FormError("value_duty", "minimumValue.duty", Seq.empty))
      }
    }

    "must bind the largest values" - {
      "totalLitresVolume" in {
        val result = testForm.bind(
          Map(
            "value.totalLitresVolume" -> "999999999.99",
            "value.pureAlcoholVolume" -> "999999999.9999",
            "value.duty"              -> "99999999999.99"
          )
        )
        result.errors mustBe Seq(
          FormError("value_pureAlcoholVolume", "inconsistentKey", Seq.empty)
        ) // As pure alcohol volume exceeds total litres, but has already passed max check
      }
    }

    "must not bind values that are too large" - {
      "totalLitresVolume" in {
        val result = testForm.bind(
          Map(
            "value.totalLitresVolume" -> "1000000000",
            "value.pureAlcoholVolume" -> "999999999.9999",
            "value.duty"              -> "99999999999.99"
          )
        )
        result.errors mustBe Seq(FormError("value_totalLitresVolume", "maximumValue.totalLitresVolume", Seq.empty))
      }

      "pureAlcoholVolume" in {
        val result = testForm.bind(
          Map(
            "value.totalLitresVolume" -> "999999999.99",
            "value.pureAlcoholVolume" -> "1000000000.0000",
            "value.duty"              -> "99999999999.99"
          )
        )
        result.errors mustBe Seq(FormError("value_pureAlcoholVolume", "maximumValue.pureAlcoholVolume", Seq.empty))
      }

      "duty" in {
        val result = testForm.bind(
          Map(
            "value.totalLitresVolume" -> "999999999.99",
            "value.pureAlcoholVolume" -> "999999999.9999",
            "value.duty"              -> "100000000000"
          )
        )
        result.errors mustBe Seq(FormError("value_duty", "maximumValue.duty", Seq.empty))
      }
    }

    "when totalLitresVolume compared to pureAlcoholVolume" - {
      "is greater than or equal to it must bind" in {
        val result = testForm.bind(
          Map(
            "value.totalLitresVolume" -> "12.46",
            "value.pureAlcoholVolume" -> "12.4500",
            "value.duty"              -> "5.31"
          )
        )
        result.errors mustBe Seq.empty

        val result2 = testForm.bind(
          Map(
            "value.totalLitresVolume" -> "12.45",
            "value.pureAlcoholVolume" -> "12.4500",
            "value.duty"              -> "5.31"
          )
        )
        result2.errors mustBe Seq.empty
      }

      "is less than it must not bind" in {
        val result = testForm.bind(
          Map(
            "value.totalLitresVolume" -> "12.44",
            "value.pureAlcoholVolume" -> "12.4500",
            "value.duty"              -> "5.31"
          )
        )
        result.errors mustBe Seq(
          FormError("value_pureAlcoholVolume", "inconsistentKey", Seq.empty)
        )
      }
    }

    "can unbind" in {
      val result =
        mapping.unbind(SpoiltVolumeWithDuty(BigDecimal(1234.45), BigDecimal(12.3456), BigDecimal(5.31)))
      result.get(".totalLitresVolume") mustBe Some("1234.45")
      result.get(".pureAlcoholVolume") mustBe Some("12.3456")
      result.get(".duty")              mustBe Some("5.31")
    }
  }
}
