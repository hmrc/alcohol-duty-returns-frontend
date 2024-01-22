package pages.$section$

import java.time.LocalDate
import pages.QuestionPage
import play.api.libs.json.JsPath

case object $className$Page extends QuestionPage[LocalDate] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "$className;format="decap"$"
}
