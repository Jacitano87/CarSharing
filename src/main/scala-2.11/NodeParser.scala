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

  def getNodeParsed(elementOption: Option[OsmObject]): (String,String,String) = {

    elementOption.fold("","","") {
      element =>
          element.nodeOption.fold("","","") {
            node => {
             if(node.tags.isEmpty) {
               (element.id.toString(), node.point.lat.toString(), node.point.lon.toString())
             }
              else
               {
                 ("","","")
               }
              //println("idNodo: " + element.id + " latNodo: " + node.point.lat + " lonNodo: " + node.point.lon)
            }

          }

    }
  }



}