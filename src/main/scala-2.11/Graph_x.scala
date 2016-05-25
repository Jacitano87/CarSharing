import io.plasmap.parser.OsmParser
import org.apache.spark.graphx._
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}
/**
  * Created by AntonioFischetti
  */
object Graph_x {


  def main(args: Array[String]): Unit = {

    val pathString = "/Users/AntonioFischetti/desktop/Acireale.osm"
    //Configurazione Spark

      val config = new SparkConf()
      config.setMaster("local")
      config.setAppName("Graph_X")
      val sc = new SparkContext(config)




    val _listObjWay = parserWay(pathString)
    val _listVertex = createListNodeWayVertex(_listObjWay)
    val _listEdge = createListNodeWayEdge(_listObjWay)

    val nodesRDD: RDD[(VertexId, Long)] = sc.parallelize(_listVertex)
    val relRDD: RDD[Edge[Long]] = sc.parallelize(_listEdge)


    val graph = Graph(nodesRDD, relRDD)

    println("NumVertex: " + graph.numVertices + " NumEdge: " + graph.numEdges )
    println("List Way Size: " + _listObjWay.size + " ListVertex: " + _listVertex.size + " ListEdge: " + _listEdge.size )

  }










  def parserWay(pathOsm: String) : List[_wayObject] = {

    println("Start Parsing ...")

    val parser = OsmParser(pathOsm)
    val _wayParser = new WayParser()
    val _listObjWayTmp = scala.collection.mutable.MutableList[_wayObject]() // List Object Way

    parser.foreach(
      optionOsmObject => {
        if (_wayParser._isWay(optionOsmObject) && _wayParser._isHighWay(optionOsmObject)){
          _listObjWayTmp.+=(_wayParser._parseObjectWay(optionOsmObject))
        }
      }

    ) //Close foreach
    println("Finish Parsing ...")
    _listObjWayTmp.toList

  }

  def createListNodeWayVertex(listWay: List[_wayObject]) : List[(Long,Long)] = {

    val _listVertex = scala.collection.mutable.MutableList[(Long,Long)]() // List Object Way

    val tmpList = listWay.map(wayObj => wayObj.nodeList.map( idNodo => idNodo))

    val _listNodeWay = tmpList.flatMap(obj=>obj)

    _listNodeWay.foreach(idNode => _listVertex.+=((idNode,idNode)))

    _listVertex.toList


  }


 def createListNodeWayEdge(_listObjWay:List[_wayObject]) : List[Edge[Long]] = {

   val _arrayEdge = scala.collection.mutable.MutableList[List[Edge[Long]]]()

   _listObjWay.foreach(
     way => {

       if (way.oneWay == "yes")

       _arrayEdge.+=(createListNodeWayEdgeForWay(way.nodeList,way.idWay))

       else
       {
         _arrayEdge.+=(createListNodeWayEdgeForWay(way.nodeList,way.idWay))
         _arrayEdge.+=(createListNodeWayEdgeForWay(way.nodeList.reverse,way.idWay))

       }


     }
   )

   _arrayEdge.flatMap(obj => obj).toList
  }


  def createListNodeWayEdgeForWay(listNodeWay: List[Long], idWay:Long) : List[Edge[(Long)]] = {

      val _arrayEdge = scala.collection.mutable.MutableList[(Edge[(Long)])]()

      val listSplitted = splitList(listNodeWay)

      for (element <- listSplitted) {

        val app = (element._1,element._2,idWay)
        _arrayEdge.+=(Edge(element._1,element._2,idWay))
      }
    _arrayEdge.toList
}

  def splitList(listaEl: List[Long]): List[(Long, Long)] = listaEl match {

    case Nil => throw new NoSuchElementException
    case first :: second :: Nil => List((first,second))
    case first :: second :: tail => (first, second) :: splitList(second :: tail)

  }

  def getCouple(listaEl: List[Long]): List[(Long, Long)] = listaEl match {

    case Nil => throw new NoSuchElementException
    case first  :: Nil => List((first,first))
    case first  :: tail => (first, first) :: getCouple(first :: tail)

  }


} //close object

class VertexProperty()
class EdgeProperty()

