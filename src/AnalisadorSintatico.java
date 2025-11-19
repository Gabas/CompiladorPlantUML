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
        try {
            // Regra: Programa -> @startuml ListaDeclaracoes @enduml
            consumir(TipoToken.T_START_UML, "Esperado '@startuml' no início.");
            
            listaDeclaracoes(); // Chama a regra principal

            consumir(TipoToken.T_END_UML, "Esperado '@enduml' no fim.");

        } catch (RuntimeException e) {
            System.err.println("Erro Sintático: " + e.getMessage());
        }
    }

    // Regra: ListaDeclaracoes -> (Declaracao)*
    private void listaDeclaracoes() {
        // Continua enquanto não for @enduml ou Fim do Arquivo
        while (!check(TipoToken.T_END_UML) && !isAtEnd()) {
            declaracao(); // Analisa a próxima declaração
        }
    }

    // Regra: Declaracao -> DeclaracaoClasse | DeclaracaoRelacionamento | T_NEWLINE
    private void declaracao() {
        // Se for 'class', é uma declaração de classe
        if (match(TipoToken.T_CLASS)) {
            declaracaoClasse();
        } 
        // Se for um ID, pode ser um relacionamento (ex: Aluno --> Turma)
        else if (check(TipoToken.T_ID) && 
                 checkProximo(TipoToken.T_LINK, TipoToken.T_ASSOCIACAO, TipoToken.T_AGREGACAO, 
                                TipoToken.T_COMPOSICAO, TipoToken.T_HERANCA, TipoToken.T_IMPLEMENTACAO)) {
            declaracaoRelacionamento();
        } 
        // Se for só uma linha em branco, consome e ignora
        else if (match(TipoToken.T_NEWLINE)) {
            // Ignora a linha em branco
        } 
        // Se for qualquer outra coisa, é um erro
        else if (!isAtEnd()){
            // Se não for o fim, mas não sabemos o que é, avance para evitar loop infinito
            System.err.println("Token inesperado ignorado: " + avancar().lexema);
        }
    }

    // Regra: DeclaracaoClasse -> 'class' ID '{' ... '}'
    private void declaracaoClasse() {
        Token nomeClasse = consumir(TipoToken.T_ID, "Esperado nome da classe.");
        
        // Adiciona a classe à nossa AST
        classes.add(new ClasseUML(nomeClasse.lexema));

        // TODO: Implementar parsing de membros (atributos/métodos)
        // Por enquanto, vamos apenas consumir até fechar as chaves
        if (match(TipoToken.T_OPEN_BRACE)) {
            while (!check(TipoToken.T_CLOSE_BRACE) && !isAtEnd()) {
                avancar(); // Avança consumindo o corpo da classe
            }
            consumir(TipoToken.T_CLOSE_BRACE, "Esperado '}' para fechar a classe.");
        }
        
        // Consome a nova linha opcional
        match(TipoToken.T_NEWLINE);
    }

    // Regra: DeclaracaoRelacionamento -> ID Operador ID (':' Label)?
    private void declaracaoRelacionamento() {
        Token classeOrigem = consumir(TipoToken.T_ID, "Esperado ID da classe de origem.");
        
        Token operador = avancar(); // Consome o operador (ex: -->, --, <|--)
        
        Token classeDestino = consumir(TipoToken.T_ID, "Esperado ID da classe de destino.");
        
        String label = null;
        // Verifica se tem um label (ex: : "matriculado")
        if (match(TipoToken.T_COLON)) {
            label = consumir(TipoToken.T_STRING_LITERAL, "Esperado um label em string.").lexema;
        }
        
        // Adiciona o relacionamento à nossa AST
        relacionamentos.add(new RelacionamentoUML(
            classeOrigem.lexema, 
            classeDestino.lexema, 
            operador.tipo, 
            label
        ));

        // Consome a nova linha opcional
        match(TipoToken.T_NEWLINE);
    }


    // --- Métodos Auxiliares do Parser ---

    /**
     * Verifica se o token atual é de algum dos tipos esperados.
     * Se for, consome o token e retorna true.
     */
    private boolean match(TipoToken... tipos) {
        for (TipoToken tipo : tipos) {
            if (check(tipo)) {
                avancar(); // Consome o token
                return true;
            }
        }
        return false;
    }

    /**
     * Consome o token atual se for do tipo esperado.
     * Se não for, lança um erro.
     */
    private Token consumir(TipoToken tipo, String mensagemErro) {
        if (check(tipo)) {
            return avancar();
        }
        throw new RuntimeException(mensagemErro + " (encontrado: " + tokenAtual().tipo + " na linha " + tokenAtual().linha + ")");
    }

    /**
     * Verifica se o token atual é do tipo esperado (sem consumir).
     */
    private boolean check(TipoToken tipo) {
        if (isAtEnd()) return false;
        return tokenAtual().tipo == tipo;
    }

    /**
     * Verifica o PRÓXIMO token (sem consumir)
     */
    private boolean checkProximo(TipoToken... tipos) {
        if (isAtEnd()) return false;
        if (tokens.get(atual + 1).tipo == TipoToken.T_EOF) return false;
        
        for (TipoToken tipo : tipos) {
            if (tokens.get(atual + 1).tipo == tipo) {
                return true;
            }
        }
        return false;
    }

    /**
     * Avança para o próximo token e o retorna.
     */
    private Token avancar() {
        if (!isAtEnd()) {
            atual++;
        }
        return tokenAnterior();
    }

    /**
     * Retorna o token anterior (o que acabamos de consumir).
     */
    private Token tokenAnterior() {
        return tokens.get(atual - 1);
    }
    
    /**
     * Retorna o token atual (sem consumir).
     */
    private Token tokenAtual() {
        return tokens.get(atual);
    }

    /**
     * Verifica se chegamos ao fim da lista de tokens.
     */
    private boolean isAtEnd() {
        return tokenAtual().tipo == TipoToken.T_EOF;
    }
}