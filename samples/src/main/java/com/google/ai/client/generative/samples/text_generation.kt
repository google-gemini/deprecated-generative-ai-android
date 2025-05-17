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

package com.google.ai.client.generative.samples

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.sample.R

// Set up your API Key
// ====================
//
// To use the Gemini API, you'll need an API key. To learn more, see
// the "Set up your API Key section" in the [Gemini API
// quickstart](https://ai.google.dev/gemini-api/docs/quickstart?lang=android#set-up-api-key).

suspend fun textGenTextOnlyPrompt() {
  // [START text_gen_text_only_prompt]
  import com.google.ai.client.generativeai.GenerativeModel
  val geminiApiKey = BuildConfig.geminiApiKey

  val generativeModel = GenerativeModel( modelName = "gemini-1.5-flash", apiKey = BuildConfig.geminiApiKey)

  val prompt = "Explain how AI works"
  val response = generativeModel.generateContent(prompt)
  print(response.text)
  // [END text_gen_text_only_prompt]
}

suspend fun textGenTextOnlyPromptStreaming() {
  // [START text_gen_text_only_prompt_streaming]
  import com.google.ai.client.generativeai.GenerativeModel
  val geminiApiKey = BuildConfig.geminiApiKey

  val generativeModel = GenerativeModel(modelName = "gemini-1.5-flash", apiKey = BuildConfig.apiKey)

  val prompt = "Write a story about a magic backpack."
  // Use streaming with text-only input
  generativeModel.generateContentStream(prompt).collect { chunk -> print(chunk.text) }

  // [END text_gen_text_only_prompt_streaming]
}

suspend fun textGenMultimodalOneImagePrompt(context: Context) {
  // [START text_gen_multimodal_one_image_prompt]
  val generativeModel =
      GenerativeModel(
          // Specify a Gemini model appropriate for your use case
          modelName = "gemini-1.5-flash",
          // Access your API key as a Build Configuration variable (see "Set up your API key" above)
          apiKey = BuildConfig.apiKey)

  val image: Bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.image)
  val inputContent = content {
    image(image)
    text("What's in this picture?")
  }

  val response = generativeModel.generateContent(inputContent)
  print(response.text)
  // [END text_gen_multimodal_one_image_prompt]
}

suspend fun textGenMultimodalOneImagePromptStreaming(context: Context) {
  // [START text_gen_multimodal_one_image_prompt_streaming]
  val generativeModel =
      GenerativeModel(
          // Specify a Gemini model appropriate for your use case
          modelName = "gemini-1.5-flash",
          // Access your API key as a Build Configuration variable (see "Set up your API key" above)
          apiKey = BuildConfig.apiKey)

  val image: Bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.image)
  val inputContent = content {
    image(image)
    text("What's in this picture?")
  }

  generativeModel.generateContentStream(inputContent).collect { chunk -> print(chunk.text) }
  // [END text_gen_multimodal_one_image_prompt_streaming]
}

suspend fun textGenMultimodalMultiImagePrompt(context: Context) {
  // [START text_gen_multimodal_multi_image_prompt]
  val generativeModel =
      GenerativeModel(
          // Specify a Gemini model appropriate for your use case
          modelName = "gemini-1.5-flash",
          // Access your API key as a Build Configuration variable (see "Set up your API key" above)
          apiKey = BuildConfig.apiKey)

  val image1: Bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.image1)
  val image2: Bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.image2)
  val inputContent = content {
    image(image1)
    image(image2)
    text("What's the difference between these pictures?")
  }

  val response = generativeModel.generateContent(inputContent)
  print(response.text)

  // [END text_gen_multimodal_multi_image_prompt]
}

suspend fun textGenMultimodalMultiImagePromptStreaming(context: Context) {
  // [START text_gen_multimodal_multi_image_prompt_streaming]
  val generativeModel =
      GenerativeModel(
          // Specify a Gemini model appropriate for your use case
          modelName = "gemini-1.5-flash",
          // Access your API key as a Build Configuration variable (see "Set up your API key" above)
          apiKey = BuildConfig.apiKey)

  val image1: Bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.image1)
  val image2: Bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.image2)
  val inputContent = content {
    image(image1)
    image(image2)
    text("What's the difference between these pictures?")
  }

  generativeModel.generateContentStream(inputContent).collect { chunk -> print(chunk.text) }
  // [END text_gen_multimodal_multi_image_prompt_streaming]
}

suspend fun textGenMultimodalVideoPrompt() {
  // [START text_gen_multimodal_video_prompt]
  // TODO
  // [END text_gen_multimodal_video_prompt]
}

suspend fun textGenMultimodalVideoPromptStreaming() {
  // [START text_gen_multimodal_video_prompt_streaming]
  // TODO
  // [END text_gen_multimodal_video_prompt_streaming]
}
