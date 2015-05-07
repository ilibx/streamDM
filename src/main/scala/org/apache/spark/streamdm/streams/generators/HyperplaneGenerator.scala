/*
 * Copyright (C) 2015 Holmes Team at HUAWEI Noah's Ark Lab.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.apache.spark.streamdm.streams.generators

import com.github.javacliparser.IntOption
import org.apache.spark.rdd.RDD
import org.apache.spark.streamdm.core.{DenseInstance, Instance, Example}
import org.apache.spark.streamdm.streams.StreamReader
import org.apache.spark.streaming.{Duration, Time, StreamingContext}
import org.apache.spark.streaming.dstream.{InputDStream, DStream}

import scala.util.Random


/**
 * Stream generator for generating data from a Hyperplane.
 *
 */

class HyperplaneGenerator extends StreamReader {

  val chunkSizeOption: IntOption = new IntOption("chunkSize", 'c',
    "Chunk Size", 1000, 1, Integer.MAX_VALUE)

  val slideDurationOption: IntOption = new IntOption("slideDuration", 'd',
    "Slide Duration in milliseconds", 1000, 1, Integer.MAX_VALUE)

  val numFeaturesOption: IntOption = new IntOption("numFeatures", 'f',
    "Number of Features", 3, 1, Integer.MAX_VALUE)

  /**
   * Obtains a stream of Examples
   * @param ssc a Spark Streaming Context
   * @return a stream of Examples
   */
  def getInstances(ssc:StreamingContext): DStream[Example] = {
    new InputDStream[Example](ssc){

      override def start(): Unit = {}

      override def stop(): Unit = {}

      override def compute(validTime: Time): Option[RDD[Example]] = {
        val examples:Array[Example] = Array.fill[Example](chunkSizeOption.getValue)(getExample)
        Some(ssc.sparkContext.parallelize(examples))
      }

      override def slideDuration = {
        new Duration(slideDurationOption.getValue)
      }

      def getExample(): Example = {
        val inputInstance = new DenseInstance(Array.fill[Double](numFeaturesOption.getValue)( 10 * Random.nextDouble() - 5))
        new Example(inputInstance, new DenseInstance(Array.fill[Double](1)(label(inputInstance))))
      }

      val weight = new DenseInstance(Array.fill[Double](numFeaturesOption.getValue)(2.0 * Random.nextDouble() - 1.0))

      val bias:Double = 2.0 * Random.nextDouble() - 1.0

      def label(inputInstance: Instance):Double = {
        val sum = weight.dot(inputInstance)
        if (sum > bias) 1
          else 0
      }
    }
  }

  def init(): Unit = {}

}
