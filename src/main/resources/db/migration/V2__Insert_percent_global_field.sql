-- Migration to insert the first global field 'Percent'
INSERT INTO global_field (field_key, label, sort_order, deleted_at)
VALUES ('percent', 'Percent', 1, NULL);