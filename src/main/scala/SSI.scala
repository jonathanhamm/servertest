/**
 * Created by jonathanh on 8/24/15.
 */

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Node
import org.jsoup.nodes.Element
import org.jsoup.nodes.Comment
import scala.collection.JavaConversions._
import scala.util.{Try, Success, Failure}

object SSI extends ServerGlobal {
  def include(src: String, routineMap: Option[Map[String, () => String]] = None): String = {
    val doc = Jsoup.parse(src)
    val ssiIncludePattern = "\\s*#include\\s+file\\s*=\\s*\"([^\"]+)\"".r
    val routineIncludePattern = "\\s*#include\\s+routine\\s*=\\s*\"([^\"]+)\"".r

    doc.getAllElements.foreach { e ⇒
      e.childNodes.filter(_.isInstanceOf[Comment]).zipWithIndex.foreach { case(n, i) ⇒
        val comment = n.asInstanceOf[Comment]
        println("matching:" + comment.getData)
        comment.getData match {
          case ssiIncludePattern(fileName) ⇒ {
            readPage(fileName) match {
              case Success(src) ⇒ {
                val included = Jsoup.parse(src.mkString)
                val root = included.select(":root").first()
                comment.replaceWith(root)
              }
              case Failure(f) ⇒ {
                println("fail: " + f.getMessage)
              }
            }
          }
          case routineIncludePattern(key) ⇒ {
            val source = routineMap match {
              case Some(rMap) =>
                rMap.get(key) match {
                  case Some(f) => f()
                  case _ => "<p>ssi routine not found</p>"
                }
              case _ => "<p>ssi routine referenced but no map provided</p>"
            }
            val included = Jsoup.parseBodyFragment(source)
            comment.replaceWith(included.select(":root").first())
          }
          case _ => println("failed to match ssi include")
        }
      }
    }
    doc.toString
  }
}
