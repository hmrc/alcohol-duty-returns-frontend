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
import models.AlcoholRegime.{Beer, Cider, Spirits, Wine}
import models.{AlcoholRegime, AlcoholRegimes, UserAnswers}
import pages.AlcoholRegimePage
import pages.dutySuspended.{DutySuspendedBeerPage, DutySuspendedCiderPage, DutySuspendedOtherFermentedPage, DutySuspendedSpiritsPage, DutySuspendedWinePage}
import play.api.libs.json.Json
import play.api.test.Helpers._
import viewmodels.checkAnswers.dutySuspended.CheckYourAnswersSummaryListHelper
import viewmodels.govuk.SummaryListFluency
import views.html.dutySuspended.CheckYourAnswersDutySuspendedDeliveriesView

class CheckYourAnswersDutySuspendedDeliveriesControllerSpec extends SpecBase with SummaryListFluency {
  val validTotal                                              = 42.34
  val validPureAlcohol                                        = 34.23
  val completeDutySuspendedDeliveriesUserAnswers: UserAnswers = userAnswersWithAllRegimes.copy(data =
    Json.obj(
      AlcoholRegimePage.toString               -> Json.toJson(AlcoholRegime.values),
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
        val checkYourAnswersHelper = new CheckYourAnswersSummaryListHelper()
        val list                   = checkYourAnswersHelper.dutySuspendedDeliveriesSummaryList(
          completeDutySuspendedDeliveriesUserAnswers
        )(getMessages(application))

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(list)(request, getMessages(application)).toString
      }
    }

    Seq(
      (DutySuspendedBeerPage, Beer),
      (DutySuspendedCiderPage, Cider),
      (DutySuspendedWinePage, Wine),
      (DutySuspendedSpiritsPage, Spirits)
    ).foreach { case (page, regime) =>
      s"must return OK and the correct view for a GET if all relevant ${regime.entryName} questions are answered" in {
        val userAnswers: UserAnswers = emptyUserAnswers.copy(
          regimes = AlcoholRegimes(Set(regime)),
          data = Json.obj(
            page.toString -> Json.obj(
              s"total${regime.entryName}"         -> validTotal,
              s"pureAlcoholIn${regime.entryName}" -> validPureAlcohol
            )
          )
        )

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request =
            FakeRequest(
              GET,
              controllers.dutySuspended.routes.CheckYourAnswersDutySuspendedDeliveriesController.onPageLoad().url
            )

          val result = route(application, request).value

          val view                   = application.injector.instanceOf[CheckYourAnswersDutySuspendedDeliveriesView]
          val checkYourAnswersHelper = new CheckYourAnswersSummaryListHelper()
          val list                   = checkYourAnswersHelper.dutySuspendedDeliveriesSummaryList(userAnswers)(getMessages(application))

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(list)(request, getMessages(application)).toString
        }
      }
    }

    "must redirect to Journey Recovery for a GET" - {
      "if no existing data is found" in {
        val application = applicationBuilder(Some(userAnswersWithAllRegimes)).build()
        running(application) {
          val request =
            FakeRequest(
              GET,
              controllers.dutySuspended.routes.CheckYourAnswersDutySuspendedDeliveriesController.onPageLoad().url
            )

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }
  }
}
