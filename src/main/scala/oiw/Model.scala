package oiw

object Model {

  sealed trait Employee {
    def name: String
  }

  sealed trait Producer extends Employee
  sealed trait Artist   extends Employee

  case class ButtonProducer     (name: String) extends Producer
  case class HeadProducer       (name: String) extends Producer
  case class BrainWaveProducer  (name: String) extends Producer
  case class PebbleProducer     (name: String) extends Producer

  case class CanvasArtist       (name: String) extends Artist
  case class BarArtist          (name: String) extends Artist
  case class GaugeArtist        (name: String) extends Artist
  case class Ev3Artist          (name: String) extends Artist
  case class SpheroArtist       (name: String) extends Artist

  case class Connection(from: Producer, to: Artist)

  val employeeTypes: Map[String, (String) => Employee] = Map(
    "Button Director"     -> ButtonProducer.apply,
    "Head Director"       -> HeadProducer.apply,
    "BrainWave Director"  -> BrainWaveProducer.apply,
    "Pebble Director"     -> PebbleProducer.apply,
    "Map Performer"       -> CanvasArtist.apply,
    "Bar Performer"       -> BarArtist.apply,
    "Gauge Performer"     -> GaugeArtist.apply,
    "EV3 Performer"       -> Ev3Artist.apply,
    "Sphero Performer"    -> SpheroArtist.apply
  )

  object Employee {
    def byTypeName(typeStr: String, name: String) =
      employeeTypes.get(typeStr).map(_(name))
  }

}
