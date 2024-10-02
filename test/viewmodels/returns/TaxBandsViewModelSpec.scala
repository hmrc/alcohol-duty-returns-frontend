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

package viewmodels.returns

import base.SpecBase
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text

class TaxBandsViewModelSpec extends SpecBase {
  "TaxBandsViewModel" - {
    "should return an empty view model when no ratebands are passed" in new SetUp {
      TaxBandsViewModel(Seq.empty) mustBe TaxBandsViewModel(Seq.empty, Seq.empty, Seq.empty, Seq.empty)
    }

    "should return the model when all ratebands are passed" in new SetUp {
      val result = TaxBandsViewModel(allRateBands.toSeq)
      result.core.map(_.content) mustBe Seq(Text("Beer between 1% and 2% ABV (tax type code 123)"))
      result.draught.map(_.content) mustBe Seq(Text("Beer between 2% and 3% ABV (tax type code 124)"))
      result.smallProducerRelief.map(_.content) mustBe Seq(Text("Beer between 3% and 4% ABV (tax type code 125)"))
      result.draughtAndSmallProducerRelief.map(_.content) mustBe Seq(
        Text("Beer between 4% and 5% ABV (tax type code 126)")
      )
      result.core.map(_.value) mustBe Seq("123")
      result.draught.map(_.value) mustBe Seq("124")
      result.smallProducerRelief.map(_.value) mustBe Seq("125")
      result.draughtAndSmallProducerRelief.map(_.value) mustBe Seq("126")
    }

    "should return the model when all ratebands except core are passed" in new SetUp {
      val result = TaxBandsViewModel((allRateBands - coreRateBand).toSeq)
      result.core.map(_.content) mustBe Seq.empty
      result.draught.map(_.content) mustBe Seq(Text("Beer between 2% and 3% ABV (tax type code 124)"))
      result.smallProducerRelief.map(_.content) mustBe Seq(Text("Beer between 3% and 4% ABV (tax type code 125)"))
      result.draughtAndSmallProducerRelief.map(_.content) mustBe Seq(
        Text("Beer between 4% and 5% ABV (tax type code 126)")
      )
      result.core.map(_.value) mustBe Seq.empty
      result.draught.map(_.value) mustBe Seq("124")
      result.smallProducerRelief.map(_.value) mustBe Seq("125")
      result.draughtAndSmallProducerRelief.map(_.value) mustBe Seq("126")
    }

    "should return the model when all ratebands except draught relief are passed" in new SetUp {
      val result = TaxBandsViewModel((allRateBands - draughtReliefRateBand).toSeq)
      result.core.map(_.content) mustBe Seq(Text("Beer between 1% and 2% ABV (tax type code 123)"))
      result.draught.map(_.content) mustBe Seq.empty
      result.smallProducerRelief.map(_.content) mustBe Seq(Text("Beer between 3% and 4% ABV (tax type code 125)"))
      result.draughtAndSmallProducerRelief.map(_.content) mustBe Seq(
        Text("Beer between 4% and 5% ABV (tax type code 126)")
      )
      result.core.map(_.value) mustBe Seq("123")
      result.draught.map(_.value) mustBe Seq.empty
      result.smallProducerRelief.map(_.value) mustBe Seq("125")
      result.draughtAndSmallProducerRelief.map(_.value) mustBe Seq("126")
    }

    "should return the model when all ratebands except small producer relief are passed" in new SetUp {
      val result = TaxBandsViewModel((allRateBands - smallProducerReliefRateBand).toSeq)
      result.core.map(_.content) mustBe Seq(Text("Beer between 1% and 2% ABV (tax type code 123)"))
      result.draught.map(_.content) mustBe Seq(Text("Beer between 2% and 3% ABV (tax type code 124)"))
      result.smallProducerRelief.map(_.content) mustBe Seq.empty
      result.draughtAndSmallProducerRelief.map(_.content) mustBe Seq(
        Text("Beer between 4% and 5% ABV (tax type code 126)")
      )
      result.core.map(_.value) mustBe Seq("123")
      result.draught.map(_.value) mustBe Seq("124")
      result.smallProducerRelief.map(_.value) mustBe Seq.empty
      result.draughtAndSmallProducerRelief.map(_.value) mustBe Seq("126")
    }

    "should return rate band description when all ratebands except draught and small roducer relief are passed" in new SetUp {
      val result = TaxBandsViewModel((allRateBands - draughtAndSmallProducerReliefRateBand).toSeq)
      result.core.map(_.content) mustBe Seq(Text("Beer between 1% and 2% ABV (tax type code 123)"))
      result.draught.map(_.content) mustBe Seq(Text("Beer between 2% and 3% ABV (tax type code 124)"))
      result.smallProducerRelief.map(_.content) mustBe Seq(Text("Beer between 3% and 4% ABV (tax type code 125)"))
      result.draughtAndSmallProducerRelief.map(_.content) mustBe Seq.empty
      result.core.map(_.value) mustBe Seq("123")
      result.draught.map(_.value) mustBe Seq("124")
      result.smallProducerRelief.map(_.value) mustBe Seq("125")
      result.draughtAndSmallProducerRelief.map(_.value) mustBe Seq.empty
    }
  }

  class SetUp {
    val application                 = applicationBuilder(userAnswers = None).build()
    implicit val messages: Messages = getMessages(application)
  }
}
