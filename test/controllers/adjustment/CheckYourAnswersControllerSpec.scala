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
import cats.data.NonEmptySeq
import connectors.UserAnswersConnector
import generators.ModelGenerators
import models.AlcoholRegime.Beer
import models.adjustment.AdjustmentEntry
import models.adjustment.AdjustmentType.{Overdeclaration, Spoilt, Underdeclaration}
import models.{ABVRange, AlcoholByVolume, AlcoholRegime, AlcoholType, RangeDetailsByRegime, RateBand, RateType, UserAnswers}
import org.mockito.ArgumentMatchers.any
import pages.adjustment._
import play.api.inject.bind
import play.api.test.Helpers._
import uk.gov.hmrc.http.HttpResponse
import viewmodels.checkAnswers.adjustment.CheckYourAnswersSummaryListHelper
import views.html.adjustment.CheckYourAnswersView

import java.time.YearMonth
import scala.concurrent.Future

class CheckYourAnswersControllerSpec extends SpecBase with ModelGenerators {

  val rate              = BigDecimal(9.27)
  val pureAlcoholVolume = BigDecimal(3.69)
  val totalLitresVolume = BigDecimal(3.69)
  val taxCode           = "311"
  val repackagedRate    = BigDecimal(10)
  val repackagedDuty    = BigDecimal(33.2)
  val newDuty           = BigDecimal(1)
  val rateBand          = RateBand(
    "310",
    "some band",
    RateType.DraughtRelief,
    Some(BigDecimal(10.99)),
    Set(
      RangeDetailsByRegime(
        AlcoholRegime.Beer,
        NonEmptySeq.one(
          ABVRange(
            AlcoholType.Beer,
            AlcoholByVolume(0.1),
            AlcoholByVolume(5.8)
          )
        )
      )
    )
  )

  val duty = BigDecimal(100)

  val currentAdjustmentEntry = AdjustmentEntry(
    adjustmentType = Some(Spoilt),
    spoiltRegime = Some(Beer),
    pureAlcoholVolume = Some(pureAlcoholVolume),
    totalLitresVolume = Some(totalLitresVolume),
    rateBand = Some(rateBand),
    period = Some(YearMonth.of(24, 1)),
    duty = Some(duty)
  )

  val savedAdjustmentEntry =
    currentAdjustmentEntry.copy(pureAlcoholVolume = Some(BigDecimal(10)), totalLitresVolume = Some(BigDecimal(11)))

  val repackagedAdjustmentEntry =
    currentAdjustmentEntry.copy(
      adjustmentType = Some(Overdeclaration),
      spoiltRegime = None,
      repackagedRateBand = Some(rateBand),
      repackagedSprDutyRate = Some(rate)
    )

  val repackagedAdjustmentEntryWithSPR =
    currentAdjustmentEntry.copy(
      adjustmentType = Some(Overdeclaration),
      sprDutyRate = Some(rate),
      spoiltRegime = None,
      repackagedRateBand = Some(rateBand),
      repackagedSprDutyRate = Some(rate)
    )

  val underdeclaredAdjustmentEntry =
    currentAdjustmentEntry.copy(spoiltRegime = None, adjustmentType = Some(Underdeclaration), sprDutyRate = Some(rate))

  val completeAdjustmentEntryUserAnswers: UserAnswers = emptyUserAnswers
    .set(CurrentAdjustmentEntryPage, currentAdjustmentEntry)
    .success
    .value
    .set(AdjustmentEntryListPage, Seq(savedAdjustmentEntry))
    .success
    .value

  val pageNumber = 1

  lazy val checkYourAnswersRoute = controllers.adjustment.routes.CheckYourAnswersController.onSubmit().url

  "CheckYourAnswers Controller" - {

    "must return OK and the correct view for a GET if all necessary questions are answered" in {
      val mockUserAnswersConnector = mock[UserAnswersConnector]

      when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])

      val userAnswers1 = emptyUserAnswers
        .set(CurrentAdjustmentEntryPage, repackagedAdjustmentEntry)
        .success
        .value

      val userAnswers2 = emptyUserAnswers
        .set(CurrentAdjustmentEntryPage, currentAdjustmentEntry)
        .success
        .value

      val userAnswers3 = emptyUserAnswers
        .set(CurrentAdjustmentEntryPage, repackagedAdjustmentEntryWithSPR)
        .success
        .value

      val userAnswers4 = emptyUserAnswers
        .set(CurrentAdjustmentEntryPage, underdeclaredAdjustmentEntry)
        .success
        .value

      val completeUserAnswersList = Seq(
        userAnswers1,
        userAnswers2,
        userAnswers3,
        userAnswers4
      )

      completeUserAnswersList.foreach { (completeUserAnswers: UserAnswers) =>
        val application = applicationBuilder(Some(completeUserAnswers))
          .overrides(
            bind[UserAnswersConnector].toInstance(mockUserAnswersConnector)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, checkYourAnswersRoute)

          val result = route(application, request).value

          val view = application.injector.instanceOf[CheckYourAnswersView]

          val list = CheckYourAnswersSummaryListHelper
            .currentAdjustmentEntrySummaryList(completeUserAnswers.get(CurrentAdjustmentEntryPage).get)(
              getMessages(app)
            )
            .get

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(list)(request, getMessages(app)).toString
        }
      }
    }

    "must return OK and load the saved adjustment entry from the user answers if index is defined" in {

      val mockUserAnswersConnector = mock[UserAnswersConnector]

      when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])

      val application = applicationBuilder(userAnswers = Some(completeAdjustmentEntryUserAnswers))
        .overrides(
          bind[UserAnswersConnector].toInstance(mockUserAnswersConnector)
        )
        .build()

      running(application) {
        val request =
          FakeRequest(GET, controllers.adjustment.routes.CheckYourAnswersController.onPageLoad(index = Some(0)).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CheckYourAnswersView]

        val list = CheckYourAnswersSummaryListHelper
          .currentAdjustmentEntrySummaryList(savedAdjustmentEntry)(getMessages(app))
          .get

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(list)(request, getMessages(app)).toString
      }
    }

    "must return OK and load the saved adjustment entry from the user answers if index is defined inside the current adjustment entry" in {
      val mockUserAnswersConnector = mock[UserAnswersConnector]

      when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])

      val userAnswers =
        completeAdjustmentEntryUserAnswers
          .set(CurrentAdjustmentEntryPage, savedAdjustmentEntry.copy(index = Some(0)))
          .success
          .value

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[UserAnswersConnector].toInstance(mockUserAnswersConnector)
        )
        .build()

      running(application) {
        val request =
          FakeRequest(GET, checkYourAnswersRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CheckYourAnswersView]

        val list = CheckYourAnswersSummaryListHelper
          .currentAdjustmentEntrySummaryList(savedAdjustmentEntry)(getMessages(app))
          .get

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(list)(request, getMessages(app)).toString
      }
    }

    "must return OK and the correct view for a GET if any optional questions are not answered" in {

      val mockUserAnswersConnector = mock[UserAnswersConnector]

      when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])

      Seq(
        currentAdjustmentEntry.copy(repackagedRateBand = None),
        currentAdjustmentEntry.copy(repackagedSprDutyRate = None)
      ).foreach { incompleteAdjustmentEntry =>
        val userAnswers = completeAdjustmentEntryUserAnswers
          .set(CurrentAdjustmentEntryPage, incompleteAdjustmentEntry)
          .success
          .value

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[UserAnswersConnector].toInstance(mockUserAnswersConnector)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, checkYourAnswersRoute)

          val result = route(application, request).value

          val view = application.injector.instanceOf[CheckYourAnswersView]

          val list = CheckYourAnswersSummaryListHelper
            .currentAdjustmentEntrySummaryList(incompleteAdjustmentEntry)(getMessages(app))
            .get

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(list)(request, getMessages(app)).toString
        }
      }
    }

    "must return OK and the correct view for a GET if all necessary questions are answered, the TaxType contains a rate and SPR duty relief is absent" in {

      val mockUserAnswersConnector = mock[UserAnswersConnector]

      when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])

      val adjustmentEntry = currentAdjustmentEntry.copy(
        sprDutyRate = None,
        repackagedSprDutyRate = None
      )

      val userAnswers = completeAdjustmentEntryUserAnswers
        .set(CurrentAdjustmentEntryPage, adjustmentEntry)
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[UserAnswersConnector].toInstance(mockUserAnswersConnector)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, checkYourAnswersRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CheckYourAnswersView]

        val list =
          CheckYourAnswersSummaryListHelper
            .currentAdjustmentEntrySummaryList(adjustmentEntry)(getMessages(app))
            .get

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(list)(request, getMessages(app)).toString
      }
    }

    "must redirect to Journey Recovery for a GET" - {

      "if no existing data is found" in {

        val application = applicationBuilder(Some(emptyUserAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, checkYourAnswersRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "if no existing data is found for a given index" in {

        val application = applicationBuilder(userAnswers = Some(completeAdjustmentEntryUserAnswers)).build()

        running(application) {
          val request = FakeRequest(
            GET,
            controllers.adjustment.routes.CheckYourAnswersController.onPageLoad(index = Some(100)).url
          )

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "if all necessary questions are not answered" in {
        val incompleteUserAnswers1 =
          completeAdjustmentEntryUserAnswers
            .set(CurrentAdjustmentEntryPage, currentAdjustmentEntry.copy(rateBand = None, sprDutyRate = None))
            .success
            .value
        val incompleteUserAnswers2 =
          completeAdjustmentEntryUserAnswers
            .set(CurrentAdjustmentEntryPage, currentAdjustmentEntry.copy(adjustmentType = None))
            .success
            .value
        val incompleteUserAnswers3 =
          completeAdjustmentEntryUserAnswers
            .set(CurrentAdjustmentEntryPage, currentAdjustmentEntry.copy(pureAlcoholVolume = None))
            .success
            .value
        val incompleteUserAnswers4 =
          completeAdjustmentEntryUserAnswers
            .set(CurrentAdjustmentEntryPage, currentAdjustmentEntry.copy(totalLitresVolume = None))
            .success
            .value
        val incompleteUserAnswers5 =
          completeAdjustmentEntryUserAnswers
            .set(CurrentAdjustmentEntryPage, currentAdjustmentEntry.copy(duty = None))
            .success
            .value

        val incompleteUserAnswersList = Seq(
          incompleteUserAnswers1,
          incompleteUserAnswers2,
          incompleteUserAnswers3,
          incompleteUserAnswers4,
          incompleteUserAnswers5
        )

        incompleteUserAnswersList.foreach { (incompleteUserAnswers: UserAnswers) =>
          val application = applicationBuilder(Some(incompleteUserAnswers)).build()

          running(application) {
            val request = FakeRequest(GET, checkYourAnswersRoute)

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
          }
        }
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockUserAnswersConnector = mock[UserAnswersConnector]

      when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])

      val application =
        applicationBuilder(userAnswers = Some(completeAdjustmentEntryUserAnswers))
          .overrides(
            bind[UserAnswersConnector].toInstance(mockUserAnswersConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, controllers.adjustment.routes.CheckYourAnswersController.onSubmit().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.adjustment.routes.AdjustmentListController
          .onPageLoad(pageNumber)
          .url

        verify(mockUserAnswersConnector, times(1)).set(any())(any())
      }
    }

    "must redirect to the next page when valid data is submitted and the adjustment entry has an index" in {

      val userAnswers =
        completeAdjustmentEntryUserAnswers
          .set(CurrentAdjustmentEntryPage, savedAdjustmentEntry.copy(index = Some(0)))
          .success
          .value

      val mockUserAnswersConnector = mock[UserAnswersConnector]

      when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[UserAnswersConnector].toInstance(mockUserAnswersConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, controllers.adjustment.routes.CheckYourAnswersController.onSubmit().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.adjustment.routes.AdjustmentListController
          .onPageLoad(pageNumber)
          .url

        verify(mockUserAnswersConnector, times(1)).set(any())(any())
      }
    }

    "must redirect to the Journey Recovery page when uncompleted data is submitted" in {

      val mockUserAnswersConnector = mock[UserAnswersConnector]

      when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])

      val incompleteAdjustmentEntryUserAnswers: UserAnswers = emptyUserAnswers
        .set(CurrentAdjustmentEntryPage, currentAdjustmentEntry.copy(adjustmentType = None))
        .success
        .value

      val application =
        applicationBuilder(userAnswers = Some(incompleteAdjustmentEntryUserAnswers))
          .overrides(
            bind[UserAnswersConnector].toInstance(mockUserAnswersConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, controllers.adjustment.routes.CheckYourAnswersController.onSubmit().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url

        verify(mockUserAnswersConnector, times(0)).set(any())(any())
      }
    }

    "must redirect to the Journey Recovery page when adjustment entry is absent" in {

      val mockUserAnswersConnector = mock[UserAnswersConnector]

      when(mockUserAnswersConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[UserAnswersConnector].toInstance(mockUserAnswersConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, controllers.adjustment.routes.CheckYourAnswersController.onSubmit().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url

        verify(mockUserAnswersConnector, times(0)).set(any())(any())
      }
    }

  }
}
