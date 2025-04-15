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

package com.google.ai.client.generativeai.common.util

import android.util.Log
import com.google.ai.client.generativeai.common.SerializationException
import kotlin.reflect.KClass
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Serializer for enums that defaults to the first ordinal on unknown types.
 *
 * Convention is that the first enum be named `UNKNOWN`, but any name is valid.
 *
 * When an unknown enum value is found, the enum itself will be logged to stderr with a message
 * about opening an issue on GitHub regarding the new enum value.
 */
@Deprecated(
  level = DeprecationLevel.WARNING,
  message = "This class is deprecated. Use enumSerializer() with nullability EnumClass? type instead. Not throw exception with serialization",
  replaceWith = ReplaceWith(
    expression = "enumSerializer()",
    imports = ["com.google.ai.client.generativeai.common.util.enumSerializer"]
  ),
)
class FirstOrdinalSerializer<T : Enum<T>>(private val enumClass: KClass<T>) : KSerializer<T> {
  override val descriptor: SerialDescriptor = buildClassSerialDescriptor("FirstOrdinalSerializer")

  override fun deserialize(decoder: Decoder): T {
    val name = decoder.decodeString()
    val values = enumClass.enumValues()

    return values.firstOrNull { it.serialName == name }
      ?: values.first().also { printWarning(name) }
  }

  private fun printWarning(name: String) {
    Log.e(
      "FirstOrdinalSerializer",
      """
        |Unknown enum value found: $name"
        |This usually means the backend was updated, and the SDK needs to be updated to match it.
        |Check if there's a new version for the SDK, otherwise please open an issue on our
        |GitHub to bring it to our attention:
        |https://github.com/google/google-ai-android
       """
        .trimMargin(),
    )
  }

  override fun serialize(encoder: Encoder, value: T) {
    encoder.encodeString(value.serialName)
  }
}

/**
 * Provides the name to be used in serialization for this enum value.
 *
 * By default an enum is serialized to its [name][Enum.name], and can be overwritten by providing a
 * [SerialName] annotation.
 */
val <T : Enum<T>> T.serialName: String
  get() = declaringJavaClass.getField(name).getAnnotation<SerialName>()?.value ?: name

/**
 * Variant of [kotlin.enumValues] that provides support for [KClass] instances of enums.
 *
 * @throws SerializationException if the class is not a valid enum. Beyond runtime emily magic, this
 *   shouldn't really be possible.
 */
fun <T : Enum<T>> KClass<T>.enumValues(): Array<T> =
  java.enumConstants ?: throw SerializationException("$simpleName is not a valid enum type.")

/**
 * A generic serializer for enum classes using Kotlin Serialization with caches.
 *
 * This serializer handles the serialization and deserialization of enum values as strings,
 * using either the `serialName` (if available) or the regular `name` of the enum.
 *
 * @param T The enum type to serialize.
 */

inline fun <reified T : Enum<T>> enumSerializer() = object : KSerializer<T?> {
  override val descriptor: SerialDescriptor =
    PrimitiveSerialDescriptor("EnumSerializer", PrimitiveKind.STRING)

  override fun serialize(encoder: Encoder, value: T?) {
    (value?.serialName ?: value?.name)?.let { encoder.encodeString(it) }
  }

  override fun deserialize(decoder: Decoder): T? {
    val decodeString = decoder.decodeString()
    return decodeString.enumBySerialName<T>() as T?
      ?: decodeString.enumByName<T>() as T?
      ?: Log.e(
        "serializer",
        """
        |Unknown enum value found: "$decodeString" in ${T::class.simpleName}
        |This usually means the backend was updated, and the SDK needs to be updated to match it.
        |Check if there's a new version for the SDK, otherwise please open an issue on our
        |GitHub to bring it to our attention:
        |https://github.com/google/google-ai-android
        """.trimMargin(),
      ).run { null }    //todo T::class.java.enumConstants?.firstOrNull()
  }
}

/**
 * A utility object that provides caching for enum name and serialized name lookups.
 *
 * This object maintains three caches:*
 * - `serialNameByEnum`: Maps enum instances to their serialized names (as defined by the `@SerialName` annotation).
 * - `enumByEnumName`: Maps enum names to their corresponding enum instances.
 * - `enumBySerialName`: Maps serialized names to their corresponding enum instances.
 *
 * The caches are populated lazily, meaning that the mappings are generated only when a particular enum class is accessed for the first time.
 */

object Caches {
  private val serialNameByEnum: MutableMap<Class<*>, Map<Enum<*>, String>> = mutableMapOf()
  private val enumByEnumName: MutableMap<Class<*>, Map<String, Enum<*>>> = mutableMapOf()
  private val enumBySerialName: MutableMap<Class<*>, Map<String, Enum<*>>> = mutableMapOf()

  /**
   * Populates the cachesfor the given enum class.
   *
   * @param declaringClass The enum class to generate caches for.
   */
  private fun <T : Enum<T>> makeCache(declaringClass: Class<T>) {
    val mapNames = declaringClass.enumConstants!!
    val pairs: List<Pair<T, String>> = mapNames
      .mapNotNull { constant ->
        val serialName = constant
          .declaringJavaClass
          .getField(constant.name)
          .getAnnotation(SerialName::class.java)?.value
        serialName?.let { constant to it }
      }
    serialNameByEnum[declaringClass] = pairs.toMap()
    enumByEnumName[declaringClass] = mapNames.associateBy { it.name }
    enumBySerialName[declaringClass] = pairs.associate { it.second to it.first }
  }

  /**
   * Returns the serialized name of the given enum instance.
   *
   * @param enum The enum instance to get the serialized name for.
   * @return The serialized name of the enum, or `null` if not found.
   */

  fun <T : Enum<T>> serialNameByEnum(enum: Enum<T>): String? {
    val declaringClass: Class<T> = enum.declaringJavaClass
    serialNameByEnum[declaringClass] ?: makeCache(declaringClass)
    return serialNameByEnum[declaringClass]!![enum]
  }

  /**
   * Returns the enum instance corresponding to the given enum name.
   *
   * @param declaringClass The enum class to search within.
   * @param serialName The simple name of the enum to find.
   * @return The enum instance, or `null` if not found.
   */

  fun <T : Enum<T>> enumByName(declaringClass: Class<T>, serialName: String): Enum<*>? {
    enumByEnumName[declaringClass] ?: makeCache(declaringClass)
    return enumByEnumName[declaringClass]!![serialName]
  }

  /**
   * Returns the enum instance corresponding to the given serialized name.
   *
   * @param declaringClass The enum class to search within.
   * @param serialName The serialized name of the enum to find.
   * @return The enum instance, or `null` if not found.
   */

  fun <T : Enum<T>> enumBySerialName(declaringClass: Class<T>, serialName: String): Enum<*>? {
    enumBySerialName[declaringClass] ?: makeCache(declaringClass)
    return enumBySerialName[declaringClass]!![serialName]
  }
}

/**
 * Returns the serialized name of the enum instance, as defined by the `@SerialName` annotation.
 *
 * @returnThe serialized name of the enum, or `null` if no `@SerialName` annotation is present.
 */

val <T : Enum<T>> Enum<T>.serialName: String?
  get() = Caches.serialNameByEnum(this)

/**
 * Attempts to findan enum instance of the reified type [T] by its simple name.
 *
 * @return The enum instance corresponding to the given name, or `null` if not found.
 */

inline fun <reified T : Enum<T>> String.enumByName(): Enum<*>? =
  Caches.enumByName(T::class.java, this)

/**
 * Attempts to find an enum instance of the reified type [T] by its serialized name.
 *
 * @return The enum instance corresponding to the given serialized name, or `null` if not found.
 */

inline fun <reified T : Enum<T>> String.enumBySerialName(): Enum<*>? =
  Caches.enumBySerialName(T::class.java, this)