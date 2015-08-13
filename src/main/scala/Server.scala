/**
 * Created by jonathanh on 8/3/15.
 */

import akka.actor.Actor.Receive
import akka.actor.{ActorLogging, Props, Actor, ActorSystem}
import akka.io.IO
import spray.can.Http
import spray.http._

import scalikejdbc._
import scalikejdbc.config._

import scala.util.{Try, Success, Failure}

class Server(port: Int) extends Actor with ActorLogging {

  override def receive: Receive = {
    case _: Http.Connected ⇒ {
      val serveActor = Start.system.actorOf(Props(new ServerRequest))
      sender ! Http.Register(serveActor)
    }
    case msg ⇒ log.warning("Unkwown message: " + msg)
  }
}

class ServerRequest extends Actor {
  /* routes */
  val route_home = "/home.html"
  val route_purchase = "/purchase"

  override def receive: Receive = {

    case HttpRequest(HttpMethods.GET, Uri.Path(`route_home`), _, _, _) ⇒ {
      readPage(route_home) match {
        case Success(buffer) ⇒ {
          val body = buffer.mkString
          sender ! HttpResponse(
            entity = HttpEntity(MediaTypes.`text/html`, body)
          ).withHeaders(List(
            HttpHeaders.Connection("close")
          ))
        }
        case Failure(f) ⇒ {
          println("read failed " + f.getMessage)
        }
      }
    }
    case HttpRequest(HttpMethods.POST, Uri.Path(`route_purchase`), header, entity, _) ⇒ {
      header.find(_.name == "Content-Type").foreach{ h ⇒
        h.value match {
          case v if v.contains("application/x-www-form-urlencoded") ⇒ {
            handlePurchase(urlFormEncodeToMap(entity.asString))
            sender ! HttpResponse()
          }
          case mime ⇒ {
            println("unknown mime type: " + mime)
          }
        }
      }
    }
    case HttpRequest(method, unknown, _, _, _) ⇒ {
      println(s"404: ${method.toString()} : ${unknown.toString()} not found")
      sender ! HttpResponse(status = StatusCodes.NotFound)
    }
    case x ⇒ {
      println("x: " + x)
    }
  }

  def readPage(path: String) = {
    Try(scala.io.Source.fromFile("web" + path))
  }

  def urlFormEncodeToMap(data: String): Map[String,String] = {
    println("data: " + data)
    data.split("&").map { prop ⇒
      val p = prop.split("=")
      if(p.length == 2) {
        p(0) -> p(1)
      }
      else {
        p(0) -> null
      }
    }
    .toMap[String,String]
  }

  /*

  		`id` INTEGER NOT NULL,
		`value` FLOAT,
		`account` INTEGER,
		`category` VARCHAR(127),
		`details` TEXT,
		`date` DATE
   */

  /*


  CREATE TABLE purchase (
		`id` INTEGER NOT NULL,
		`value` FLOAT,
		`account` INTEGER,
		`category` VARCHAR(127),
		`details` TEXT,
		`date` DATE
		);

DROP TABLE IF EXISTS credit;
CREATE TABLE credit(
		`id` INTEGER NOT NULL,
		`value` FLOAT,
		`account` INTEGER,
		`source` INTEGER,
		`date` DATE
		);

DROP TABLE IF EXISTS source;
CREATE TABLE source(
		`id` INTEGER NOT NULL,
		`name` VARCHAR(127)
		);

DROP TABLE IF EXISTS account;
CREATE TABLE account(
		`id` INTEGER NOT NULL,
		`name` VARCHAR(127),
    `balance` FLOAT
		);


DROP TABLE IF EXISTS chron_payment;
CREATE TABLE cron_payment(
		`id` INTEGER NOT NULL,
		`name` VARCHAR(127),
		`last_update` DATE
		);


   */

  def handlePurchase(m: Map[String, String]): Unit = {
    val num = "\\d+(?:\\.\\d*)?"

    if(
      m.exists{case(k, v) ⇒ k == "value" && v.matches(num)}
      &&
      m.exists{case(k, _) ⇒ k == "category"}
      &&
      m.exists{case(k, _) ⇒ k == "name"}
      &&
      m.exists{case(k, _) ⇒ k == "date"}
    ) {
      val value = m("value").toFloat
      val category = m("category")
      val name = m("name")
      val date = m("date")


      println("executing query")

      implicit val session = AutoSession

      DBs.setupAll()
      Class.forName("com.mysql.jdbc.Driver")


      sql"INSERT INTO purchase(`value`,`account`,`category`,`details`,`date`) values ($value,0,$category,$name,$date)".update().apply()


    }
    else {
      println("bad input data")
    }
  }

}

object Start {
  implicit val system = ActorSystem()

  def main(args: Array[String]): Unit = {


    if(args.length != 1) {
      println("Specify Port")
    }
    else {
      val listenPort = args(0).toInt

      val actor = system.actorOf(Props(new Server(listenPort)))
      IO(Http) ! Http.Bind(actor, interface = "localhost", port = listenPort)
    }

    DBs.closeAll()
  }
}