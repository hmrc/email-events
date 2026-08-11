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

package uk.gov.hmrc.emailevents.service

import org.mockito.ArgumentMatchers.any
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.emailevents.connector.EmailConnector
import uk.gov.hmrc.emailevents.models.controllers.RawEvent
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.libs.json.{ JsValue, Json }
import uk.gov.hmrc.emailevents.models.{ EventIgnored, EventSaved }
import uk.gov.hmrc.emailevents.models.connector.Event

import java.util.UUID
import scala.concurrent.Future
import scala.io.Source

class EventsProcessingSpec extends AnyWordSpec with Matchers with ScalaFutures {

  "Event processing" must {
    "send correct event object to the EmailConnector" in new TestCase {
      when(emailConnectorMock.send(any[Event])).thenReturn(Future.successful(EventSaved("true")))

      val jsonData: JsValue = Json.parse(Source.fromResource("submitted_event.json").mkString)
      val rawEvent: RawEvent = jsonData.as[RawEvent]

      val eventsProcessing = new EventsProcessingImpl(emailConnectorMock)
      eventsProcessing(rawEvent).futureValue mustBe EventSaved("true")
    }

    "return EventIgnored for UnInterested delivery status" in new TestCase {
      val jsonData: JsValue = Json.parse(deliveryInfoNotifWithStatusUninterested)
      val rawEvent: RawEvent = jsonData.as[RawEvent]

      val eventsProcessing = new EventsProcessingImpl(emailConnectorMock)
      eventsProcessing(rawEvent).futureValue mustBe EventIgnored("Event is ignored and its not processed")
    }

    "return UnInterested if event is not in our list of interested events" in new TestCase {
      when(emailConnectorMock.send(any[Event])).thenReturn(Future.successful(EventSaved("true")))

      val jsonData: JsValue = Json.parse(Source.fromResource("invalid_event.json").mkString)
      val rawEvent: RawEvent = jsonData.as[RawEvent]

      val eventsProcessing = new EventsProcessingImpl(emailConnectorMock)
      eventsProcessing(rawEvent).futureValue mustBe EventIgnored("Event is ignored and its not processed")
    }
  }

  class TestCase {
    val emailConnectorMock: EmailConnector = mock[EmailConnector]

    val transId: UUID = UUID.randomUUID()
    val correlationId: UUID = UUID.randomUUID()

    val deliveryInfoNotifWithStatusUninterested: String =
      """{"deliveryInfoNotification":{
        |"deliveryInfo":{"timeStamp":"2022-12-07T14:40:46.886Z",
        |"Description":"Submitted",
        |"code":"7501",
        |"deliveryChannel":"email",
        |"additionalInfo":"",
        |"destination":"test.dc@digital.hmrc.gov.uk",
        |"destinationType":"email",
        |"deliveryStatus":"UnInterested"
        |},
        |"subtid":"",
        |"transid":"4310b3f8-9d89-47a3-9c72-4482f9ef14c9",
        |"callbackData":"eyJuYW1lIjoiZW5jcnlwdGVkU3RyaW5nIiwicmVnaW1lIjoiZW5jcnlwdGVkU3RyaW5nIiwidGVtcGxhdGVJZCI6ImVuY3J5cHRlZFN0cmluZyIsInBsYXRmb3JtIjoiZW5jcnlwdGVkU3RyaW5nIiwiQ29udGFjdFBvbGljeUdyb3VwSWQiOiIifQ==",
        |"correlationid":"4310b3f8-9d89-47a3-9c72-4482f9ef14c9"}
        |}""".stripMargin
  }
}
