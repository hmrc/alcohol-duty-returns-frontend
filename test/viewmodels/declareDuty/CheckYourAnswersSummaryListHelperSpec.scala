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

package viewmodels.declareDuty

import base.SpecBase
import models.AlcoholRegime.Beer
import models.ErrorModel
import play.api.http.Status.BAD_REQUEST
import play.api.i18n.Messages
import org.mockito.Mockito.when
import uk.gov.hmrc.govukfrontend.views.Aliases.{SummaryList, SummaryListRow}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, Value}

class CheckYourAnswersSummaryListHelperSpec extends SpecBase {
  "createSummaryList" - {
    "must return a Left containing an ErrorModel if any required declaration details are not present" in new SetUp {
      val userAnswers = specifyWhatDoYouNeedToDeclare(userAnswersWithBeer, Beer)

      checkYourAnswersSummaryListHelper.createSummaryList(Beer, userAnswers) mustBe
        Left(ErrorModel(BAD_REQUEST, "Missing declaration details for non-SPR rate bands"))
    }

    "must return a Right containing the summary lists if all required declaration details are present" in new SetUp {
      val userAnswersSetup1 = specifyWhatDoYouNeedToDeclare(userAnswersWithBeer, Beer)
      val userAnswersSetup2 = specifyAllHowMuchDoYouNeedToDeclare(userAnswersSetup1, Beer)
      val userAnswersSetup3 = specifyTellUsAboutAllSingleSPRRate(userAnswersSetup2, Beer)
      val userAnswers       = doYouHaveMultipleSPRDutyRatesPage(userAnswersSetup3, Beer, hasMultiple = false)

      when(mockWhatDoYouNeedToDeclareSummary.summaryList(Beer, allRateBands)).thenReturn(summaryList1)
      when(mockHowMuchDoYouNeedToDeclareSummary.summaryList(Beer, allRateBands, userAnswers))
        .thenReturn(Some(summaryList2))
      when(mockSmallProducerReliefSummary.summaryList(Beer, userAnswers)).thenReturn(Some(summaryList3))

      checkYourAnswersSummaryListHelper.createSummaryList(Beer, userAnswers) mustBe Right(
        ReturnSummaryList(
          whatDoYouNeedToDeclareSummary = summaryList1,
          howMuchDoYouNeedToDeclareSummary = Some(summaryList2),
          smallProducerReliefSummary = Some(summaryList3)
        )
      )
    }
  }

  "checkDeclarationDetailsArePresent" - {
    "must return a Right containing the declared rate bands if all required declaration details are present" - {
      "and no SPR rate bands are declared" in new SetUp {
        val userAnswersSetup1 =
          whatDoYouNeedToDeclarePage(userAnswersWithBeer, Beer, allNonSmallProducerReliefRateBands)
        val userAnswers       = specifyAllHowMuchDoYouNeedToDeclare(userAnswersSetup1, Beer)

        checkYourAnswersSummaryListHelper.checkDeclarationDetailsArePresent(Beer, userAnswers) mustBe Right(
          allNonSmallProducerReliefRateBands
        )
      }

      "and single SPR is selected" in new SetUp {
        val userAnswersSetup1 = specifyWhatDoYouNeedToDeclare(userAnswersWithBeer, Beer)
        val userAnswersSetup2 = specifyAllHowMuchDoYouNeedToDeclare(userAnswersSetup1, Beer)
        val userAnswersSetup3 = specifyTellUsAboutAllSingleSPRRate(userAnswersSetup2, Beer)
        val userAnswers       = doYouHaveMultipleSPRDutyRatesPage(userAnswersSetup3, Beer, hasMultiple = false)

        checkYourAnswersSummaryListHelper.checkDeclarationDetailsArePresent(Beer, userAnswers) mustBe Right(
          allRateBands
        )
      }

      "and multiple SPR is selected" in new SetUp {
        val userAnswersSetup1 = specifyWhatDoYouNeedToDeclare(userAnswersWithBeer, Beer)
        val userAnswersSetup2 = specifyAllHowMuchDoYouNeedToDeclare(userAnswersSetup1, Beer)
        val userAnswersSetup3 = specifyAllMultipleSPRList(userAnswersSetup2, Beer)
        val userAnswers       = doYouHaveMultipleSPRDutyRatesPage(userAnswersSetup3, Beer, hasMultiple = true)

        checkYourAnswersSummaryListHelper.checkDeclarationDetailsArePresent(Beer, userAnswers) mustBe Right(
          allRateBands
        )
      }
    }

    "must return a Left containing an ErrorModel" - {
      "if user answers do not contain declared rate bands" in new SetUp {
        checkYourAnswersSummaryListHelper.createSummaryList(Beer, userAnswersWithBeer) mustBe
          Left(ErrorModel(BAD_REQUEST, "No declared rate bands in user answers"))
      }

      "if non-SPR rate band details are missing" in new SetUp {
        val userAnswers = specifyWhatDoYouNeedToDeclare(userAnswersWithBeer, Beer)

        checkYourAnswersSummaryListHelper.createSummaryList(Beer, userAnswers) mustBe
          Left(ErrorModel(BAD_REQUEST, "Missing declaration details for non-SPR rate bands"))
      }

      "if SPR rate bands are declared but single/multiple SPR question is not answered" in new SetUp {
        val userAnswersSetup1 = specifyWhatDoYouNeedToDeclare(userAnswersWithBeer, Beer)
        val userAnswers       = specifyAllHowMuchDoYouNeedToDeclare(userAnswersSetup1, Beer)

        checkYourAnswersSummaryListHelper.createSummaryList(Beer, userAnswers) mustBe
          Left(ErrorModel(BAD_REQUEST, "SPR rate bands declared but single/multiple SPR question not answered"))
      }

      "if single SPR is selected but details are missing" in new SetUp {
        val userAnswersSetup1 = specifyWhatDoYouNeedToDeclare(userAnswersWithBeer, Beer)
        val userAnswersSetup2 = specifyAllHowMuchDoYouNeedToDeclare(userAnswersSetup1, Beer)
        val userAnswers       = doYouHaveMultipleSPRDutyRatesPage(userAnswersSetup2, Beer, hasMultiple = false)

        checkYourAnswersSummaryListHelper.createSummaryList(Beer, userAnswers) mustBe
          Left(ErrorModel(BAD_REQUEST, "Missing declaration details for single SPR"))
      }

      "if multiple SPR is selected but multiple SPR list is empty" in new SetUp {
        val userAnswersSetup1 = specifyWhatDoYouNeedToDeclare(userAnswersWithBeer, Beer)
        val userAnswersSetup2 = specifyAllHowMuchDoYouNeedToDeclare(userAnswersSetup1, Beer)
        val userAnswers       = doYouHaveMultipleSPRDutyRatesPage(userAnswersSetup2, Beer, hasMultiple = true)

        checkYourAnswersSummaryListHelper.createSummaryList(Beer, userAnswers) mustBe
          Left(ErrorModel(BAD_REQUEST, "Multiple SPR list is empty"))
      }
    }
  }

  class SetUp {
    val application                 = applicationBuilder(userAnswers = None).build()
    implicit val messages: Messages = getMessages(application)

    val mockHowMuchDoYouNeedToDeclareSummary: HowMuchDoYouNeedToDeclareSummary = mock[HowMuchDoYouNeedToDeclareSummary]
    val mockSmallProducerReliefSummary: SmallProducerReliefSummary             = mock[SmallProducerReliefSummary]
    val mockWhatDoYouNeedToDeclareSummary: WhatDoYouNeedToDeclareSummary       = mock[WhatDoYouNeedToDeclareSummary]

    val checkYourAnswersSummaryListHelper = new CheckYourAnswersSummaryListHelper(
      mockHowMuchDoYouNeedToDeclareSummary,
      mockSmallProducerReliefSummary,
      mockWhatDoYouNeedToDeclareSummary
    )

    val summaryList1 = SummaryList(rows =
      Seq(SummaryListRow(key = Key(content = Text("Key1")), value = Value(content = Text("Value1"))))
    )
    val summaryList2 = SummaryList(rows =
      Seq(SummaryListRow(key = Key(content = Text("Key2")), value = Value(content = Text("Value2"))))
    )
    val summaryList3 = SummaryList(rows =
      Seq(SummaryListRow(key = Key(content = Text("Key3")), value = Value(content = Text("Value3"))))
    )
  }
}
