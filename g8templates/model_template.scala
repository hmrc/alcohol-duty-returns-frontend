package models.$section$
$pre_modelImports$
import play.api.libs.json._

case class $className$($pre_modelFields$)

object $className$ {
  implicit val format: OFormat[$className$] = Json.format[$className$]
}
