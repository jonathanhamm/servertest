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
    val generated = subTrees.filter(_.parent.isDefined)
      .map(genJSONSubTreeString).mkString("[",",","]")
    println("generated: " + generated)
    generated
  }

  def emitJSONDeclarations: String = {
    println("flatten size: " + flatten.size)
    flatten.zipWithIndex.map{case (l, i) =>
      s"var _cat$i={${l.list.head.toString}}"}
      .mkString(";")
  }

  def genJSONSubTreeString(root: CategoryTreeNode): String = {
    s"""{"item": "${root.name}",""" +
    s"""${root.list.map(_.toString).mkString}""" +
      root.getParent(subTrees).map(_.name).mkString(",\"parent\":\"","","\"") + {
      if (root.children.size > 0) {
        s""","children":${root.children.map(genJSONSubTreeString).mkString("[", ",", "]")}}"""
      }
      else {
        "}"
      }
    }
  }
}
