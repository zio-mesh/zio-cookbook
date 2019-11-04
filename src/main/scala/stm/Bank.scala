package zio.cookbook.stm

trait Account {
  def debit(): Unit
  def credit(): Unit
}

// trait Customer {
//   def bid (): Unit
//   def ask (): Unit
// }

// trait Bank {
//   def newTr
// }

// object Bank extends Bank {
//   val
// }
