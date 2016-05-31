import java.util.Calendar

import io.plasmap.parser.OsmParser
import org.apache.spark.graphx._
import org.apache.spark.graphx.util.GraphGenerators
import org.apache.spark.rdd.RDD
import org.apache.log4j.Logger
import org.apache.log4j.Level
import org.apache.spark.storage.StorageLevel
import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.mutable
/**
  * Created by AntonioFischetti
  */
object Graph_x {


  def main(args: Array[String]): Unit = {

    val pathString = "/Users/AntonioFischetti/desktop/Acireale.osm"

    Logger.getLogger("org").setLevel(Level.OFF)
    Logger.getLogger("akka").setLevel(Level.OFF)

    val _listObjWay = parserWay(pathString) // ObjectWay
    val _listVertex = createListNodeWayVertex(_listObjWay) // Vertex (Long:contatore,Long:idWay)

    val _listEdge = createListNodeWayEdge(_listObjWay,_listVertex) // Edge Edge(NodeId:Long,NodeIdDest:Long,idWay:Long)
   // val _listObjNodeWithLatAndLon = parserNode(pathString,_listVertex) //Contain (idNode:Long,Latitude:Long,Longitude:Long) List of nodeObject

    //println(_listEdge)

    val config = new SparkConf()
    config.setMaster("local")
    config.setAppName("Graph_x")
    val sc = new SparkContext(config)





  val nodesRDD: RDD[(VertexId, Long)] = sc.parallelize(_listVertex)

  val relRDD: RDD[Edge[Long]] = sc.parallelize(_listEdge)



  val graph:Graph[VertexId, Long] = Graph(nodesRDD, relRDD , 0L)


    val now = Calendar.getInstance()
    val currentMinute = now.get(Calendar.MINUTE)
    val currentSecond = now.get(Calendar.SECOND)
    val currentMillisecond = now.get(Calendar.MILLISECOND)

println("Start Dijkstra: " + currentMinute + ":" +currentSecond + ":" + currentMillisecond)



    val graph2 = GraphGenerators.logNormalGraph(sc, numVertices = 5, numEParts = sc.defaultParallelism, mu = 4.0, sigma = 1.3).mapEdges(e => e.attr.toDouble)
   // graph2.edges.foreach(println)

    // initialize all vertices except the root to have distance infinity
    val sourceId: VertexId = 0
    val initialGraph : Graph[(Double, List[VertexId]), Long] = graph.mapVertices((id, _) => if (id == sourceId) (0.0, List[VertexId](sourceId)) else (Double.PositiveInfinity, List[VertexId]()))

    val sssp = initialGraph.pregel((Double.PositiveInfinity, List[VertexId]()), Int.MaxValue, EdgeDirection.Out)(
      // vertex program
      (id, dist, newDist) => if (dist._1 < newDist._1) dist else newDist,

      // send message
      triplet => {
        if (triplet.srcAttr._1 < triplet.dstAttr._1 - triplet.attr ) {
          Iterator((triplet.dstId, (triplet.srcAttr._1 + triplet.attr , triplet.srcAttr._2 :+ triplet.dstId)))
        } else {
          Iterator.empty
        }
      },

      // merge message
      (a, b) => if (a._1 < b._1) a else b)


    val now2 = Calendar.getInstance()
    val currentMinute2 = now2.get(Calendar.MINUTE)
    val currentSecond2 = now2.get(Calendar.SECOND)
    val currentMillisecond2 = now2.get(Calendar.MILLISECOND)

println("Stop Dijkstra: " + currentMinute2 + ":" +currentSecond2 + ":" + currentMillisecond2)

    println(sssp.vertices.collect.toList.filter(_._1 == 5))






  println("NumVertex: " + graph.numVertices + " NumEdge: " + graph.numEdges )




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

    val tmpList = listWay.map(wayObj => wayObj.idWay)

    var contatore = 0
    tmpList.foreach(
      idWay => {
        _listVertex.+=((contatore,idWay))
        contatore = contatore + 1
      })



    _listVertex.toList


  }


 def createListNodeWayEdge(_listObjWay:List[_wayObject] , _listVertex:List[(Long,Long)]) : List[Edge[Long]] = {

   val _arrayEdge = scala.collection.mutable.MutableList[Edge[Long]]()
   val _tmpList = scala.collection.mutable.MutableList[List[(Long,Long)]]()


   _listObjWay.map(wayObj => _tmpList.+=(wayObj.nodeList.map( idNodo => ( idNodo , wayObj.idWay )  )))



   _listObjWay.foreach({
     wayObj1 =>
       _listObjWay.foreach({
          wayObj2 =>

            wayObj1.nodeList.foreach(
              {
                var contatoreList1 = 1
                idNodoObj1 => {

                  wayObj2.nodeList.foreach(

                    idNodoObj2 => {

                      if (idNodoObj1 == idNodoObj2 && wayObj1.idWay != wayObj2.idWay) {
                      //  println("Contatore: " + contatoreList1)
                      //  println("NAMEobj1: " + wayObj1.nameWay + " obj2: " + wayObj2.nameWay)
                      //  println("IDobj1: " + wayObj1.idWay + " obj2: " + wayObj2.idWay + " idNodoComune: " + idNodoObj1)
                       val id1 = _listVertex.filter(_._2==wayObj1.idWay).map(idVertex=>idVertex._1).head
                        val id2 = _listVertex.filter(_._2==wayObj2.idWay).map(idVertex=>idVertex._1).head
                        _arrayEdge.+=(Edge(id1,id2,contatoreList1))
                      }
                    }

                  )

                }
                  contatoreList1 = contatoreList1 + 1
              }
            )



        })

   })

   _arrayEdge.toList

}

  
  def dijkstra(graph: Graph[Long,Long], srcId:Long , destID:Long ) : Unit = {

    val g = graph.mapVertices((id, _) =>
      if (id == srcId) Array(0.0, id)
      else Array(Double.PositiveInfinity, id)
    )

    val sssp = g.pregel(Array(Double.PositiveInfinity, -1))(
      (id, dist, newDist) => {

        if (dist(0) < newDist(0)) dist
        else newDist
      },
      triplet => {

        if (triplet.srcAttr(0) + triplet.attr < triplet.dstAttr(0)) {
          if(triplet.srcAttr(0) == destID) Iterator.empty
            else
          Iterator((triplet.dstId, Array(triplet.srcAttr(0) + triplet.attr, triplet.srcId)))
        }
        else {
          Iterator.empty
        }
      },
      (a, b) => {
        if (a(0) < b(0)) a
        else b
      }
    )

    println(sssp.vertices.collect.toList.filter( idNode => idNode._1 == destID).map(array=>array._2))
  }


} //close object

class VertexProperty()
class EdgeProperty()

