/**
 * Created by jonathanh on 11/20/15.
 */

case class CategoryData( joined: List[(Database.Category, Database.CategoryBudget)],
                         children: collection.mutable.Map[Int, (Int, Int)])

class CategoryTreeNode(id: Int, parent: Int) {
  var children = Seq[CategoryTreeNode]()
}

class CategoryTree(data: CategoryData) {
  var subTrees = collection.mutable.ListBuffer[CategoryTreeNode]()
  var strTest = "["
  init()

  def init(): Unit = {

    data.joined.groupBy{ case(category, _) => category.id}
      .foreach{ case (k, _) =>
        strTest += (k + ",")
      }
    strTest += "]"

    data.joined.foreach{ j =>
      subTrees += new CategoryTreeNode(1,2)
    }
  }

  def toJsonString(): String = {
    strTest
  }

}
