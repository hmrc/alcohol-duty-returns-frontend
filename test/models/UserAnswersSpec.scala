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
import play.api.libs.json.{JsPath, Json}
import queries.{Gettable, Settable}

import scala.util.Success

class UserAnswersSpec extends SpecBase {
  case object TestSeqPage extends Gettable[Seq[String]] with Settable[Seq[String]] {
    override def path: JsPath = JsPath \ toString
  }

  case object TestMapPage extends Gettable[Map[String, String]] with Settable[Map[String, String]] {
    override def path: JsPath = JsPath \ toString
  }

  case object TestMapSeqPage extends Gettable[Map[String, Seq[String]]] with Settable[Map[String, Seq[String]]] {
    override def path: JsPath = JsPath \ toString
  }

  "UserAnswer" - {
    val json          =
      s"""{"_id":{"appaId":"$appaId","periodKey":"$periodKey"},"groupId":"$groupId","internalId":"$internalId","regimes":["Spirits","Wine","Cider","OtherFermentedProduct","Beer"],"data":{},"startedTime":{"$$date":{"$$numberLong":"1718118467838"}},"lastUpdated":{"$$date":{"$$numberLong":"1718118467838"}}}"""
    val noRegimesJson =
      s"""{"_id":{"appaId":"$appaId","periodKey":"$periodKey"},"groupId":"$groupId","internalId":"$internalId","regimes":[],"data":{},"startedTime":{"$$date":{"$$numberLong":"1718118467838"}},"lastUpdated":{"$$date":{"$$numberLong":"1718118467838"}}}"""

    "must add a value to a set for a given page and get the same value" in {

      val userAnswers = emptyUserAnswers

      val expectedValue = "value"

      val updatedUserAnswer = userAnswers.addToSeq(TestSeqPage, expectedValue) match {
        case Success(value) => value
        case _              => fail()
      }

      val actualValue = updatedUserAnswer.getByIndex(TestSeqPage, 0) match {
        case Some(value) => value
        case _           => fail()
      }

      expectedValue mustBe actualValue
    }

    "must remove a value for a given Page" in {
      val userAnswers = emptyUserAnswers
        .set(TestSeqPage, Seq("123"))
        .success
        .value

      val updatedUserAnswer = userAnswers.removeBySeqIndex(TestSeqPage, 0) match {
        case Success(ua) => ua
        case _           => fail()
      }
      val actualValueOption = updatedUserAnswer.getByIndex(TestSeqPage, 0)
      actualValueOption mustBe None
    }

    "must serialise to json" in {
      Json.toJson(emptyUserAnswers).toString() mustBe json
    }

    "must deserialise from json" in {
      Json.parse(json).as[UserAnswers] mustBe emptyUserAnswers
    }

    "must throw an error if no regimes found" in {
      an[IllegalArgumentException] mustBe thrownBy(Json.parse(noRegimesJson).as[UserAnswers])
    }

    "map functionality" - {

      case object TestMapPage extends Gettable[Map[String, String]] with Settable[Map[String, String]] {
        override def path: JsPath = JsPath \ toString
      }

      "must get the value of an answer for a given page and index" in {
        val userAnswers = emptyUserAnswers
          .set(TestMapPage, Map("1" -> "123", "2" -> "456", "3" -> "789"))
          .success
          .value
        userAnswers.getByKey(TestMapPage, "1") mustBe Some("123")
        userAnswers.getByKey(TestMapPage, "2") mustBe Some("456")
        userAnswers.getByKey(TestMapPage, "3") mustBe Some("789")
        userAnswers.getByKey(TestMapPage, "4") mustBe None
      }

      "must set the value of an answer for a given page and index and get the same value" in {
        val userAnswers = emptyUserAnswers

        val expectedValue = "value"

        val updatedUserAnswer = userAnswers.setByKey(TestMapPage, "1", expectedValue) match {
          case Success(value) => value
          case _              => fail()
        }

        val actualValue = updatedUserAnswer.getByKey(TestMapPage, "1") match {
          case Some(value) => value
          case _           => fail()
        }

        expectedValue mustBe actualValue
      }

      "must return None when value is not present" in {
        val userAnswers = emptyUserAnswers

        val result = userAnswers.get(TestSeqPage)
        result mustBe None
      }

      "must add value to existing sequence" in {
        val userAnswers = emptyUserAnswers
          .set(TestSeqPage, Seq("existingValue"))
          .success
          .value

        val updatedUserAnswer = userAnswers.addToSeq(TestSeqPage, "newValue") match {
          case Success(value) => value
          case _              => fail()
        }

        updatedUserAnswer.get(TestSeqPage) mustBe Some(Seq("existingValue", "newValue"))
      }

      "must return None when no value exists at given index" in {
        val userAnswers = emptyUserAnswers
          .set(TestSeqPage, Seq("value1", "value2"))
          .success
          .value

        val result = userAnswers.getByIndex(TestSeqPage, 2)
        result mustBe None
      }
    }

    "must add a value to a sequence by key and append another value" in {
      val userAnswers = emptyUserAnswers

      val updatedUserAnswer1 = userAnswers.addToSeqByKey(TestMapSeqPage, "1", "value1") match {
        case Success(value) => value
        case _              => fail()
      }

      val actualValue1 = updatedUserAnswer1.getByKeyAndIndex(TestMapSeqPage, "1", 0) match {
        case Some(value) => value
        case _           => fail("First value not found")
      }
      actualValue1 mustBe "value1"

      val updatedUserAnswer2 = updatedUserAnswer1.addToSeqByKey(TestMapSeqPage, "1", "value2") match {
        case Success(value) => value
        case _              => fail()
      }

      val actualValue2 = updatedUserAnswer2.getByKeyAndIndex(TestMapSeqPage, "1", 1) match {
        case Some(value) => value
        case _           => fail("Second value not found")
      }

      actualValue1 mustBe "value1"
      actualValue2 mustBe "value2"
    }
  }
}
