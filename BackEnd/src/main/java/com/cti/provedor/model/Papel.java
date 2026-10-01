package com.cti.provedor.model;

/** Papel de um usuário na plataforma. */
public enum Papel {
    /** Gerencia usuários e tudo o mais. */
    ADMIN,
    /** Envia planilhas e mantém clientes, contratos, serviços, consultores e insights. */
    ANALISTA,
    /** Só consulta. */
    LEITOR
}
