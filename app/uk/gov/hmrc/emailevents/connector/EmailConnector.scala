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

package uk.gov.hmrc.emailevents.connector

import play.api.Logging
import play.api.http.Status
import play.api.libs.json.{ JsValue, Json }
import play.api.libs.ws.writeableOf_JsValue
import uk.gov.hmrc.emailevents.models.connector.Event
import uk.gov.hmrc.emailevents.models.{ EventSaveError, EventSaveFailed, EventSaveResult, EventSaved }
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{ HeaderCarrier, HttpResponse, StringContextOps }
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import javax.inject.{ Inject, Singleton }
import scala.concurrent.{ ExecutionContext, Future }
import scala.language.implicitConversions

@Singleton
class EmailConnector @Inject() (servicesConfig: ServicesConfig, httpClient: HttpClientV2)(implicit ec: ExecutionContext)
    extends Logging {

  implicit val headerCarrier: HeaderCarrier = HeaderCarrier()
  val serviceUrl: String = servicesConfig.baseUrl("email")

  def send(event: Event): Future[EventSaveResult] = {
    val eventualResponse: Future[HttpResponse] =
      httpClient.post(url"$serviceUrl/events").withBody(Json.toJson(event)).execute[HttpResponse]
    eventualResponse
      .map { response =>
        response.status match {
          case Status.CREATED => EventSaved("true")
          case status =>
            EventSaveFailed(EventSaveError(status.intValue, s"Event save failed - status code: ${status.intValue}"))
        }
      }
      .recover { case e =>
        EventSaveFailed(
          EventSaveError(Status.INTERNAL_SERVER_ERROR, s"HTTP request to email failed with error: ${e.getMessage}")
        )
      }
  }
}
