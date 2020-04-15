package graph

import scalax.collection.Graph
import scalax.collection.GraphPredef._

sealed trait AST extends Product with Serializable

final case class NodeA(id: Int, op: Function1[Int, Int]) extends AST
final case class NodeB(id: Int, op: Function1[Int, Int]) extends AST

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

  // val fld = g.nodes.foldLeft(g1)((a, b) => a + b)

  // Find Edges
  println(g.find(n0))

  // Remove/Add edges
  val g0 = g - n3
  println(g0)

  // Traverse graph
  def procNode(node: g.NodeT): Unit = println(node)

  root.innerEdgeTraverser.map(_.foreach(procNode))
  val res0 = root.outerNodeTraverser.map(_.value)
  println(res0)
}
