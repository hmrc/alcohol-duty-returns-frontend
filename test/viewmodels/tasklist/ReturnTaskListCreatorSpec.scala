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
import models.returns.AlcoholDuty
import models.{AlcoholRegime, CheckMode, NormalMode, UserAnswers}
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import pages.dutySuspended.{DeclareDutySuspendedDeliveriesQuestionPage, DutySuspendedBeerPage, DutySuspendedCiderPage, DutySuspendedOtherFermentedPage, DutySuspendedSpiritsPage, DutySuspendedWinePage}
import pages.productEntry.ProductEntryListPage
import pages.returns.{AlcoholDutyPage, DeclareAlcoholDutyQuestionPage, WhatDoYouNeedToDeclarePage}
import pages.spiritsQuestions.{AlcoholUsedPage, DeclareQuarterlySpiritsPage, DeclareSpiritsTotalPage, EthyleneGasOrMolassesUsedPage, GrainsUsedPage, OtherIngredientsUsedPage, OtherMaltedGrainsPage, OtherSpiritsProducedPage, SpiritTypePage, WhiskyPage}
import play.api.Application
import play.api.i18n.Messages
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text

class ReturnTaskListCreatorSpec extends SpecBase {
  val application: Application    = applicationBuilder().build()
  implicit val messages: Messages = messages(application)
  val returnTaskListCreator       = new ReturnTaskListCreator()

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

    "when the user answers yes to DeclareAlcoholDuty question, must return a complete section and the regime tasks" - {
      val declaredAlcoholDutyUserAnswer = emptyUserAnswers
        .set(DeclareAlcoholDutyQuestionPage, true)
        .success
        .value

      "must have a link to the 'What do you need to declare?' screen if the user has not answered any other question" in {
        val result = returnTaskListCreator.returnSection(declaredAlcoholDutyUserAnswer)

        result.completedTask                     shouldBe false
        result.taskList.items.size               shouldBe AlcoholRegime.values.size + 1
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

      "must have a link to 'What do you need to declare?' screen and status set as 'In Progress' if the user has answered some questions" in {
        val userAnswers = declaredAlcoholDutyUserAnswer
          .set(ProductEntryListPage, Seq.empty)
          .success
          .value

        val filledUserAnswers = AlcoholRegime.values.foldRight(userAnswers) { (regime, ua) =>
          val rateBands = genListOfRateBandForRegime(regime).sample.value
          ua
            .setByKey(WhatDoYouNeedToDeclarePage, regime, rateBands.toSet)
            .success
            .value
        }

        val result = returnTaskListCreator.returnSection(filledUserAnswers)
        result.completedTask                     shouldBe false
        result.taskList.items.size               shouldBe AlcoholRegime.values.size + 1
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
              task.status shouldBe AlcholDutyTaskListItemStatus.inProgress
              task.href   shouldBe Some(
                controllers.returns.routes.WhatDoYouNeedToDeclareController.onPageLoad(NormalMode, regime).url
              )
            case None       => fail(s"Task for regime $regime not found")
          }
        )
      }

      "must have a link to 'CheckYourAnswers' screen and status set as 'Completed' if the user has answered all the questions" in {
        val userAnswers = declaredAlcoholDutyUserAnswer
          .set(ProductEntryListPage, Seq.empty)
          .success
          .value

        val filledUserAnswers = AlcoholRegime.values.foldRight(userAnswers) { (regime, ua) =>
          val rateBands = genListOfRateBandForRegime(regime).sample.value

          val dutiesByTaxType = arbitraryDutyByTaxType(rateBands).arbitrary.sample.value

          val alcoholDutyMock = AlcoholDuty(
            dutiesByTaxType = dutiesByTaxType,
            totalDuty = dutiesByTaxType.map(_.dutyRate).sum
          )

          ua
            .setByKey(WhatDoYouNeedToDeclarePage, regime, rateBands.toSet)
            .success
            .value
            .setByKey(AlcoholDutyPage, regime, alcoholDutyMock)
            .success
            .value
        }

        val result = returnTaskListCreator.returnSection(filledUserAnswers)
        result.completedTask                     shouldBe true
        result.taskList.items.size               shouldBe AlcoholRegime.values.size + 1
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
              task.status shouldBe AlcholDutyTaskListItemStatus.completed
              task.href   shouldBe Some(
                controllers.returns.routes.CheckYourAnswersController.onPageLoad(regime).url
              )
            case None       => fail(s"Task for regime $regime not found")
          }
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
}
