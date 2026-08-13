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

package uk.gov.hmrc.emailevents.controllers

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.http.Status.{ ACCEPTED, BAD_GATEWAY, INTERNAL_SERVER_ERROR, OK }
import play.api.http.{ ContentTypes, Status }
import play.api.libs.json.{ JsValue, Json }
import play.api.mvc.Result
import play.api.test.Helpers.{ CONTENT_TYPE, contentAsJson, defaultAwaitTimeout, status }
import play.api.test.{ FakeHeaders, FakeRequest, Helpers }
import uk.gov.hmrc.emailevents.connector.EmailConnector
import uk.gov.hmrc.emailevents.models.*
import uk.gov.hmrc.emailevents.service.EventsProcessingImpl
import uk.gov.hmrc.emailevents.models.connector.Event

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.io.Source

class EventsControllerSpec extends AnyWordSpec with Matchers {

  "POST /events" must {
    "return OK if DeliveryStatus is valid" in new TestSetup {
      when(emailConnectorMock.send(any[Event])).thenReturn(Future.successful(EventSaved("true")))

      val fakeRequest = FakeRequest("POST", "/email-events", fakeHeaders, data)
      val result: Future[Result] = controller.events()(fakeRequest)

      status(result) mustBe OK
      contentAsJson(result) mustBe Json.parse("""{"message":"true"}""")
    }

    "return ACCEPTED if DeliveryStatus is not recognised" in new TestSetup {
      when(emailConnectorMock.send(any[Event])).thenReturn(Future.successful(EventIgnored("unrecognised")))

      val fakeRequest = FakeRequest("POST", "/events", fakeHeaders, invalidData)

      val result: Future[Result] = controller.events()(fakeRequest)
      status(result) mustBe ACCEPTED
      contentAsJson(result) mustBe Json.parse(
        """{"message":"Event with deliveryStatus SomethingElse is ignored and not processed"}"""
      )
    }

    "return ACCEPTED if DeliveryStatus is UnInterested" in new TestSetup {
      val fakeRequest = FakeRequest("POST", "/events", fakeHeaders, Json.parse(unInterestedData))

      val result: Future[Result] = controller.events()(fakeRequest)

      status(result) mustBe ACCEPTED
      contentAsJson(result) mustBe Json.parse(
        """{"message":"Event with deliveryStatus UnInterested is ignored and not processed"}"""
      )
    }

    "return InternalServerError if event fails to save" in new TestSetup {
      when(emailConnectorMock.send(any[Event]))
        .thenReturn(Future.successful(EventSaveFailed(EventSaveError(BAD_GATEWAY, "Bad gateway"))))

      val fakeRequest = FakeRequest("POST", "/events", fakeHeaders, data)

      val result: Future[Result] = controller.events()(fakeRequest)

      status(result) mustBe INTERNAL_SERVER_ERROR
      contentAsJson(result) mustBe Json.parse(
        """{"error":{"statusCode":502,"message":"Bad gateway"}}"""
      )
    }
  }

  "scrapeEmail" must {
    "scrape single email in text" in new TestSetup {
      when(emailConnectorMock.send(any[Event])).thenReturn(Future.successful(EventSaved("true")))

      val text = "already bounced : test@wags.co.uk"
      val scrapedText: String = controller.scrapeEmails(text)

      scrapedText mustBe "already bounced : emailHidden"
    }

    "scrape multiple emails in text" in new TestSetup {
      when(emailConnectorMock.send(any[Event]))
        .thenReturn(Future.successful(EventIgnored("Event is ignored and its not processed")))

      val text: String =
        "already bounced : test@wags.co.uk some infinite text and" +
          " an email appears test@gmail.com and then it appears again lee@willhill.co.uk"
      val scrapedText: String = controller.scrapeEmails(text)

      scrapedText mustBe "already bounced : emailHidden some infinite text and" +
        " an email appears emailHidden and then it appears again emailHidden"
    }
  }

  class TestSetup() {
    val emailConnectorMock: EmailConnector = mock[EmailConnector]
    val eventsProcessing = new EventsProcessingImpl(emailConnectorMock)

    val data: JsValue = Json.parse(Source.fromResource("submitted_event.json").mkString)
    val invalidData: JsValue = Json.parse(Source.fromResource("invalid_event.json").mkString)
    val unInterestedData: String =
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

    val fakeHeaders = FakeHeaders(
      Seq(
        CONTENT_TYPE      -> ContentTypes.JSON,
        "X-Hub-Signature" -> "sha256=561d34d16b5c0f27b91487c05530e734c904b4fed4cdba1a207f214dbaf8373a"
      )
    )

    val controller = new EventsController(Helpers.stubControllerComponents(), eventsProcessing)
  }
}
