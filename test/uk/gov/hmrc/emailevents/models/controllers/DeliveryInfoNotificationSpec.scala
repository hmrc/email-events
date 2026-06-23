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

package uk.gov.hmrc.emailevents.models.controllers

import com.fasterxml.jackson.core.JsonParseException
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{ JsObject, Json }

import java.time.{ LocalDateTime, Month }
import java.util.{ Base64, UUID };

class DeliveryInfoNotificationSpec extends AnyWordSpec with Matchers {

  "DeliveryInfoNotification" must {

    "Read from Json with Base64 encoded callbackData" in new TestCase {
      val callbackData: Map[String, String] = Map("name" -> "test", "regime" -> "taxRegime")
      val id = UUID.randomUUID()

      val json = Json.obj(
        "deliveryInfo"  -> deliveryInfoJson,
        "subtid"        -> "1234567890",
        "transid"       -> id,
        "callbackData"  -> encodeCallbackData(callbackData),
        "correlationid" -> id
      )

      val result = json.as[DeliveryInfoNotification]

      result.callbackData mustBe callbackData
      result.correlationId mustBe id
      result.transId mustBe id
      result.subtId mustBe "1234567890"
    }

    "Write to Json with callbackData as a plain map" in {
      val notification = DeliveryInfoNotification(
        deliveryInfo = DeliveryInfo(
          timeStamp = LocalDateTime.of(2026, Month.FEBRUARY, 3, 16, 30, 0),
          description = "delivered",
          deliveryChannel = "email",
          code = "200",
          deliveryStatus = DeliveryStatus.Delivered,
          additionalInfo = "none",
          destination = "test@test.com",
          destinationType = "to"
        ),
        subtId = "",
        transId = UUID.randomUUID(),
        callbackData = Map("name" -> "test"),
        correlationId = UUID.randomUUID()
      )

      val json = Json.toJson(notification)
      (json \ "callbackData" \ "name").as[String] mustBe "test"
    }

    "fail when callbackData is not valid Base64 encoded" in new TestCase {
      val json: JsObject = Json.obj(
        "deliveryInfo"  -> deliveryInfoJson,
        "subtid"        -> "",
        "transid"       -> "4310b3f8-9d89-47a3-9c72-4482f9ef14c9",
        "callbackData"  -> "not-valid-base64!!!",
        "correlationid" -> "4310b3f8-9d89-47a3-9c72-4482f9ef14c9"
      )

      an[IllegalArgumentException] mustBe thrownBy(json.as[DeliveryInfoNotification])
    }

    "fail when callbackData is not a valid Json" in new TestCase {
      val invalidJson: String = Base64.getEncoder.encodeToString("not json".getBytes)

      val json = Json.obj(
        "deliveryInfo"  -> deliveryInfoJson,
        "subtid"        -> "",
        "transid"       -> "4310b3f8-9d89-47a3-9c72-4482f9ef14c9",
        "callbackData"  -> invalidJson,
        "correlationid" -> "4310b3f8-9d89-47a3-9c72-4482f9ef14c9"
      )

      a[JsonParseException] mustBe thrownBy(json.as[DeliveryInfoNotification])
    }
  }

  class TestCase {

    def encodeCallbackData(data: Map[String, String]): String =
      Base64.getEncoder.encodeToString(Json.toJson(data).toString.getBytes)

    val deliveryInfoJson: JsObject = Json.obj(
      "timeStamp"       -> "2026-02-03T16:30:00Z",
      "Description"     -> "Description",
      "deliveryChannel" -> "email",
      "code"            -> "200",
      "deliveryStatus"  -> "Delivered",
      "additionalInfo"  -> "none",
      "destination"     -> "test@test.com",
      "destinationType" -> "destinationType"
    )
  }
}
