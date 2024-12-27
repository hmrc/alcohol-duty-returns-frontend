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

package viewmodels.declareDuty

import models.RateType.{Core, DraughtRelief}
import models.declareDuty.VolumeAndRateByTaxType
import models.{AlcoholRegime, CheckMode, RateBand, UserAnswers}
import pages.declareDuty.HowMuchDoYouNeedToDeclarePage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Actions, Card, CardTitle, SummaryList, SummaryListRow}
import RateBandHelper.rateBandRecap
import config.Constants.Css
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

class HowMuchDoYouNeedToDeclareSummary {

  def summaryList(regime: AlcoholRegime, rateBands: Set[RateBand], userAnswers: UserAnswers)(implicit
    messages: Messages
  ): Option[SummaryList] =
    userAnswers.getByKey(HowMuchDoYouNeedToDeclarePage, regime) match {
      case Some(dutyByTaxTypes) =>
        Some(
          SummaryList(
            rows = rows(regime, rateBands, dutyByTaxTypes),
            card = Some(
              Card(
                title = Some(
                  CardTitle(content =
                    Text(
                      messages(
                        s"howMuchDoYouNeedToDeclare.checkYourAnswersLabel.cardTitle",
                        messages(s"return.regime.${regime.toString}")
                      ).capitalize
                    )
                  )
                ),
                actions = Some(
                  Actions(
                    items = Seq(
                      ActionItemViewModel(
                        "site.change",
                        controllers.declareDuty.routes.HowMuchDoYouNeedToDeclareController
                          .onPageLoad(CheckMode, regime)
                          .url
                      )
                    )
                  )
                )
              )
            )
          )
        )
      case _                    => None
    }

  private def rows(regime: AlcoholRegime, rateBands: Set[RateBand], dutyByTaxTypes: Seq[VolumeAndRateByTaxType])(
    implicit messages: Messages
  ): Seq[SummaryListRow] = {
    val rateBandsByRateType = rateBands
      .groupBy(_.rateType)

    val coreRows = rateBandsByRateType
      .get(Core)
      .map { coreRateBands =>
        createRowValues(coreRateBands, regime, dutyByTaxTypes)
      }
      .getOrElse(Seq.empty)

    val draughtReliefRows = rateBandsByRateType
      .get(DraughtRelief)
      .map { draughtReliefRateBands =>
        createRowValues(draughtReliefRateBands, regime, dutyByTaxTypes)
      }
      .getOrElse(Seq.empty)

    coreRows ++ draughtReliefRows
  }

  private def createRowValues(
    rateBands: Set[RateBand],
    regime: AlcoholRegime,
    dutyByTaxTypes: Seq[VolumeAndRateByTaxType]
  )(implicit messages: Messages): Seq[SummaryListRow] =
    rateBands.toSeq.sortBy(_.taxTypeCode).flatMap { rateBand =>
      dutyByTaxTypes.find(_.taxType == rateBand.taxTypeCode) match {
        case Some(dutyByTaxType) =>
          Seq(
            SummaryListRowViewModel(
              key = messages("howMuchDoYouNeedToDeclare.checkYourAnswersLabel.row.description"),
              value = ValueViewModel(rateBandRecap(rateBand, Some(regime)))
            ).withCssClass(Css.summaryListRowNoBorderCssClass),
            SummaryListRowViewModel(
              key = messages("howMuchDoYouNeedToDeclare.checkYourAnswersLabel.row.totalLitres"),
              value = ValueViewModel(
                s"${messages("site.2DP", dutyByTaxType.totalLitres)} ${messages("site.unit.litres")}"
              )
            ).withCssClass(Css.summaryListRowNoBorderCssClass),
            SummaryListRowViewModel(
              key = messages("howMuchDoYouNeedToDeclare.checkYourAnswersLabel.row.pureAlcohol"),
              value = ValueViewModel(
                s"${messages("site.4DP", dutyByTaxType.pureAlcohol)} ${messages("site.unit.litres")}"
              )
            )
          )
        case _                   => throw new IllegalArgumentException(s"Invalid tax type: ${rateBand.taxTypeCode}")
      }
    }
}
