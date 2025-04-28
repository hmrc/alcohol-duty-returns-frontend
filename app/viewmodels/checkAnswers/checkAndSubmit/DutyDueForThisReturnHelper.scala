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

package viewmodels.checkAnswers.checkAndSubmit

import cats.data.EitherT
import config.Constants.Css
import config.FrontendAppConfig
import connectors.AlcoholDutyCalculatorConnector
import models.AlcoholRegime.{Beer, Cider, OtherFermentedProduct, Spirits, Wine}
import models.checkAndSubmit.{AdrDutySuspended, AdrSpirits}
import models.declareDuty.AlcoholDuty
import models.{AlcoholRegime, NormalMode, ReturnPeriod, UserAnswers}
import pages.adjustment.{AdjustmentTotalPage, DeclareAdjustmentQuestionPage}
import pages.declareDuty.{AlcoholDutyPage, DeclareAlcoholDutyQuestionPage}
import play.api.Logging
import play.api.i18n.Messages
import services.checkAndSubmit.AdrReturnSubmissionService
import uk.gov.hmrc.govukfrontend.views.Aliases.Value
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{SummaryList, SummaryListRow}
import uk.gov.hmrc.http.HeaderCarrier
import viewmodels.Money
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

case class DutyDueForThisReturnViewModel(
  dutiesBreakdownSummaryList: SummaryList,
  youveAlsoDeclaredSummaryList: SummaryList,
  totalDue: BigDecimal
)

class DutyDueForThisReturnHelper @Inject() (
  calculatorConnector: AlcoholDutyCalculatorConnector,
  adrReturnSubmissionService: AdrReturnSubmissionService,
  appConfig: FrontendAppConfig
)(implicit executionContext: ExecutionContext)
    extends Logging {

  private val dutyDueOrder = Seq(Beer, Cider, Wine, Spirits, OtherFermentedProduct)

  def getDutyDueViewModel(
    userAnswers: UserAnswers,
    returnPeriod: ReturnPeriod
  )(implicit hc: HeaderCarrier, messages: Messages): EitherT[Future, String, DutyDueForThisReturnViewModel] =
    for {
      dutiesByRegime             <- getDutiesByAlcoholRegime(userAnswers)
      totalAdjustment            <- getTotalAdjustment(userAnswers)
      total                      <- calculateTotal(dutiesByRegime, totalAdjustment)
      dutiesBreakdownViewModel   <- createDutySummaryList(dutiesByRegime, totalAdjustment)
      dutySuspended              <- adrReturnSubmissionService.getDutySuspended(userAnswers)
      maybeSpirits               <- adrReturnSubmissionService.getSpirits(userAnswers, returnPeriod)
      youveAlsoAnsweredViewModel <- createYouveAlsoAnsweredSummaryList(dutySuspended, maybeSpirits)
    } yield DutyDueForThisReturnViewModel(
      dutiesBreakdownViewModel,
      youveAlsoAnsweredViewModel,
      total
    )

  private def getTotalAdjustment(userAnswers: UserAnswers): EitherT[Future, String, BigDecimal] =
    (userAnswers.get(DeclareAdjustmentQuestionPage), userAnswers.get(AdjustmentTotalPage)) match {
      case (Some(false), _)                    => EitherT.rightT[Future, String](BigDecimal(0))
      case (Some(true), Some(adjustmentTotal)) => EitherT.rightT[Future, String](adjustmentTotal)
      case (_, _)                              => EitherT.leftT[Future, BigDecimal]("Unable to get adjustment totals when calculating duty due")
    }

  private def getDutiesByAlcoholRegime(
    userAnswers: UserAnswers
  ): EitherT[Future, String, Seq[Tuple2[AlcoholRegime, AlcoholDuty]]] =
    (userAnswers.get(DeclareAlcoholDutyQuestionPage), userAnswers.get(AlcoholDutyPage)) match {
      case (Some(false), _)                  => EitherT.rightT(Seq.empty)
      case (Some(true), Some(alcoholDuties)) =>
        EitherT.rightT(alcoholDuties.toSeq.sortBy { case (regime, _) => dutyDueOrder.indexOf(regime) })
      case (_, _)                            => EitherT.leftT("Unable to get duties due when calculating duty due")
    }

  private def calculateTotal(dutiesByAlcoholRegime: Seq[Tuple2[AlcoholRegime, AlcoholDuty]], adjustment: BigDecimal)(
    implicit hc: HeaderCarrier
  ): EitherT[Future, String, BigDecimal] = EitherT {
    calculatorConnector
      .calculateTotalAdjustment(dutiesByAlcoholRegime.map(_._2.totalDuty) :+ adjustment)
      .flatMap(response => Future.successful(Right(response.duty)))
      .recover { case e: Exception =>
        Left(s"Failed to calculate total duty due: ${e.getMessage}")
      }
  }

  private def createDutySummaryList(dutiesByRegime: Seq[(AlcoholRegime, AlcoholDuty)], totalAdjustment: BigDecimal)(
    implicit messages: Messages
  ): EitherT[Future, String, SummaryList] = {
    val returnDutiesRow = createReturnSummaryListRows(dutiesByRegime)
    val adjustmentRow   = createSummaryListAdjustmentRow(totalAdjustment)
    EitherT.rightT[Future, String](
      SummaryList(
        rows = returnDutiesRow :+ adjustmentRow
      )
    )
  }

  private def createYouveAlsoAnsweredSummaryList(dutySuspended: AdrDutySuspended, maybeSpirits: Option[AdrSpirits])(
    implicit messages: Messages
  ): EitherT[Future, String, SummaryList] = {
    val returnDutiesRow = createDutySuspendedSummaryListRow(dutySuspended.declared)
    val maybeSpiritsRow = maybeSpirits.map(spirits => createSpiritsSummaryListRow(spirits.spiritsDeclared))
    EitherT.rightT[Future, String](
      SummaryList(
        rows = maybeSpiritsRow.fold(returnDutiesRow)(returnDutiesRow ++ _)
      )
    )
  }

  private def createReturnSummaryListRows(dutiesByRegime: Seq[(AlcoholRegime, AlcoholDuty)])(implicit
    messages: Messages
  ): Seq[SummaryListRow] =
    if (dutiesByRegime.isEmpty) {
      Seq(
        SummaryListRowViewModel(
          key = messages("dutyDueForThisReturn.table.nil.label"),
          value = ValueViewModel(messages("dutyDueForThisReturn.table.nil.value")),
          actions = Seq(
            ActionItemViewModel(
              content = Text(messages("site.change")),
              href = controllers.declareDuty.routes.DeclareAlcoholDutyQuestionController.onPageLoad(NormalMode).url
            ).withVisuallyHiddenText(messages("dutyDueForThisReturn.table.nil.label"))
          )
        )
      )
    } else {
      dutiesByRegime.map { case (alcoholRegime, alcoholDuty) =>
        SummaryListRowViewModel(
          key = messages(s"dutyDueForThisReturn.table.dutyDue.${alcoholRegime.regimeMessageKey}"),
          value = Value(
            content = Text(Money.format(alcoholDuty.totalDuty)),
            classes = s"${Css.noWrap}"
          ),
          actions = Seq(
            ActionItemViewModel(
              content = Text(messages("site.change")),
              href = controllers.declareDuty.routes.CheckYourAnswersController.onPageLoad(alcoholRegime).url
            ).withVisuallyHiddenText(messages(s"dutyDueForThisReturn.table.dutyDue.${alcoholRegime.regimeMessageKey}"))
          )
        )
      }
    }

  private def createDutySuspendedSummaryListRow(
    hasDutySuspendedDeclaration: Boolean
  )(implicit messages: Messages): Seq[SummaryListRow] =
    if (hasDutySuspendedDeclaration) {
      Seq(
        SummaryListRowViewModel(
          key = messages("dutyDueForThisReturn.dutySuspended.alcohol"),
          value = ValueViewModel(messages("dutyDueForThisReturn.dutySuspended.declared")),
          actions = Seq(
            ActionItemViewModel(
              content = Text(messages("site.change")),
              href = if (appConfig.dutySuspendedNewJourneyEnabled) {
                controllers.dutySuspendedNew.routes.CheckYourAnswersController.onPageLoad().url
              } else {
                controllers.dutySuspended.routes.CheckYourAnswersDutySuspendedDeliveriesController.onPageLoad().url
              }
            ).withVisuallyHiddenText(messages("dutyDueForThisReturn.dutySuspended.alcohol"))
          )
        )
      )
    } else {
      Seq(
        SummaryListRowViewModel(
          key = messages("dutyDueForThisReturn.dutySuspended.alcohol"),
          value = ValueViewModel(messages("dutyDueForThisReturn.dutySuspended.nothingToDeclare")),
          actions = Seq(
            ActionItemViewModel(
              content = Text(messages("site.change")),
              href = if (appConfig.dutySuspendedNewJourneyEnabled) {
                controllers.dutySuspendedNew.routes.DeclareDutySuspenseQuestionController.onPageLoad(NormalMode).url
              } else {
                controllers.dutySuspended.routes.DeclareDutySuspendedDeliveriesQuestionController
                  .onPageLoad(NormalMode)
                  .url
              }
            ).withVisuallyHiddenText(messages("dutyDueForThisReturn.dutySuspended.alcohol"))
          )
        )
      )
    }

  private def createSpiritsSummaryListRow(hasSpirits: Boolean)(implicit messages: Messages): Seq[SummaryListRow] =
    if (hasSpirits) {
      Seq(
        SummaryListRowViewModel(
          key = messages("dutyDueForThisReturn.spirits.production"),
          value = ValueViewModel(messages("dutyDueForThisReturn.spirits.declared")),
          actions = Seq(
            ActionItemViewModel(
              content = Text(messages("site.change")),
              href = controllers.spiritsQuestions.routes.CheckYourAnswersController.onPageLoad().url
            ).withVisuallyHiddenText(messages("dutyDueForThisReturn.spirits.production"))
          )
        )
      )
    } else {
      Seq(
        SummaryListRowViewModel(
          key = messages("dutyDueForThisReturn.spirits.production"),
          value = ValueViewModel(messages("dutyDueForThisReturn.spirits.nothingToDeclare")),
          actions = Seq(
            ActionItemViewModel(
              content = Text(messages("site.change")),
              href = controllers.spiritsQuestions.routes.DeclareQuarterlySpiritsController.onPageLoad(NormalMode).url
            ).withVisuallyHiddenText(messages("dutyDueForThisReturn.spirits.production"))
          )
        )
      )
    }

  private def createSummaryListAdjustmentRow(totalAdjustment: BigDecimal)(implicit messages: Messages): SummaryListRow =
    if (totalAdjustment == 0) {
      SummaryListRowViewModel(
        key = messages("dutyDueForThisReturn.table.adjustmentDue"),
        value = ValueViewModel(messages("dutyDueForThisReturn.table.nil.value")),
        actions = Seq(
          ActionItemViewModel(
            content = Text(messages("site.change")),
            href = controllers.adjustment.routes.DeclareAdjustmentQuestionController.onPageLoad(NormalMode).url
          ).withVisuallyHiddenText(messages("dutyDueForThisReturn.table.adjustmentDue"))
        )
      )
    } else {
      SummaryListRowViewModel(
        key = messages("dutyDueForThisReturn.table.adjustmentDue"),
        value = Value(
          content = Text(Money.format(totalAdjustment)),
          classes = s"${Css.noWrap}"
        ),
        actions = Seq(
          ActionItemViewModel(
            content = Text(messages("site.change")),
            href = controllers.adjustment.routes.AdjustmentListController.onPageLoad(1).url
          ).withVisuallyHiddenText(messages("dutyDueForThisReturn.table.adjustmentDue"))
        )
      )
    }
}
