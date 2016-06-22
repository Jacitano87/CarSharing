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
import org.joda.time.{DateTime, Period}




object Graph_x {




  def main(args: Array[String]): Unit = {


    val pathString = "Acireale.osm"

    Logger.getLogger("org").setLevel(Level.OFF) //Disable console spark
    Logger.getLogger("akka").setLevel(Level.OFF)

    val _listObjWay = parserWay(pathString) // ObjectWay
    val _listVertex = createListNodeWayVertex(_listObjWay) // Vertex (Long:count,Long:idWay)
    val _listEdge = createListNodeWayEdge(_listObjWay,_listVertex) // Edge Edge(NodeId:Long,NodeIdDest:Long,idWay:Long)
    val _listObjNodeWithLatAndLon = parserNode(pathString,_listVertex) //Contain (idNode:Long,Latitude:Long,Longitude:Long) List of nodeObject


    //Load Shortest Path From File
    val _dijkstraObjList = scala.collection.mutable.MutableList[_dijkstraObj]()
    getPathDijkstraFromFile().foreach(objDjk=>{_dijkstraObjList.+=(objDjk)})

    println("Nodi Calcolati:" + _dijkstraObjList.map(a=>a.idSrc).distinct.size)



   val config = new SparkConf()
   config.setMaster("local")
   config.setAppName("Graph_x")
   val sc = new SparkContext(config)


    val nodesRDD: RDD[(VertexId, Long)] = sc.parallelize(_listVertex)
    val relRDD: RDD[Edge[Long]] = sc.parallelize(_listEdge)

    val graph:Graph[VertexId, Long] = Graph(nodesRDD, relRDD)

    println("NumVertex: " + graph.numVertices + " NumEdge: " + graph.numEdges )

    for( a <-0 to 5000) {
      println("Dijkstra n° " + a)
      val random = scala.util.Random

      // se voglio evitare il dijkstra
      // val src = 14
      // ogni volta che lo avvio mi calcola un dijkstra da un vertex random
     val src = _listVertex.toList(random.nextInt(_listVertex.size))._1
     //   val src = 664
      val dst = 100

      //Eseguire Dijkstra solo se non esiste già il cammino minimo nella lista di path
      if (_dijkstraObjList.map(djkObj => djkObj.idSrc).distinct.filter(_ == src).toList.isEmpty) {
        dijkstra(sc, graph, src, dst).foreach(objDjk => _dijkstraObjList.+=(objDjk))
        saveDijkstraPathFile(_dijkstraObjList.toList) //save new list path
      }



    }

  val randomTrips =  createRandomTrip(1000,_listObjNodeWithLatAndLon,_dijkstraObjList.toList,_listEdge,sc,graph)
 // val Shareability =  createShareabilityNetwork(randomTrips,5,10,_listEdge,_dijkstraObjList.toList)


    println("Dijkstra: " +_dijkstraObjList.size)

  sc.stop()


    //  println("Distance: "  + distanceFrom(37.6215537 , 15.1624967 , 37.6135414 , 15.1658417))
  }


  def parserNode(pathOsm: String , _listNode:List[(Long,Long)]) : List[_nodeObject] = {

    val _listObjNode = scala.collection.mutable.MutableList[_nodeObject]() // List Object Node
    val parserNode = OsmParser(pathOsm)

    val _nodeParser = new NodeParser()

    parserNode.foreach(
      node => {

        if(_nodeParser._isNode(node))
          if(_nodeParser.getNodeParsed(node,_listNode).idNode != 0 ){
            _listObjNode.+=(_nodeParser.getNodeParsed(node,_listNode))
          }

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
    val _arraIndices = scala.collection.mutable.MutableList[Int]()


    _listNodeObj.nodeList.indices.foreach(i => {
      intersect.foreach( idNode=> {
         if(idNode ==  _listNodeObj.nodeList(i))
           _arraIndices.+=(i)
      })
    })

    if (_listNodeObj.oneWay != "yes") {

      if (intersect.size > 1) {
        val listSplitted = splitList(intersect)
        val listIndices = splitWeight(_arraIndices.toList)

        var counter = 0
        var weight = 0
        for (element <- listSplitted) {

          val src = _listVertex.filter(_._2 == element._1).head._1
          val dst = _listVertex.filter(_._2 == element._2).head._1

          listIndices.indices.foreach(
            i => {
              if (i == counter) weight = listIndices(i)
            })

          _arrayEdge.+=(Edge(src, dst, weight))
          counter = counter + 1
          weight = 0

        }

        val listSplitted2 = splitList(intersect.reverse)
        val listIndices2 = splitWeightReverse(_arraIndices.reverse.toList)

        var counter2 = 0
        var weight2 = 0

        for (element2 <- listSplitted2) {

          val src2 = _listVertex.filter(_._2 == element2._1).head._1
          val dst2 = _listVertex.filter(_._2 == element2._2).head._1

          listIndices2.indices.foreach(
            i => {
              if (i == counter2) weight2 = listIndices2(i)
            })

          _arrayEdge.+=(Edge(src2, dst2, weight2))

          counter2 = counter2 + 1
          weight2 = 0


        }


      }
    }
    else
    {
      if (intersect.size > 1) {
        val listSplitted = splitList(intersect)
        val listIndices = splitWeight(_arraIndices.toList)

        var counter = 0
        var weight = 0
        for (element <- listSplitted) {

          val src = _listVertex.filter(_._2 == element._1).head._1
          val dst = _listVertex.filter(_._2 == element._2).head._1

          listIndices.indices.foreach(
            i => {
              if (i == counter) weight = listIndices(i)
            })

          _arrayEdge.+=(Edge(src, dst, weight))
          counter = counter + 1
          weight = 0

        }}
    }




})






   _arrayEdge.toList

}


  def dijkstra(sc: SparkContext, graph: Graph[Long,Long], srcId:Long , destId:Long ) : List[_dijkstraObj] = {

    println("Start Dijkstra Src,Dst: " + srcId +","+ destId)

   val initialGraph : Graph[(Double, List[VertexId]), Long] =
      graph.mapVertices((id, _) =>
        if (id == srcId) (0.0, List[VertexId](srcId))
         else (Double.PositiveInfinity, List[VertexId]()))

               initialGraph.unpersist(true)
    // initialize all vertices except the root to have distance infinity
    val sourceId: VertexId = srcId
//Int.MaxValue
    val sssp = initialGraph.pregel((Double.PositiveInfinity, List[VertexId]()), 20000, EdgeDirection.Out)(
      // vertex program
      (id, dist, newDist) => if (dist._1 < newDist._1) dist else newDist,

      // send message
      triplet => {
     //   println("src: " + triplet.srcId)
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


      if(a._2._2.isEmpty){
        val obj = _dijkstraObj(srcId, a._1, a._2._2, -1)
        obj
      }
      else {
        val obj = _dijkstraObj(srcId, a._1, a._2._2, a._2._1.toLong)
       // println(obj.idSrc + " Dest: " + obj.idDst + " Path:" + obj.path)
        obj
      }


    })

  }

  def splitList(listaEl: List[Long]): List[(Long, Long)] = listaEl match {

    case Nil => throw new NoSuchElementException
    case first :: second :: Nil => List((first,second))
    case first :: second :: tail => (first, second) :: splitList(second :: tail)

  }

  def splitWeight(listaEl: List[Int]): List[Int] = listaEl match {

    case Nil => throw new NoSuchElementException
    case first :: Nil => Nil
    case first :: second :: Nil =>  (second - first) :: splitWeight(second :: Nil)
    case first :: second :: tail => (second - first) :: splitWeight(second :: tail)

  }

  def splitWeightReverse(listaEl: List[Int]): List[Int] = listaEl match {

    case Nil => throw new NoSuchElementException
    case first :: Nil => Nil
    case first :: second :: Nil =>  (first - second) :: splitWeightReverse(second :: Nil)
    case first :: second :: tail => (first - second) :: splitWeightReverse(second :: tail)

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


  def distanceFrom(lat1:Double,lng1: Double,lat2:Double,lng2:Double) : Double = {
    val R = 6371 // km (change this constant to get miles)
    val dLat = (lat2-lat1) * Math.PI / 180
    val dLon = (lng2-lng1) * Math.PI / 180
    val a = Math.sin(dLat/2) * Math.sin(dLat/2) +
      Math.cos(lat1 * Math.PI / 180 ) * Math.cos(lat2 * Math.PI / 180 ) *
        Math.sin(dLon/2) * Math.sin(dLon/2)
    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a))
    val d = R * c
    if (d>1) Math.round(d)
    else if (d<=1) Math.round(d*1000)
    d

  }

  def maxMatchingWeighted(_listVertex:List[(Long,Long)] , _listEdge:List[Edge[Long]]) : Unit = {

    var _V = scala.collection.mutable.ArrayBuffer[Long]()
    var _E = scala.collection.mutable.ArrayBuffer[(Long,Long,Long)]()
    val _T = scala.collection.mutable.ArrayBuffer[(Long,Long)]()

    _listVertex.foreach( idVertex => _V.+=(idVertex._1))
    _listEdge.foreach( idEdge => _E.+=((idEdge.dstId.toLong,idEdge.srcId.toLong,idEdge.attr.toLong)))

    var maxEdge = -1L
    var source = -1L
    var destination = -1L

    var matching = true
while (matching) {

  _E.foreach(
    idEdge => {
      if (idEdge._3 >= maxEdge &&
        _V.contains(idEdge._1) &&
        _V.contains(idEdge._2)
      ) {

        maxEdge = idEdge._3
        source = idEdge._1
        destination = idEdge._2
      }
      else 0
    })
  if (source != -1 && destination != -1) {

    _V -= source
    _V -= destination
    _E.-=((source, destination, maxEdge))
    _T.+=((source, destination))

    source = -1
    destination = -1
    maxEdge = -1L
  }
  else matching = false


}
    println("MaxMatching: " + _T.size)
  }






  def createRandomTrip(numTrip:Int ,_objNode :List[_nodeObject], _dijkstraList:List[_dijkstraObj], listEdge: List[Edge[(Long)]] , sc: SparkContext, graph: Graph[Long,Long]) : List[_objTrip] = {

    val _tmpList = scala.collection.mutable.MutableList[_objTrip]()

    val random = scala.util.Random

    for( a <-1 to numTrip){
      //Prendo come partenza un nodo di cui ho già calcolato dijkstra
      val startIntersection = _dijkstraList.toList(random.nextInt(_dijkstraList.size)).idSrc
      var arriveIntersection = _dijkstraList.toList(random.nextInt(_dijkstraList.size)).idSrc

      while(startIntersection == arriveIntersection){
        arriveIntersection = _dijkstraList.toList(random.nextInt(_dijkstraList.size)).idSrc

      }

      //  val arriveIntersection = random.nextInt(_objNode.size)

    //println(_dijkstraList.map(a=>a.idSrc).distinct )

      val objStart = _objNode.filter(p=>p.idVertex == startIntersection)
      val objArrive = _objNode.filter(p=>p.idVertex == arriveIntersection)

      val tripOne =  _dijkstraList.filter(a=>a.idSrc == startIntersection).filter(p=>p.idDst == arriveIntersection)

      val timePath = getTimeTrip(tripOne.head.idSrc,tripOne.head.idDst,tripOne.head.path,listEdge)

      val tmptime = new DateTime("2016-01-01T10:00:00.000-00:00")
      val startTime = tmptime.plus(Period.minutes(random.nextInt(180)))
      val endTime = startTime.plus(Period.minutes(timePath._1.toInt))

      _tmpList.+=(_objTrip(a.toLong,startIntersection,arriveIntersection, objStart.head.latitude,objStart.head.longitude,objArrive.head.latitude,objArrive.head.longitude,startTime,endTime,timePath))
    }

    _tmpList.toList

  }



//Restituisce per tutte le intersezioni il tempo (1 = 1min)
  def getTimeTrip(srcId:Long , destId:Long , path:List[Long], listEdge: List[Edge[(Long)]] ) : (Long,List[(Long,Long,Long)]) = {

    var _tmpList = scala.collection.mutable.MutableList[(Long,Long,Long)]()
    var weightTmp:Long = 0
if(path.size > 1) {
  path.sliding(2).foreach(
    idNode => {
      val link = listEdge.filter(p => p.srcId == idNode.head).filter(p => p.dstId == idNode.tail.head)
      weightTmp = weightTmp + link.head.attr.toLong
      _tmpList.+=((link.head.srcId.toLong, link.head.dstId.toLong, weightTmp))
      }
    )
  (weightTmp,_tmpList.toList)
}
  else {
  (weightTmp,List())
}
  }

  def createShareabilityNetwork(trips:List[_objTrip] , waithTime: Long , deltaTime: Long , listEdge: List[Edge[(Long)]] , _dijkstraList:List[_dijkstraObj]) : Unit = {

    println("Start Creating Shareability ...")
    val _nodeShareability = scala.collection.mutable.MutableList[(Long,Long)]()
    val _edgeShareability = scala.collection.mutable.MutableList[Edge[(Long)]]()
    trips.foreach(
      tripOne => {
        trips.foreach(
          tripTwo =>{

            if(tripOne.idTrip != tripTwo.idTrip) {
              //Suppongo siano due diversi viaggi.

              //Ratti
              if (tripTwo.startTime.isBefore((tripOne.arrivalTime.plus(waithTime))) &&
                tripOne.startTime.isBefore(tripTwo.arrivalTime.plus(waithTime))
              )
              {
                //controllo se nel path di tripOne c'è il nodoStart del trip 2.
              val isInTrip = tripOne.pathTimed._2.filter(p => p._1 == tripTwo.nodeStart)
              if (isInTrip nonEmpty) {

                // controllo se il nodo partenza del secondo viaggio è contenuto nel primo.
                val tmpTime = tripOne.startTime
                val addTime = tripOne.pathTimed._2.filter(p => p._2 == tripTwo.nodeStart)

                var timeActualeTripOne = tmpTime
                val timeStartTripTwo = tripTwo.startTime

                if (addTime nonEmpty) {

                  timeActualeTripOne = tmpTime.plus(addTime.head._3)
                }

                val differenceTime = timeActualeTripOne.getMinuteOfDay - timeStartTripTwo.getMinuteOfDay

                //Se la differenza è minore del valore delta impostato allora è possibile condividerle
                if (differenceTime.abs <= deltaTime) {
                //  println("Trip1 S: " + tripOne.nodeStart + " D: " + tripOne.nodeDest + " Stime: " + tripOne.startTime + " Atime: " + tripOne.arrivalTime + " path: " + tripOne.pathTimed)
                //  println("Trip2 S: " + tripTwo.nodeStart + " D: " + tripTwo.nodeDest + " Stime: " + tripTwo.startTime + " Atime: " + tripTwo.arrivalTime + " path: " + tripTwo.pathTimed)

                //  println("ActualTimeT1: " + timeActualeTripOne + " timeStartTripTwo: " + timeStartTripTwo)
                //  println("Difference Trip1: " + tripOne.nodeStart + " Trip2: " + tripTwo.nodeStart + " -> " + differenceTime.abs)

                  //Calcolo distanza tra destinazione trip1 e destinazione trip2 (e viceversa)
                  val distBetwDestTripOneAndTripTwo =  _dijkstraList.filter(a=>a.idSrc == tripOne.nodeDest).filter(p=>p.idDst == tripTwo.nodeDest)
                  val distBetwDestTripTwoAndTripOne =  _dijkstraList.filter(a=>a.idSrc == tripTwo.nodeDest).filter(p=>p.idDst == tripOne.nodeDest)
                  if((distBetwDestTripOneAndTripTwo nonEmpty) && (distBetwDestTripTwoAndTripOne nonEmpty))
                  {

                    val timeBetwDestTripOneAndTripTwo = getTimeTrip(distBetwDestTripOneAndTripTwo.head.idSrc,distBetwDestTripOneAndTripTwo.head.idDst,distBetwDestTripOneAndTripTwo.head.path,listEdge)
                    val timeBetwDestTripTwoAndTripOne = getTimeTrip(distBetwDestTripTwoAndTripOne.head.idSrc,distBetwDestTripTwoAndTripOne.head.idDst,distBetwDestTripTwoAndTripOne.head.path,listEdge)


                      if(timeBetwDestTripOneAndTripTwo._1 <= deltaTime || timeBetwDestTripTwoAndTripOne._1 <= deltaTime){

                        var bestRoute = 0L
                        //Sommo path destinazioni
                        if(timeBetwDestTripOneAndTripTwo._1 <= timeBetwDestTripTwoAndTripOne._1) bestRoute = tripOne.pathTimed._1 + timeBetwDestTripOneAndTripTwo._1
                        else bestRoute = tripTwo.pathTimed._1 + timeBetwDestTripTwoAndTripOne._1

                        if(bestRoute <= tripOne.pathTimed._1 + tripTwo.pathTimed._1 )
                          {
                            val weigth = tripOne.pathTimed._1 + tripTwo.pathTimed._1
                            //println("TotalPath: " + bestRoute + " Sum: " + sum )
                            //println("Trip1 S: " + tripOne.nodeStart + " D: " + tripOne.nodeDest + " Stime: " + tripOne.startTime + " Atime: " + tripOne.arrivalTime + " path: " + tripOne.pathTimed)
                            //println("Trip2 S: " + tripTwo.nodeStart + " D: " + tripTwo.nodeDest + " Stime: " + tripTwo.startTime + " Atime: " + tripTwo.arrivalTime + " path: " + tripTwo.pathTimed)
                            //println("ActualTimeT1: " + timeActualeTripOne + " timeStartTripTwo: " + timeStartTripTwo)
                            //println("Difference Trip1: " + tripOne.nodeStart + " Trip2: " + tripTwo.nodeStart + " -> " + differenceTime.abs)


                            _nodeShareability.+=((tripOne.idTrip,tripOne.idTrip))
                            _edgeShareability.+=(Edge( tripOne.idTrip,tripTwo.idTrip,weigth))

                           // println("Condivisibile: " + tripOne.idTrip + " & " + tripTwo.idTrip + " Weight: " + weigth)

                          }



 }

                  }




                }

              }
            }
          }
          })

      })
    println("Stop Creating Shareability ...")
   val _vertexDistinct = _nodeShareability.distinct
    maxMatchingWeighted(_vertexDistinct.toList,_edgeShareability.toList)

  }




} //close object

class VertexProperty()
class EdgeProperty()

