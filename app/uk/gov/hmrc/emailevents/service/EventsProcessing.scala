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

package uk.gov.hmrc.emailevents.service

import play.api.Logging
import uk.gov.hmrc.emailevents.connector.EmailConnector
import uk.gov.hmrc.emailevents.models.{ EventIgnored, EventSaveResult }
import uk.gov.hmrc.emailevents.models.connector.Event
import uk.gov.hmrc.emailevents.models.controllers.DeliveryStatus.UnInterested
import uk.gov.hmrc.emailevents.models.controllers.RawEvent

import javax.inject.{ Inject, Singleton }
import scala.concurrent.Future

trait EventsProcessing {
  def apply(rawEvent: RawEvent): Future[EventSaveResult]
}

@Singleton
class EventsProcessingImpl @Inject() (emailConnector: EmailConnector) extends EventsProcessing with Logging {
  override def apply(rawEvent: RawEvent): Future[EventSaveResult] =
    rawEvent.deliveryInfoNotification.deliveryInfo.deliveryStatus match {
      case UnInterested =>
        Future.successful(EventIgnored(s"Event is ignored and its not processed"))
      case _ =>
        emailConnector.send(
          Event(
            rawEvent.deliveryInfoNotification.transId,
            rawEvent.deliveryInfoNotification.correlationId,
            rawEvent.deliveryInfoNotification.deliveryInfo.deliveryStatus,
            rawEvent.deliveryInfoNotification.deliveryInfo.timeStamp,
            rawEvent.deliveryInfoNotification.deliveryInfo.code,
            rawEvent.deliveryInfoNotification.deliveryInfo.description,
            rawEvent.deliveryInfoNotification.deliveryInfo.additionalInfo,
            rawEvent.deliveryInfoNotification.deliveryInfo.destination,
            rawEvent.deliveryInfoNotification.callbackData
          )
        )
    }
}
