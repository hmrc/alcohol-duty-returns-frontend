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

package models.binders

import java.time.{Instant, LocalDate}
import java.time.format.{DateTimeFormatter, DateTimeParseException}
import play.api.mvc.QueryStringBindable

import scala.util.Try

object Binders {

  implicit def localDateQueryStringBinder(implicit stringBinder: QueryStringBindable[String]): QueryStringBindable[LocalDate] = new QueryStringBindable[LocalDate] {
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, LocalDate]] = {
      stringBinder.bind(key, params).map {
        case Right(dateString) =>
          try {
            Right(LocalDate.parse(dateString, formatter))
          } catch {
            case e: Exception => Left(s"Cannot parse parameter $key as LocalDate: ${e.getMessage}")
          }
        case Left(error) => Left(error)
      }
    }

    override def unbind(key: String, value: LocalDate): String = {
      stringBinder.unbind(key, value.format(formatter))
    }
  }

  implicit def instantQueryStringBinder(implicit stringBinder: QueryStringBindable[String]): QueryStringBindable[Instant] = new QueryStringBindable[Instant] {

    override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, Instant]] = {
      stringBinder.bind(key, params).map {
        case Right(instantString) =>
          try {
            Right(Instant.parse(instantString))
          } catch {
            case e: DateTimeParseException => Left(s"Cannot parse parameter $key as Instant: ${e.getMessage}")
          }
        case Left(error) => Left(error)
      }
    }

    override def unbind(key: String, value: Instant): String = {
      stringBinder.unbind(key, value.toString)
    }
  }

  implicit def bigDecimalQueryStringBinder(implicit stringBinder: QueryStringBindable[String]): QueryStringBindable[BigDecimal] = new QueryStringBindable[BigDecimal] {

    override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, BigDecimal]] = {
      stringBinder.bind(key, params).map {
        case Right(bigDecimalString) =>
          Try(BigDecimal(bigDecimalString)).toEither.left.map(_.getMessage)
        case Left(error) => Left(error)
      }
    }

    override def unbind(key: String, value: BigDecimal): String = {
      stringBinder.unbind(key, value.toString())
    }
  }
}

