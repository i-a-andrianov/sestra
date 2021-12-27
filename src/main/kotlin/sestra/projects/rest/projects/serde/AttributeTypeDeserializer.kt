package sestra.projects.rest.projects.serde

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import sestra.projects.api.layers.AttributeType
import sestra.projects.api.layers.BooleanAttributeType
import sestra.projects.api.layers.EnumAttributeType
import sestra.projects.api.layers.FloatAttributeType
import sestra.projects.api.layers.IntAttributeType
import sestra.projects.api.layers.StringAttributeType

class AttributeTypeDeserializer : StdDeserializer<AttributeType>(AttributeType::class.java) {
    @Suppress("UNCHECKED_CAST")
    override fun deserialize(jp: JsonParser, ctx: DeserializationContext): AttributeType {
        val data = jp.readValueAs(Map::class.java)

        val name = data["name"] as String
        return when (name) {
            "boolean" -> BooleanAttributeType
            "int" -> IntAttributeType
            "float" -> FloatAttributeType
            "string" -> StringAttributeType
            "enum" -> EnumAttributeType(
                values = data["values"] as List<String>
            )
            else -> throw IllegalStateException("Unknown attribute type '$name'")
        }
    }
}
