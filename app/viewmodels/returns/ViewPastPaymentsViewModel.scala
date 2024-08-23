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

import play.api.Logging
import play.api.i18n.Messages
import viewmodels.{Money, TableRowActionViewModel, TableRowViewModel, TableViewModel}

import java.time.LocalDate
import javax.inject.Inject
import uk.gov.hmrc.govukfrontend.views.Aliases.{HeadCell, HtmlContent, TableRow, Text}
import config.Constants
import config.Constants.boldFontCssClass
import models.OutstandingPaymentStatusToDisplay.{Due, NothingToPay, Overdue}
import models.TransactionType.RPI
import models.{OutstandingPayment, OutstandingPaymentStatusToDisplay, TransactionType, UnallocatedPayment}
import uk.gov.hmrc.govukfrontend.views.html.components.{GovukTag, Tag}
import play.twirl.api.Html

import java.time.format.DateTimeFormatter

class ViewPastPaymentsViewModel @Inject() () extends Logging {

  private val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy")

  def getOutstandingPaymentsTable(
    outstandingPaymentsData: Seq[OutstandingPayment]
  )(implicit messages: Messages): TableViewModel = {
    val sortedOutstandingPaymentsData = outstandingPaymentsData.sortBy(_.dueDate)(Ordering[LocalDate].reverse)
    TableViewModel(
      head = getOutstandingPaymentsTableHeader(),
      rows = getOutstandingPaymentsDataTableRows(sortedOutstandingPaymentsData),
      total = None
    )
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
    outstandingPaymentsData.map { outstandingPaymentsData =>
      val status    = getOutstandingPaymentStatus(outstandingPaymentsData)
      val statusTag = createStatusTag(status)
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
        actions = getAction(outstandingPaymentsData, status)
      )
    }

  def getUnallocatedPaymentsTable(
    unallocatedPaymentsData: Seq[UnallocatedPayment]
  )(implicit messages: Messages): TableViewModel = {
    val sortedUnallocatedPaymentsData = unallocatedPaymentsData.sortBy(_.paymentDate)(Ordering[LocalDate].reverse)
    if (sortedUnallocatedPaymentsData.nonEmpty) {
      TableViewModel(
        head = getUnallocatedPaymentsHeader(),
        rows = getUnallocatedPaymentsDataTableRows(sortedUnallocatedPaymentsData),
        total = None
      )
    } else {
      TableViewModel(head = Seq.empty, Seq.empty, total = None)
    }
  }

  private def getUnallocatedPaymentsHeader()(implicit messages: Messages): Seq[HeadCell] =
    Seq(
      HeadCell(content = Text(messages("viewPastPayments.unallocatedPayments.paymentDate"))),
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

  private def getAction(
    outstandingPaymentsData: OutstandingPayment,
    status: OutstandingPaymentStatusToDisplay
  )(implicit messages: Messages): Seq[TableRowActionViewModel] =
    if (status.equals(OutstandingPaymentStatusToDisplay.NothingToPay)) {
      Seq.empty
    } else {
      Seq(
        TableRowActionViewModel(
          label = messages("viewPastPayments.payNow"),
          href = controllers.routes.JourneyRecoveryController.onPageLoad(),
          visuallyHiddenText = Some(messages("viewPastPayments.payNow.hidden"))
        )
      )
    }

  private def formatDateYearMonth(date: LocalDate): String =
    date.format(formatter)

  private def formatDescription(
    transactionType: TransactionType,
    chargeReference: Option[String],
    remainingAmount: BigDecimal
  )(implicit
    messages: Messages
  ): Html = {
    val (description, reference) = (remainingAmount, chargeReference, transactionType) match {
      case (_, Some(chargeReference), transactionType) if transactionType == RPI =>
        (messages(s"viewPastPayments.RPI.description"), messages("viewPastPayments.ref", chargeReference))
      case (remainingAmount, Some(chargeReference), _) if remainingAmount < 0    =>
        (messages(s"viewPastPayments.credit.description"), messages("viewPastPayments.ref", chargeReference))
      case (_, Some(chargeReference), _)                                         =>
        (messages(s"viewPastPayments.$transactionType.description"), messages("viewPastPayments.ref", chargeReference))
      case (_, None, _)                                                          =>
        logger.logger.warn("Couldn't fetch chargeReference for outstanding payment")
        (messages(s"viewPastPayments.$transactionType.description"), "")
    }
    Html(s"$description<br>$reference")
  }

  private def getOutstandingPaymentStatus(
    outstandingPaymentsData: OutstandingPayment
  ): OutstandingPaymentStatusToDisplay =
    if (outstandingPaymentsData.transactionType == RPI || outstandingPaymentsData.remainingAmount < 0) {
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
