import io.plasmap.model.OsmObject

/**
  * Created by AntonioFischetti on 19/05/16.
  */
class RelationClass {

  def _isRelation(element: Option[OsmObject]): Boolean = {
    element.fold(false)(el => {
      if (el.isRelation) true else false
    })
  }

  def getRelationParsed(elementOption: Option[OsmObject]): (String,String,String) = {

    elementOption.fold("","","")

  }

}
