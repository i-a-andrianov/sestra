package sestra.projects.rest.serde

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import sestra.projects.api.core.AttributeType
import sestra.projects.api.core.BooleanAttributeType
import sestra.projects.api.core.EnumAttributeType
import sestra.projects.api.core.FloatAttributeType
import sestra.projects.api.core.IntAttributeType
import sestra.projects.api.core.StringAttributeType

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
