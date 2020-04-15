package graph

import scalax.collection.Graph
import scalax.collection.GraphEdge._

case class MyNode(id: Int, op: Function1[Int, Int])

case class MyEdge(
  val nodeFrom: MyNode,
  val nodeTo: MyNode
) extends DiEdge(NodeProduct(nodeFrom, nodeTo))

object App1 extends App {

  val f = (_: Int) + 1
  val g = (_: Int) * 2
  val h = (_: Int) - 3

  val n1 = MyNode(1, f)
  val n2 = MyNode(2, g)
  val n3 = MyNode(3, h)

  val e1 = MyEdge(n1, n2)
  val e2 = MyEdge(n2, n3)

  val nset = Set(n1, n2, n3)
  val eset = Set(e1, e2)
  val g1   = Graph.from(nset, eset)

  println(g1.isAcyclic)
  println(g1.isComplete)

  val fold = g1.nodes.foldLeft(0)((a, b) => a + b.op(1))

}
