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

package controllers

import base.SpecBase
import models.UserAnswers
import pages.{DeclareDutySuspendedDeliveriesOutsideUkPage, DeclareDutySuspendedReceivedPage, DutySuspendedDeliveriesPage}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import viewmodels.checkAnswers.CheckYourAnswersSummaryListHelper
import viewmodels.govuk.SummaryListFluency
import views.html.dutySuspended.CheckYourAnswersDutySuspendedDeliveriesView

class CheckYourAnswersDutySuspendedDeliveriesControllerSpec extends SpecBase with SummaryListFluency {

  "Check Your Answers Duty Suspended Deliveries Controller" - {

    "must return OK and the correct view for a GET if all necessary questions are answered" in {

      val application = applicationBuilder(userAnswers = Some(completeDutySuspendedDeliveriesUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.CheckYourAnswersDutySuspendedDeliveriesController.onPageLoad.url)

        val result = route(application, request).value

        val view                   = application.injector.instanceOf[CheckYourAnswersDutySuspendedDeliveriesView]
        val checkYourAnswersHelper =
          new CheckYourAnswersSummaryListHelper(completeDutySuspendedDeliveriesUserAnswers)(messages(application))
        val list                   = checkYourAnswersHelper.dutySuspendedDeliveriesSummaryList.get

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(list)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET" - {

      "if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(GET, routes.CheckYourAnswersDutySuspendedDeliveriesController.onPageLoad.url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
        }
      }
      "if all necessary questions are answered are not answered" in {
        val incompleteUserAnswers1 =
          completeDutySuspendedDeliveriesUserAnswers.remove(DeclareDutySuspendedDeliveriesOutsideUkPage).success.value
        val incompleteUserAnswers2 =
          completeDutySuspendedDeliveriesUserAnswers.remove(DutySuspendedDeliveriesPage).success.value
        val incompleteUserAnswers3 =
          completeDutySuspendedDeliveriesUserAnswers.remove(DeclareDutySuspendedReceivedPage).success.value

        val incompleteUserAnswersList = Seq(incompleteUserAnswers1, incompleteUserAnswers2, incompleteUserAnswers3)

        incompleteUserAnswersList.foreach { (incompleteUserAnswers: UserAnswers) =>
          val application = applicationBuilder(Some(incompleteUserAnswers)).build()

          running(application) {
            val request = FakeRequest(GET, routes.CheckYourAnswersDutySuspendedDeliveriesController.onPageLoad.url)

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
          }
        }
      }
    }
  }
}
