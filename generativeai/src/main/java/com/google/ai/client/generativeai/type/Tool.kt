/*
 * Copyright 2024 Google LLC
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

package com.google.ai.client.generativeai.type

import org.json.JSONObject

/**
 * Contains a set of function declarations that the model has access to. These can be used to gather
 * information, or complete tasks
 *
 * @param functionDeclarations The set of functions that this tool allows the model access to
 * @param codeExecution This is a flag value to enable Code Execution. Use [CODE_EXECUTION].
 * @param googleSearchRetrieval This is a Retrieval tool that is powered by Google search.
 */
class Tool
@JvmOverloads
constructor(
  val functionDeclarations: List<FunctionDeclaration>? = null,
  val codeExecution: JSONObject? = null,
  val googleSearchRetrieval: GoogleSearchRetrieval? = null
) {
  companion object {
    @JvmField val CODE_EXECUTION = Tool(codeExecution = JSONObject())
  }
}
