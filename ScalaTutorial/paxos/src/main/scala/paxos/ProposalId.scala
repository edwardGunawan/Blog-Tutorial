package paxos

case class ProposalId(uid: Long, machineId: Long) extends Ordered[ProposalId] {
  override def compare(that: ProposalId): Int =
    if (uid < that.uid) -1
    else if (uid == that.uid) {
      (machineId - that.machineId).toInt
    } else {
      1
    }
}
