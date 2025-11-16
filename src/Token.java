public class Token {
    
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
        T_SRING_LITERAL,    // "texto"

        // Controle
        T_NEWLINE,          // Quebra de linha
        T_EOF               // Fim do arquivo
    }

}