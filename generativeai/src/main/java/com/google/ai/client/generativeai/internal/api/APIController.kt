/*
 * Copyright 2023 Google LLC
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

package com.google.ai.client.generativeai.internal.api

import com.google.ai.client.generativeai.BuildConfig
import com.google.ai.client.generativeai.internal.util.decodeToFlow
import com.google.ai.client.generativeai.type.ServerException
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.preparePost
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlin.time.Duration
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.timeout
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

internal const val DOMAIN = "https://generativelanguage.googleapis.com"

internal val JSON = Json {
  ignoreUnknownKeys = true
  prettyPrint = false
}

/**
 * Backend class for interfacing with the Gemini API.
 *
 * This class handles making HTTP requests to the API and streaming the responses back.
 *
 * @param httpEngine The HTTP client engine to be used for making requests. Defaults to CIO engine.
 *   Exposed primarily for DI in tests.
 * @property key The API key used for authentication.
 * @property model The model to use for generation.
 * @property apiVersion the endpoint version to communicate with.
 * @property timeout the maximum amount of time for a request to take in the initial exchange.
 */
internal class APIController(
  private val key: String,
  model: String,
  private val apiVersion: String,
  private val timeout: Duration,
  httpEngine: HttpClientEngine = OkHttp.create(),
) {
  private val model = fullModelName(model)

  private val client =
    HttpClient(httpEngine) {
      install(HttpTimeout) {
        requestTimeoutMillis = timeout.inWholeMilliseconds
        socketTimeoutMillis = 80_000
      }
      install(ContentNegotiation) { json(JSON) }
    }

  suspend fun generateContent(request: GenerateContentRequest): GenerateContentResponse =
    client
      .post("$DOMAIN/$apiVersion/$model:generateContent") { applyCommonConfiguration(request) }
      .also { validateResponse(it) }
      .body()

  fun generateContentStream(request: GenerateContentRequest): Flow<GenerateContentResponse> {
    return client.postStream<GenerateContentResponse>(
      "$DOMAIN/$apiVersion/$model:streamGenerateContent?alt=sse"
    ) {
      applyCommonConfiguration(request)
    }
  }

  suspend fun countTokens(request: CountTokensRequest): CountTokensResponse =
    client
      .post("$DOMAIN/$apiVersion/$model:countTokens") { applyCommonConfiguration(request) }
      .also { validateResponse(it) }
      .body()

  private fun HttpRequestBuilder.applyCommonConfiguration(request: Request) {
    when (request) {
      is GenerateContentRequest -> setBody<GenerateContentRequest>(request)
      is CountTokensRequest -> setBody<CountTokensRequest>(request)
    }
    contentType(ContentType.Application.Json)
    header("x-goog-api-key", key)
    header("x-goog-api-client", "genai-android/${BuildConfig.VERSION_NAME}")
  }
}

/**
 * Ensures the model name provided has a `models/` prefix
 *
 * Models must be prepended with the `models/` prefix when communicating with the backend.
 */
private fun fullModelName(name: String): String = name.takeIf { it.contains("/") } ?: "models/$name"

/**
 * Makes a POST request to the specified [url] and returns a [Flow] of deserialized response objects
 * of type [R]. The response is expected to be a stream of JSON objects that are parsed in real-time
 * as they are received from the server.
 *
 * This function is intended for internal use within the client that handles streaming responses.
 *
 * Example usage:
 * ```
 * val client: HttpClient = HttpClient(CIO)
 * val request: Request = GenerateContentRequest(...)
 * val url: String = "http://example.com/stream"
 *
 * val responses: GenerateContentResponse = client.postStream(url) {
 *   setBody(request)
 *   contentType(ContentType.Application.Json)
 * }
 * responses.collect {
 *   println("Got a response: $it")
 * }
 * ```
 *
 * @param R The type of the response object.
 * @param url The URL to which the POST request will be made.
 * @param config An optional [HttpRequestBuilder] callback for request configuration.
 * @return A [Flow] of response objects of type [R].
 */
private inline fun <reified R : Response> HttpClient.postStream(
  url: String,
  crossinline config: HttpRequestBuilder.() -> Unit = {}
): Flow<R> = channelFlow {
  launch(CoroutineName("postStream")) {
    preparePost(url) { config() }
      .execute {
        validateResponse(it)

        val channel = it.bodyAsChannel()
        val flow = JSON.decodeToFlow<R>(channel)

        flow.collect { send(it) }
      }
  }
}

private suspend fun validateResponse(response: HttpResponse) {
  if (response.status != HttpStatusCode.OK) {
    val text = response.bodyAsText()
    val message =
      try {
        JSON.decodeFromString<GRpcErrorResponse>(text).error.message
      } catch (e: Throwable) {
        "Unexpected Response:\n$text"
      }

    throw ServerException(message)
  }
}
