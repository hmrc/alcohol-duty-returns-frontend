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

import models.productEntry.ProductEntry
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{SummaryList, SummaryListRow}
import viewmodels.govuk.summarylist._

object CheckYourAnswersSummaryListHelper {

  def currentProductEntrySummaryList(
    productEntry: ProductEntry
  )(implicit messages: Messages): Option[SummaryList] = {

    val productName         = getOptionalRow(ProductNameSummary.row(productEntry))
    val draughtRelief       = getOptionalRow(DraughtReliefQuestionSummary.row(productEntry))
    val smallProducerRelief = getOptionalRow(SmallProducerReliefQuestionSummary.row(productEntry))

    for {
      alcoholByVolume <- AlcoholByVolumeQuestionSummary.row(productEntry)
      taxType         <- TaxTypeSummary.row(productEntry)
      productVolume   <- ProductVolumeSummary.row(productEntry)
      calculations    <- CurrentProductEntrySummary.calculations(productEntry)
    } yield SummaryListViewModel(
      rows = productName ++
        Seq(alcoholByVolume) ++
        draughtRelief ++
        smallProducerRelief ++
        Seq(taxType, productVolume) ++
        calculations
    )
  }

  private def getOptionalRow(row: Option[SummaryListRow]): Seq[SummaryListRow] =
    row match {
      case Some(row) => Seq(row)
      case None      => Seq.empty
    }
}
