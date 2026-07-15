CREATE TABLE email_disparo (
    id UUID PRIMARY KEY,
    proposta_id UUID NOT NULL REFERENCES proposta(id),
    destinatario VARCHAR(255) NOT NULL,
    assunto VARCHAR(255) NOT NULL,
    template_json JSON NOT NULL,
    status VARCHAR(20) NOT NULL,
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_email_proposta_id ON email_disparo(proposta_id);
CREATE INDEX idx_email_criado_em ON email_disparo(criado_em DESC);
