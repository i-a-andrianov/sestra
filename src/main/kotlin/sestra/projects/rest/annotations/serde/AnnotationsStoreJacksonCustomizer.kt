package sestra.projects.rest.annotations.serde

import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.stereotype.Component

@Component
class AnnotationsStoreJacksonCustomizer : Jackson2ObjectMapperBuilderCustomizer {
    override fun customize(builder: Jackson2ObjectMapperBuilder) {
        builder.serializers(
            AnnotationValueSerializer(),
            AttributeValueSerializer()
        )
        builder.deserializers(
            AnnotationValueDeserializer(),
            AttributeValueDeserializer()
        )
    }
}
