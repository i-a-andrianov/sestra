package sestra.projects.rest.projects.serde

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import sestra.projects.api.layers.LayerType
import sestra.projects.api.layers.RelationLayerSpanRole
import sestra.projects.api.layers.RelationLayerType
import sestra.projects.api.layers.SpanLayerType

class LayerTypeDeserializer : StdDeserializer<LayerType>(LayerType::class.java) {
    @Suppress("UNCHECKED_CAST")
    override fun deserialize(jp: JsonParser, ctx: DeserializationContext): LayerType {
        val data = jp.readValueAs(Map::class.java)

        val name = data["name"] as String
        return when (name) {
            "span" -> SpanLayerType
            "relation" -> RelationLayerType(
                spanRoles = (data["spanRoles"] as List<Map<String, String>>).map { role ->
                    RelationLayerSpanRole(
                        name = role["name"]!!,
                        targetLayerName = role["targetLayerName"]!!
                    )
                }
            )
            else -> throw IllegalStateException("Unknown layer type '$name'")
        }
    }
}
