package paxos

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class ProposerOpsSpec extends AnyWordSpec with Matchers with ProposerOps {
  "ProposerOps" should {
    "initialized to proposalId correctly" in {
      initialized(2L) must equal(ProposalId(0L, 2L))
    }

    "isMajority compare the majority of element correctly" in {
      isMajority(3, 10) must equal(false)
      isMajority(6, 10) must equal(true)
    }

    "sendPrepareProposal will send the prepare message to the AcceptorGroup" in {
      sendPrepareProposal(MessagePrepare(initialized(2L), 2)) must equal(
        Broadcast(msg = MessagePrepare(initialized(2L), 2), broadcastGroup = AcceptorsGroup)
      )
    }

    "receive handlePromise didn't reach majority returns noop" in {
      val proposer = Proposer(value = 2, proposalId = ProposalId(2L, 2L), quorumSize = 10)
      val lstOfMessage = (0 to 4).toList.map { _ =>
        MessagePromise[Int](proposalId = ProposalId(2L, 2L), None)
      }

      val (nextProposer, action) = handlePromise(lstOfMessage).run(proposer).value
      action must equal(Noop)
      nextProposer must equal(constructHigherProposalId(proposer))
    }

    "receive handlePromise reach majority and there are no accept message will broadcast the proposalId to the acceptors" in {
      val proposalId = ProposalId(2L, 2L)
      val proposer = Proposer(value = 2, proposalId = proposalId, quorumSize = 10)
      val lstOfMessage = (0 to 6).toList.map { _ =>
        MessagePromise[Int](proposalId = proposalId, None)
      }

      val (nextProposer, action) = handlePromise(lstOfMessage).run(proposer).value
      action must equal(
        Broadcast(msg = MessageAccept(proposalId = proposalId, value = 2), broadcastGroup = AcceptorsGroup)
      )
      nextProposer must equal(proposer)
    }

    "receive handlePromise that is reach majority and there is one accept message will broadcast THAT accept message to the acceptors" in {
      val proposalId = ProposalId(2L, 2L)
      val proposer = Proposer(value = 2, proposalId = proposalId, quorumSize = 10)
      val lstOfMessage = (0 to 5).toList.map { _ =>
        MessagePromise[Int](proposalId = proposalId, None)
      }
      val acceptedProposalId = ProposalId(1L, 3L)
      val oneAcceptMessage = MessagePromise[Int](proposalId = proposalId, maybeAccepted = Some((acceptedProposalId, 5))) :: lstOfMessage

      val (nextProposer, action) = handlePromise(oneAcceptMessage).run(proposer).value
      action must equal(
        Broadcast(msg = MessageAccept(proposalId = acceptedProposalId, value = 5), broadcastGroup = AcceptorsGroup)
      )
      nextProposer must equal(proposer.copy(proposalId = acceptedProposalId, value = 5))
    }

    "receive handlePromise that is reach majority and there is two accept message will broadcast the largest accept message to the acceptors" in {
      val proposalId = ProposalId(2L, 2L)
      val proposer = Proposer(value = 2, proposalId = proposalId, quorumSize = 10)
      val lstOfMessage = (0 to 5).toList.map { _ =>
        MessagePromise[Int](proposalId = proposalId, None)
      }
      val smallerAcceptedProposalId = ProposalId(1L, 3L)
      val biggerAcceptedProposalId = ProposalId(2L, 3L)

      val smallerAcceptedMessage =
        MessagePromise[Int](proposalId = proposalId, maybeAccepted = Some((smallerAcceptedProposalId, 5)))
      val biggerAcceptedMessage =
        MessagePromise[Int](proposalId = proposalId, maybeAccepted = Some((biggerAcceptedProposalId, 6)))
      val multipleAcceptMessage = smallerAcceptedMessage :: biggerAcceptedMessage :: lstOfMessage

      val (nextProposer, action) = handlePromise(multipleAcceptMessage).run(proposer).value
      action must equal(
        Broadcast(
          msg = MessageAccept(proposalId = biggerAcceptedProposalId, value = 6),
          broadcastGroup = AcceptorsGroup
        )
      )
      nextProposer must equal(proposer.copy(proposalId = biggerAcceptedProposalId, value = 6))
    }

    "receive handleAccept of a list of message that is over majority will broadcast the accepted message to the learnersGroup" in {
      val proposalId = ProposalId(2L, 2L)
      val proposer = Proposer(value = 2, proposalId = proposalId, quorumSize = 10)
      val lstOfMessage = (0 to 6).toList.map { _ =>
        MessageAccepted(proposalId = proposalId, 2)
      }

      val (nextProposer, action) = handleAccept(lstOfMessage).run(proposer).value
      action must equal(
        Broadcast(msg = MessageAccepted(proposalId = proposalId, value = 2), broadcastGroup = LearnersGroup)
      )
      nextProposer must equal(proposer)
    }

    "receive handleAccept of a list of message that is not over the majority will construct a higher proposalId" in {
      val proposalId = ProposalId(2L, 2L)
      val proposer = Proposer(value = 2, proposalId = proposalId, quorumSize = 10)
      val lstOfMessage = (0 to 4).toList.map { _ =>
        MessageAccepted(proposalId = proposalId, 2)
      }

      val (nextProposer, action) = handleAccept(lstOfMessage).run(proposer).value
      action must equal(
        Noop
      )
      nextProposer must equal(constructHigherProposalId(proposer))
    }

  }
}
