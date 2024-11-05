package models

import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.checkboxes.CheckboxItem
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import viewmodels.govuk.checkbox._

sealed trait CustomLogin

object CustomLogin extends Enumerable.Implicits {

  case object he extends WithName("1") with CustomLogin
  case object she extends WithName("2") with CustomLogin

  val values: Seq[CustomLogin] = Seq(
    1,
    2
  )

  def checkboxItems(implicit messages: Messages): Seq[CheckboxItem] =
    values.zipWithIndex.map {
      case (value, index) =>
        CheckboxItemViewModel(
          content = Text(messages(s"customLogin.${value.toString}")),
          fieldId = "value",
          index   = index,
          value   = value.toString
        )
    }

  implicit val enumerable: Enumerable[CustomLogin] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
