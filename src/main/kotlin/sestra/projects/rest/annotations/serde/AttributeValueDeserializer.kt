package sestra.projects.rest.annotations.serde

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import sestra.projects.api.annotations.AttributeValue

class AttributeValueDeserializer : StdDeserializer<AttributeValue>(AttributeValue::class.java) {
    private val mapper = ObjectMapper().findAndRegisterModules()

    @Suppress("UNCHECKED_CAST")
    override fun deserialize(jp: JsonParser, ctx: DeserializationContext): AttributeValue {
        val json = jp.readValueAs(Map::class.java) as Map<String, Any>

        val type = json["type"] as String
        val value = json - "type"

        val classObj = when (type) {
            "boolean" -> AttributeValue.Boolean::class
            "int" -> AttributeValue.Int::class
            "float" -> AttributeValue.Float::class
            "string" -> AttributeValue.String::class
            "enum" -> AttributeValue.Enum::class
            else -> throw IllegalStateException("Unknown attribute type '$type'")
        }

        return mapper.convertValue(value, classObj.java)
    }
}
