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
import connectors.{AlcoholDutyCalculatorConnector, UserAnswersConnector}
import forms.adjustment.AdjustmentRepackagedTaxTypeFormProvider
import models.adjustment.AdjustmentEntry
import models.adjustment.AdjustmentType.{RepackagedDraughtProducts, Spoilt}
import models.{ABVRange, AlcoholByVolume, AlcoholRegime, AlcoholType, NormalMode, RangeDetailsByRegime, RateBand, RateType, UserAnswers}
import navigation.AdjustmentNavigator
import org.mockito.ArgumentMatchers.any
import pages.adjustment.CurrentAdjustmentEntryPage
import play.api.data.Form
import play.api.inject.bind
import play.api.test.Helpers._
import play.twirl.api.Html
import views.html.adjustment.AdjustmentRepackagedTaxTypeView

import java.time.YearMonth

class AdjustmentRepackagedTaxTypeControllerSpec extends SpecBase {

  val formProvider                              = new AdjustmentRepackagedTaxTypeFormProvider()
  val form: Form[Int]                           = formProvider()
  val content: Html                             = Html("blah")
  val mockView: AdjustmentRepackagedTaxTypeView = mock[AdjustmentRepackagedTaxTypeView]
  when(mockView.apply(any(), any(), any(), any())(any(), any())).thenReturn(content)

  val period: YearMonth                = YearMonth.of(2024, 1)
  val adjustmentEntry: AdjustmentEntry =
    AdjustmentEntry(adjustmentType = Some(RepackagedDraughtProducts), period = Some(period))
  val userAnswers: UserAnswers         = emptyUserAnswers.set(CurrentAdjustmentEntryPage, adjustmentEntry).success.value

  val validAnswer = 361

  val rate: Option[BigDecimal] = Some(BigDecimal(10.99))
  val rateBand: RateBand       = RateBand(
    "310",
    "some band",
    RateType.DraughtRelief,
    rate,
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

  lazy val adjustmentRepackagedTaxTypeRoute: String                           =
    controllers.adjustment.routes.AdjustmentRepackagedTaxTypeController.onPageLoad(NormalMode).url
  lazy val mockAlcoholDutyCalculatorConnector: AlcoholDutyCalculatorConnector = mock[AlcoholDutyCalculatorConnector]
  lazy val mockUserAnswersConnector: UserAnswersConnector                     = mock[UserAnswersConnector]
  lazy val mockAdjustmentNavigator: AdjustmentNavigator                       = mock[AdjustmentNavigator]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockAlcoholDutyCalculatorConnector)
    reset(mockUserAnswersConnector)
    reset(mockAdjustmentNavigator)
  }

  "AdjustmentRepackagedTaxType Controller" - {

    "must populate the view correctly on a GET" in {
      val adjustmentEntry = AdjustmentEntry(
        adjustmentType = Some(RepackagedDraughtProducts),
        rateBand = fullRepackageAdjustmentEntry.rateBand,
        repackagedRateBand = fullRepackageAdjustmentEntry.rateBand
      )

      val previousUserAnswers = userAnswers.set(CurrentAdjustmentEntryPage, adjustmentEntry).success.value

      val application = applicationBuilder(userAnswers = Some(previousUserAnswers))
        .overrides(
          bind[AdjustmentRepackagedTaxTypeView].toInstance(mockView)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, adjustmentRepackagedTaxTypeRoute)
        val result  = route(application, request).value
        status(result)          mustEqual OK
        contentAsString(result) mustEqual content.body
      }
    }

    "must redirect to Journey Recovery for a GET if repackaged rate band is incomplete" in {
      val adjustmentEntry = AdjustmentEntry(
        adjustmentType = Some(RepackagedDraughtProducts),
        rateBand = fullRepackageAdjustmentEntry.rateBand
      )

      val previousUserAnswers = userAnswers.set(CurrentAdjustmentEntryPage, adjustmentEntry).success.value

      val application = applicationBuilder(userAnswers = Some(previousUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, adjustmentRepackagedTaxTypeRoute)

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a GET if not repackaged journey" in {
      val adjustmentEntry = AdjustmentEntry(
        adjustmentType = Some(Spoilt),
        rateBand = Some(rateBand)
      )

      val previousUserAnswers = userAnswers.set(CurrentAdjustmentEntryPage, adjustmentEntry).success.value

      val application = applicationBuilder(userAnswers = Some(previousUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, adjustmentRepackagedTaxTypeRoute)

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to the next page when the user accepts the repackaged rate" in {
      val adjustmentEntry = AdjustmentEntry(
        adjustmentType = Some(RepackagedDraughtProducts),
        rateBand = fullRepackageAdjustmentEntry.rateBand,
        repackagedRateBand = fullRepackageAdjustmentEntry.rateBand
      )

      val previousUserAnswers = userAnswers.set(CurrentAdjustmentEntryPage, adjustmentEntry).success.value

      val application = applicationBuilder(userAnswers = Some(previousUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, adjustmentRepackagedTaxTypeRoute)
            .withFormUrlEncodedBody(("new-tax-type-code", validAnswer.toString))

        val result = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.adjustment.routes.AdjustmentVolumeController
          .onPageLoad(NormalMode)
          .url
      }
    }
  }
}
