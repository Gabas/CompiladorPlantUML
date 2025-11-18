public enum TipoToken {
    // Palavras-chave
    T_START_UML,
    T_END_UML,
    T_CLASS,
    T_ABSTRACT,
    T_STATIC,

    // Símbolos
    T_OPEN_BRACE,       // {
    T_CLOSE_BRACE,      // }
    T_OPEN_PAREN,       // (
    T_CLOSE_PAREN,      // )
    T_SEMICOLON,        // ;
    T_COLON,            // :
    T_COMMA,            // ,

    // Visibilidade
    T_PUBLIC,           // +
    T_PRIVATE,          // -
    T_PROTECTED,        // #
    T_PACKAGE,          // ~

    // Relacionamentos
    T_HERANCA,          // <|--
    T_IMPLEMENTACAO,    // <|..
    T_ASSOCIACAO,       // -->
    T_AGREGACAO,        // o--
    T_COMPOSICAO,       // *--
    T_LINK,             // --

    // Identificadores e Literais
    T_ID,               // Identificador
    // Lembre-se de corrigir o erro de digitação aqui:
    T_STRING_LITERAL,    // "texto" (estava T_SRING_LITERAL)

    // Controle
    T_NEWLINE,          // Quebra de linha
    T_EOF               // Fim do arquivo
}
// Sem mais nada depois deste }