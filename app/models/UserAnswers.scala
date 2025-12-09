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

import play.api.libs.json._
import queries.{Gettable, Settable}
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

import java.time.Instant
import scala.util.{Failure, Success, Try}

case class ReturnId(
  appaId: String,
  periodKey: String
)

object ReturnId {

  val reads: Reads[ReturnId] = {
    import play.api.libs.functional.syntax._

    (
      (__ \ "appaId").read[String] and
        (__ \ "periodKey").read[String]
    )(ReturnId.apply _)
  }

  val writes: OWrites[ReturnId] = {

    import play.api.libs.functional.syntax._
    (
      (__ \ "appaId").write[String] and
        (__ \ "periodKey").write[String]
    )(o => Tuple.fromProductTyped(o))
  }

  implicit val format: OFormat[ReturnId] = OFormat(reads, writes)
}

case class UserAnswers(
  returnId: ReturnId,
  groupId: String,
  internalId: String,
  regimes: AlcoholRegimes,
  data: JsObject = Json.obj(),
  startedTime: Instant,
  lastUpdated: Instant,
  validUntil: Option[Instant] = None
) {

  def get[A](page: Gettable[A])(implicit rds: Reads[A]): Option[A] =
    Reads.optionNoError(Reads.at(page.path)).reads(data).getOrElse(None)

  def set[A](page: Settable[A], value: A)(implicit writes: Writes[A]): Try[UserAnswers] = {

    val updatedData = data.setObject(page.path, Json.toJson(value)) match {
      case JsSuccess(jsValue, _) =>
        Success(jsValue)
      case JsError(errors)       =>
        Failure(JsResultException(errors))
    }

    updatedData.flatMap { d =>
      val updatedAnswers = copy(data = d)
      page.cleanup(Some(value), updatedAnswers)
    }
  }

  def remove[A](page: Settable[A]): Try[UserAnswers] = {

    val updatedData = data.removeObject(page.path) match {
      case JsSuccess(jsValue, _) =>
        Success(jsValue)
      case JsError(_)            =>
        Success(data)
    }

    updatedData.flatMap { d =>
      val updatedAnswers = copy(data = d)
      page.cleanup(None, updatedAnswers)
    }
  }

  def remove(pages: List[Settable[_]]): Try[UserAnswers] =
    pages.foldLeft(Try(this)) { (oldAnswerList, page) =>
      oldAnswerList.flatMap(_.remove(page))
    }

  def addToSeq[A](page: Settable[Seq[A]], value: A)(implicit writes: Writes[A], rds: Reads[A]): Try[UserAnswers] = {
    val path        = page.path
    val data        = get[Seq[A]](path)
    val updatedData = data match {
      case Some(valueSeq: Seq[A]) => set(path, valueSeq :+ value)
      case _                      => set(path, Seq(value))
    }
    updatedData.flatMap { d =>
      Try(copy(data = d))
    }
  }

  def addToSeqByKey[A, B](page: Settable[Map[A, Seq[B]]], key: A, value: B)(implicit
    writes: Writes[B],
    rds: Reads[B]
  ): Try[UserAnswers] = {
    val path        = page.path \ key.toString
    val data        = get[Seq[B]](path)
    val updatedData = data match {
      case Some(valueSeq: Seq[B]) => set(path, valueSeq :+ value)
      case _                      => set(path, Seq(value))
    }
    updatedData.flatMap { d =>
      Try(copy(data = d))
    }
  }

  def getByIndex[A](page: Gettable[Seq[A]], index: Int)(implicit rds: Reads[A]): Option[A] = {
    val path = page.path \ index
    get(path)
  }

  def get[A](path: JsPath)(implicit rds: Reads[A]): Option[A] =
    Reads.optionNoError(Reads.at(path)).reads(data).getOrElse(None)

  def set[A](path: JsPath, value: A)(implicit writes: Writes[A]): Try[JsObject] =
    data.setObject(path, Json.toJson(value)) match {
      case JsSuccess(jsValue, _) =>
        Success(jsValue)
      case JsError(errors)       =>
        Failure(JsResultException(errors))
    }

  def setByIndex[A](page: Settable[Seq[A]], index: Int, value: A)(implicit writes: Writes[A]): Try[UserAnswers] = {
    val path        = page.path \ index
    val updatedData = set(path, value)
    updatedData.flatMap { d =>
      val updatedAnswers = copy(data = d)
      page.cleanup(None, updatedAnswers)
    }
  }

  def removeBySeqIndex[A](page: Settable[Seq[A]], index: Int): Try[UserAnswers] = {
    val path        = page.path \ index
    val updatedData = remove(path)
    cleanupPage(page, updatedData)
  }

  def getByKey[A, B](page: Gettable[Map[A, B]], key: A)(implicit rds: Reads[B]): Option[B] = {
    val path = page.path \ key.toString
    get(path)
  }

  def getByKeyAndIndex[A, B](page: Gettable[Map[A, Seq[B]]], key: A, index: Int)(implicit rds: Reads[B]): Option[B] = {
    val path = page.path \ key.toString \ index
    get(path)
  }

  def setByKey[A, B](page: Settable[Map[A, B]], key: A, value: B)(implicit writes: Writes[B]): Try[UserAnswers] = {
    val path        = page.path \ key.toString
    val updatedData = set(path, value)
    cleanupPage(page, updatedData)
  }

  def setByKeyAndIndex[A, B](page: Settable[Map[A, Seq[B]]], key: A, value: B, index: Int)(implicit
    writes: Writes[B]
  ): Try[UserAnswers] = {
    val path        = page.path \ key.toString \ index
    val updatedData = set(path, value)
    cleanupPage(page, updatedData)
  }

  def removeByKey[A, B](page: Settable[Map[A, B]], key: A): Try[UserAnswers] = {
    val path        = page.path \ key.toString
    val updatedData = remove(path)
    cleanupPage(page, updatedData)
  }

  def removePagesByKey[A, B](pages: Seq[_ <: Settable[Map[A, B]]], key: A): Try[UserAnswers] =
    pages.foldLeft(Try(this)) { (oldAnswerList, page) =>
      oldAnswerList.flatMap(_.removeByKey(page, key))
    }

  def removeByKeyAndIndex[A, B](page: Settable[B], key: A, index: Int): Try[UserAnswers] = {
    val path        = page.path \ key.toString \ index
    val updatedData = remove(path)
    cleanupPage(page, updatedData)
  }

  def remove[A](path: JsPath): Try[JsObject] =
    data.removeObject(path) match {
      case JsSuccess(jsValue, _) =>
        Success(jsValue)
      case JsError(_)            =>
        Success(data)
    }

  def cleanupPage(page: Settable[_], updatedData: Try[JsObject]): Try[UserAnswers] =
    updatedData.flatMap { d =>
      val updatedAnswers = copy(data = d)
      page.cleanup(None, updatedAnswers)
    }
}

object UserAnswers {

  val reads: Reads[UserAnswers] = {

    import play.api.libs.functional.syntax._

    (
      (__ \ "_id").read[ReturnId] and
        (__ \ "groupId").read[String] and
        (__ \ "internalId").read[String] and
        AlcoholRegimes.alcoholRegimesFormat and
        (__ \ "data").read[JsObject] and
        (__ \ "startedTime").read(MongoJavatimeFormats.instantFormat) and
        (__ \ "lastUpdated").read(MongoJavatimeFormats.instantFormat) and
        (__ \ "validUntil").readNullable(MongoJavatimeFormats.instantFormat)
    )(UserAnswers.apply _)
  }

  val writes: OWrites[UserAnswers] = {

    import play.api.libs.functional.syntax._

    (
      (__ \ "_id").write[ReturnId] and
        (__ \ "groupId").write[String] and
        (__ \ "internalId").write[String] and
        AlcoholRegimes.alcoholRegimesFormat and
        (__ \ "data").write[JsObject] and
        (__ \ "startedTime").write(MongoJavatimeFormats.instantFormat) and
        (__ \ "lastUpdated").write(MongoJavatimeFormats.instantFormat) and
        (__ \ "validUntil").writeNullable(MongoJavatimeFormats.instantFormat)
    )(o => Tuple.fromProductTyped(o))
  }

  implicit val format: OFormat[UserAnswers] = OFormat(reads, writes)
}
