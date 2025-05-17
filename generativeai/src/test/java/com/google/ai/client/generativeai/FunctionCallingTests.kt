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

package com.google.ai.client.generativeai

import com.google.ai.client.generativeai.type.FunctionCallPart
import com.google.ai.client.generativeai.type.Schema
import com.google.ai.client.generativeai.type.Tool
import com.google.ai.client.generativeai.type.defineFunction
import io.kotest.assertions.throwables.shouldThrowWithMessage
import io.kotest.matchers.shouldBe
import org.json.JSONObject
import org.junit.Test

internal class FunctionCallingTests {

  @Test
  fun `function calls with valid args should succeed`() = doBlocking {
    val functionDeclaration =
      defineFunction(
        "name",
        "description",
        Schema.str("param1", "description"),
        Schema.num("param2", "description"),
        Schema.bool("param3", "description"),
        Schema.int("param4", "description"),
      ) { param1, param2, param3, param4 ->
        JSONObject(mapOf("result" to "success"))
      }
    val model = GenerativeModel("model", "key", tools = listOf(Tool(listOf(functionDeclaration))))

    val functionCall =
      FunctionCallPart(
        "name",
        mapOf(
          ("param1" to "valid parameter"),
          ("param2" to "2.2"),
          ("param3" to "false"),
          ("param4" to "2"),
        ),
      )

    val result = model.executeFunction(functionCall)

    result["result"] shouldBe "success"
  }

  @Test
  fun `function calls with invalid args should fail`() = doBlocking {
    val functionDeclaration =
      defineFunction("name", "description", Schema.str("param1", "description")) { param1 ->
        JSONObject(mapOf("result" to "success"))
      }
    val model = GenerativeModel("model", "key", tools = listOf(Tool(listOf(functionDeclaration))))

    val functionCall = FunctionCallPart("name", mapOf("param1" to null))

    shouldThrowWithMessage<RuntimeException>(
      "Missing argument for parameter \"param1\" for function \"name\""
    ) {
      model.executeFunction(functionCall)
    }
  }
}
