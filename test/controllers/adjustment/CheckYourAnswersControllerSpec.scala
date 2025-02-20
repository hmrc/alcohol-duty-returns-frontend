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

package controllers.adjustment

import base.SpecBase
import connectors.UserAnswersConnector
import generators.ModelGenerators
import models.adjustment.AdjustmentEntry
import models.UserAnswers
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import pages.adjustment._
import play.api.inject.bind
import play.api.test.Helpers._
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import uk.gov.hmrc.http.HttpResponse
import viewmodels.checkAnswers.adjustment.CheckYourAnswersSummaryListHelper
import views.html.adjustment.CheckYourAnswersView

import scala.concurrent.Future

class CheckYourAnswersControllerSpec extends SpecBase with ModelGenerators {
  "CheckYourAnswersController" - {
    "must display answers when no index is supplied and update current adjustment entry" in new SetUp {
      val userAnswers: UserAnswers = emptyUserAnswers
        .set(CurrentAdjustmentEntryPage, adjustmentEntry)
        .success
        .value

      val application = applicationBuilder(Some(userAnswers))
        .overrides(
          bind[CheckYourAnswersSummaryListHelper].toInstance(mockCheckYourAnswersSummaryListHelper),
          bind[CheckYourAnswersView].toInstance(mockView),
          bind[UserAnswersConnector].toInstance(mockUserAnswersConnector)
        )
        .build()

      when(mockCheckYourAnswersSummaryListHelper.currentAdjustmentEntrySummaryList(eqTo(adjustmentEntry))(any()))
        .thenReturn(Some(summaryList))

      when(mockUserAnswersConnector.set(any())(any())).thenReturn(Future(HttpResponse(OK)))

      running(application) {
        val request = FakeRequest(GET, checkYourAnswersRoute)

        val result = route(application, request).value

        status(result)          mustEqual OK
        contentAsString(result) mustEqual content.body

        verify(mockUserAnswersConnector, times(1)).set(eqTo(userAnswers))(any())
      }
    }

    "must display answers when an index is supplied and update current adjustment entry" in new SetUp {
      val userAnswers: UserAnswers = emptyUserAnswers
        .set(AdjustmentEntryListPage, Seq(adjustmentEntry))
        .success
        .value

      val userAnswersWithCurrentAdjustmentSet: UserAnswers = userAnswers
        .set(CurrentAdjustmentEntryPage, adjustmentEntryWithIndex)
        .success
        .value

      val application = applicationBuilder(Some(userAnswers))
        .overrides(
          bind[CheckYourAnswersSummaryListHelper].toInstance(mockCheckYourAnswersSummaryListHelper),
          bind[CheckYourAnswersView].toInstance(mockView),
          bind[UserAnswersConnector].toInstance(mockUserAnswersConnector)
        )
        .build()

      when(
        mockCheckYourAnswersSummaryListHelper.currentAdjustmentEntrySummaryList(eqTo(adjustmentEntryWithIndex))(any())
      ).thenReturn(Some(summaryList))

      when(mockUserAnswersConnector.set(any())(any())).thenReturn(Future(HttpResponse(OK)))

      running(application) {
        val request = FakeRequest(GET, checkYourAnswersRouteWithIndex)

        val result = route(application, request).value

        status(result)          mustEqual OK
        contentAsString(result) mustEqual content.body

        verify(mockUserAnswersConnector, times(1)).set(eqTo(userAnswersWithCurrentAdjustmentSet))(any())
      }
    }

    "must redirect to journey recovery when unable to get a page" in new SetUp {
      val application = applicationBuilder(Some(emptyUserAnswers))
        .overrides(
          bind[CheckYourAnswersSummaryListHelper].toInstance(mockCheckYourAnswersSummaryListHelper),
          bind[CheckYourAnswersView].toInstance(mockView),
          bind[UserAnswersConnector].toInstance(mockUserAnswersConnector)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, checkYourAnswersRoute)

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url

        verify(mockUserAnswersConnector, never).set(any())(any())
      }
    }

    "must redirect to journey recovery when a problem getting the summary list" in new SetUp {
      val userAnswers: UserAnswers = emptyUserAnswers
        .set(AdjustmentEntryListPage, Seq(adjustmentEntry))
        .success
        .value

      val application = applicationBuilder(Some(userAnswers))
        .overrides(
          bind[CheckYourAnswersSummaryListHelper].toInstance(mockCheckYourAnswersSummaryListHelper),
          bind[CheckYourAnswersView].toInstance(mockView),
          bind[UserAnswersConnector].toInstance(mockUserAnswersConnector)
        )
        .build()

      when(
        mockCheckYourAnswersSummaryListHelper.currentAdjustmentEntrySummaryList(eqTo(adjustmentEntryWithIndex))(any())
      ).thenReturn(None)

      running(application) {
        val request = FakeRequest(GET, checkYourAnswersRouteWithIndex)

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url

        verify(mockUserAnswersConnector, never).set(any())(any())
      }
    }

    "must redirect to the next page when valid data is submitted and no index" in new SetUp {
      val userAnswers: UserAnswers = emptyUserAnswers
        .set(CurrentAdjustmentEntryPage, completedAdjustmentEntry)
        .success
        .value

      val userAnswersWithAdjustmentEntryListPage: UserAnswers = emptyUserAnswers
        .set(AdjustmentEntryListPage, Seq(completedAdjustmentEntry))
        .success
        .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[UserAnswersConnector].toInstance(mockUserAnswersConnector)
          )
          .build()

      when(mockUserAnswersConnector.set(any())(any())).thenReturn(Future.successful(mock[HttpResponse]))

      running(application) {
        val request =
          FakeRequest(POST, controllers.adjustment.routes.CheckYourAnswersController.onSubmit().url)

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.adjustment.routes.AdjustmentListController
          .onPageLoad(1)
          .url

        verify(mockUserAnswersConnector, times(1)).set(eqTo(userAnswersWithAdjustmentEntryListPage))(any())
      }
    }

    "must redirect to the next page when valid data is submitted and an index" in new SetUp {
      val userAnswers: UserAnswers = emptyUserAnswers
        .set(CurrentAdjustmentEntryPage, completedAdjustmentEntryWithIndex)
        .success
        .value

      val userAnswersWithAdjustmentEntryListPage: UserAnswers = emptyUserAnswers
        .set(AdjustmentEntryListPage, Seq(completedAdjustmentEntry))
        .success
        .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[UserAnswersConnector].toInstance(mockUserAnswersConnector)
          )
          .build()

      when(mockUserAnswersConnector.set(any())(any())).thenReturn(Future.successful(mock[HttpResponse]))

      running(application) {
        val request =
          FakeRequest(POST, controllers.adjustment.routes.CheckYourAnswersController.onSubmit().url)

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.adjustment.routes.AdjustmentListController
          .onPageLoad(1)
          .url

        verify(mockUserAnswersConnector, times(1)).set(eqTo(userAnswersWithAdjustmentEntryListPage))(any())
      }
    }

    "must redirect to journey recovery no CurrentAdjustmentEntry page found" in new SetUp {
      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[UserAnswersConnector].toInstance(mockUserAnswersConnector)
          )
          .build()

      when(mockUserAnswersConnector.set(any())(any())).thenReturn(Future.successful(mock[HttpResponse]))

      running(application) {
        val request =
          FakeRequest(POST, controllers.adjustment.routes.CheckYourAnswersController.onSubmit().url)

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url

        verify(mockUserAnswersConnector, never).set(any())(any())
      }
    }

    "must redirect to journey recovery when submitting an incomplete entry" in new SetUp {
      val userAnswers: UserAnswers = emptyUserAnswers
        .set(CurrentAdjustmentEntryPage, adjustmentEntry)
        .success
        .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[UserAnswersConnector].toInstance(mockUserAnswersConnector)
          )
          .build()

      when(mockUserAnswersConnector.set(any())(any())).thenReturn(Future.successful(mock[HttpResponse]))

      running(application) {
        val request =
          FakeRequest(POST, controllers.adjustment.routes.CheckYourAnswersController.onSubmit().url)

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url

        verify(mockUserAnswersConnector, never).set(any())(any())
      }
    }
  }

  class SetUp {
    val index = 0

    val adjustmentEntry = AdjustmentEntry()
    val summaryList     = SummaryList()

    val completedAdjustmentEntry = fullRepackageAdjustmentEntry.copy(index = None)

    val adjustmentEntryWithIndex          = adjustmentEntry.copy(index = Some(index))
    val completedAdjustmentEntryWithIndex = fullRepackageAdjustmentEntry

    val checkYourAnswersRoute          = controllers.adjustment.routes.CheckYourAnswersController.onSubmit().url
    val checkYourAnswersRouteWithIndex =
      controllers.adjustment.routes.CheckYourAnswersController.onPageLoad(index = Some(index)).url

    val mockCheckYourAnswersSummaryListHelper = mock[CheckYourAnswersSummaryListHelper]

    val mockView = mock[CheckYourAnswersView]

    val content = Html("blah")

    when(mockView.apply(any())(any(), any())).thenReturn(content)

    val mockUserAnswersConnector = mock[UserAnswersConnector]
  }
}
