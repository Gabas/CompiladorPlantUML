import java.util.List;
import java.util.ArrayList;

public class AnalisadorSintatico {

    private final List<Token> tokens;
    private int atual = 0; // Ponteiro para o token atual

    // A "AST" - o resultado final do parsing
    public final List<ClasseUML> classes = new ArrayList<>();
    public final List<RelacionamentoUML> relacionamentos = new ArrayList<>();

    public AnalisadorSintatico(List<Token> tokens) {
        this.tokens = tokens;
    }

    // O método principal que vai iniciar a análise
    public void parse() {
        // Implementaremos a lógica baseada na gramática aqui
        System.out.println("Iniciando análise sintática...");
        // TODO: Implementar as regras da gramática
    }

    // --- Métodos auxiliares que usaremos ---

    private Token tokenAtual() {
        return tokens.get(atual);
    }

    private Token tokenAnterior() {
        return tokens.get(atual - 1);
    }

    private Token avancar() {
        if (!isAtEnd()) {
            atual++;
        }
        return tokenAnterior();
    }

    private boolean isAtEnd() {
        return tokenAtual().tipo == TipoToken.T_EOF;
    }
}