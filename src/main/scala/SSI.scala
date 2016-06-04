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

  val maxDepth = 500

  def include(src: String, routineMap: Option[Map[String, () => String]] = None, depth: Int = 0): Document = {
    val doc = Jsoup.parseBodyFragment(src)
    val ssiIncludePattern = "\\s*#include\\s+file\\s*=\\s*\"([^\"]+)\"".r
    val routineIncludePattern = "\\s*#include\\s+routine\\s*=\\s*\"([^\"]+)\"".r

    doc.getAllElements.foreach { e =>
      e.childNodes.filter(_.isInstanceOf[Comment]).zipWithIndex.foreach { case(n, i) =>
        val comment = n.asInstanceOf[Comment]
        comment.getData match {
          case ssiIncludePattern(fileName) => {
            if(depth <= maxDepth) {
              readPage(fileName) match {
                case Success(nSrc) => {
                  val root = include(nSrc.mkString, routineMap, depth + 1)
                    .select(":root").first()
                  comment.replaceWith(root)
                }
                case Failure(f) => {
                  println("fail: " + f.getMessage)
                }
              }
            }
            else {
              val root = Jsoup.parseBodyFragment(
                "<p>max include recursion depth exceeded, <b>yew long-legged motherfugger yew</b></p>"
              ).select(":root").first()
              comment.replaceWith(root)
            }
          }
          case routineIncludePattern(key) => {
            val source = routineMap match {
              case Some(rMap) =>
                rMap.get(key) match {
                  case Some(f) => f()
                  case _ => "<p>ssi routine not found</p>"
                }
              case _ => "<p>ssi routine referenced but no map provided, <b>yew long-legged motherfugger yew</b></p>"
            }
            val included = Jsoup.parseBodyFragment(source)
            comment.replaceWith(included.select(":root").first())
          }
          case _ => println("failed to match ssi include")
        }
      }
    }
    doc
  }
}
