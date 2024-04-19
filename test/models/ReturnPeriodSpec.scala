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

import java.time.YearMonth

class ReturnPeriodSpec extends SpecBase {
  "ReturnPeriod" - {
    "when apply is called" - {
      "should construct a correct ReturnPeriod" in {
        ReturnPeriod.apply("24AE", 2024, 5).yearMonth mustBe YearMonth.of(2024, 5)
        ReturnPeriod.apply("24AE", 2024, 5).periodKey mustBe "24AE"
      }
    }

    "should return the correct view string" - {
      "for the first date of the period" in {
        ReturnPeriod.apply("24AE", 2024, 5).firstDateViewString() mustBe "1 May 2024"
      }

      "for the last date of the period" in {
        ReturnPeriod.apply("24AE", 2024, 5).lastDateViewString() mustBe "31 May 2024"
      }
    }

    "should convert from period key" - {
      "returning an error if" - {
        "the key is more than 4 characters" in {
          ReturnPeriod.fromPeriodKey("24AC1").isLeft mustBe true
        }

        "the key is less than 4 characters" in {
          ReturnPeriod.fromPeriodKey("24A").isLeft mustBe true
        }

        "the key is empty" in {
          ReturnPeriod.fromPeriodKey("").isLeft mustBe true
        }

        "the first character is not a digit" in {
          ReturnPeriod.fromPeriodKey("/4AC").isLeft mustBe true
          ReturnPeriod.fromPeriodKey(":4AC").isLeft mustBe true
          ReturnPeriod.fromPeriodKey("A4AC").isLeft mustBe true
        }

        "the second character is not a digit" in {
          ReturnPeriod.fromPeriodKey("2/AC").isLeft mustBe true
          ReturnPeriod.fromPeriodKey("2:AC").isLeft mustBe true
          ReturnPeriod.fromPeriodKey("2AAC").isLeft mustBe true
        }

        "the third character is not an A" in {
          ReturnPeriod.fromPeriodKey("24BC").isLeft mustBe true
          ReturnPeriod.fromPeriodKey("244C").isLeft mustBe true
          ReturnPeriod.fromPeriodKey("24aC").isLeft mustBe true
        }

        "the fourth character is not a A-L" in {
          ReturnPeriod.fromPeriodKey("24A@").isLeft mustBe true
          ReturnPeriod.fromPeriodKey("24AM").isLeft mustBe true
          ReturnPeriod.fromPeriodKey("24Aa").isLeft mustBe true
          ReturnPeriod.fromPeriodKey("24A9").isLeft mustBe true
        }
      }

      "return a correct ReturnPeriod when" - {
        "a valid period key is passed" in {
          ReturnPeriod.fromPeriodKey("24AA") mustBe Right(ReturnPeriod("24AA", 2024, 1))
          ReturnPeriod.fromPeriodKey("24AL") mustBe Right(ReturnPeriod("24AL", 2024, 12))
          ReturnPeriod.fromPeriodKey("28AC") mustBe Right(ReturnPeriod("28AC", 2028, 3))
        }
      }
    }
  }
}
