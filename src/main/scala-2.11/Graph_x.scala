/**
  * Created by AntonioFischetti
  */

import java.io.{FileInputStream, FileOutputStream, ObjectInputStream, ObjectOutputStream}
import java.nio.file.{Files, Paths}

import io.plasmap.parser.OsmParser
import org.apache.log4j.{Level, Logger}
import org.apache.spark.graphx._
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object Graph_x {


  def main(args: Array[String]): Unit = {


    val pathString = "/Users/AntonioFischetti/desktop/Acireale.osm"

    Logger.getLogger("org").setLevel(Level.OFF) //Disable console spark
    Logger.getLogger("akka").setLevel(Level.OFF)

    val _listObjWay = parserWay(pathString) // ObjectWay
    val _listVertex = createListNodeWayVertex(_listObjWay) // Vertex (Long:count,Long:idWay)
    val _listEdge = createListNodeWayEdge(_listObjWay,_listVertex) // Edge Edge(NodeId:Long,NodeIdDest:Long,idWay:Long)
     val _listObjNodeWithLatAndLon = parserNode(pathString,_listVertex) //Contain (idNode:Long,Latitude:Long,Longitude:Long) List of nodeObject

    //Load Shortest Path From File
    val _dijkstraObjList = scala.collection.mutable.MutableList[_dijkstraObj]()
    getPathDijkstraFromFile().foreach(objDjk=>{_dijkstraObjList.+=(objDjk)})
    //_dijkstraObjList.map(a=>{println(a.idSrc,a.idDst) ; a})


   val config = new SparkConf()
   config.setMaster("local[2]")
   config.setAppName("Graph_x")
   val sc = new SparkContext(config)


    val nodesRDD: RDD[(VertexId, Long)] = sc.parallelize(_listVertex)
    val relRDD: RDD[Edge[Long]] = sc.parallelize(_listEdge)

    val graph:Graph[VertexId, Long] = Graph(nodesRDD, relRDD)



    val src = 455 //Via tomadio
    val dst = 0

   //Eseguire Dijkstra solo se non esiste già il cammino minimo nella lista di path
 if(_dijkstraObjList.map(djkObj=>djkObj.idSrc).distinct.filter(_==src).toList.isEmpty) {
   //dijkstra(graph,src,dst).foreach(objDjk => _dijkstraObjList.+=(objDjk))
   //saveDijkstraPathFile(_dijkstraObjList.toList) //save new list path
 }


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

  def createListNodeWayVertex(_listObjWay: List[_wayObject]) : List[(Long,Long)] = {

    val _listVertex = scala.collection.mutable.MutableList[(Long,Long)]() // List Object Way
    val _tmpVertex = scala.collection.mutable.MutableList[(Long)]()

   _listObjWay.foreach({
     _listNodeObj1 =>
       _listObjWay.foreach({
         _listNodeObj2 =>
           if(_listNodeObj1.idWay != _listNodeObj2.idWay){
              val intersection = _listNodeObj1.nodeList.intersect(_listNodeObj2.nodeList)
             if(intersection.nonEmpty){
               intersection.foreach({
                 idNode => _tmpVertex.+=(idNode)
               })
             }

           }
       })

   })
    var cont = 0
    _tmpVertex.distinct.foreach({
      idNode => _listVertex.+=((cont,idNode))
        cont = cont + 1
    })

    _listVertex.toList
  }


 def createListNodeWayEdge(_listObjWay:List[_wayObject] , _listVertex:List[(Long,Long)]) : List[Edge[Long]] = {

   val _arrayEdge = scala.collection.mutable.MutableList[Edge[Long]]()
   val _tmpList = scala.collection.mutable.MutableList[List[(Long,Long)]]()


_listObjWay.foreach({
  _listNodeObj =>
   val intersect = _listNodeObj.nodeList.intersect(_listVertex.map(node=>node._2))

    if(intersect.size > 1 ) {
      val listSplitted = splitList(intersect)

      for (element <- listSplitted) {

        val src = _listVertex.filter(_._2 == element._1).head._1
        val dst = _listVertex.filter(_._2 == element._2).head._1

        val weight = getWeight(_listNodeObj.nodeList, element._1, element._2, 0)

        _arrayEdge.+=(Edge(src, dst, 1))

      }
      if (_listNodeObj.oneWay != "yes") {
        val listSplitted2 = splitList(intersect.reverse)

        for (element2 <- listSplitted2) {

          val src2 = _listVertex.filter(_._2 == element2._1).head._1
          val dst2 = _listVertex.filter(_._2 == element2._2).head._1

          val weight2 = getWeight(_listNodeObj.nodeList, element2._1, element2._2, 0)

          _arrayEdge.+=(Edge(src2, dst2, 1))

        }

      }
    }
})






   _arrayEdge.toList

}


  def dijkstra(graph: Graph[Long,Long], srcId:Long , destId:Long ) : List[_dijkstraObj] = {


    println("Start Dijkstra Src,Dst: " + srcId +","+ destId)

    val initialGraph : Graph[(Double, List[VertexId]), Long] =
      graph.mapVertices((id, _) =>
        if (id == srcId) (0.0, List[VertexId](srcId))
        else (Double.PositiveInfinity, List[VertexId]()))



    // initialize all vertices except the root to have distance infinity
    val sourceId: VertexId = srcId
   // val initialGraph : Graph[(Double, List[VertexId]), Long] = graph.mapVertices((id, _) => if (id == sourceId) (0.0, List[VertexId](sourceId)) else (Double.PositiveInfinity, List[VertexId]()))

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




    println("Stop Dijkstra Src,Dst: " + srcId +","+ destId)

    sssp.vertices.collect.toList.map(a => {

      val obj = _dijkstraObj(srcId, a._1, a._2._2, a._2._1.toLong)

      obj
    })

  }

  def splitList(listaEl: List[Long]): List[(Long, Long)] = listaEl match {

    case Nil => throw new NoSuchElementException
    case first :: second :: Nil => List((first,second))
    case first :: second :: tail => (first, second) :: splitList(second :: tail)

  }

  def getWeight(listaEl: List[Long], start:Long, end:Long , acc:Int ): Long = listaEl match {

    case Nil => throw new NoSuchElementException
    case first  :: Nil => acc
    case first  :: tail => {
      val acc2 = acc + 1
      if (first == start) getWeight(tail,start,end,0)
      if (first == end) acc2
      else getWeight(tail,start,end,acc2)
    }

  }

  def getPathDijkstraFromFile(): List[_dijkstraObj] = {

    if ( Files.exists(Paths.get("dijkstra")) ){
    val ois = new ObjectInputStream(new FileInputStream("dijkstra"))

    val obj = ois.readObject.asInstanceOf[List[_dijkstraObj]]
    ois.close()
    obj
    }
    else List()

  }

  def saveDijkstraPathFile(listObjDjk:List[_dijkstraObj]): Unit = {

    val oos = new ObjectOutputStream(new FileOutputStream("dijkstra"))

    oos.writeObject(listObjDjk)
    oos.close()

  }

} //close object

class VertexProperty()
class EdgeProperty()

