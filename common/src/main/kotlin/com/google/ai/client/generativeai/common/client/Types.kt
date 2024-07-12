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

package com.google.ai.client.generativeai.common.client

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class GenerationConfig(
  val temperature: Float? = null,
  val topP: Float? = null,
  val topK: Int? = null,
  val candidateCount: Int? = null,
  val maxOutputTokens: Int? = null,
  val stopSequences: List<String> = emptyList(),
  val responseMimeType: String? = null,
  val presencePenalty: Float? = null,
  val frequencyPenalty: Float? = null,
  val responseSchema: Schema? = null,
)

@Serializable
data class Tool(
  val functionDeclarations: List<FunctionDeclaration> = emptyList(),
  // This is a json object because it is not possible to make a data class with no parameters.
  val codeExecution: JsonObject? = null,
)

@Serializable data class ToolConfig(val functionCallingConfig: FunctionCallingConfig? = null)

@Serializable
data class FunctionCallingConfig(val mode: Mode = Mode.UNSPECIFIED) {
  @Serializable
  enum class Mode {
    @SerialName("MODE_UNSPECIFIED") UNSPECIFIED,
    AUTO,
    ANY,
    NONE
  }
}

@Serializable
data class FunctionDeclaration(
  val name: String,
  val description: String,
  val parameters: Schema? = null,
)

@Serializable
data class Schema(
  val type: String,
  val description: String = "",
  val format: String = "",
  val nullable: Boolean = false,
  val enum: List<String> = emptyList(),
  val properties: Map<String, Schema> = emptyMap(),
  val required: List<String> = emptyList(),
  val items: Schema? = null,
)
