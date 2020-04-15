package graph

import scalax.collection.Graph
import scalax.collection.GraphPredef._

sealed trait AST

final case class NodeA(id: Int, op: Function1[Int, Int]) extends AST {
  def run(): Unit = println(">>>>> Run A")
}

final case class NodeB(id: Int, op: Function1[Int, Int]) extends AST {
  def run(): Unit = println(">>>>> Run B")
}

object App0 extends App {

  // Some methods
  val f = (_: Int) + 1
  val q = (_: Int) * 2
  val h = (_: Int) - 3

  // Build graph
  val n0 = NodeA(0, f)
  val n1 = NodeB(1, f)
  val n2 = NodeB(2, h)
  val n3 = NodeA(3, q)

  val nodes = Set(n0, n1, n2, n3)
  val edges = Set(n0 ~> n1, n0 ~> n2)

  val g    = Graph.from(nodes, edges)
  val root = g.get(n0)

  println(g)
  println(g.nodes)
  println(g.edges)
  println(s"Graph is acyclic: ${g.isAcyclic}")
  println(s"Graph is directed: ${g.isDirected}")
  println(s"Graph is connected: ${g.isConnected}")

  // Find nodes
  println(g.find(n0))

  // Remove/Add nodes
  val g0 = g - n3

  // Traverse graph nodes
  def procNode(node: g.NodeT): Unit = node.value match {
    case n: NodeA => n.run()
    case n: NodeB => n.run()
    case _        =>
  }
  root.innerNodeTraverser.foreach(procNode)

  // Traverse graph edges
  val res0 = root.outerEdgeTraverser.map(_.value)
  println(res0)
}
