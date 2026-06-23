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

package uk.gov.hmrc.emailevents.models.connectors

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{ Format, JsValue, Json }
import uk.gov.hmrc.emailevents.models.connector.Event
import uk.gov.hmrc.emailevents.models.controllers.DeliveryStatus

import java.time.LocalDateTime
import java.util.UUID

class EventSpec extends AnyWordSpec with Matchers {
  "Event" must {

    "successfully write to JSON" in new TestCase {
      val json: JsValue = Json.toJson(event)

      (json \ "messageId").as[UUID] mustBe id
      (json \ "status").as[String] mustBe "Delivered"
      (json \ "timeStamp").as[String] mustBe "2025-02-05T10:30:00"
      (json \ "tags" \ "formId").as[String] mustBe "NIRef1"
    }

    "successfully read from JSON" in new TestCase {
      val json = Json.toJson(event)
      json.as[Event] mustBe event
    }
  }

  class TestCase {
    implicit val eventFormat: Format[Event] = Json.format[Event]

    val id: UUID = UUID.randomUUID()

    val event: Event = Event(
      messageId = id,
      correlationId = id,
      status = DeliveryStatus.Delivered,
      timeStamp = LocalDateTime.of(2025, 2, 5, 10, 30, 0),
      code = "NIRef1",
      description = "CA123456A - Application submitted - Reference number: NI123456A.",
      additionalInfo = "additional Info",
      emailAddress = "test@example.com",
      tags = Map("formId" -> "NIRef1")
    )
  }
}
