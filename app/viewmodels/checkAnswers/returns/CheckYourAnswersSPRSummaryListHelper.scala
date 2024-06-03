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

import models.{AlcoholRegime, UserAnswers}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList

object CheckYourAnswersSPRSummaryListHelper {

  def summaryList(regime: AlcoholRegime, userAnswers: UserAnswers, index: Option[Int])(implicit
    messages: Messages
  ): Option[SummaryList] = {
    val rows = TellUsAboutMultipleSPRRateSummary.rows(regime, userAnswers, index)
    if (rows.isEmpty) None else Some(SummaryList(rows = rows))
  }

}
