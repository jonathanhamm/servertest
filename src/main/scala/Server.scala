/**
 * Created by jonathanh on 8/3/15.
 */

import akka.actor.Actor.Receive
import akka.actor.{ActorLogging, Props, Actor, ActorSystem}
import akka.io.IO
import spray.can.Http
import spray.http._

import java.util.Date

import scalikejdbc._
import scalikejdbc.config._

import scala.util.{Try, Success, Failure}

trait ServerGlobal {
  val root = "web"

  def readPage(path: String) = {
    Try(scala.io.Source.fromFile(root + path))
  }
}

class Server(port: Int) extends Actor with ActorLogging {
  override def receive: Receive = {
    case _: Http.Connected ⇒ {
      val serveActor = Start.system.actorOf(Props(new ServerRequest))
      sender ! Http.Register(serveActor)
    }
    case msg ⇒ log.warning("Unkwown message: " + msg)
  }
}

class ServerRequest extends Actor with ServerGlobal {
  /* routes */
  val route_home = "/home.html"
  val route_history = "/history.html"
  val route_purchase = "/purchase"

  /* DB setup */
  implicit val session = AutoSession
  DBs.setupAll()
  Class.forName("com.mysql.jdbc.Driver")

  override def receive: Receive = {
    case HttpRequest(HttpMethods.GET, Uri.Path(`route_home`), _, _, _) ⇒ {
      sender ! serveGet(`route_home`)
    }
    case HttpRequest(HttpMethods.GET, Uri.Path(`route_history`), _, _, _) ⇒ {
      sender ! serveGet(`route_history`)
    }
    case HttpRequest(HttpMethods.POST, Uri.Path(`route_purchase`), header, entity, _) ⇒ {
      header.find(_.name == "Content-Type").foreach{ h ⇒
        h.value match {
          case v if v.contains("application/x-www-form-urlencoded") ⇒ {
              sender ! handlePurchase(urlFormEncodeToMap(entity.asString))
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

  def serveGet(route: String): HttpResponse = {
    readPage(route) match {
      case Success(buffer) ⇒ {
        val body = buffer.mkString
        val included = SSI.include(body)
        HttpResponse(
          entity = HttpEntity(MediaTypes.`text/html`, included)
        ).withHeaders(List(
          HttpHeaders.Connection("close")
        ))
      }
      case Failure(f) ⇒ {
        println("read failed: " + f.getMessage)
        HttpResponse(status = StatusCodes.NotFound)
      }
    }
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

  def handlePurchase(m: Map[String, String]): HttpResponse = {
    val num = "^\\d+(?:\\.\\d*)?$"

    if(
      m.exists{case(k, v) ⇒ k == "value" && v != null && v.matches(num)}
      &&
      m.exists{case(k, _) ⇒ k == "category"}
      &&
      m.exists{case(k, _) ⇒ k == "name"}
      &&
      m.exists{case(k, _) ⇒ k == "account"}
      &&
      m.exists{case(k, _) ⇒ k == "date"}
    ) {
      val value = m("value").toFloat
      val category = m("category")
      val name = m("name")
      val account = m("account")
      val dateLong = m("date").toLong
      val date = new Date(dateLong)
      val c = Database.Purchase.column

      updateAccount(value, account)
      updateCategoryAndGet(value, category)

      withSQL {
        insert.into(Database.Purchase).namedValues(
          c.value -> value,
          c.details -> name,
          c.date -> date
        )
      }.update().apply()


      HttpResponse(status = StatusCodes.OK)
    }
    else {
      HttpResponse(status = StatusCodes.InternalServerError, entity = """["Invalid Input Supplied to Server for Purchase"]""")
    }
  }

  def updateCategoryAndGet(value: Float, name: String): Option[Int] = {
    val category = Database.Category.syntax
    val c = Database.Category.column
    var id: Option[Int] = None

    withSQL {
      select.from(Database.Category as category).where.eq(c.name, name)
    }.map { rs ⇒
      withSQL {
        update(Database.Category).set(
          c.balance -> (rs.float(category.resultName.balance) - value)
        )
      }.update().apply()
      id = Some(rs.int(category.resultName.id))
    }.list().apply()
    id
  }

  def updateAccount(diff: Float, name: String): Unit = {
    val account = Database.Account.syntax
    val c = Database.Account.column

    withSQL {
      select.from(Database.Account as account).where.eq(c.name, name)
    }.map { rs ⇒
      withSQL {
        update(Database.Account).set(
          c.balance -> (rs.float(account.resultName.balance) - diff)
        ).where.eq(account.id, rs.int(account.resultName.id))
      }.update().apply()
    }.list().apply()
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