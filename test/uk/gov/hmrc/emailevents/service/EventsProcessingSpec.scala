/*
 * Copyright 2023 HM Revenue & Customs
 *
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
