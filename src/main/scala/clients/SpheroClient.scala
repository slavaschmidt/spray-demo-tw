package clients

import jssc.SerialPort
import sphero.Sphero._

class SpheroClient(port: String = "/dev/tty.Sphero-RBR-AMP-SPP") {

  private[this] val serialPort = {
    val hwPort = new SerialPort(port)
    hwPort.openPort()
    hwPort.setParams(SerialPort.BAUDRATE_115200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE)
    Runtime.getRuntime.addShutdownHook(new Thread { override def run(): Unit = shutdown() })
    hwPort.writeBytes(SetStabilization(enabled = true).pkgBytes)
    hwPort.writeBytes(SetHeading(0).pkgBytes)
    hwPort.writeBytes(SelfLevel(11).pkgBytes)
    hwPort.writeBytes(SetMotionTimeout(1000).pkgBytes)
    hwPort
  }

  def shutdown() {
    perform("stop", 0)
    serialPort.closePort()
  }

  def perform(direction: String, speed: Int) {
    val realSpeed       = math.max(math.min(100, speed), 0)
    val spheroSpeed     = math.round(realSpeed * 2.55)
    val spheroDirection = if (direction == "right") 0 else 180
    val cmd = Roll(spheroSpeed.toByte, spheroDirection, state = true, answer = false)
    serialPort.writeBytes(cmd.pkgBytes)
  }
}
