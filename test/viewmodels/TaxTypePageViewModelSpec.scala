/*
 * Copyright 2023 HM Revenue & Customs
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

package viewmodels

import base.SpecBase
import generators.ModelGenerators
import models.UserAnswers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.{AlcoholByVolumeQuestionPage, DraughtReliefQuestionPage, SmallProducerReliefQuestionPage}
import play.api.Application
import play.api.i18n.Messages
import org.scalacheck.Arbitrary.arbBool
import org.scalacheck.Gen

class TaxTypePageViewModelSpec extends SpecBase with ScalaCheckPropertyChecks with ModelGenerators {

  val application: Application    = applicationBuilder().build()
  implicit val messages: Messages = messages(application)

  "TaxTypePageViewModel apply" - {
    "should return Some TaxTypePageViewModel" - {
      "when there is Alcohol By Volume value, draught relief eligibility and small producer relief eligibility in UserAnswers" in {
        forAll(genAlcoholByVolumeValue, arbBool.arbitrary, arbBool.arbitrary) {
          (alcoholByVolume: BigDecimal, eligibleForDraughtRelief: Boolean, eligibleForSmallProducerRelief: Boolean) =>
            val userAnswers = UserAnswers(userAnswersId)
              .set(AlcoholByVolumeQuestionPage, alcoholByVolume)
              .success
              .value
              .set(DraughtReliefQuestionPage, eligibleForDraughtRelief)
              .success
              .value
              .set(SmallProducerReliefQuestionPage, eligibleForSmallProducerRelief)
              .success
              .value
            TaxTypePageViewModel(userAnswers) mustBe Some(
              TaxTypePageViewModel(
                alcoholByVolume.toString + messages("site.percentage"),
                eligibleForDraughtRelief,
                eligibleForSmallProducerRelief
              )
            )
        }
      }
    }
    "should return None" - {
      val fullUserAnswers = UserAnswers(userAnswersId)
        .set(AlcoholByVolumeQuestionPage, BigDecimal(3.5))
        .success
        .value
        .set(DraughtReliefQuestionPage, true)
        .success
        .value
        .set(SmallProducerReliefQuestionPage, false)
        .success
        .value
      "when there is no Alcohol By Volume value in UserAnswers" in {
        val userAnswers = fullUserAnswers.remove(AlcoholByVolumeQuestionPage).success.value
        TaxTypePageViewModel(userAnswers) mustBe None
      }
      "when there is no draught relief eligibility in UserAnswers" in {
        val userAnswers = fullUserAnswers.remove(DraughtReliefQuestionPage).success.value
        TaxTypePageViewModel(userAnswers) mustBe None
      }
      "when there is no small producer relief eligibility in UserAnswers" in {
        val userAnswers = fullUserAnswers.remove(SmallProducerReliefQuestionPage).success.value
        TaxTypePageViewModel(userAnswers) mustBe None
      }
    }
  }
}
