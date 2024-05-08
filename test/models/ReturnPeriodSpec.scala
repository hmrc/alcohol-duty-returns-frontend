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

package models

import base.SpecBase
import play.api.libs.json.{JsString, Json}

import java.time.YearMonth

class ReturnPeriodSpec extends SpecBase {
  "ReturnPeriod" - {
    "should convert from period key" - {
      "returning an error if" - {
        "the key is more than 4 characters" in {
          ReturnPeriod.fromPeriodKey("24AC1") mustBe None
        }

        "the key is less than 4 characters" in {
          ReturnPeriod.fromPeriodKey("24A") mustBe None
        }

        "the key is empty" in {
          ReturnPeriod.fromPeriodKey("") mustBe None
        }

        "the first character is not a digit" in {
          ReturnPeriod.fromPeriodKey("/4AC") mustBe None
          ReturnPeriod.fromPeriodKey(":4AC") mustBe None
          ReturnPeriod.fromPeriodKey("A4AC") mustBe None
        }

        "the second character is not a digit" in {
          ReturnPeriod.fromPeriodKey("2/AC") mustBe None
          ReturnPeriod.fromPeriodKey("2:AC") mustBe None
          ReturnPeriod.fromPeriodKey("2AAC") mustBe None
        }

        "the third character is not an A" in {
          ReturnPeriod.fromPeriodKey("24BC") mustBe None
          ReturnPeriod.fromPeriodKey("244C") mustBe None
          ReturnPeriod.fromPeriodKey("24aC") mustBe None
        }

        "the fourth character is not a A-L" in {
          ReturnPeriod.fromPeriodKey("24A@") mustBe None
          ReturnPeriod.fromPeriodKey("24AM") mustBe None
          ReturnPeriod.fromPeriodKey("24Aa") mustBe None
          ReturnPeriod.fromPeriodKey("24A9") mustBe None
        }
      }

      "return a correct ReturnPeriod when" - {
        "a valid period key is passed" in {
          ReturnPeriod.fromPeriodKey("24AA") mustBe Some(ReturnPeriod(YearMonth.of(2024, 1)))
          ReturnPeriod.fromPeriodKey("24AL") mustBe Some(ReturnPeriod(YearMonth.of(2024, 12)))
          ReturnPeriod.fromPeriodKey("28AC") mustBe Some(ReturnPeriod(YearMonth.of(2028, 3)))
        }
      }
    }

    "should parse ReturnPeriod with the right json value" in {
      val returnPeriod = ReturnPeriod(YearMonth.of(2024, 1))
      val result       = Json.toJson(returnPeriod)
      result mustBe JsString("24AA")
    }

    "should transform a valid Period Key json string into a Return Period" in {
      val periodKey         = periodKeyGen.sample.get
      val periodKeyJsString = JsString(periodKey)
      val result            = periodKeyJsString.as[ReturnPeriod]
      result.toPeriodKey mustBe periodKey
    }

    "should throw an Illegal Argument exception when an invalid period key json string is parsed" in {
      val invalidPeriodKey  = invalidPeriodKeyGen.sample.get
      val periodKeyJsString = JsString(invalidPeriodKey)

      val exception = intercept[IllegalArgumentException](
        periodKeyJsString.as[ReturnPeriod]
      )

      exception mustBe a[IllegalArgumentException]
    }
  }
}
