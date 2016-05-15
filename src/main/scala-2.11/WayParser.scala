import io.plasmap.model.OsmObject

/**
  * Created by AntonioFischetti on 12/05/16.
  */

class WayParser() {


  def _isWay(element:Option[OsmObject]) : Boolean = {
    element.fold(false)( el => { if (el.isWay) true else false } )
  }

  def _isHighway(element:Option[OsmObject]): Boolean = {

    element.fold(false) {
      el2 => el2.wayOption.fold(false) {
        element2 => element2.wayOption.fold(false) {
          ele => ele.tags.headOption.fold(false) {
            el => {
              if (el.key == "highway")
                {
                  if(el.value == "primary" || el.value == "secondary" ||
                     el.value == "tertiary"  || el.value == "unclassified" ||
                     el.value == "road" || el.value == "livingstreet") {
                    true
                  }
                  else false
                }
              else false
            }
          }
        }
      }

    }
  }


def getWayParsered(element:Option[OsmObject]) : (String,String,String,String,List[String]) = {

  val _listNodes = scala.collection.mutable.MutableList[String]()
  //var _listNodes = scala.collection.mutable.MutableList[String]()
  var _id = ""
  var _nameWay = ""
  var _highway = ""
  var _oneWay = "no"

  element.fold((_id,_nameWay,_highway,_oneWay,_listNodes.toList)){
      element2 => {
        element2.wayOption.fold(_id,_nameWay,_highway,_oneWay,_listNodes.toList) {
          element3 => {

            _id = element3.id.toString()

            element3.nds.foreach(
                nodi => {

                  _listNodes.+=(nodi.value.toString())
                }

            )

            element3.tags.foreach(
                f = element4 => {
                  if (element4.key == "name") { _nameWay = element4.value}
                  if (element4.key == "highway") { _highway = element4.value }
                  if (element4.key == "oneway") { _oneWay = element4.value }
                }
            )

            (_id,_nameWay,_highway,_oneWay,_listNodes.toList)


        }
        }
      }
  }
}














}

