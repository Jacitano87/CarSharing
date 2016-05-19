/**
  * Created by AntonioFischetti on 17/05/16.
  */
object nodeObject {

  def apply(idNode:String, latitude:String, longitude:String ) : _nodeObject =
    _nodeObject (idNode, latitude, longitude)

}

case class _nodeObject(   idNode:String,
                          latitude:String,
                          longitude:String
                     )