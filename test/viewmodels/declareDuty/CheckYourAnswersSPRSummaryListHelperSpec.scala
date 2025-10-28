/*
 * Copyright 2025 HM Revenue & Customs
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
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.SummaryList

class CheckYourAnswersSPRSummaryListHelperSpec extends SpecBase {
  implicit val messages: Messages = getMessages(app)

  "CheckYourAnswersSPRSummaryListHelper" - {
    "must return a summary list containing the rows if TellUsAboutMultipleSPRRateSummary returns rows" in {
      val answers = tellUsAboutMultipleSPRRatePage(
        whatDoYouNeedToDeclarePage(userAnswersWithBeer, Beer, allRateBands),
        Beer,
        volumeAndRateByTaxType1
      )

      val rows = TellUsAboutMultipleSPRRateSummary.rows(Beer, answers, None)

      CheckYourAnswersSPRSummaryListHelper.summaryList(Beer, answers, None) mustBe Some(SummaryList(rows))
    }

    "must return None if TellUsAboutMultipleSPRRateSummary returns no rows" in {
      val answers = whatDoYouNeedToDeclarePage(userAnswersWithBeer, Beer, allRateBands)

      CheckYourAnswersSPRSummaryListHelper.summaryList(Beer, answers, None) mustBe None
    }
  }
}
