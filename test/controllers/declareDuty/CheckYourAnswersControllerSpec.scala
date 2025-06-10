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

package controllers.declareDuty

import base.SpecBase
import connectors.UserAnswersConnector
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import pages.declareDuty.{MissingRateBandsToDeletePage, MultipleSPRMissingDetailsConfirmationPage, MultipleSPRMissingDetailsPage}
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryList, SummaryListRow, Value}
import uk.gov.hmrc.http.HttpResponse
import viewmodels.declareDuty.{CheckYourAnswersSummaryListHelper, ReturnSummaryList}
import views.html.declareDuty.CheckYourAnswersView

import scala.concurrent.Future

class CheckYourAnswersControllerSpec extends SpecBase {
  "CheckYourAnswers Controller" - {
    "must return OK and the correct view for a GET when no missing rate bands have just been removed" in new SetUp {
      when(
        mockCheckYourAnswersSummaryListHelper.createSummaryList(eqTo(regime), eqTo(emptyUserAnswers))(any())
      ) thenReturn Some(returnSummaryList)
      when(mockUserAnswersConnector.set(eqTo(emptyUserAnswers))(any())) thenReturn Future.successful(mock[HttpResponse])

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[CheckYourAnswersSummaryListHelper].toInstance(mockCheckYourAnswersSummaryListHelper))
        .overrides(bind[UserAnswersConnector].toInstance(mockUserAnswersConnector))
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.declareDuty.routes.CheckYourAnswersController.onPageLoad(regime).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CheckYourAnswersView]

        status(result)          mustEqual OK
        contentAsString(result) mustEqual view(regime, returnSummaryList, None)(
          request,
          getMessages(application)
        ).toString

        verify(mockUserAnswersConnector, times(1)).set(eqTo(emptyUserAnswers))(any())
      }
    }

    "must return OK and the correct view for a GET (with notification banner) when some rate bands have just been removed" in new SetUp {
      val userAnswersWithPagesToDelete =
        emptyUserAnswers
          .setByKey(MultipleSPRMissingDetailsPage, regime, false)
          .success
          .value
          .setByKey(MultipleSPRMissingDetailsConfirmationPage, regime, true)
          .success
          .value
          .setByKey(MissingRateBandsToDeletePage, regime, MultipleSPRMissingDetails.missingSPRRateBands(regime))
          .success
          .value

      val expectedCachedUserAnswers = emptyUserAnswers.copy(data =
        Json.obj(
          MultipleSPRMissingDetailsPage.toString             -> Json.obj(),
          MultipleSPRMissingDetailsConfirmationPage.toString -> Json.obj(),
          MissingRateBandsToDeletePage.toString              -> Json.obj()
        )
      )

      when(
        mockCheckYourAnswersSummaryListHelper.createSummaryList(eqTo(regime), eqTo(expectedCachedUserAnswers))(any())
      ) thenReturn Some(returnSummaryList)
      when(mockUserAnswersConnector.set(eqTo(expectedCachedUserAnswers))(any())) thenReturn Future.successful(
        mock[HttpResponse]
      )

      val application = applicationBuilder(userAnswers = Some(userAnswersWithPagesToDelete))
        .overrides(bind[CheckYourAnswersSummaryListHelper].toInstance(mockCheckYourAnswersSummaryListHelper))
        .overrides(bind[UserAnswersConnector].toInstance(mockUserAnswersConnector))
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.declareDuty.routes.CheckYourAnswersController.onPageLoad(regime).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CheckYourAnswersView]

        val removedRateBandDescriptions = MultipleSPRMissingDetails.missingRateBandDescriptions(regime)

        status(result)          mustEqual OK
        contentAsString(result) mustEqual view(regime, returnSummaryList, Some(removedRateBandDescriptions))(
          request,
          getMessages(application)
        ).toString

        verify(mockUserAnswersConnector, times(1)).set(eqTo(expectedCachedUserAnswers))(any())
      }
    }

    "must redirect to the Journey Recovery page when no summary can be returned" in new SetUp {
      when(
        mockCheckYourAnswersSummaryListHelper.createSummaryList(eqTo(regime), eqTo(emptyUserAnswers))(any())
      ) thenReturn None
      when(mockUserAnswersConnector.set(eqTo(emptyUserAnswers))(any())) thenReturn Future.successful(mock[HttpResponse])

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[CheckYourAnswersSummaryListHelper].toInstance(mockCheckYourAnswersSummaryListHelper))
        .overrides(bind[UserAnswersConnector].toInstance(mockUserAnswersConnector))
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.declareDuty.routes.CheckYourAnswersController.onPageLoad(regime).url)

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url

        verify(mockUserAnswersConnector, times(1)).set(eqTo(emptyUserAnswers))(any())
      }
    }

    class SetUp {
      val mockCheckYourAnswersSummaryListHelper: CheckYourAnswersSummaryListHelper =
        mock[CheckYourAnswersSummaryListHelper]

      val mockUserAnswersConnector: UserAnswersConnector = mock[UserAnswersConnector]

      val regime = regimeGen.sample.value

      val summaryList1 = SummaryList(rows =
        Seq(SummaryListRow(key = Key(content = Text("Key1")), value = Value(content = Text("Value1"))))
      )
      val summaryList2 = SummaryList(rows =
        Seq(SummaryListRow(key = Key(content = Text("Key2")), value = Value(content = Text("Value2"))))
      )
      val summaryList3 = SummaryList(rows =
        Seq(SummaryListRow(key = Key(content = Text("Key3")), value = Value(content = Text("Value3"))))
      )

      val returnSummaryList = ReturnSummaryList(
        whatDoYouNeedToDeclareSummary = summaryList1,
        howMuchDoYouNeedToDeclareSummary = Some(summaryList2),
        smallProducerReliefSummary = Some(summaryList3)
      )
    }
  }
}
