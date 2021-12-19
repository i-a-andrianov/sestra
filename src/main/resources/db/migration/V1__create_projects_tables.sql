CREATE TABLE projects(
    id serial PRIMARY KEY,
    name varchar(256) NOT NULL,
    created_by varchar(256) NOT NULL,
    UNIQUE (name)
);

CREATE TABLE layers(
    id serial PRIMARY KEY,
    project_id int NOT NULL REFERENCES projects,
    in_project_index int NOT NULL,
    name varchar(256) NOT NULL,
    type varchar(256) NOT NULL,
    UNIQUE (project_id, name)
);

CREATE TABLE relation_layer_span_roles(
    id serial PRIMARY KEY,
    layer_id int NOT NULL REFERENCES layers,
    in_layer_index int NOT NULL,
    name varchar(256) NOT NULL,
    target_layer_name varchar(256) NOT NULL,
    UNIQUE (layer_id, name)
);

CREATE TABLE attributes(
    id serial PRIMARY KEY,
    layer_id int NOT NULL REFERENCES layers,
    in_layer_index int NOT NULL,
    name varchar(256) NOT NULL,
    type varchar(256) NOT NULL,
    UNIQUE (layer_id, name)
);

CREATE TABLE enum_attribute_values(
    id serial PRIMARY KEY,
    attribute_id int NOT NULL REFERENCES attributes,
    in_attribute_index int NOT NULL,
    name varchar(256) NOT NULL,
    UNIQUE (attribute_id, name)
);
