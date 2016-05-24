/**
  * Created by AntonioFischetti on 13/05/16.
  */

object wayObject {

  def apply(idWay:Long, nameWay:String, highway:String, oneWay:String, nodeList:List[Long] ) : _wayObject =
  _wayObject (idWay, nameWay, highway, oneWay , nodeList)

}
case class _wayObject(   idWay:Long,
                         nameWay:String,
                         highway:String,
                        oneWay: String,
                      nodeList:List[Long]
                  )


