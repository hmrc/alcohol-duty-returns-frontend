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
import enumeratum.{Enum, EnumEntry, PlayEnum, PlayJsonEnum}
import play.api.libs.json._

import java.time.YearMonth
import scala.util.{Failure, Success, Try}

sealed trait RateType extends EnumEntry {
  val isSPR: Boolean
  val isDraught: Boolean
}

object RateType extends Enum[RateType] with PlayJsonEnum[RateType] {
  val values = findValues

  case object Core extends RateType {
    val isSPR     = false
    val isDraught = false
  }

  case object DraughtRelief extends RateType {
    val isSPR     = false
    val isDraught = true
  }

  case object SmallProducerRelief extends RateType {
    val isSPR     = true
    val isDraught = false
  }

  case object DraughtAndSmallProducerRelief extends RateType {
    val isSPR     = true
    val isDraught = true
  }

  def apply(hasDraughtRelief: Boolean, hasSmallProducerRelief: Boolean): RateType =
    (hasDraughtRelief, hasSmallProducerRelief) match {
      case (true, true)  => RateType.DraughtAndSmallProducerRelief
      case (true, false) => RateType.DraughtRelief
      case (false, true) => RateType.SmallProducerRelief
      case _             => RateType.Core
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
}

sealed trait AlcoholType extends EnumEntry
object AlcoholType extends Enum[AlcoholType] with PlayEnum[AlcoholType] {
  val values = findValues

  case object Beer extends AlcoholType
  case object Cider extends AlcoholType
  case object SparklingCider extends AlcoholType
  case object Wine extends AlcoholType
  case object Spirits extends AlcoholType
  case object OtherFermentedProduct extends AlcoholType

  def fromAlcoholRegime(regime: AlcoholRegime): AlcoholType =
    regime match {
      case AlcoholRegime.Beer                  => Beer
      case AlcoholRegime.Cider                 => Cider
      case AlcoholRegime.Wine                  => Wine
      case AlcoholRegime.Spirits               => Spirits
      case AlcoholRegime.OtherFermentedProduct => OtherFermentedProduct
    }
}

case class ABVRange(alcoholType: AlcoholType, minABV: AlcoholByVolume, maxABV: AlcoholByVolume)

object ABVRange {
  implicit val format: Format[ABVRange] = Json.format[ABVRange]
}

case class RangeDetailsByRegime(alcoholRegime: AlcoholRegime, abvRanges: NonEmptySeq[ABVRange])

object RangeDetailsByRegime {
  implicit val nonEmptySeqFormat: Format[NonEmptySeq[ABVRange]] = new Format[NonEmptySeq[ABVRange]] {
    override def reads(json: JsValue): JsResult[NonEmptySeq[ABVRange]] =
      json.validate[Seq[ABVRange]].flatMap {
        case head +: tail => JsSuccess(NonEmptySeq.fromSeqUnsafe(Seq(head) ++ tail))
        case _            => JsError("Empty sequence")
      }

    override def writes(o: NonEmptySeq[ABVRange]): JsValue = Writes.seq(ABVRange.format).writes(o.toList)
  }

  implicit val format: Format[RangeDetailsByRegime] = Json.format[RangeDetailsByRegime]
}

case class RateBand(
  taxTypeCode: String,
  description: String,
  rateType: RateType,
  rate: Option[BigDecimal],
  rangeDetails: Set[RangeDetailsByRegime]
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
