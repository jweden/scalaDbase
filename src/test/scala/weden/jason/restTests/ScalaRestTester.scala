package weden.jason.restTests

import org.testng.annotations.Test
import org.scalatest.testng.TestNGSuite
import org.apache.log4j.{Logger, LogManager}

//import actors.Actor

import cc.spray.http._
import cc.spray.http.HttpMethods._
import cc.spray.can.HttpClient
import akka.config.Supervision._
import akka.actor.{PoisonPill, Actor, Supervisor}
import cc.spray.client.{Get, HttpConduit}
import cc.spray.typeconversion.SprayJsonSupport

//import cc.spray.utils.Logging


class ScalaRestTester extends TestNGSuite {
  private final val LOG: Logger = LogManager.getLogger(classOf[ScalaRestTester])

  @Test(invocationCount = 1, description = "something")
  def firstTest() {
    // start and supervise the spray-can HttpClient actor
    Supervisor(
      SupervisorConfig(
        OneForOneStrategy(List(classOf[Exception]), 3, 100),
        List(Supervise(Actor.actorOf(new HttpClient()), Permanent))
      )
    )

    fetchAndShowGoogleDotCom()

    fetchAndShowHeightOfMtEverest()

    Actor.registry.actors.foreach(_ ! PoisonPill)

    ///////////////////////////////////////////////////

    def fetchAndShowGoogleDotCom() {
      // the HttpConduit gives us access to an HTTP server, it manages a pool of connections
      val conduit = new HttpConduit("www.google.com")

      // send a simple request
      val responseFuture = conduit.sendReceive(HttpRequest(method = GET, uri = "/"))
      val response = responseFuture.get

      LOG.info("status: " + response.status.value)
      LOG.info("headers: " + response.headers)
      LOG.info("body: " + response.content)

      conduit.close()
    }

    def fetchAndShowHeightOfMtEverest() {
      LOG.info("Requesting the elevation of Mt. Everest from Googles Elevation API...")
      val conduit = new HttpConduit("maps.googleapis.com")
        with SprayJsonSupport
        with ElevationJsonProtocol {
        val elevationPipeline = simpleRequest ~> sendReceive ~> unmarshal[GoogleApiResult[Elevation]]
      }
      val responseFuture = conduit.elevationPipeline(Get("/maps/api/elevation/json?locations=27.988056,86.925278&sensor=false"))
      //   log.info("The elevation of Mt. Everest is: %s m", responseFuture.get.results.head.elevation)
      LOG.info("The elevation of Mt. Everest is: " + responseFuture.get.results.head.elevation)
      conduit.close()
    }
  }


}

