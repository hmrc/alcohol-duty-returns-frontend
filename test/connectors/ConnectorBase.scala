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

package connectors

import base.SpecBase
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, getRequestedFor, postRequestedFor, urlEqualTo}
import com.github.tomakehurst.wiremock.matching.EqualToJsonPattern
import config.FrontendAppConfig
import org.scalatest.concurrent.ScalaFutures
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.http.test.{HttpClientV2Support, WireMockSupport}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

abstract class ConnectorBase extends SpecBase with ScalaFutures with WireMockSupport with HttpClientV2Support {
  protected val endpointConfigurationPath = "microservice.services"
  protected val endpointName: String

  protected trait WireMockHelper {
    private def stripToPath(url: String) =
      if (url.startsWith("http://") || url.startsWith("https://"))
        url.dropWhile(_ != '/').dropWhile(_ == '/').dropWhile(_ != '/')
      else
        url

    private def urlWithParameters(url: String, parameters: Map[String, String]) = {
      val queryParams = parameters.map { case (k, v) => s"$k=$v" }.mkString("&")

      s"${stripToPath(url)}?$queryParams"
    }

    def stubGet(url: String, status: Int, body: String): Unit =
      wireMockServer.stubFor(
        WireMock.get(urlEqualTo(stripToPath(url))).willReturn(aResponse().withStatus(status).withBody(body))
      )

    def stubGetWithParameters(url: String, parameters: Map[String, String], status: Int, body: String): Unit =
      wireMockServer.stubFor(
        WireMock
          .get(urlEqualTo(urlWithParameters(url, parameters)))
          .willReturn(aResponse().withStatus(status).withBody(body))
      )

    def stubPost(url: String, status: Int, requestBody: String, returnBody: String): Unit =
      wireMockServer.stubFor(
        WireMock
          .post(urlEqualTo(stripToPath(url)))
          .withRequestBody(new EqualToJsonPattern(requestBody, true, false))
          .willReturn(aResponse().withStatus(status).withBody(returnBody))
      )

    def verifyGet(url: String): Unit =
      wireMockServer.verify(getRequestedFor(urlEqualTo(stripToPath(url))))

    def verifyGetWithParameters(url: String, parameters: Map[String, String]): Unit =
      wireMockServer.verify(getRequestedFor(urlEqualTo(urlWithParameters(url, parameters))))

    def verifyPost(url: String): Unit =
      wireMockServer.verify(postRequestedFor(urlEqualTo(stripToPath(url))))
  }

  protected class ConnectorFixture extends WireMockHelper {
    private val application =
      new GuiceApplicationBuilder()
        .configure(
          s"$endpointConfigurationPath.$endpointName.host" -> wireMockHost,
          s"$endpointConfigurationPath.$endpointName.port" -> wireMockPort
        )
        .build()

    private val servicesConfig    = new ServicesConfig(application.configuration)
    val config: FrontendAppConfig = new FrontendAppConfig(application.configuration, servicesConfig)

    protected implicit val hc: HeaderCarrier = HeaderCarrier()
  }
}
