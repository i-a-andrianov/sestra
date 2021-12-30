package sestra.projects.rest.projects.serde

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import sestra.projects.api.layers.AttributeType

class AttributeTypeSerializer : StdSerializer<AttributeType>(AttributeType::class.java) {
    override fun serialize(type: AttributeType, jg: JsonGenerator, prov: SerializerProvider) {
        val name = when (type) {
            is AttributeType.Boolean -> "boolean"
            is AttributeType.Int -> "int"
            is AttributeType.Float -> "float"
            is AttributeType.String -> "string"
            is AttributeType.Enum -> "enum"
        }

        val data = mutableMapOf<String, Any>()
        data["name"] = name
        if (type is AttributeType.Enum) {
            data["values"] = type.values
        }

        jg.writeObject(data)
    }
}
