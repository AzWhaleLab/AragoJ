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

import com.aragoj.imageprocessor.model.Image
import com.aragoj.imageprocessor.model.ImageItem
import com.aragoj.imageprocessor.util.ImageLruCache

/**
 * Responsible for managing image memory cache.
 *
 * It functions as follows:
 * - The actual image and metadata/thumbnail are cached separately but with the same key (file path)
 * - Metadata/thumbnail is considered essential and lives forever until removed
 * - The actual image is cached through an LRU strategy (max size: 60% of available memory)
 *
 * Metadata and thumbnail are considered essential because they're always on display and accessible
 * to the user through a selection list. The actual image, however, is always seen one at a time so
 * the cache only serves as a speedup process to avoid disk reading.
 *
 *
 * It attempts to cache at most 60% of the available memory
 */
internal class ImageCacheManager {

  private val imageMetaCache = HashMap<String, ImageItem>()
  private val imageLruCache: ImageLruCache by lazy {
    val maxSize =
        Runtime.getRuntime().maxMemory() - (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())
    ImageLruCache((maxSize * 0.6).toLong())
  }

  fun cacheImageItem(imageItem: ImageItem){
    imageMetaCache[imageItem.path] = imageItem
  }

  fun cacheImage(image: Image) {
    imageLruCache.put(image.path, image)
  }

  fun removeImageCache(path: String) {
    imageMetaCache.remove(path)
    imageLruCache.remove(path)
  }

  fun getCachedImage(path: String): Image? {
    return imageLruCache.get(path)
  }

  fun getCachedImageItem(path: String): ImageItem? {
    return imageMetaCache[path]
  }


}