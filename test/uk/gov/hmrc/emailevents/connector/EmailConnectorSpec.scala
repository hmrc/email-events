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

package uk.gov.hmrc.emailevents.connector

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.Configuration
import play.api.http.Status
import play.api.http.Status.{ BAD_REQUEST, CREATED }
import uk.gov.hmrc.emailevents.models.connector.Event
import uk.gov.hmrc.emailevents.models.controllers.DeliveryStatus
import uk.gov.hmrc.emailevents.models.{ EventSaveError, EventSaveFailed, EventSaved }
import uk.gov.hmrc.http.client.{ HttpClientV2, RequestBuilder }
import uk.gov.hmrc.http.{ HeaderCarrier, HttpReads, HttpResponse }
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import java.net.URL
import java.time.LocalDateTime
import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ ExecutionContext, Future }

class EmailConnectorSpec extends AnyWordSpec with Matchers with ScalaFutures {
  "EmailConnector" must {
    "return EventSaved when email service returns 201 CREATED" in new TestCase {
      when(requestBuilder.execute(using any[HttpReads[HttpResponse]], any[ExecutionContext]))
        .thenReturn(Future.successful(HttpResponse(CREATED, "saved")))
      emailConnector
        .send(event)
        .futureValue mustBe EventSaved("true")
    }

    "return EventSaveFailed with correct error message when email service returns non-successful status" in new TestCase {
      when(requestBuilder.execute(using any[HttpReads[HttpResponse]], any[ExecutionContext]))
        .thenReturn(Future.successful(HttpResponse(BAD_REQUEST, "")))
      emailConnector.send(event).futureValue mustBe EventSaveFailed(
        EventSaveError(Status.BAD_REQUEST, "Event save failed - status code: 400")
      )
    }

    "return EventSaveFailed with correct error message when there is an exception while making HTTP request" in new TestCase {
      when(requestBuilder.execute(using any[HttpReads[HttpResponse]], any[ExecutionContext]))
        .thenReturn(Future.failed(new Exception("Http request failed")))
      emailConnector.send(event).futureValue mustBe
        EventSaveFailed(
          EventSaveError(Status.INTERNAL_SERVER_ERROR, "HTTP request to email failed with error: Http request failed")
        )
    }
  }

  class TestCase {
    val httpClient: HttpClientV2 = mock[HttpClientV2]
    val servicesConfig: ServicesConfig = new ServicesConfig(
      Configuration(
        "microservice.services.email.host"     -> "some-host",
        "microservice.services.email.port"     -> "1234",
        "microservice.services.email.protocol" -> "http"
      )
    )

    val requestBuilder: RequestBuilder = mock[RequestBuilder]

    when(httpClient.post(any[URL])(any[HeaderCarrier])).thenReturn(requestBuilder)
    when(requestBuilder.withBody(any)(using any, any, any)).thenReturn(requestBuilder)

    val event = Event(
      UUID.randomUUID(),
      UUID.randomUUID(),
      DeliveryStatus.Submitted,
      LocalDateTime.now(),
      "7501",
      "Submitted",
      "additionalInfo",
      "test@gmail.com",
      Map.empty
    )

    val emailConnector = new EmailConnector(servicesConfig, httpClient)
  }
}
