import java.util.Date
import scalikejdbc._
import collection.mutable.ListBuffer

object Database {
  case class Purchase( id: Int,
                       value: Float,
                       account: Int,
                       category_budget: Int,
                       details: String,
                       date: Date)
  object Purchase extends SQLSyntaxSupport[Purchase] {
    override val tableName = "purchase"
    def apply(g: ResultName[Purchase])(rs: WrappedResultSet): Purchase = {
      new Purchase(
        rs.int(g.id), rs.float(g.value),
        rs.int(g.account), rs.int(g.category_budget),
        rs.string(g.details), rs.date(g.date)
      )
    }
  }

  case class Income( id: Int,
                     value: Float,
                     account: Int,
                     source: Int,
                     date: Date,
                     period: Date)
  object Income extends SQLSyntaxSupport[Income] {
    override val tableName = "income"
    def apply(g: ResultName[Income])(rs: WrappedResultSet): Income = {
      new Income(
        rs.int(g.id), rs.float(g.value),
        rs.int(g.account), rs.int(g.source),
        rs.date(g.date), rs.date(g.period)
      )
    }
  }

  case class Account( id: Int,
                      name: String,
                      balance: Float,
                      description: String)
  object Account extends SQLSyntaxSupport[Account] {
    override val tableName = "account"
    def apply(g: ResultName[Account])(rs: WrappedResultSet): Account = {
      new Account(
        id = rs.int(g.id), name = rs.string(g.name),
        balance = rs.float(g.balance), description = rs.string(g.description)
      )
    }
  }

  case class Category( id: Int,
                       name: String,
                       active: Boolean)
  object Category extends SQLSyntaxSupport[Category] {
    override val tableName = "category"
    def apply(g: ResultName[Category])(rs: WrappedResultSet): Category = {
      new Category(
        rs.int(g.id), rs.string(g.name), rs.boolean(g.active)
      )
    }
    def apply(g: SyntaxProvider[Category])(rs: WrappedResultSet): Category = apply(g.resultName)(rs)

  }

  case class CategoryBudget( id: Int,
                             start: Date,
                             balance: Float,
                             budget: Float,
                             category: Int,
                             pid: Option[Int]) {
    var children = ListBuffer.empty[CategoryBudget]

    def addChild(c: CategoryBudget): Unit = {
      println("add child called for: " + c.id)
      children += c
    }

    def getParent(subTrees: Iterable[CategoryTreeNode]): Option[CategoryBudget] = {
      pid.flatMap(p => subTrees.flatMap(_.list.find(_.id == p)).lastOption)
    }

    def getCategory(subTrees: Iterable[CategoryTreeNode]): Option[CategoryTreeNode] = {
      subTrees.find(_.id == category)
    }

    override def toString: String = {
      s""""id":$id,"start":"$start","balance":$balance,"budget":$budget,"category":$category"""
    }
  }
  object CategoryBudget extends SQLSyntaxSupport[CategoryBudget] {
    override val tableName = "category_budget"
    def apply(g: ResultName[CategoryBudget])(rs: WrappedResultSet): CategoryBudget = {
      new CategoryBudget(
        rs.int(g.id), rs.date(g.start),
        rs.float(g.balance), rs.float(g.budget),
        rs.int(g.category), rs.intOpt(g.pid)
      )
    }
  }

}