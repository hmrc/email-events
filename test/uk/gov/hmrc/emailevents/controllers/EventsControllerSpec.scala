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

package uk.gov.hmrc.emailevents.controllers

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.http.{ ContentTypes, Status }
import play.api.libs.json.Json
import play.api.test.Helpers.{ CONTENT_TYPE, contentAsJson, defaultAwaitTimeout, status }
import play.api.test.{ FakeHeaders, FakeRequest, Helpers }
import uk.gov.hmrc.emailevents.models.{ EventIgnored, EventSaved }
import uk.gov.hmrc.emailevents.models.controllers.RawEvent
import uk.gov.hmrc.emailevents.service.EventsProcessing
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.io.Source

class EventsControllerSpec extends AnyWordSpec with Matchers {

  "POST /events" must {
    "return OK if DeliveryStatus is valid" in new TestSetup {
      val fakeRequest = FakeRequest("POST", "/email-events", fakeHeaders, data)
      val result = controller.events()(fakeRequest)
      status(result) mustBe Status.OK
      contentAsJson(result) mustBe Json.parse("""{"message":"true"}""")
    }

    "return ACCEPTED if DeliveryStatus is not recognised" in new TestSetup {
      when(eventsProcessingMock.apply(any[RawEvent])).thenReturn(Future.successful(EventIgnored("unrecognised")))
      val fakeRequest = FakeRequest("POST", "/events", fakeHeaders, invalidData)
      val result = controller.events()(fakeRequest)
      status(result) mustBe Status.ACCEPTED
      contentAsJson(result) mustBe Json.parse(
        """{"message":"Event with deliveryStatus SomethingElse is ignored and not processed"}"""
      )
    }
  }

  "scrapeEmail" must {
    "scrape single email in text" in new TestSetup {
      val text = "already bounced : test@wags.co.uk"
      val scrapedText = controller.scrapeEmails(text)

      scrapedText mustBe "already bounced : emailHidden"
    }
    "scrape multiple emails in text" in new TestSetup {
      val text =
        "already bounced : test@wags.co.uk some infinite text and an email appears test@gmail.com and then it appears again lee@willhill.co.uk"
      val scrapedText = controller.scrapeEmails(text)
      scrapedText mustBe "already bounced : emailHidden some infinite text and an email appears emailHidden and then it appears again emailHidden"
    }
  }

  class TestSetup() {
    val eventsProcessingMock = mock[EventsProcessing]
    when(eventsProcessingMock.apply(any[RawEvent])).thenReturn(Future.successful(EventSaved("true")))
    val data = Json.parse(Source.fromResource("submitted_event.json").mkString)
    val invalidData = Json.parse(Source.fromResource("invalid_event.json").mkString)
    val fakeHeaders = FakeHeaders(
      Seq(
        CONTENT_TYPE      -> ContentTypes.JSON,
        "X-Hub-Signature" -> "sha256=561d34d16b5c0f27b91487c05530e734c904b4fed4cdba1a207f214dbaf8373a"
      )
    )

    val controller = new EventsController(Helpers.stubControllerComponents(), eventsProcessingMock)
  }
}
