/**
  * Created by AntonioFischetti on 17/05/16.
  */
object nodeObject {

  def apply(idNode:Long, idVertex:Long, latitude:Double, longitude:Double ) : _nodeObject =
    _nodeObject (idNode, idVertex, latitude, longitude)

}

case class _nodeObject(   idNode:Long,
                          idVertex:Long,
                          latitude:Double,
                          longitude:Double
                     )