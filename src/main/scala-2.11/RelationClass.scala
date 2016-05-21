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

  def getRelationParsed(elementOption: Option[OsmObject]): Unit = {

    elementOption.fold() {
       element => {

         element.tags.headOption.fold() {
           tag =>{
             //println("Tag: " +tag.key)

         }


         }
       }
    }

  }

}
