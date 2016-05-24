import io.plasmap.parser.OsmParser

/**
  * Created by AntonioFischetti
  */
object Graph_x {


  def main(args: Array[String]): Unit = {

    //Configurazione Spark
    /**
      * val config = new SparkConf()
      * config.setMaster("local")
      * config.setAppName("Graph_X")
      * val sc = new SparkContext(config)
      **/





    val _listObjWay = parserWay("/Users/AntonioFischetti/desktop/Acireale.osm")
    val _listNodeWay = createListNodeWayVertex(_listObjWay)
    createListNodeWayEdge(_listObjWay)
    println("List Way Size: " + _listObjWay.size + "List Node Way Size: " + _listNodeWay.size)


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

  def createListNodeWayVertex(listWay: List[_wayObject]) : List[Long] = {

    println("Start Create Node List ...")
    val listNodeWay = listWay.map(wayObj => wayObj.nodeList.map( idNodo => idNodo))
        listNodeWay.flatMap(obj => obj).distinct



  }

def createListNodeWayEdge(listWay: List[_wayObject]) : Unit = {

 var listNodeWay = listWay.map(wayObj => wayObj.nodeList.map( idNodo => idNodo))

  listNodeWay.foreach(
    list => {
      list //.foldRight(z)
    }
  )

  println("listNodeWay: " +listNodeWay)

}






} //close object




