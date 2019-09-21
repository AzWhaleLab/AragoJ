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

package com.aragoj.mainscreen.io.preferences

import java.util.prefs.Preferences

/**
 * This class manages the preferences of mainscreen I/O operations (opening, saving, exporting...)
 */
class IoPreferencesManager(val preferences: Preferences) {

  private companion object PreferencesKeys{
    const val FILECHOOSER_OPEN_SESSION = "filechooser_open_session_location"
    const val FILECHOOSER_SAVEAS_SESSION = "filechooser_saveas_session_location";
    const val FILECHOOSER_EXPORTCSV_LASTOPENED = "filechooser_exportcsv_location";
    const val FILECHOOSER_IMPORT_IMAGES = "filechooser_import_images_location";
  }

  /**
   * Retrieves the last opened location of a session.
   * Returns empty string if there's no recorded location
   */
  fun getLastOpenedSessionLocation(): String {
    return preferences.get(
        FILECHOOSER_OPEN_SESSION, "")
  }

  fun saveLastOpenedSessionLocation(location: String){
    preferences.put(
        FILECHOOSER_OPEN_SESSION, location)
  }

  /**
   * Retrieves the last export location of a session.
   * Returns empty string if there's no recorded location
   */
  fun getLastOpenedExportCSVLocation(): String {
    return preferences.get(
        FILECHOOSER_EXPORTCSV_LASTOPENED, "")
  }

  fun saveLastOpenedExportCSVLocation(location: String){
    preferences.put(
        FILECHOOSER_EXPORTCSV_LASTOPENED, location)
  }

  /**
   * Retrieves the last saved location of a session.
   * Returns empty string if there's no recorded location
   */
  fun getLastSavedAsSessionLocation(): String {
    return preferences.get(
        FILECHOOSER_SAVEAS_SESSION, "")
  }

  fun saveLastSavedAsSessionLocation(location: String){
    preferences.put(
        FILECHOOSER_SAVEAS_SESSION, location)
  }

  /**
   * Retrieves the last import location of images.
   * Returns empty string if there's no recorded location
   */
  fun getLastOpenedImpotImagesLocation(): String {
    return preferences.get(
        FILECHOOSER_IMPORT_IMAGES, "")
  }

  fun saveLastOpenedImpotImagesLocation(location: String){
    preferences.put(
        FILECHOOSER_IMPORT_IMAGES, location)
  }
}