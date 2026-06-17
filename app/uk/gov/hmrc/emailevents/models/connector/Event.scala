/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.emailevents.models.connector

import play.api.libs.json.{ Json, OWrites }
import uk.gov.hmrc.emailevents.models.controllers.DeliveryStatus
import java.time.LocalDateTime
import java.util.UUID

case class Event(
  messageId: UUID,
  correlationId: UUID,
  status: DeliveryStatus,
  timeStamp: LocalDateTime,
  code: String,
  description: String,
  additionalInfo: String,
  emailAddress: String,
  tags: Map[String, String]
)

object Event {
  implicit val formatWrites: OWrites[Event] = Json.writes[Event]
}
