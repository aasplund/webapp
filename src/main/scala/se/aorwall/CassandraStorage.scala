package se.aorwall

import scala.collection.immutable.List
import scala.concurrent.Future
import me.prettyprint.cassandra.serializers.StringSerializer
import me.prettyprint.cassandra.serializers.UUIDSerializer
import me.prettyprint.cassandra.service.CassandraHostConfigurator
import me.prettyprint.cassandra.service.template.ThriftColumnFamilyTemplate
import me.prettyprint.cassandra.utils.TimeUUIDUtils
import me.prettyprint.hector.api.factory.HFactory
import se.aorwall.model.Task
import java.util.UUID
import me.prettyprint.hector.api.ddl.ComparatorType
import me.prettyprint.hector.api.ddl.KeyspaceDefinition
import me.prettyprint.cassandra.service.ThriftKsDef
import java.util.Arrays

object CassandraStorage extends Storage[String, Task] {
  
  val cluster = HFactory.getOrCreateCluster("TestCluster", new CassandraHostConfigurator("localhost:9160"))
  
  def createSchema() {
    val cfDef = HFactory.createColumnFamilyDefinition("TestKeyspace", "Tasks")
    val newKeyspace = HFactory.createKeyspaceDefinition("TestKeyspace", ThriftKsDef.DEF_STRATEGY_CLASS, 1, Arrays.asList(cfDef))
    cluster.addKeyspace(newKeyspace, true)
  }
  
  val keyspaceDef = cluster.describeKeyspace("TestKeyspace")
  if (keyspaceDef == null) {
    createSchema()
  }
  
  val keyspace = HFactory.createKeyspace("TestKeyspace", cluster)  
  val template = new ThriftColumnFamilyTemplate[UUID, String](keyspace, "Tasks", UUIDSerializer.get(), StringSerializer.get())

  def create(task: Task): Future[String] = {  
    val id = TimeUUIDUtils.getTimeUUID(task.timestamp)
    val updater = template.createUpdater(id)
    updater.setLong("timestamp", task.timestamp)
    updater.setString("title", task.title)
    updater.setBoolean("isDone", task.isDone)
    template.update(updater)
    Future.successful(id.toString)
  }

  def read(id: String): Future[Task] = {
    val uuid = UUID.fromString(id)
    val res = template.queryColumns(uuid)
    if (res.hasResults()){
      val timestamp = res.getLong("timestamp")
      val title = res.getString("title")
      val isDone = res.getBoolean("isDone")
      Future.successful(Task(timestamp, title, isDone))  
    } else {
      Future.failed(new IllegalArgumentException("No task found with id: " + id))
    }
    
  }

  def readAll(): Future[List[Task]] = { 
    null
  }

  def update(task: Task): Future[Unit] = { 
    val id = TimeUUIDUtils.getTimeUUID(task.timestamp)
    val res = template.queryColumns(id)
    if(res != null){
      create(task)
      Future.successful()
    } else {
      Future.failed(new IllegalArgumentException("No task found with id: " + id))
    }
  }

  def delete(id: String): Future[Unit] = { 
    val uuid = UUID.fromString(id)
    template.deleteRow(uuid)
    Future.successful()
  }
  
}