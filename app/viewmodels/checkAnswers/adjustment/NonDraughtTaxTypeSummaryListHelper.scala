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

package viewmodels.checkAnswers.adjustment

import models.RateBand
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import viewmodels.govuk.summarylist._

import javax.inject.Inject

class NonDraughtTaxTypeSummaryListHelper @Inject() (
  draughtTaxTypeCodeSummary: DraughtTaxTypeCodeSummary,
  nonDraughtTaxTypeCodeSummary: NonDraughtTaxTypeCodeSummary,
  nonDraughtTaxTypeCodeDescSummary: NonDraughtTaxTypeCodeDescSummary
) {

  def nonDraughtTaxTypeSummaryList(rateBand: RateBand, repackagedRateBand: RateBand)(implicit
    message: Messages
  ): SummaryList = {

    val draughtTaxTypeCode        = draughtTaxTypeCodeSummary.row(rateBand)
    val nonDraughtTaxTypeCode     = nonDraughtTaxTypeCodeSummary.row(repackagedRateBand)
    val nonDraughtTaxTypeCodeDesc = nonDraughtTaxTypeCodeDescSummary.row(repackagedRateBand)

    SummaryListViewModel(
      rows = Seq(draughtTaxTypeCode, nonDraughtTaxTypeCode, nonDraughtTaxTypeCodeDesc)
    )
  }
}
