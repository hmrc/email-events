/*
 * Copyright 2026 HM Revenue & Customs
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

package uk.gov.hmrc.emailevents

import com.typesafe.config.ConfigFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.{ Application, Configuration }
import play.api.libs.json.{ JsValue, Json }
import uk.gov.hmrc.http.HeaderCarrier
import utils.WireMockSupportProvider
import org.scalatest.OptionValues.*

import java.util.UUID
import com.github.tomakehurst.wiremock.client.WireMock.{ created, equalTo, matchingJsonPath, post, urlPathMatching }
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.http.Status.OK
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers.POST
import play.api.test.Helpers.*
import play.api.test.*
import uk.gov.hmrc.emailevents.models.{ EventIgnored, EventSaved }

import scala.concurrent.Future

class CaptureEventsSpec
    extends AnyWordSpec with Matchers with ScalaFutures with GuiceOneServerPerSuite with WireMockSupportProvider {

  "email-events endpoint" should {

    "store event in event-hub collection in email" when {

      "deliveryStatus is Submitted" in new TestClass {
        wireMockServer.stubFor(
          post(urlPathMatching("/events"))
            .withRequestBody(
              matchingJsonPath("$.status", equalTo("Submitted"))
            )
            .withRequestBody(
              matchingJsonPath("$.description", equalTo("Submitted"))
            )
            .withRequestBody(
              matchingJsonPath("$.emailAddress", equalTo("test.dc@digital.hmrc.gov.uk"))
            )
            .withRequestBody(
              matchingJsonPath("$.code", equalTo("7501"))
            )
            .withRequestBody(
              matchingJsonPath("$.correlationId", equalTo("4310b3f8-9d89-47a3-9c72-4482f9ef14c9"))
            )
            .willReturn(created)
        )

        val emailEventsRequest: FakeRequest[JsValue] =
          FakeRequest(POST, controllers.routes.EventsController.events().url)
            .withBody(dataWithSubmittedStatus)

        val emailEventsResponse: Future[Result] = route(application, emailEventsRequest).value

        status(emailEventsResponse) shouldBe OK
        contentAsString(emailEventsResponse) shouldBe Json.toJson(EventSaved("true")).toString
      }

      "deliveryStatus is Delivered" in new TestClass {
        wireMockServer.stubFor(
          post(urlPathMatching("/events"))
            .withRequestBody(
              matchingJsonPath("$.status", equalTo("Delivered"))
            )
            .withRequestBody(
              matchingJsonPath("$.description", equalTo("Delivered"))
            )
            .withRequestBody(
              matchingJsonPath("$.emailAddress", equalTo("test.dc@digital.hmrc.gov.uk"))
            )
            .withRequestBody(
              matchingJsonPath("$.code", equalTo("7501"))
            )
            .withRequestBody(
              matchingJsonPath("$.correlationId", equalTo("4310b3f8-9d89-47a3-9c72-4482f9ef10ab"))
            )
            .willReturn(created)
        )

        val emailEventsRequest: FakeRequest[JsValue] =
          FakeRequest(POST, controllers.routes.EventsController.events().url)
            .withBody(dataWithDeliveredStatus)

        val emailEventsResponse: Future[Result] = route(application, emailEventsRequest).value

        status(emailEventsResponse) shouldBe OK
        contentAsString(emailEventsResponse) shouldBe Json.toJson(EventSaved("true")).toString
      }

      "deliveryStatus is UnInterested" in new TestClass {
        val emailEventsRequest: FakeRequest[JsValue] =
          FakeRequest(POST, controllers.routes.EventsController.events().url)
            .withBody(dataWithUnInterestedStatus)

        val emailEventsResponse: Future[Result] = route(application, emailEventsRequest).value

        status(emailEventsResponse) shouldBe ACCEPTED
        contentAsString(emailEventsResponse) shouldBe Json
          .toJson(EventIgnored("Event with deliveryStatus UnInterested is ignored and not processed"))
          .toString
      }

      "deliveryStatus is Bounce" in new TestClass {
        wireMockServer.stubFor(
          post(urlPathMatching("/events"))
            .withRequestBody(
              matchingJsonPath("$.status", equalTo("Bounce"))
            )
            .withRequestBody(
              matchingJsonPath("$.description", equalTo("Transient_ContentRejected"))
            )
            .withRequestBody(
              matchingJsonPath("$.emailAddress", equalTo("test.dc@digital.hmrc.gov.uk"))
            )
            .withRequestBody(
              matchingJsonPath("$.code", equalTo("7501"))
            )
            .withRequestBody(
              matchingJsonPath("$.correlationId", equalTo("4310b3f8-9d89-47a3-9c72-4482f9ef16b8"))
            )
            .willReturn(created)
        )

        val emailEventsRequest: FakeRequest[JsValue] =
          FakeRequest(POST, controllers.routes.EventsController.events().url)
            .withBody(dataWithBounceStatus)

        val emailEventsResponse: Future[Result] = route(application, emailEventsRequest).value

        status(emailEventsResponse) shouldBe OK
        contentAsString(emailEventsResponse) shouldBe Json.toJson(EventSaved("true")).toString
      }
    }

  }

  override def config: Configuration = Configuration(
    ConfigFactory.parseString(
      s"""
         |microservice {
         |  services {
         |  email {
         |            host = $wireMockHost
         |            port = $wireMockPort
         |        }
         |  }
         |}
         |""".stripMargin
    )
  )

  class TestClass {
    val transitId: String = UUID.randomUUID().toString
    val callBackData =
      "eyJuYW1lIjoiZW5jcnlwdGVkU3RyaW5nIiwicmVnaW1lIjoiZW5jcnlwdGVkU3RyaW5nIiwidGVtcGxhdGVJZCI6ImVuY3J5cHRlZFN0cmluZyIsInBsYXRmb3JtIjoiZW5jcnlwdGVkU3RyaW5nIiwiQ29udGFjdFBvbGljeUdyb3VwSWQiOiIifQ=="

    val dataWithSubmittedStatus: JsValue = Json.parse(
      s"""{
         |  "deliveryInfoNotification": {
         |    "deliveryInfo": {
         |      "timeStamp": "2022-12-07T14:40:46.886Z",
         |      "Description": "Submitted",
         |      "code": "7501",
         |      "deliveryChannel": "email",
         |      "additionalInfo": "",
         |      "destination": "test.dc@digital.hmrc.gov.uk",
         |      "destinationType": "email",
         |      "deliveryStatus": "Submitted"
         |    },
         |    "subtid": "",
         |    "transid": "$transitId",
         |    "callbackData": "$callBackData",
         |    "correlationid": "4310b3f8-9d89-47a3-9c72-4482f9ef14c9"
         |  }
         |}""".stripMargin
    )

    val dataWithUnInterestedStatus: JsValue = Json.parse(
      s"""{
         |  "deliveryInfoNotification": {
         |    "deliveryInfo": {
         |      "timeStamp": "2022-12-07T14:40:46.886Z",
         |      "Description": "Submitted",
         |      "code": "7501",
         |      "deliveryChannel": "email",
         |      "additionalInfo": "",
         |      "destination": "test.dc@digital.hmrc.gov.uk",
         |      "destinationType": "email",
         |      "deliveryStatus": "UnInterested"
         |    },
         |    "subtid": "",
         |    "transid": "$transitId",
         |    "callbackData": "$callBackData",
         |    "correlationid": "4310b3f8-9d89-47a3-9c72-4482f9ef15e9"
         |  }
         |}""".stripMargin
    )

    val dataWithBounceStatus: JsValue = Json.parse(
      s"""{
         |  "deliveryInfoNotification": {
         |    "deliveryInfo": {
         |      "timeStamp": "2022-12-07T14:40:46.886Z",
         |      "Description": "Transient_ContentRejected",
         |      "code": "7501",
         |      "deliveryChannel": "email",
         |      "additionalInfo": "",
         |      "destination": "test.dc@digital.hmrc.gov.uk",
         |      "destinationType": "email",
         |      "deliveryStatus": "Bounce"
         |    },
         |    "subtid": "",
         |    "transid": "$transitId",
         |    "callbackData": "$callBackData",
         |    "correlationid": "4310b3f8-9d89-47a3-9c72-4482f9ef16b8"
         |  }
         |}""".stripMargin
    )

    val dataWithDeliveredStatus: JsValue = Json.parse(
      s"""{
         |  "deliveryInfoNotification": {
         |    "deliveryInfo": {
         |      "timeStamp": "2022-12-07T14:40:46.886Z",
         |      "Description": "Delivered",
         |      "code": "7501",
         |      "deliveryChannel": "email",
         |      "additionalInfo": "",
         |      "destination": "test.dc@digital.hmrc.gov.uk",
         |      "destinationType": "email",
         |      "deliveryStatus": "Delivered"
         |    },
         |    "subtid": "",
         |    "transid": "$transitId",
         |    "callbackData": "$callBackData",
         |    "correlationid": "4310b3f8-9d89-47a3-9c72-4482f9ef10ab"
         |  }
         |}""".stripMargin
    )

    val application: Application = new GuiceApplicationBuilder()
      .configure(
        "play.filters.csp.nonce.enabled"        -> false,
        "auditing.enabled"                      -> "false",
        "microservice.metrics.graphite.enabled" -> "false",
        "metrics.enabled"                       -> "false"
      )
      .configure(config)
      .build()

    val baseUrl: String = s"http://$wireMockHost:$wireMockPort"

    given headerCarrier: HeaderCarrier = HeaderCarrier()
  }

}
