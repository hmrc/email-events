/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.emailevents.models.controllers

import play.api.libs.functional.syntax.*
import play.api.libs.json.*

import java.time.LocalDateTime
case class DeliveryInfo(
  timeStamp: LocalDateTime,
  description: String,
  code: String,
  deliveryChannel: String,
  additionalInfo: String,
  destination: String,
  destinationType: String,
  deliveryStatus: DeliveryStatus
)

object DeliveryInfo {
  implicit val formatReads: Reads[DeliveryInfo] = (
    (__ \ "timeStamp").read[LocalDateTime] and
      (__ \ "Description")
        .read[String] and
      (__ \ "code").read[String] and
      (__ \ "deliveryChannel").read[String] and
      (__ \ "additionalInfo").read[String] and
      (__ \ "destination").read[String] and
      (__ \ "destinationType").read[String] and
      (__ \ "deliveryStatus")
        .read[String]
        .map(status => DeliveryStatus.format.reads(JsString(status)).getOrElse(DeliveryStatus.UnInterested))
  )(DeliveryInfo.apply _)

  implicit val formatWrites: OWrites[DeliveryInfo] = Json.writes[DeliveryInfo]
}
