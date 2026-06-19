/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.emailevents.models

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.Json
import EventSaveFailed.eventSaveErrorWrites

class EventSaveResultSpec extends AnyWordSpec with Matchers {

  "EventSaved" must {
    "write to JSON" in {
      val result = EventSaved("Event stored successfully")
      Json.toJson(result) mustBe Json.obj("message" -> "Event stored successfully")
    }
  }

  "EventIgnored" must {
    "write to JSON" in {
      val result = EventIgnored("Duplicate event ignored")
      Json.toJson(result) mustBe Json.obj("message" -> "Duplicate event ignored")
    }
  }

  "EventSaveFailed" must {
    "write to JSON" in {
      val error = EventSaveError(500, "Internal Server Error")
      val result = EventSaveFailed(error)
      Json.toJson(result) mustBe Json.obj(
        "error" -> Json.obj(
          "statusCode" -> 500,
          "message"    -> "Internal Server Error"
        )
      )
    }
  }

  "EventSaveResult" must {
    "be handled exhaustively" in {
      val results: List[EventSaveResult] = List(
        EventSaved("ok"),
        EventIgnored("ignored"),
        EventSaveFailed(EventSaveError(500, "boom"))
      )

      results.foreach {
        case EventSaved(_)      => succeed
        case EventIgnored(_)    => succeed
        case EventSaveFailed(_) => succeed
      }
    }
  }

  "EventSaveError" must {
    "write to JSON" in {
      val error = EventSaveError(404, "Not Found")
      Json.toJson(error) mustBe Json.obj(
        "statusCode" -> 404,
        "message"    -> "Not Found"
      )
    }
  }
}
