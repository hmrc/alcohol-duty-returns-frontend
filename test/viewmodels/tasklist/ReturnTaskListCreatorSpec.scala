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

package viewmodels.tasklist

import base.SpecBase
import models.adjustment.AdjustmentEntry
import models.returns.{AlcoholDuty, DutyByTaxType}
import models.{AlcoholRegime, CheckMode, NormalMode, UserAnswers}
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import pages.adjustment.{AdjustmentEntryListPage, AdjustmentListPage, DeclareAdjustmentQuestionPage, OverDeclarationReasonPage, OverDeclarationTotalPage, UnderDeclarationReasonPage, UnderDeclarationTotalPage}
import pages.dutySuspended._
import pages.returns.{AlcoholDutyPage, AlcoholTypePage, DeclareAlcoholDutyQuestionPage, WhatDoYouNeedToDeclarePage}
import pages.spiritsQuestions._
import play.api.Application
import play.api.i18n.Messages
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text

class ReturnTaskListCreatorSpec extends SpecBase {
  val application: Application    = applicationBuilder().build()
  implicit val messages: Messages = getMessages(application)
  val returnTaskListCreator       = new ReturnTaskListCreator()
  val pageNumber                  = 1

  "on calling returnSection" - {

    "when the user answers object is empty, must return the not started section" in {
      val result = returnTaskListCreator.returnSection(emptyUserAnswers)

      result.completedTask                     shouldBe false
      result.taskList.items.size               shouldBe 1
      result.title                             shouldBe messages("taskList.section.returns.heading")
      result.taskList.items.head.title.content shouldBe Text(
        messages("taskList.section.returns.needToDeclare.notStarted")
      )
      result.taskList.items.head.status        shouldBe AlcholDutyTaskListItemStatus.notStarted
      result.taskList.items.head.href          shouldBe Some(
        controllers.returns.routes.DeclareAlcoholDutyQuestionController.onPageLoad(NormalMode).url
      )
    }

    "when the user answers no to DeclareAlcoholDuty question, must return a complete section" in {
      val userAnswers = emptyUserAnswers
        .set(DeclareAlcoholDutyQuestionPage, false)
        .success
        .value
      val result      = returnTaskListCreator.returnSection(userAnswers)

      result.completedTask                     shouldBe true
      result.taskList.items.size               shouldBe 1
      result.title                             shouldBe messages("taskList.section.returns.heading")
      result.taskList.items.head.title.content shouldBe Text(
        messages("taskList.section.returns.needToDeclare.no")
      )
      result.taskList.items.head.status        shouldBe AlcholDutyTaskListItemStatus.completed
      result.taskList.items.head.href          shouldBe Some(
        controllers.returns.routes.DeclareAlcoholDutyQuestionController.onPageLoad(CheckMode).url
      )
    }

    "when the user answers yes to DeclareAlcoholDuty question, and the AlcoholType screen must return a In progress section" - {
      val declaredAlcoholDutyUserAnswer = emptyUserAnswers
        .set(DeclareAlcoholDutyQuestionPage, true)
        .success
        .value

      val result = returnTaskListCreator.returnSection(declaredAlcoholDutyUserAnswer)

      result.completedTask                     shouldBe false
      result.taskList.items.size               shouldBe 1
      result.title                             shouldBe messages("taskList.section.returns.heading")
      result.taskList.items.head.title.content shouldBe Text(
        messages("taskList.section.returns.needToDeclare.yes")
      )
      result.taskList.items.head.status        shouldBe AlcholDutyTaskListItemStatus.inProgress
      result.taskList.items.head.href          shouldBe Some(
        controllers.returns.routes.DeclareAlcoholDutyQuestionController.onPageLoad(CheckMode).url
      )

    }

    "when the user answers yes to DeclareAlcoholDuty question, must return a complete section and the regime tasks" - {
      val declaredAlcoholDutyUserAnswer = emptyUserAnswers
        .set(DeclareAlcoholDutyQuestionPage, true)
        .success
        .value
        .set(AlcoholTypePage, AlcoholRegime.values.toSet)
        .success
        .value

      "must have a link to the 'What do you need to declare?' screen if the user has not answered any other question" in {
        val result = returnTaskListCreator.returnSection(declaredAlcoholDutyUserAnswer)

        result.completedTask                     shouldBe false
        result.taskList.items.size               shouldBe AlcoholRegime.values.toSet.size + 1
        result.title                             shouldBe messages("taskList.section.returns.heading")
        result.taskList.items.head.title.content shouldBe Text(
          messages("taskList.section.returns.needToDeclare.yes")
        )
        result.taskList.items.head.status        shouldBe AlcholDutyTaskListItemStatus.completed
        result.taskList.items.head.href          shouldBe Some(
          controllers.returns.routes.DeclareAlcoholDutyQuestionController.onPageLoad(CheckMode).url
        )

        AlcoholRegime.values.foreach(regime =>
          result.taskList.items
            .find(_.title.content == Text(messages(s"taskList.section.returns.${regime.toString}"))) match {
            case Some(task) =>
              task.status shouldBe AlcholDutyTaskListItemStatus.notStarted
              task.href   shouldBe Some(
                controllers.returns.routes.WhatDoYouNeedToDeclareController.onPageLoad(NormalMode, regime).url
              )
            case None       => fail(s"Task for regime $regime not found")
          }
        )
      }

      "when return data is filled in it must return a completed section" in {

        val regime          = regimeGen.sample.value
        val rateBands       = genListOfRateBandForRegime(regime).sample.value.toSet
        val volumesAndRates = arbitraryVolumeAndRateByTaxType(
          rateBands.toSeq
        ).arbitrary.sample.value

        val dutiesByTaxType = volumesAndRates.map { volumeAndRate =>
          val totalDuty = volumeAndRate.dutyRate * volumeAndRate.pureAlcohol
          DutyByTaxType(
            taxType = volumeAndRate.taxType,
            totalLitres = volumeAndRate.totalLitres,
            pureAlcohol = volumeAndRate.pureAlcohol,
            dutyRate = volumeAndRate.dutyRate,
            dutyDue = totalDuty
          )
        }

        val alcoholDuty = AlcoholDuty(
          dutiesByTaxType = dutiesByTaxType,
          totalDuty = dutiesByTaxType.map(_.dutyDue).sum
        )

        val result = returnTaskListCreator.returnSection(
          declaredAlcoholDutyUserAnswer
            .setByKey(AlcoholDutyPage, regime, alcoholDuty)
            .success
            .value
            .set(AlcoholTypePage, Set(regime))
            .success
            .value
        )

        result.completedTask                     shouldBe true
        result.taskList.items.size               shouldBe 2
        result.title                             shouldBe messages("taskList.section.returns.heading")
        result.taskList.items.head.title.content shouldBe Text(
          messages("taskList.section.returns.needToDeclare.yes")
        )
        result.taskList.items.head.status        shouldBe AlcholDutyTaskListItemStatus.completed
        result.taskList.items.head.href          shouldBe Some(
          controllers.returns.routes.DeclareAlcoholDutyQuestionController.onPageLoad(CheckMode).url
        )

        result.taskList.items
          .find(_.title.content == Text(messages(s"taskList.section.returns.${regime.toString}"))) match {
          case Some(task) =>
            task.status shouldBe AlcholDutyTaskListItemStatus.completed
            task.href   shouldBe Some(
              controllers.returns.routes.CheckYourAnswersController.onPageLoad(regime).url
            )
          case None       => fail(s"Task for regime $regime not found")
        }

      }

      "when return data is not completely filled in it must return an In progress section" in {

        val regime = regimeGen.sample.value

        val rateBandList = genListOfRateBandForRegime(regime).sample.value

        val result = returnTaskListCreator.returnSection(
          declaredAlcoholDutyUserAnswer
            .setByKey(WhatDoYouNeedToDeclarePage, regime, rateBandList.toSet)
            .success
            .value
            .set(AlcoholTypePage, Set(regime))
            .success
            .value
        )

        result.completedTask                     shouldBe false
        result.taskList.items.size               shouldBe 2
        result.title                             shouldBe messages("taskList.section.returns.heading")
        result.taskList.items.head.title.content shouldBe Text(
          messages("taskList.section.returns.needToDeclare.yes")
        )
        result.taskList.items.head.status        shouldBe AlcholDutyTaskListItemStatus.completed
        result.taskList.items.head.href          shouldBe Some(
          controllers.returns.routes.DeclareAlcoholDutyQuestionController.onPageLoad(CheckMode).url
        )

        result.taskList.items
          .find(_.title.content == Text(messages(s"taskList.section.returns.${regime.toString}"))) match {
          case Some(task) =>
            task.status shouldBe AlcholDutyTaskListItemStatus.inProgress
            task.href   shouldBe Some(
              controllers.returns.routes.WhatDoYouNeedToDeclareController.onPageLoad(NormalMode, regime).url
            )
          case None       => fail(s"Task for regime $regime not found")
        }

      }

    }
  }

  "on calling returnAdjustmentSection" - {

    "when the user answers object is empty, must return a not started section" in {
      val result = returnTaskListCreator.returnAdjustmentSection(emptyUserAnswers)

      result.completedTask                     shouldBe false
      result.taskList.items.size               shouldBe 1
      result.title                             shouldBe messages("taskList.section.adjustment.heading")
      result.taskList.items.head.title.content shouldBe Text(
        messages("taskList.section.adjustment.needToDeclare.notStarted")
      )
      result.taskList.items.head.status        shouldBe AlcholDutyTaskListItemStatus.notStarted
      result.taskList.items.head.href          shouldBe Some(
        controllers.adjustment.routes.DeclareAdjustmentQuestionController.onPageLoad(NormalMode).url
      )
    }

    "when the user answers no to DeclareAdjustment question, must return a complete section" in {
      val userAnswers = emptyUserAnswers
        .set(DeclareAdjustmentQuestionPage, false)
        .success
        .value
      val result      = returnTaskListCreator.returnAdjustmentSection(userAnswers)

      result.completedTask                     shouldBe true
      result.taskList.items.size               shouldBe 1
      result.title                             shouldBe messages("taskList.section.adjustment.heading")
      result.taskList.items.head.title.content shouldBe Text(
        messages("taskList.section.adjustment.needToDeclare.no")
      )
      result.taskList.items.head.status        shouldBe AlcholDutyTaskListItemStatus.completed
      result.taskList.items.head.href          shouldBe Some(
        controllers.adjustment.routes.DeclareAdjustmentQuestionController.onPageLoad(CheckMode).url
      )
    }

    "when the user answers yes to DeclareAdjustment question, must return a complete section and the other task" - {
      val declaredAdjustmentUserAnswer = emptyUserAnswers
        .set(DeclareAdjustmentQuestionPage, true)
        .success
        .value

      "must have a link to the 'Adjustment List' screen if the user has not answered any other question and the task must be not started" in {
        val result = returnTaskListCreator.returnAdjustmentSection(declaredAdjustmentUserAnswer)

        result.completedTask                     shouldBe false
        result.taskList.items.size               shouldBe 2
        result.title                             shouldBe messages("taskList.section.adjustment.heading")
        result.taskList.items.head.title.content shouldBe Text(
          messages("taskList.section.adjustment.needToDeclare.yes")
        )
        result.taskList.items.head.status        shouldBe AlcholDutyTaskListItemStatus.completed
        result.taskList.items.head.href          shouldBe Some(
          controllers.adjustment.routes.DeclareAdjustmentQuestionController.onPageLoad(CheckMode).url
        )

        result.taskList.items(1).title.content shouldBe Text(
          messages("taskList.section.adjustment.notStarted")
        )
        result.taskList.items(1).status        shouldBe AlcholDutyTaskListItemStatus.notStarted
        result.taskList.items(1).href          shouldBe Some(
          controllers.adjustment.routes.AdjustmentListController.onPageLoad(pageNumber).url
        )
      }

      "must have a link to the 'Adjustment List' screen if the user has answered some questions and the task must be in progress" in {
        val result = returnTaskListCreator.returnAdjustmentSection(
          declaredAdjustmentUserAnswer.set(AdjustmentEntryListPage, List(AdjustmentEntry())).success.value
        )

        result.completedTask                     shouldBe false
        result.taskList.items.size               shouldBe 2
        result.title                             shouldBe messages("taskList.section.adjustment.heading")
        result.taskList.items.head.title.content shouldBe Text(
          messages("taskList.section.adjustment.needToDeclare.yes")
        )
        result.taskList.items.head.status        shouldBe AlcholDutyTaskListItemStatus.completed
        result.taskList.items.head.href          shouldBe Some(
          controllers.adjustment.routes.DeclareAdjustmentQuestionController.onPageLoad(CheckMode).url
        )

        result.taskList.items(1).title.content shouldBe Text(
          messages("taskList.section.adjustment.inProgress")
        )
        result.taskList.items(1).status        shouldBe AlcholDutyTaskListItemStatus.inProgress
        result.taskList.items(1).href          shouldBe Some(
          controllers.adjustment.routes.AdjustmentListController.onPageLoad(pageNumber).url
        )
      }

      "must have a link to the 'Adjustment List' screen if the user has answered some questions and the task must be completed" in {
        val result = returnTaskListCreator.returnAdjustmentSection(
          declaredAdjustmentUserAnswer
            .set(AdjustmentEntryListPage, List(AdjustmentEntry()))
            .success
            .value
            .set(AdjustmentListPage, false)
            .success
            .value
        )

        result.completedTask                     shouldBe true
        result.taskList.items.size               shouldBe 2
        result.title                             shouldBe messages("taskList.section.adjustment.heading")
        result.taskList.items.head.title.content shouldBe Text(
          messages("taskList.section.adjustment.needToDeclare.yes")
        )
        result.taskList.items.head.status        shouldBe AlcholDutyTaskListItemStatus.completed
        result.taskList.items.head.href          shouldBe Some(
          controllers.adjustment.routes.DeclareAdjustmentQuestionController.onPageLoad(CheckMode).url
        )

        result.taskList.items(1).title.content shouldBe Text(
          messages("taskList.section.adjustment.completed")
        )
        result.taskList.items(1).status        shouldBe AlcholDutyTaskListItemStatus.completed
        result.taskList.items(1).href          shouldBe Some(
          controllers.adjustment.routes.AdjustmentListController.onPageLoad(pageNumber).url
        )
      }

      "must have a link to the Under/Over declaration reason screens if the user has totals over 1000 and the task must be Not started" in {
        val result = returnTaskListCreator.returnAdjustmentSection(
          declaredAdjustmentUserAnswer
            .set(AdjustmentEntryListPage, List(AdjustmentEntry()))
            .success
            .value
            .set(AdjustmentListPage, false)
            .success
            .value
            .set(UnderDeclarationTotalPage, BigDecimal(1000))
            .success
            .value
            .set(OverDeclarationTotalPage, BigDecimal(2000))
            .success
            .value
        )

        result.completedTask       shouldBe false
        result.taskList.items.size shouldBe 4

        result.taskList.items(2).title.content shouldBe Text(
          messages("taskList.section.adjustment.under-declaration")
        )
        result.taskList.items(2).status        shouldBe AlcholDutyTaskListItemStatus.notStarted
        result.taskList.items(2).href          shouldBe Some(
          controllers.adjustment.routes.UnderDeclarationReasonController.onPageLoad(NormalMode).url
        )

        result.taskList.items(3).title.content shouldBe Text(
          messages("taskList.section.adjustment.over-declaration")
        )
        result.taskList.items(3).status        shouldBe AlcholDutyTaskListItemStatus.notStarted
        result.taskList.items(3).href          shouldBe Some(
          controllers.adjustment.routes.OverDeclarationReasonController.onPageLoad(NormalMode).url
        )
      }

      "and must have a link to the Under/OverDeclaration reason screens if user has totals over 1000 and reasons are completed then it must be Completed" in {
        val result = returnTaskListCreator.returnAdjustmentSection(
          declaredAdjustmentUserAnswer
            .set(AdjustmentEntryListPage, List(AdjustmentEntry()))
            .success
            .value
            .set(AdjustmentListPage, false)
            .success
            .value
            .set(UnderDeclarationTotalPage, BigDecimal(1000))
            .success
            .value
            .set(OverDeclarationTotalPage, BigDecimal(2000))
            .success
            .value
            .set(UnderDeclarationReasonPage, "test")
            .success
            .value
            .set(OverDeclarationReasonPage, "test")
            .success
            .value
        )

        result.taskList.items(2).title.content shouldBe Text(
          messages("taskList.section.adjustment.under-declaration")
        )
        result.taskList.items(2).status        shouldBe AlcholDutyTaskListItemStatus.completed
        result.taskList.items(2).href          shouldBe Some(
          controllers.adjustment.routes.UnderDeclarationReasonController.onPageLoad(NormalMode).url
        )

        result.taskList.items(3).title.content shouldBe Text(
          messages("taskList.section.adjustment.over-declaration")
        )
        result.taskList.items(3).status        shouldBe AlcholDutyTaskListItemStatus.completed
        result.taskList.items(3).href          shouldBe Some(
          controllers.adjustment.routes.OverDeclarationReasonController.onPageLoad(NormalMode).url
        )
      }

    }
  }

  "on calling returnDSDSection" - {
    "when the user answers object is empty, must return a not started section" in {
      val result = returnTaskListCreator.returnDSDSection(emptyUserAnswers)

      result.completedTask                     shouldBe false
      result.taskList.items.size               shouldBe 1
      result.title                             shouldBe messages("taskList.section.dutySuspended.heading")
      result.taskList.items.head.title.content shouldBe Text(
        messages("taskList.section.dutySuspended.needToDeclare.notStarted")
      )
      result.taskList.items.head.status        shouldBe AlcholDutyTaskListItemStatus.notStarted
      result.taskList.items.head.href          shouldBe Some(
        controllers.dutySuspended.routes.DeclareDutySuspendedDeliveriesQuestionController.onPageLoad(NormalMode).url
      )
    }

    "when the user answers no to Declare DSD question, must return a complete section" in {
      val userAnswers = emptyUserAnswers
        .set(DeclareDutySuspendedDeliveriesQuestionPage, false)
        .success
        .value
      val result      = returnTaskListCreator.returnDSDSection(userAnswers)

      result.completedTask                     shouldBe true
      result.taskList.items.size               shouldBe 1
      result.title                             shouldBe messages("taskList.section.dutySuspended.heading")
      result.taskList.items.head.title.content shouldBe Text(
        messages("taskList.section.dutySuspended.needToDeclare.no")
      )
      result.taskList.items.head.status        shouldBe AlcholDutyTaskListItemStatus.completed
      result.taskList.items.head.href          shouldBe Some(
        controllers.dutySuspended.routes.DeclareDutySuspendedDeliveriesQuestionController.onPageLoad(CheckMode).url
      )
    }

    "when the user answers yes to Declare DSD question, must return a complete section and the DSD task" - {
      val declaredDSDUserAnswer = emptyUserAnswers
        .set(DeclareDutySuspendedDeliveriesQuestionPage, true)
        .success
        .value

      "must have a link to DeclareDutySuspendedDeliveriesQuestionController if the user has not answered any other question" in {
        val result = returnTaskListCreator.returnDSDSection(declaredDSDUserAnswer)

        result.completedTask                     shouldBe false
        result.taskList.items.size               shouldBe 2
        result.title                             shouldBe messages("taskList.section.dutySuspended.heading")
        result.taskList.items.head.title.content shouldBe Text(
          messages("taskList.section.dutySuspended.needToDeclare.yes")
        )
        result.taskList.items.head.status        shouldBe AlcholDutyTaskListItemStatus.completed
        result.taskList.items.head.href          shouldBe Some(
          controllers.dutySuspended.routes.DeclareDutySuspendedDeliveriesQuestionController.onPageLoad(CheckMode).url
        )

        result.taskList.items(1).title.content shouldBe Text(
          messages("taskList.section.dutySuspended.notStarted")
        )
        result.taskList.items(1).status        shouldBe AlcholDutyTaskListItemStatus.notStarted
        result.taskList.items(1).href          shouldBe Some(
          controllers.dutySuspended.routes.DutySuspendedDeliveriesGuidanceController.onPageLoad().url
        )
      }

      "must have a link to DeclareDutySuspendedDeliveriesQuestionController if the user answers yes to DSD question and not all regime questions are answered" in {
        val validTotal                                                = 42.34
        val validPureAlcohol                                          = 34.23
        val incompleteDutySuspendedDeliveriesUserAnswers: UserAnswers = userAnswersWithAllRegimes
          .copy(data =
            Json.obj(
              DutySuspendedBeerPage.toString    -> Json.obj(
                "totalBeer"         -> validTotal,
                "pureAlcoholInBeer" -> validPureAlcohol
              ),
              DutySuspendedCiderPage.toString   -> Json.obj(
                "totalCider"         -> validTotal,
                "pureAlcoholInCider" -> validPureAlcohol
              ),
              DutySuspendedSpiritsPage.toString -> Json.obj(
                "totalSpirits"         -> validTotal,
                "pureAlcoholInSpirits" -> validPureAlcohol
              ),
              DutySuspendedWinePage.toString    -> Json.obj(
                "totalWine"         -> validTotal,
                "pureAlcoholInWine" -> validPureAlcohol
              )
            )
          )
          .set(DeclareDutySuspendedDeliveriesQuestionPage, true)
          .success
          .value

        val result = returnTaskListCreator.returnDSDSection(incompleteDutySuspendedDeliveriesUserAnswers)

        result.completedTask                     shouldBe false
        result.taskList.items.size               shouldBe 2
        result.title                             shouldBe messages("taskList.section.dutySuspended.heading")
        result.taskList.items.head.title.content shouldBe Text(
          messages("taskList.section.dutySuspended.needToDeclare.yes")
        )
        result.taskList.items.head.status        shouldBe AlcholDutyTaskListItemStatus.completed
        result.taskList.items.head.href          shouldBe Some(
          controllers.dutySuspended.routes.DeclareDutySuspendedDeliveriesQuestionController.onPageLoad(CheckMode).url
        )

        result.taskList.items(1).title.content shouldBe Text(
          messages("taskList.section.dutySuspended.inProgress")
        )
        result.taskList.items(1).status        shouldBe AlcholDutyTaskListItemStatus.inProgress
        result.taskList.items(1).href          shouldBe Some(
          controllers.dutySuspended.routes.DutySuspendedDeliveriesGuidanceController.onPageLoad().url
        )
      }

      "must have a link to CYA DSD controller if the user answers yes to the Declare DSD question and all regime questions are answered" in {
        val validTotal                                              = 42.34
        val validPureAlcohol                                        = 34.23
        val completeDutySuspendedDeliveriesUserAnswers: UserAnswers = userAnswersWithAllRegimes
          .copy(data =
            Json.obj(
              DutySuspendedBeerPage.toString           -> Json.obj(
                "totalBeer"         -> validTotal,
                "pureAlcoholInBeer" -> validPureAlcohol
              ),
              DutySuspendedCiderPage.toString          -> Json.obj(
                "totalCider"         -> validTotal,
                "pureAlcoholInCider" -> validPureAlcohol
              ),
              DutySuspendedSpiritsPage.toString        -> Json.obj(
                "totalSpirits"         -> validTotal,
                "pureAlcoholInSpirits" -> validPureAlcohol
              ),
              DutySuspendedWinePage.toString           -> Json.obj(
                "totalWine"         -> validTotal,
                "pureAlcoholInWine" -> validPureAlcohol
              ),
              DutySuspendedOtherFermentedPage.toString -> Json.obj(
                "totalOtherFermented"         -> validTotal,
                "pureAlcoholInOtherFermented" -> validPureAlcohol
              )
            )
          )
          .set(DeclareDutySuspendedDeliveriesQuestionPage, true)
          .success
          .value

        val result = returnTaskListCreator.returnDSDSection(completeDutySuspendedDeliveriesUserAnswers)

        result.completedTask                     shouldBe true
        result.taskList.items.size               shouldBe 2
        result.title                             shouldBe messages("taskList.section.dutySuspended.heading")
        result.taskList.items.head.title.content shouldBe Text(
          messages("taskList.section.dutySuspended.needToDeclare.yes")
        )
        result.taskList.items.head.status        shouldBe AlcholDutyTaskListItemStatus.completed
        result.taskList.items.head.href          shouldBe Some(
          controllers.dutySuspended.routes.DeclareDutySuspendedDeliveriesQuestionController.onPageLoad(CheckMode).url
        )

        result.taskList.items(1).title.content shouldBe Text(
          messages("taskList.section.dutySuspended.completed")
        )
        result.taskList.items(1).status        shouldBe AlcholDutyTaskListItemStatus.completed
        result.taskList.items(1).href          shouldBe Some(
          controllers.dutySuspended.routes.CheckYourAnswersDutySuspendedDeliveriesController.onPageLoad().url
        )
      }
    }
  }

  "on calling returnQSSection" - {
    "when the user answers object is empty, must return a not started section" in {
      val result = returnTaskListCreator.returnQSSection(emptyUserAnswers)

      result.completedTask                     shouldBe false
      result.taskList.items.size               shouldBe 1
      result.title                             shouldBe messages("taskList.section.spirits.heading")
      result.taskList.items.head.title.content shouldBe Text(
        messages("taskList.section.spirits.needToDeclare.notStarted")
      )
      result.taskList.items.head.status        shouldBe AlcholDutyTaskListItemStatus.notStarted
      result.taskList.items.head.href          shouldBe Some(
        controllers.spiritsQuestions.routes.DeclareQuarterlySpiritsController.onPageLoad(NormalMode).url
      )
    }

    "when the user answers no to Declare QS question, must return a complete section" in {
      val userAnswers = emptyUserAnswers
        .set(DeclareQuarterlySpiritsPage, false)
        .success
        .value
      val result      = returnTaskListCreator.returnQSSection(userAnswers)

      result.completedTask                     shouldBe true
      result.taskList.items.size               shouldBe 1
      result.title                             shouldBe messages("taskList.section.spirits.heading")
      result.taskList.items.head.title.content shouldBe Text(
        messages("taskList.section.spirits.needToDeclare.no")
      )
      result.taskList.items.head.status        shouldBe AlcholDutyTaskListItemStatus.completed
      result.taskList.items.head.href          shouldBe Some(
        controllers.spiritsQuestions.routes.DeclareQuarterlySpiritsController.onPageLoad(CheckMode).url
      )
    }

    "when the user answers yes to Declare QS question, must return a complete section and" - {
      "the QS task must have a link to DeclareQuarterlySpiritsController and" - {
        val declareQuarterleySpiritsPage           = Json.obj(DeclareQuarterlySpiritsPage.toString -> true)
        val declareSpiritsTotalPage                = Json.obj(DeclareSpiritsTotalPage.toString -> 10000)
        val whiskyPage                             = Json.obj(WhiskyPage.toString -> Json.obj("scotchWhisky" -> 5000, "irishWhiskey" -> 2500))
        val spiritTypePage                         = Json.obj(SpiritTypePage.toString -> Seq("maltSpirits", "ciderOrPerry"))
        val spiritTypePageWithOther                = Json.obj(SpiritTypePage.toString -> Seq("maltSpirits", "ciderOrPerry", "other"))
        val otherSpiritsProducedPage               = Json.obj(OtherSpiritsProducedPage.toString -> "Coco Pops Vodka")
        val grainsUsedPage                         = Json.obj(
          GrainsUsedPage.toString -> Json.obj(
            "maltedBarleyQuantity"     -> 100000,
            "wheatQuantity"            -> 200000,
            "maizeQuantity"            -> 300000,
            "ryeQuantity"              -> 400000,
            "unmaltedGrainQuantity"    -> 500000,
            "usedMaltedGrainNotBarley" -> false
          )
        )
        val grainsUsedPageWithOther                = Json.obj(
          GrainsUsedPage.toString -> Json.obj(
            "maltedBarleyQuantity"     -> 100000,
            "wheatQuantity"            -> 200000,
            "maizeQuantity"            -> 300000,
            "ryeQuantity"              -> 400000,
            "unmaltedGrainQuantity"    -> 500000,
            "usedMaltedGrainNotBarley" -> true
          )
        )
        val otherMaltedGrainsPage                  = Json.obj(
          OtherMaltedGrainsPage.toString -> Json.obj(
            "otherMaltedGrainsTypes"    -> "Coco Pops",
            "otherMaltedGrainsQuantity" -> 600000,
            "maizeQuantity"             -> 300000
          )
        )
        val alcoholUsedPage                        = Json.obj(
          AlcoholUsedPage.toString -> Json.obj("beer" -> 100, "wine" -> 200, "madeWine" -> 300, "ciderOrPerry" -> 400)
        )
        val ethyleneGasOrMolassesUsedPage          = Json.obj(
          EthyleneGasOrMolassesUsedPage.toString -> Json
            .obj("ethyleneGas" -> 10000, "molasses" -> 20000, "otherIngredients" -> false)
        )
        val ethyleneGasOrMolassesUsedPageWithOther = Json.obj(
          EthyleneGasOrMolassesUsedPage.toString -> Json
            .obj("ethyleneGas" -> 10000, "molasses" -> 20000, "otherIngredients" -> true)
        )
        val otherIngredientsUsedPage               = Json.obj(
          OtherIngredientsUsedPage.toString -> Json.obj(
            "otherIngredientsUsedTypes"    -> "Weetabix",
            "otherIngredientsUsedUnit"     -> "Tonnes",
            "otherIngredientsUsedQuantity" -> 200
          )
        )

        def setUserAnswers(pages: Seq[JsObject]): UserAnswers = emptyUserAnswers.copy(data = pages.reduce(_ ++ _))

        "not be started when no other questions are answered" in {
          val userAnswers = setUserAnswers(Seq(declareQuarterleySpiritsPage))
          val result      = returnTaskListCreator.returnQSSection(userAnswers)

          result.completedTask                     shouldBe false
          result.taskList.items.size               shouldBe 2
          result.title                             shouldBe messages("taskList.section.spirits.heading")
          result.taskList.items.head.title.content shouldBe Text(
            messages("taskList.section.spirits.needToDeclare.yes")
          )
          result.taskList.items.head.status        shouldBe AlcholDutyTaskListItemStatus.completed
          result.taskList.items.head.href          shouldBe Some(
            controllers.spiritsQuestions.routes.DeclareQuarterlySpiritsController.onPageLoad(CheckMode).url
          )

          result.taskList.items(1).title.content shouldBe Text(
            messages("taskList.section.spirits.notStarted")
          )
          result.taskList.items(1).status        shouldBe AlcholDutyTaskListItemStatus.notStarted
          result.taskList.items(1).href          shouldBe Some(
            controllers.spiritsQuestions.routes.DeclareSpiritsTotalController.onPageLoad(NormalMode).url
          )
        }

        Seq(
          ("whisky", declareSpiritsTotalPage),
          ("spirits type", whiskyPage),
          ("grains used", spiritTypePage),
          ("alcohol used", grainsUsedPage),
          ("ingredients used", alcoholUsedPage)
        ).foldLeft(Seq(declareQuarterleySpiritsPage)) { case (completedBefore, (nextPageName, page)) =>
          val completedPages = completedBefore :+ page

          s"be in progress when $nextPageName has just been completed" in {
            val userAnswers = setUserAnswers(completedPages)
            val result      = returnTaskListCreator.returnQSSection(userAnswers)

            result.completedTask                     shouldBe false
            result.taskList.items.size               shouldBe 2
            result.title                             shouldBe messages("taskList.section.spirits.heading")
            result.taskList.items.head.title.content shouldBe Text(
              messages("taskList.section.spirits.needToDeclare.yes")
            )
            result.taskList.items.head.status        shouldBe AlcholDutyTaskListItemStatus.completed
            result.taskList.items.head.href          shouldBe Some(
              controllers.spiritsQuestions.routes.DeclareQuarterlySpiritsController.onPageLoad(CheckMode).url
            )

            result.taskList.items(1).title.content shouldBe Text(
              messages("taskList.section.spirits.inProgress")
            )
            result.taskList.items(1).status        shouldBe AlcholDutyTaskListItemStatus.inProgress
            result.taskList.items(1).href          shouldBe Some(
              controllers.spiritsQuestions.routes.DeclareSpiritsTotalController.onPageLoad(NormalMode).url
            )
          }

          completedPages
        }

        "be in progress when all but other spirits when declared have been completed" in {
          val userAnswers = setUserAnswers(
            Seq(
              declareQuarterleySpiritsPage,
              declareSpiritsTotalPage,
              whiskyPage,
              spiritTypePageWithOther,
              grainsUsedPage,
              alcoholUsedPage,
              ethyleneGasOrMolassesUsedPage
            )
          )
          val result      = returnTaskListCreator.returnQSSection(userAnswers)

          result.taskList.items(1).title.content shouldBe Text(
            messages("taskList.section.spirits.inProgress")
          )
          result.taskList.items(1).status        shouldBe AlcholDutyTaskListItemStatus.inProgress
          result.taskList.items(1).href          shouldBe Some(
            controllers.spiritsQuestions.routes.DeclareSpiritsTotalController.onPageLoad(NormalMode).url
          )
        }

        "be in progress when all but other malted grains used when declared have been completed" in {
          val userAnswers = setUserAnswers(
            Seq(
              declareQuarterleySpiritsPage,
              declareSpiritsTotalPage,
              whiskyPage,
              spiritTypePageWithOther,
              otherSpiritsProducedPage,
              grainsUsedPageWithOther,
              alcoholUsedPage,
              ethyleneGasOrMolassesUsedPage
            )
          )
          val result      = returnTaskListCreator.returnQSSection(userAnswers)

          result.taskList.items(1).title.content shouldBe Text(
            messages("taskList.section.spirits.inProgress")
          )
          result.taskList.items(1).status        shouldBe AlcholDutyTaskListItemStatus.inProgress
          result.taskList.items(1).href          shouldBe Some(
            controllers.spiritsQuestions.routes.DeclareSpiritsTotalController.onPageLoad(NormalMode).url
          )
        }

        "be in progress when all but other ingredients used when declared have been completed" in {
          val userAnswers = setUserAnswers(
            Seq(
              declareQuarterleySpiritsPage,
              declareSpiritsTotalPage,
              whiskyPage,
              spiritTypePageWithOther,
              otherSpiritsProducedPage,
              grainsUsedPageWithOther,
              otherMaltedGrainsPage,
              alcoholUsedPage,
              ethyleneGasOrMolassesUsedPageWithOther
            )
          )
          val result      = returnTaskListCreator.returnQSSection(userAnswers)

          result.taskList.items(1).title.content shouldBe Text(
            messages("taskList.section.spirits.inProgress")
          )
          result.taskList.items(1).status        shouldBe AlcholDutyTaskListItemStatus.inProgress
          result.taskList.items(1).href          shouldBe Some(
            controllers.spiritsQuestions.routes.DeclareSpiritsTotalController.onPageLoad(NormalMode).url
          )
        }

        "must have a link to CYA QS controller if the user answers yes to the Declare QS question and questions without other selections are answered" in {
          val userAnswers = setUserAnswers(
            Seq(
              declareQuarterleySpiritsPage,
              declareSpiritsTotalPage,
              whiskyPage,
              spiritTypePage,
              grainsUsedPage,
              alcoholUsedPage,
              ethyleneGasOrMolassesUsedPage
            )
          )
          val result      = returnTaskListCreator.returnQSSection(userAnswers)

          result.completedTask                     shouldBe true
          result.taskList.items.size               shouldBe 2
          result.title                             shouldBe messages("taskList.section.spirits.heading")
          result.taskList.items.head.title.content shouldBe Text(
            messages("taskList.section.spirits.needToDeclare.yes")
          )
          result.taskList.items.head.status        shouldBe AlcholDutyTaskListItemStatus.completed
          result.taskList.items.head.href          shouldBe Some(
            controllers.spiritsQuestions.routes.DeclareQuarterlySpiritsController.onPageLoad(CheckMode).url
          )

          result.taskList.items(1).title.content shouldBe Text(
            messages("taskList.section.spirits.completed")
          )
          result.taskList.items(1).status        shouldBe AlcholDutyTaskListItemStatus.completed
          result.taskList.items(1).href          shouldBe Some(
            controllers.spiritsQuestions.routes.CheckYourAnswersController.onPageLoad().url
          )
        }

        "must be complete if the user answers yes to the Declare QS question and all questions including spirits produced with other are answered" in {
          val userAnswers = setUserAnswers(
            Seq(
              declareQuarterleySpiritsPage,
              declareSpiritsTotalPage,
              whiskyPage,
              spiritTypePageWithOther,
              otherSpiritsProducedPage,
              grainsUsedPage,
              alcoholUsedPage,
              ethyleneGasOrMolassesUsedPage
            )
          )
          val result      = returnTaskListCreator.returnQSSection(userAnswers)

          result.taskList.items(1).status shouldBe AlcholDutyTaskListItemStatus.completed
        }

        "must be complete if the user answers yes to the Declare QS question and all questions including grains used with other are answered" in {
          val userAnswers = setUserAnswers(
            Seq(
              declareQuarterleySpiritsPage,
              declareSpiritsTotalPage,
              whiskyPage,
              spiritTypePage,
              grainsUsedPageWithOther,
              otherMaltedGrainsPage,
              alcoholUsedPage,
              ethyleneGasOrMolassesUsedPage
            )
          )
          val result      = returnTaskListCreator.returnQSSection(userAnswers)

          result.taskList.items(1).status shouldBe AlcholDutyTaskListItemStatus.completed
        }

        "must be complete if the user answers yes to the Declare QS question and all questions including ethylene or molassess used with other are answered" in {
          val userAnswers = setUserAnswers(
            Seq(
              declareQuarterleySpiritsPage,
              declareSpiritsTotalPage,
              whiskyPage,
              spiritTypePage,
              grainsUsedPageWithOther,
              otherMaltedGrainsPage,
              alcoholUsedPage,
              ethyleneGasOrMolassesUsedPageWithOther,
              otherIngredientsUsedPage
            )
          )
          val result      = returnTaskListCreator.returnQSSection(userAnswers)

          result.taskList.items(1).status shouldBe AlcholDutyTaskListItemStatus.completed
        }
      }
    }
  }

  "on calling checkAndSubmitSection" - {

    "when the other sections are incomplete, must return the cannot start section" in {
      val result = returnTaskListCreator.returnCheckAndSubmitSection(0, 4)

      result.completedTask                     shouldBe false
      result.taskList.items.size               shouldBe 1
      result.title                             shouldBe messages("taskList.section.checkAndSubmit.heading")
      result.taskList.items.head.title.content shouldBe Text(
        messages("taskList.section.checkAndSubmit.needToDeclare")
      )
      result.taskList.items.head.status        shouldBe AlcholDutyTaskListItemStatus.cannotStart
      result.taskList.items.head.href          shouldBe None
    }

    "when the other sections are complete, must return the not started section" in {
      val result = returnTaskListCreator.returnCheckAndSubmitSection(4, 4)

      result.completedTask                     shouldBe false
      result.taskList.items.size               shouldBe 1
      result.title                             shouldBe messages("taskList.section.checkAndSubmit.heading")
      result.taskList.items.head.title.content shouldBe Text(
        messages("taskList.section.checkAndSubmit.needToDeclare")
      )
      result.taskList.items.head.status        shouldBe AlcholDutyTaskListItemStatus.notStarted
      result.taskList.items.head.href          shouldBe Some(
        controllers.checkAndSubmit.routes.DutyDueForThisReturnController.onPageLoad().url
      )
    }
  }
}
