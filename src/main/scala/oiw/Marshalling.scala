package oiw

import oiw.Model.{Artist, Connection, Employee, Producer}

object Marshalling {

  implicit def employes2String(data: (Seq[Connection], Seq[Employee])) =
    orgStructure(data._1, data._2)

  def structure(labour: Seq[Employee]) = {
    val columns = labour filter {
      _.isInstanceOf[Producer]
    } map {
      _.asInstanceOf[Producer]
    }
    val rows = labour filter {
      _.isInstanceOf[Artist]
    } map {
      _.asInstanceOf[Artist]
    }
    (columns, rows)
  }

  def mkRow(connections: Seq[Connection], artists: Seq[Artist])(p: Producer) = {
    val values = artists map { a => connections.contains(Connection(p, a))}
    val nameValues = artists zip values
    val rowParts = nameValues map { case (name, value) => "\"" + name.name + "\":" + value} mkString ", "
    "\"" + p.name + "\":{ " + rowParts + "}"
  }

  def asMatrix(connections: Seq[Connection], producers: Seq[Producer], artists: Seq[Artist]) =
    producers map mkRow(connections, artists)

  def orgStructure(connections: Seq[Connection], labour: Seq[Employee]) = {
    val (columns, rows) = structure(labour)
    val columnNames = columns.map(_.name).mkString("[\"", "\",\"", "\"]")
    val rowNames = rows.map(_.name).mkString("[\"", "\",\"", "\"]")
    val matrix = asMatrix(connections, columns, rows)
    s"""{"config": { ${matrix.mkString(",")} },"rows": $rowNames,"columns" : $columnNames}"""
  }
}
