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
import play.api.libs.json.{ JsError, JsNumber, JsString, Json }

class DeliveryStatusSpec extends AnyWordSpec with Matchers {

  "DeliveryStatus" must {

    "write all statuses to their string representation" in {
      DeliveryStatus.values.foreach { status =>
        Json.toJson(status) mustBe JsString(status.toString)
      }
    }

    "read all valid statuses" in {
      DeliveryStatus.values.foreach { status =>
        JsString(status.toString).as[DeliveryStatus] mustBe status
      }
    }

    "return JsError for unknown string value" in {
      val result = JsString("Unknown").validate[DeliveryStatus]
      result mustBe a[JsError]
    }

    "return JsError for non-string Json numbers" in {
      val result = JsNumber(123).validate[DeliveryStatus]
      result mustBe a[JsError]
    }

    "return JsError for non-string Json objects" in {
      val result = Json.obj("status" -> "Delivered").validate[DeliveryStatus]
      result mustBe a[JsError]
    }
  }
}
