package models.adjustment

import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.OptionValues
import play.api.libs.json.{JsError, JsString, Json}

class AdjustmentTypeSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with OptionValues {

  "AdjustmentType" - {

    "must deserialise valid values" in {

      val gen = Gen.oneOf(AdjustmentType.values.toSeq)

      forAll(gen) {
        adjustmentType =>

          JsString(adjustmentType.toString).validate[AdjustmentType].asOpt.value mustEqual adjustmentType
      }
    }

    "must fail to deserialise invalid values" in {

      val gen = arbitrary[String] suchThat (!AdjustmentType.values.map(_.toString).contains(_))

      forAll(gen) {
        invalidValue =>

          JsString(invalidValue).validate[AdjustmentType] mustEqual JsError("error.invalid")
      }
    }

    "must serialise" in {

      val gen = Gen.oneOf(AdjustmentType.values.toSeq)

      forAll(gen) {
        adjustmentType =>

          Json.toJson(adjustmentType) mustEqual JsString(adjustmentType.toString)
      }
    }
  }
}
