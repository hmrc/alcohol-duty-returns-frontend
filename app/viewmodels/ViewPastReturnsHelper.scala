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

package viewmodels
import models.ObligationStatus.Open
import models.{ObligationData, ObligationStatusToDisplay, ReturnPeriod, UserAnswers}
import play.api.i18n.Messages
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.Aliases.{HeadCell, HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.html.components.{GovukTag, Tag}

import java.time.format.DateTimeFormatter
import java.time.{LocalDate, YearMonth}

object ViewPastReturnsHelper {

  def outstandingReturnsTable(obligationData: Seq[ObligationData])(implicit messages: Messages): TableViewModel = {
    TableViewModel(head = getTableHeader(messages),
      rows= getObligationDataTableRows(obligationData),
    total= 0)
  }

  def completedReturnsTable(obligationData: Seq[ObligationData])(implicit messages: Messages): TableViewModel = {
    println(obligationData)
    TableViewModel(head = getTableHeader(messages),
      rows = getObligationDataTableRows(obligationData),
      total = 0)
  }

  private def getTableHeader(messages: Messages): Seq[HeadCell] = {
    Seq(
      HeadCell(content = Text(messages("viewPastReturns.period")), classes = "govuk-!-width-one-quarter"),
      HeadCell(content = Text(messages("viewPastReturns.status")), classes = "govuk-!-width-one-quarter"),
      HeadCell(content = Text(messages("viewPastReturns.action")), classes = "govuk-!-width-one-quarter")
    )
  }

  private def getObligationDataTableRows(obligationData: Seq[ObligationData])(implicit
                                                                              messages: Messages
  ): Seq[TableRowViewModel] =
    obligationData.map{
      obligationData  => {
        val status = getObligationStatus(obligationData, LocalDate.now())
        val statusTag = createStatusTag(status)
        TableRowViewModel(
          cells = Seq(
            Text(formatYearMonth(getPeriod(obligationData.periodKey))),
            HtmlContent(statusTag)
          ),
          actions = getAction(messages, obligationData, status)
        )
      }

    }

  private def getAction(messages: Messages, obligationData: ObligationData, status: ObligationStatusToDisplay): Seq[TableRowActionViewModel] = {
    if (status.equals(ObligationStatusToDisplay.Completed)) {
      Seq(
        TableRowActionViewModel(
          label = messages("viewPastReturns.viewReturn"),
          href = controllers.routes.TaskListController.onPageLoad,
          visuallyHiddenText = Some(messages("viewPastReturns.viewReturn.hidden"))
        )
      )
    }
    else{
      Seq(
        TableRowActionViewModel(
          label = messages("viewPastReturns.submitReturn"),
          href = controllers.routes.BeforeStartReturnController.onPageLoad(obligationData.periodKey),
          visuallyHiddenText = Some(messages("viewPastReturns.submitReturn.hidden"))
        )
      )
    }
  }

  private def formatYearMonth(yearMonth: YearMonth): String =
    {
      val formatter = DateTimeFormatter.ofPattern("MMMM yyyy")
      yearMonth.format(formatter)
    }

  private def getObligationStatus(obligationData: ObligationData, today: LocalDate): ObligationStatusToDisplay =
    {
      if(obligationData.status == Open){
        if(obligationData.dueDate.isBefore(today)) {
          ObligationStatusToDisplay.Overdue
        }
        else {
          ObligationStatusToDisplay.Due
        }
      }
      else
        {
          ObligationStatusToDisplay.Completed
        }
    }

  private def createStatusTag(status: ObligationStatusToDisplay)(implicit
                                              messages: Messages
  ): Html ={
    val tag = status match{
      case ObligationStatusToDisplay.Due => Tag(content= Text(messages("viewPastReturns.status.due")), classes= "govuk-tag--blue")
      case ObligationStatusToDisplay.Overdue => Tag(content= Text(messages("viewPastReturns.status.overdue")), classes= "govuk-tag--red")
      case ObligationStatusToDisplay.Completed => Tag(content= Text(messages("viewPastReturns.status.completed")), classes= "govuk-tag--green")
      case _ => throw new RuntimeException("Couldn't create status for obligation data")
    }
    new GovukTag()(tag)
  }

  private def getPeriod(periodKey: String): YearMonth ={
    ReturnPeriod.fromPeriodKey(periodKey) match {
      case Some(returnPeriod) => returnPeriod.period
      case _ => throw new RuntimeException("Couldn't fetch period from periodKey")// do match case if None log and throw exception
    }
  }


}
