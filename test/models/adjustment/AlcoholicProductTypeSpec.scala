package models.adjustment

import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.OptionValues
import play.api.libs.json.{JsError, JsString, Json}

class AlcoholicProductTypeSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with OptionValues {

  "AlcoholicProductType" - {

    "must deserialise valid values" in {

      val gen = Gen.oneOf(AlcoholicProductType.values.toSeq)

      forAll(gen) {
        alcoholicProductType =>

          JsString(alcoholicProductType.toString).validate[AlcoholicProductType].asOpt.value mustEqual alcoholicProductType
      }
    }

    "must fail to deserialise invalid values" in {

      val gen = arbitrary[String] suchThat (!AlcoholicProductType.values.map(_.toString).contains(_))

      forAll(gen) {
        invalidValue =>

          JsString(invalidValue).validate[AlcoholicProductType] mustEqual JsError("error.invalid")
      }
    }

    "must serialise" in {

      val gen = Gen.oneOf(AlcoholicProductType.values.toSeq)

      forAll(gen) {
        alcoholicProductType =>

          Json.toJson(alcoholicProductType) mustEqual JsString(alcoholicProductType.toString)
      }
    }
  }
}
