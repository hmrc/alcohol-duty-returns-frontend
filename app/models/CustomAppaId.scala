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

import play.api.data.Forms._
import play.api.data.format.Formatter
import play.api.data.{Field, Form, FormError}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.label.Label
import uk.gov.hmrc.govukfrontend.views.viewmodels.select.Select
import viewmodels.govuk.select

sealed trait JourneyType {
  val objName: String
  val name: String
}

object JourneyType extends Enumerable.Implicits {
  val values: Seq[JourneyType] = Seq(
    Start,
    BeforeYouStartReturn,
    ViewPayments,
    CheckYourReturns
  )

//  def checkboxItems(implicit messages: Messages): Seq[Select] =
//    values.zipWithIndex.map { case (value, _) =>
//      select.SelectViewModel(
//        id = String,
//        name = String,
//        value = None,
//        label = Label(),
//        items = (value.toString, value.toString)
//      )
//    }

  def checkboxItems(implicit messages: Messages): Select =
    select.SelectViewModel.apply2(
      id = "testId",
      name = "journeyType",
      value = None,
      label = Label(),
      items = values.map(value => (value.toString, value.toString))
    )

  implicit val enumerable: Enumerable[JourneyType] =
    Enumerable(values.map(v => v.toString -> v): _*)
}

case object Start extends JourneyType {
  val name    = "start"
  val objName = "Start"
}

case object BeforeYouStartReturn extends JourneyType {
  val name    = "beforeStart"
  val objName = "BeforeYouStartReturn"
}

case object ViewPayments extends JourneyType {
  val name    = "viewPayments"
  val objName = "ViewPayments"
}

case object CheckYourReturns extends JourneyType {
  val name    = "checkReturns"
  val objName = "CheckYourReturns"
}

/////////////

sealed trait BysrScenario

case object AllApprovals extends BysrScenario

case object AllApprovalsExceptBeer extends BysrScenario

/////////////

sealed trait VpScenario

case object OneDue extends BysrScenario

case object OneDueOneOverdue extends BysrScenario

/////////////

case class CustomAppaId(journeyType: JourneyType)

class CustomAppaIdProvider {
  val form: Form[CustomAppaId] =
    Form(
      mapping(
        "journeyType" -> of[JourneyType](JourneyTypeFormatter.journeyTypeFormatter())
      )(CustomAppaId.apply)(CustomAppaId.unapply)
    )
}

object JourneyTypeFormatter {

  def journeyTypeFormatter(): Formatter[JourneyType] = new Formatter[JourneyType] {

    println("EEEEEEEEEE")

    def errorMessage(key: String): Left[Seq[FormError], Nothing] =
      Left(Seq(FormError(key, s"SOMETHING WRONG")))

    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], JourneyType] = {
      println("FFFFFFFFFFF\n")
      println(s"Key: $key\n")
      println(s"Data: $data\n")
      data.get(key) match {
        case Some(choice) if choice == Start.objName                => Right(Start)
        case Some(choice) if choice == BeforeYouStartReturn.objName =>
          println("IIIIIIIIII\n")
          Right(BeforeYouStartReturn)
        case Some(choice) if choice == ViewPayments.objName         => Right(ViewPayments)
        case Some(choice) if choice == CheckYourReturns.objName     => Right(CheckYourReturns)
        case None                                                   =>
          println("GGGGGGGGGG")
          errorMessage(key)
      }
    }

    override def unbind(key: String, value: JourneyType): Map[String, String] =
      Map(key -> value.name)
  }

}
