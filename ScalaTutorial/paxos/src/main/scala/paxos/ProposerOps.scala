package paxos

import cats.data.State

trait ProposerOps {

  def initialized(networkUid: Long): ProposalId = ProposalId(0L, networkUid)

  implicit def maxAcceptedId[V] = new Ordering[MessagePromise[V]] {
    // assuming that both has the option type
    override def compare(x: MessagePromise[V], y: MessagePromise[V]): Int =
      x.maybeAccepted.get._1.compareTo(y.maybeAccepted.get._1)
  }

  def isMajority(size: Int, quorumSize: Int): Boolean =
    size >= (quorumSize / 2 + 1)

  def sendPrepareProposal[V](message: MessagePrepare[V]): Action =
    Broadcast(message, AcceptorsGroup)

  // loop through the promise and if there is an accept get the largest accept proposal id number and broad cast it to
  // all the acceptor
  def handlePromise[V](
    messages: List[MessagePromise[V]]
  ): State[Proposer[V], Action] =
    State[Proposer[V], Action] { proposer =>
      if (isMajority(size = messages.length, quorumSize = proposer.quorumSize)) {

        // check if promise message contains an accepted message, and if so get the maximum proposalId
        val acceptedMessage = messages.filter {
          case MessagePromise(_, maybeAccepted) =>
            maybeAccepted.isDefined
          case _ => false
        }
        // if there are accepted language
        if (acceptedMessage.nonEmpty) {
          // set the proposer to the proposer to the accepted proposer
          val (id, v) = acceptedMessage.max.maybeAccepted.get
          // let the proposer be the accepted proposalId and broadcast them to the messageAccept
          (
            proposer.copy(value = v, proposalId = id),
            Broadcast(msg = MessageAccept(proposalId = id, value = v), AcceptorsGroup)
          )

        } else {
          (
            proposer,
            Broadcast(
              MessageAccept(
                proposalId = proposer.proposalId,
                value = proposer.value
              ),
              AcceptorsGroup
            )
          )
        }
      } else {
        (constructHigherProposalId(prevProposal = proposer), Noop)
      }

    }

  // check if the majority of the messages are accepted, then broadcast it to the learner
  def handleAccept[V](
    messages: List[MessageAccepted[V]]
  ): State[Proposer[V], Action] =
    State { proposer =>
      if (isMajority(messages.length, proposer.quorumSize)) { // broadcast it to the learner
        (
          proposer,
          Broadcast(
            msg = MessageAccepted(
              proposalId = proposer.proposalId,
              value = proposer.value
            ),
            LearnersGroup
          )
        )
      } else { // construct a higher proposerId
        (constructHigherProposalId(prevProposal = proposer), Noop)
      }
    }

  // constructing a higher proposalId
  def constructHigherProposalId[V](prevProposal: Proposer[V]): Proposer[V] =
    prevProposal.copy(
      proposalId = prevProposal.proposalId.copy(uid = prevProposal.proposalId.uid + 1L)
    )
}
