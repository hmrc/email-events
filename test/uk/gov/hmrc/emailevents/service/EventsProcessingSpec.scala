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

package uk.gov.hmrc.emailevents.service

import org.mockito.ArgumentMatchers.any
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.emailevents.connector.EmailConnector
import uk.gov.hmrc.emailevents.models.controllers.RawEvent
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.libs.json.Json
import uk.gov.hmrc.emailevents.models.{ EventIgnored, EventSaved }
import uk.gov.hmrc.emailevents.models.connector.Event
import java.util.UUID
import scala.concurrent.Future
import scala.io.Source

class EventsProcessingSpec extends AnyWordSpec with Matchers with ScalaFutures {

  "Event processing" must {
    "send correct event object to the EmailConnector" in new TestCase {
      when(emailConnectorMock.send(any[Event])).thenReturn(Future.successful(EventSaved("true")))
      val jsonData = Json.parse(Source.fromResource("submitted_event.json").mkString)
      val rawEvent = jsonData.as[RawEvent]

      val eventsProcessing = new EventsProcessingImpl(emailConnectorMock)
      eventsProcessing(rawEvent).futureValue mustBe EventSaved("true")
    }
    "return UnInterested if event is not in our list of interested events" in new TestCase {
      when(emailConnectorMock.send(any[Event])).thenReturn(Future.successful(EventSaved("true")))
      val jsonData = Json.parse(Source.fromResource("invalid_event.json").mkString)
      val rawEvent = jsonData.as[RawEvent]

      val eventsProcessing = new EventsProcessingImpl(emailConnectorMock)
      eventsProcessing(rawEvent).futureValue mustBe EventIgnored("Event is ignored and its not processed")
    }
  }

  class TestCase {
    val emailConnectorMock = mock[EmailConnector]

    val transId = UUID.randomUUID()
    val correlationId = UUID.randomUUID()
  }
}
