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

package models

import base.SpecBase
import generators.ModelGenerators
import org.scalacheck.Gen
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json._

import java.time.YearMonth

class RatePeriodSpec extends SpecBase with MockitoSugar with ScalaCheckPropertyChecks with ModelGenerators {
  "RateType" - {
    "when apply is called" - {
      "should create correct instances" in {
        RateType(hasDraughtRelief = false, hasSmallProducerRelief = false) mustEqual RateType.Core
        RateType(hasDraughtRelief = true, hasSmallProducerRelief = false) mustEqual RateType.DraughtRelief
        RateType(hasDraughtRelief = false, hasSmallProducerRelief = true) mustEqual RateType.SmallProducerRelief
        RateType(
          hasDraughtRelief = true,
          hasSmallProducerRelief = true
        ) mustEqual RateType.DraughtAndSmallProducerRelief
      }
    }
    "when writing to json" - {
      "should return the correct string representation" in {
        Json.toJson[RateType](RateType.Core) mustEqual JsString("Core")
        Json.toJson[RateType](RateType.DraughtRelief) mustEqual JsString("DraughtRelief")
        Json.toJson[RateType](RateType.SmallProducerRelief) mustEqual JsString("SmallProducerRelief")
        Json.toJson[RateType](RateType.DraughtAndSmallProducerRelief) mustEqual JsString(
          "DraughtAndSmallProducerRelief"
        )
      }
    }
    "when reading from json" - {
      "should translate from the string rep, to the correct case object" in {
        JsString("Core").as[RateType] mustEqual RateType.Core
        JsString("DraughtRelief").as[RateType] mustEqual RateType.DraughtRelief
        JsString("SmallProducerRelief").as[RateType] mustEqual RateType.SmallProducerRelief
        JsString("DraughtAndSmallProducerRelief").as[RateType] mustEqual RateType.DraughtAndSmallProducerRelief
      }
      "should return an exception in response to an unrecognised string" in {
        JsString("some-other").validate[RateType] mustEqual JsError("some-other is not a valid RateType")
      }
      "should return a JsError when passed a type that is not a string" in {
        val result = Json.fromJson[RateType](JsBoolean(true))
        result mustBe a[JsError]
      }
    }
  }

  "AlcoholRegime" - {
    "when writing to json" - {
      "should return the correct string representation" in {
        Json.toJson[AlcoholRegime](AlcoholRegime.Beer) mustEqual JsString("Beer")
        Json.toJson[AlcoholRegime](AlcoholRegime.Cider) mustEqual JsString("Cider")
        Json.toJson[AlcoholRegime](AlcoholRegime.Wine) mustEqual JsString("Wine")
        Json.toJson[AlcoholRegime](AlcoholRegime.Spirits) mustEqual JsString("Spirits")
      }
    }
    "when reading from json" - {
      "should translate from the string rep, to the correct case object" in {
        JsString("Beer").as[AlcoholRegime] mustEqual AlcoholRegime.Beer
        JsString("Cider").as[AlcoholRegime] mustEqual AlcoholRegime.Cider
        JsString("Wine").as[AlcoholRegime] mustEqual AlcoholRegime.Wine
        JsString("Spirits").as[AlcoholRegime] mustEqual AlcoholRegime.Spirits
      }
      "should return an exception in response to an unrecognised string" in {
        JsString("some-other").validate[AlcoholRegime] mustEqual JsError("some-other is not a valid AlcoholRegime")
      }
      "should return a JsError when passed a type that is not a string" in {
        val result = Json.fromJson[AlcoholRegime](JsBoolean(true))
        result mustBe a[JsError]
      }
    }
  }

  "AlcoholRate" - {
    "when creating an instance" - {
      "should successfully save the BigDecimal value when the value is in range" in {
        forAll(genAlcoholByVolumeValue) { validValue: BigDecimal =>
          AlcoholByVolume(validValue).value mustEqual validValue
        }
      }

      "should throw an exception when the value is not in range" in {
        forAll(genAlcoholByVolumeValueOutOfRange) { invalidValue: BigDecimal =>
          val exception = intercept[IllegalArgumentException] {
            AlcoholByVolume(invalidValue)
          }
          exception.getMessage must include("Percentage must be between 0 and 100")
        }
      }

      "should throw an exception when the value has more decimal points than 1" in {
        forAll(genAlcoholByVolumeValueTooBigScale) { invalidValue: BigDecimal =>
          val exception = intercept[IllegalArgumentException] {
            AlcoholByVolume(invalidValue)
          }
          exception.getMessage must include("Alcohol By Volume must have maximum 1 decimal place")
        }
      }
    }

    "when writing to json" - {
      "should return the correct number representation with decimal points" in {
        forAll(genAlcoholByVolumeValue) { validValue =>
          Json.toJson(AlcoholByVolume(validValue)) mustEqual JsNumber(validValue)
        }
      }
    }

    "when reading from json" - {
      "should translate from the number representation when it is a valid Alcohol By Volume value" in {
        forAll(genAlcoholByVolumeValue) { validValue =>
          JsNumber(validValue).as[AlcoholByVolume] mustEqual AlcoholByVolume(validValue)
        }
      }

      "should return an exception in response to an invalid rate value" in {
        forAll(genAlcoholByVolumeValueTooBigScale) { invalidValue =>
          JsNumber(invalidValue).validate[AlcoholByVolume] mustBe a[JsError]
        }
      }

      "should return a JsError when passed a type that is not a number" in {
        forAll(Gen.oneOf(Gen.alphaStr, Gen.calendar)) { invalidJsValue =>
          val result = Json.fromJson[AlcoholByVolume](JsString(invalidJsValue.toString))
          result mustBe a[JsError]
        }
      }
    }
  }

  "YearMonth formats" - {

    implicit val yearMonthFormats: Format[YearMonth]               = RatePeriod.yearMonthFormat
    implicit val optionYearMonthFormats: Format[Option[YearMonth]] = RatePeriod.optionYearMonthFormat

    "should serialise and deserialise YearMonth" in {
      forAll { (yearMonth: YearMonth) =>
        val json   = Json.toJson(yearMonth)
        val result = json.validate[YearMonth]
        result mustBe a[JsSuccess[_]]
        result.get mustEqual yearMonth
      }
    }

    "should serialise and deserialise Option[YearMonth]" in {
      forAll { (optionYearMonth: Option[YearMonth]) =>
        val json   = Json.toJson(optionYearMonth)
        val result = json.validate[Option[YearMonth]]

        result mustBe a[JsSuccess[_]]
        result.get mustEqual optionYearMonth
      }
    }
  }

  "TaxType" - {
    "reading from json" - {
      "translate from the String representation when it is a valid Tax Type value" in {
        JsString("322").as[TaxType] mustBe TaxType("322")
      }
    }
    "writing to json" - {
      "return the correct representation" in {
        Json.toJson(TaxType("322")) mustBe JsString("322")
      }
    }
    "should return a JsError when passed a type that is not a String" - {
      val result = Json.fromJson[TaxType](JsNumber(1234))
      result mustBe a[JsError]

    }
  }
}
