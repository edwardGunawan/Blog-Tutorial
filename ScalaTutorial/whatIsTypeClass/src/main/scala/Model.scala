sealed trait Animal
final case class Dog(name:String) extends Animal
final case class Cat(name:String) extends Animal
final case class Bird(name:String) extends Animal