package pages.spiritsQuestions

import pages.QuestionPage
import play.api.libs.json.JsPath

case object RyeIngredientPage extends QuestionPage[Int] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "ryeIngredient"
}
