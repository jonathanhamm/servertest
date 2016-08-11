/**
 * Created by jonathanh on 11/20/15.
 */

import collection.mutable.ListBuffer

case class CategoryData( joined: List[(Database.Category, Database.CategoryBudget)])

case class CategoryTreeNode(id: Int, name: String, list: Seq[Database.CategoryBudget]) extends Iterable[CategoryTreeNode] {
  override def iterator = Iterator[CategoryTreeNode]()

  def toJSON(n: Int): String =
    list.take(n).map("{" + _ + "}").mkString("[", ",", "]")

  def parent: Option[Int] = list.head.pid

}

class CategoryTree(data: CategoryData) {
  var subTrees: ListBuffer[CategoryTreeNode] = ListBuffer.empty[CategoryTreeNode]

  init()

  def init(): Unit = {
    val groups = data.joined.groupBy{ case(category, _) => category.id}

    subTrees ++= groups.map{ case(key, tuple) =>
      val (cat, _) = tuple.head
      val list = makeCategoryList(tuple)

      //list.foreach(l => l.setParent())
      new CategoryTreeNode(key, cat.name, list)
    }

    subTrees.foreach{st =>
      st.list.foreach{ n =>
        if(n.getParent(subTrees).isEmpty) {
          println("parent is empty")
        }
        else {
          println("parent is not empty")
        }
        n.getParent(subTrees).foreach(_.addChild(n))
      }
    }
  }

  def makeCategoryList(list: List[(Database.Category, Database.CategoryBudget)]): Seq[Database.CategoryBudget] = {
    list.map {
      case (_, bud: Database.CategoryBudget) =>
        new Database.CategoryBudget(bud.id, bud.start, bud.balance, bud.budget, bud.category, bud.pid)
      case _ => null
    }
  }

  def makeJsonString(): String = {
    val varMap = genVarMap()
    val roots = subTrees.filter(_.parent.isEmpty).map{c =>
      varMap(c.name) match {case (n, _) => n}
    }.mkString("[",",","]")
    s"${emitJSONDeclarations(varMap)};var cData=$roots;"
  }

  def genVarMap(): Map[String, (String, CategoryTreeNode)] = {
    subTrees.zipWithIndex.map { case(c, i) =>
      (c.name, ("_c" + i, c))
    }.toMap
  }

  def emitJSONDeclarations(varMap: Map[String, (String, CategoryTreeNode)], budgetHistory: Int = 1): String = {
    val declarations = subTrees.zipWithIndex.map{case (l, i) =>
      l.list.head.getParent(subTrees) match {
        case Some(p) => {
          p.getCategory(subTrees) match {
            case Some(t) => {
              val pVar = varMap(t.name) match {case(v, _) => v}
              s"""var _c$i={"id":"${l.id}","item":"${l.name}","parent":$pVar,"budgets":${l.toJSON(budgetHistory)}}"""
            }
            case _ => """["Error: Category Not Found"]"""
          }
        }
        case _ => s"""var _c$i={"item":"${l.name}","budgets":${l.toJSON(budgetHistory)}}"""
      }
    }.mkString(";")

    varMap.foreach{case(_, (_, record)) => println("record id: " + record.id)}

    val idMap = varMap.map{case(_, (name, record)) =>
      s"""${record.id}:$name"""
    }.mkString("var cDataIdMap = {", ",", "}")

    val childAssign = varMap.map{ case(name, (varName, cat)) =>
      if(cat.list.head.children.size > 0) {
        cat.list.head.children.map{ c =>
          println("at child: : " + c.id)
          c.getCategory(subTrees) match {
            case Some(t) => {
              varMap(t.name) match {
                case (n, _) => n
              }
            }
            case _ => """["Error: Category Not Found"]"""
          }
        }.mkString(s"$varName.budgets[0].children=[", ",", "]")
      }
      else {""}
    }.mkString(";")

    s"$declarations;$idMap;$childAssign"
  }
}