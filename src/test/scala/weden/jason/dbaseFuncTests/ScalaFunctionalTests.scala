package weden.jason.dbaseFuncTests

import org.testng.annotations.Test
import org.scalatest.testng.TestNGSuite
import org.apache.log4j.{Logger, LogManager}
import org.squeryl.adapters.PostgreSqlAdapter
import java.util.Date
import org.squeryl.{Schema, Session}

class ScalaFunctionalTests extends TestNGSuite {
  private final val LOG: Logger = LogManager.getLogger(classOf[ScalaFunctionalTests])

  @Test(invocationCount = 1, description = "Do select statement against postgres database and print out result.")
  def quickTest() {
    import org.squeryl.PrimitiveTypeMode._
    import Library._

    Class.forName("org.postgresql.Driver");
    val session = Session.create(java.sql.DriverManager.getConnection("jdbc:postgresql://localhost:5432/postgres", "postgres", "postgres"), new PostgreSqlAdapter)

    using(session) {
      val c = from(customer)(c => where(c.last_name === "weden") select (c))
      c.foreach(cust => LOG.info(cust.first_name))
    }
  }


  object Library extends Schema {
    //When the table name doesn't match the class name, it is specified here :
    val customer = table[Customer]("customer")
  }

}

class Customer(
                val first_name: String,
                val last_name: String,
                val address: String,
                val city: String,
                val country: String,
                val birth_date: Date) {
}