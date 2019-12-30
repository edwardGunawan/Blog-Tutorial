/*
  Step 3: Define the interface syntax.

  This is a way to implicitly call the instance to the caller
 */
object SpeakBehaviorSyntax {
  implicit class speakBehaviorOps[A](animal:A){
    def speak(implicit instance:SpeakBehavior[A]):Unit = instance.speak(animal)
  }

}
