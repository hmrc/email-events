/*
 * Copyright 2024 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.emailevents

import org.scalatest.concurrent.{ IntegrationPatience, ScalaFutures }
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.libs.json.Json
import play.api.libs.ws.writeableOf_JsValue
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{ HeaderCarrier, HttpResponse, StringContextOps }
import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global

class CaptureEventsSpec
    extends AnyWordSpec with Matchers with ScalaFutures with IntegrationPatience with GuiceOneServerPerSuite {
  private val httpClient: HttpClientV2 = app.injector.instanceOf[HttpClientV2]
  private val baseUrl: String = s"http://localhost:$port"
  val emailBaseUrl = s"http://localhost:8300"

  "email-events endpoint" should {
    "store event in event-hub collection in email" in new TestClass {
      given headerCarrier: HeaderCarrier = HeaderCarrier()
      val emailEventsResponse =
        httpClient.post(url"$baseUrl/email-events").withBody(data).execute[HttpResponse].futureValue
      emailEventsResponse.status shouldBe 200
    }
  }

  class TestClass {
    val transitId = UUID.randomUUID().toString
    val data = Json.parse(
      s"""{
         |  "deliveryInfoNotification": {
         |    "deliveryInfo": {
         |      "timeStamp": "2022-12-07T14:40:46.886Z",
         |      "Description": "Submitted",
         |      "code": "7501",
         |      "deliveryChannel": "email",
         |      "additionalInfo": "",
         |      "destination": "test.dc@digital.hmrc.gov.uk",
         |      "destinationType": "email",
         |      "deliveryStatus": "Submitted"
         |    },
         |    "subtid": "",
         |    "transid": "$transitId",
         |    "callbackData": "eyJuYW1lIjoiZW5jcnlwdGVkU3RyaW5nIiwicmVnaW1lIjoiZW5jcnlwdGVkU3RyaW5nIiwidGVtcGxhdGVJZCI6ImVuY3J5cHRlZFN0cmluZyIsInBsYXRmb3JtIjoiZW5jcnlwdGVkU3RyaW5nIiwiQ29udGFjdFBvbGljeUdyb3VwSWQiOiIifQ==",
         |    "correlationid": "4310b3f8-9d89-47a3-9c72-4482f9ef14c9"
         |  }
         |}""".stripMargin
    )
  }

}
