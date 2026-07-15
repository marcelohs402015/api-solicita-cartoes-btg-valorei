CREATE TABLE historico (
    id UUID PRIMARY KEY,
    proposta_id UUID NOT NULL REFERENCES proposta(id),
    evento VARCHAR(80) NOT NULL,
    status VARCHAR(20) NOT NULL,
    payload JSON NOT NULL,
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_historico_proposta_id ON historico(proposta_id);
CREATE INDEX idx_historico_criado_em ON historico(criado_em DESC);
