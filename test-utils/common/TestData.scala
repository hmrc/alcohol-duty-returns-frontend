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

package common

import generators.ModelGenerators
import models.AlcoholRegime.{Beer, Cider, OtherFermentedProduct, Spirits, Wine}
import models.{ReturnId, UserAnswers}
import play.api.libs.json.{JsObject, Json}

trait TestData extends ModelGenerators {
  val appaId: String     = appaIdGen.sample.get
  val periodKey: String  = periodKeyGen.sample.get
  val groupId: String    = "groupid"
  val internalId: String = "id"
  val returnId: ReturnId = ReturnId(appaId, periodKey)

  val periodKeyJan = "24AA"
  val periodKeyFeb = "24AB"
  val periodKeyMar = "24AC"
  val periodKeyApr = "24AD"
  val periodKeyMay = "24AE"
  val periodKeyJun = "24AF"
  val periodKeyJul = "24AG"
  val periodKeyAug = "24AH"
  val periodKeySep = "24AI"
  val periodKeyOct = "24AJ"
  val periodKeyNov = "24AK"
  val periodKeyDec = "24AL"

  val quarterPeriodKeys    = Seq(periodKeyMar, periodKeyJun, periodKeySep, periodKeyDec)
  val nonQuarterPeriodKeys =
    Seq(periodKeyJan, periodKeyFeb, periodKeyApr, periodKeyMay, periodKeyJul, periodKeyAug, periodKeyOct, periodKeyNov)

  val emptyUserAnswers: UserAnswers = UserAnswers(returnId, groupId, internalId)

  val userAnswersWithBeer: UserAnswers                     =
    emptyUserAnswers.copy(data = JsObject(Seq("alcoholRegime" -> Json.toJson(Set(Beer)))))
  val userAnswersWithoutBeer: UserAnswers                  = UserAnswers(returnId, groupId, internalId).copy(data =
    JsObject(Seq("alcoholRegime" -> Json.toJson(Set(Cider, Wine, Spirits, OtherFermentedProduct))))
  )
  val userAnswersWithCider: UserAnswers                    =
    emptyUserAnswers.copy(data = JsObject(Seq("alcoholRegime" -> Json.toJson(Set(Cider)))))
  val userAnswersWithoutCider: UserAnswers                 = UserAnswers(returnId, groupId, internalId).copy(data =
    JsObject(Seq("alcoholRegime" -> Json.toJson(Set(Beer, Wine, Spirits, OtherFermentedProduct))))
  )
  val userAnswersWithWine: UserAnswers                     =
    emptyUserAnswers.copy(data = JsObject(Seq("alcoholRegime" -> Json.toJson(Set(Wine)))))
  val userAnswersWithoutWine: UserAnswers                  = UserAnswers(returnId, groupId, internalId).copy(data =
    JsObject(Seq("alcoholRegime" -> Json.toJson(Set(Beer, Cider, Spirits, OtherFermentedProduct))))
  )
  val userAnswersWithSpirits: UserAnswers                  =
    emptyUserAnswers.copy(data = JsObject(Seq("alcoholRegime" -> Json.toJson(Set(Spirits)))))
  val userAnswersWithoutSpirits: UserAnswers               = UserAnswers(returnId, groupId, internalId).copy(data =
    JsObject(Seq("alcoholRegime" -> Json.toJson(Set(Beer, Cider, Wine, OtherFermentedProduct))))
  )
  val userAnswersWithOtherFermentedProduct: UserAnswers    =
    emptyUserAnswers.copy(data = JsObject(Seq("alcoholRegime" -> Json.toJson(Set(OtherFermentedProduct)))))
  val userAnswersWithoutOtherFermentedProduct: UserAnswers = UserAnswers(returnId, groupId, internalId).copy(data =
    JsObject(Seq("alcoholRegime" -> Json.toJson(Set(Beer, Spirits))))
  )

  val userAnswersWithAllRegimes: UserAnswers =
    emptyUserAnswers.copy(data =
      JsObject(Seq("alcoholRegime" -> Json.toJson(Set(Beer, Cider, Wine, Spirits, OtherFermentedProduct))))
    )
}
