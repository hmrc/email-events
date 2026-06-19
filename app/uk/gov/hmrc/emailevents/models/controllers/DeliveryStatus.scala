/*
 * Copyright 2023 HM Revenue & Customs
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
