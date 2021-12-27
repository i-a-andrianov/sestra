ALTER TABLE relation_layer_span_roles
    ADD COLUMN target_layer_id int REFERENCES layers;

UPDATE relation_layer_span_roles role
    SET target_layer_id = layer.id
    FROM layers layer
    WHERE role.target_layer_name = layer.name;

ALTER TABLE relation_layer_span_roles
    ALTER COLUMN target_layer_id SET NOT NULL;

ALTER TABLE relation_layer_span_roles
    DROP COLUMN target_layer_name;
