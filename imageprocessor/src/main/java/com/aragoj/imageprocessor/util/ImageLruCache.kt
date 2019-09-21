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

package com.aragoj.imageprocessor.util

import com.aragoj.imageprocessor.model.Image

class ImageLruCache(maxSize: Long) : LruCache<String, Image>(maxSize) {

  override fun sizeOf(key: String?, image: Image?): Long {
    var size: Long = key?.toByteArray()?.size?.toLong() ?: 0
    size += image?.size() ?: 1
    return size
  }
}