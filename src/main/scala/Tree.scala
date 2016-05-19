/**
 * Created by jonathanh on 11/20/15.
 */

import collection.mutable.ListBuffer

case class CategoryData( joined: List[(Database.Category, Database.CategoryBudget)],
                         children: collection.mutable.Map[Int, (Int, Int)])

case class CategoryTreeNode(id: Int, parent: Option[Int], name: String, list: Seq[Database.CategoryBudget]) extends Iterable[CategoryTreeNode] {
  override def iterator = Iterator[CategoryTreeNode]()

  var children = ListBuffer.empty[CategoryTreeNode]

  def addChild(child: CategoryTreeNode): Unit = {
    children += child
  }

  def getParent(subTrees: Iterable[CategoryTreeNode]): Option[CategoryTreeNode] = {
    parent.flatMap( p => subTrees.find(_.id == p))
  }
}

class CategoryTree(data: CategoryData) {
  var subTrees: ListBuffer[CategoryTreeNode] = ListBuffer.empty[CategoryTreeNode]

  init()

  def init(): Unit = {
    val groups = data.joined.groupBy{ case(category, _) => category.id}
    subTrees ++= groups.map{ case(key, tuple) =>
      val (cat, _) = tuple.head
      val list = makeCategoryList(tuple)
      new CategoryTreeNode(key, cat.parent_id, cat.name, list)
    }
    subTrees.foreach {st => st.getParent(subTrees).foreach(_.addChild(st))}
  }

  def makeCategoryList(list: List[(Database.Category, Database.CategoryBudget)]): Seq[Database.CategoryBudget] = {
    list.map {
      case (_, bud: Database.CategoryBudget) =>
        new Database.CategoryBudget(bud.id, bud.start, bud.balance, bud.budget, bud.category)
      case _ => null
    }
  }

  def flatten: ListBuffer[CategoryTreeNode] = {
    def flattenNode(root: CategoryTreeNode): ListBuffer[CategoryTreeNode] =
      root +: root.children.flatMap(flattenNode)
    subTrees.flatMap(flattenNode)
  }

  def makeJsonString(): String = {
    val varMap = genVarMap()
    val roots = subTrees.filter(_.parent.isEmpty).map{c =>
      varMap(c.name) match {case (n, _) => n}
    }.mkString("[",",","]")
    s"${emitJSONDeclarations(varMap)};var cData=$roots;"
  }

  def genVarMap(): Map[String, (String, CategoryTreeNode)] = {
    flatten.zipWithIndex.map { case(c, i) =>
      (c.name, ("_c" + i, c))
    }.toMap
  }

  def emitJSONDeclarations(varMap: Map[String, (String, CategoryTreeNode)]): String = {
    val declarations = flatten.zipWithIndex.map{case (l, i) =>
      l.getParent(subTrees) match {
        case Some(p) => {
          val pVar = varMap(p.name) match {case(v, _) => v}
          s"""var _c$i={"item":"${l.name}",${l.list.head.toString},"parent":$pVar}"""
        }
        case _ => s"""var _c$i={"item":"${l.name}",${l.list.head.toString}}"""
      }
    }.mkString(";")

    val idMap = varMap.map{case(_, (name, record)) =>
      s"""${record.id}:$name"""
    }.mkString("var cDataIdMap = {", ",", "}")

    val childAssign = varMap.map{ case(name, (varName, cat)) =>
      if(cat.children.size > 0) {
        cat.children.map { c =>
          varMap(c.name) match {
            case (n, _) => n
          }
        }.mkString(s"$varName.children=[", ",", "]")
      }
      else { "" }
    }.mkString(";")

    s"$declarations;$idMap;$childAssign"
  }
}
