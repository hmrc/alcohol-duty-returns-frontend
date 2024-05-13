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

//import play.api.libs.json.{Format, JsError, JsResult, JsString, JsSuccess, JsValue}
//
//sealed trait AlcoholRegime
//
//object AlcoholRegime {
//  case object Beer extends AlcoholRegime
//  case object Cider extends AlcoholRegime
//  case object Wine extends AlcoholRegime
//  case object Spirits extends AlcoholRegime
//  case object OtherFermentedProduct extends AlcoholRegime
//
//  implicit val format: Format[AlcoholRegime] = new Format[AlcoholRegime] {
//    override def reads(json: JsValue): JsResult[AlcoholRegime] = json.validate[String] match {
//      case JsSuccess(value, _) =>
//        value match {
//          case "Beer"                  => JsSuccess(Beer)
//          case "Cider"                 => JsSuccess(Cider)
//          case "Wine"                  => JsSuccess(Wine)
//          case "Spirits"               => JsSuccess(Spirits)
//          case "OtherFermentedProduct" => JsSuccess(OtherFermentedProduct)
//          case s                       => JsError(s"$s is not a valid AlcoholRegime")
//        }
//      case e: JsError          => e
//    }
//
//    override def writes(o: AlcoholRegime): JsValue = JsString(o.toString)
//  }
//
//  def fromString(str: String): Option[AlcoholRegime] =
//    str match {
//      case "Beer"                  => Some(Beer)
//      case "Cider"                 => Some(Cider)
//      case "Wine"                  => Some(Wine)
//      case "Spirits"               => Some(Spirits)
//      case "OtherFermentedProduct" => Some(OtherFermentedProduct)
//      case _                       => None
//    }
//}

//import enumeratum.{Enum, EnumEntry, PlayEnum}
//
//sealed trait AlcoholRegime extends EnumEntry
//
//object AlcoholRegime extends Enum[AlcoholRegime] with PlayEnum[AlcoholRegime] {
//  val values = findValues
//
//  case object Beer extends AlcoholRegime
//  case object Cider extends AlcoholRegime
//  case object Wine extends AlcoholRegime
//  case object Spirits extends AlcoholRegime
//  case object OtherFermentedProduct extends AlcoholRegime
//
//  override def toString: String = "alcoholRegime"
//}
