package paxos

/*
  Paxos algorithm consist of 5 messages (Prepare, Promise, Accept, Accepted, NoAcknowledgement) and 2 phases (Prepare, and Accept)
 */
sealed trait Message[V]
case class MessagePrepare[V](proposalId: ProposalId, value: V) extends Message[V]
case class MessagePromise[V](proposalId: ProposalId, maybeAccepted: Option[(ProposalId, V)]) extends Message[V]
case class MessageAccept[V](proposalId: ProposalId, value: V) extends Message[V]
case class MessageAccepted[V](proposalId: ProposalId, value: V) extends Message[V]
