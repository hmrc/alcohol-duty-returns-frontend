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

package controllers.payments

import base.SpecBase
import config.Constants.ukTimeZoneStringId
import config.FrontendAppConfig
import connectors.AlcoholDutyAccountConnector
import org.mockito.ArgumentMatchers.any
import play.api.Configuration
import play.api.inject.bind
import play.api.test.Helpers._
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import viewmodels.payments.ViewPastPaymentsHelper
import viewmodels.payments.ViewPastPaymentsViewModel
import views.html.payments.ViewPastPaymentsView

import java.time.{Clock, LocalDate, ZoneId}
import scala.concurrent.Future

class ViewPastPaymentsControllerSpec extends SpecBase {

  val instant   = LocalDate.of(2025, 8, 22).atStartOfDay(ZoneId.of(ukTimeZoneStringId)).toInstant
  val clock2025 = Clock.fixed(instant, ZoneId.of(ukTimeZoneStringId))

  "ViewPastPayments Controller" - {
    "must return OK and the correct view for a GET when the claim refund gform feature toggle is disabled" in {
      val testConfiguration  = app.injector.instanceOf[Configuration]
      val testServicesConfig = app.injector.instanceOf[ServicesConfig]

      val testAppConfig = new FrontendAppConfig(testConfiguration, testServicesConfig) {
        override val claimARefundGformEnabled = false
      }

      val viewModelHelper                  = new ViewPastPaymentsHelper(createDateTimeHelper(), testAppConfig, clock2025)
      val mockAlcoholDutyAccountsConnector = mock[AlcoholDutyAccountConnector]
      when(mockAlcoholDutyAccountsConnector.outstandingPayments(any())(any())) thenReturn Future.successful(
        openPaymentsData
      )
      when(mockAlcoholDutyAccountsConnector.historicPayments(any())(any())) thenReturn Future.successful(
        historicPaymentsData
      )

      val application = applicationBuilder(userAnswers = None)
        .overrides(
          bind[AlcoholDutyAccountConnector].toInstance(mockAlcoholDutyAccountsConnector),
          bind[FrontendAppConfig].toInstance(testAppConfig),
          bind[Clock].toInstance(clock2025)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.payments.routes.ViewPastPaymentsController.onPageLoad.url)
        val result  = route(application, request).value

        val view                          = application.injector.instanceOf[ViewPastPaymentsView]
        val sortedOutstandingPaymentsData =
          openPaymentsData.outstandingPayments.sortBy(_.dueDate)(Ordering[LocalDate].reverse)
        val outstandingPaymentsTable      =
          viewModelHelper.getOutstandingPaymentsTable(sortedOutstandingPaymentsData)(getMessages(application))
        val unallocatedPaymentsTable      =
          viewModelHelper.getUnallocatedPaymentsTable(openPaymentsData.unallocatedPayments)(getMessages(application))
        val historicPaymentsTable         =
          viewModelHelper.getHistoricPaymentsTable(historicPayments2025.payments)(getMessages(application))
        val viewModel                     = ViewPastPaymentsViewModel(
          openPaymentsData.totalOpenPaymentsAmount,
          2025,
          Seq(2024, 2022),
          claimARefundGformEnabled = false
        )

        status(result)          mustEqual OK
        contentAsString(result) mustEqual view(
          outstandingPaymentsTable,
          unallocatedPaymentsTable,
          historicPaymentsTable,
          viewModel
        )(
          request,
          getMessages(application)
        ).toString
      }
    }

    "must return OK and the correct view for a GET when the claim refund gform feature toggle is enabled" - {
      "and there are payments from past years" in {
        val testConfiguration  = app.injector.instanceOf[Configuration]
        val testServicesConfig = app.injector.instanceOf[ServicesConfig]

        val testAppConfig = new FrontendAppConfig(testConfiguration, testServicesConfig) {
          override val claimARefundGformEnabled = true
        }

        val viewModelHelper                  = new ViewPastPaymentsHelper(createDateTimeHelper(), testAppConfig, clock2025)
        val mockAlcoholDutyAccountsConnector = mock[AlcoholDutyAccountConnector]
        when(mockAlcoholDutyAccountsConnector.outstandingPayments(any())(any())) thenReturn Future.successful(
          openPaymentsData
        )
        when(mockAlcoholDutyAccountsConnector.historicPayments(any())(any())) thenReturn Future.successful(
          historicPaymentsData
        )

        val application = applicationBuilder(userAnswers = None)
          .overrides(
            bind[AlcoholDutyAccountConnector].toInstance(mockAlcoholDutyAccountsConnector),
            bind[Clock].toInstance(clock2025)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.payments.routes.ViewPastPaymentsController.onPageLoad.url)
          val result  = route(application, request).value

          val view                          = application.injector.instanceOf[ViewPastPaymentsView]
          val sortedOutstandingPaymentsData =
            openPaymentsData.outstandingPayments.sortBy(_.dueDate)(Ordering[LocalDate].reverse)
          val outstandingPaymentsTable      =
            viewModelHelper.getOutstandingPaymentsTable(sortedOutstandingPaymentsData)(getMessages(application))
          val unallocatedPaymentsTable      =
            viewModelHelper.getUnallocatedPaymentsTable(openPaymentsData.unallocatedPayments)(getMessages(application))
          val historicPaymentsTable         =
            viewModelHelper.getHistoricPaymentsTable(historicPayments2025.payments)(getMessages(application))
          val viewModel                     = ViewPastPaymentsViewModel(
            openPaymentsData.totalOpenPaymentsAmount,
            2025,
            Seq(2024, 2022),
            claimARefundGformEnabled = true
          )

          status(result)          mustEqual OK
          contentAsString(result) mustEqual view(
            outstandingPaymentsTable,
            unallocatedPaymentsTable,
            historicPaymentsTable,
            viewModel
          )(request, getMessages(application)).toString
        }
      }

      "and there are no payments from past years" in {
        val testConfiguration  = app.injector.instanceOf[Configuration]
        val testServicesConfig = app.injector.instanceOf[ServicesConfig]

        val testAppConfig = new FrontendAppConfig(testConfiguration, testServicesConfig) {
          override val claimARefundGformEnabled = true
        }

        val viewModelHelper                  = new ViewPastPaymentsHelper(createDateTimeHelper(), testAppConfig, clock2025)
        val mockAlcoholDutyAccountsConnector = mock[AlcoholDutyAccountConnector]
        when(mockAlcoholDutyAccountsConnector.outstandingPayments(any())(any())) thenReturn Future.successful(
          openPaymentsData
        )
        when(mockAlcoholDutyAccountsConnector.historicPayments(any())(any())) thenReturn Future.successful(
          historicPaymentsData2
        )

        val application = applicationBuilder(userAnswers = None)
          .overrides(
            bind[AlcoholDutyAccountConnector].toInstance(mockAlcoholDutyAccountsConnector),
            bind[Clock].toInstance(clock2025)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.payments.routes.ViewPastPaymentsController.onPageLoad.url)
          val result  = route(application, request).value

          val view                          = application.injector.instanceOf[ViewPastPaymentsView]
          val sortedOutstandingPaymentsData =
            openPaymentsData.outstandingPayments.sortBy(_.dueDate)(Ordering[LocalDate].reverse)
          val outstandingPaymentsTable      =
            viewModelHelper.getOutstandingPaymentsTable(sortedOutstandingPaymentsData)(getMessages(application))
          val unallocatedPaymentsTable      =
            viewModelHelper.getUnallocatedPaymentsTable(openPaymentsData.unallocatedPayments)(getMessages(application))
          val historicPaymentsTable         =
            viewModelHelper.getHistoricPaymentsTable(historicPayments2025.payments)(getMessages(application))
          val viewModel                     = ViewPastPaymentsViewModel(
            openPaymentsData.totalOpenPaymentsAmount,
            2025,
            Seq.empty,
            claimARefundGformEnabled = true
          )

          status(result)          mustEqual OK
          contentAsString(result) mustEqual view(
            outstandingPaymentsTable,
            unallocatedPaymentsTable,
            historicPaymentsTable,
            viewModel
          )(request, getMessages(application)).toString
        }
      }
    }

    "must redirect to Journey Recovery if there is an Exception due to the historic payments list being absent for the current year" in {
      val testConfiguration  = app.injector.instanceOf[Configuration]
      val testServicesConfig = app.injector.instanceOf[ServicesConfig]

      val testAppConfig = new FrontendAppConfig(testConfiguration, testServicesConfig) {
        override val claimARefundGformEnabled = true
      }

      val mockAlcoholDutyAccountsConnector = mock[AlcoholDutyAccountConnector]
      when(mockAlcoholDutyAccountsConnector.outstandingPayments(any())(any())) thenReturn Future.successful(
        openPaymentsData
      )
      when(mockAlcoholDutyAccountsConnector.historicPayments(any())(any())) thenReturn Future.successful(
        Seq(historicPayments2024)
      )

      val application = applicationBuilder(userAnswers = None)
        .overrides(
          bind[AlcoholDutyAccountConnector].toInstance(mockAlcoholDutyAccountsConnector),
          bind[FrontendAppConfig].toInstance(testAppConfig),
          bind[Clock].toInstance(clock2025)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.payments.routes.ViewPastPaymentsController.onPageLoad.url)
        val result  = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery if there is an Exception due to a failed Future" in {
      val mockAlcoholDutyAccountsConnector = mock[AlcoholDutyAccountConnector]
      when(mockAlcoholDutyAccountsConnector.outstandingPayments(any())(any())) thenReturn Future.failed(
        new Exception("test Exception")
      )
      when(mockAlcoholDutyAccountsConnector.historicPayments(any())(any())) thenReturn Future.successful(
        historicPaymentsData
      )

      val application = applicationBuilder(userAnswers = None)
        .overrides(
          bind[AlcoholDutyAccountConnector].toInstance(mockAlcoholDutyAccountsConnector)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.payments.routes.ViewPastPaymentsController.onPageLoad.url)
        val result  = route(application, request).value

        status(result)                 mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
