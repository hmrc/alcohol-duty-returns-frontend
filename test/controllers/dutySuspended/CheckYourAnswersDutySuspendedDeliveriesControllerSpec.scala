/*
 * Copyright 2023 HM Revenue & Customs
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

package controllers.dutySuspended

import base.SpecBase
import models.UserAnswers
import pages.dutySuspended.{DutySuspendedBeerPage, DutySuspendedCiderPage, DutySuspendedOtherFermentedPage, DutySuspendedSpiritsPage, DutySuspendedWinePage}
import play.api.libs.json.Json
import play.api.test.Helpers._
import viewmodels.checkAnswers.dutySuspended.CheckYourAnswersSummaryListHelper
import viewmodels.govuk.SummaryListFluency
import views.html.dutySuspended.CheckYourAnswersDutySuspendedDeliveriesView

class CheckYourAnswersDutySuspendedDeliveriesControllerSpec extends SpecBase with SummaryListFluency {
  val validTotal       = 42.34
  val validPureAlcohol = 34.23

  val completeDutySuspendedDeliveriesUserAnswers: UserAnswers =
    Seq(
      (
        DutySuspendedBeerPage.path,
        Json.obj(
          "totalBeer"         -> validTotal,
          "pureAlcoholInBeer" -> validPureAlcohol
        )
      ),
      (
        DutySuspendedCiderPage.path,
        Json.obj(
          "totalCider"         -> validTotal,
          "pureAlcoholInCider" -> validPureAlcohol
        )
      ),
      (
        DutySuspendedWinePage.path,
        Json.obj(
          "totalWine"         -> validTotal,
          "pureAlcoholInWine" -> validPureAlcohol
        )
      ),
      (
        DutySuspendedSpiritsPage.path,
        Json.obj(
          "totalSpirits"         -> validTotal,
          "pureAlcoholInSpirits" -> validPureAlcohol
        )
      ),
      (
        DutySuspendedOtherFermentedPage.path,
        Json.obj(
          "totalOtherFermented"         -> validTotal,
          "pureAlcoholInOtherFermented" -> validPureAlcohol
        )
      )
    ).foldLeft(userAnswersWithAllRegimes) { case (userAnswers, (path, obj)) =>
      userAnswers.copy(data = userAnswers.set(path, obj).get)
    }

  "Check Your Answers Duty Suspended Deliveries Controller" - {

    "must return OK and the correct view for a GET if all necessary questions are answered" in {

      val application = applicationBuilder(userAnswers = Some(completeDutySuspendedDeliveriesUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(
            GET,
            controllers.dutySuspended.routes.CheckYourAnswersDutySuspendedDeliveriesController.onPageLoad().url
          )

        val result = route(application, request).value

        val view                   = application.injector.instanceOf[CheckYourAnswersDutySuspendedDeliveriesView]
        val checkYourAnswersHelper =
          new CheckYourAnswersSummaryListHelper(completeDutySuspendedDeliveriesUserAnswers)(messages(application))
        val summaryList            = checkYourAnswersHelper.dutySuspendedDeliveriesSummaryList

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(summaryList)(request, messages(application)).toString
      }
    }

    "must return OK and the correct view for a GET if all relevant regime questions are answered" in {

      val beerScreenAnswer: UserAnswers = userAnswersWithBeer.copy(data =
        userAnswersWithBeer
          .set(
            DutySuspendedBeerPage.path,
            Json.obj(
              "totalBeer"         -> validTotal,
              "pureAlcoholInBeer" -> validPureAlcohol
            )
          )
          .get
      )

      val application = applicationBuilder(userAnswers = Some(beerScreenAnswer)).build()

      running(application) {
        val request =
          FakeRequest(
            GET,
            controllers.dutySuspended.routes.CheckYourAnswersDutySuspendedDeliveriesController.onPageLoad().url
          )

        val result = route(application, request).value

        val view                   = application.injector.instanceOf[CheckYourAnswersDutySuspendedDeliveriesView]
        val checkYourAnswersHelper =
          new CheckYourAnswersSummaryListHelper(beerScreenAnswer)(messages(application))
        val summaryList            = checkYourAnswersHelper.dutySuspendedDeliveriesSummaryList

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(summaryList)(request, messages(application)).toString
      }
    }
  }
}
