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

package viewmodels.checkAnswers.dutySuspendedNew

import controllers.dutySuspendedNew.routes
import models.{AlcoholRegime, AlcoholRegimes, CheckMode, UserAnswers}
import pages.dutySuspendedNew.{DutySuspendedAlcoholTypePage, DutySuspendedFinalVolumesPage}
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{SummaryList, SummaryListRow}
import viewmodels.AlcoholRegimesViewOrder
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

class CheckYourAnswersSummaryListHelper {
  def alcoholTypeSummaryList(userAnswers: UserAnswers)(implicit messages: Messages): Option[SummaryList] =
    alcoholTypeRow(userAnswers).map(row => SummaryListViewModel(rows = Seq(row)))

  def dutySuspendedAmountsSummaryList(userAnswers: UserAnswers)(implicit messages: Messages): Option[SummaryList] = {
    val maybeSelectedRegimes = userAnswers.get(DutySuspendedAlcoholTypePage)

    maybeSelectedRegimes.flatMap { selectedRegimes =>
      val orderedRegimes     = AlcoholRegimesViewOrder.regimesInViewOrder(AlcoholRegimes(selectedRegimes))
      val selectedRegimeRows = orderedRegimes.map(regime => amountsRow(userAnswers, regime))

      if (selectedRegimeRows.contains(None)) {
        None
      } else {
        Some(SummaryListViewModel(rows = selectedRegimeRows.flatten))
      }
    }
  }

  private def alcoholTypeRow(userAnswers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    userAnswers.get(DutySuspendedAlcoholTypePage).map { alcoholTypes =>
      val orderedRegimes = AlcoholRegimesViewOrder.regimesInViewOrder(AlcoholRegimes(alcoholTypes))
      val rowValue       =
        orderedRegimes.map(regime => HtmlFormat.escape(messages(regime.regimeMessageKey)).toString.capitalize)

      SummaryListRowViewModel(
        key = messages("dutySuspended.checkYourAnswers.alcoholType.summaryListKey"),
        value = ValueViewModel(HtmlContent(rowValue.mkString("<br>"))),
        actions = Seq(
          ActionItemViewModel("site.change", routes.DutySuspendedAlcoholTypeController.onPageLoad(CheckMode).url)
            .withVisuallyHiddenText(messages("dutySuspended.checkYourAnswers.alcoholType.change.hidden"))
        )
      )
    }

  private def amountsRow(userAnswers: UserAnswers, regime: AlcoholRegime)(implicit
    messages: Messages
  ): Option[SummaryListRow] =
    userAnswers.getByKey(DutySuspendedFinalVolumesPage, regime).map { volumes =>
      SummaryListRowViewModel(
        key = KeyViewModel(HtmlContent(messages(regime.regimeMessageKey).capitalize)),
        value = ValueViewModel(
          HtmlContent(
            s"${messages("dutySuspended.checkYourAnswers.totalLitres", messages("site.2DP", volumes.totalLitres))}"
              + "<br>"
              + s"${messages("dutySuspended.checkYourAnswers.pureAlcohol", messages("site.4DP", volumes.pureAlcohol))}"
          )
        ),
        actions = Seq(
          ActionItemViewModel("site.change", routes.DutySuspendedQuantitiesController.onPageLoad(CheckMode, regime).url)
            .withVisuallyHiddenText(messages("dutySuspended.checkYourAnswers.amount.change.hidden"))
        )
      )
    }
}
