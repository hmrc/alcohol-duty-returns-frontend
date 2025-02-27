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

package utils

import base.SpecBase
import utils.ListHelpers._

class ListHelpersSpec extends SpecBase {
  "ListHelper" - {
    "calling nextItem" - {
      "must return None if the List is empty" in {
        List.empty[Int].nextItem(1) mustBe None
      }

      "must return None if the item is not found" in {
        List(1, 2, 3).nextItem(4) mustBe None
      }

      "must return None if the item is the last" in {
        List(1, 2, 3).nextItem(3) mustBe None
      }

      "must return the second item if the item is the first" in {
        List(1, 2, 3).nextItem(1) mustBe Some(2)
      }

      "must return the third item if the item is the second" in {
        List(1, 2, 3).nextItem(2) mustBe Some(3)
      }
    }
  }
}
