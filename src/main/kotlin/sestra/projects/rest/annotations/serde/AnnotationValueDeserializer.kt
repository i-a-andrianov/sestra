package sestra.projects.rest.annotations.serde

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import sestra.projects.api.annotations.AnnotationValue

class AnnotationValueDeserializer : StdDeserializer<AnnotationValue>(AnnotationValue::class.java) {
    private val mapper = ObjectMapper().findAndRegisterModules()

    @Suppress("UNCHECKED_CAST")
    override fun deserialize(jp: JsonParser, ctx: DeserializationContext): AnnotationValue {
        val json = jp.readValueAs(Map::class.java) as Map<String, Any>

        val type = json["type"] as String
        val value = json - "type"

        val classObj = when (type) {
            "span" -> AnnotationValue.Span::class
            "relation" -> AnnotationValue.Relation::class
            else -> throw IllegalStateException("Unknown annotation type '$type'")
        }

        return mapper.convertValue(value, classObj.java)
    }
}
