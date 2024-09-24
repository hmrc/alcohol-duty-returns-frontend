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

import config.Constants
import models.ObligationStatus.Open
import models.{ObligationData, ObligationStatusToDisplay, ReturnPeriod}
import play.api.Logging
import play.api.i18n.Messages
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.Aliases.{HeadCell, HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.html.components.{GovukTag, Tag}
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.TableRow
import viewmodels.{TableRowActionViewModel, TableRowViewModel, TableViewModel}

import java.time.format.DateTimeFormatter
import java.time.{LocalDate, YearMonth}
import javax.inject.Inject

class ViewPastReturnsHelper @Inject() () extends Logging {

  def getReturnsTable(obligationData: Seq[ObligationData])(implicit messages: Messages): TableViewModel = {
    val sortedObligationData = obligationData.sortBy(_.dueDate)(Ordering[LocalDate].reverse)
    if (sortedObligationData.nonEmpty) {
      TableViewModel(
        head = getTableHeader(messages),
        rows = getObligationDataTableRows(sortedObligationData),
        total = None
      )
    } else {
      TableViewModel.empty()
    }
  }

  private def getTableHeader(messages: Messages): Seq[HeadCell] =
    Seq(
      HeadCell(content = Text(messages("viewPastReturns.period")), classes = Constants.oneQuarterCssClass),
      HeadCell(content = Text(messages("viewPastReturns.status")), classes = Constants.oneQuarterCssClass),
      HeadCell(content = Text(messages("viewPastReturns.action")), classes = Constants.oneQuarterCssClass)
    )

  private def getObligationDataTableRows(obligationData: Seq[ObligationData])(implicit
    messages: Messages
  ): Seq[TableRowViewModel] =
    obligationData.map { obligationData =>
      val periodKey = obligationData.periodKey
      val status    = getObligationStatus(obligationData, LocalDate.now())
      val statusTag = createStatusTag(status)
      TableRowViewModel(
        cells = Seq(
          TableRow(content = Text(formatYearMonth(getPeriod(periodKey)))),
          TableRow(content = HtmlContent(statusTag))
        ),
        actions = getAction(messages, obligationData, status, periodKey)
      )
    }

  private def getAction(
    messages: Messages,
    obligationData: ObligationData,
    status: ObligationStatusToDisplay,
    periodKey: String
  ): Seq[TableRowActionViewModel] =
    if (status.equals(ObligationStatusToDisplay.Completed)) {
      Seq(
        TableRowActionViewModel(
          label = messages("viewPastReturns.viewReturn"),
          href = controllers.returns.routes.ViewReturnController.onPageLoad(periodKey),
          visuallyHiddenText = Some(messages("viewPastReturns.viewReturn.hidden"))
        )
      )
    } else {
      Seq(
        TableRowActionViewModel(
          label = messages("viewPastReturns.submitReturn"),
          href = controllers.routes.BeforeStartReturnController.onPageLoad(obligationData.periodKey),
          visuallyHiddenText = Some(messages("viewPastReturns.submitReturn.hidden"))
        )
      )
    }

  private def formatYearMonth(yearMonth: YearMonth): String = {
    val formatter = DateTimeFormatter.ofPattern("MMMM yyyy")
    yearMonth.format(formatter)
  }

  private def getObligationStatus(obligationData: ObligationData, today: LocalDate): ObligationStatusToDisplay =
    if (obligationData.status == Open) {
      if (obligationData.dueDate.isBefore(today)) {
        ObligationStatusToDisplay.Overdue
      } else {
        ObligationStatusToDisplay.Due
      }
    } else {
      ObligationStatusToDisplay.Completed
    }

  private def createStatusTag(status: ObligationStatusToDisplay)(implicit
    messages: Messages
  ): Html = {
    val tag = status match {
      case ObligationStatusToDisplay.Due       =>
        Tag(content = Text(messages("viewPastReturns.status.due")), classes = Constants.blueTagCssClass)
      case ObligationStatusToDisplay.Overdue   =>
        Tag(content = Text(messages("viewPastReturns.status.overdue")), classes = Constants.redTagCssClass)
      case ObligationStatusToDisplay.Completed =>
        Tag(content = Text(messages("viewPastReturns.status.completed")), classes = Constants.greenTagCssClass)
    }
    new GovukTag()(tag)
  }

  private def getPeriod(periodKey: String): YearMonth =
    ReturnPeriod.fromPeriodKey(periodKey) match {
      case Some(returnPeriod) => returnPeriod.period
      case _                  =>
        logger.warn("Couldn't fetch period from periodKey")
        throw new RuntimeException(
          "Couldn't fetch period from periodKey"
        )
    }
}
