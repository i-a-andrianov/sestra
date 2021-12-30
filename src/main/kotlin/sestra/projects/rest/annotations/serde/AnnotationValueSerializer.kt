package sestra.projects.rest.annotations.serde

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import sestra.projects.api.annotations.AnnotationValue

class AnnotationValueSerializer : StdSerializer<AnnotationValue>(AnnotationValue::class.java) {
    private val mapper = ObjectMapper().findAndRegisterModules()

    @Suppress("UNCHECKED_CAST")
    override fun serialize(obj: AnnotationValue, jg: JsonGenerator, sp: SerializerProvider) {
        val data = mutableMapOf<String, Any>()

        data.putAll(mapper.convertValue(obj, Map::class.java) as Map<String, Any>)

        data["type"] = when (obj) {
            is AnnotationValue.Span -> "span"
            is AnnotationValue.Relation -> "relation"
        }

        jg.writeObject(data)
    }
}
