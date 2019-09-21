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

package com.aragoj.imageprocessor.imagereaders

import com.adobe.xmp.XMPException
import com.adobe.xmp.properties.XMPPropertyInfo
import com.aragoj.imageprocessor.model.Image
import com.aragoj.imageprocessor.model.ImageItem
import com.aragoj.imageprocessor.util.ImageUtility
import com.drew.imaging.ImageMetadataReader
import com.drew.metadata.Metadata
import com.drew.metadata.MetadataException
import com.drew.metadata.exif.ExifIFD0Directory
import com.drew.metadata.exif.ExifSubIFDDirectory
import com.drew.metadata.exif.GpsDirectory
import com.drew.metadata.file.FileMetadataDirectory
import com.drew.metadata.xmp.XmpDirectory
import java.io.File
import java.nio.file.Paths
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class DefaultImageReader {

  fun readImageItem(path: String): ImageItem {
    return ImageItem(
        Paths.get(path).fileName.toString(), path,
        javafx.scene.image.Image(path, 50.0, 50.0, true, true))
  }

  fun readImage(path: String): Image {
    val image = javafx.scene.image.Image(path)
    val metadata =
        processMetadata(ImageMetadataReader.readMetadata(File(path)), image.width.toInt(),
            image.height.toInt())
    return Image(path, metadata, image)
  }


  private fun processMetadata(metadata: Metadata, width: Int, height: Int): Image.Metadata {
    val result = Image.Metadata()

    result.addAll(getFileMetadata(metadata, width, height))
    getExifMetadata(metadata)?.also { exif -> result.add(exif) }
    getGPSMetadata(metadata)?.also{ gps -> result.add(gps)}
    getXmpMetadata(metadata)?.also{ xmp -> result.add(xmp)}
    getDerivedMetadata(metadata)?.also{ derived -> result.add(derived)}
    return result
  }

  private fun getDerivedMetadata(metadata: Metadata): Image.Metadata.Directory?{
    val derived: Image.Metadata.Directory = Image.Metadata.Directory("Derived")

    val exifSubIFDDirectory = metadata.getFirstDirectoryOfType(
        ExifSubIFDDirectory::class.java)

    var focalLength = 0.0
    var eqFocalLength = 0.0
    var aperture = 0.0
    var shutterSpeed = 0.0
    var iso = 0.0

    try {
      if (exifSubIFDDirectory.containsTag(ExifSubIFDDirectory.TAG_35MM_FILM_EQUIV_FOCAL_LENGTH)) {
        eqFocalLength =
            exifSubIFDDirectory.getDouble(ExifSubIFDDirectory.TAG_35MM_FILM_EQUIV_FOCAL_LENGTH)
      }
      if (exifSubIFDDirectory.containsTag(ExifSubIFDDirectory.TAG_FOCAL_LENGTH)) {
        focalLength = exifSubIFDDirectory.getDouble(ExifSubIFDDirectory.TAG_FOCAL_LENGTH)
      }
      if (exifSubIFDDirectory.containsTag(ExifSubIFDDirectory.TAG_SHUTTER_SPEED)) {
        val desc = exifSubIFDDirectory.getDescription(ExifSubIFDDirectory.TAG_SHUTTER_SPEED)
        val split = desc.substring(0, desc.length - 4).split("/")
        shutterSpeed = split[0].toDouble() / split[1].toDouble()
      } else if (exifSubIFDDirectory.containsTag(ExifSubIFDDirectory.TAG_EXPOSURE_TIME)) {
        shutterSpeed = exifSubIFDDirectory.getDouble(ExifSubIFDDirectory.TAG_EXPOSURE_TIME)
      }
      if (exifSubIFDDirectory.containsTag(ExifSubIFDDirectory.TAG_ISO_EQUIVALENT)) {
        iso = exifSubIFDDirectory.getDouble(ExifSubIFDDirectory.TAG_ISO_EQUIVALENT)
      }
      if (exifSubIFDDirectory.containsTag(ExifSubIFDDirectory.TAG_FNUMBER)) {
        aperture = exifSubIFDDirectory.getDouble(ExifSubIFDDirectory.TAG_FNUMBER)
      } else if (exifSubIFDDirectory.containsTag(ExifSubIFDDirectory.TAG_APERTURE)) {
        aperture = exifSubIFDDirectory.getDouble(ExifSubIFDDirectory.TAG_APERTURE)
      }
    } catch (e: MetadataException) {
      e.printStackTrace()
    }

    if (eqFocalLength != 0.0 && focalLength != 0.0) {
      val eqFactor = ImageUtility.getScaleFactor(eqFocalLength, focalLength)
      val coc = ImageUtility.getCircleOfConfusion(eqFactor)
      val fovValue = ImageUtility.getFOV(focalLength, eqFactor)
      val scaleFactor = Image.Metadata.Item("Scale Factor to 35mm", String.format("%.1f", eqFactor))
      val cocRow = Image.Metadata.Item("Circle of Confusion", String.format("%.3f", coc) + " mm")
      val fov = Image.Metadata.Item("Field of View", String.format("%.1f", fovValue) + " deg")
      derived.addItems(scaleFactor, cocRow, fov)

      if (aperture != 0.0) {
        val hyperfocalDistance = ImageUtility.getHyperfocalDistance(eqFactor, focalLength, aperture)
        val hyperFocal =
            Image.Metadata.Item("Hyperfocal Distance", String.format("%.2f", hyperfocalDistance) + " m")
        derived.addItem(hyperFocal)
      }
    }
    if (aperture != 0.0 && iso != 0.0 && shutterSpeed != 0.0) {
      val lightValue = ImageUtility.getLightValue(aperture, shutterSpeed, iso)
      val lightV = Image.Metadata.Item("Light Value", String.format("%.1f", lightValue))
      derived.addItem(lightV)
    }
    return if(!derived.isEmpty()) derived else null
  }

  private fun getXmpMetadata(metadata: Metadata): Image.Metadata.Directory? {
    var xmp: Image.Metadata.Directory? = null

    val xmpDirectories = metadata.getDirectoriesOfType(XmpDirectory::class.java)
    if(xmpDirectories != null){
      xmp = Image.Metadata.Directory("XMP")

      for (xmpDirectory in xmpDirectories) {
        try {
          val xmpMeta = xmpDirectory.xmpMeta
          val iterator = xmpMeta.iterator()
          while (iterator.hasNext()) {
            val xmpPropertyInfo = iterator.next() as XMPPropertyInfo
            val path = xmpPropertyInfo.path
            path?.apply { xmp.addItem(Image.Metadata.Item(path, xmpPropertyInfo.value)) }
          }
        } catch (e: XMPException) {
          e.printStackTrace()
        }
      }
    }
    return if(xmp != null && !xmp.isEmpty()) xmp else null
  }

  private fun getGPSMetadata(metadata: Metadata): Image.Metadata.Directory? {
    var gps: Image.Metadata.Directory? = null

    val gpsDirectory = metadata.getFirstDirectoryOfType(GpsDirectory::class.java)
    if (gpsDirectory != null) {
      gps = Image.Metadata.Directory("GPS")

      val location = gpsDirectory.geoLocation
      if (location != null) {
        val latRow = Image.Metadata.Item("Latitude", location.latitude.toString())
        val longRow = Image.Metadata.Item("Longitude", location.longitude.toString())
        gps.addItem(latRow)
        gps.addItem(longRow)
        if (gpsDirectory.containsTag(GpsDirectory.TAG_ALTITUDE_REF)) {
          val row =
              Image.Metadata.Item("Altitude Ref", gpsDirectory.getDescription(GpsDirectory.TAG_ALTITUDE_REF))
          gps.addItem(row)
        }
        if (gpsDirectory.containsTag(GpsDirectory.TAG_ALTITUDE)) {
          val row =
              Image.Metadata.Item("Altitude", gpsDirectory.getDescription(GpsDirectory.TAG_ALTITUDE))
          gps.addItem(row)
        }
      }
    }
    return if(gps != null && !gps.isEmpty()) gps else null
  }

  private fun getExifMetadata(metadata: Metadata): Image.Metadata.Directory? {
    var exif: Image.Metadata.Directory? = null

    val exifIFD0Directory = metadata.getFirstDirectoryOfType(
        ExifIFD0Directory::class.java)
    val exifSubIFDDirectory = metadata.getFirstDirectoryOfType(
        ExifSubIFDDirectory::class.java)

    if (exifIFD0Directory != null && exifSubIFDDirectory != null) {
      exif = Image.Metadata.Directory("Exif")
      if (exifIFD0Directory.containsTag(ExifIFD0Directory.TAG_MAKE)) {
        val row = Image.Metadata.Item("Make", exifIFD0Directory.getString(ExifIFD0Directory.TAG_MAKE))
        exif.addItem(row)
      }
      if (exifIFD0Directory.containsTag(ExifIFD0Directory.TAG_MODEL)) {
        val row = Image.Metadata.Item("Model", exifIFD0Directory.getString(ExifIFD0Directory.TAG_MODEL))
        exif.addItem(row)
      }
      if (exifIFD0Directory.containsTag(ExifIFD0Directory.TAG_X_RESOLUTION)) {
        val row =
            Image.Metadata.Item("X Resolution", exifIFD0Directory.getString(ExifIFD0Directory.TAG_X_RESOLUTION))
        exif.addItem(row)
      }
      if (exifIFD0Directory.containsTag(ExifIFD0Directory.TAG_Y_RESOLUTION)) {
        val row =
            Image.Metadata.Item("Y Resolution", exifIFD0Directory.getString(ExifIFD0Directory.TAG_Y_RESOLUTION))
        exif.addItem(row)
      }

      for (tag in exifSubIFDDirectory.tags) {
        val row = Image.Metadata.Item(tag.tagName, tag.description)
        exif.addItem(row)
      }
    }
    return if(exif != null && !exif.isEmpty()) exif else null
  }


  private fun getFileMetadata(metadata: Metadata, width: Int, height: Int): List<Image.Metadata.Item> {
    val list = ArrayList<Image.Metadata.Item>()
    val fileDirectory = metadata.getFirstDirectoryOfType(
        FileMetadataDirectory::class.java)
    if (fileDirectory != null) {
      if (fileDirectory.containsTag(FileMetadataDirectory.TAG_FILE_MODIFIED_DATE)) {
        val date = fileDirectory.getDate(FileMetadataDirectory.TAG_FILE_MODIFIED_DATE)
        val tModDate = Image.Metadata.Item("Modified date ${getDate(date)}")
        list.add(tModDate)
      }
      if (fileDirectory.containsTag(FileMetadataDirectory.TAG_FILE_SIZE)) {
        try {
          val tSize = Image.Metadata.Item("Size",
              formatFileSize(fileDirectory.getLong(FileMetadataDirectory.TAG_FILE_SIZE)))
          list.add(tSize)
        } catch (e: MetadataException) {
        }
      }
    }
    val tWidth = Image.Metadata.Item("Width $width pixels")
    val tHeight = Image.Metadata.Item("Height $height pixels")
    list.add(tWidth)
    list.add(tHeight)
    return list
  }

  private fun getDate(date: Date): String {
    return SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(date)
  }

  private fun formatFileSize(size: Long): String {
    var hrSize: String? = null

    val b = size.toDouble()
    val k = size / 1024.0
    val m = size / 1024.0 / 1024.0
    val g = size / 1024.0 / 1024.0 / 1024.0
    val t = size / 1024.0 / 1024.0 / 1024.0 / 1024.0

    val dec = DecimalFormat("0.00")

    if (t > 1) {
      hrSize = dec.format(t) + " TB"
    } else if (g > 1) {
      hrSize = dec.format(g) + " GB"
    } else if (m > 1) {
      hrSize = dec.format(m) + " MB"
    } else if (k > 1) {
      hrSize = dec.format(k) + " KB"
    } else {
      hrSize = dec.format(b) + " Bytes"
    }

    return hrSize
  }

  fun getSupportedFormats(): List<String> {
    return listOf(".jpeg")
  }
}