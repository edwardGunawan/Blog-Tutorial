package paxos

import cats.data.State

trait LearnerOps {

  def isMajority(size: Int, quorumSize: Int): Boolean =
    size >= (quorumSize / 2 + 1)

  def handleAccepted[V](msg: MessageAccepted[V]): State[Learner[V], Unit] =
    for {
      _ <- State.modify[Learner[V]] { learner =>
        val newLearner = learner.incrementAcceptedIdSoFar(msg.value)
        if (isMajority(
              newLearner.acceptedIdSoFar(msg.value),
              newLearner.quorumSize
            )) { // if it is the majority set it as finalValue
          newLearner.copy(maybeFinalValue = Some(msg.value))
        } else { // noop
          newLearner
        }
      }
    } yield ()

  def getValue[V]: State[Learner[V], Option[V]] =
    for {
      learner <- State.get[Learner[V]]
    } yield learner.maybeFinalValue
}
