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
import connectors.CacheConnector
import play.api.test.Helpers._
import uk.gov.hmrc.http.HttpResponse
import views.html.adjustment.AdjustmentDutyDueView
import models.{AlcoholByVolume, AlcoholRegime, RateBand, RateType}
import models.adjustment.AdjustmentEntry
import models.adjustment.AdjustmentType.Spoilt
import org.mockito.ArgumentMatchers.any
import play.api.inject.bind
import services.adjustment.AdjustmentEntryService

import java.time.YearMonth
import scala.concurrent.Future

class AdjustmentDutyDueControllerSpec extends SpecBase {
  private lazy val adjustmentDutyDueRoute = controllers.adjustment.routes.AdjustmentDutyDueController.onPageLoad().url

  "AdjustmentDutyDue Controller" - {

    val dutyDue           = BigDecimal(34.2)
    val rate              = BigDecimal(9.27)
    val pureAlcoholVolume = BigDecimal(3.69)
    val taxCode           = "311"
    val volume            = BigDecimal(10)
    val spoilt            = Spoilt.toString
    val rateBand          = RateBand(
      taxCode,
      "some band",
      RateType.DraughtRelief,
      Set(AlcoholRegime.Beer),
      AlcoholByVolume(0.1),
      AlcoholByVolume(5.8),
      Some(rate)
    )

    val adjustmentEntry = AdjustmentEntry(
      pureAlcoholVolume = Some(pureAlcoholVolume),
      totalLitresVolume = Some(volume),
      rateBand = Some(rateBand),
      duty = Some(dutyDue),
      adjustmentType = Some(Spoilt),
      period = Some(YearMonth.of(24, 1))
    )

    val mockCacheConnector = mock[CacheConnector]
    when(mockCacheConnector.set(any())(any())) thenReturn Future.successful(mock[HttpResponse])

    "must return OK and the correct view for a GET" in {

      val adjustmentEntryService = mock[AdjustmentEntryService]
      when(adjustmentEntryService.createAdjustment(any())(any(), any())) thenReturn Future.successful(adjustmentEntry)

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[AdjustmentEntryService].toInstance(adjustmentEntryService),
          bind[CacheConnector].toInstance(mockCacheConnector)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, adjustmentDutyDueRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AdjustmentDutyDueView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(spoilt, volume, dutyDue, pureAlcoholVolume, taxCode, rate)(
          request,
          messages(application)
        ).toString
      }
    }
    "must redirect to Journey Recovery if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(GET, adjustmentDutyDueRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    val incompleteAdjustmentEntries = List(
      (adjustmentEntry.copy(totalLitresVolume = None), "totalLitres"),
      (adjustmentEntry.copy(pureAlcoholVolume = None), "pureAlcoholVolume"),
      (adjustmentEntry.copy(rateBand = None, sprDutyRate = None), "rate"),
      (adjustmentEntry.copy(duty = None), "duty")
    )

    incompleteAdjustmentEntries.foreach { case (adjustmentEntry, msg) =>
      s"must redirect to Journey Recovery for a GET if adjustment entry does not contain $msg" in {

        val adjustmentEntryService = mock[AdjustmentEntryService]
        when(adjustmentEntryService.createAdjustment(any())(any(), any())) thenReturn Future.successful(adjustmentEntry)

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .overrides(
              bind[AdjustmentEntryService].toInstance(adjustmentEntryService),
              bind[CacheConnector].toInstance(mockCacheConnector)
            )
            .build()

        running(application) {
          val request = FakeRequest(GET, adjustmentDutyDueRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }
  }
}
