package clients

import jssc.SerialPort

class Ev3Client(port: String = "/dev/tty.EV3-SerialPort") {

  private[this] val serialPort = {
    val hwPort = new SerialPort(port)
    hwPort.openPort()
    hwPort.setParams(SerialPort.BAUDRATE_9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE)
    Runtime.getRuntime.addShutdownHook(new Thread { override def run(): Unit = shutdown() })
    hwPort
  }

  def shutdown() {
    perform("stop", 0)
    serialPort.closePort()
  }

  def perform(direction: String, speed: Int) {
    val realSpeed   = math.max(math.min(100, speed), 0)
    val ev3Speed    = direction match {
      case "right"  => realSpeed / 100 * 0x7F
      case "left"   => 0xFF - realSpeed / 100 * 0x7F
    }
    val cmd = if (realSpeed == 0) Ev3Protocol.stopMotorsAB else Ev3Protocol.forwardMotorsAB(ev3Speed)
    serialPort.writeBytes(cmd)
  }
}

object Ev3Protocol {

  /*
   * Start motor connected to port A with speed 20:
          Byte codes:             opOUTPUT_POWER,LC0(0),LC0(0x01),LC0(20), opOUTPUT_START,LC0(0),LC0(0x01)
                                           \    /
                                            \  /
        Hex values send:    0C00xxxx80 00 00 A4 00 01 14 A6 00 01

  Stop and float motor connected to port A:
        Byte codes:               opOUTPUT_STOP,LC0(0),LC0(0x01),LC0(0),
                                           \    /
                                            \  /
        Hex values send:    0900xxxx800000  A3 00 01 00
   */

  val stopMotorsAB = Array(0x09, 0x00, 0x01, 0x00, 0x80, 0x00, 0x00, 0xA3, 0x00, 0x03, 0x00).map(_.toByte)

  // speed forward = 0x01 - 0x7F
  // speed backward = 0xFF - SPEED // 0x80 ->
  def forwardMotorsAB(speed: Int) = Array(
    0x0D, 0x00,
    0x00, 0x00,
    0x80, 0x00, 0x00,
    0xA5, 0x00, 0x03, 0x81, speed,
    0xA6, 0x00, 0x03
  ).map(_.toByte)

}