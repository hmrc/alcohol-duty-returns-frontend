package models.SelectAppaId

import generators.ModelGenerators
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.OptionValues
import play.api.libs.json.{JsError, JsString, Json}

class CustomLoginSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with OptionValues with ModelGenerators {

  "CustomLogin" - {

    "must deserialise valid values" in {

      val gen = arbitrary[CustomLogin]

      forAll(gen) {
        customLogin =>

          JsString(customLogin.toString).validate[CustomLogin].asOpt.value mustEqual customLogin
      }
    }

    "must fail to deserialise invalid values" in {

      val gen = arbitrary[String] suchThat (!CustomLogin.values.map(_.toString).contains(_))

      forAll(gen) {
        invalidValue =>

          JsString(invalidValue).validate[CustomLogin] mustEqual JsError("error.invalid")
      }
    }

    "must serialise" in {

      val gen = arbitrary[CustomLogin]

      forAll(gen) {
        customLogin =>

          Json.toJson(customLogin) mustEqual JsString(customLogin.toString)
      }
    }
  }
}
