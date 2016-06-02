/**
  * Created by AntonioFischetti on 02/06/16.
  */
object dijkstraObj {

  def apply(idSrc:Long, idDst:Long, path:List[Long], weight:Long) : _dijkstraObj =
    _dijkstraObj (idSrc, idDst, path, weight)

}
case class _dijkstraObj(   idSrc:Long,
                           idDst:Long,
                           path:List[Long],
                           weight: Long
                       )



