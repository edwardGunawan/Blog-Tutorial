package paxos

import cats.data.State
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class LearnerOpsSpec extends AnyWordSpec with Matchers with LearnerOps {
  "LearnerOps" should {
    "handleAccepted finding a value that is over majority will announce consensus" in {
      val learner = Learner(quorumSize = 6, maybeFinalValue = None, acceptedIdSoFar = Map(6 -> 3))
      val msg = MessageAccepted(proposalId = ProposalId(1L, 2L), value = 6)

      handleAccepted(msg).runS(learner).value.maybeFinalValue.get must equal(6)
    }
    "handleAccepted that doesn't have any majority will just be a noop" in {
      val learner = Learner(quorumSize = 6, maybeFinalValue = None, acceptedIdSoFar = Map(6 -> 2))
      val msg = MessageAccepted(proposalId = ProposalId(1L, 2L), value = 3)

      handleAccepted(msg).runS(learner).value.maybeFinalValue must equal(None)
    }
    "getValue works" in {
      val learner = Learner(quorumSize = 6, maybeFinalValue = None, acceptedIdSoFar = Map(6 -> 3))
      val msg = MessageAccepted(proposalId = ProposalId(1L, 2L), value = 6)
      val state = for {
        _ <- handleAccepted(msg)
        value <- getValue
      } yield value

      state.runA(learner).value must equal(Some(6))

    }
  }
}
