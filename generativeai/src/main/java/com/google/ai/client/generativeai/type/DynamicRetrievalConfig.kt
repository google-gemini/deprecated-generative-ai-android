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

import androidx.annotation.FloatRange

/*
* Specifies the dynamic retrieval configuration for the given source.
*/
data class DynamicRetrievalConfig(
    /*
    * The mode of the predictor to be used in dynamic retrieval.
     */
    val mode: DynamicRetrievalMode,
    /*
    * (Optional) The threshold to be used in dynamic retrieval. If not set, a system default value is used.
     */
    @FloatRange(0.0, 1.0)
    val dynamicThreshold: Float? = null
)
