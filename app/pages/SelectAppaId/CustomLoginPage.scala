package pages.SelectAppaId

import models.SelectAppaId.CustomLogin
import pages.QuestionPage
import play.api.libs.json.JsPath

case object CustomLoginPage extends QuestionPage[Set[CustomLogin]] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "customLogin"
}
