/**
 * Created by jonathanh on 8/3/15.
 */

import akka.actor.Actor.Receive
import akka.actor.{ActorLogging, Props, Actor, ActorSystem}
import akka.io.IO
import spray.can.Http
import spray.http._


import scala.util.{Try, Success, Failure}

class Server(port: Int) extends Actor with ActorLogging {

  override def receive: Receive = {
    case _: Http.Connected ⇒ sender ! Http.Register(new ServerRequest().self)
    case msg ⇒ log.warning("Unkwown message: " + msg)
  }

}

class ServerRequest extends Actor {
  /* routes */
  val route_home = "/home.html"

  override def receive: Receive = {

    case HttpRequest(HttpMethods.GET, Uri.Path(`route_home`), _, _, _) ⇒ {
      readPage(route_home) match {
        case Success(buffer) ⇒ {
          val body = buffer.mkString
          sender ! HttpResponse(
            entity = HttpEntity(MediaTypes.`text/html`, body)
          )
            .withHeaders(List(
            HttpHeaders.Connection("close")
          ))
        }
        case Failure(f) ⇒ {
          println("read failed " + f.getMessage)
        }
      }
    }
    case HttpRequest(HttpMethods.GET, unknown, _, _, _) ⇒ {
      println(s"404: ${unknown} not found")
    }
    case x ⇒ {
      println("x: " + x)
    }
  }

  def readPage(path: String) = {
    Try(scala.io.Source.fromFile("web" + path))
  }
}

object Start {
  implicit val system = ActorSystem()


  def main(args: Array[String]): Unit = {
    if(args.length != 1) {
      println("Specify Port");
    }
    else {
      val listenPort = args(0).toInt

      val actor = system.actorOf(Props(new Server(listenPort)))
      IO(Http) ! Http.Bind(actor, interface = "localhost", port = listenPort)

    }
  }
}