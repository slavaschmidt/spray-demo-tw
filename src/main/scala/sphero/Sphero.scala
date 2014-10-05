package sphero

object Sphero {

  var YES = true
  val NO  = false

  def byteArrayToHex(a: Array[Byte]) = {
    val sb = new StringBuilder(a.length * 2)
    for (b <- a) sb.append(String.format("0x%02X, ", (b & 0xFF).asInstanceOf[AnyRef]))
    sb.delete(sb.length-2, sb.length-1)
    sb.toString()
  }

  trait Packet {
    val SOP1          : Byte = 0xFF.toByte
    protected def bit (value: Boolean) = if (value) "1" else "0"
    protected def byte(value: Boolean) = if (value) 0x01.toByte else zero
    protected def bool(value: Byte)    = value != zero
    protected def bytes(value: Int): List[Byte] = List((value >> 8).toByte, (value & 0x00FF).toByte)
    protected def bytes32(value: Int): List[Byte] = bytes(value >> 16) ::: bytes(value & 0x0000FFFF)
    protected val zero: Byte = 0x00
    def seq           : Byte
    def data          : List[Byte]  = Nil
    def content       : List[Byte]  = Nil
    def dlen          : Int
    def SOP2          : Byte
    lazy val checkSum : Byte = (content.sum & 0xFF ^ 0xFF).toByte
    lazy val pkgBytes = ((SOP1 :: SOP2 :: content) :+ checkSum).toArray
  }

  trait Response extends Packet {
    def ack   : Boolean
    def MRSP  : Byte
    val SOP2_ASYNC  = 0xFE.toByte
    val SOP2_SYNC   = 0xFF.toByte
    val SOP2    : Byte = Integer.parseInt("1111111" + bit(ack), 2).toByte

    def parse(buf: Array[Byte]) = buf.toList match {
      case bytes if bytes.size < 4 => None
      case SOP1 :: SOP2_ASYNC :: bytes => tryParse(ack = false, bytes)
      case SOP1 :: SOP2_SYNC  :: bytes => tryParse(ack = true, bytes)
      case _ => None
    }

    def tryParse(ack: Boolean, bytes: List[Byte]): Option[Response]
  }

  trait Command extends Packet {
    def cid           : Int
    def answer        : Boolean

    def resetTimeout  : Boolean     = true
    def did           : Int        = 0x00

    override val seq = zero // ignored if sync is false
    lazy val dlen  = data.size + 1
    val SOP2  : Byte = Integer.parseInt("111111" + bit(resetTimeout) + bit(answer), 2).toByte

    override lazy val content  = did.toByte :: cid.toByte :: seq :: dlen.toByte :: data
  }

  // ------------------- CORE ---------------------- //
  case class Ping(answer: Boolean = YES) extends Command { val cid = 0x01 }
  case class GetVersioning(answer: Boolean = YES) extends Command { val cid = 0x02 }
  case class GetBluetoothInfo(answer: Boolean = YES) extends Command { val cid = 0x11 }
  case class GetAutoReconnect(answer: Boolean = YES) extends Command { val cid = 0x13 }
  case class GetPowerState(answer: Boolean = YES) extends Command { val cid = 0x20 }
  case class GetVoltageTripPoints(answer: Boolean = YES) extends Command { val cid = 0x23 }
  case class JumpToBootloader(answer: Boolean = YES) extends Command { val cid = 0x30 }
  case class PerformLevel1Diagnostics(answer: Boolean = YES) extends Command { val cid = 0x40 }
  case class PerformLevel2Diagnostics(answer: Boolean = YES) extends Command { val cid = 0x41 }
  case class ClearCounters(answer: Boolean = YES) extends Command { val cid = 0x42 }

  case class UserHackMode(answer: Boolean = YES) extends Command {
    override lazy val pkgBytes = Array(0xFF, 0xFF, 0x02, 0x42, 0x33, 0x02, 0x01, 0x85).map(_.toByte)
    override def cid = 0x42
  }

  case class NormalMode(answer: Boolean = YES) extends Command {
    override lazy val pkgBytes = "smn\n\r".getBytes
    override def cid = 0x42
  }

  case class ControlUartTxLine(enable: Boolean, answer: Boolean = YES) extends Command {
    override val cid  = 0x03
    override val data = List(byte(enable))
  }

  case class SetDeviceName(name: String, answer: Boolean = YES) extends Command {
    override val cid  = 0x10
    override val data = name.getBytes.toList
  }

  case class SetAutoReconnect(enable: Boolean, time: Byte, answer: Boolean = YES) extends Command {
    val cid = 0x12
    override val data = List(byte(enable), time)
  }


  case class SetPowerNotification(enable: Boolean, answer: Boolean = YES) extends Command {
    val cid = 0x21
    override val data = List(byte(enable))
  }

  case class Sleep(wakeupAfterSeconds: Int, macroId: Byte, orbBasic: Int, answer: Boolean = YES) extends Command {
    val cid = 0x22
    val NO_WAKEUP = 0
    val DEEP_SLEEP = 0xFFFF
    override val data = bytes(wakeupAfterSeconds) ::: macroId :: bytes(orbBasic)
  }

  /**
   * @param vlow - must be in the range 675 to 725 (±25)
   * @param vcrit - must be in the range 625 to 675 (±25)
   *              There must be 0.25V of separation between the two values
   */
  case class SetVoltageTripPoints(vlow: Int, vcrit: Int, answer: Boolean = YES) extends Command {
    val cid = 0x24
    override val data = bytes(vlow) ::: bytes(vcrit)
  }

  case class SetInactivityTimeout(timeSeconds: Int, answer: Boolean = YES) extends Command {
    val cid = 0x25
    override val data = bytes(timeSeconds)
  }

  case class AssignTimeValue(value: Int, answer: Boolean = YES) extends Command {
    val cid = 0x50
    override val data = bytes32(value)
  }

  case class PollPacketTimes(value: Int, answer: Boolean = YES) extends Command {
    val cid = 0x51
    override val data = bytes32(value)
  }


  // ------------------- SPHERO ---------------------- //

  case class SetHeading(headingDegrees: Int, answer: Boolean = YES) extends Command {
    val cid = 0x01
    override val did = 0x02
    override val data = bytes(headingDegrees)
  }

  case class SetStabilization(enabled: Boolean, answer: Boolean = YES) extends Command {
    val cid = 0x02
    override val did = 0x02
    override val data = byte(enabled) :: Nil
  }

  case class SetRotationRate(rate: Byte, answer: Boolean = YES) extends Command {
    val cid = 0x01
    override val did = 0x02
    override val data = List(rate)
  }

  case class SetBackLedOutput(brightness: Byte, answer: Boolean = YES) extends Command {
    val cid = 0x21
    override val did = 0x02
    override val data = List(brightness)
  }

  case class SetRgbLedOutput(red: Byte, green: Byte, blue: Byte, persist: Boolean, answer: Boolean = YES) extends Command {
    val cid = 0x20
    override val did = 0x02
    override val data = List(red, green, blue, byte(persist))
  }

  case class Roll(speed: Byte, heading: Int, state: Boolean, answer: Boolean = YES) extends Command {
    val cid = 0x30
    override val did = 0x02
    override val data = (speed :: bytes(heading)) :+ byte(state)
  }
  case class SelfLevel(options: Byte, angleLimit: Byte = 3, timeout: Byte = 15, trueTime: Byte = 30, answer: Boolean = YES) extends Command {
    val cid = 0x09
    override val did = 0x02
    override val data = List(options, angleLimit, timeout, trueTime)
  }
  case class SetMotionTimeout(time: Int, answer: Boolean = YES) extends Command {
    val cid = 0x34
    override val did = 0x02
    override val data = bytes(time)
  }

  /* ----- responses ----- */

  case class PingResp(override val seq: Byte, override val ack: Boolean) extends Response {
    def tryParse(ack: Boolean, bytes: List[Byte]) = bytes match {
      case MRSP :: seqN :: len :: chk :: Nil
        if len == dlen && chk == PingResp(seqN, ack).checkSum => Some(PingResp(seqN, ack))
      case _ => None
    }

    override val MRSP = zero
    override val dlen = 0x01
  }

  case class GetVersioningResp(override val seq: Byte, override val ack: Boolean, override val data: List[Byte]) extends Response {
    def tryParse(ack: Boolean, bytes: List[Byte]) = bytes match {
      case (MRSP :: seqN :: len :: buf) :+ chk
        if len == dlen && chk == GetVersioningResp(seqN, ack, buf).checkSum => Some(GetVersioningResp(seqN, ack, buf))
      case _ => None
    }

    override val MRSP = zero
    override val dlen = 0x0B

    lazy val List(recordVersion, modelNumber, hardwareVersion, mainAppVersion, mainAppRevision,
        bootloaderVersion, basicVersion, macroVersion, apiMajorRevision, apiMinorRevision) = data
  }

  case class GetBluetoothInfoResp(override val seq: Byte, override val ack: Boolean, override val data: List[Byte]) extends Response {
    def tryParse(ack: Boolean, bytes: List[Byte]) = bytes match {
      case (MRSP :: seqN :: len :: buf) :+ chk
        if len == dlen && chk == GetBluetoothInfoResp(seqN, ack, buf).checkSum => Some(GetBluetoothInfoResp(seqN, ack, buf))
      case _ => None
    }

    override val MRSP = zero
    override val dlen = 0x21
    lazy val List(asciiName, asciiBta) = data.grouped(16).map(_.toString().trim)
  }

  case class GetAutoReconnectResp(override val seq: Byte, override val ack: Boolean, override val data: List[Byte]) extends Response {
    def tryParse(ack: Boolean, bytes: List[Byte]) = bytes match {
      case (MRSP :: seqN :: len :: buf) :+ chk
        if len == dlen && chk == GetAutoReconnectResp(seqN, ack, buf).checkSum => Some(GetAutoReconnectResp(seqN, ack, buf))
      case _ => None
    }
    override val MRSP = zero
    override val dlen = 0x03
    lazy val (flag, time) = (bool(data(0)), data(1))
  }

  case class GetPowerStateResp(override val seq: Byte, override val ack: Boolean, override val data: List[Byte]) extends Response {
    def tryParse(ack: Boolean, bytes: List[Byte]) = bytes match {
      case (MRSP :: seqN :: len :: buf) :+ chk
        if len == dlen && chk == GetPowerStateResp(seqN, ack, buf).checkSum => Some(GetPowerStateResp(seqN, ack, buf))
      case _ => None
    }
    override val MRSP = zero
    override val dlen = 0x09

    trait BatteryState
    case object Charging extends BatteryState
    case object OK extends BatteryState
    case object Low extends BatteryState
    case object Critical extends BatteryState

    lazy val recordVersion = data(0)
    lazy val powerState = data(1) match {
      case 0x01 => Charging
      case 0x02 => OK
      case 0x03 => Low
      case 0x04 => Critical
    }
    lazy val voltage             = (data(2) * 256 + data(3)) / 100f
    lazy val numCharges          = data(4) * 256 + data(5)
    lazy val secondsSinceCharge  = data(6) * 256 + data(7)

  }

}
