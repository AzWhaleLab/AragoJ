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

package com.aragoj.base

import com.aragoj.utils.Translator
import de.saxsys.mvvmfx.FxmlView
import javafx.fxml.Initializable
import javafx.stage.Stage
import java.net.URL
import java.util.*

abstract class BaseView<V: ViewModel>: FxmlView<V>, Initializable {

  lateinit var stage: Stage

  override fun initialize(location: URL?, resources: ResourceBundle?) {

  }

  fun getString(id: String): String{
    return Translator.getString(id)
  }
}