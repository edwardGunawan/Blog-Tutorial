object main extends App{
  /*
    Using Object interface to call new behavior on dog
    Un-comment the part to call the object interface
   */
//  import SpeakLikeHuman._ // import all the implicit value instances
  val dog = Dog("Rover")
  // This is how you will evoke the object interface
//  BehavesLikeHuman.speak(dog) // the implicit value is retrived from the SpeakLikeHuman instance

  /*
    Using Object Syntax to call new behavior on dog
   */
  import SpeakBehaviorSyntax._ // import the SpeakBehaviorSyntaxOps
  import SpeakLikeHuman.dogSpeakLikeHuman // you also need to import the syntax
  dog.speak // this looks to the caller that dog has this behavior in the source code


}
