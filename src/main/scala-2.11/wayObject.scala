/**
  * Created by AntonioFischetti on 13/05/16.
  */

object wayObject {

  def apply(idWay:String, nameWay:String, highway:String, oneWay:String, nodeList:List[String] ) : _wayObject =
  _wayObject (idWay, nameWay, highway, oneWay , nodeList)

}
case class _wayObject(   idWay:String,
                         nameWay:String,
                         highway:String,
                        oneWay: String,
                      nodeList:List[String]
                  )


