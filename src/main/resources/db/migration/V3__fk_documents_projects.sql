ALTER TABLE documents
    ADD COLUMN project_id int REFERENCES projects;

UPDATE documents document
    SET project_id = project.id
    FROM projects project
    WHERE document.project_name = project.name;

ALTER TABLE documents
    ALTER COLUMN project_id SET NOT NULL;

ALTER TABLE documents
    DROP COLUMN project_name;
