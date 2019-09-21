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
 * Class responsible for managing metadata export preferences
 */
class MetadataExportPreferencesManager(val preferences: Preferences) {

  private companion object PreferencesKeys{
    const val EXPORT_PREFS = "export_prefs"
  }

  private var exportPreferencesList = HashSet<String>()

  fun addFieldToExport(fieldId: String){
    exportPreferencesList.add(fieldId)
  }

  fun removeFieldToExport(fieldId: String){
    exportPreferencesList.remove(fieldId)
  }

  fun saveMetadataExportPreferences(){
    val sb = StringBuilder()
    exportPreferencesList.forEach { field ->
      sb.append(field).append(";")
    }
    preferences.put(EXPORT_PREFS, sb.toString())
  }

  fun getSaveMetadataExportPreferences(){
    exportPreferencesList = preferences.get(EXPORT_PREFS, "").split(";").toHashSet()
  }

  fun containsPreference(tag: String): Boolean{
    return exportPreferencesList.contains(tag)
  }
}