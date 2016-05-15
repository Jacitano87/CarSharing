import io.plasmap.model.{OsmId, OsmObject, OsmTag}

/**
  * Created by AntonioFischetti on 12/05/16.
  */
class NodeParser() {

  val listNodes = scala.collection.mutable.MutableList[Node]()

  def _isNode(element: Option[OsmObject]): Boolean = {
    element.fold(false)(el => {
      if (el.isNode) true else false
    })
  }

  def _a(elementOption: Option[OsmObject]): Unit = {
    elementOption.fold(println("no parse")) {
      element =>
        if (element.isNode)
          element.nodeOption.fold(println("Non funge")) {
            node => {
              listNodes += Node(element.id, node.point.lat, node.point.lon, element.tags)
              println("Nodo" + Node(element.id, node.point.lat, node.point.lon, element.tags))
            }

          }
    }
  }


  case class Node(id: OsmId,
                  lat: Double,
                  lon: Double,
                  tags: List[OsmTag]
                 )

}