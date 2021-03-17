package paxos

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import cats.implicits._

class AcceptorsOpsSpec extends AnyWordSpec with Matchers with AcceptorOps {
  "AcceptorsOps" should {
    "handlePrepare message return Noop that has a promise message that is bigger than current propose message" in {
      val acceptor = Acceptor[Int](promiseId = Some(ProposalId(uid = 12L, machineId = 1L)), maybeAccepted = None)
      val messagePrepare = MessagePrepare(proposalId = ProposalId(uid = 1L, machineId = 2L), value = 2)

      val (actualAcceptor, action) = handlePrepare(messagePrepare).run(acceptor).value

      action must equal(Noop)
      actualAcceptor must equal(acceptor)

    }

    "handlePrepare message return MessagePromise to the proposed message that has a higher proposalId than the current promised message" in {
      val acceptor = Acceptor[Int](promiseId = Some(ProposalId(uid = 1L, machineId = 1L)), maybeAccepted = None)
      val higherProposalId = ProposalId(uid = 2L, machineId = 2L)
      val messagePrepare = MessagePrepare(proposalId = higherProposalId, value = 2)

      val (actualAcceptor, action) = handlePrepare(messagePrepare).run(acceptor).value

      action must equal(Send(MessagePromise(proposalId = higherProposalId, maybeAccepted = None)))
      actualAcceptor must equal(acceptor.copy(promiseId = Some(higherProposalId)))
    }

    "handlePrepare message will return MessagePromise if it doesn't have any promise message yet" in {
      val acceptor = Acceptor[Int](promiseId = None, maybeAccepted = None)
      val higherProposalId = ProposalId(uid = 2L, machineId = 2L)
      val messagePrepare = MessagePrepare(proposalId = higherProposalId, value = 2)

      val (actualAcceptor, action) = handlePrepare(messagePrepare).run(acceptor).value
      action must equal(Send(MessagePromise(proposalId = higherProposalId, maybeAccepted = None)))
      actualAcceptor must equal(acceptor.copy(promiseId = Some(higherProposalId)))
    }

    "handleAccept message will return Noop when the proposed accept message is less than the acceptor proposalId" in {
      val acceptor = Acceptor[Int](promiseId = Some(ProposalId(uid = 3L, machineId = 3L)), maybeAccepted = None)
      val higherProposalId = ProposalId(uid = 2L, machineId = 2L)
      val messageAccept = MessageAccept(proposalId = higherProposalId, value = 2)
      val (actualAcceptor, action) = handleAccept(messageAccept).run(acceptor).value
      action must equal(Noop)
      actualAcceptor must equal(acceptor)

    }

    "handleAccept message will return messageAccepted when the proposed promise message is equivalent or greater than the acceptor proposalId" in {
      val acceptor = Acceptor[Int](promiseId = Some(ProposalId(uid = 2L, machineId = 2L)), maybeAccepted = None)
      val equivalentProposalId = ProposalId(uid = 2L, machineId = 2L)
      val messageAccept = MessageAccept(proposalId = equivalentProposalId, value = 2)
      val (actualAcceptor, action) = handleAccept(messageAccept).run(acceptor).value
      action must equal(Send(MessageAccepted(proposalId = equivalentProposalId, value = 2)))
      actualAcceptor must equal(acceptor.copy(maybeAccepted = Some((equivalentProposalId, 2))))
    }

    "handleAccept message will return messageAccepted to the proposed accept message when there are no promised message in the acceptor" in {
      val acceptor = Acceptor[Int](promiseId = None, maybeAccepted = None)
      val equivalentProposalId = ProposalId(uid = 2L, machineId = 2L)
      val messageAccept = MessageAccept(proposalId = equivalentProposalId, value = 2)
      val (actualAcceptor, action) = handleAccept(messageAccept).run(acceptor).value
      action must equal(Send(MessageAccepted(proposalId = equivalentProposalId, value = 2)))
      actualAcceptor must equal(
        acceptor.copy(promiseId = Some(equivalentProposalId), maybeAccepted = Some((equivalentProposalId, 2)))
      )
    }
  }
}
