package sestra.projects.rest.serde

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import sestra.projects.api.core.AttributeType
import sestra.projects.api.core.BooleanAttributeType
import sestra.projects.api.core.EnumAttributeType
import sestra.projects.api.core.FloatAttributeType
import sestra.projects.api.core.IntAttributeType
import sestra.projects.api.core.StringAttributeType

class AttributeTypeSerializer : StdSerializer<AttributeType>(AttributeType::class.java) {
    override fun serialize(type: AttributeType, jg: JsonGenerator, prov: SerializerProvider) {
        val name = when (type) {
            is BooleanAttributeType -> "boolean"
            is IntAttributeType -> "int"
            is FloatAttributeType -> "float"
            is StringAttributeType -> "string"
            is EnumAttributeType -> "enum"
        }

        val data = mutableMapOf<String, Any>()
        data["name"] = name
        if (type is EnumAttributeType) {
            data["values"] = type.values
        }

        jg.writeObject(data)
    }
}
