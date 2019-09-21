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

package com.aragoj.imageprocessor

import com.aragoj.imageprocessor.imagereaders.DefaultImageReader
import com.aragoj.imageprocessor.model.Image
import com.aragoj.imageprocessor.model.ImageItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Responsible for managing the retrieval of images.
 * Note that it uses coroutines for asynchronous operations.
 */
class ImageManager {

  private val imageCacheManager = ImageCacheManager()
  private val defaultImageReader = DefaultImageReader()

  suspend fun getImageItem(path: String): ImageItem = withContext(Dispatchers.IO) {
    defaultImageReader.readImageItem(path)
  }

  suspend fun getImage(path: String): Image = withContext(Dispatchers.IO) {
    imageCacheManager.getCachedImage(path) ?: processImage(path)
  }

  private fun processImage(path: String): Image {
    val result = defaultImageReader.readImage(path)
    imageCacheManager.cacheImage(result)
    return result
  }


}