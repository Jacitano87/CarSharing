/**
  * Created by AntonioFischetti on 17/05/16.
  */
object nodeObject {

  def apply(idNode:Long, latitude:Double, longitude:Double ) : _nodeObject =
    _nodeObject (idNode, latitude, longitude)

}

case class _nodeObject(   idNode:Long,
                          latitude:Double,
                          longitude:Double
                     )