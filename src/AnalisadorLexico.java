import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnalisadorLexico {

    private final String codigoFonte;
    private final List<Token> tokens = new ArrayList<>();
    
    private static final Map<String, TipoToken> palavrasChave;

    static {
        palavrasChave = new HashMap<>();
        palavrasChave.put("@startuml", TipoToken.T_START_UML);
        palavrasChave.put("@enduml",   TipoToken.T_END_UML);
        palavrasChave.put("class",     TipoToken.T_CLASS);
        palavrasChave.put("abstract",  TipoToken.T_ABSTRACT);
        palavrasChave.put("title",     TipoToken.T_TITLE);
    }

    private int atual = 0;
    private int linha = 1;
    private int coluna = 1;

    public AnalisadorLexico(String codigoFonte) {
        this.codigoFonte = codigoFonte;
    }

    public List<Token> scanTokens() {
        while (!isAtEnd()) {
            scanToken();
        }
        tokens.add(new Token(TipoToken.T_EOF, "", linha, coluna));
        return tokens;
    }

    // --- MÉTODO PRINCIPAL DE SCAN ---
    private void scanToken() {
        char c = avancar();

        switch (c) {
            // --- SÍMBOLOS SIMPLES ---
            case '{': adicionarToken(TipoToken.T_OPEN_BRACE); break;
            case '}': adicionarToken(TipoToken.T_CLOSE_BRACE); break;
            case '(': adicionarToken(TipoToken.T_OPEN_PAREN); break;
            case ')': adicionarToken(TipoToken.T_CLOSE_PAREN); break;
            case ':': adicionarToken(TipoToken.T_COLON); break;
            case ',': adicionarToken(TipoToken.T_COMMA); break;
            case '+': adicionarToken(TipoToken.T_PUBLIC); break;
            case '#': adicionarToken(TipoToken.T_PROTECTED); break;
            case '~': adicionarToken(TipoToken.T_PACKAGE); break;
            
            // comentários de linha: ignora tudo até o \n
            case '\'': 
                while (peek() != '\n' && !isAtEnd()) {
                    avancar();
                }
                break;

            // -> e -->
            case '-':
                if (match('>')) {
                    adicionarToken(TipoToken.T_ASSOCIACAO, "->");
                } else if (match('-')) {
                    if (match('>')) {
                        adicionarToken(TipoToken.T_ASSOCIACAO, "-->");
                    } else {
                        adicionarToken(TipoToken.T_LINK, "--");
                    }
                } else { 
                    adicionarToken(TipoToken.T_PRIVATE);
                }
                break;
            
            // herança e implementação
            case '<':
                if (match('|')) {
                    if (match('.')) {
                        if (match('.')) {
                            adicionarToken(TipoToken.T_IMPLEMENTACAO, "<|..");
                        }
                    } else if (match('-')) {
                        if (match('-')) {
                            adicionarToken(TipoToken.T_HERANCA, "<|--");
                        }
                    }
                } else {
                    // apenas '<' isolado, que não é esperado na gramática
                    // deixa passar só pra não travar o analisador
                }
                break;

            case 'o':
                if (peek() == '-') {
                     avancar(); 
                     if (match('-')) {
                         adicionarToken(TipoToken.T_AGREGACAO, "o--");
                         break; // sai do switch, token já adicionado
                     }
                     // se chegou aqui, era 'o-' mas não 'o--'. 
                }
                // se não for operador, é um identificador que começa com 'o'
                identificador();
                break;

            case '*':
                if (match('-') && match('-')) {
                    adicionarToken(TipoToken.T_COMPOSICAO, "*--");
                } else {
                    // mesmo caso do 'o', se for asterisco solto (multiplicidade) ou ID
                }
                break;
            
            case '>':
                adicionarToken(TipoToken.T_GREATER);
                break;

            // ignorar espaços em branco
            case ' ':
            case '\r':
            case '\t':
                break;
            
            case '\n':
                adicionarToken(TipoToken.T_NEWLINE);
                linha++;
                coluna = 1;
                break;
            
            case '"':
                stringLiteral();
                break;

            default:
                // acento e letras Unicode suportados
                if (isLetra(c) || c == '@') { 
                    identificador();
                } 
                else {
                    System.err.printf("Erro Léxico: Caractere inesperado '%c' na Linha %d Col %d\n", c, linha, coluna);
                }
                break;
        }
    }

    // --- MÉTODOS AUXILIARES ---

    private char avancar() {
        coluna++;
        return codigoFonte.charAt(atual++);
    }

    private char peek() {
        if (isAtEnd()) return '\0';
        return codigoFonte.charAt(atual);
    }

    private boolean match(char esperado) {
        if (isAtEnd()) return false;
        if (codigoFonte.charAt(atual) != esperado) return false;
        
        atual++;
        coluna++;
        return true;
    }
    
    private void identificador() {
        // 'atual - 1' pega o caractere que já consumimos (ex: 'o' de organiza)
        int inicio = atual - 1;
        while (isLetraOuDigito(peek())) {
            avancar();
        }
        
        String texto = codigoFonte.substring(inicio, atual);
        TipoToken tipo = palavrasChave.get(texto);
        
        if (tipo == null) {
            tipo = TipoToken.T_ID;
        }
        adicionarToken(tipo, texto);
    }
    
    private void stringLiteral() {
        int inicio = atual; 
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') {
                linha++; 
                coluna = 1;
            }
            avancar();
        }

        if (isAtEnd()) {
            System.err.printf("Erro Léxico: String não terminada na Linha %d\n", linha);
            return;
        }
        
        String valor = codigoFonte.substring(inicio, atual);
        avancar(); 
        adicionarToken(TipoToken.T_STRING_LITERAL, valor);
    }

    // --- CORREÇÃO: Suporte a Unicode (Acentos) ---
    private boolean isLetra(char c) {
        return Character.isLetter(c) || c == '_';
    }

    private boolean isDigito(char c) {
        return Character.isDigit(c);
    }
    
    private boolean isLetraOuDigito(char c) {
        return isLetra(c) || isDigito(c);
    }

    private boolean isAtEnd() {
        return atual >= codigoFonte.length();
    }

    private void adicionarToken(TipoToken tipo) {
        String lexema = codigoFonte.substring(atual - 1, atual);
        tokens.add(new Token(tipo, lexema, linha, coluna - 1));
    }
    
    private void adicionarToken(TipoToken tipo, String lexema) {
        int col = coluna - lexema.length();
        tokens.add(new Token(tipo, lexema, linha, col));
    }
}