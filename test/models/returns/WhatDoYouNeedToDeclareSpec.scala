package models.returns

import generators.ModelGenerators
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.OptionValues
import play.api.libs.json.{JsError, JsString, Json}

class WhatDoYouNeedToDeclareSpec
    extends AnyFreeSpec
    with Matchers
    with ScalaCheckPropertyChecks
    with OptionValues
    with ModelGenerators {

  "WhatDoYouNeedToDeclare" - {

    "must deserialise valid values" in {

      val gen = arbitrary[WhatDoYouNeedToDeclare]

      forAll(gen) { whatDoYouNeedToDeclare =>
        JsString(whatDoYouNeedToDeclare.toString)
          .validate[WhatDoYouNeedToDeclare]
          .asOpt
          .value mustEqual whatDoYouNeedToDeclare
      }
    }

    "must fail to deserialise invalid values" in {

      val gen = arbitrary[String] suchThat (!WhatDoYouNeedToDeclare.values.map(_.toString).contains(_))

      forAll(gen) { invalidValue =>
        JsString(invalidValue).validate[WhatDoYouNeedToDeclare] mustEqual JsError("error.invalid")
      }
    }

    "must serialise" in {

      val gen = arbitrary[WhatDoYouNeedToDeclare]

      forAll(gen) { whatDoYouNeedToDeclare =>
        Json.toJson(whatDoYouNeedToDeclare) mustEqual JsString(whatDoYouNeedToDeclare.toString)
      }
    }
  }
}
