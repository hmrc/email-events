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

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{ JsObject, Json }
import play.api.libs.json.Json.JsValueWrapper

import java.time.LocalDateTime

class DeliveryInfoSpec extends AnyWordSpec with Matchers {
  "DeliveryInfo" must {

    "read from Json with correct field mappings" in new TestCase {
      val json: JsObject = validJsonWith()

      val result = json.as[DeliveryInfo]
      result.timeStamp mustBe LocalDateTime.parse("2022-12-07T14:40:46.886")
      result.description mustBe "Submitted"
      result.code mustBe "1234"
      result.deliveryStatus mustBe DeliveryStatus.Submitted
    }

    "map all known delivery statuses correctly" in new TestCase {
      DeliveryStatus.values.foreach { status =>
        val json = validJsonWith("deliveryStatus" -> status.toString)
        json.as[DeliveryInfo].deliveryStatus mustBe status
      }
    }

    "default to UnInterested for unknown delivery status" in new TestCase {
      val json: JsObject = validJsonWith("deliveryStatus" -> "SomeUnknownStatus")
      json.as[DeliveryInfo].deliveryStatus mustBe DeliveryStatus.UnInterested
    }

    "serialize to JSON with camelCase field names" in {
      val info = DeliveryInfo(
        LocalDateTime.parse("2022-12-07T14:40:46.886"),
        "Submitted",
        "7501",
        "email",
        "",
        "test@example.com",
        "email",
        DeliveryStatus.Submitted
      )

      val json = Json.toJson(info)

      (json \ "description").as[String] mustBe "Submitted"
      (json \ "deliveryStatus").as[String] mustBe "Submitted"
    }
  }

  class TestCase {
    def validJsonWith(overrides: (String, JsValueWrapper)*): JsObject =
      Json.obj(
        "timeStamp"       -> "2022-12-07T14:40:46.886",
        "Description"     -> "Submitted",
        "code"            -> "1234",
        "deliveryChannel" -> "email",
        "additionalInfo"  -> "additional Info",
        "destination"     -> "test@example.com",
        "destinationType" -> "email",
        "deliveryStatus"  -> "Submitted"
      ) ++ Json.obj(overrides*)
  }
}
