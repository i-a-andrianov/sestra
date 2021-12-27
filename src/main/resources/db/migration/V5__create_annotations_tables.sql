CREATE TABLE annotations(
    id serial PRIMARY KEY,
    uuid uuid NOT NULL,
    document_id int NOT NULL REFERENCES documents,
    layer_id int NOT NULL REFERENCES layers,
    type varchar(256) NOT NULL,
    span_start int,
    span_end int,
    created_by varchar(256) NOT NULL,
    UNIQUE (uuid)
);

CREATE INDEX ON annotations (document_id, layer_id, created_by);

CREATE TABLE relation_annotation_span_roles(
    id serial PRIMARY KEY,
    annotation_id int NOT NULL REFERENCES annotations,
    in_annotation_index int NOT NULL,
    name varchar(256) NOT NULL,
    target_annotation_id int NOT NULL REFERENCES annotations,
    UNIQUE (annotation_id, name)
);

CREATE TABLE annotation_attributes(
    id serial PRIMARY KEY,
    annotation_id int NOT NULL REFERENCES annotations,
    in_annotation_index int NOT NULL,
    name varchar(256) NOT NULL,
    type varchar(256) NOT NULL,
    boolean_value boolean,
    int_value int,
    float_value float,
    string_value varchar(256),
    enum_value varchar(256),
    UNIQUE (annotation_id, name)
);
