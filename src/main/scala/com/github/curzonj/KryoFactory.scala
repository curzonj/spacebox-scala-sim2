package com.github.curzonj

import java.util.zip.{InflaterInputStream, DeflaterOutputStream}
import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import com.twitter.chill.ScalaKryoInstantiator
import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.{Input, Output}

/*
  TODO: pool instead of TLS, we probably only ever need one
  TODO: reuse the baos and the input/outputs also
 */
object KryoFactory {
  def deflate(value: Any): Array[Byte] = {
    val kryo = kryos.get
    val baos = new ByteArrayOutputStream
    val stream = new DeflaterOutputStream(baos)
    val output = new Output(stream, 4096)
    kryo.writeObject(output, value)
    output.close()

    baos.toByteArray
  }

  def inflate[T](data: Array[Byte], klass: Class[T]): T = {
    val kryo = kryos.get
    val stream = new InflaterInputStream(new ByteArrayInputStream(data))
    val input = new Input(stream)
    kryo.readObject(input, klass)
  }

  def bytes(value: Any): Array[Byte] = {
    val kryo = kryos.get
    val baos = new ByteArrayOutputStream
    val output = new Output(baos, 4096)
    kryo.writeObject(output, value)
    output.close()

    baos.toByteArray
  }

  def fromBytes[T](data: Array[Byte], klass: Class[T]): T = {
    val kryo = kryos.get
    val input = new Input(data)
    kryo.readObject(input, klass)
  }

  private val kryos: ThreadLocal[Kryo] = new ThreadLocal[Kryo] {
    override def initialValue: Kryo =  {
      val instantiator = new ScalaKryoInstantiator
      instantiator.setRegistrationRequired(false)
      instantiator.newKryo()
    }
  }
}
