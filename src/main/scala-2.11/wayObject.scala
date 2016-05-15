/**
  * Created by AntonioFischetti on 13/05/16.
  */

object wayObject {

  def apply(idWay:String, highway:String, nameWay:String, oneWay:String, nodeList:List[String] ) : _wayObject =
  _wayObject (idWay, highway, nameWay, oneWay , nodeList)

}
case class _wayObject(   idWay:String,
                       highway:String,
                       nameWay:String,
                        oneWay: String,
                      nodeList:List[String]
                  )


