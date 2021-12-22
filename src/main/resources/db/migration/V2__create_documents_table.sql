CREATE TABLE documents(
    id serial PRIMARY KEY,
    project_name varchar(256) NOT NULL,
    name varchar(256) NOT NULL,
    text text NOT NULL,
    created_by varchar(256) NOT NULL,
    UNIQUE (project_name, name)
);
