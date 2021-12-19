package sestra.projects.rest.serde

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import sestra.projects.api.core.LayerType
import sestra.projects.api.core.RelationLayerType
import sestra.projects.api.core.SpanLayerType

class LayerTypeSerializer : StdSerializer<LayerType>(LayerType::class.java) {
    override fun serialize(type: LayerType, jg: JsonGenerator, prov: SerializerProvider) {
        val name = when (type) {
            is SpanLayerType -> "span"
            is RelationLayerType -> "relation"
        }

        val data = mutableMapOf<String, Any>()
        data["name"] = name
        if (type is RelationLayerType) {
            data["spanRoles"] = type.spanRoles
        }

        jg.writeObject(data)
    }
}
