/*
    Step 1: Define the trait for the behavior that you want to override
 */
trait SpeakBehavior[A] {
  def speak(a:A):Unit
}

/*
    Step 2: Define the instance of the behavior. This is where you will put more concrete stuff like implementation,
    the type that you want your new behavior to have. In this case, it is Dog type in the Model.scala file
 */
object SpeakLikeHuman {
  implicit val dogSpeakLikeHuman: SpeakBehavior[Dog] = new SpeakBehavior[Dog] {
    def speak(a: Dog): Unit = {
      println(s"I'm a Dog, my name is ${a.name}")
    }
  }
}