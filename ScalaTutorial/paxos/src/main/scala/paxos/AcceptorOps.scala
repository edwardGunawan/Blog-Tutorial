package paxos

import cats.data.State

trait AcceptorOps {

  // check if this message has already exist with the max, if it is smaller, return noop
  // if the message is bigger than set the PromiseMessage to that proposer
  // if there is already an accepted message, put the maybeAccepted message in the payload as an action
  def handlePrepare[V](msg: MessagePrepare[V]): State[Acceptor[V], Action] =
    for {
      acceptor <- State.get[Acceptor[V]]
      (newAcceptor, action) = acceptor.promiseId match {
        case Some(proposalId) if (msg.proposalId < proposalId) => (acceptor, Noop)
        case _ =>
          (
            acceptor.copy(promiseId = Some(msg.proposalId)),
            Send(
              MessagePromise(proposalId = msg.proposalId, acceptor.maybeAccepted)
            )
          )
      }
      _ <- State.set[Acceptor[V]](newAcceptor)
    } yield action

  // check if the message accept is equivalent to the same acceptedId, if it is then return the value
  // if it is not then ignore
  // what happen when they send an handleAccept value to an acceptor that doesn't have a promise value yet?
  // if it already accepted a value then it will also ignore the proposal, meaning it already accept the value.
  // if the proposalId is bigger than the current promisedId, then accepts the proposal.
  // if there is no promisedId, accept the current messageAccept proposer
  // we don't check if it has already accepted the value because it might be the previous algorithm iteration
  def handleAccept[V](msg: MessageAccept[V]): State[Acceptor[V], Action] =
    for {
      acceptor <- State.get[Acceptor[V]]
      (newAcceptor, action) = acceptor.promiseId match {
        case Some(proposalId) if (msg.proposalId < proposalId) =>
          (acceptor, Noop)
        case _ =>
          (
            acceptor.copy(promiseId = Some(msg.proposalId), maybeAccepted = Some((msg.proposalId, msg.value))),
            Send(MessageAccepted(proposalId = msg.proposalId, value = msg.value))
          )
      }
      _ <- State.set(newAcceptor)
    } yield action

}
