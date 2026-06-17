/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.emailevents.models.controllers

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.Json
import uk.gov.hmrc.emailevents.models.controllers.DeliveryStatus.{ Bounce, Complained, Delivered, Failed, Read, Submitted }
import java.time.LocalDateTime
import java.util.UUID
import scala.io.Source

class RawEventTest extends AnyWordSpec with Matchers {
  "event data" must {
    "deserialize submitted event" in {
      val jsonData = Json.parse(Source.fromResource("submitted_event.json").mkString)
      val model = jsonData.as[RawEvent]
      model mustBe RawEvent(
        DeliveryInfoNotification(
          DeliveryInfo(
            LocalDateTime.parse("2022-12-07T14:40:46.886"),
            "Submitted",
            "7501",
            "email",
            "",
            "test.dc@digital.hmrc.gov.uk",
            "email",
            Submitted
          ),
          "",
          UUID.fromString("4310b3f8-9d89-47a3-9c72-4482f9ef14c9"),
          Map(
            "name"                 -> "encryptedString",
            "regime"               -> "encryptedString",
            "templateId"           -> "encryptedString",
            "platform"             -> "encryptedString",
            "ContactPolicyGroupId" -> ""
          ),
          UUID.fromString("4310b3f8-9d89-47a3-9c72-4482f9ef14c9")
        )
      )
    }
    "deserialize read event" in {
      val jsonData = Json.parse(Source.fromResource("read_event.json").mkString)
      val model = jsonData.as[RawEvent]
      model mustBe RawEvent(
        DeliveryInfoNotification(
          DeliveryInfo(
            LocalDateTime.parse("2022-12-07T14:40:46.886"),
            "Read",
            "7501",
            "email",
            "",
            "test.dc@digital.hmrc.gov.uk",
            "email",
            Read
          ),
          "",
          UUID.fromString("4310b3f8-9d89-47a3-9c72-4482f9ef14c9"),
          Map(
            "name"                 -> "encryptedString",
            "regime"               -> "encryptedString",
            "templateId"           -> "encryptedString",
            "platform"             -> "encryptedString",
            "ContactPolicyGroupId" -> ""
          ),
          UUID.fromString("4310b3f8-9d89-47a3-9c72-4482f9ef14c9")
        )
      )
    }
    "deserialize delivered event" in {
      val jsonData = Json.parse(Source.fromResource("delivered_event.json").mkString)
      val model = jsonData.as[RawEvent]
      model mustBe RawEvent(
        DeliveryInfoNotification(
          DeliveryInfo(
            LocalDateTime.parse("2022-12-07T14:40:46.886"),
            "Delivered",
            "7501",
            "email",
            "",
            "test.dc@digital.hmrc.gov.uk",
            "email",
            Delivered
          ),
          "",
          UUID.fromString("4310b3f8-9d89-47a3-9c72-4482f9ef14c9"),
          Map(
            "name"                 -> "encryptedString",
            "regime"               -> "encryptedString",
            "templateId"           -> "encryptedString",
            "platform"             -> "encryptedString",
            "ContactPolicyGroupId" -> ""
          ),
          UUID.fromString("4310b3f8-9d89-47a3-9c72-4482f9ef14c9")
        )
      )
    }
    "deserialize bounced event" in {
      val jsonData = Json.parse(Source.fromResource("bounced_event.json").mkString)
      val model = jsonData.as[RawEvent]
      model mustBe RawEvent(
        DeliveryInfoNotification(
          DeliveryInfo(
            LocalDateTime.parse("2022-12-07T14:40:46.886"),
            "Transient_ContentRejected",
            "7520",
            "email",
            "",
            "test.dc@digital.hmrc.gov.uk",
            "email",
            Bounce
          ),
          "",
          UUID.fromString("4310b3f8-9d89-47a3-9c72-4482f9ef14c9"),
          Map(
            "name"                 -> "encryptedString",
            "regime"               -> "encryptedString",
            "templateId"           -> "encryptedString",
            "platform"             -> "encryptedString",
            "ContactPolicyGroupId" -> ""
          ),
          UUID.fromString("4310b3f8-9d89-47a3-9c72-4482f9ef14c9")
        )
      )
    }
    "deserialize delayed_bounced event" in {
      val jsonData = Json.parse(Source.fromResource("delayed_bounced_event.json").mkString)
      val model = jsonData.as[RawEvent]
      model mustBe RawEvent(
        DeliveryInfoNotification(
          DeliveryInfo(
            LocalDateTime.parse("2022-12-07T14:40:46.886"),
            "Transient_General",
            "7520",
            "email",
            "",
            "test.dc@digital.hmrc.gov.uk",
            "email",
            Bounce
          ),
          "",
          UUID.fromString("4310b3f8-9d89-47a3-9c72-4482f9ef14c9"),
          Map(
            "name"                 -> "encryptedString",
            "regime"               -> "encryptedString",
            "templateId"           -> "encryptedString",
            "platform"             -> "encryptedString",
            "ContactPolicyGroupId" -> ""
          ),
          UUID.fromString("4310b3f8-9d89-47a3-9c72-4482f9ef14c9")
        )
      )
    }

    "deserialize complained event" in {
      val jsonData = Json.parse(Source.fromResource("complained_event.json").mkString)
      val model = jsonData.as[RawEvent]
      model mustBe RawEvent(
        DeliveryInfoNotification(
          DeliveryInfo(
            LocalDateTime.parse("2022-12-07T14:40:46.886"),
            "Complained",
            "7521",
            "email",
            "",
            "test.dc@digital.hmrc.gov.uk",
            "email",
            Complained
          ),
          "",
          UUID.fromString("4310b3f8-9d89-47a3-9c72-4482f9ef14c9"),
          Map(
            "name"                 -> "encryptedString",
            "regime"               -> "encryptedString",
            "templateId"           -> "encryptedString",
            "platform"             -> "encryptedString",
            "ContactPolicyGroupId" -> ""
          ),
          UUID.fromString("4310b3f8-9d89-47a3-9c72-4482f9ef14c9")
        )
      )
    }

    "deserialize failed event" in {
      val jsonData = Json.parse(Source.fromResource("failed_event.json").mkString)
      val model = jsonData.as[RawEvent]
      model mustBe RawEvent(
        DeliveryInfoNotification(
          DeliveryInfo(
            LocalDateTime.parse("2023-05-11T17:04:58.156"),
            "Recipient has not consented to message",
            "9002",
            "email",
            "",
            "test.dc@digital.hmrc.gov.uk",
            "email",
            Failed
          ),
          "",
          UUID.fromString("ceb2adf2-d617-4d32-9574-4fcdb34b4584"),
          Map(
            "name"                 -> "encryptedString",
            "regime"               -> "encryptedString",
            "templateId"           -> "encryptedString",
            "platform"             -> "encryptedString",
            "ContactPolicyGroupId" -> ""
          ),
          UUID.fromString("b1c9c3be-41c7-4d0b-ab9e-68de1e636b1e")
        )
      )
    }
  }
}
