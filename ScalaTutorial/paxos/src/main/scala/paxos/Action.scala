package paxos

sealed trait BroadcastGroup
case object AcceptorsGroup extends BroadcastGroup
case object LearnersGroup extends BroadcastGroup

sealed trait Action
case class Send[V](msg: Message[V]) extends Action
case class Broadcast[V](msg: Message[V], broadcastGroup: BroadcastGroup) extends Action
case object Noop extends Action
