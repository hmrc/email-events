/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.emailevents.models.controllers

import play.api.libs.json.*

enum DeliveryStatus {
  case Submitted
  case Read
  case Delivered
  case Bounce
  case Failed
  case Complained
  case UnInterested
}

object DeliveryStatus {
  implicit val format: Format[DeliveryStatus] = new Format[DeliveryStatus] {
    override def writes(o: DeliveryStatus): JsString = JsString(o.toString)
    override def reads(json: JsValue): JsResult[DeliveryStatus] = json match {
      case JsString("Submitted")    => JsSuccess(DeliveryStatus.Submitted)
      case JsString("Read")         => JsSuccess(DeliveryStatus.Read)
      case JsString("Delivered")    => JsSuccess(DeliveryStatus.Delivered)
      case JsString("Bounce")       => JsSuccess(DeliveryStatus.Bounce)
      case JsString("Failed")       => JsSuccess(DeliveryStatus.Failed)
      case JsString("Complained")   => JsSuccess(DeliveryStatus.Complained)
      case JsString("UnInterested") => JsSuccess(DeliveryStatus.UnInterested)
      case _                        => JsError("Invalid DeliveryStatus")
    }
  }
}
