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

package viewmodels.checkAnswers.adjustment

import connectors.AlcoholDutyCalculatorConnector
import models.UserAnswers
import models.adjustment.{AdjustmentDuty, AdjustmentType}
import models.adjustment.AdjustmentType.{Overdeclaration, Underdeclaration}
import pages.adjustment.{AdjustmentEntryListPage, OverDeclarationTotalPage, UnderDeclarationTotalPage}
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AdjustmentOverUnderDeclarationCalculationHelper @Inject() (
  calculatorConnector: AlcoholDutyCalculatorConnector
)(implicit ec: ExecutionContext) {
  def fetchOverUnderDeclarationTotals(
    userAnswers: UserAnswers,
    value: Boolean = false
  )(implicit hc: HeaderCarrier): Future[UserAnswers] =
    if (value) {
      Future.successful(userAnswers)
    } else {
      val underDeclarationDuties = getDutiesByAdjustmentType(Underdeclaration, userAnswers)
      val overDeclarationDuties  = getDutiesByAdjustmentType(Overdeclaration, userAnswers)

      val underDeclarationTotalFuture = calculateTotalDuty(underDeclarationDuties)
      val overDeclarationTotalFuture  = calculateTotalDuty(overDeclarationDuties)

      for {
        underDeclarationTotal           <- underDeclarationTotalFuture
        overDeclarationTotal            <- overDeclarationTotalFuture
        userAnswersWithUnderDeclaration <-
          Future.fromTry(userAnswers.set(UnderDeclarationTotalPage, underDeclarationTotal.duty))
        updatedUserAnswers              <-
          Future.fromTry(userAnswersWithUnderDeclaration.set(OverDeclarationTotalPage, overDeclarationTotal.duty))
      } yield updatedUserAnswers
    }

  private def getDutiesByAdjustmentType(adjustmentType: AdjustmentType, userAnswers: UserAnswers): Seq[BigDecimal] =
    userAnswers
      .get(AdjustmentEntryListPage)
      .toSeq
      .flatten
      .filter(_.adjustmentType.contains(adjustmentType))
      .flatMap(_.duty)

  private def calculateTotalDuty(duties: Seq[BigDecimal])(implicit hc: HeaderCarrier): Future[AdjustmentDuty] =
    if (duties.nonEmpty) {
      calculatorConnector.calculateTotalAdjustment(duties)
    } else {
      Future.successful(AdjustmentDuty(BigDecimal(0)))
    }
}
