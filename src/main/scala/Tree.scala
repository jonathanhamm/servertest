/**
 * Created by jonathanh on 11/20/15.
 */

case class CategoryData( joined: List[(Database.Category, Database.CategoryBudget)],
                         children: collection.mutable.Map[Int, (Int, Int)])

case class CategoryTreeNode(id: Int, parent: Option[Int], name: String, list: Seq[Database.CategoryBudget]) {
  var children = collection.mutable.ListBuffer[CategoryTreeNode]()

  def addChild(child: CategoryTreeNode): Unit = {
    children += child
  }

  def getParent(subTrees: Iterable[CategoryTreeNode]): Option[CategoryTreeNode] = {
    parent.flatMap( p => subTrees.find(n => p == n.id))
  }
}

class CategoryTree(data: CategoryData) {
  var subTrees: List[CategoryTreeNode] = init()

  def init(): List[CategoryTreeNode] = {
    val groups = data.joined.groupBy{ case(category, _) => category.id}
    subTrees = groups.map{ case(key, tuple) =>
      val (cat, _) = tuple.head
      val list = makeCategoryList(tuple)
      new CategoryTreeNode(key, cat.parent_id, cat.name, list)
    }.toList
    subTrees.foreach {st => st.getParent(subTrees).foreach(_.addChild(st))}
    subTrees
  }

  def makeCategoryList(list: List[(Database.Category, Database.CategoryBudget)]): Seq[Database.CategoryBudget] = {
    list.map {
      case (_, bud: Database.CategoryBudget) =>
        new Database.CategoryBudget(bud.id, bud.start, bud.balance, bud.budget, bud.category)
      case _ => null
    }
  }

  def makeJsonString(): String = {
    subTrees.filter{t => t.parent match {
      case Some(_) => false
      case _ => true
    }}.map(genJSONSubTreeString).mkString("[", ",", "]")
  }

  def genJSONSubTreeString(root: CategoryTreeNode): String = {
    s"""["${root.name}",""" +
    s"""${root.list.map(_.toString).mkString("[", ",", "]")},""" +
    s"""${root.children.map(genJSONSubTreeString).mkString("[", ",", "]")}]"""
  }
}
