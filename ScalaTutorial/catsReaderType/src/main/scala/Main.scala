import cats.data._
/*
  If you want to run the function, you can check un comment the print statement to see the result.
 */

case class Repository(userDB:Map[Long, String], emailDB: Map[String, List[String]])

/*
  object Reader {
    def apply[A,B](f:A => B) : Reader[A,B] = ReaderT[Id, A, B](f)
   }
 */

// remember, you define all the function that accept the configuration as a parameter. Leave the injection and configuration until the last
object UserDB {
  type UserReader[A] = Reader[Repository, A]
  def getUser(id:Long):UserReader[Option[String]] = Reader{repo =>
    repo.userDB.get(id)
  }

  def getEmail(username:Option[String]): UserReader[List[String]] = Reader {repo =>
    repo.emailDB.getOrElse(username.fold("none")(st => st), List.empty[String])
  }

  // overhere we chain all the operation and validation together using Reader to produce large operation that accept a single config
  def checkIfEmailMatch(id:Long, email:String):UserReader[Boolean] = for {
    usernameOption <- getUser(id)
    emails <- getEmail(usernameOption)
  } yield {

    emails.contains(email)
  }


}


object Main extends App {
  val userDB = Map(
    1L -> "john",
    2L -> "jane",
    3L -> "kate"
  )
  val emailDB = Map(
    "john" -> List("something@gmail.com"),
    "jane" -> List("jane@yahoo.com", "jane@gmail.com"),
    "kate" -> List("kate@hotmail.com", "kate123@yahoo.com")
  )

  val repo = Repository(userDB,emailDB)

  // this is when you inject the configuration
  val res = UserDB.checkIfEmailMatch(2L, "jane@gmail.com").run(repo)
//  println(res)


  // Map example
  case class Cat(sound:String)
  val retrieveSound :Reader[Cat,String] = Reader {cat => cat.sound}
  val checkSound:Reader[Cat,Boolean] = retrieveSound.map(_ == "meow")

//  println(checkSound.run(Cat("bark")))

 // flatMap example
  val greet:Reader[Cat, String] = Reader{cat =>
    s"hello $cat"
  }

  val sound:Reader[Cat, String] = Reader{cat =>
    s"${cat.sound} ${cat.sound}"
  }

  val greetAndSound = for{
    g <- greet
    check <- checkSound
    s <- sound
  } yield {
    if(check) g + s else "sound is not right"
  }

  val result = greetAndSound.run(Cat("meow"))
//  println(result)
  val notRight = greetAndSound.run(Cat("bark"))
//  println(notRight)

}
