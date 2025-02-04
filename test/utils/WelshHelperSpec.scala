/*
 * Copyright 2025 HM Revenue & Customs
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

package utils

import base.SpecBase

class WelshHelperSpec extends SpecBase {
  "WelshHelper" - {
    "when calling chooseAnd" - {
      Seq(
        (1, false),
        (2, true),
        (3, true),
        (4, true),
        (5, true),
        (6, true),
        (7, true),
        (8, false),
        (9, true),
        (10, true)
      ).foreach { case (number, startsConsonantWelsh) =>
        s"must${if (startsConsonantWelsh) {
          ""
        } else {
          "n't"
        }} indicate and $number should be written with a rather than ac" in {
          val expectedAndMessageKey = if (startsConsonantWelsh) {
            "welsh.and.consonant"
          } else {
            "welsh.and.vowel"
          }
          WelshHelper.chooseAnd(BigDecimal(number)) mustBe expectedAndMessageKey
        }
      }

      "must work even has decimal places" in {
        WelshHelper.chooseAnd(BigDecimal(7.42)) mustBe "welsh.and.consonant"
        WelshHelper.chooseAnd(BigDecimal(8.42)) mustBe "welsh.and.vowel"
      }

      "must handle several digits in the whole part" in { // Note we don't differentiate 11, 16, 17 right now
        WelshHelper.chooseAnd(BigDecimal(17)) mustBe "welsh.and.consonant"
      }

      "must handle scientific notation" in { // Note we don't differentiate 11, 16, 17 right now
        WelshHelper.chooseAnd(BigDecimal("8E-1")) mustBe "welsh.and.consonant"
        WelshHelper.chooseAnd(BigDecimal("8E0")) mustBe "welsh.and.vowel"
        WelshHelper.chooseAnd(BigDecimal("8E1")) mustBe "welsh.and.consonant"
      }
    }
  }
}
