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

package pages.dutySuspendedNew

import models.AlcoholRegime
import models.AlcoholRegime.{Beer, Cider}
import pages.behaviours.PageBehaviours
import play.api.libs.json.Json

class DutySuspendedAlcoholTypePageSpec extends PageBehaviours {
  "DutySuspendedAlcoholTypePage must" - {

    beRetrievable[Set[AlcoholRegime]](DutySuspendedAlcoholTypePage)

    beSettable[Set[AlcoholRegime]](DutySuspendedAlcoholTypePage)

    beRemovable[Set[AlcoholRegime]](DutySuspendedAlcoholTypePage)

    "for regimes removed from previous answers, clear data from subsequent pages" in {
      val alcoholRegimesSelected: Set[AlcoholRegime] = Set(Beer)

      val userAnswers = emptyUserAnswers.copy(data =
        Json.obj(
          DeclareDutySuspenseQuestionPage.toString -> true,
          DutySuspendedAlcoholTypePage.toString    -> Json.arr("Beer", "Cider"),
          DutySuspendedQuantitiesPage.toString     -> Json.obj(
            "Beer"  -> Json.obj(
              "quantitiesByDutySuspendedCategory" -> Json.arr(
                Json.obj(
                  "category"    -> "deliveredInsideUK",
                  "totalLitres" -> 100,
                  "pureAlcohol" -> 10
                ),
                Json.obj(
                  "category"    -> "deliveredOutsideUK",
                  "totalLitres" -> 0,
                  "pureAlcohol" -> 0
                ),
                Json.obj(
                  "category"    -> "received",
                  "totalLitres" -> 0,
                  "pureAlcohol" -> 0
                )
              )
            ),
            "Cider" -> Json.obj(
              "quantitiesByDutySuspendedCategory" -> Json.arr(
                Json.obj(
                  "category"    -> "deliveredInsideUK",
                  "totalLitres" -> 100,
                  "pureAlcohol" -> 10
                ),
                Json.obj(
                  "category"    -> "deliveredOutsideUK",
                  "totalLitres" -> 0,
                  "pureAlcohol" -> 0
                ),
                Json.obj(
                  "category"    -> "received",
                  "totalLitres" -> 0,
                  "pureAlcohol" -> 0
                )
              )
            )
          ),
          DutySuspendedFinalVolumesPage.toString   -> Json.obj(
            "Beer"  -> Json.obj(
              "totalLitres" -> 100,
              "pureAlcohol" -> 10
            ),
            "Cider" -> Json.obj(
              "totalLitres" -> 100,
              "pureAlcohol" -> 10
            )
          )
        )
      )

      val updatedAnswers = userAnswers.set(DutySuspendedAlcoholTypePage, alcoholRegimesSelected).success.value

      updatedAnswers.getByKey(DutySuspendedQuantitiesPage, Beer)   mustNot be(empty)
      updatedAnswers.getByKey(DutySuspendedFinalVolumesPage, Beer) mustNot be(empty)

      updatedAnswers.getByKey(DutySuspendedQuantitiesPage, Cider)   must be(empty)
      updatedAnswers.getByKey(DutySuspendedFinalVolumesPage, Cider) must be(empty)
    }

//    "not cleanup subsequent answers from the journey when true is selected" in {
//      val alcoholRegimesSet: Set[AlcoholRegime] = Set(Beer, Cider, Wine)
//
//      val userAnswers    = emptyUserAnswers.set(DutySuspendedAlcoholTypePage, alcoholRegimesSet).success.value
//      val updatedAnswers = userAnswers.set(DeclareDutySuspenseQuestionPage, true).success.value
//
//      updatedAnswers.get(DutySuspendedAlcoholTypePage) mustNot be(empty)
//    }
  }
}
