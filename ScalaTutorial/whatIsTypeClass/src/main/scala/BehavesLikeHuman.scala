/*
  Step 3: Define the Object Interface
  This is the example for Object Inteface
  Usually this will be put into a util file for as an interface
 */
object BehavesLikeHuman {
  def speak[A](animal:A)(implicit instance:SpeakBehavior[A]): Unit = instance.speak(animal)
}
