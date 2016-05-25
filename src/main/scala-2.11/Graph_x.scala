import io.plasmap.parser.OsmParser
import org.apache.spark.graphx._
/**
  * Created by AntonioFischetti
  */
object Graph_x {


  def main(args: Array[String]): Unit = {

    val pathString = "/Users/AntonioFischetti/desktop/Acireale.osm"


    val _listObjWay = parserWay(pathString) // ObjectWay
    val _listVertex = createListNodeWayVertex(_listObjWay) // Vertex (Long:idNodo,Long:idNodo)

    val _listEdge = createListNodeWayEdge(_listObjWay)     // Edge Edge(NodeId:Long,NodeIdDest:Long,idWay:Long)
    val _listObjNodeWithLatAndLon = parserNode(pathString,_listVertex) //Contain (idNode:Long,Latitude:Long,Longitude:Long) List of nodeObject

/**

    val config = new SparkConf()
    config.setMaster("local")
    config.setAppName("Graph_X")
    val sc = new SparkContext(config)

    val nodesRDD: RDD[(VertexId, Long)] = sc.parallelize(_listVertex)
    val relRDD: RDD[Edge[Long]] = sc.parallelize(_listEdge)

    val graph = Graph(nodesRDD, relRDD)

  println("NumWay: " + _listObjWay.size + "NumVertex: " + graph.numVertices + " NumEdge: " + graph.numEdges )

**/


  }


  def parserNode(pathOsm: String , _listNode:List[(Long,Long)]) : List[_nodeObject] = {

    val _listObjNode = scala.collection.mutable.MutableList[_nodeObject]() // List Object Node
    val parserNode = OsmParser(pathOsm)

    val _nodeParser = new NodeParser()

    parserNode.foreach(
      node => {
        if(_nodeParser._isNode(node))
          if(_nodeParser.getNodeParsed(node,_listNode).idNode != 0 )
          _listObjNode.+=(_nodeParser.getNodeParsed(node,_listNode))
      }
    )
    _listObjNode.toList
  }







  def parserWay(pathOsm: String) : List[_wayObject] = {

    println("Start Parsing ...")

    val parserWay = OsmParser(pathOsm)
    val _wayParser = new WayParser()
    val _listObjWayTmp = scala.collection.mutable.MutableList[_wayObject]() // List Object Way

    parserWay.foreach(
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

    val _listNodeWay = tmpList.flatMap(obj=>obj).distinct

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




} //close object

class VertexProperty()
class EdgeProperty()

