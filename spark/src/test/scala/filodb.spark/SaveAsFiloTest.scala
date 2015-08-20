package filodb.spark

import akka.actor.ActorSystem
import org.apache.spark.SparkContext
import org.apache.spark.sql.SQLContext

import filodb.core.cassandra.AllTablesTest

object SaveAsFiloTest {
  val system = ActorSystem("test")
}

/**
 * Test saveAsFiloDataset
 */
class SaveAsFiloTest extends AllTablesTest(SaveAsFiloTest.system) {
  // Setup SQLContext and a sample DataFrame
  val sc = new SparkContext("local[4]", "test")
  val sql = new SQLContext(sc)

  override def beforeAll() {
    createAllTables()
  }

  override def afterAll() {
    super.afterAll()
    sc.stop()
  }

  before {
    truncateAllTables()
  }

  // Sample data
  val jsonRows = Seq(
    """{"id":0,"sqlDate":"2015/03/15T15:00Z","monthYear":32015,"year":2015}""",
    """{"id":1,"sqlDate":"2015/03/15T16:00Z","monthYear":42015}""",
    """{"id":2,"sqlDate":"2015/03/15T17:00Z",                  "year":2015}"""
  )
  val dataDF = sql.read.json(sc.parallelize(jsonRows))

  import filodb.spark._

  it("should error out if dataset not created and createDataset=false") {
    intercept[DatasetNotFound] {
      sql.saveAsFiloDataset(dataDF, AllTablesTest.CassConfig, "gdelt1")
    }
  }

  it("should create missing columns and partition and write table") {
    sql.saveAsFiloDataset(dataDF, AllTablesTest.CassConfig, "gdelt1", createDataset=true)
    // Now read stuff back and ensure it got written
    // Also potentially check on columns, etc. make sure it all lines up
  }
}