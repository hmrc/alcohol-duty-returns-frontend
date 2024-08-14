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
import models.OutstandingPaymentStatusToDisplay.{Due, NothingToPay, Overdue, PartiallyPaid}
import models.TransactionType.{LPI, PaymentOnAccount, RPI}
import models.{OutstandingPayment, OutstandingPaymentStatusToDisplay, TransactionType}
import uk.gov.hmrc.govukfrontend.views.html.components.{GovukTag, Tag}
import play.twirl.api.Html

import java.time.format.DateTimeFormatter

class ViewPastPaymentsViewModel @Inject() () extends Logging {

  def getOutstandingPaymentsTable(
    outstandingPaymentsData: Seq[OutstandingPayment]
  )(implicit messages: Messages): TableViewModel = {

    implicit val transactionTypeOrdering: Ordering[TransactionType] = {
      val customOrder = Map[TransactionType, Int](
        TransactionType.Return           -> 1,
        TransactionType.LPI              -> 2,
        TransactionType.RPI              -> 3,
        TransactionType.PaymentOnAccount -> 4
      )
      Ordering.by[TransactionType, Int](customOrder)
    }
//      Ordering.by[TransactionType, String](_.entryName).reverse

    implicit val reverseDateOrdering: Ordering[Option[LocalDate]] = Ordering
      .by[Option[LocalDate], Long] {
        case Some(date) => date.toEpochDay
        case None       => Long.MinValue
      }
      .reverse

    val sortedOutstandingPaymentsData = outstandingPaymentsData.sortBy(data => (data.transactionType, data.date))
    TableViewModel(
      head = getOutstandingPaymentsTableHeader(),
      rows = getOutstandingPaymentsDataTableRows(sortedOutstandingPaymentsData),
      total = None
    )
  }

  private def getOutstandingPaymentsTableHeader()(implicit messages: Messages): Seq[HeadCell] =
    Seq(
      HeadCell(content = Text(messages("viewPastPayments.outstandingPayments.dueDate"))),
      HeadCell(content = Text(messages("viewPastPayments.outstandingPayments.description"))),
      HeadCell(content = Text(messages("viewPastPayments.outstandingPayments.totalAmount"))),
      HeadCell(content = Text(messages("viewPastPayments.outstandingPayments.remainingAmount"))),
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
          TableRow(content = Text(outstandingPaymentsData.date.map(formatDateYearMonth).getOrElse(" "))),
          TableRow(content =
            HtmlContent(
              formatDescription(outstandingPaymentsData.transactionType, outstandingPaymentsData.chargeReference)
            )
          ),
          TableRow(
            content = Text(Money.format(outstandingPaymentsData.totalAmount)),
            classes = boldFontCssClass
          ),
          TableRow(
            content = Text(Money.format(outstandingPaymentsData.remainingAmount)),
            classes = boldFontCssClass
          ),
          TableRow(content = HtmlContent(statusTag))
        ),
        actions = getAction(outstandingPaymentsData, status)
      )
    }

  private def getAction(
    outstandingPaymentsData: OutstandingPayment,
    status: OutstandingPaymentStatusToDisplay
  )(implicit messages: Messages): Seq[TableRowActionViewModel] =
    if (status.equals(OutstandingPaymentStatusToDisplay.NothingToPay)) {
      Seq(
        TableRowActionViewModel(
          label = messages("viewPastPayments.claimRefund"),
          href = controllers.routes.JourneyRecoveryController.onPageLoad(),
          visuallyHiddenText = Some(messages("viewPastPayments.claimRefund.hidden"))
        )
      )
    } else {
      Seq(
        TableRowActionViewModel(
          label = messages("viewPastPayments.payNow"),
          href = controllers.routes.JourneyRecoveryController.onPageLoad(),
          visuallyHiddenText = Some(messages("viewPastPayments.payNow.hidden"))
        )
      )
    }

  private def formatDateYearMonth(date: LocalDate): String = {
    val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy")
    date.format(formatter)
  }

  private def formatDescription(transactionType: TransactionType, chargeReference: Option[String])(implicit
    messages: Messages
  ): Html =
    (transactionType, chargeReference) match {
      case (_, Some(chargeReference))                                     =>
        val description = messages(s"viewPastPayments.$transactionType.description")
        val reference   = messages("viewPastPayments.ref", chargeReference)
        Html(s"$description<br>$reference")
      case (transactionType, None) if transactionType == PaymentOnAccount => Html("Payment on account")
    }

  private def getOutstandingPaymentStatus(
    outstandingPaymentsData: OutstandingPayment
  ): OutstandingPaymentStatusToDisplay =
    if (outstandingPaymentsData.transactionType == LPI) {
      Due
    } else if (
      outstandingPaymentsData.transactionType == RPI || outstandingPaymentsData.transactionType == PaymentOnAccount
    ) {
      NothingToPay
    } else if (outstandingPaymentsData.totalAmount < 0) {
      NothingToPay
    } else if (outstandingPaymentsData.date.exists(_.isBefore(LocalDate.now()))) {
      Overdue
    } else if (outstandingPaymentsData.totalAmount != outstandingPaymentsData.remainingAmount) {
      PartiallyPaid
    } else {
      Due
    }

  private def createStatusTag(status: OutstandingPaymentStatusToDisplay)(implicit
    messages: Messages
  ): Html = {
    val tag = status match {
      case OutstandingPaymentStatusToDisplay.Due           =>
        Tag(content = Text(messages("viewPastReturns.status.due")), classes = Constants.blueTagCssClass)
      case OutstandingPaymentStatusToDisplay.Overdue       =>
        Tag(content = Text(messages("viewPastReturns.status.overdue")), classes = Constants.redTagCssClass)
      case OutstandingPaymentStatusToDisplay.PartiallyPaid =>
        Tag(content = Text(messages("viewPastPayments.status.partiallyPaid")), classes = Constants.yellowTagCssClass)
      case OutstandingPaymentStatusToDisplay.NothingToPay  =>
        Tag(content = Text(messages("viewPastPayments.status.nothingToPay")), classes = Constants.greyTagCssClass)
    }
    new GovukTag()(tag)
  }
}
