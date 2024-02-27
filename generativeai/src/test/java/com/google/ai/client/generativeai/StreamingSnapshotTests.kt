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

package com.google.ai.client.generativeai

import com.google.ai.client.generativeai.type.BlockReason
import com.google.ai.client.generativeai.type.FinishReason
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.InvalidAPIKeyException
import com.google.ai.client.generativeai.type.PromptBlockedException
import com.google.ai.client.generativeai.type.ResponseStoppedException
import com.google.ai.client.generativeai.type.SerializationException
import com.google.ai.client.generativeai.type.ServerException
import com.google.ai.client.generativeai.util.goldenStreamingFile
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.ktor.http.HttpStatusCode
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withTimeout
import org.junit.Test

internal class StreamingSnapshotTests {
  private val testTimeout = 5.seconds

  @Test
  fun `short reply`() =
    goldenStreamingFile("success-basic-reply-short.txt") {
      val responses = model.generateContentStream()

      withTimeout(testTimeout) {
        val responseList = responses.toList()
        responseList.isEmpty() shouldBe false
        responseList.first().candidates.first().finishReason shouldBe FinishReason.STOP
        responseList.first().candidates.first().content.parts.isEmpty() shouldBe false
        responseList.first().candidates.first().safetyRatings.isEmpty() shouldBe false
      }
    }

  @Test
  fun `long reply`() =
    goldenStreamingFile("success-basic-reply-long.txt") {
      val responses = model.generateContentStream()

      withTimeout(testTimeout) {
        val responseList = responses.toList()
        responseList.isEmpty() shouldBe false
        responseList.forEach {
          it.candidates.first().finishReason shouldBe FinishReason.STOP
          it.candidates.first().content.parts.isEmpty() shouldBe false
          it.candidates.first().safetyRatings.isEmpty() shouldBe false
        }
      }
    }

  @Test
  fun `unknown enum`() =
    goldenStreamingFile("success-unknown-enum.txt") {
      val responses = model.generateContentStream()

      withTimeout(testTimeout) {
        responses.first {
          it.candidates.any { it.safetyRatings.any { it.category == HarmCategory.UNKNOWN } }
        }
      }
    }

  @Test
  fun `quotes escaped`() =
    goldenStreamingFile("success-quotes-escaped.txt") {
      val responses = model.generateContentStream()

      withTimeout(testTimeout) {
        val responseList = responses.toList()

        responseList.isEmpty() shouldBe false
        responseList.first().text!!.contains("\"")
      }
    }

  @Test
  fun `prompt blocked for safety`() =
    goldenStreamingFile("failure-prompt-blocked-safety.txt") {
      val responses = model.generateContentStream()

      withTimeout(testTimeout) {
        val exception = shouldThrow<PromptBlockedException> { responses.collect() }
        exception.response.promptFeedback?.blockReason shouldBe BlockReason.SAFETY
      }
    }

  @Test
  fun `empty content`() =
    goldenStreamingFile("failure-empty-content.txt") {
      val responses = model.generateContentStream()

      withTimeout(testTimeout) { shouldThrow<SerializationException> { responses.collect() } }
    }

  @Test
  fun `http errors`() =
    goldenStreamingFile("failure-http-error.txt", HttpStatusCode.PreconditionFailed) {
      val responses = model.generateContentStream()

      withTimeout(testTimeout) { shouldThrow<ServerException> { responses.collect() } }
    }

  @Test
  fun `stopped for safety`() =
    goldenStreamingFile("failure-finish-reason-safety.txt") {
      val responses = model.generateContentStream()

      withTimeout(testTimeout) {
        val exception = shouldThrow<ResponseStoppedException> { responses.collect() }
        exception.response.candidates.first().finishReason shouldBe FinishReason.SAFETY
      }
    }

  @Test
  fun `citation parsed correctly`() =
    goldenStreamingFile("success-citations.txt") {
      val responses = model.generateContentStream()

      withTimeout(testTimeout) {
        val responseList = responses.toList()
        responseList.any { it.candidates.any { it.citationMetadata.isNotEmpty() } } shouldBe true
      }
    }

  @Test
  fun `stopped for recitation`() =
    goldenStreamingFile("failure-recitation-no-content.txt") {
      val responses = model.generateContentStream()

      withTimeout(testTimeout) {
        val exception = shouldThrow<ResponseStoppedException> { responses.collect() }
        exception.response.candidates.first().finishReason shouldBe FinishReason.RECITATION
      }
    }

  @Test
  fun `image rejected`() =
    goldenStreamingFile("failure-image-rejected.txt", HttpStatusCode.BadRequest) {
      val responses = model.generateContentStream()

      withTimeout(testTimeout) { shouldThrow<ServerException> { responses.collect() } }
    }

  @Test
  fun `unknown model`() =
    goldenStreamingFile("failure-unknown-model.txt", HttpStatusCode.NotFound) {
      val responses = model.generateContentStream()

      withTimeout(testTimeout) { shouldThrow<ServerException> { responses.collect() } }
    }

  @Test
  fun `invalid api key`() =
    goldenStreamingFile("failure-api-key.txt", HttpStatusCode.BadRequest) {
      val responses = model.generateContentStream()

      withTimeout(testTimeout) { shouldThrow<InvalidAPIKeyException> { responses.collect() } }
    }
}
