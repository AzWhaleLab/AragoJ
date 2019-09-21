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

package com.aragoj.mainscreen

import com.aragoj.base.ViewModel
import com.aragoj.mainscreen.io.preferences.IoPreferencesManager
import com.aragoj.mainscreen.io.preferences.MetadataExportPreferencesManager
import com.aragoj.mainscreen.menu.MenuItem
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject


class MainScreenViewModel(val metadataExportPrefsManager: MetadataExportPreferencesManager,
                          val ioPrefsManager: IoPreferencesManager) : ViewModel() {

  val events: PublishSubject<MainScreenEvent> = PublishSubject.create()
  val state: BehaviorSubject<MainScreenState> = BehaviorSubject.create()

  override fun onViewAdded() {
    super.onViewAdded()
  }

  override fun onViewRemoved() {
    super.onViewRemoved()
  }

  /**
   * Session handling
   */

  private fun startNewSession() {

  }

  fun saveSession(){

  }

  /**
   * Menu items handling
   */

  fun handleMenuItemClick(menuItem: MenuItem){
    when(menuItem){
      MenuItem.NEW_SESSION -> events.onNext(ShowNewSessionDialog())
    }
  }

  fun handleExitClick(){
    metadataExportPrefsManager.saveMetadataExportPreferences()
    events.onNext(ShowCloseDialog())
  }

  fun handleCloseVerificationDialogInput(shouldSave: Boolean){
    if(shouldSave) saveSession()
    exitApp()
  }

  fun handleNewSessionVerificationDialogInput(shouldSave: Boolean){
    if(shouldSave) saveSession()
    startNewSession()
  }


}