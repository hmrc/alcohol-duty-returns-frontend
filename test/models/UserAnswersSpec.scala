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

import generators.ModelGenerators
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.JsPath
import queries.{Gettable, Settable}

import scala.util.Success

class UserAnswersSpec extends AnyFreeSpec with Matchers with ModelGenerators {
  case object TestSeqPage extends Gettable[Seq[String]] with Settable[Seq[String]] {
    override def path: JsPath = JsPath \ toString
  }

  case object TestMapPage extends Gettable[Map[String, String]] with Settable[Map[String, String]] {
    override def path: JsPath = JsPath \ toString
  }

  "UserAnswer" - {
    val appaId: String        = appaIdGen.sample.get
    val periodKey: String     = periodKeyGen.sample.get
    val groupId: String       = "groupid"
    val userAnswersId: String = "id"

    "should add a value to a set for a given page and get the same value" in {

      val userAnswers = UserAnswers(ReturnId(appaId, periodKey), groupId, userAnswersId)

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

    "should remove a value for a given Page" in {
      val userAnswers =
        UserAnswers(ReturnId(appaId, periodKey), groupId, userAnswersId).set(TestSeqPage, Seq("123")).success.value

      val updatedUserAnswer = userAnswers.removeBySeqIndex(TestSeqPage, 0) match {
        case Success(ua) => ua
        case _           => fail()
      }
      val actualValueOption = updatedUserAnswer.getByIndex(TestSeqPage, 0)
      actualValueOption mustBe None
    }

    "map functionality" - {

      case object TestMapPage extends Gettable[Map[String, String]] with Settable[Map[String, String]] {
        override def path: JsPath = JsPath \ toString
      }

      "should get the value of an answer for a given page and index" in {
        val userAnswers = UserAnswers(ReturnId(appaId, periodKey), groupId, userAnswersId)
          .set(TestMapPage, Map("1" -> "123", "2" -> "456", "3" -> "789"))
          .success
          .value
        userAnswers.getByKey(TestMapPage, "1") mustBe Some("123")
        userAnswers.getByKey(TestMapPage, "2") mustBe Some("456")
        userAnswers.getByKey(TestMapPage, "3") mustBe Some("789")
        userAnswers.getByKey(TestMapPage, "4") mustBe None
      }

      "should set the value of an answer for a given page and index and get the same value" in {
        val userAnswers = UserAnswers(ReturnId(appaId, periodKey), groupId, userAnswersId)

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

    }
  }
}
