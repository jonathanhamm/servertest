import java.util.Date
import scalikejdbc._


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
                       parent_id: Int,
                       name: String,
                       active: Boolean)
  object Category extends SQLSyntaxSupport[Category] {
    override val tableName = "category"
    def apply(g: ResultName[Category])(rs: WrappedResultSet): Category = {
      new Category(
        rs.int(g.id), rs.int(g.parent_id),
        rs.string(g.name), rs.boolean(g.active)
      )
    }
    def apply(g: SyntaxProvider[Category])(rs: WrappedResultSet): Category = apply(g.resultName)(rs)

  }

  case class CategoryChildren( id: Int,
                               parent_id: Int,
                               child_id: Int )
  object CategoryChildren extends SQLSyntaxSupport[CategoryChildren] {
    override val tableName = "category_children"
    def apply(g: ResultName[CategoryChildren])(rs: WrappedResultSet): CategoryChildren = {
      new CategoryChildren(
        rs.int(g.id), rs.int(g.parent_id),
        rs.int(g.child_id)
      )
    }
  }

  case class CategoryBudget( id: Int,
                             start: Date,
                             balance: Float,
                             budget: Float,
                             category: Int)
  object CategoryBudget extends SQLSyntaxSupport[CategoryBudget] {
    override val tableName = "category_budget"
    def apply(g: ResultName[CategoryBudget])(rs: WrappedResultSet): CategoryBudget = {
      new CategoryBudget(
        rs.int(g.id), rs.date(g.start),
        rs.float(g.balance), rs.float(g.budget),
        rs.int(g.category)
      )
    }
  }

}