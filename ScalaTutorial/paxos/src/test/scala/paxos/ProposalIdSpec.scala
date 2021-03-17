package paxos

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class ProposalIdSpec extends AnyWordSpec with Matchers {
  "ProposalId" should {
    "comparing ordered works" in {
      val proposalIdOne = ProposalId(uid = 1L, machineId = 2L)
      val proposalIdTwo = ProposalId(uid = 2L, machineId = 3L)

      (proposalIdOne > proposalIdTwo) must equal(false)
    }
  }
}
