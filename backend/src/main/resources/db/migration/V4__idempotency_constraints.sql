ALTER TABLE historico ADD COLUMN source_event_id UUID;

CREATE UNIQUE INDEX uk_historico_source_event_id ON historico(source_event_id) WHERE source_event_id IS NOT NULL;

CREATE UNIQUE INDEX uk_email_disparo_proposta_id ON email_disparo(proposta_id);
