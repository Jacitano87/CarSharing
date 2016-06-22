import io.plasmap.model.OsmObject


/**
  * Created by AntonioFischetti on 12/05/16.
  */

class WayParser() {


  def _isWay(element:Option[OsmObject]) : Boolean = {
    element.fold(false)( el => { if (el.isWay) true else false } )
  }

  def _isHighWay(element:Option[OsmObject]) : Boolean = {

    val osmTagKey = element.map(osmObj => osmObj.wayOption.map(osmWay => osmWay.tags.map(osmTag => osmTag))).get.get
    val highway = osmTagKey.filter(osmTag => osmTag.key =="highway")
    val typeHighway = highway.map(osmHighway => osmHighway.value)

    if(typeHighway.nonEmpty){
      if (typeHighway.head == "primary" || typeHighway.head == "secondary" ||
        typeHighway.head == "tertiary" || typeHighway.head == "residential" || typeHighway.head == "unclassified"
          ||typeHighway.head == "road" || typeHighway.head == "livingstreet" || typeHighway.head == "unclassified")
      {
      //  val osmListNodeId = element.map(osmObj => osmObj.wayOption.map(osmWay => osmWay.nds.map(osmId => osmId.value))).get.get
      //  if(osmListNodeId.size > 2) true
      //  else false
      true
      }
      else false
    }
    else false
  }

  def _parseObjectWay(element:Option[OsmObject]) : _wayObject = {

    val osmTagKey = element.map(osmObj => osmObj.wayOption.map(osmWay => osmWay.tags.map(osmTag => osmTag))).get.get

    //Type of highway
    val highway = osmTagKey.filter(osmTag => osmTag.key =="highway")
    val typeHighway = highway.map(osmHighway => osmHighway.value)
    val osmTypeHighWay = typeHighway.head


    //Oneway
    val osmOneWay = osmTagKey.filter(osmTag => osmTag.key =="oneway")
    val oneWay = osmOneWay.map(osmNameway => osmNameway.value)
    var _oneWay = "no"
    if(osmOneWay.nonEmpty) _oneWay = oneWay.head

    //NameWay
    val osmNameway = osmTagKey.filter(osmTag => osmTag.key =="name")
    val nameHighway = osmNameway.map(osmNameway => osmNameway.value)
    var _nameHighway = ""
    if  (nameHighway.nonEmpty) _nameHighway = nameHighway.head

    //ListNodeId
    val osmListNodeId = element.map(osmObj => osmObj.wayOption.map(osmWay => osmWay.nds.map(osmId => osmId.value))).get.get
    val osmIdWay = element.map(osmObj => osmObj.id.value).get


    wayObject(osmIdWay,_nameHighway,osmTypeHighWay,_oneWay,osmListNodeId)

  }





}
