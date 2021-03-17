package paxos

/*
  OOP way: https://github.com/cocagne/scala-composable-paxos/blob/master/src/main/scala/com/github/cocagne/composable_paxos/Proposer.scala
 */
/*
  This will just handle the internal state of the proposer. The question is should we encapsulate these inside a case class or should we make this as as a data type and put the method in the request
    Returning an Action will separate the IO with the value.
    Then, we can use the Proposer as a model, and make a trait for ProposerOps to separate the model and the interpreter.
    Instead of having the  WRAPPING THE value of the proposer inside the case class with a `var` keyword. We can pass it in as a state, treat it inside of a Kleisli or a State monad.
 */
case class Proposer[V](value: V, proposalId: ProposalId, quorumSize: Int)
case class Acceptor[V](promiseId: Option[ProposalId], maybeAccepted: Option[(ProposalId, V)])
case class Learner[V](quorumSize: Int, maybeFinalValue: Option[V], acceptedIdSoFar: Map[V, Int]) {

  def incrementAcceptedIdSoFar(value: V): Learner[V] =
    copy(
      acceptedIdSoFar = acceptedIdSoFar ++ Map(
        value -> (acceptedIdSoFar.getOrElse(value, 0) + 1)
      )
    )
}
