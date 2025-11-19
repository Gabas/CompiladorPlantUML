import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Analisador Léxico (Scanner) para a linguagem PlantUML (Diagrama de Classe).
 * Lê o código fonte como uma String e o transforma em uma lista de Tokens.
 */
public class AnalisadorLexico {

    private final String codigoFonte;
    private final List<Token> tokens = new ArrayList<>();
    
    // Mapa de palavras-chave da linguagem
    private static final Map<String, TipoToken> palavrasChave;

    static {
        palavrasChave = new HashMap<>();
        // Use o nome da enum para acessar os valores
        palavrasChave.put("@startuml", TipoToken.T_START_UML);
        palavrasChave.put("@enduml",   TipoToken.T_END_UML);
        palavrasChave.put("class",     TipoToken.T_CLASS);
        palavrasChave.put("abstract",  TipoToken.T_ABSTRACT);
    }

    // Ponteiros para controlar a leitura
    private int atual = 0;   // Posição atual no `codigoFonte`
    private int linha = 1;     // Linha atual (para reportar erros)
    private int coluna = 1;  // Coluna atual (para reportar erros)

    public AnalisadorLexico(String codigoFonte) {
        this.codigoFonte = codigoFonte;
    }

    /**
     * Método principal que escaneia todo o código e retorna a lista de tokens.
     */
    public List<Token> scanTokens() {
        // Continua enquanto não chegarmos ao fim do arquivo
        while (!isAtEnd()) {
            // Analisamos um caractere de cada vez
            scanToken();
        }

        // Adiciona um token especial de Fim de Arquivo (EOF)
        tokens.add(new Token(TipoToken.T_EOF, "", linha, coluna));
        return tokens;
    }

    /**
     * Analisa o PRÓXIMO caractere e decide qual token criar.
     */
    private void scanToken() {
        char c = avancar(); // Pega o caractere atual e avança o ponteiro

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

            // --- SÍMBOLOS DE MÚLTIPLOS CARACTERES ---
            case '-':
                if (match('>')) {
                    adicionarToken(TipoToken.T_ASSOCIACAO, "-->"); // Corrigido
                } else if (match('-')) { 
                    adicionarToken(TipoToken.T_LINK, "--"); // Corrigido
                } else { 
                    adicionarToken(TipoToken.T_PRIVATE);
                }
                break;
            
            case '<':
                if (match('|')) {
                    if (match('.')) {
                        if (match('.')) {
                            adicionarToken(TipoToken.T_IMPLEMENTACAO, "<|.."); // Adicionado
                        }
                    } else if (match('-')) {
                        if (match('-')) {
                            adicionarToken(TipoToken.T_HERANCA, "<|--"); // Adicionado
                        }
                    }
                }
                break;

            case 'o':
                if (match('-')) {
                    if (match('-')) {
                        adicionarToken(TipoToken.T_AGREGACAO, "o--"); // Adicionado
                    }
                }
                break;

            case '*':
                if (match('-')) {
                    if (match('-')) {
                        adicionarToken(TipoToken.T_COMPOSICAO, "*--"); // Adicionado
                    }
                }
                break;

            // --- IGNORAR ESPAÇOS EM BRANCO ---
            case ' ':
            case '\r':
            case '\t':
                // Ignora.
                break;
            
            // --- NOVA LINHA ---
            case '\n':
                adicionarToken(TipoToken.T_NEWLINE);
                linha++;
                coluna = 1;
                break;
            
            // --- STRINGS LITERAIS ---
            case '"':
                stringLiteral();
                break;

            default:
                // --- PALAVRAS-CHAVE E IDENTIFICADORES ---
                if (isLetra(c) || c == '@') { 
                    identificador();
                } 
                // --- ERRO ---
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
        int inicio = atual - 1;
        while (isLetraOuDigito(peek())) {
            avancar();
        }
        
        String texto = codigoFonte.substring(inicio, atual);
        TipoToken tipo = palavrasChave.get(texto); // Verifica se é palavra-chave
        
        if (tipo == null) {
            tipo = TipoToken.T_ID; // Se não for, é um ID
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
        avancar(); // Consome o '"' final
        
        // Use T_STRING_LITERAL (o nome corrigido)
        adicionarToken(TipoToken.T_STRING_LITERAL, valor);
    }

    // --- Métodos de Verificação ---
    private boolean isLetra(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }

    private boolean isDigito(char c) {
        return c >= '0' && c <= '9';
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