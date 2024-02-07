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

package viewmodels.checkAnswers.productEntry

import models.UserAnswers
import models.productEntry.ProductEntry
import play.api.i18n.Messages
import viewmodels.implicits._
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.{SummaryListNoValue, SummaryListRowNoValue}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Actions, Key}

object ProductEntryListSummary {

  /*def row(productList: Seq[ProductEntry])(implicit messages: Messages): Option[SummaryListNoValue] =
    productList => SummaryListNoValue(
      rows = productList,

    )*/
  def row(productList: Seq[ProductEntry])(implicit messages: Messages): Seq[SummaryListRowNoValue] = {
    for {
      i <- productList.indices
      product = productList(i)
      name <- product.name
    } yield {
      //val productName = Seq(name)
      SummaryListRowNoValue(
        key = Key(name).withCssClass("govuk-!-font-weight-regular hmrc-summary-list__key"),
        actions = Some(
          Actions(items = Seq(
            ActionItemViewModel("site.change", controllers.letting.routes.CheckYourAnswersController.onPageLoad(i, mode).url)
              .withCssClass("summary-list-change-link")
              .withVisuallyHiddenText(addressLines),
            ActionItemViewModel("site.remove", routes.PropertyAddedController.remove(i, mode).url)
              .withCssClass("summary-list-remove-link")
              .withVisuallyHiddenText(addressLines)
          )
          )))
    }
  }
}
