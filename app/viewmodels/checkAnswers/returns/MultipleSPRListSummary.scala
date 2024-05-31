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

package viewmodels.checkAnswers.returns

import controllers.returns.routes
import models.AlcoholRegime.Beer
import models.{CheckMode, UserAnswers}
import pages.returns.DoYouWantToAddMultipleSPRToListPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object MultipleSPRListSummary {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(DoYouWantToAddMultipleSPRToListPage).map { answer =>
      val value = ""

      SummaryListRowViewModel(
        key = "multipleSPRList.checkYourAnswersLabel",
        value = ValueViewModel(value),
        actions = Seq(
          ActionItemViewModel("site.change", routes.MultipleSPRListController.onPageLoad(CheckMode, Beer).url)
            .withVisuallyHiddenText(messages("multipleSPRList.change.hidden"))
        )
      )
    }
}
