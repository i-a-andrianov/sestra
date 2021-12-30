package sestra.projects.rest.projects.serde

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import sestra.projects.api.layers.LayerType

class LayerTypeSerializer : StdSerializer<LayerType>(LayerType::class.java) {
    override fun serialize(type: LayerType, jg: JsonGenerator, prov: SerializerProvider) {
        val name = when (type) {
            is LayerType.Span -> "span"
            is LayerType.Relation -> "relation"
        }

        val data = mutableMapOf<String, Any>()
        data["name"] = name
        if (type is LayerType.Relation) {
            data["spanRoles"] = type.spanRoles
        }

        jg.writeObject(data)
    }
}
