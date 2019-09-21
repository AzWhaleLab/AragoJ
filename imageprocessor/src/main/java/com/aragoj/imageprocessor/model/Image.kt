/*
 * Copyright 2019 franciscoaleixo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.aragoj.imageprocessor.model

import javafx.scene.image.Image

class Image(val path: String, val metadata: Metadata? = null, val image: Image? = null) {

  class Metadata {
    private var metadata: MutableList<Item> = ArrayList()

    fun add(item: Item){
      metadata.add(item)
    }

    fun addAll(vararg item: Item){
      metadata.addAll(item)
    }

    fun addAll(items: Collection<Item>){
      metadata.addAll(items)
    }

    class Directory(val tagName: String) : Item(tagName){
      private val items: MutableList<Item> = ArrayList()

      fun addItem(item: Item){
        items.add(item)
      }

      fun addItems(vararg item: Item){
        items.addAll(item)
      }

      fun isEmpty(): Boolean{
        return items.size == 0
      }
    }

    open class Item(val tag: String, val value: String = "")
  }

  /**
   * Retrieves approximate size of this Image object.
   * We only calculate the image & thumbnail size and assume that the rest is negligible.
   */
  fun size(): Long {
    return image?.getSize() ?: 1
  }

  private fun Image.getSize(): Long{
    return width.toLong() * height.toLong() * 4
  }

}