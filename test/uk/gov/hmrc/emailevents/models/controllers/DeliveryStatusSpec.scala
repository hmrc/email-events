/*
 * Copyright 2026 HM Revenue & Customs
 *
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
