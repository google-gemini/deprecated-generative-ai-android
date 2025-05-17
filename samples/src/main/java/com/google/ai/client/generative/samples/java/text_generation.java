// Copyright 2024 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.ai.client.generative.samples.java;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.ai.sample.R;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

// Set up your API Key
// ====================
//
// To use the Gemini API, you'll need an API key. To learn more, see
// the "Set up your API Key section" in the [Gemini API
// quickstart](https://ai.google.dev/gemini-api/docs/quickstart?lang=android#set-up-api-key).

class TextGeneration {
  void TextGenTextOnlyPrompt() {
    // [START text_gen_text_only_prompt]
    // Specify a Gemini model appropriate for your use case
    import com.google.ai.client.generativeai.GenerativeModel;
    import com.google.ai.client.generativeai.java.GenerativeModelFutures;
    import com.google.ai.client.generativeai.type.Content;
    import com.google.ai.client.generativeai.type.GenerateContentResponse;
    
    GenerativeModel gm =
        new GenerativeModel(
            /* modelName */ "gemini-1.5-flash",
            // Access your API key as a Build Configuration variable (see "Set up your API key"
            // above)
            /* apiKey */ BuildConfig.apiKey);
    GenerativeModelFutures model = GenerativeModelFutures.from(gm);

    Content content =
        new Content.Builder().addText("Write a story about a magic backpack.").build();

    // For illustrative purposes only. You should use an executor that fits your needs.
    Executor executor = Executors.newSingleThreadExecutor();

    ListenableFuture<GenerateContentResponse> response = model.generateContent(content);
    Futures.addCallback(
        response,
        new FutureCallback<GenerateContentResponse>() {
          @Override
          public void onSuccess(GenerateContentResponse result) {
            String resultText = result.getText();
            System.out.println(resultText);
          }

          @Override
          public void onFailure(Throwable t) {
            t.printStackTrace();
          }
        },
        executor);
    // [END text_gen_text_only_prompt]
  }

  void TextGenTextOnlyPromptStreaming() {
    // [START text_gen_text_only_prompt_streaming]
    // Specify a Gemini model appropriate for your use case
    GenerativeModel gm =
        new GenerativeModel(
            /* modelName */ "gemini-1.5-flash",
            // Access your API key as a Build Configuration variable (see "Set up your API key"
            // above)
            /* apiKey */ BuildConfig.apiKey);
    GenerativeModelFutures model = GenerativeModelFutures.from(gm);

    Content content =
        new Content.Builder().addText("Write a story about a magic backpack.").build();

    Publisher<GenerateContentResponse> streamingResponse = model.generateContentStream(content);

    StringBuilder outputContent = new StringBuilder();

    streamingResponse.subscribe(
        new Subscriber<GenerateContentResponse>() {
          @Override
          public void onNext(GenerateContentResponse generateContentResponse) {
            String chunk = generateContentResponse.getText();
            outputContent.append(chunk);
          }

          @Override
          public void onComplete() {
            System.out.println(outputContent);
          }

          @Override
          public void onError(Throwable t) {
            t.printStackTrace();
          }

          @Override
          public void onSubscribe(Subscription s) {
            s.request(Long.MAX_VALUE);
          }
        });
    // [END text_gen_text_only_prompt_streaming]
  }

  void TextGenMultimodalOneImagePrompt(Context context) {
    // [START text_gen_multimodal_one_image_prompt]
    // Specify a Gemini model appropriate for your use case
    GenerativeModel gm =
        new GenerativeModel(
            /* modelName */ "gemini-1.5-flash",
            // Access your API key as a Build Configuration variable (see "Set up your API key"
            // above)
            /* apiKey */ BuildConfig.apiKey);
    GenerativeModelFutures model = GenerativeModelFutures.from(gm);

    Bitmap image = BitmapFactory.decodeResource(context.getResources(), R.drawable.image);

    Content content =
        new Content.Builder()
            .addText("What's different between these pictures?")
            .addImage(image)
            .build();

    // For illustrative purposes only. You should use an executor that fits your needs.
    Executor executor = Executors.newSingleThreadExecutor();

    ListenableFuture<GenerateContentResponse> response = model.generateContent(content);
    Futures.addCallback(
        response,
        new FutureCallback<GenerateContentResponse>() {
          @Override
          public void onSuccess(GenerateContentResponse result) {
            String resultText = result.getText();
            System.out.println(resultText);
          }

          @Override
          public void onFailure(Throwable t) {
            t.printStackTrace();
          }
        },
        executor);
    // [END text_gen_multimodal_one_image_prompt]
  }

  void TextGenMultimodalOneImagePromptStreaming(Context context) {
    // [START text_gen_multimodal_one_image_prompt_streaming]
    // Specify a Gemini model appropriate for your use case
    GenerativeModel gm =
        new GenerativeModel(
            /* modelName */ "gemini-1.5-flash",
            // Access your API key as a Build Configuration variable (see "Set up your API key"
            // above)
            /* apiKey */ BuildConfig.apiKey);
    GenerativeModelFutures model = GenerativeModelFutures.from(gm);

    Bitmap image1 = BitmapFactory.decodeResource(context.getResources(), R.drawable.image1);
    Bitmap image2 = BitmapFactory.decodeResource(context.getResources(), R.drawable.image2);

    Content content =
        new Content.Builder()
            .addText("What's different between these pictures?")
            .addImage(image1)
            .addImage(image2)
            .build();

    // For illustrative purposes only. You should use an executor that fits your needs.
    Executor executor = Executors.newSingleThreadExecutor();

    Publisher<GenerateContentResponse> streamingResponse = model.generateContentStream(content);

    StringBuilder outputContent = new StringBuilder();

    streamingResponse.subscribe(
        new Subscriber<GenerateContentResponse>() {
          @Override
          public void onNext(GenerateContentResponse generateContentResponse) {
            String chunk = generateContentResponse.getText();
            outputContent.append(chunk);
          }

          @Override
          public void onComplete() {
            System.out.println(outputContent);
          }

          @Override
          public void onError(Throwable t) {
            t.printStackTrace();
          }

          @Override
          public void onSubscribe(Subscription s) {
            s.request(Long.MAX_VALUE);
          }
        });
    // [END text_gen_multimodal_one_image_prompt_streaming]
  }

  void TextGenMultimodalMultiImagePrompt(Context context) {
    // [START text_gen_multimodal-multi-image-prompt]
    // Specify a Gemini model appropriate for your use case
    GenerativeModel gm =
        new GenerativeModel(
            /* modelName */ "gemini-1.5-flash",
            // Access your API key as a Build Configuration variable (see "Set up your API key"
            // above)
            /* apiKey */ BuildConfig.apiKey);
    GenerativeModelFutures model = GenerativeModelFutures.from(gm);

    Bitmap image1 = BitmapFactory.decodeResource(context.getResources(), R.drawable.image1);
    Bitmap image2 = BitmapFactory.decodeResource(context.getResources(), R.drawable.image2);

    Content content =
        new Content.Builder()
            .addText("What's different between these pictures?")
            .addImage(image1)
            .addImage(image2)
            .build();

    // For illustrative purposes only. You should use an executor that fits your needs.
    Executor executor = Executors.newSingleThreadExecutor();

    ListenableFuture<GenerateContentResponse> response = model.generateContent(content);
    Futures.addCallback(
        response,
        new FutureCallback<GenerateContentResponse>() {
          @Override
          public void onSuccess(GenerateContentResponse result) {
            String resultText = result.getText();
            System.out.println(resultText);
          }

          @Override
          public void onFailure(Throwable t) {
            t.printStackTrace();
          }
        },
        executor);
    // [END text_gen_multimodal_multi_image_prompt]
  }

  void TextGenMultimodalMultiImagePromptStreaming(Context context) {
    // [START text_gen_multimodal_multi_image_prompt_streaming]
    // Specify a Gemini model appropriate for your use case
    GenerativeModel gm =
        new GenerativeModel(
            /* modelName */ "gemini-1.5-flash",
            // Access your API key as a Build Configuration variable (see "Set up your API key"
            // above)
            /* apiKey */ BuildConfig.apiKey);
    GenerativeModelFutures model = GenerativeModelFutures.from(gm);

    Bitmap image1 = BitmapFactory.decodeResource(context.getResources(), R.drawable.image1);
    Bitmap image2 = BitmapFactory.decodeResource(context.getResources(), R.drawable.image2);

    Content content =
        new Content.Builder()
            .addText("What's different between these pictures?")
            .addImage(image1)
            .addImage(image2)
            .build();

    Executor executor; // = ...

    Publisher<GenerateContentResponse> streamingResponse = model.generateContentStream(content);

    StringBuilder outputContent = new StringBuilder();

    streamingResponse.subscribe(
        new Subscriber<GenerateContentResponse>() {
          @Override
          public void onNext(GenerateContentResponse generateContentResponse) {
            String chunk = generateContentResponse.getText();
            outputContent.append(chunk);
          }

          @Override
          public void onComplete() {
            System.out.println(outputContent);
          }

          @Override
          public void onError(Throwable t) {
            t.printStackTrace();
          }

          @Override
          public void onSubscribe(Subscription s) {
            s.request(Long.MAX_VALUE);
          }
        });
    // [END text_gen_multimodal_multi_image_prompt_streaming]
  }

  void TextGenMultimodalVideoPrompt() {
    // [START text_gen_multimodal_video_prompt]
    // TODO
    // [END text_gen_multimodal_video_prompt]
  }

  void TextGenMultimodalVideoPromptStreaming() {
    // [START text_gen_multimodal_video_prompt_streaming]
    // TODO
    // [END text_gen_multimodal_video_prompt_streaming]
  }
}
