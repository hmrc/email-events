/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.emailevents.models

import play.api.libs.json.{ Json, OWrites }

sealed trait EventSaveResult
case class EventSaved(message: String) extends EventSaveResult
case class EventIgnored(message: String) extends EventSaveResult
case class EventSaveFailed(error: EventSaveError) extends EventSaveResult

case class EventSaveError(statusCode: Int, message: String)

object EventSaved {
  implicit val writes: OWrites[EventSaved] = Json.writes[EventSaved]
}

object EventIgnored {
  implicit val writes: OWrites[EventIgnored] = Json.writes[EventIgnored]
}

object EventSaveFailed {
  implicit val eventSaveErrorWrites: OWrites[EventSaveError] = Json.writes[EventSaveError]
  implicit val writes: OWrites[EventSaveFailed] = Json.writes[EventSaveFailed]
}
