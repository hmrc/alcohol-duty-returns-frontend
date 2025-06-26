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
import models.AlcoholRegime._
import models.adjustment.AdjustmentEntry
import models.adjustment.AdjustmentType.Underdeclaration
import models.declareDuty.{AlcoholDuty, DutyByTaxType}
import models.{AlcoholRegime, CheckMode, NormalMode, UserAnswers}
import pages.adjustment._
import pages.declareDuty.{AlcoholDutyPage, AlcoholTypePage, DeclareAlcoholDutyQuestionPage, WhatDoYouNeedToDeclarePage}
import pages.dutySuspended._
import pages.spiritsQuestions._
import play.api.Application
import play.api.i18n.Messages
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text

class ReturnTaskListCreatorSpec extends SpecBase {
  val application: Application    = applicationBuilder().build()
  implicit val messages: Messages = getMessages(application)
  val returnTaskListCreator       = new ReturnTaskListCreator
  val pageNumber                  = 1

  "on calling returnSection" - {
    "when the user hasn't answered the DeclareAlcoholDuty question" - {
      val result = returnTaskListCreator.returnSection(emptyUserAnswers)

      "the task's title must be correct" in {
        result.title mustBe messages("taskList.section.returns.heading")
      }

      "the task must not be completed" in {
        result.completedTask mustBe false
      }

      "only one subtask must be available" in {
        result.taskList.items.size mustBe 1
      }

      "the subtask's title must be correct" in {
        result.taskList.items.head.title.content mustBe Text(
          messages("taskList.section.returns.needToDeclare")
        )
      }

      "the subtask must not be started" in {
        result.taskList.items.head.status mustBe AlcoholDutyTaskListItemStatus.notStarted
      }

      "the subtask must link to the declare alcohol duty question" in {
        result.taskList.items.head.href mustBe Some(
          controllers.declareDuty.routes.DeclareAlcoholDutyQuestionController.onPageLoad(NormalMode).url
        )
      }

      "no hint must be displayed" in {
        result.taskList.items.head.hint.map(_.content) mustBe None
      }

      "the idPrefix must be set" in {
        result.taskList.idPrefix mustBe "returns"
      }
    }

    "when the user answers no to the DeclareAlcoholDuty question" - {
      val userAnswers = emptyUserAnswers
        .set(DeclareAlcoholDutyQuestionPage, false)
        .success
        .value
      val result      = returnTaskListCreator.returnSection(userAnswers)

      "the task's title must be correct" in {
        result.title mustBe messages("taskList.section.returns.heading")
      }

      "the task must be completed" in {
        result.completedTask mustBe true
      }

      "only one subtask must be available" in {
        result.taskList.items.size mustBe 1
      }

      "the subtask's title must be correct" in {
        result.taskList.items.head.title.content mustBe Text(
          messages("taskList.section.returns.needToDeclare")
        )
      }

      "the subtask must be completed" in {
        result.taskList.items.head.status mustBe AlcoholDutyTaskListItemStatus.completed
      }

      "the subtask must link to the declare alcohol duty question in CheckMode" in {
        result.taskList.items.head.href mustBe Some(
          controllers.declareDuty.routes.DeclareAlcoholDutyQuestionController.onPageLoad(CheckMode).url
        )
      }

      "no hint must be displayed" in {
        result.taskList.items.head.hint.map(_.content) mustBe None
      }

      "the idPrefix must be set" in {
        result.taskList.idPrefix mustBe "returns"
      }
    }

    "when the user answers yes to the DeclareAlcoholDuty question" - {
      "but hasn't selected any regimes to declare duty on" - {
        val declaredAlcoholDutyUserAnswer = emptyUserAnswers
          .set(DeclareAlcoholDutyQuestionPage, true)
          .success
          .value

        val result = returnTaskListCreator.returnSection(declaredAlcoholDutyUserAnswer)

        "the task's title must be correct" in {
          result.title mustBe messages("taskList.section.returns.heading")
        }

        "the task must not be completed" in {
          result.completedTask mustBe false
        }

        "only one subtask must be available" in {
          result.taskList.items.size mustBe 1
        }

        "the subtask's title must be correct" in {
          result.taskList.items.head.title.content mustBe Text(
            messages("taskList.section.returns.needToDeclare")
          )
        }

        "the subtask must be in progress" in {
          result.taskList.items.head.status mustBe AlcoholDutyTaskListItemStatus.inProgress
        }

        "the subtask must link to the declare alcohol duty question in CheckMode" in {
          result.taskList.items.head.href mustBe Some(
            controllers.declareDuty.routes.DeclareAlcoholDutyQuestionController.onPageLoad(CheckMode).url
          )
        }

        "no hint must be displayed" in {
          result.taskList.items.head.hint.map(_.content) mustBe None
        }

        "the idPrefix must be set" in {
          result.taskList.idPrefix mustBe "returns"
        }
      }

      "and has selected all regimes to declare duty on but not started any's task" - {
        val declaredAlcoholDutyUserAnswer = emptyUserAnswers
          .set(DeclareAlcoholDutyQuestionPage, true)
          .success
          .value
          .set(AlcoholTypePage, AlcoholRegime.values.toSet)
          .success
          .value

        val result = returnTaskListCreator.returnSection(declaredAlcoholDutyUserAnswer)

        "the task's title must be correct" in {
          result.title mustBe messages("taskList.section.returns.heading")
        }

        "the task must not be completed" in {
          result.completedTask mustBe false
        }

        "a subtask for each regime must be available in addition to the declaration subtask" in {
          result.taskList.items.size mustBe 1 + AlcoholRegime.values.size
        }

        "the declaration subtask's title must be correct" in {
          result.taskList.items.head.title.content mustBe Text(
            messages("taskList.section.returns.needToDeclare")
          )
        }

        "the declaration subtask must be completed" in {
          result.taskList.items.head.status mustBe AlcoholDutyTaskListItemStatus.completed
        }

        "the declaration subtask must link to the declare alcohol duty question in CheckMode" in {
          result.taskList.items.head.href mustBe Some(
            controllers.declareDuty.routes.DeclareAlcoholDutyQuestionController.onPageLoad(CheckMode).url
          )
        }

        AlcoholRegime.values.foreach { regime =>
          s"the subtask for ${regime.entryName} must be found and not be started" in {
            val maybeTask = result.taskList.items.find(
              _.title.content == Text(messages(s"taskList.section.returns.${regime.entryName}"))
            )

            maybeTask            mustBe defined
            maybeTask.get.status mustBe AlcoholDutyTaskListItemStatus.notStarted
          }

          s"the subtask for ${regime.entryName} must link to the what do you need to declare page for that regime" in {
            val maybeTask = result.taskList.items.find(
              _.title.content == Text(messages(s"taskList.section.returns.${regime.entryName}"))
            )

            maybeTask.get.href mustBe Some(
              controllers.declareDuty.routes.WhatDoYouNeedToDeclareController.onPageLoad(NormalMode, regime).url
            )
          }
        }

        "no hint must be displayed" in {
          result.taskList.items.head.hint.map(_.content) mustBe None
        }

        "the idPrefix must be set" in {
          result.taskList.idPrefix mustBe "returns"
        }
      }

      "and has selected a single regime to declare duty on but not started its task" - {
        AlcoholRegime.values.foreach { regime =>
          s"for ${regime.entryName}" - {
            val declaredAlcoholDutyUserAnswer = emptyUserAnswers
              .set(DeclareAlcoholDutyQuestionPage, true)
              .success
              .value
              .set(AlcoholTypePage, Set(regime))
              .success
              .value

            val result = returnTaskListCreator.returnSection(declaredAlcoholDutyUserAnswer)

            "the task's title must be correct" in {
              result.title mustBe messages("taskList.section.returns.heading")
            }

            "the task must not be completed" in {
              result.completedTask mustBe false
            }

            "a subtask for the regime must must be available in addition to the declaration subtask" in {
              result.taskList.items.size mustBe 2
            }

            "the declaration subtask's title must be correct" in {
              result.taskList.items.head.title.content mustBe Text(
                messages("taskList.section.returns.needToDeclare")
              )
            }

            "the declaration subtask must be completed" in {
              result.taskList.items.head.status mustBe AlcoholDutyTaskListItemStatus.completed
            }

            "the declaration subtask must link to the declare alcohol duty question in CheckMode" in {
              result.taskList.items.head.href mustBe Some(
                controllers.declareDuty.routes.DeclareAlcoholDutyQuestionController.onPageLoad(CheckMode).url
              )
            }

            s"the sub task for ${regime.entryName} must be found and not be started" in {
              val maybeTask = result.taskList.items.find(
                _.title.content == Text(messages(s"taskList.section.returns.${regime.entryName}"))
              )

              maybeTask            mustBe defined
              maybeTask.get.status mustBe AlcoholDutyTaskListItemStatus.notStarted
            }

            s"the sub task for ${regime.entryName} must link to the what do you need to declare page for that regime" in {
              val maybeTask = result.taskList.items.find(
                _.title.content == Text(messages(s"taskList.section.returns.${regime.entryName}"))
              )

              maybeTask.get.href mustBe Some(
                controllers.declareDuty.routes.WhatDoYouNeedToDeclareController.onPageLoad(NormalMode, regime).url
              )
            }

            "no hint must be displayed" in {
              result.taskList.items.head.hint.map(_.content) mustBe None
            }

            "the idPrefix must be set" in {
              result.taskList.idPrefix mustBe "returns"
            }
          }
        }
      }

      "and has selected a single regime to declare duty on and started but not finished its task" - {
        AlcoholRegime.values.foreach { regime =>
          val rateBands                                            = genListOfRateBandForRegime(regime).sample.value
          val declaredAlcoholDutyUserAnswerAndDeclarationForRegime = emptyUserAnswers
            .set(DeclareAlcoholDutyQuestionPage, true)
            .success
            .value
            .set(AlcoholTypePage, Set(regime))
            .success
            .value
            .setByKey(WhatDoYouNeedToDeclarePage, regime, rateBands.toSet)
            .success
            .value

          s"for ${regime.entryName}" - {
            val result = returnTaskListCreator.returnSection(declaredAlcoholDutyUserAnswerAndDeclarationForRegime)

            "the task's title must be correct" in {
              result.title mustBe messages("taskList.section.returns.heading")
            }

            "the task must not be completed" in {
              result.completedTask mustBe false
            }

            "a subtask for the regime must must be available in addition to the declaration subtask" in {
              result.taskList.items.size mustBe 2
            }

            "the declaration subtask's title must be correct" in {
              result.taskList.items.head.title.content mustBe Text(
                messages("taskList.section.returns.needToDeclare")
              )
            }

            "the declaration subtask must be completed" in {
              result.taskList.items.head.status mustBe AlcoholDutyTaskListItemStatus.completed
            }

            "the declaration subtask must link to the declare alcohol duty question in CheckMode" in {
              result.taskList.items.head.href mustBe Some(
                controllers.declareDuty.routes.DeclareAlcoholDutyQuestionController.onPageLoad(CheckMode).url
              )
            }

            s"the sub task for ${regime.entryName} must be found and be in progress" in {
              val maybeTask = result.taskList.items.find(
                _.title.content == Text(messages(s"taskList.section.returns.${regime.entryName}"))
              )

              maybeTask            mustBe defined
              maybeTask.get.status mustBe AlcoholDutyTaskListItemStatus.inProgress
            }

            s"the sub task for ${regime.entryName} must link to the what do you need to declare page for that regime" in {
              val maybeTask = result.taskList.items.find(
                _.title.content == Text(messages(s"taskList.section.returns.${regime.entryName}"))
              )

              maybeTask.get.href mustBe Some(
                controllers.declareDuty.routes.WhatDoYouNeedToDeclareController.onPageLoad(NormalMode, regime).url
              )
            }

            "no hint must be displayed" in {
              result.taskList.items.head.hint.map(_.content) mustBe None
            }

            "the idPrefix must be set" in {
              result.taskList.idPrefix mustBe "returns"
            }
          }
        }
      }

      "and has selected a single regime to declare duty on and finished its task" - {
        AlcoholRegime.values.foreach { regime =>
          val rateBands       = genListOfRateBandForRegime(regime).sample.value
          val volumesAndRates = arbitraryVolumeAndRateByTaxType(
            rateBands
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

          val declaredAlcoholDutyUserAnswerAndDeclarationAndDutyForRegime = emptyUserAnswers
            .set(DeclareAlcoholDutyQuestionPage, true)
            .success
            .value
            .set(AlcoholTypePage, Set(regime))
            .success
            .value
            .setByKey(WhatDoYouNeedToDeclarePage, regime, rateBands.toSet)
            .success
            .value
            .setByKey(AlcoholDutyPage, regime, alcoholDuty)
            .success
            .value

          s"for ${regime.entryName}" - {
            val result =
              returnTaskListCreator.returnSection(declaredAlcoholDutyUserAnswerAndDeclarationAndDutyForRegime)

            "the task's title must be correct" in {
              result.title mustBe messages("taskList.section.returns.heading")
            }

            "the task must be completed" in {
              result.completedTask mustBe true
            }

            "a subtask for the regime must must be available in addition to the declaration subtask" in {
              result.taskList.items.size mustBe 2
            }

            "the declaration subtask's title must be correct" in {
              result.taskList.items.head.title.content mustBe Text(
                messages("taskList.section.returns.needToDeclare")
              )
            }

            "the declaration subtask must be completed" in {
              result.taskList.items.head.status mustBe AlcoholDutyTaskListItemStatus.completed
            }

            "the declaration subtask must link to the declare alcohol duty question in CheckMode" in {
              result.taskList.items.head.href mustBe Some(
                controllers.declareDuty.routes.DeclareAlcoholDutyQuestionController.onPageLoad(CheckMode).url
              )
            }

            s"the sub task for ${regime.entryName} must be found and be completed" in {
              val maybeTask = result.taskList.items.find(
                _.title.content == Text(messages(s"taskList.section.returns.${regime.entryName}"))
              )

              maybeTask            mustBe defined
              maybeTask.get.status mustBe AlcoholDutyTaskListItemStatus.completed
            }

            s"the sub task for ${regime.entryName} must link to the check your answers page for that regime" in {
              val maybeTask = result.taskList.items.find(
                _.title.content == Text(messages(s"taskList.section.returns.${regime.entryName}"))
              )

              maybeTask.get.href mustBe Some(
                controllers.declareDuty.routes.CheckYourAnswersController.onPageLoad(regime).url
              )
            }

            "no hint must be displayed" in {
              result.taskList.items.head.hint.map(_.content) mustBe None
            }

            "the idPrefix must be set" in {
              result.taskList.idPrefix mustBe "returns"
            }
          }
        }
      }

      "and has selected a two regimes to declare duty on and finished one of the tasks and started the other" - {
        val rateBandsBeer = genListOfRateBandForRegime(Beer).sample.value

        val rateBandsCider       = genListOfRateBandForRegime(Cider).sample.value
        val volumesAndRatesCider = arbitraryVolumeAndRateByTaxType(
          rateBandsCider
        ).arbitrary.sample.value

        val dutiesByTaxTypeCider = volumesAndRatesCider.map { volumeAndRate =>
          val totalDuty = volumeAndRate.dutyRate * volumeAndRate.pureAlcohol
          DutyByTaxType(
            taxType = volumeAndRate.taxType,
            totalLitres = volumeAndRate.totalLitres,
            pureAlcohol = volumeAndRate.pureAlcohol,
            dutyRate = volumeAndRate.dutyRate,
            dutyDue = totalDuty
          )
        }

        val alcoholDutyCider = AlcoholDuty(
          dutiesByTaxType = dutiesByTaxTypeCider,
          totalDuty = dutiesByTaxTypeCider.map(_.dutyDue).sum
        )

        val regimes: Set[AlcoholRegime] = Set(Beer, Cider)

        val declaredAlcoholDutyUserAnswerAndDeclarationAndDutyForRegime = emptyUserAnswers
          .set(DeclareAlcoholDutyQuestionPage, true)
          .success
          .value
          .set(AlcoholTypePage, regimes)
          .success
          .value
          .setByKey(WhatDoYouNeedToDeclarePage, Beer, rateBandsBeer.toSet)
          .success
          .value
          .setByKey(WhatDoYouNeedToDeclarePage, Cider, rateBandsCider.toSet)
          .success
          .value
          .setByKey(AlcoholDutyPage, Cider, alcoholDutyCider)
          .success
          .value

        val result =
          returnTaskListCreator.returnSection(declaredAlcoholDutyUserAnswerAndDeclarationAndDutyForRegime)

        "the task's title must be correct" in {
          result.title mustBe messages("taskList.section.returns.heading")
        }

        "the task must not be completed" in {
          result.completedTask mustBe false
        }

        "a subtask for the regime must must be available in addition to the declaration subtasks" in {
          result.taskList.items.size mustBe 3
        }

        "the declaration subtask's title must be correct" in {
          result.taskList.items.head.title.content mustBe Text(
            messages("taskList.section.returns.needToDeclare")
          )
        }

        "the declaration subtask must be completed" in {
          result.taskList.items.head.status mustBe AlcoholDutyTaskListItemStatus.completed
        }

        "the declaration subtask must link to the declare alcohol duty question in CheckMode" in {
          result.taskList.items.head.href mustBe Some(
            controllers.declareDuty.routes.DeclareAlcoholDutyQuestionController.onPageLoad(CheckMode).url
          )
        }

        "the sub task for Beer must be found and be in progress" in {
          val maybeTask = result.taskList.items.find(
            _.title.content == Text(messages(s"taskList.section.returns.${Beer.entryName}"))
          )

          maybeTask            mustBe defined
          maybeTask.get.status mustBe AlcoholDutyTaskListItemStatus.inProgress
        }

        "the sub task for Beer must link to the check your answers page for that regime" in {
          val maybeTask = result.taskList.items.find(
            _.title.content == Text(messages(s"taskList.section.returns.${Beer.entryName}"))
          )

          maybeTask.get.href mustBe Some(
            controllers.declareDuty.routes.WhatDoYouNeedToDeclareController.onPageLoad(NormalMode, Beer).url
          )
        }

        "the sub task for Cider must be found and be completed" in {
          val maybeTask = result.taskList.items.find(
            _.title.content == Text(messages(s"taskList.section.returns.${Cider.entryName}"))
          )

          maybeTask            mustBe defined
          maybeTask.get.status mustBe AlcoholDutyTaskListItemStatus.completed
        }

        "the sub task for Cider must link to the check your answers page for that regime" in {
          val maybeTask = result.taskList.items.find(
            _.title.content == Text(messages(s"taskList.section.returns.${Cider.entryName}"))
          )

          maybeTask.get.href mustBe Some(
            controllers.declareDuty.routes.CheckYourAnswersController.onPageLoad(Cider).url
          )
        }

        "no hint must be displayed" in {
          result.taskList.items.head.hint.map(_.content) mustBe None
        }

        "the idPrefix must be set" in {
          result.taskList.idPrefix mustBe "returns"
        }
      }

      "and has selected a two regimes to declare duty on and finished both tasks" - {
        val rateBandsBeer       = genListOfRateBandForRegime(Beer).sample.value
        val volumesAndRatesBeer = arbitraryVolumeAndRateByTaxType(
          rateBandsBeer
        ).arbitrary.sample.value

        val dutiesByTaxTypeBeer = volumesAndRatesBeer.map { volumeAndRate =>
          val totalDuty = volumeAndRate.dutyRate * volumeAndRate.pureAlcohol
          DutyByTaxType(
            taxType = volumeAndRate.taxType,
            totalLitres = volumeAndRate.totalLitres,
            pureAlcohol = volumeAndRate.pureAlcohol,
            dutyRate = volumeAndRate.dutyRate,
            dutyDue = totalDuty
          )
        }

        val alcoholDutyBeer = AlcoholDuty(
          dutiesByTaxType = dutiesByTaxTypeBeer,
          totalDuty = dutiesByTaxTypeBeer.map(_.dutyDue).sum
        )

        val rateBandsCider       = genListOfRateBandForRegime(Cider).sample.value
        val volumesAndRatesCider = arbitraryVolumeAndRateByTaxType(
          rateBandsCider
        ).arbitrary.sample.value

        val dutiesByTaxTypeCider = volumesAndRatesCider.map { volumeAndRate =>
          val totalDuty = volumeAndRate.dutyRate * volumeAndRate.pureAlcohol
          DutyByTaxType(
            taxType = volumeAndRate.taxType,
            totalLitres = volumeAndRate.totalLitres,
            pureAlcohol = volumeAndRate.pureAlcohol,
            dutyRate = volumeAndRate.dutyRate,
            dutyDue = totalDuty
          )
        }

        val alcoholDutyCider = AlcoholDuty(
          dutiesByTaxType = dutiesByTaxTypeCider,
          totalDuty = dutiesByTaxTypeCider.map(_.dutyDue).sum
        )

        val regimes: Set[AlcoholRegime] = Set(Beer, Cider)

        val declaredAlcoholDutyUserAnswerAndDeclarationAndDutyForRegime = emptyUserAnswers
          .set(DeclareAlcoholDutyQuestionPage, true)
          .success
          .value
          .set(AlcoholTypePage, regimes)
          .success
          .value
          .setByKey(WhatDoYouNeedToDeclarePage, Beer, rateBandsBeer.toSet)
          .success
          .value
          .setByKey(AlcoholDutyPage, Beer, alcoholDutyBeer)
          .success
          .value
          .setByKey(WhatDoYouNeedToDeclarePage, Cider, rateBandsCider.toSet)
          .success
          .value
          .setByKey(AlcoholDutyPage, Cider, alcoholDutyCider)
          .success
          .value

        val result =
          returnTaskListCreator.returnSection(declaredAlcoholDutyUserAnswerAndDeclarationAndDutyForRegime)

        "the task's title must be correct" in {
          result.title mustBe messages("taskList.section.returns.heading")
        }

        "the task must be completed" in {
          result.completedTask mustBe true
        }

        "a subtask for the regime must must be available in addition to the declaration subtasks" in {
          result.taskList.items.size mustBe 3
        }

        "the declaration subtask's title must be correct" in {
          result.taskList.items.head.title.content mustBe Text(
            messages("taskList.section.returns.needToDeclare")
          )
        }

        "the declaration subtask must be completed" in {
          result.taskList.items.head.status mustBe AlcoholDutyTaskListItemStatus.completed
        }

        "the declaration subtask must link to the declare alcohol duty question in CheckMode" in {
          result.taskList.items.head.href mustBe Some(
            controllers.declareDuty.routes.DeclareAlcoholDutyQuestionController.onPageLoad(CheckMode).url
          )
        }

        "the sub task for Beer must be found and be completed" in {
          val maybeTask = result.taskList.items.find(
            _.title.content == Text(messages(s"taskList.section.returns.${Beer.entryName}"))
          )

          maybeTask            mustBe defined
          maybeTask.get.status mustBe AlcoholDutyTaskListItemStatus.completed
        }

        "the sub task for Beer must link to the check your answers page for that regime" in {
          val maybeTask = result.taskList.items.find(
            _.title.content == Text(messages(s"taskList.section.returns.${Beer.entryName}"))
          )

          maybeTask.get.href mustBe Some(
            controllers.declareDuty.routes.CheckYourAnswersController.onPageLoad(Beer).url
          )
        }

        "the sub task for Cider must be found and be completed" in {
          val maybeTask = result.taskList.items.find(
            _.title.content == Text(messages(s"taskList.section.returns.${Cider.entryName}"))
          )

          maybeTask            mustBe defined
          maybeTask.get.status mustBe AlcoholDutyTaskListItemStatus.completed
        }

        "the sub task for Cider must link to the check your answers page for that regime" in {
          val maybeTask = result.taskList.items.find(
            _.title.content == Text(messages(s"taskList.section.returns.${Cider.entryName}"))
          )

          maybeTask.get.href mustBe Some(
            controllers.declareDuty.routes.CheckYourAnswersController.onPageLoad(Cider).url
          )
        }

        "no hint must be displayed" in {
          result.taskList.items.head.hint.map(_.content) mustBe None
        }

        "the idPrefix must be set" in {
          result.taskList.idPrefix mustBe "returns"
        }
      }
    }
  }

  "on calling returnAdjustmentSection" - {
    "when the user hasn't answered the DeclareAdjustment question" - {
      val result = returnTaskListCreator.returnAdjustmentSection(emptyUserAnswers)

      "the task's title must be correct" in {
        result.title mustBe messages("taskList.section.adjustment.heading")
      }

      "the task must not be completed" in {
        result.completedTask mustBe false
      }

      "only one subtask must be available" in {
        result.taskList.items.size mustBe 1
      }

      "the subtask's title must be correct" in {
        result.taskList.items.head.title.content mustBe Text(
          messages("taskList.section.adjustment.needToDeclare")
        )
      }

      "the subtask must not be started" in {
        result.taskList.items.head.status mustBe AlcoholDutyTaskListItemStatus.notStarted
      }

      "the subtask must link to the declare adjustment question" in {
        result.taskList.items.head.href mustBe Some(
          controllers.adjustment.routes.DeclareAdjustmentQuestionController.onPageLoad(NormalMode).url
        )
      }

      "no hint must be displayed" in {
        result.taskList.items.head.hint.map(_.content) mustBe None
      }
    }

    "when the user answers no to the DeclareAdjustment question" - {
      val userAnswers = emptyUserAnswers
        .set(DeclareAdjustmentQuestionPage, false)
        .success
        .value
      val result      = returnTaskListCreator.returnAdjustmentSection(userAnswers)

      "the task's title must be correct" in {
        result.title mustBe messages("taskList.section.adjustment.heading")
      }

      "the task must be completed" in {
        result.completedTask mustBe true
      }

      "only one subtask must be available" in {
        result.taskList.items.size mustBe 1
      }

      "the subtask's title must be correct" in {
        result.taskList.items.head.title.content mustBe Text(
          messages("taskList.section.adjustment.needToDeclare")
        )
      }

      "the subtask must be completed" in {
        result.taskList.items.head.status mustBe AlcoholDutyTaskListItemStatus.completed
      }

      "the subtask must link to the declare adjustment question in CheckMode" in {
        result.taskList.items.head.href mustBe Some(
          controllers.adjustment.routes.DeclareAdjustmentQuestionController.onPageLoad(CheckMode).url
        )
      }

      "no hint must be displayed" in {
        result.taskList.items.head.hint.map(_.content) mustBe None
      }
    }

    "when the user answers yes to the DeclareAdjustment question" - {
      "but not declared or declaring any adjustments" - {
        val declaredAdjustmentUserAnswer = emptyUserAnswers
          .set(DeclareAdjustmentQuestionPage, true)
          .success
          .value

        val result = returnTaskListCreator.returnAdjustmentSection(declaredAdjustmentUserAnswer)

        "the task's title must be correct" in {
          result.title mustBe messages("taskList.section.adjustment.heading")
        }

        "the task must not be completed" in {
          result.completedTask mustBe false
        }

        "a subtask for the adjustments must must be available in addition to the declaration subtask" in {
          result.taskList.items.size mustBe 2
        }

        "the declaration subtask's title must be correct" in {
          result.taskList.items.head.title.content mustBe Text(
            messages("taskList.section.adjustment.needToDeclare")
          )
        }

        "the declaration subtask must be completed" in {
          result.taskList.items.head.status mustBe AlcoholDutyTaskListItemStatus.completed
        }

        "the declaration subtask must link to the declare adjustment question in CheckMode" in {
          result.taskList.items.head.href mustBe Some(
            controllers.adjustment.routes.DeclareAdjustmentQuestionController.onPageLoad(CheckMode).url
          )
        }

        "the sub task for the adjustments must be found and not be started" in {
          val maybeTask = result.taskList.items.find(
            _.title.content == Text(messages("taskList.section.adjustment"))
          )

          maybeTask            mustBe defined
          maybeTask.get.status mustBe AlcoholDutyTaskListItemStatus.notStarted
        }

        "the sub task for the adjustments must link to the what do you need to declare page" in {
          val maybeTask = result.taskList.items.find(
            _.title.content == Text(messages("taskList.section.adjustment"))
          )

          maybeTask.get.href mustBe Some(
            controllers.adjustment.routes.AdjustmentTypeController.onPageLoad(NormalMode).url
          )
        }

        "no hint must be displayed" in {
          result.taskList.items.head.hint.map(_.content) mustBe None
        }

        "the idPrefix must be set" in {
          result.taskList.idPrefix mustBe "adjustment"
        }
      }

      "and are in the process of declaring a first adjustment" - {
        val adjustmentEntry              = AdjustmentEntry(adjustmentType = Some(Underdeclaration))
        val declaredAdjustmentUserAnswer = emptyUserAnswers
          .set(DeclareAdjustmentQuestionPage, true)
          .success
          .value
          .set(CurrentAdjustmentEntryPage, adjustmentEntry)
          .success
          .value

        val result = returnTaskListCreator.returnAdjustmentSection(declaredAdjustmentUserAnswer)

        "the task's title must be correct" in {
          result.title mustBe messages("taskList.section.adjustment.heading")
        }

        "the task must not be completed" in {
          result.completedTask mustBe false
        }

        "a subtask for the adjustments must must be available in addition to the declaration subtask" in {
          result.taskList.items.size mustBe 2
        }

        "the declaration subtask's title must be correct" in {
          result.taskList.items.head.title.content mustBe Text(
            messages("taskList.section.adjustment.needToDeclare")
          )
        }

        "the declaration subtask must be completed" in {
          result.taskList.items.head.status mustBe AlcoholDutyTaskListItemStatus.completed
        }

        "the declaration subtask must link to the declare adjustment question in CheckMode" in {
          result.taskList.items.head.href mustBe Some(
            controllers.adjustment.routes.DeclareAdjustmentQuestionController.onPageLoad(CheckMode).url
          )
        }

        "the sub task for the adjustments must be found and be in progress" in {
          val maybeTask = result.taskList.items.find(
            _.title.content == Text(messages("taskList.section.adjustment"))
          )

          maybeTask            mustBe defined
          maybeTask.get.status mustBe AlcoholDutyTaskListItemStatus.inProgress
        }

        "the sub task for the adjustments must link to the what do you need to declare page" in {
          val maybeTask = result.taskList.items.find(
            _.title.content == Text(messages("taskList.section.adjustment"))
          )

          maybeTask.get.href mustBe Some(
            controllers.adjustment.routes.AdjustmentTypeController.onPageLoad(NormalMode).url
          )
        }

        "no hint must be displayed" in {
          result.taskList.items.head.hint.map(_.content) mustBe None
        }

        "the idPrefix must be set" in {
          result.taskList.idPrefix mustBe "adjustment"
        }
      }

      "and have declared an adjustment are in the process of declaring a second adjustment" - {
        val adjustmentEntry              = AdjustmentEntry(adjustmentType = Some(Underdeclaration))
        val declaredAdjustmentUserAnswer = emptyUserAnswers
          .set(DeclareAdjustmentQuestionPage, true)
          .success
          .value
          .set(AdjustmentEntryListPage, List(AdjustmentEntry()))
          .success
          .value
          .set(CurrentAdjustmentEntryPage, adjustmentEntry)
          .success
          .value

        val result = returnTaskListCreator.returnAdjustmentSection(declaredAdjustmentUserAnswer)

        "the task's title must be correct" in {
          result.title mustBe messages("taskList.section.adjustment.heading")
        }

        "the task must not be completed" in {
          result.completedTask mustBe false
        }

        "a subtask for the adjustments must must be available in addition to the declaration subtask" in {
          result.taskList.items.size mustBe 2
        }

        "the declaration subtask's title must be correct" in {
          result.taskList.items.head.title.content mustBe Text(
            messages("taskList.section.adjustment.needToDeclare")
          )
        }

        "the declaration subtask must be completed" in {
          result.taskList.items.head.status mustBe AlcoholDutyTaskListItemStatus.completed
        }

        "the declaration subtask must link to the declare adjustment question in CheckMode" in {
          result.taskList.items.head.href mustBe Some(
            controllers.adjustment.routes.DeclareAdjustmentQuestionController.onPageLoad(CheckMode).url
          )
        }

        "the sub task for the adjustments must be found and be in progress" in {
          val maybeTask = result.taskList.items.find(
            _.title.content == Text(messages("taskList.section.adjustment"))
          )

          maybeTask            mustBe defined
          maybeTask.get.status mustBe AlcoholDutyTaskListItemStatus.inProgress
        }

        "the sub task for the adjustments must link to the what do you need to declare page" in {
          val maybeTask = result.taskList.items.find(
            _.title.content == Text(messages("taskList.section.adjustment"))
          )

          maybeTask.get.href mustBe Some(
            controllers.adjustment.routes.AdjustmentTypeController.onPageLoad(NormalMode).url
          )
        }

        "no hint must be displayed" in {
          result.taskList.items.head.hint.map(_.content) mustBe None
        }

        "the idPrefix must be set" in {
          result.taskList.idPrefix mustBe "adjustment"
        }
      }

      "and have declared an adjustment, but have not answered that there are no more to declared" - {
        val declaredAdjustmentUserAnswer = emptyUserAnswers
          .set(DeclareAdjustmentQuestionPage, true)
          .success
          .value
          .set(AdjustmentEntryListPage, List(AdjustmentEntry()))
          .success
          .value

        val result = returnTaskListCreator.returnAdjustmentSection(declaredAdjustmentUserAnswer)

        "the task's title must be correct" in {
          result.title mustBe messages("taskList.section.adjustment.heading")
        }

        "the task must not be completed" in {
          result.completedTask mustBe false
        }

        "a subtask for the adjustments must must be available in addition to the declaration subtask" in {
          result.taskList.items.size mustBe 2
        }

        "the declaration subtask's title must be correct" in {
          result.taskList.items.head.title.content mustBe Text(
            messages("taskList.section.adjustment.needToDeclare")
          )
        }

        "the declaration subtask must be completed" in {
          result.taskList.items.head.status mustBe AlcoholDutyTaskListItemStatus.completed
        }

        "the declaration subtask must link to the declare adjustment question in CheckMode" in {
          result.taskList.items.head.href mustBe Some(
            controllers.adjustment.routes.DeclareAdjustmentQuestionController.onPageLoad(CheckMode).url
          )
        }

        "the sub task for the adjustments must be found and be in progress" in {
          val maybeTask = result.taskList.items.find(
            _.title.content == Text(messages("taskList.section.adjustment"))
          )

          maybeTask            mustBe defined
          maybeTask.get.status mustBe AlcoholDutyTaskListItemStatus.inProgress
        }

        s"the sub task for the adjustments must link to the adjustment list controller on page $pageNumber" in {
          val maybeTask = result.taskList.items.find(
            _.title.content == Text(messages("taskList.section.adjustment"))
          )

          maybeTask.get.href mustBe Some(
            controllers.adjustment.routes.AdjustmentListController.onPageLoad(pageNumber).url
          )
        }

        "no hint must be displayed" in {
          result.taskList.items.head.hint.map(_.content) mustBe None
        }

        "the idPrefix must be set" in {
          result.taskList.idPrefix mustBe "adjustment"
        }
      }

      "and have declared an adjustment are have no more to declare, and need to add an under-declaration reason" - {
        val declaredAdjustmentUserAnswer = emptyUserAnswers
          .set(DeclareAdjustmentQuestionPage, true)
          .success
          .value
          .set(AdjustmentEntryListPage, List(AdjustmentEntry()))
          .success
          .value
          .set(AdjustmentListPage, false)
          .success
          .value
          .set(UnderDeclarationTotalPage, BigDecimal(1000))
          .success
          .value

        val result = returnTaskListCreator.returnAdjustmentSection(declaredAdjustmentUserAnswer)

        "the task's title must be correct" in {
          result.title mustBe messages("taskList.section.adjustment.heading")
        }

        "the task must not be completed" in {
          result.completedTask mustBe false
        }

        "a subtask for the adjustments and under-declaration reason must must be available in addition to the declaration subtask" in {
          result.taskList.items.size mustBe 3
        }

        "the declaration subtask's title must be correct" in {
          result.taskList.items.head.title.content mustBe Text(
            messages("taskList.section.adjustment.needToDeclare")
          )
        }

        "the declaration subtask must be completed" in {
          result.taskList.items.head.status mustBe AlcoholDutyTaskListItemStatus.completed
        }

        "the declaration subtask must link to the declare adjustment question in CheckMode" in {
          result.taskList.items.head.href mustBe Some(
            controllers.adjustment.routes.DeclareAdjustmentQuestionController.onPageLoad(CheckMode).url
          )
        }

        "the sub task for the adjustments must be found and be completed" in {
          val maybeTask = result.taskList.items.find(
            _.title.content == Text(messages("taskList.section.adjustment"))
          )

          maybeTask            mustBe defined
          maybeTask.get.status mustBe AlcoholDutyTaskListItemStatus.completed
        }

        s"the sub task for the adjustments must link to the adjustment list controller on page $pageNumber" in {
          val maybeTask = result.taskList.items.find(
            _.title.content == Text(messages("taskList.section.adjustment"))
          )

          maybeTask.get.href mustBe Some(
            controllers.adjustment.routes.AdjustmentListController.onPageLoad(pageNumber).url
          )
        }

        "the sub task for the under-declaration reason must be found and be not started" in {
          val maybeTask = result.taskList.items.find(
            _.title.content == Text(messages("taskList.section.adjustment.under-declaration"))
          )

          maybeTask            mustBe defined
          maybeTask.get.status mustBe AlcoholDutyTaskListItemStatus.notStarted
        }

        "the sub task for the under-declaration reason must link to the under-declaration page" in {
          val maybeTask = result.taskList.items.find(
            _.title.content == Text(messages("taskList.section.adjustment.under-declaration"))
          )

          maybeTask.get.href mustBe Some(
            controllers.adjustment.routes.UnderDeclarationReasonController.onPageLoad(NormalMode).url
          )
        }

        "no hint must be displayed" in {
          result.taskList.items.head.hint.map(_.content) mustBe None
        }

        "the idPrefix must be set" in {
          result.taskList.idPrefix mustBe "adjustment"
        }
      }

      "and have declared an adjustment are have no more to declare, and need to add an over-declaration reason" - {
        val declaredAdjustmentUserAnswer = emptyUserAnswers
          .set(DeclareAdjustmentQuestionPage, true)
          .success
          .value
          .set(AdjustmentEntryListPage, List(AdjustmentEntry()))
          .success
          .value
          .set(AdjustmentListPage, false)
          .success
          .value
          .set(OverDeclarationTotalPage, BigDecimal(1000))
          .success
          .value

        val result = returnTaskListCreator.returnAdjustmentSection(declaredAdjustmentUserAnswer)

        "the task's title must be correct" in {
          result.title mustBe messages("taskList.section.adjustment.heading")
        }

        "the task must not be completed" in {
          result.completedTask mustBe false
        }

        "a subtask for the adjustments and over-declaration reason must must be available in addition to the declaration subtask" in {
          result.taskList.items.size mustBe 3
        }

        "the declaration subtask's title must be correct" in {
          result.taskList.items.head.title.content mustBe Text(
            messages("taskList.section.adjustment.needToDeclare")
          )
        }

        "the declaration subtask must be completed" in {
          result.taskList.items.head.status mustBe AlcoholDutyTaskListItemStatus.completed
        }

        "the declaration subtask must link to the declare adjustment question in CheckMode" in {
          result.taskList.items.head.href mustBe Some(
            controllers.adjustment.routes.DeclareAdjustmentQuestionController.onPageLoad(CheckMode).url
          )
        }

        "the sub task for the adjustments must be found and be completed" in {
          val maybeTask = result.taskList.items.find(
            _.title.content == Text(messages("taskList.section.adjustment"))
          )

          maybeTask            mustBe defined
          maybeTask.get.status mustBe AlcoholDutyTaskListItemStatus.completed
        }

        s"the sub task for the adjustments must link to the adjustment list controller on page $pageNumber" in {
          val maybeTask = result.taskList.items.find(
            _.title.content == Text(messages("taskList.section.adjustment"))
          )

          maybeTask.get.href mustBe Some(
            controllers.adjustment.routes.AdjustmentListController.onPageLoad(pageNumber).url
          )
        }

        "the sub task for the over-declaration reason must be found and be not started" in {
          val maybeTask = result.taskList.items.find(
            _.title.content == Text(messages("taskList.section.adjustment.over-declaration"))
          )

          maybeTask            mustBe defined
          maybeTask.get.status mustBe AlcoholDutyTaskListItemStatus.notStarted
        }

        "the sub task for the over-declaration reason must link to the over-declaration page" in {
          val maybeTask = result.taskList.items.find(
            _.title.content == Text(messages("taskList.section.adjustment.over-declaration"))
          )

          maybeTask.get.href mustBe Some(
            controllers.adjustment.routes.OverDeclarationReasonController.onPageLoad(NormalMode).url
          )
        }

        "no hint must be displayed" in {
          result.taskList.items.head.hint.map(_.content) mustBe None
        }

        "the idPrefix must be set" in {
          result.taskList.idPrefix mustBe "adjustment"
        }
      }

      "and have declared an adjustment are have no more to declare, and need to add both an under and over-declaration reason" - {
        val declaredAdjustmentUserAnswer = emptyUserAnswers
          .set(DeclareAdjustmentQuestionPage, true)
          .success
          .value
          .set(AdjustmentEntryListPage, List(AdjustmentEntry()))
          .success
          .value
          .set(AdjustmentListPage, false)
          .success
          .value
          .set(UnderDeclarationTotalPage, BigDecimal(1000))
          .success
          .value
          .set(OverDeclarationTotalPage, BigDecimal(1000))
          .success
          .value

        val result = returnTaskListCreator.returnAdjustmentSection(declaredAdjustmentUserAnswer)

        "the task's title must be correct" in {
          result.title mustBe messages("taskList.section.adjustment.heading")
        }

        "the task must not be completed" in {
          result.completedTask mustBe false
        }

        "a subtask for the adjustments and under and over-declaration reason must must be available in addition to the declaration subtask" in {
          result.taskList.items.size mustBe 4
        }

        "the declaration subtask's title must be correct" in {
          result.taskList.items.head.title.content mustBe Text(
            messages("taskList.section.adjustment.needToDeclare")
          )
        }

        "the declaration subtask must be completed" in {
          result.taskList.items.head.status mustBe AlcoholDutyTaskListItemStatus.completed
        }

        "the declaration subtask must link to the declare adjustment question in CheckMode" in {
          result.taskList.items.head.href mustBe Some(
            controllers.adjustment.routes.DeclareAdjustmentQuestionController.onPageLoad(CheckMode).url
          )
        }

        "the sub task for the adjustments must be found and be completed" in {
          val maybeTask = result.taskList.items.find(
            _.title.content == Text(messages("taskList.section.adjustment"))
          )

          maybeTask            mustBe defined
          maybeTask.get.status mustBe AlcoholDutyTaskListItemStatus.completed
        }

        s"the sub task for the adjustments must link to the adjustment list controller on page $pageNumber" in {
          val maybeTask = result.taskList.items.find(
            _.title.content == Text(messages("taskList.section.adjustment"))
          )

          maybeTask.get.href mustBe Some(
            controllers.adjustment.routes.AdjustmentListController.onPageLoad(pageNumber).url
          )
        }

        "the sub task for the under-declaration reason must be found and be not started" in {
          val maybeTask = result.taskList.items.find(
            _.title.content == Text(messages("taskList.section.adjustment.under-declaration"))
          )

          maybeTask            mustBe defined
          maybeTask.get.status mustBe AlcoholDutyTaskListItemStatus.notStarted
        }

        "the sub task for the under-declaration reason must link to the under-declaration page" in {
          val maybeTask = result.taskList.items.find(
            _.title.content == Text(messages("taskList.section.adjustment.under-declaration"))
          )

          maybeTask.get.href mustBe Some(
            controllers.adjustment.routes.UnderDeclarationReasonController.onPageLoad(NormalMode).url
          )
        }

        "the sub task for the over-declaration reason must be found and be not started" in {
          val maybeTask = result.taskList.items.find(
            _.title.content == Text(messages("taskList.section.adjustment.over-declaration"))
          )

          maybeTask            mustBe defined
          maybeTask.get.status mustBe AlcoholDutyTaskListItemStatus.notStarted
        }

        "the sub task for the over-declaration reason must link to the over-declaration page" in {
          val maybeTask = result.taskList.items.find(
            _.title.content == Text(messages("taskList.section.adjustment.over-declaration"))
          )

          maybeTask.get.href mustBe Some(
            controllers.adjustment.routes.OverDeclarationReasonController.onPageLoad(NormalMode).url
          )
        }

        "no hint must be displayed" in {
          result.taskList.items.head.hint.map(_.content) mustBe None
        }

        "the idPrefix must be set" in {
          result.taskList.idPrefix mustBe "adjustment"
        }
      }

      "and have declared an adjustment are have no more to declare, and need to add both an under and over-declaration reason, but just the under-declaration reason is added" - {
        val declaredAdjustmentUserAnswer = emptyUserAnswers
          .set(DeclareAdjustmentQuestionPage, true)
          .success
          .value
          .set(AdjustmentEntryListPage, List(AdjustmentEntry()))
          .success
          .value
          .set(AdjustmentListPage, false)
          .success
          .value
          .set(UnderDeclarationTotalPage, BigDecimal(1000))
          .success
          .value
          .set(OverDeclarationTotalPage, BigDecimal(1000))
          .success
          .value
          .set(UnderDeclarationReasonPage, "reason")
          .success
          .value

        val result = returnTaskListCreator.returnAdjustmentSection(declaredAdjustmentUserAnswer)

        "the task's title must be correct" in {
          result.title mustBe messages("taskList.section.adjustment.heading")
        }

        "the task must not be completed" in {
          result.completedTask mustBe false
        }

        "a subtask for the adjustments and under and over-declaration reason must must be available in addition to the declaration subtask" in {
          result.taskList.items.size mustBe 4
        }

        "the declaration subtask's title must be correct" in {
          result.taskList.items.head.title.content mustBe Text(
            messages("taskList.section.adjustment.needToDeclare")
          )
        }

        "the declaration subtask must be completed" in {
          result.taskList.items.head.status mustBe AlcoholDutyTaskListItemStatus.completed
        }

        "the declaration subtask must link to the declare adjustment question in CheckMode" in {
          result.taskList.items.head.href mustBe Some(
            controllers.adjustment.routes.DeclareAdjustmentQuestionController.onPageLoad(CheckMode).url
          )
        }

        "the sub task for the adjustments must be found and be completed" in {
          val maybeTask = result.taskList.items.find(
            _.title.content == Text(messages("taskList.section.adjustment"))
          )

          maybeTask            mustBe defined
          maybeTask.get.status mustBe AlcoholDutyTaskListItemStatus.completed
        }

        s"the sub task for the adjustments must link to the adjustment list controller on page $pageNumber" in {
          val maybeTask = result.taskList.items.find(
            _.title.content == Text(messages("taskList.section.adjustment"))
          )

          maybeTask.get.href mustBe Some(
            controllers.adjustment.routes.AdjustmentListController.onPageLoad(pageNumber).url
          )
        }

        "the sub task for the under-declaration reason must be found and be completed" in {
          val maybeTask = result.taskList.items.find(
            _.title.content == Text(messages("taskList.section.adjustment.under-declaration"))
          )

          maybeTask            mustBe defined
          maybeTask.get.status mustBe AlcoholDutyTaskListItemStatus.completed
        }

        "the sub task for the under-declaration reason must link to the under-declaration page" in {
          val maybeTask = result.taskList.items.find(
            _.title.content == Text(messages("taskList.section.adjustment.under-declaration"))
          )

          maybeTask.get.href mustBe Some(
            controllers.adjustment.routes.UnderDeclarationReasonController.onPageLoad(NormalMode).url
          )
        }

        "the sub task for the over-declaration reason must be found and be not started" in {
          val maybeTask = result.taskList.items.find(
            _.title.content == Text(messages("taskList.section.adjustment.over-declaration"))
          )

          maybeTask            mustBe defined
          maybeTask.get.status mustBe AlcoholDutyTaskListItemStatus.notStarted
        }

        "the sub task for the over-declaration reason must link to the over-declaration page" in {
          val maybeTask = result.taskList.items.find(
            _.title.content == Text(messages("taskList.section.adjustment.over-declaration"))
          )

          maybeTask.get.href mustBe Some(
            controllers.adjustment.routes.OverDeclarationReasonController.onPageLoad(NormalMode).url
          )
        }

        "no hint must be displayed" in {
          result.taskList.items.head.hint.map(_.content) mustBe None
        }

        "the idPrefix must be set" in {
          result.taskList.idPrefix mustBe "adjustment"
        }
      }

      "and have declared an adjustment are have no more to declare, and need to add both an under and over-declaration reason, but just the over-declaration reason is added" - {
        val declaredAdjustmentUserAnswer = emptyUserAnswers
          .set(DeclareAdjustmentQuestionPage, true)
          .success
          .value
          .set(AdjustmentEntryListPage, List(AdjustmentEntry()))
          .success
          .value
          .set(AdjustmentListPage, false)
          .success
          .value
          .set(UnderDeclarationTotalPage, BigDecimal(1000))
          .success
          .value
          .set(OverDeclarationTotalPage, BigDecimal(1000))
          .success
          .value
          .set(OverDeclarationReasonPage, "reason")
          .success
          .value

        val result = returnTaskListCreator.returnAdjustmentSection(declaredAdjustmentUserAnswer)

        "the task's title must be correct" in {
          result.title mustBe messages("taskList.section.adjustment.heading")
        }

        "the task must not be completed" in {
          result.completedTask mustBe false
        }

        "a subtask for the adjustments and under and over-declaration reason must must be available in addition to the declaration subtask" in {
          result.taskList.items.size mustBe 4
        }

        "the declaration subtask's title must be correct" in {
          result.taskList.items.head.title.content mustBe Text(
            messages("taskList.section.adjustment.needToDeclare")
          )
        }

        "the declaration subtask must be completed" in {
          result.taskList.items.head.status mustBe AlcoholDutyTaskListItemStatus.completed
        }

        "the declaration subtask must link to the declare adjustment question in CheckMode" in {
          result.taskList.items.head.href mustBe Some(
            controllers.adjustment.routes.DeclareAdjustmentQuestionController.onPageLoad(CheckMode).url
          )
        }

        "the sub task for the adjustments must be found and be completed" in {
          val maybeTask = result.taskList.items.find(
            _.title.content == Text(messages("taskList.section.adjustment"))
          )

          maybeTask            mustBe defined
          maybeTask.get.status mustBe AlcoholDutyTaskListItemStatus.completed
        }

        s"the sub task for the adjustments must link to the adjustment list controller on page $pageNumber" in {
          val maybeTask = result.taskList.items.find(
            _.title.content == Text(messages("taskList.section.adjustment"))
          )

          maybeTask.get.href mustBe Some(
            controllers.adjustment.routes.AdjustmentListController.onPageLoad(pageNumber).url
          )
        }

        "the sub task for the under-declaration reason must be found and be not started" in {
          val maybeTask = result.taskList.items.find(
            _.title.content == Text(messages("taskList.section.adjustment.under-declaration"))
          )

          maybeTask            mustBe defined
          maybeTask.get.status mustBe AlcoholDutyTaskListItemStatus.notStarted
        }

        "the sub task for the under-declaration reason must link to the under-declaration page" in {
          val maybeTask = result.taskList.items.find(
            _.title.content == Text(messages("taskList.section.adjustment.under-declaration"))
          )

          maybeTask.get.href mustBe Some(
            controllers.adjustment.routes.UnderDeclarationReasonController.onPageLoad(NormalMode).url
          )
        }

        "the sub task for the over-declaration reason must be found and be completed" in {
          val maybeTask = result.taskList.items.find(
            _.title.content == Text(messages("taskList.section.adjustment.over-declaration"))
          )

          maybeTask            mustBe defined
          maybeTask.get.status mustBe AlcoholDutyTaskListItemStatus.completed
        }

        "the sub task for the over-declaration reason must link to the over-declaration page" in {
          val maybeTask = result.taskList.items.find(
            _.title.content == Text(messages("taskList.section.adjustment.over-declaration"))
          )

          maybeTask.get.href mustBe Some(
            controllers.adjustment.routes.OverDeclarationReasonController.onPageLoad(NormalMode).url
          )
        }

        "no hint must be displayed" in {
          result.taskList.items.head.hint.map(_.content) mustBe None
        }

        "the idPrefix must be set" in {
          result.taskList.idPrefix mustBe "adjustment"
        }
      }

      "and have declared an adjustment are have no more to declare, but no extra reason tasks expected" - {
        val declaredAdjustmentUserAnswer = emptyUserAnswers
          .set(DeclareAdjustmentQuestionPage, true)
          .success
          .value
          .set(AdjustmentEntryListPage, List(AdjustmentEntry()))
          .success
          .value
          .set(AdjustmentListPage, false)
          .success
          .value

        val result = returnTaskListCreator.returnAdjustmentSection(declaredAdjustmentUserAnswer)

        "the task's title must be correct" in {
          result.title mustBe messages("taskList.section.adjustment.heading")
        }

        "the task mustt be completed" in {
          result.completedTask mustBe true
        }

        "a subtask for the adjustments must must be available in addition to the declaration subtask" in {
          result.taskList.items.size mustBe 2
        }

        "the declaration subtask's title must be correct" in {
          result.taskList.items.head.title.content mustBe Text(
            messages("taskList.section.adjustment.needToDeclare")
          )
        }

        "the declaration subtask must be completed" in {
          result.taskList.items.head.status mustBe AlcoholDutyTaskListItemStatus.completed
        }

        "the declaration subtask must link to the declare adjustment question in CheckMode" in {
          result.taskList.items.head.href mustBe Some(
            controllers.adjustment.routes.DeclareAdjustmentQuestionController.onPageLoad(CheckMode).url
          )
        }

        "the sub task for the adjustments must be found and be completed" in {
          val maybeTask = result.taskList.items.find(
            _.title.content == Text(messages("taskList.section.adjustment"))
          )

          maybeTask            mustBe defined
          maybeTask.get.status mustBe AlcoholDutyTaskListItemStatus.completed
        }

        s"the sub task for the adjustments must link to the adjustment list controller on page $pageNumber" in {
          val maybeTask = result.taskList.items.find(
            _.title.content == Text(messages("taskList.section.adjustment"))
          )

          maybeTask.get.href mustBe Some(
            controllers.adjustment.routes.AdjustmentListController.onPageLoad(pageNumber).url
          )
        }

        "no hint must be displayed" in {
          result.taskList.items.head.hint.map(_.content) mustBe None
        }
      }

      "and have declared an adjustment are have no more to declare, and have added a required under-declaration reason" - {
        val declaredAdjustmentUserAnswer = emptyUserAnswers
          .set(DeclareAdjustmentQuestionPage, true)
          .success
          .value
          .set(AdjustmentEntryListPage, List(AdjustmentEntry()))
          .success
          .value
          .set(AdjustmentListPage, false)
          .success
          .value
          .set(UnderDeclarationTotalPage, BigDecimal(1000))
          .success
          .value
          .set(UnderDeclarationReasonPage, "reason")
          .success
          .value

        val result = returnTaskListCreator.returnAdjustmentSection(declaredAdjustmentUserAnswer)

        "the task's title must be correct" in {
          result.title mustBe messages("taskList.section.adjustment.heading")
        }

        "the task must be completed" in {
          result.completedTask mustBe true
        }

        "a subtask for the adjustments and under-declaration reason must must be available in addition to the declaration subtask" in {
          result.taskList.items.size mustBe 3
        }

        "the declaration subtask's title must be correct" in {
          result.taskList.items.head.title.content mustBe Text(
            messages("taskList.section.adjustment.needToDeclare")
          )
        }

        "the declaration subtask must be completed" in {
          result.taskList.items.head.status mustBe AlcoholDutyTaskListItemStatus.completed
        }

        "the declaration subtask must link to the declare adjustment question in CheckMode" in {
          result.taskList.items.head.href mustBe Some(
            controllers.adjustment.routes.DeclareAdjustmentQuestionController.onPageLoad(CheckMode).url
          )
        }

        "the sub task for the adjustments must be found and be completed" in {
          val maybeTask = result.taskList.items.find(
            _.title.content == Text(messages("taskList.section.adjustment"))
          )

          maybeTask            mustBe defined
          maybeTask.get.status mustBe AlcoholDutyTaskListItemStatus.completed
        }

        s"the sub task for the adjustments must link to the adjustment list controller on page $pageNumber" in {
          val maybeTask = result.taskList.items.find(
            _.title.content == Text(messages("taskList.section.adjustment"))
          )

          maybeTask.get.href mustBe Some(
            controllers.adjustment.routes.AdjustmentListController.onPageLoad(pageNumber).url
          )
        }

        "the sub task for the under-declaration reason must be found and be completed" in {
          val maybeTask = result.taskList.items.find(
            _.title.content == Text(messages("taskList.section.adjustment.under-declaration"))
          )

          maybeTask            mustBe defined
          maybeTask.get.status mustBe AlcoholDutyTaskListItemStatus.completed
        }

        "the sub task for the under-declaration reason must link to the under-declaration page" in {
          val maybeTask = result.taskList.items.find(
            _.title.content == Text(messages("taskList.section.adjustment.under-declaration"))
          )

          maybeTask.get.href mustBe Some(
            controllers.adjustment.routes.UnderDeclarationReasonController.onPageLoad(NormalMode).url
          )
        }

        "no hint must be displayed" in {
          result.taskList.items.head.hint.map(_.content) mustBe None
        }

        "the idPrefix must be set" in {
          result.taskList.idPrefix mustBe "adjustment"
        }
      }

      "and have declared an adjustment are have no more to declare, and have added a required over-declaration reason" - {
        val declaredAdjustmentUserAnswer = emptyUserAnswers
          .set(DeclareAdjustmentQuestionPage, true)
          .success
          .value
          .set(AdjustmentEntryListPage, List(AdjustmentEntry()))
          .success
          .value
          .set(AdjustmentListPage, false)
          .success
          .value
          .set(OverDeclarationTotalPage, BigDecimal(1000))
          .success
          .value
          .set(OverDeclarationReasonPage, "reason")
          .success
          .value

        val result = returnTaskListCreator.returnAdjustmentSection(declaredAdjustmentUserAnswer)

        "the task's title must be correct" in {
          result.title mustBe messages("taskList.section.adjustment.heading")
        }

        "the task must be completed" in {
          result.completedTask mustBe true
        }

        "a subtask for the adjustments and over-declaration reason must must be available in addition to the declaration subtask" in {
          result.taskList.items.size mustBe 3
        }

        "the declaration subtask's title must be correct" in {
          result.taskList.items.head.title.content mustBe Text(
            messages("taskList.section.adjustment.needToDeclare")
          )
        }

        "the declaration subtask must be completed" in {
          result.taskList.items.head.status mustBe AlcoholDutyTaskListItemStatus.completed
        }

        "the declaration subtask must link to the declare adjustment question in CheckMode" in {
          result.taskList.items.head.href mustBe Some(
            controllers.adjustment.routes.DeclareAdjustmentQuestionController.onPageLoad(CheckMode).url
          )
        }

        "the sub task for the adjustments must be found and be completed" in {
          val maybeTask = result.taskList.items.find(
            _.title.content == Text(messages("taskList.section.adjustment"))
          )

          maybeTask            mustBe defined
          maybeTask.get.status mustBe AlcoholDutyTaskListItemStatus.completed
        }

        s"the sub task for the adjustments must link to the adjustment list controller on page $pageNumber" in {
          val maybeTask = result.taskList.items.find(
            _.title.content == Text(messages("taskList.section.adjustment"))
          )

          maybeTask.get.href mustBe Some(
            controllers.adjustment.routes.AdjustmentListController.onPageLoad(pageNumber).url
          )
        }

        "the sub task for the over-declaration reason must be found and be completed" in {
          val maybeTask = result.taskList.items.find(
            _.title.content == Text(messages("taskList.section.adjustment.over-declaration"))
          )

          maybeTask            mustBe defined
          maybeTask.get.status mustBe AlcoholDutyTaskListItemStatus.completed
        }

        "the sub task for the over-declaration reason must link to the over-declaration page" in {
          val maybeTask = result.taskList.items.find(
            _.title.content == Text(messages("taskList.section.adjustment.over-declaration"))
          )

          maybeTask.get.href mustBe Some(
            controllers.adjustment.routes.OverDeclarationReasonController.onPageLoad(NormalMode).url
          )
        }

        "no hint must be displayed" in {
          result.taskList.items.head.hint.map(_.content) mustBe None
        }

        "the idPrefix must be set" in {
          result.taskList.idPrefix mustBe "adjustment"
        }
      }

      "and have declared an adjustment are have no more to declare, and have added both a required under and over-declaration reason" - {
        val declaredAdjustmentUserAnswer = emptyUserAnswers
          .set(DeclareAdjustmentQuestionPage, true)
          .success
          .value
          .set(AdjustmentEntryListPage, List(AdjustmentEntry()))
          .success
          .value
          .set(AdjustmentListPage, false)
          .success
          .value
          .set(UnderDeclarationTotalPage, BigDecimal(1000))
          .success
          .value
          .set(OverDeclarationTotalPage, BigDecimal(1000))
          .success
          .value
          .set(UnderDeclarationReasonPage, "reason")
          .success
          .value
          .set(OverDeclarationReasonPage, "reason")
          .success
          .value

        val result = returnTaskListCreator.returnAdjustmentSection(declaredAdjustmentUserAnswer)

        "the task's title must be correct" in {
          result.title mustBe messages("taskList.section.adjustment.heading")
        }

        "the task must not completed" in {
          result.completedTask mustBe true
        }

        "a subtask for the adjustments and under and over-declaration reason must must be available in addition to the declaration subtask" in {
          result.taskList.items.size mustBe 4
        }

        "the declaration subtask's title must be correct" in {
          result.taskList.items.head.title.content mustBe Text(
            messages("taskList.section.adjustment.needToDeclare")
          )
        }

        "the declaration subtask must be completed" in {
          result.taskList.items.head.status mustBe AlcoholDutyTaskListItemStatus.completed
        }

        "the declaration subtask must link to the declare adjustment question in CheckMode" in {
          result.taskList.items.head.href mustBe Some(
            controllers.adjustment.routes.DeclareAdjustmentQuestionController.onPageLoad(CheckMode).url
          )
        }

        "the sub task for the adjustments must be found and be completed" in {
          val maybeTask = result.taskList.items.find(
            _.title.content == Text(messages("taskList.section.adjustment"))
          )

          maybeTask            mustBe defined
          maybeTask.get.status mustBe AlcoholDutyTaskListItemStatus.completed
        }

        s"the sub task for the adjustments must link to the adjustment list controller on page $pageNumber" in {
          val maybeTask = result.taskList.items.find(
            _.title.content == Text(messages("taskList.section.adjustment"))
          )

          maybeTask.get.href mustBe Some(
            controllers.adjustment.routes.AdjustmentListController.onPageLoad(pageNumber).url
          )
        }

        "the sub task for the under-declaration reason must be found and be completed" in {
          val maybeTask = result.taskList.items.find(
            _.title.content == Text(messages("taskList.section.adjustment.under-declaration"))
          )

          maybeTask            mustBe defined
          maybeTask.get.status mustBe AlcoholDutyTaskListItemStatus.completed
        }

        "the sub task for the under-declaration reason must link to the under-declaration page" in {
          val maybeTask = result.taskList.items.find(
            _.title.content == Text(messages("taskList.section.adjustment.under-declaration"))
          )

          maybeTask.get.href mustBe Some(
            controllers.adjustment.routes.UnderDeclarationReasonController.onPageLoad(NormalMode).url
          )
        }

        "the sub task for the over-declaration reason must be found and be completed" in {
          val maybeTask = result.taskList.items.find(
            _.title.content == Text(messages("taskList.section.adjustment.over-declaration"))
          )

          maybeTask            mustBe defined
          maybeTask.get.status mustBe AlcoholDutyTaskListItemStatus.completed
        }

        "the sub task for the over-declaration reason must link to the over-declaration page" in {
          val maybeTask = result.taskList.items.find(
            _.title.content == Text(messages("taskList.section.adjustment.over-declaration"))
          )

          maybeTask.get.href mustBe Some(
            controllers.adjustment.routes.OverDeclarationReasonController.onPageLoad(NormalMode).url
          )
        }

        "no hint must be displayed" in {
          result.taskList.items.head.hint.map(_.content) mustBe None
        }

        "the idPrefix must be set" in {
          result.taskList.idPrefix mustBe "adjustment"
        }
      }
    }
  }

  "on calling returnDSDSection" - {
    "when the user hasn't answered the DSD question" - {
      val result = returnTaskListCreator.returnDSDSection(emptyUserAnswers)

      "the task's title must be correct" in {
        result.title mustBe messages("taskList.section.dutySuspended.heading")
      }

      "the task must not be completed" in {
        result.completedTask mustBe false
      }

      "only one subtask must be available" in {
        result.taskList.items.size mustBe 1
      }

      "the subtask's title must be correct" in {
        result.taskList.items.head.title.content mustBe Text(
          messages("taskList.section.dutySuspended.needToDeclare")
        )
      }

      "the subtask must not be started" in {
        result.taskList.items.head.status mustBe AlcoholDutyTaskListItemStatus.notStarted
      }

      "the subtask must link to the declare alcohol duty question" in {
        result.taskList.items.head.href mustBe Some(
          controllers.dutySuspended.routes.DeclareDutySuspendedDeliveriesQuestionController.onPageLoad(NormalMode).url
        )
      }

      "no hint must be displayed" in {
        result.taskList.items.head.hint.map(_.content) mustBe None
      }
    }

    "when the user answers no to the DSD question" - {
      val userAnswers = emptyUserAnswers
        .set(DeclareDutySuspendedDeliveriesQuestionPage, false)
        .success
        .value
      val result      = returnTaskListCreator.returnDSDSection(userAnswers)

      "the task's title must be correct" in {
        result.title mustBe messages("taskList.section.dutySuspended.heading")
      }

      "the task must be completed" in {
        result.completedTask mustBe true
      }

      "only one subtask must be available" in {
        result.taskList.items.size mustBe 1
      }

      "the subtask's title must be correct" in {
        result.taskList.items.head.title.content mustBe Text(
          messages("taskList.section.dutySuspended.needToDeclare")
        )
      }

      "the subtask must be completed" in {
        result.taskList.items.head.status mustBe AlcoholDutyTaskListItemStatus.completed
      }

      "the subtask must link to the declare alcohol duty question in CheckMode" in {
        result.taskList.items.head.href mustBe Some(
          controllers.dutySuspended.routes.DeclareDutySuspendedDeliveriesQuestionController.onPageLoad(CheckMode).url
        )
      }

      "no hint must be displayed" in {
        result.taskList.items.head.hint.map(_.content) mustBe None
      }

      "the idPrefix must be set" in {
        result.taskList.idPrefix mustBe "dutySuspended"
      }
    }

    "when the user answers yes to the DeclareDutySuspendedDeliveriesQuestionPage question" - {
      "but has not started the declaration task" - {
        val userAnswers = emptyUserAnswers
          .set(DeclareDutySuspendedDeliveriesQuestionPage, true)
          .success
          .value
        val result      = returnTaskListCreator.returnDSDSection(userAnswers)

        "the task's title must be correct" in {
          result.title mustBe messages("taskList.section.dutySuspended.heading")
        }

        "the task must not be completed" in {
          result.completedTask mustBe false
        }

        "only both subtasks must be available" in {
          result.taskList.items.size mustBe 2
        }

        "the subtask's title must be correct" in {
          result.taskList.items.head.title.content mustBe Text(
            messages("taskList.section.dutySuspended.needToDeclare")
          )
        }

        "the subtask must be completed" in {
          result.taskList.items.head.status mustBe AlcoholDutyTaskListItemStatus.completed
        }

        "the subtask must link to the declare duty suspended deliveries question in CheckMode" in {
          result.taskList.items.head.href mustBe Some(
            controllers.dutySuspended.routes.DeclareDutySuspendedDeliveriesQuestionController.onPageLoad(CheckMode).url
          )
        }

        "no hint must be displayed" in {
          result.taskList.items.head.hint.map(_.content) mustBe None
        }
      }

      "and has started the declaration task and answered for the only regime" - {
        val validTotal                                 = 42.34
        val validPureAlcohol                           = 34.23
        val completeDutySuspendedDeliveriesUserAnswers = userAnswersWithOtherFermentedProduct
          .copy(data =
            Json.obj(
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

        "the task's title must be correct" in {
          result.title mustBe messages("taskList.section.dutySuspended.heading")
        }

        "the task must be completed" in {
          result.completedTask mustBe true
        }

        "a subtask for the regime must must be available in addition to the declaration subtask" in {
          result.taskList.items.size mustBe 2
        }

        "the declaration subtask's title must be correct" in {
          result.taskList.items.head.title.content mustBe Text(
            messages("taskList.section.dutySuspended.needToDeclare")
          )
        }

        "the declaration subtask must be completed" in {
          result.taskList.items.head.status mustBe AlcoholDutyTaskListItemStatus.completed
        }

        "the declaration subtask must link to the declare duty suspended deliveries question in CheckMode" in {
          result.taskList.items.head.href mustBe Some(
            controllers.dutySuspended.routes.DeclareDutySuspendedDeliveriesQuestionController.onPageLoad(CheckMode).url
          )
        }

        "the sub task must found and be completed" in {
          val maybeTask = result.taskList.items.find(
            _.title.content == Text(messages("taskList.section.dutySuspended"))
          )

          maybeTask            mustBe defined
          maybeTask.get.status mustBe AlcoholDutyTaskListItemStatus.completed
        }

        "the sub task must link to the CYA duty suspended guidance controller" in {
          val maybeTask = result.taskList.items.find(
            _.title.content == Text(messages("taskList.section.dutySuspended"))
          )

          maybeTask.get.href mustBe Some(
            controllers.dutySuspended.routes.CheckYourAnswersDutySuspendedDeliveriesController.onPageLoad().url
          )
        }

        "no hint must be displayed" in {
          result.taskList.items.head.hint.map(_.content) mustBe None
        }

        "the idPrefix must be set" in {
          result.taskList.idPrefix mustBe "dutySuspended"
        }
      }

      "and has started the declaration task but not answered for all the regimes" - {
        val validTotal                                   = 42.34
        val validPureAlcohol                             = 34.23
        val incompleteDutySuspendedDeliveriesUserAnswers = userAnswersWithAllRegimes
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

        "the task's title must be correct" in {
          result.title mustBe messages("taskList.section.dutySuspended.heading")
        }

        "the task must not be completed" in {
          result.completedTask mustBe false
        }

        "a subtask for the regime must must be available in addition to the declaration subtask" in {
          result.taskList.items.size mustBe 2
        }

        "the declaration subtask's title must be correct" in {
          result.taskList.items.head.title.content mustBe Text(
            messages("taskList.section.dutySuspended.needToDeclare")
          )
        }

        "the declaration subtask must be completed" in {
          result.taskList.items.head.status mustBe AlcoholDutyTaskListItemStatus.completed
        }

        "the subtask must link to the declare duty suspended deliveries question in CheckMode" in {
          result.taskList.items.head.href mustBe Some(
            controllers.dutySuspended.routes.DeclareDutySuspendedDeliveriesQuestionController.onPageLoad(CheckMode).url
          )
        }

        "the sub task must found and be in progress" in {
          val maybeTask = result.taskList.items.find(
            _.title.content == Text(messages("taskList.section.dutySuspended"))
          )

          maybeTask            mustBe defined
          maybeTask.get.status mustBe AlcoholDutyTaskListItemStatus.inProgress
        }

        "the sub task must link to the duty suspended guidance controller" in {
          val maybeTask = result.taskList.items.find(
            _.title.content == Text(messages("taskList.section.dutySuspended"))
          )

          maybeTask.get.href mustBe Some(
            controllers.dutySuspended.routes.DutySuspendedDeliveriesGuidanceController.onPageLoad().url
          )
        }

        "no hint must be displayed" in {
          result.taskList.items.head.hint.map(_.content) mustBe None
        }

        "the idPrefix must be set" in {
          result.taskList.idPrefix mustBe "dutySuspended"
        }
      }

      "and has started the declaration task and answered for all the regimes" - {
        val validTotal                                 = 42.34
        val validPureAlcohol                           = 34.23
        val completeDutySuspendedDeliveriesUserAnswers = userAnswersWithAllRegimes
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
              DutySuspendedWinePage.toString           -> Json.obj(
                "totalWine"         -> validTotal,
                "pureAlcoholInWine" -> validPureAlcohol
              ),
              DutySuspendedSpiritsPage.toString        -> Json.obj(
                "totalSpirits"         -> validTotal,
                "pureAlcoholInSpirits" -> validPureAlcohol
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

        "the task's title must be correct" in {
          result.title mustBe messages("taskList.section.dutySuspended.heading")
        }

        "the task must be completed" in {
          result.completedTask mustBe true
        }

        "a subtask for the regime must must be available in addition to the declaration subtask" in {
          result.taskList.items.size mustBe 2
        }

        "the declaration subtask's title must be correct" in {
          result.taskList.items.head.title.content mustBe Text(
            messages("taskList.section.dutySuspended.needToDeclare")
          )
        }

        "the declaration subtask must be completed" in {
          result.taskList.items.head.status mustBe AlcoholDutyTaskListItemStatus.completed
        }

        "the declaration subtask must link to the declare duty suspended deliveries question in CheckMode" in {
          result.taskList.items.head.href mustBe Some(
            controllers.dutySuspended.routes.DeclareDutySuspendedDeliveriesQuestionController.onPageLoad(CheckMode).url
          )
        }

        "the sub task must found and be completed" in {
          val maybeTask = result.taskList.items.find(
            _.title.content == Text(messages("taskList.section.dutySuspended"))
          )

          maybeTask            mustBe defined
          maybeTask.get.status mustBe AlcoholDutyTaskListItemStatus.completed
        }

        "the sub task must link to the CYA duty suspended guidance controller" in {
          val maybeTask = result.taskList.items.find(
            _.title.content == Text(messages("taskList.section.dutySuspended"))
          )

          maybeTask.get.href mustBe Some(
            controllers.dutySuspended.routes.CheckYourAnswersDutySuspendedDeliveriesController.onPageLoad().url
          )
        }

        "no hint must be displayed" in {
          result.taskList.items.head.hint.map(_.content) mustBe None
        }

        "the idPrefix must be set" in {
          result.taskList.idPrefix mustBe "dutySuspended"
        }
      }
    }
  }

  "on calling returnQSSection" - {
    "when the user hasn't answered the quarterly spirits question" - {
      val result = returnTaskListCreator.returnQSSection(emptyUserAnswers)

      "the task's title must be correct" in {
        result.title mustBe messages("taskList.section.spirits.heading")
      }

      "the task must not be completed" in {
        result.completedTask mustBe false
      }

      "only one subtask must be available" in {
        result.taskList.items.size mustBe 1
      }

      "the subtask's title must be correct" in {
        result.taskList.items.head.title.content mustBe Text(
          messages("taskList.section.spirits.needToDeclare")
        )
      }

      "the subtask must not be started" in {
        result.taskList.items.head.status mustBe AlcoholDutyTaskListItemStatus.notStarted
      }

      "the subtask must link to the declare quarterly spirits question" in {
        result.taskList.items.head.href mustBe Some(
          controllers.spiritsQuestions.routes.DeclareQuarterlySpiritsController.onPageLoad(NormalMode).url
        )
      }

      "a hint must be available and correct" in {
        result.taskList.items.head.hint.map(_.content) mustBe Some(
          Text(messages("taskList.section.spirits.hint"))
        )
      }

      "the idPrefix must be set" in {
        result.taskList.idPrefix mustBe "spirits"
      }
    }

    "when the user answers no to the quarterly spirits question" - {
      val userAnswers = emptyUserAnswers
        .set(DeclareQuarterlySpiritsPage, false)
        .success
        .value
      val result      = returnTaskListCreator.returnQSSection(userAnswers)

      "the task's title must be correct" in {
        result.title mustBe messages("taskList.section.spirits.heading")
      }

      "the task must be completed" in {
        result.completedTask mustBe true
      }

      "only one subtask must be available" in {
        result.taskList.items.size mustBe 1
      }

      "the subtask's title must be correct" in {
        result.taskList.items.head.title.content mustBe Text(
          messages("taskList.section.spirits.needToDeclare")
        )
      }

      "the subtask must be completed" in {
        result.taskList.items.head.status mustBe AlcoholDutyTaskListItemStatus.completed
      }

      "the subtask must link to the declare quarterly spirits in CheckMode" in {
        result.taskList.items.head.href mustBe Some(
          controllers.spiritsQuestions.routes.DeclareQuarterlySpiritsController.onPageLoad(CheckMode).url
        )
      }

      "a hint must be available and correct" in {
        result.taskList.items.head.hint.map(_.content) mustBe Some(
          Text(messages("taskList.section.spirits.hint"))
        )
      }

      "the idPrefix must be set" in {
        result.taskList.idPrefix mustBe "spirits"
      }
    }

    "when the user answers yes to the quarterly spirits question" - {
      val declareQuarterlySpiritsPage = Json.obj(DeclareQuarterlySpiritsPage.toString -> true)
      val declareSpiritsTotalPage     = Json.obj(DeclareSpiritsTotalPage.toString -> 10000)
      val whiskyPage                  = Json.obj(WhiskyPage.toString -> Json.obj("scotchWhisky" -> 5000, "irishWhiskey" -> 2500))
      val spiritTypePage              = Json.obj(SpiritTypePage.toString -> Seq("maltSpirits", "ciderOrPerry"))
      val spiritTypePageWithOther     = Json.obj(SpiritTypePage.toString -> Seq("maltSpirits", "ciderOrPerry", "other"))
      val otherSpiritsProducedPage    = Json.obj(OtherSpiritsProducedPage.toString -> "Coco Pops Vodka")

      def setUserAnswers(pages: Seq[JsObject]): UserAnswers = emptyUserAnswers.copy(data = pages.reduce(_ ++ _))

      "but has not started the declaration task" - {
        val userAnswers = emptyUserAnswers
          .set(DeclareQuarterlySpiritsPage, true)
          .success
          .value
        val result      = returnTaskListCreator.returnQSSection(userAnswers)

        "the task's title must be correct" in {
          result.title mustBe messages("taskList.section.spirits.heading")
        }

        "the task must not be completed" in {
          result.completedTask mustBe false
        }

        "a subtask for the details must be available in addition to the declaration subtask" in {
          result.taskList.items.size mustBe 2
        }

        "the declaration subtask's title must be correct" in {
          result.taskList.items.head.title.content mustBe Text(
            messages("taskList.section.spirits.needToDeclare")
          )
        }

        "the declaration subtask must be completed" in {
          result.taskList.items.head.status mustBe AlcoholDutyTaskListItemStatus.completed
        }

        "the declaration subtask must link to the declare quarterly spirits question in CheckMode" in {
          result.taskList.items.head.href mustBe Some(
            controllers.spiritsQuestions.routes.DeclareQuarterlySpiritsController.onPageLoad(CheckMode).url
          )
        }

        "the details subtask must be found and be not started" in {
          val maybeTask = result.taskList.items.find(
            _.title.content == Text(messages("taskList.section.spirits"))
          )

          maybeTask            mustBe defined
          maybeTask.get.status mustBe AlcoholDutyTaskListItemStatus.notStarted
        }

        "the details subtask must link to the declare spirits total controller" in {
          val maybeTask = result.taskList.items.find(
            _.title.content == Text(messages("taskList.section.spirits"))
          )

          maybeTask.get.href mustBe Some(
            controllers.spiritsQuestions.routes.DeclareSpiritsTotalController.onPageLoad(NormalMode).url
          )
        }

        "a hint must be available and correct" in {
          result.taskList.items.head.hint.map(_.content) mustBe Some(
            Text(messages("taskList.section.spirits.hint"))
          )
        }

        "the idPrefix must be set" in {
          result.taskList.idPrefix mustBe "spirits"
        }
      }

      Seq(
        ("whisky", declareSpiritsTotalPage),
        ("spirits type", whiskyPage)
      ).foldLeft(Seq(declareQuarterlySpiritsPage)) { case (completedBefore, (nextPageName, page)) =>
        val completedPages = completedBefore :+ page

        s"and has started the declaration task when $nextPageName has just been completed" - {
          val userAnswers = setUserAnswers(completedPages)
          val result      = returnTaskListCreator.returnQSSection(userAnswers)

          "the task's title must be correct" in {
            result.title mustBe messages("taskList.section.spirits.heading")
          }

          "the task must not be completed" in {
            result.completedTask mustBe false
          }

          "a subtask for the details must be available in addition to the declaration subtask" in {
            result.taskList.items.size mustBe 2
          }

          "the declaration subtask's title must be correct" in {
            result.taskList.items.head.title.content mustBe Text(
              messages("taskList.section.spirits.needToDeclare")
            )
          }

          "the declaration subtask must be completed" in {
            result.taskList.items.head.status mustBe AlcoholDutyTaskListItemStatus.completed
          }

          "the declaration subtask must link to the declare quarterly spirits question in CheckMode" in {
            result.taskList.items.head.href mustBe Some(
              controllers.spiritsQuestions.routes.DeclareQuarterlySpiritsController.onPageLoad(CheckMode).url
            )
          }

          "the details subtask must be found and be in progress" in {
            val maybeTask = result.taskList.items.find(
              _.title.content == Text(messages("taskList.section.spirits"))
            )

            maybeTask            mustBe defined
            maybeTask.get.status mustBe AlcoholDutyTaskListItemStatus.inProgress
          }

          "the details subtask must link to the declare spirits total controller" in {
            val maybeTask = result.taskList.items.find(
              _.title.content == Text(messages("taskList.section.spirits"))
            )

            maybeTask.get.href mustBe Some(
              controllers.spiritsQuestions.routes.DeclareSpiritsTotalController.onPageLoad(NormalMode).url
            )
          }

          "a hint must be available and correct" in {
            result.taskList.items.head.hint.map(_.content) mustBe Some(
              Text(messages("taskList.section.spirits.hint"))
            )
          }

          "the idPrefix must be set" in {
            result.taskList.idPrefix mustBe "spirits"
          }
        }

        completedPages
      }

      "and has started the declaration task but other spirits needs to be completed and has not been" - {
        val userAnswers = setUserAnswers(
          Seq(
            declareQuarterlySpiritsPage,
            declareSpiritsTotalPage,
            whiskyPage,
            spiritTypePageWithOther
          )
        )
        val result      = returnTaskListCreator.returnQSSection(userAnswers)

        "the task's title must be correct" in {
          result.title mustBe messages("taskList.section.spirits.heading")
        }

        "the task must not be completed" in {
          result.completedTask mustBe false
        }

        "a subtask for the details must be available in addition to the declaration subtask" in {
          result.taskList.items.size mustBe 2
        }

        "the declaration subtask's title must be correct" in {
          result.taskList.items.head.title.content mustBe Text(
            messages("taskList.section.spirits.needToDeclare")
          )
        }

        "the declaration subtask must be completed" in {
          result.taskList.items.head.status mustBe AlcoholDutyTaskListItemStatus.completed
        }

        "the declaration subtask must link to the declare quarterly spirits question in CheckMode" in {
          result.taskList.items.head.href mustBe Some(
            controllers.spiritsQuestions.routes.DeclareQuarterlySpiritsController.onPageLoad(CheckMode).url
          )
        }

        "the details subtask must be found and be in progress" in {
          val maybeTask = result.taskList.items.find(
            _.title.content == Text(messages("taskList.section.spirits"))
          )

          maybeTask            mustBe defined
          maybeTask.get.status mustBe AlcoholDutyTaskListItemStatus.inProgress
        }

        "the details subtask must link to the declare spirits total controller" in {
          val maybeTask = result.taskList.items.find(
            _.title.content == Text(messages("taskList.section.spirits"))
          )

          maybeTask.get.href mustBe Some(
            controllers.spiritsQuestions.routes.DeclareSpiritsTotalController.onPageLoad(NormalMode).url
          )
        }

        "a hint must be available and correct" in {
          result.taskList.items.head.hint.map(_.content) mustBe Some(
            Text(messages("taskList.section.spirits.hint"))
          )
        }

        "the idPrefix must be set" in {
          result.taskList.idPrefix mustBe "spirits"
        }
      }

      "and has completed the declaration task but didn't need to declare other spirits" - {
        val userAnswers = setUserAnswers(
          Seq(
            declareQuarterlySpiritsPage,
            declareSpiritsTotalPage,
            whiskyPage,
            spiritTypePage
          )
        )

        val result = returnTaskListCreator.returnQSSection(userAnswers)

        "the task's title must be correct" in {
          result.title mustBe messages("taskList.section.spirits.heading")
        }

        "the task must be completed" in {
          result.completedTask mustBe true
        }

        "a subtask for the details must be available in addition to the declaration subtask" in {
          result.taskList.items.size mustBe 2
        }

        "the declaration subtask's title must be correct" in {
          result.taskList.items.head.title.content mustBe Text(
            messages("taskList.section.spirits.needToDeclare")
          )
        }

        "the declaration subtask must be completed" in {
          result.taskList.items.head.status mustBe AlcoholDutyTaskListItemStatus.completed
        }

        "the declaration subtask must link to the declare quarterly spirits question in CheckMode" in {
          result.taskList.items.head.href mustBe Some(
            controllers.spiritsQuestions.routes.DeclareQuarterlySpiritsController.onPageLoad(CheckMode).url
          )
        }

        "the details subtask must be found and be completed" in {
          val maybeTask = result.taskList.items.find(
            _.title.content == Text(messages("taskList.section.spirits"))
          )

          maybeTask            mustBe defined
          maybeTask.get.status mustBe AlcoholDutyTaskListItemStatus.completed
        }

        "the details subtask must link to the CYA page" in {
          val maybeTask = result.taskList.items.find(
            _.title.content == Text(messages("taskList.section.spirits"))
          )

          maybeTask.get.href mustBe Some(
            controllers.spiritsQuestions.routes.CheckYourAnswersController.onPageLoad().url
          )
        }

        "a hint must be available and correct" in {
          result.taskList.items.head.hint.map(_.content) mustBe Some(
            Text(messages("taskList.section.spirits.hint"))
          )
        }

        "the idPrefix must be set" in {
          result.taskList.idPrefix mustBe "spirits"
        }
      }

      "and has completed the declaration task as well as other spirits" - {
        val userAnswers = setUserAnswers(
          Seq(
            declareQuarterlySpiritsPage,
            declareSpiritsTotalPage,
            whiskyPage,
            spiritTypePageWithOther,
            otherSpiritsProducedPage
          )
        )
        val result      = returnTaskListCreator.returnQSSection(userAnswers)

        "the task's title must be correct" in {
          result.title mustBe messages("taskList.section.spirits.heading")
        }

        "the task must be completed" in {
          result.completedTask mustBe true
        }

        "a subtask for the details must be available in addition to the declaration subtask" in {
          result.taskList.items.size mustBe 2
        }

        "the declaration subtask's title must be correct" in {
          result.taskList.items.head.title.content mustBe Text(
            messages("taskList.section.spirits.needToDeclare")
          )
        }

        "the declaration subtask must be completed" in {
          result.taskList.items.head.status mustBe AlcoholDutyTaskListItemStatus.completed
        }

        "the declaration subtask must link to the declare quarterly spirits question in CheckMode" in {
          result.taskList.items.head.href mustBe Some(
            controllers.spiritsQuestions.routes.DeclareQuarterlySpiritsController.onPageLoad(CheckMode).url
          )
        }

        "the details subtask must be found and be completed" in {
          val maybeTask = result.taskList.items.find(
            _.title.content == Text(messages("taskList.section.spirits"))
          )

          maybeTask            mustBe defined
          maybeTask.get.status mustBe AlcoholDutyTaskListItemStatus.completed
        }

        "the details subtask must link to the CYA page" in {
          val maybeTask = result.taskList.items.find(
            _.title.content == Text(messages("taskList.section.spirits"))
          )

          maybeTask.get.href mustBe Some(
            controllers.spiritsQuestions.routes.CheckYourAnswersController.onPageLoad().url
          )
        }

        "a hint must be available and correct" in {
          result.taskList.items.head.hint.map(_.content) mustBe Some(
            Text(messages("taskList.section.spirits.hint"))
          )
        }

        "the idPrefix must be set" in {
          result.taskList.idPrefix mustBe "spirits"
        }
      }
    }
  }

  "on calling checkAndSubmitSection" - {
    "and the other sections are incomplete, it must return the cannot start section" - {
      val result = returnTaskListCreator.returnCheckAndSubmitSection(0, 4)

      "the task's title must be correct" in {
        result.title mustBe messages("taskList.section.checkAndSubmit.heading")
      }

      "the task must not be completed" in {
        result.completedTask mustBe false
      }

      "only the task must be available" in {
        result.taskList.items.size mustBe 1
      }

      "the check and submit subtask's title must be correct" in {
        result.taskList.items.head.title.content mustBe Text(
          messages("taskList.section.checkAndSubmit.needToDeclare")
        )
      }

      "the check and submit subtask must be cannot start" in {
        result.taskList.items.head.status mustBe AlcoholDutyTaskListItemStatus.cannotStart
      }

      "the subtask must not have a link" in {
        result.taskList.items.head.href mustBe None
      }

      "a hint must be available and correct" in {
        result.taskList.items.head.hint.map(_.content) mustBe Some(
          Text(messages("taskList.section.checkAndSubmit.hint"))
        )
      }

      "the idPrefix must be set" in {
        result.taskList.idPrefix mustBe "checkAndSubmit"
      }
    }

    "and the other sections are complete, it must return the task" - {
      val result = returnTaskListCreator.returnCheckAndSubmitSection(4, 4)

      "the task's title must be correct" in {
        result.title mustBe messages("taskList.section.checkAndSubmit.heading")
      }

      "the task must not be completed" in {
        result.completedTask mustBe false
      }

      "only the task must be available" in {
        result.taskList.items.size mustBe 1
      }

      "the declaration subtask's title must be correct" in {
        result.taskList.items.head.title.content mustBe Text(
          messages("taskList.section.checkAndSubmit.needToDeclare")
        )
      }

      "the check and submit subtask must be not started" in {
        result.taskList.items.head.status mustBe AlcoholDutyTaskListItemStatus.notStarted
      }

      "the subtask must link to the duty due for this return page" in {
        result.taskList.items.head.href mustBe Some(
          controllers.checkAndSubmit.routes.DutyDueForThisReturnController.onPageLoad().url
        )
      }

      "no hint must be displayed" in {
        result.taskList.items.head.hint.map(_.content) mustBe None
      }

      "the idPrefix must be set" in {
        result.taskList.idPrefix mustBe "checkAndSubmit"
      }
    }
  }
}
