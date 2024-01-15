package pages.$section$

import models.$section$.$className$
import play.api.libs.json.JsPath

case object $className$Page extends QuestionPage[$className$] {
  
  override def path: JsPath = JsPath \ toString
  
  override def toString: String = "$className;format="decap"$"
}
