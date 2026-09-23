package com.revlog.data.backup

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive

@OptIn(ExperimentalSerializationApi::class)
object FlexibleNullableDoubleSerializer : KSerializer<Double?> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("FlexibleNullableDouble", PrimitiveKind.DOUBLE)

    override fun deserialize(decoder: Decoder): Double? {
        val jsonDecoder = decoder as? JsonDecoder
        if (jsonDecoder == null) {
            return if (decoder.decodeNotNullMark()) decoder.decodeDouble() else null
        }
        return when (val element = jsonDecoder.decodeJsonElement()) {
            JsonNull -> null
            is JsonPrimitive -> when {
                element.isString -> element.content
                    .trim()
                    .replace(',', '.')
                    .toDoubleOrNull()
                else -> element.content.toDoubleOrNull()
            }
            else -> null
        }
    }

    override fun serialize(encoder: Encoder, value: Double?) {
        val jsonEncoder = encoder as? JsonEncoder
        if (value == null) {
            if (jsonEncoder != null) {
                jsonEncoder.encodeNull()
            } else {
                encoder.encodeNull()
            }
        } else {
            encoder.encodeDouble(value)
        }
    }
}
