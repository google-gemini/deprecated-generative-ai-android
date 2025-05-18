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

package com.google.ai.sample.feature.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.asTextOrNull
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException

class ChatViewModel(
    generativeModel: GenerativeModel
) : ViewModel() {
    private val chat = generativeModel.startChat(
        history = listOf(
            content(role = "user") { text("Hello, I have 2 dogs in my house.") },
            content(role = "model") { text("Great to meet you. What would you like to know?") }
        )
    )

    private val _uiState: MutableStateFlow<ChatUiState> = MutableStateFlow(
        ChatUiState(
            isLoading = false,
            messages = initMessage(),
        )
    )
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private fun initMessage(): List<ChatMessage> = chat.history.map { content ->
        // Map the initial messages
        ChatMessage(
            text = content.parts.first().asTextOrNull() ?: "",
            participant = if (content.role == "user") Participant.USER else Participant.MODEL,
        )
    }


    fun sendMessage(userMessage: String) {
        // Loading state, update user message
        _uiState.update { currentState ->
            currentState.copy(
                isLoading = true,
                messages = currentState.messages + ChatMessage(
                    text = userMessage,
                    participant = Participant.USER,
                )
            )
        }

        viewModelScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    chat.sendMessage(userMessage)
                }
                val modelResponse = response.text ?: return@launch
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        messages = it.messages + ChatMessage(
                            text = modelResponse,
                            participant = Participant.MODEL,
                        )
                    )
                }
            } catch (cancel: CancellationException) {
                throw cancel
            } catch (throwable: Throwable) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        messages = it.messages + ChatMessage(
                            text = throwable.localizedMessage ?: "Error",
                            participant = Participant.ERROR,
                        )
                    )
                }
            }
        }
    }
}
