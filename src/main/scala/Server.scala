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
    case _: Http.Connected => {
      val serveActor = Start.system.actorOf(Props(new ServerRequest))
      sender ! Http.Register(serveActor)
    }
    case msg => log.warning("Unkwown message: " + msg)
  }
}

class ServerRequest extends Actor with ServerGlobal {
  /* routes */
  val route_home = "/home.html"
  val route_history = "/history.html"
  val route_plan = "/plan.html"
  val route_purchase = "/purchase"
  val route_jquery = "/jquery/dist/jquery.min.js"
  val route_tree = "/tree.js"
  val route_tree_style = "/tree.css"
  val route_new_category = "/new-category"

  /* DB setup */
  implicit val session = AutoSession
  DBs.setupAll()
  Class.forName("com.mysql.jdbc.Driver")

  override def receive: Receive = {
    case HttpRequest(HttpMethods.GET, Uri.Path(`route_home`), _, _, _) => {
      sender ! serveGet(`route_home`)
    }
    case HttpRequest(HttpMethods.GET, Uri.Path(`route_history`), _, _, _) => {
      sender ! serveGet(`route_history`)
    }
    case HttpRequest(HttpMethods.GET, Uri.Path(`route_plan`), _, _, _) => {
      sender ! serveGet(`route_plan`)
    }
    case HttpRequest(HttpMethods.GET, Uri.Path(`route_jquery`), _, _, _) => {
      sender ! serveGet(`route_jquery`)
    }
    case HttpRequest(HttpMethods.GET, Uri.Path(`route_tree`), _, _, _) => {
      sender ! serveGet(`route_tree`)
    }
    case HttpRequest(HttpMethods.GET, Uri.Path(`route_tree_style`), _, _, _) => {
      sender ! serveGet(`route_tree_style`)
    }
    case HttpRequest(HttpMethods.POST, Uri.Path(`route_purchase`), header, entity, _) => {
      header.find(_.name == "Content-Type").foreach{ h =>
        h.value match {
          case v if v.contains("application/x-www-form-urlencoded") => {
              sender ! handlePurchase(urlFormEncodeToMap(entity.asString))
          }
          case mime => {
            println("unknown mime type: " + mime)
          }
        }
      }
    }
    case HttpRequest(HttpMethods.POST, Uri.Path(`route_new_category`), _, entity, _) => {
      if(entity.isEmpty) {
        sender ! make400()
      }
      else {
        val p = "parent=(\\d+)".r
        sender ! {
          entity.asString match {
            case p(pID) => HttpResponse(
              status = StatusCodes.OK,
              entity = HttpEntity(makeNewCategory(pID.toInt))
            )
            case _ => make400()
          }
        }
      }
    }
    case HttpRequest(method, unknown, _, _, _) => {
      println(s"404: ${method.toString()} : ${unknown.toString()} not found")
      sender ! make404()
    }
    case x => {
      println("x: " + x)
    }
  }

  def serveGet(route: String): HttpResponse = {
    readPage(route) match {
      case Success(buffer) => {
        val htmlPattern = "(.*\\.html?)$".r
        val body = buffer.mkString
        val payload = route match {
          case htmlPattern(_) => SSI.include(body, Some(ssiRMap)).toString
          case _ => body
        }
        HttpResponse(
          entity = HttpEntity(MediaTypes.`text/html`, payload)
        ).withHeaders(List(
          HttpHeaders.Connection("close")
        ))
      }
      case Failure(f) => {
        println("read failed: " + f.getMessage)
        make404()
      }
    }
  }

  def urlFormEncodeToMap(data: String): Map[String,String] = {
    data.split("&").map { prop =>
      val p = prop.split("=")

      if(p.length == 2) {
        p(0) -> p(1)
      }
      else {
        p(0) -> null
      }
    }
    .toMap[String, String]
  }

  def handlePurchase(m: Map[String, String]): HttpResponse = {
    val num = "^\\d+(?:\\.\\d*)?$"

    if(
      m.exists{case(k, v) => k == "value" && v != null && v.matches(num)}
      &&
      m.exists{case(k, _) => k == "category"}
      &&
      m.exists{case(k, _) => k == "name"}
      &&
      m.exists{case(k, _) => k == "account"}
      &&
      m.exists{case(k, _) => k == "date"}
    ) {
      val value = m("value").toFloat
      val category = m("category")
      val name = m("name")
      val account = m("account")
      val dateLong = m("date").toLong
      val date = new Date(dateLong)
      val c = Database.Purchase.column
      val aID = updateAccountAndGet(value, account)

      getLatestCategoryBudget(category) match {
        case Some(cb) => {
          applyUpdate {
            insert.into(Database.Purchase).namedValues(
              c.value -> value, c.account -> aID,
              c.category_budget -> cb.id, c.details -> name,
              c.date -> date
            )
          }
          HttpResponse(status = StatusCodes.OK)
        }
        case _ => {
          HttpResponse(status = StatusCodes.InternalServerError)
        }
      }
    }
    else {
      HttpResponse(status = StatusCodes.InternalServerError, entity = """["Invalid Input Supplied to Server for Purchase"]""")
    }
  }

  def make404(): HttpResponse = {
    val responseEntity = readPage("/404.html") match {
      case Success(buffer) => SSI.include(buffer.mkString).toString
      case _ => "404 Not Found, AND FAILED TO READ THE 404 FILE. SUPER FAIL"
    }

    HttpResponse(
      entity = HttpEntity(MediaTypes.`text/html`, responseEntity),
      status = StatusCodes.NotFound
    )
  }

  def make400(): HttpResponse = HttpResponse(status = StatusCodes.BadRequest)

  def getLatestCategoryBudget(name: String): Option[Database.CategoryBudget] = {
    val categoryBudget = Database.CategoryBudget.syntax
    val category = Database.Category.syntax
    val results = withSQL {
      select.from(Database.CategoryBudget as categoryBudget)
        .join(Database.Category as category)
        .on(category.id, categoryBudget.category)
    }.map(Database.CategoryBudget(categoryBudget.resultName))
      .list().apply()
    Option(results.maxBy(_.start))
  }

  def updateAccountAndGet(diff: Float, name: String): Int = {
    val account = Database.Account.syntax
    val c = Database.Account.column
    var id: Int = 0

    withSQL {
      select.from(Database.Account as account).where.eq(c.name, name)
    }.map { rs =>
      withSQL {
        update(Database.Account).set(
          c.balance -> (rs.float(account.resultName.balance) - diff)
        ).where.eq(account.id, rs.int(account.resultName.id))
      }.update().apply()
      id = rs.int(account.resultName.id)
    }.list().apply()
    id
  }

  def queryCategoryData(): CategoryData = {
    val category = Database.Category.syntax
    val categoryBudget = Database.CategoryBudget.syntax

    val joined = withSQL {
      select.from(Database.Category as category).join(Database.CategoryBudget as categoryBudget)
      .on(category.id, categoryBudget.category)
    }.map{ rs =>
      val cat = Database.Category(
        rs.int(category.resultName.id),
        rs.string(category.resultName.name),
        rs.boolean(category.resultName.active)
      )
      val catBud = Database.CategoryBudget(
        rs.int(categoryBudget.resultName.id),
        rs.date(categoryBudget.resultName.start),
        rs.float(categoryBudget.resultName.balance),
        rs.float(categoryBudget.resultName.budget),
        rs.int(categoryBudget.resultName.category),
        rs.intOpt(categoryBudget.resultName.pid)
      )
      (cat, catBud)
    }.list().apply()

    new CategoryData(joined)
  }

  /*
  var _c0 = {
    "item": "qwer",
    "parent": _c1,
    "budgets": [{
        "id": 6,
        "start": "2016-05-25",
        "balance": 0.0,
        "budget": 300.0,
        "category": 5
    }]

    s"""{"id":$id,"item":"","parent":$pID,"budgets":[{}]}"""
};

   */
  def makeNewCategory(pID: Int): String = {
    val c = Database.Category.column

    DB localTx { implicit session =>
      applyUpdate {
        insert.into(Database.Category).namedValues(
            c.name -> "test",
            c.active -> true
        )
      }
      sql"SELECT LAST_INSERT_ID()".map(_.int(1)).single().apply()
    }
    match {
      case Some(id) => {
        val bId = makeNewBudget(pID, id)
        println("bID: " + bId)
        s"""{"id":$id,"item":"","start":"${new Date()}","balance":0,"budget":0,"category":$id}"""
      }
      case _ => ""
    }
  }

  def makeNewBudget(pID: Int, category: Int): Int = {
    val c = Database.CategoryBudget.column

    DB localTx { implicit session =>
      applyUpdate {
        insert.into(Database.CategoryBudget).namedValues(
          c.start -> new Date(),
          c.balance -> 0.0f,
          c.budget -> 0.0f,
          c.category -> category,
          c.pid -> pID
        )
      }
      sql"SELECT LAST_INSERT_ID()".map(_.int(1)).single().apply()
    }
    match {
      case Some(bId) => bId
      case _ => -1
    }
  }

  val prepareCategoryData = () => {
    val tree = new CategoryTree(queryCategoryData())
    s"<script>${tree.makeJsonString()}</script>"
  }

  val ssiRMap: Map[String, () => String] = Map(
    "fetchCategoryData" -> prepareCategoryData
  )
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