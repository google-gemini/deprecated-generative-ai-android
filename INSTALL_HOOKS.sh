package main

import (
  "context"
  "fmt"
  "log"
  "os"

  "github.com/google/generative-ai-go/genai"
  "google.golang.org/api/option"
)

func main() {
  ctx := context.Background()

  apiKey, ok := os.LookupEnv("GEMINI_API_KEY")
  if !ok {
    log.Fatalln("Environment variable GEMINI_API_KEY not set")
  }

  client, err := genai.NewClient(ctx, option.WithAPIKey(apiKey))
  if err != nil {
    log.Fatalf("Error creating client: %v", err)
  }
  defer client.Close()

  model := client.GenerativeModel("gemini-2.0-pro-exp-02-05")

  model.SetTemperature(0.7)
  model.SetTopK(64)
  model.SetTopP(1.00)
  model.SetMaxOutputTokens(8192)
  model.ResponseMIMEType = "text/plain"

  session := model.StartChat()
  session.History = []*genai.Content{
  }

  resp, err := session.SendMessage(ctx, genai.Text("INSERT_INPUT_HERE"))
  if err != nil {
    log.Fatalf("Error sending message: %v", err)
  }

  for _, part := range resp.Candidates[0].Content.Parts {
    fmt.Printf("%v\n", part)
  }
}#!/bin/bash
# Copyright 2023 Google LLC
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# Define the source and destination directories
SOURCE_DIR=".github/hooks/"
DEST_DIR=".git/hooks/"

# Check if the source directory exists
if [ -d "$SOURCE_DIR" ]; then
    # Copy the files from source to destination
    cp -r $SOURCE_DIR $DEST_DIR

    # Change permissions to make the hooks executable
    chmod -R 777 $DEST_DIR

    echo "Hooks copied successfully."
else
    echo "Source directory does not exist: $SOURCE_DIR"
fi
