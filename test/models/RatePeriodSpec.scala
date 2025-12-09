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
import cats.data.NonEmptySeq
import generators.ModelGenerators
import models.AlcoholRegime.Beer
import models.RateType.Core
import org.scalacheck.Gen
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json._

import java.time.YearMonth

class RatePeriodSpec extends SpecBase with MockitoSugar with ScalaCheckPropertyChecks with ModelGenerators {
  "RateType" - {
    "when apply is called" - {
      "must create correct instances" in {
        RateType(hasDraughtRelief = false, hasSmallProducerRelief = false) mustEqual RateType.Core
        RateType(hasDraughtRelief = true, hasSmallProducerRelief = false)  mustEqual RateType.DraughtRelief
        RateType(hasDraughtRelief = false, hasSmallProducerRelief = true)  mustEqual RateType.SmallProducerRelief
        RateType(
          hasDraughtRelief = true,
          hasSmallProducerRelief = true
        )                                                                  mustEqual RateType.DraughtAndSmallProducerRelief
      }
    }
    "when writing to json" - {
      "must return the correct string representation" in {
        Json.toJson[RateType](RateType.Core)                          mustEqual JsString("Core")
        Json.toJson[RateType](RateType.DraughtRelief)                 mustEqual JsString("DraughtRelief")
        Json.toJson[RateType](RateType.SmallProducerRelief)           mustEqual JsString("SmallProducerRelief")
        Json.toJson[RateType](RateType.DraughtAndSmallProducerRelief) mustEqual JsString(
          "DraughtAndSmallProducerRelief"
        )
      }
    }
    "when reading from json" - {
      "must translate from the string rep, to the correct case object" in {
        JsString("Core").as[RateType]                          mustEqual RateType.Core
        JsString("DraughtRelief").as[RateType]                 mustEqual RateType.DraughtRelief
        JsString("SmallProducerRelief").as[RateType]           mustEqual RateType.SmallProducerRelief
        JsString("DraughtAndSmallProducerRelief").as[RateType] mustEqual RateType.DraughtAndSmallProducerRelief
      }
      "must return an exception in response to an unrecognised string" in {
        JsString("some-other").validate[RateType] mustEqual JsError("error.expected.validenumvalue")
      }
      "must return a JsError when passed a type that is not a string" in {
        val result = Json.fromJson[RateType](JsBoolean(true))
        result mustBe a[JsError]
      }
    }
  }

  "AlcoholByVolume" - {
    "when creating an instance" - {
      "must successfully save the BigDecimal value when the value is in range" in
        forAll(genAlcoholByVolumeValue) {
          (validValue: BigDecimal) =>
            AlcoholByVolume(validValue).value mustEqual validValue
        }

      "must throw an exception when the value is not in range" in
        forAll(genAlcoholByVolumeValueOutOfRange) {
          (invalidValue: BigDecimal) =>
            val exception = intercept[IllegalArgumentException] {
              AlcoholByVolume(invalidValue)
            }
            exception.getMessage must include("Percentage must be between 0 and 100")
        }

      "must throw an exception when the value has more decimal points than 1" in
        forAll(genAlcoholByVolumeValueTooBigScale) {
          (invalidValue: BigDecimal) =>
            val exception = intercept[IllegalArgumentException] {
              AlcoholByVolume(invalidValue)
            }
            exception.getMessage must include("Alcohol By Volume must have maximum 1 decimal place")
        }
    }

    "when writing to json" - {
      "must return the correct number representation with decimal points" in
        forAll(genAlcoholByVolumeValue) { validValue =>
          Json.toJson(AlcoholByVolume(validValue)) mustEqual JsNumber(validValue)
        }
    }

    "when reading from json" - {
      "must translate from the number representation when it is a valid Alcohol By Volume value" in
        forAll(genAlcoholByVolumeValue) { validValue =>
          JsNumber(validValue).as[AlcoholByVolume] mustEqual AlcoholByVolume(validValue)
        }

      "must return an exception in response to an invalid rate value" in
        forAll(genAlcoholByVolumeValueTooBigScale) { invalidValue =>
          JsNumber(invalidValue).validate[AlcoholByVolume] mustBe a[JsError]
        }

      "must return a JsError when passed a type that is not a number" in
        forAll(Gen.oneOf(Gen.alphaStr, Gen.calendar)) { invalidJsValue =>
          val result = Json.fromJson[AlcoholByVolume](JsString(invalidJsValue.toString))
          result mustBe a[JsError]
        }
    }
  }

  "AlcoholType" - {
    "must convert AlcoholRegimes to AlcoholTypes" in {
      AlcoholType.fromAlcoholRegime(AlcoholRegime.Beer)                  mustBe AlcoholType.Beer
      AlcoholType.fromAlcoholRegime(AlcoholRegime.Cider)                 mustBe AlcoholType.Cider
      AlcoholType.fromAlcoholRegime(AlcoholRegime.Wine)                  mustBe AlcoholType.Wine
      AlcoholType.fromAlcoholRegime(AlcoholRegime.Spirits)               mustBe AlcoholType.Spirits
      AlcoholType.fromAlcoholRegime(AlcoholRegime.OtherFermentedProduct) mustBe AlcoholType.OtherFermentedProduct
    }
  }

  "YearMonth formats" - {

    implicit val yearMonthFormats: Format[YearMonth]               = RatePeriod.yearMonthFormat
    implicit val optionYearMonthFormats: Format[Option[YearMonth]] = RatePeriod.optionYearMonthFormat

    "must serialise and deserialise YearMonth" in
      forAll { (yearMonth: YearMonth) =>
        val json   = Json.toJson(yearMonth)
        val result = json.validate[YearMonth]
        result        mustBe a[JsSuccess[_]]
        result.get mustEqual yearMonth
      }

    "must serialise and deserialise Option[YearMonth]" in
      forAll { (optionYearMonth: Option[YearMonth]) =>
        val json   = Json.toJson(optionYearMonth)
        val result = json.validate[Option[YearMonth]]

        result        mustBe a[JsSuccess[_]]
        result.get mustEqual optionYearMonth
      }
  }

  "Rate Band" - {
    "must deserialise a json into a RateBand object" in {
      val json =
        """
          |      {
          |        "taxTypeCode": "311",
          |        "description": "Beer from 1.3% to 3.4%",
          |        "rateType": "Core",
          |        "rate": 9.27,
          |        "rangeDetails": [
          |          {
          |            "alcoholRegime":"Beer",
          |            "abvRanges": [
          |              {
          |                "alcoholType": "Beer",
          |                "minABV": 1.3,
          |                "maxABV": 3.4
          |              }
          |            ]
          |          }
          |        ]
          |      }
          |""".stripMargin

      val result = Json.parse(json).validate[RateBand].get

      result                                    mustBe a[RateBand]
      result.taxTypeCode                        mustBe "311"
      result.rateType                           mustBe Core
      result.rangeDetails.size                  mustBe 1
      result.rangeDetails.head.alcoholRegime    mustBe Beer
      result.rangeDetails.head.abvRanges.length mustBe 1
      result.rangeDetails.head.abvRanges.head   mustBe ABVRange(
        AlcoholType.Beer,
        AlcoholByVolume(1.3),
        AlcoholByVolume(3.4)
      )
      result.rate                               mustBe Some(BigDecimal(9.27))
    }

    "must error if the RateBand json is empty" in {
      val json =
        """
          |      {
          |        "taxTypeCode": "311",
          |        "description": "Beer from 1.3% to 3.4%",
          |        "rateType": "Core",
          |        "rate": 9.27,
          |        "rangeDetails": [
          |          {
          |            "alcoholRegime":"Beer",
          |            "abvRanges": []
          |          }
          |        ]
          |      }
          |""".stripMargin

      a[JsResultException] mustBe thrownBy(Json.parse(json).as[RateBand])
    }

    "serialize RateBand object into Json" in {
      val rateBand = RateBand(
        taxTypeCode = "311",
        description = "Description",
        rateType = Core,
        rangeDetails = Set(
          RangeDetailsByRegime(
            AlcoholRegime.Beer,
            NonEmptySeq.one(ABVRange(AlcoholType.Beer, AlcoholByVolume(1.3), AlcoholByVolume(3.4)))
          )
        ),
        rate = Some(BigDecimal(9.27))
      )

      val expectedResult = """
                             |      {
                             |        "taxTypeCode": "311",
                             |        "description": "Description",
                             |        "rateType": "Core",
                             |        "rate": 9.27,
                             |        "rangeDetails": [
                             |          {
                             |            "alcoholRegime":"Beer",
                             |            "abvRanges": [
                             |              {
                             |                "alcoholType": "Beer",
                             |                "minABV": 1.3,
                             |                "maxABV": 3.4
                             |              }
                             |            ]
                             |          }
                             |        ]
                             |      }
                             |""".stripMargin.replace(" ", "").replace("\n", "")

      val result = Json.toJson(rateBand)

      result.toString() mustBe expectedResult
    }
  }

}
