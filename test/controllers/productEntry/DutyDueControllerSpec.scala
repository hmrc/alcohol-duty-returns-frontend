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

package controllers.productEntry

import base.SpecBase
import generators.ModelGenerators
import models.{AlcoholByVolume, UserAnswers}
import models.productEntry.ProductEntry
import pages.productEntry.CurrentProductEntryPage
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.productEntry.DutyDueView

class DutyDueControllerSpec extends SpecBase with ModelGenerators {

  lazy val dutyDueRoute = controllers.productEntry.routes.DutyDueController.onPageLoad().url

  "DutyDue Controller" - {

    "must return OK and the correct view for a GET" in {

      val dutyDue           = BigDecimal(34.2)
      val rate              = BigDecimal(9.27)
      val pureAlcoholVolume = BigDecimal(3.69)
      val taxCode           = "311"

      val productEntry = ProductEntry(
        name = Some("Name"),
        abv = Some(AlcoholByVolume(1)),
        volume = Some(BigDecimal(1)),
        draughtRelief = Some(false),
        smallProducerRelief = Some(false),
        taxRate = Some(rate),
        pureAlcoholVolume = Some(pureAlcoholVolume),
        duty = Some(dutyDue),
        taxCode = Some(taxCode)
      )

      val userAnswers = UserAnswers(userAnswersId).set(CurrentProductEntryPage, productEntry).success.value
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, dutyDueRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[DutyDueView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(dutyDue, pureAlcoholVolume, taxCode, rate)(
          request,
          messages(application)
        ).toString
      }
    }
    "must redirect to Journey Recovery if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(GET, dutyDueRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }

  val productEntry: ProductEntry = arbitraryProductEntry.arbitrary.sample.get
  Seq(
    (productEntry.copy(taxRate = None, sprDutyRate = None), "rate"),
    (productEntry.copy(duty = None), "duty"),
    (productEntry.copy(pureAlcoholVolume = None), "pure alcohol volume")
  ).foreach { test =>
    val (productEntry, field) = test
    s"must redirect to Journey Recovery if product entry does not contain $field" in {

      val userAnswers = UserAnswers(userAnswersId).set(CurrentProductEntryPage, productEntry).success.value
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request =
          FakeRequest(GET, dutyDueRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
