import io.plasmap.parser.OsmParser
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by AntonioFischetti
  */
object Graph_x {


  def main(args: Array[String]): Unit = {

    //Configurazione Spark

    //val listNodes = scala.collection.mutable.MutableList[Node]()
   // val wayList = scala.collection.mutable.MutableList[Way]()
  //  val wayObj = wayObject(9000,List(1,2,3),"Primaria","Via minkia",true)
    val config = new SparkConf()
    config.setMaster("local")
    config.setAppName("Graph_X")
    val sc = new SparkContext(config)

    val parser = OsmParser("/Users/AntonioFischetti/desktop/Acireale.osm")
    val parser2 = OsmParser("/Users/AntonioFischetti/desktop/Acireale.osm")

    val _listWay = scala.collection.mutable.MutableList[_wayObject]()
    val _listNodeTemp = scala.collection.mutable.MutableList[_nodeObject]()
    val _listNodeFinal = scala.collection.mutable.MutableList[_nodeObject]()

    val way = new WayParser()
    val node = new NodeParser()

    println("Start Parsing data...")

    // Way get Primary - Secondary - Tertiary - Residential - Unclassified - Road - Living Street
    parser.foreach(elementOption => {
      if (way._isWay(elementOption)) {
        if (way._isHighway(elementOption)) {
            val _way = way.getWayParsed(elementOption)
            val _objWay = wayObject(_way._1,_way._2,_way._3,_way._4,_way._5)

            _listWay.+=(_objWay)

        }
      } /** Close isWay**/

      if(node._isNode(elementOption)){
        val _node = node.getNodeParsed(elementOption)
        val _objNode = nodeObject(_node._1,_node._2,_node._3)
        _listNodeTemp.+=(_objNode)
      } /** Close isNode**/
    }
    ) /** Close ForeachParser**/

    println("Finish Parsing data...")


  } // Close Main
}
    /**
      * parser2.foreach(elementOption => {
      * if(node._isNode(elementOption)){
      * //  node._a(elementOption)
      * //println("Is A Node")
      * }
      * }
      * )
      * }
      * }
    **/

//case class Way(id:OsmId,lat:Double,long:Double)

/**
  * parser.foreach(elementOption =>
  * elementOption.fold(println("no parse")){ //fold la applica solo
  * element =>
  * if(element.isNode)
  * element.nodeOption.fold(println("Non funge")) {
  * node => {
  * // listNodes += Node(element.id,node.point.lat,node.point.lon,element.tags)
  * //println(Node(element.id,node.point.lat,node.point.lon,element.tags))
  * }
  **
  *}
  *if(element.isWay)
  *element.wayOption.fold(println("Nessun Elemento")){
  *node => {
  *node.wayOption.fold(println("Nessun Elemento")){
  *element => {
  *element.wayOption.fold(println("Nessun Elemento")){
  *ele => {
  *ele.tags.headOption.fold(println("Nes")){
  *el => {
  *if(el.key == "highway" && el.value == "primary") println(el.value)
  **
  *if(el.key == "highway" && el.value == "secondary") println(el.value)
  **
  *if(el.key == "highway" && el.value == "tertiary") println(el.value)
  **
 *if(el.key == "highway" && el.value == "residential") println(el.value)
  **
 *if(el.key == "highway" && el.value == "unclassified") println(el.value)
  **
 *if(el.key == "highway" && el.value == "road") println(el.value)
  **
 *if(el.key == "highway" && el.value == "livingstreet") println(el.value)
  *}
  *}
  *}
  * }
  **
  *}
 **
 *}
  *}
  * }
  **
  * }
  **
  * )
  **
  *
  * }
  **
  *
 *}
 **
 *case class NodeGraph(id:OsmId,
  *tags:List[OsmTag],
  *nodeOption:Option[OsmNode],
  *relationOption:Option[OsmRelation],
  *user:Option[OsmUser],
  *version:OsmVersion,
  *wayOption:Option[OsmWay])
 **
 *case class Node(id:OsmId,
  *lat:Double,
  *lon:Double,
  *tags:List[OsmTag]
  *)
 **
 *case class Way(id:OsmId)
 **
 *case class Relation()
  *
  **/