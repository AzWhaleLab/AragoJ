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

package com.aragoj.session

import com.aragoj.equation.model.EquationItem
import com.aragoj.session.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.xml.bind.JAXBContext

class SessionManager {
  private var cachedSession: Session? = null

  /**
   * Attempts to open a session in the given path.
   *
   * @throws javax.xml.bind.JAXBException
   * @throws java.io.FileNotFoundException
   */
  suspend fun openSession(path: String): Session = withContext(Dispatchers.IO) {
    val file = getExistingFile(path)
    val jaxbContext = JAXBContext.newInstance(Session::class.java, EditorItem::class.java,
        EditorItemLine::class.java, EquationItem::class.java, EditorItemZoom::class.java,
        EditorItemArea::class.java, EditorItemPosition::class.java)
    val jaxbUnmarshaller = jaxbContext.createUnmarshaller()
    val session = jaxbUnmarshaller.unmarshal(file) as Session
    session.path = file.path
    session
  }
}