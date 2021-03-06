package com.lucidchart.open.relate

import java.io.ByteArrayInputStream
import java.io.InputStream
import java.sql.{Date => SqlDate, PreparedStatement, Statement, Timestamp, Types}
import java.math.{BigDecimal => JBigDecimal, BigInteger => JBigInt}
import java.io.ByteArrayInputStream
import java.util.{Date, UUID}
import java.nio.ByteBuffer
import scala.collection.mutable
import scala.reflect.ClassTag

/**
 * A smart wrapper around the PreparedStatement class that allows inserting
 * parameter values by name rather than by index. Provides methods for inserting
 * all necessary datatypes.
 */
class SqlStatement(val stmt: PreparedStatement, val names: scala.collection.Map[String, List[Int]],
  val listParams: mutable.Map[String, ListParam]) {

  private def list[A](name: String, values: TraversableOnce[A], rule: (Int, A) => Unit): Unit = {
    names.get(name).map { nameData: List[Int] =>
      values.toIterator.zipWithIndex.foreach {
        case (value, index) =>
          nameData.foreach { nameDataIndex: Int =>
            rule(nameDataIndex + index, value)
          }
      }
    }
  }

  private def insert[A](name: String, value: A, rule: (Int, A) => Unit): Unit = {
    names.get(name).foreach(_.foreach(rule(_, value)))
  }

  /**
   * Set a BigDecimal in the PreparedStatement
   * @param name the name of the parameter to put the BigDecimal in
   * @param value the BigDecimal to put in the query
   */
  def bigDecimal(name: String, value: BigDecimal) = insert(name, value.bigDecimal, stmt.setBigDecimal _)
  
  /**
   * Insert multiple BigDecimals into the PreparedStatement
   * @param name the name of the parameter to put the values into
   * @param values the values to put into the query
   */
  def bigDecimals(name: String, values: TraversableOnce[BigDecimal]): Unit = list[JBigDecimal](name, values.map(_.bigDecimal), stmt.setBigDecimal _)

  /**
   * Set a Java BigDecimal in the PreparedStatement
   * @param name the name of the parameter to put the BigDecimal in 
   * @param value the BigDecimal to put in the query
   */
  def bigDecimal(name: String, value: JBigDecimal) = insert(name, value, stmt.setBigDecimal _)
  
  /**
   * Insert multiple Java BigDecimals into the PreparedStatement
   * @param name the name of the parameter to put the values into
   * @param values the values to put into the query
   */
  def bigDecimals[X: ClassTag](name: String, values: TraversableOnce[JBigDecimal]): Unit = list[JBigDecimal](name, values, stmt.setBigDecimal _)
  
  /**
   * Insert a BigDecimal Option into the PreparedStatement
   * @param name the name of the parameter to the value into
   * @param value the Option to insert
   */
  def bigDecimalOption[A](name: String, value: Option[A])(implicit bd: Query.BigDecimalLike[A]): Unit = {
    value.map(d => bigDecimal(name, bd.get(d))).getOrElse(insert(name, Types.DECIMAL, stmt.setNull _))
  }

  /**
   * Set a BigInt in the PreparedStatement
   * @param name the name of the parameter to put the BigInt in 
   * @param value the BigInt to put into the query
   */
  def bigInt(name: String, value: BigInt) = insert(name, new JBigDecimal(value.bigInteger), stmt.setBigDecimal _)
  
  /**
   * Insert multiple BigInts into the PreparedStatement
   * @param name the name of the parameter to put the values into
   * @param values the values to put into the query
   */
  def bigInts(name: String, values: TraversableOnce[BigInt]): Unit = list[JBigDecimal](name, values.map{ i: BigInt => new JBigDecimal(i.bigInteger) }, stmt.setBigDecimal _)

  /**
   * Set a Java BigInteger in the PreparedStatement
   * @param name the name of the parameter to put the BigInteger in
   * @param value the BigInteger to put in the query
   */
  def bigInt(name: String, value: JBigInt) = insert(name, new JBigDecimal(value), stmt.setBigDecimal _)
  
  /**
   * Insert multiple Java BigIntegers into the PreparedStatement
   * @param name the name of the parameter to put the values into
   * @param values the values to put into the query
   */
  def bigInts[X: ClassTag](name: String, values: TraversableOnce[JBigInt]): Unit = list[JBigDecimal](name, values.map(new JBigDecimal(_)), stmt.setBigDecimal _)
  
  /**
   * Insert a BigInt Option into the PreparedStatement
   * @param name the name of the parameter to the value into
   * @param value the Option to insert
   */
  def bigIntOption[A](name: String, value: Option[A])(implicit bd: Query.BigIntLike[A]): Unit = {
    value.map(i => bigInt(name, bd.get(i))).getOrElse(insert(name, Types.BIGINT, stmt.setNull _))
  }

  /**
   * Set a Boolean in the PreparedStatement
   * @param name the name of the parameter to put the Boolean in
   * @param value the Boolean to put in the query
   */
  def bool(name: String, value: Boolean) = insert(name, value, stmt.setBoolean _)
  
  /**
   * Insert multiple Booleans into the PreparedStatement
   * @param name the name of the parameter to put the values into
   * @param values the values to put into the query
   */
  def bools(name: String, values: TraversableOnce[Boolean]): Unit = list[Boolean](name, values, stmt.setBoolean _)
  
  /**
   * Insert a Boolean Option into the PreparedStatement
   * @param name the name of the parameter to the value into
   * @param value the Option to insert
   */
  def boolOption(name: String, value: Option[Boolean]): Unit = {
    value.map(bool(name, _)).getOrElse(insert(name, Types.BOOLEAN, stmt.setNull _))
  }

  /**
   * Set a Byte in the PreparedStatement
   * @param name the name of the parameter to put the Byte in
   * @param value the Byte to put in the query
   */
  def byte(name: String, value: Byte) = insert(name, value, stmt.setByte _)
  
  /**
   * Insert multiple Bytes into the PreparedStatement
   * @param name the name of the parameter to put the values into
   * @param values the values to put into the query
   */
  def bytes(name: String, values: TraversableOnce[Byte]): Unit = list[Byte](name, values, stmt.setByte _)
  
  /**
   * Insert a Byte Option into the PreparedStatement
   * @param name the name of the parameter to the value into
   * @param value the Option to insert
   */
  def byteOption(name: String, value: Option[Byte]): Unit = {
    value.map(byte(name, _)).getOrElse(insert(name, Types.TINYINT, stmt.setNull _))
  }

  /**
   * Set a ByteArray in the PreparedStatement
   * @param name the name of the parameter to put the ArrayByte in
   * @param value the ByteArray to put in the query
   */
  def byteArray(name: String, value: Array[Byte]) = insert(name, value, stmt.setObject _)
  
  /**
   * Insert multiple Byte Arrays into the PreparedStatement
   * @param name the name of the parameter to put the values into
   * @param values the values to put into the query
   */
  def byteArrays(name: String, values: TraversableOnce[Array[Byte]]) = list(name, values, stmt.setObject _)
  
  /**
   * Insert a Byte Array Option into the PreparedStatement
   * @param name the name of the parameter to the value into
   * @param value the Option to insert
   */
  def byteArrayOption(name: String, value: Option[Array[Byte]]) = {
    value.map(byteArray(name, _)).getOrElse(insert(name, Types.BLOB, stmt.setNull _))
  }

  /**
   * Set a Char in the PreparedStatement
   * @param name the name of the parameter to put the Char in 
   * @param value the Char to put in the query
   */
  def char(name: String, value: Char) = insert(name, value.toString, stmt.setString _)
  
  /**
   * Insert multiple Chars into the PreparedStatement
   * @param name the name of the parameter to put the values into
   * @param values the values to put into the query
   */
  def chars(name: String, values: TraversableOnce[Char]): Unit = list[String](name, values.map(_.toString), stmt.setString _)
  
  /**
   * Insert a Char Option into the PreparedStatement
   * @param name the name of the parameter to the value into
   * @param value the Option to insert
   */
  def charOption(name: String, value: Option[Char]): Unit = {
    value.map(char(name, _)).getOrElse(insert(name, Types.CHAR, stmt.setNull _))
  }

  /**
   * Set a Date in the PreparedStatement
   * @param name the name of the parameter to put the Date in
   * @param value the Date to put in the query
   */
  def date(name: String, value: Date) = insert(name, new Timestamp(value.getTime), stmt.setTimestamp _)
  
  /**
   * Insert multiple Dates into the PreparedStatement
   * @param name the name of the parameter to put the values into
   * @param values the values to put into the query
   */
  def dates(name: String, values: TraversableOnce[Date]): Unit = list[Timestamp](name, values.map{ d: Date => new Timestamp(d.getTime)}, stmt.setTimestamp _)
  
  /**
   * Insert a Date Option into the PreparedStatement
   * @param name the name of the parameter to the value into
   * @param value the Option to insert
   */
  def dateOption(name: String, value: Option[Date]): Unit = {
    value.map(date(name, _)).getOrElse(insert(name, Types.DATE, stmt.setNull _))
  }

  /**
   * Set a Double in the PreparedStatement
   * @param name the name of the parameter to put the Double in
   * @param value the Double to put in the query
   */
  def double(name: String, value: Double) = insert(name, value, stmt.setDouble _)
  
  /**
   * Insert multiple Doubles into the PreparedStatement
   * @param name the name of the parameter to put the values into
   * @param values the values to put into the query
   */
  def doubles(name: String, values: TraversableOnce[Double]): Unit = list[Double](name, values, stmt.setDouble _)
  
  /**
   * Insert a Double Option into the PreparedStatement
   * @param name the name of the parameter to the value into
   * @param value the Option to insert
   */
  def doubleOption(name: String, value: Option[Double]): Unit = {
    value.map(double(name, _)).getOrElse(insert(name, Types.DOUBLE, stmt.setNull _))
  }

  /**
   * Set a Float in the PreparedStatement
   * @param name the name of the parameter to put the Float in
   * @param value the Float to put in the query
   */
  def float(name: String, value: Float) = insert(name, value, stmt.setFloat _)
  
  /**
   * Insert multiple Floats into the PreparedStatement
   * @param name the name of the parameter to put the values into
   * @param values the values to put into the query
   */
  def floats(name: String, values: TraversableOnce[Float]): Unit = list[Float](name, values, stmt.setFloat _)
  
  /**
   * Insert a Float Option into the PreparedStatement
   * @param name the name of the parameter to the value into
   * @param value the Option to insert
   */
  def floatOption(name: String, value: Option[Float]): Unit = {
    value.map(float(name, _)).getOrElse(insert(name, Types.FLOAT, stmt.setNull _))
  }

  /**
   * Set an Int in the PreparedStatement
   * @param name the name of the parameter to put the int in
   * @param value the int to put in the query
   */
  def int(name: String, value: Int) = insert(name, value, stmt.setInt _)
  
  /**
   * Insert multiple Ints into the PreparedStatement
   * @param name the name of the parameter to put the values into
   * @param values the values to put into the query
   */
  def ints(name: String, values: TraversableOnce[Int]): Unit = list[Int](name, values, stmt.setInt _)
  
  /**
   * Insert an Int Option into the PreparedStatement
   * @param name the name of the parameter to the value into
   * @param value the Option to insert
   */
  def intOption(name: String, value: Option[Int]): Unit = {
    value.map(int(name, _)).getOrElse(insert(name, Types.INTEGER, stmt.setNull _))
  }

  /**
   * Set a Long in the PreparedStatement
   * @param name the name of the parameter to put the Long in
   * @param value the Long to put in the query
   */
  def long(name: String, value: Long) = insert(name, value, stmt.setLong)
  
  /**
   * Insert multiple Longs into the PreparedStatement
   * @param name the name of the parameter to put the values into
   * @param values the values to put into the query
   */
  def longs(name: String, values: TraversableOnce[Long]): Unit = list[Long](name, values, stmt.setLong _)
  
  /**
   * Insert a Long Option into the PreparedStatement
   * @param name the name of the parameter to the value into
   * @param value the Option to insert
   */
  def longOption(name: String, value: Option[Long]): Unit = {
    value.map(long(name, _)).getOrElse(insert(name, Types.BIGINT, stmt.setNull _))
  }

  /**
   * Set a Short in the PreparedStatement
   * @param name the name of the parameter to put the Short in
   * @param value the Short to put in the query
   */
  def short(name: String, value: Short) = insert(name, value, stmt.setShort _)
  
  /**
   * Insert multiple Shorts into the PreparedStatement
   * @param name the name of the parameter to put the values into
   * @param values the values to put into the query
   */
  def shorts(name: String, values: TraversableOnce[Short]): Unit = list[Short](name, values, stmt.setShort _)
  
  /**
   * Insert a Short Option into the PreparedStatement
   * @param name the name of the parameter to the value into
   * @param value the Option to insert
   */
  def shortOption(name: String, value: Option[Short]): Unit = {
    value.map(short(name, _)).getOrElse(insert(name, Types.SMALLINT, stmt.setNull _))
  }

  /**
   * Set a String in the PreparedStatement
   * @param name the name of the parameter to put the string in
   * @param value the value to put in the query
   */
  def string(name: String, value: String) = insert(name, value, stmt.setString _)
  
  /**
   * Insert multiple Strings into the PreparedStatement
   * @param name the name of the parameter to put the values into
   * @param values the values to put into the query
   */
  def strings(name: String, values: TraversableOnce[String]): Unit = list[String](name, values, stmt.setString _)
  
  /**
   * Insert a String Option into the PreparedStatement
   * @param name the name of the parameter to the value into
   * @param value the Option to insert
   */
  def stringOption(name: String, value: Option[String]): Unit = {
    value.map(string(name, _)).getOrElse(insert(name, Types.VARCHAR, stmt.setNull _))
  }

  /**
   * Set a Timestamp in the PreparedStatement
   * @param name the name of the parameter to put the Timestamp in
   * @param value the Timestamp to put into the query
   */
  def timestamp(name: String, value: Timestamp) = insert(name, value, stmt.setTimestamp)
  
  /**
   * Insert multiple Timestamps into the PreparedStatement
   * @param name the name of the parameter to put the values into
   * @param values the values to put into the query
   */
  def timestamps(name: String, values: TraversableOnce[Timestamp]): Unit = list[Timestamp](name, values, stmt.setTimestamp _)
  
  /**
   * Insert a Timestamp Option into the PreparedStatement
   * @param name the name of the parameter to the value into
   * @param value the Option to insert
   */
  def timestampOption(name: String, value: Option[Timestamp]): Unit = {
    value.map(timestamp(name, _)).getOrElse(insert(name, Types.TIMESTAMP, stmt.setNull _))
  }

  /**
   * Set a UUID in the PreparedStatement
   * @param name the name of the parameter to put the UUID in
   * @param value the UUID to put in the query
   */
  def uuid(name: String, value: UUID) = insert(name, ByteHelper.uuidToByteArray(value), stmt.setBytes _)
  
  /**
   * Insert multiple UUIDs into the PreparedStatement
   * @param name the name of the parameter to put the values into
   * @param values the values to put into the query
   */
  def uuids(name: String, values: TraversableOnce[UUID]): Unit = list[Array[Byte]](name, values.map(ByteHelper.uuidToByteArray(_)), stmt.setBytes _)
  
  /**
   * Insert an UUID Option into the PreparedStatement
   * @param name the name of the parameter to the value into
   * @param value the Option to insert
   */
  def uuidOption(name: String, value: Option[UUID]): Unit = {
    value.map(uuid(name, _)).getOrElse(insert(name, Types.VARCHAR, stmt.setNull _))
  }

}

/** This class is used to insert tuple data into a prepared statement */
case class TupleStatement(
  stmt: PreparedStatement,
  params: Map[String, Int],
  index: Int // the index in the prepared statement that this tuple starts at
) {
  
  /** 
   * Insert a value into the PreparedStatement
   * @param name the identifier for the value
   * @param value the value to insert
   * @param rule the rule to use on the PreparedStatement
   */
  private def insert[A](name: String, value: A, rule: (Int, A) => Unit) {
    rule(index + params(name), value)
  }

  def bigDecimal(name: String, value: BigDecimal) = insert(name, value.bigDecimal, stmt.setBigDecimal _)
  def bigDecimalOption(name: String, value: Option[BigDecimal]) = value.map(bigDecimal(name, _)).getOrElse(insert(name, Types.DECIMAL, stmt.setNull _))
  def bigDecimal(name: String, value: JBigDecimal) = insert(name, value, stmt.setBigDecimal _)
  def bigDecimalOption[X: ClassTag](name: String, value: Option[JBigDecimal]) = value.map(bigDecimal(name, _)).getOrElse(insert(name, Types.DECIMAL, stmt.setNull _))
  def bigInt(name: String, value: BigInt) = insert(name, new JBigDecimal(value.bigInteger), stmt.setBigDecimal _)
  def bigIntOption(name: String, value: Option[BigInt]) = value.map(bigInt(name, _)).getOrElse(insert(name, Types.DECIMAL, stmt.setNull _))
  def bigInt(name: String, value: JBigInt) = insert(name, new JBigDecimal(value), stmt.setBigDecimal _)
  def bigIntOption[X: ClassTag](name: String, value: Option[JBigInt]) = value.map(bigInt(name, _)).getOrElse(insert(name, Types.DECIMAL, stmt.setNull _))
  def bool(name: String, value: Boolean) = insert(name, value, stmt.setBoolean _)
  def boolOption(name: String, value: Option[Boolean]) = value.map(bool(name, _)).getOrElse(insert(name, Types.BOOLEAN, stmt.setNull _))
  def byte(name: String, value: Byte) = insert(name, value, stmt.setByte _)
  def byteOption(name: String, value: Option[Byte]) = value.map(byte(name, _)).getOrElse(insert(name, Types.TINYINT, stmt.setNull _))
  def char(name: String, value: Char) = insert(name, value.toString, stmt.setString _)
  def charOption(name: String, value: Option[Char]) = value.map(char(name, _)).getOrElse(insert(name, Types.CHAR, stmt.setNull _))
  def date(name: String, value: Date) = insert(name, new Timestamp(value.getTime), stmt.setTimestamp _)
  def dateOption(name: String, value: Option[Date]) = value.map(date(name, _)).getOrElse(insert(name, Types.DATE, stmt.setNull _))
  def double(name: String, value: Double) = insert(name, value, stmt.setDouble _)
  def doubleOption(name: String, value: Option[Double]) = value.map(double(name, _)).getOrElse(insert(name, Types.DOUBLE, stmt.setNull _))
  def float(name: String, value: Float) = insert(name, value, stmt.setFloat _)
  def floatOption(name: String, value: Option[Float]) = value.map(float(name, _)).getOrElse(insert(name, Types.FLOAT, stmt.setNull _))
  def int(name: String, value: Int) = insert(name, value, stmt.setInt _)
  def intOption(name: String, value: Option[Int]) = value.map(int(name, _)).getOrElse(insert(name, Types.INTEGER, stmt.setNull _))
  def long(name: String, value: Long) = insert(name, value, stmt.setLong _)
  def longOption(name: String, value: Option[Long]) = value.map(long(name, _)).getOrElse(insert(name, Types.BIGINT, stmt.setNull _))
  def short(name: String, value: Short) = insert(name, value, stmt.setShort _)
  def shortOption(name: String, value: Option[Short]) = value.map(short(name, _)).getOrElse(insert(name, Types.SMALLINT, stmt.setNull _))
  def string(name: String, value: String) = insert(name, value, stmt.setString _)
  def stringOption(name: String, value: Option[String]) = value.map(string(name, _)).getOrElse(insert(name, Types.VARCHAR, stmt.setNull _))
  def timestamp(name: String, value: Timestamp) = insert(name, value, stmt.setTimestamp _)
  def timestampOption(name: String, value: Option[Timestamp]) = value.map(timestamp(name, _)).getOrElse(insert(name, Types.TIMESTAMP, stmt.setNull _))
  def uuid(name: String, value: UUID) = insert(name, ByteHelper.uuidToByteArray(value), stmt.setBytes _)
  def uuidOption(name: String, value: Option[UUID]) = value.map(uuid(name, _)).getOrElse(insert(name, Types.VARCHAR, stmt.setNull _))
}

private[relate] object ByteHelper {

  def uuidToByteArray(uuid: UUID): Array[Byte] = {
    val bb = ByteBuffer.wrap(new Array[Byte](16))
    bb.putLong(uuid.getMostSignificantBits)
    bb.putLong(uuid.getLeastSignificantBits)
    bb.array()
  }

}
