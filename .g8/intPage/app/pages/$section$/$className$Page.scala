package pages.$section$

import pages.QuestionPage
import play.api.libs.json.JsPath

case object $className$Page extends QuestionPage[Int] {
  
  override def path: JsPath = JsPath \ toString
  
  override def toString: String = "$className;format="decap"$"
}
