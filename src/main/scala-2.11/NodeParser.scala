import io.plasmap.model.OsmObject
/**
  * Created by AntonioFischetti on 12/05/16.
  */
class NodeParser() {


  def _isNode(element: Option[OsmObject]): Boolean = {
    element.fold(false)(el => {
      if (el.isNode) true else false
    })
  }

  def getNodeParsed(element: Option[OsmObject] , _listNode:List[(Long,Long)]): _nodeObject = {



    val osmNode = element.map(osmObj => osmObj.nodeOption.map(osmNode => osmNode)).get

    val idOsmNode = osmNode.map( nodeObj => nodeObj.id.value).get

    var app:(Long,Double,Double) = (0,0,0)

    _listNode.foreach(
       idNode => {

        if(idOsmNode == idNode._1) {
          val latitude = osmNode.map(nodeObj => nodeObj.point.lat).get
          val longitude = osmNode.map(nodeObj => nodeObj.point.lon).get
         app = (idNode._1,latitude,longitude)
        }
       }
    )
    _nodeObject(app._1,app._2,app._3)




  }



}