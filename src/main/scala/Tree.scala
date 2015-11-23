
/**
 * Created by jonathanh on 11/20/15.
 */

case class CategoryData( joined: List[(Database.Category, Database.CategoryBudget)],
                         children: collection.mutable.Map[Int, (Int, Int)])

case class CategoryTreeNode(id: Int, parent: Option[Int], name: String) {
  var list = collection.mutable.ListBuffer[Database.CategoryBudget]()
  var children = collection.mutable.ListBuffer[CategoryTreeNode]()

  def addChild(child: CategoryTreeNode): Unit = {
    children += child
  }

  def getParent(subTrees: Iterable[CategoryTreeNode]): Option[CategoryTreeNode] = {
    parent.flatMap( p => subTrees.find(n => p == n.id))
  }
}

class CategoryTree(data: CategoryData) {
  var subTrees: List[CategoryTreeNode] = Nil

  init()

  def init(): Unit = {
    val groups = data.joined
              .groupBy{ case(category, _) => category.id}
    subTrees = groups.map{ case(key, tuple) =>
      val (cat, _) = tuple.head
      new CategoryTreeNode(key, cat.parent_id, cat.name)
    }.toList

    subTrees.foreach { st =>
      st.getParent(subTrees).foreach(_.addChild(st))
    }
  }

  def toJsonString(): String = {
    subTrees.map(genJSONSubTreeString).mkString("[", ",", "]")
  }

  def genJSONSubTreeString(root: CategoryTreeNode): String = {
    println("name: " + root.name)
    s"'${root.name}'"
  }
}
