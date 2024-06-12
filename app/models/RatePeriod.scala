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

import cats.data._
import cats.implicits._
import enumeratum.{Enum, EnumEntry, PlayEnum}
import play.api.libs.json._

import java.time.YearMonth
import scala.util.{Failure, Success, Try}

sealed trait RateType

object RateType {

  case object Core extends RateType
  case object DraughtRelief extends RateType
  case object SmallProducerRelief extends RateType
  case object DraughtAndSmallProducerRelief extends RateType

  def apply(hasDraughtRelief: Boolean, hasSmallProducerRelief: Boolean): RateType =
    (hasDraughtRelief, hasSmallProducerRelief) match {
      case (true, true)  => RateType.DraughtAndSmallProducerRelief
      case (true, false) => RateType.DraughtRelief
      case (false, true) => RateType.SmallProducerRelief
      case _             => RateType.Core
    }

  implicit val format: Format[RateType] = new Format[RateType] {
    override def reads(json: JsValue): JsResult[RateType] = json.validate[String] match {
      case JsSuccess(value, _) =>
        value match {
          case "Core"                          => JsSuccess(Core)
          case "DraughtRelief"                 => JsSuccess(DraughtRelief)
          case "SmallProducerRelief"           => JsSuccess(SmallProducerRelief)
          case "DraughtAndSmallProducerRelief" => JsSuccess(DraughtAndSmallProducerRelief)
          case s                               => JsError(s"$s is not a valid RateType")
        }
      case e: JsError          => e
    }

    override def writes(o: RateType): JsValue = JsString(o.toString)
  }
}
case class RateTypeResponse(rateType: RateType)

object RateTypeResponse {
  implicit val format: Format[RateTypeResponse] = Json.format[RateTypeResponse]
}

sealed trait AlcoholRegimeName extends EnumEntry
object AlcoholRegimeName extends Enum[AlcoholRegimeName] with PlayEnum[AlcoholRegimeName] {
  val values = findValues

  case object Beer extends AlcoholRegimeName
  case object Cider extends AlcoholRegimeName
  case object Wine extends AlcoholRegimeName
  case object Spirits extends AlcoholRegimeName
  case object OtherFermentedProduct extends AlcoholRegimeName

  implicit val format: Format[AlcoholRegimeName] = new Format[AlcoholRegimeName] {
    override def reads(json: JsValue): JsResult[AlcoholRegimeName] = json.validate[String] match {
      case JsSuccess(value, _) =>
        value match {
          case "Beer"                  => JsSuccess(Beer)
          case "Cider"                 => JsSuccess(Cider)
          case "Wine"                  => JsSuccess(Wine)
          case "Spirits"               => JsSuccess(Spirits)
          case "OtherFermentedProduct" => JsSuccess(OtherFermentedProduct)
          case s                       => JsError(s"$s is not a valid AlcoholRegime")
        }
      case e: JsError          => e
    }

    override def writes(o: AlcoholRegimeName): JsValue = JsString(o.toString)
  }

  def fromString(str: String): Option[AlcoholRegimeName] =
    str match {
      case "Beer"                  => Some(Beer)
      case "Cider"                 => Some(Cider)
      case "Wine"                  => Some(Wine)
      case "Spirits"               => Some(Spirits)
      case "OtherFermentedProduct" => Some(OtherFermentedProduct)
      case _                       => None
    }
}

case class AlcoholByVolume private (value: BigDecimal) {
  require(value >= 0 && value <= 100, "Percentage must be between 0 and 100")
}

object AlcoholByVolume {
  def apply(value: BigDecimal): AlcoholByVolume = {
    require(value.scale <= 1, "Alcohol By Volume must have maximum 1 decimal place")
    new AlcoholByVolume(value)
  }

  implicit val format: Format[AlcoholByVolume] = new Format[AlcoholByVolume] {
    override def reads(json: JsValue): JsResult[AlcoholByVolume] = json.validate[BigDecimal] match {
      case JsSuccess(value, _) =>
        Try(AlcoholByVolume(value)) match {
          case Success(v)         => JsSuccess(v)
          case Failure(exception) => JsError(s"$value is not a valid Alcohol By Volume value. Failed with: $exception")
        }
      case e: JsError          => e
    }

    override def writes(o: AlcoholByVolume): JsValue = JsNumber(o.value)
  }

  val MAX: AlcoholByVolume = AlcoholByVolume(100)
}

sealed trait ABVRangeName extends EnumEntry
object ABVRangeName extends Enum[ABVRangeName] with PlayEnum[ABVRangeName] {
  val values = findValues

  case object Beer extends ABVRangeName
  case object Cider extends ABVRangeName
  case object SparklingCider extends ABVRangeName
  case object Wine extends ABVRangeName
  case object Spirits extends ABVRangeName
  case object OtherFermentedProduct extends ABVRangeName
}

case class ABVRange(name: ABVRangeName, minABV: AlcoholByVolume, maxABV: AlcoholByVolume)

object ABVRange {
  implicit val format: Format[ABVRange] = Json.format[ABVRange]
}

case class AlcoholRegime(name: AlcoholRegimeName, abvRanges: NonEmptySeq[ABVRange])

object AlcoholRegime {
  implicit val nonEmptySeqFormat: Format[NonEmptySeq[ABVRange]] = new Format[NonEmptySeq[ABVRange]] {
    override def reads(json: JsValue): JsResult[NonEmptySeq[ABVRange]] =
      json.validate[Seq[ABVRange]].flatMap {
        case head +: tail => JsSuccess(NonEmptySeq.fromSeqUnsafe(Seq(head) ++ tail))
        case _            => JsError("Empty sequence")
      }

    override def writes(o: NonEmptySeq[ABVRange]): JsValue = Writes.seq(ABVRange.format).writes(o.toList)
  }

  implicit val format: Format[AlcoholRegime] = Json.format[AlcoholRegime]
}

case class RateBand(
  taxType: String,
  description: String,
  rateType: RateType,
  alcoholRegimes: Set[AlcoholRegime],
  rate: Option[BigDecimal]
)

object RateBand {
  implicit val formats: Format[RateBand] = Json.format[RateBand]
}

case class RatePeriod(
  name: String,
  isLatest: Boolean,
  validityStartDate: YearMonth,
  validityEndDate: Option[YearMonth],
  rateBands: Seq[RateBand]
)

object RatePeriod {

  implicit val yearMonthFormat: Format[YearMonth] = new Format[YearMonth] {
    override def reads(json: JsValue): JsResult[YearMonth] =
      json.validate[String].map(YearMonth.parse)

    override def writes(yearMonth: YearMonth): JsValue =
      JsString(yearMonth.toString)
  }

  implicit val optionYearMonthFormat: Format[Option[YearMonth]] = new Format[Option[YearMonth]] {
    override def reads(json: JsValue): JsResult[Option[YearMonth]] =
      json.validateOpt[YearMonth](yearMonthFormat)

    override def writes(o: Option[YearMonth]): JsValue =
      o.map(yearMonthFormat.writes).getOrElse(JsNull)
  }
  implicit val formats: OFormat[RatePeriod]                     = Json.format[RatePeriod]
}
