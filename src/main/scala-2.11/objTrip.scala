import org.joda.time.DateTime

/**
  * Created by AntonioFischetti on 06/06/16.
  */
object objTrip {

  def apply(idTrip: Long , nodeStart:Long,  nodeDest:Long, origLat:Double, origLon:Double, destLat:Double , destLon:Double , startTime:DateTime , arrivalTime:DateTime , pathTimed:(Long,List[(Long,Long,Long)]) ) : _objTrip =
    _objTrip (idTrip,  nodeStart,  nodeDest, origLat, origLon, destLat , destLon , startTime , arrivalTime,pathTimed)

}

case class _objTrip(   idTrip: Long,
                       nodeStart:Long,
                       nodeDest:Long,
                       origLat:Double,
                       origLon:Double,
                       destLat:Double ,
                       destLon:Double ,
                       startTime:DateTime,
                       arrivalTime:DateTime,
                       pathTimed:(Long,List[(Long,Long,Long)])
                      )
