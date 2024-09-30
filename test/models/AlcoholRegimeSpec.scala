/*
 * Copyright 2024 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package models

import base.SpecBase
import models.AlcoholRegime._
import play.api.libs.json.{JsNull, JsResultException, Json}

class AlcoholRegimeSpec extends SpecBase {
  "AlcoholRegime" - {
    "reads" - {
      Seq(
        ("Beer", Beer),
        ("Cider", Cider),
        ("Wine", Wine),
        ("Spirits", Spirits),
        ("OtherFermentedProduct", OtherFermentedProduct)
      ).foreach { case (alcoholType, regime) =>
        s"should deserialise $alcoholType correctly" in {
          Json.parse(s""""$alcoholType"""").as[AlcoholRegime] mustBe regime
        }
      }

      "fail for an unknown alcoholType" in {
        a[JsResultException] mustBe thrownBy(Json.parse(""""Water"""").as[AlcoholRegime])
      }
    }

    "fail for a bad json" in {
      a[JsResultException] mustBe thrownBy(JsNull.as[AlcoholRegime])
    }
  }

  "writes" - {
    Seq(
      ("Beer", Beer),
      ("Cider", Cider),
      ("Wine", Wine),
      ("Spirits", Spirits),
      ("OtherFermentedProduct", OtherFermentedProduct)
    ).foreach { case (alcoholType, regime) =>
      s"should serialise $alcoholType correctly" in {
        Json.toJson(regime).toString() mustBe s""""$alcoholType""""
      }
    }
  }

  "fromString" - {
    Seq(
      ("Beer", Beer),
      ("Cider", Cider),
      ("Wine", Wine),
      ("Spirits", Spirits),
      ("OtherFermentedProduct", OtherFermentedProduct)
    ).foreach { case (alcoholType, regime) =>
      s"should convert String $alcoholType to the correct regime" in {
        AlcoholRegime.fromString(alcoholType) mustBe Some(regime)
      }
    }

    "should return None for an unknown alcohol type" in {
      AlcoholRegime.fromString("Water") mustBe None
    }
  }
}
