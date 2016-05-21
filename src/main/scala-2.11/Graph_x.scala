import io.plasmap.parser.OsmParser
import org.apache.spark.graphx._
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by AntonioFischetti
  */
object Graph_x {


  def main(args: Array[String]): Unit = {

    //Configurazione Spark

    val config = new SparkConf()
    config.setMaster("local")
    config.setAppName("Graph_X")
    val sc = new SparkContext(config)

    val parser = OsmParser("/Users/AntonioFischetti/desktop/Acireale.osm")
    val parser2 = OsmParser("/Users/AntonioFischetti/desktop/Acireale.osm")

    val _listWay = scala.collection.mutable.MutableList[_wayObject]()
    val _listOSMNode = scala.collection.mutable.MutableList[_nodeObject]()
    val _listNodeWay = scala.collection.mutable.MutableList[String]()

    // Vertex and Link
    val _arrayVertex = scala.collection.mutable.MutableList[(Long,String)]()
    val _arrayEdge = scala.collection.mutable.MutableList[Edge[(String)]]()

    val way = new WayParser()
    val node = new NodeParser()
    val relation = new RelationClass()

    println("Start Parsing Way...")
    // Way get Primary - Secondary - Tertiary - Residential - Unclassified - Road - Living Street
    parser.foreach(elementOption => {
      if (way._isWay(elementOption)) {
        if (way._isHighway(elementOption)) {
            val _way = way.getWayParsed(elementOption)
            val _objWay = wayObject(_way._1,_way._2,_way._3,_way._4,_way._5)
          _way._5.foreach(
            stringNodo => {

              _listNodeWay.+=(stringNodo)

              //Create Vertex Graph
              _arrayVertex.+=((stringNodo.toLong, stringNodo))
            }
          )
            _listWay.+=(_objWay)


        }
      } /** Close isWay**/

      println("Start Parsing Node...")

      if(node._isNode(elementOption)){
        val _node = node.getNodeParsed(elementOption)
        val _objNode = nodeObject(_node._1,_node._2,_node._3)
        _listOSMNode.+=(_objNode)
      } /** Close isNode**/

      println("Start Parsing Relation...")

      if(relation._isRelation(elementOption)){
        relation.getRelationParsed(elementOption)
      }/** Close isRelation**/

    }
    ) /** Close ForeachParser**/


    println("Finish Parsing...")

    println("Start Creation Graph...")




  _listWay.foreach(
   element => {
       if(element.nodeList.size > 1) {

         val splitted:List[(String,String)] = split(element.nodeList)
         for (string <- splitted) {
           _arrayEdge.+=(Edge(string._1.toLong, string._2.toLong, element.idWay))
         }

       }
   }
  )
    val nodesRDD: RDD[(VertexId, String)] = sc.parallelize(_arrayVertex)
    val linkRDD: RDD[Edge[String]] = sc.parallelize(_arrayEdge)

    val graph  = Graph(nodesRDD, linkRDD)

    println("Finish Creation Graph...")





  } // Close Main
  def split(list: List[String]): List[(String, String)] = list match {

      case first :: second :: Nil => List((first, second))
      case first :: second :: tail => (first, second) :: split(second :: tail)

    }


}
class EdgeProperty()
class VertexProperty()
//case class Way(id:OsmId,lat:Double,long:Double)

/**
  **
 *class VertexProperty()
 *class EdgeProperty()
  **
 *class VertexProperty()
  *case class UserProperty(name: String) extends VertexProperty
  *case class ProductProperty(name: String, price: Double) extends VertexProperty
  *var graph: Graph[VertexProperty, String] = null
  **
 *case class _vertexObject(   idNode:Long,
  *tag:String
  *) extends VertexProperty
 *
 **/
