package sestra.projects.rest.projects.serde

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import sestra.projects.api.layers.AttributeType

class AttributeTypeDeserializer : StdDeserializer<AttributeType>(AttributeType::class.java) {
    @Suppress("UNCHECKED_CAST")
    override fun deserialize(jp: JsonParser, ctx: DeserializationContext): AttributeType {
        val data = jp.readValueAs(Map::class.java)

        val name = data["name"] as String
        return when (name) {
            "boolean" -> AttributeType.Boolean
            "int" -> AttributeType.Int
            "float" -> AttributeType.Float
            "string" -> AttributeType.String
            "enum" -> AttributeType.Enum(
                values = data["values"] as List<String>
            )
            else -> throw IllegalStateException("Unknown attribute type '$name'")
        }
    }
}
