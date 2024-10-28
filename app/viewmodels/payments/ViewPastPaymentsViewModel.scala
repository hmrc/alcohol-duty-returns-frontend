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

package viewmodels.payments

import config.Constants
import config.Constants.boldFontCssClass
import models.OutstandingPaymentStatusToDisplay.{Due, NothingToPay, Overdue}
import models.TransactionType.RPI
import models.{HistoricPayment, OutstandingPayment, OutstandingPaymentStatusToDisplay, TransactionType, UnallocatedPayment}
import play.api.Logging
import play.api.i18n.Messages
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.Aliases.{HeadCell, HtmlContent, TableRow, Text}
import uk.gov.hmrc.govukfrontend.views.html.components.{GovukTag, Tag}
import viewmodels._

import java.time.{LocalDate, YearMonth}
import javax.inject.Inject

class ViewPastPaymentsViewModel @Inject() (dateTimeHelper: DateTimeHelper) extends Logging {

  def getOutstandingPaymentsTable(
    outstandingPaymentsData: Seq[OutstandingPayment]
  )(implicit messages: Messages): TableViewModel =
    if (outstandingPaymentsData.nonEmpty) {
      TableViewModel(
        head = getOutstandingPaymentsTableHeader(),
        rows = getOutstandingPaymentsDataTableRows(outstandingPaymentsData),
        total = None
      )
    } else {
      TableViewModel.empty()
    }

  private def getOutstandingPaymentsTableHeader()(implicit messages: Messages): Seq[HeadCell] =
    Seq(
      HeadCell(content = Text(messages("viewPastPayments.outstandingPayments.dueDate"))),
      HeadCell(content = Text(messages("viewPastPayments.description"))),
      HeadCell(
        content = Text(messages("viewPastPayments.outstandingPayments.remainingAmount")),
        classes = Constants.textAlignRightCssClass
      ),
      HeadCell(content = Text(messages("viewPastPayments.outstandingPayments.status"))),
      HeadCell(content = Text(messages("viewPastPayments.outstandingPayments.action")))
    )

  private def getOutstandingPaymentsDataTableRows(
    outstandingPaymentsData: Seq[OutstandingPayment]
  )(implicit messages: Messages): Seq[TableRowViewModel] =
    outstandingPaymentsData.zipWithIndex.map { case (outstandingPaymentsData, index) =>
      val status    = getOutstandingPaymentStatus(outstandingPaymentsData)
      val statusTag = createStatusTag(status)
      val amount    = Money.format(outstandingPaymentsData.remainingAmount)
      TableRowViewModel(
        cells = Seq(
          TableRow(content = Text(formatDateYearMonth(outstandingPaymentsData.dueDate))),
          TableRow(content =
            HtmlContent(
              formatDescription(
                outstandingPaymentsData.transactionType,
                outstandingPaymentsData.chargeReference,
                outstandingPaymentsData.remainingAmount
              )
            )
          ),
          TableRow(
            content = Text(Money.format(outstandingPaymentsData.remainingAmount)),
            classes = s"$boldFontCssClass ${Constants.textAlignRightCssClass}"
          ),
          TableRow(content = HtmlContent(statusTag))
        ),
        actions = getAction(status, index, amount)
      )
    }

  def getUnallocatedPaymentsTable(
    unallocatedPaymentsData: Seq[UnallocatedPayment]
  )(implicit messages: Messages): TableViewModel = {
    val sortedUnallocatedPaymentsData = unallocatedPaymentsData.sortBy(_.paymentDate)(Ordering[LocalDate].reverse)
    if (sortedUnallocatedPaymentsData.nonEmpty) {
      TableViewModel(
        head = getHistoricOrUnallocatedPaymentsHeader("unallocated"),
        rows = getUnallocatedPaymentsDataTableRows(sortedUnallocatedPaymentsData),
        total = None
      )
    } else {
      TableViewModel.empty()
    }
  }

  private def getHistoricOrUnallocatedPaymentsHeader(paymentType: String)(implicit messages: Messages): Seq[HeadCell] =
    Seq(
      HeadCell(content = Text(messages(s"viewPastPayments.$paymentType.payments.paymentDate"))),
      HeadCell(content = Text(messages("viewPastPayments.description"))),
      HeadCell(content = Text(messages("viewPastPayments.totalAmount")), classes = Constants.textAlignRightCssClass)
    )

  private def getUnallocatedPaymentsDataTableRows(
    unallocatedPaymentsData: Seq[UnallocatedPayment]
  )(implicit messages: Messages): Seq[TableRowViewModel] =
    unallocatedPaymentsData.map { unallocatedPaymentData =>
      TableRowViewModel(
        cells = Seq(
          TableRow(content = Text(formatDateYearMonth(unallocatedPaymentData.paymentDate))),
          TableRow(content = Text(messages("viewPastPayments.unallocatedPayments.description"))),
          TableRow(
            content = Text(Money.format(unallocatedPaymentData.unallocatedAmount)),
            classes = s"$boldFontCssClass ${Constants.textAlignRightCssClass}"
          )
        )
      )
    }

  def getHistoricPaymentsTable(
    historicPaymentsData: Seq[HistoricPayment]
  )(implicit messages: Messages): TableViewModel = {
    val sortedHistoricPaymentsData = historicPaymentsData.sortBy(_.period.period)(Ordering[YearMonth].reverse)
    if (sortedHistoricPaymentsData.nonEmpty) {
      TableViewModel(
        head = getHistoricOrUnallocatedPaymentsHeader("historic"),
        rows = getHistoricPaymentsDataTableRows(sortedHistoricPaymentsData),
        total = None
      )
    } else {
      TableViewModel.empty()
    }
  }

  private def getHistoricPaymentsDataTableRows(
    historicPaymentsData: Seq[HistoricPayment]
  )(implicit messages: Messages): Seq[TableRowViewModel] =
    historicPaymentsData.map { historicPaymentsData =>
      val monthYear = dateTimeHelper.formatMonthYear(historicPaymentsData.period.period)
      TableRowViewModel(
        cells = Seq(
          TableRow(content = Text(monthYear)),
          TableRow(content =
            HtmlContent(
              formatHistoricPaymentsDescription(
                historicPaymentsData.transactionType,
                historicPaymentsData.chargeReference
              )
            )
          ),
          TableRow(
            content = Text(Money.format(historicPaymentsData.amountPaid)),
            classes = s"$boldFontCssClass ${Constants.textAlignRightCssClass}"
          )
        )
      )
    }

  private def getAction(
    status: OutstandingPaymentStatusToDisplay,
    index: Int,
    amount: String
  )(implicit messages: Messages): Seq[TableRowActionViewModel] =
    if (status.equals(OutstandingPaymentStatusToDisplay.NothingToPay)) {
      Seq.empty
    } else {
      Seq(
        TableRowActionViewModel(
          label = messages("viewPastPayments.payNow.firstPart.hidden"),
          href = controllers.payments.routes.StartPaymentController.initiateAndRedirectFromPastPayments(index),
          visuallyHiddenText = Some(messages("viewPastPayments.payNow.secondPart.hidden", amount))
        )
      )
    }

  private def formatDateYearMonth(date: LocalDate): String =
    dateTimeHelper.formatDateMonthYear(date)

  private def formatDescription(
    transactionType: TransactionType,
    chargeReference: Option[String],
    remainingAmount: BigDecimal
  )(implicit
    messages: Messages
  ): Html = {
    val (description, reference) = (remainingAmount, chargeReference, transactionType) match {
      case (_, Some(chargeReference), transactionType) if transactionType == RPI =>
        (messages(s"viewPastPayments.$transactionType.description"), messages("viewPastPayments.ref", chargeReference))
      case (remainingAmount, Some(chargeReference), _) if remainingAmount < 0    =>
        (messages("viewPastPayments.credit.description"), messages("viewPastPayments.ref", chargeReference))
      case (_, Some(chargeReference), _)                                         =>
        (messages(s"viewPastPayments.$transactionType.description"), messages("viewPastPayments.ref", chargeReference))
      case (_, None, _)                                                          =>
        logger.warn("Couldn't fetch chargeReference for outstanding payment")
        (messages(s"viewPastPayments.$transactionType.description"), "")
    }
    Html(s"$description<br>$reference")
  }

  private def formatHistoricPaymentsDescription(transactionType: TransactionType, chargeReference: Option[String])(
    implicit messages: Messages
  ): Html = {
    val (description, reference) = chargeReference.fold {
      logger.warn("Couldn't fetch chargeReference for historic payment")
      (messages(s"viewPastPayments.historic.$transactionType.description"), "")
    } { chargeReference =>
      (
        messages(s"viewPastPayments.historic.$transactionType.description"),
        messages("viewPastPayments.ref", chargeReference)
      )
    }
    Html(s"$description<br>$reference")
  }

  private def getOutstandingPaymentStatus(
    outstandingPaymentsData: OutstandingPayment
  ): OutstandingPaymentStatusToDisplay =
    if (outstandingPaymentsData.transactionType == RPI || outstandingPaymentsData.remainingAmount <= 0) {
      NothingToPay
    } else if (outstandingPaymentsData.dueDate.isBefore(LocalDate.now())) {
      Overdue
    } else {
      Due
    }

  private def createStatusTag(status: OutstandingPaymentStatusToDisplay)(implicit
    messages: Messages
  ): Html = {
    val tag = status match {
      case OutstandingPaymentStatusToDisplay.Due          =>
        Tag(content = Text(messages("viewPastReturns.status.due")), classes = Constants.blueTagCssClass)
      case OutstandingPaymentStatusToDisplay.Overdue      =>
        Tag(content = Text(messages("viewPastReturns.status.overdue")), classes = Constants.redTagCssClass)
      case OutstandingPaymentStatusToDisplay.NothingToPay =>
        Tag(content = Text(messages("viewPastPayments.status.nothingToPay")), classes = Constants.greyTagCssClass)
    }
    new GovukTag()(tag)
  }
}