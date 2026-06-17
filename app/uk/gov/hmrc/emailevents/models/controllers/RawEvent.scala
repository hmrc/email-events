/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.emailevents.models.controllers

import play.api.libs.json.{ Format, Json }

case class RawEvent(deliveryInfoNotification: DeliveryInfoNotification)

object RawEvent {
  implicit val format: Format[RawEvent] = Json.format[RawEvent]
}
