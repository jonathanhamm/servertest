import java.util.Date
import scalikejdbc._


object Database {
  case class Purchase( id: Int,
                       value: Float,
                       account: Int,
                       category: Int,
                       details: String,
                       date: Date)
  object Purchase extends SQLSyntaxSupport[Purchase] {
    override val tableName = "purchase"
    def apply(g: ResultName[Purchase])(rs: WrappedResultSet): Purchase = {
      new Purchase(
        rs.int(g.id), rs.float(g.value),
        rs.int(g.account), rs.int(g.category),
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
        rs.int(g.id), rs.string(g.name),
        rs.float(g.balance), rs.string(g.description)
      )
    }
  }

  case class Category( id: Int,
                       name: String,
                       budget: Float)
  object Category extends SQLSyntaxSupport[Category] {
    override val tableName = "category"
    def apply(g: ResultName[Category])(rs: WrappedResultSet): Category = {
      new Category(
        rs.int(g.id), rs.string(g.name), rs.float(g.budget)
      )
    }
  }

}