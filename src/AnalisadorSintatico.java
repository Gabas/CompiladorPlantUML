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

    // Regra: Declaracao -> DeclaracaoClasse | DeclaracaoRelacionamento | Titulo | T_NEWLINE
    private void declaracao() {
        if (match(TipoToken.T_CLASS)) {
            declaracaoClasse();
        } 
        else if (match(TipoToken.T_TITLE)) { // <--- NOVO
            tratarTitulo();
        }
        else if (check(TipoToken.T_ID) && 
                 checkProximo(TipoToken.T_LINK, TipoToken.T_ASSOCIACAO, TipoToken.T_AGREGACAO, 
                                TipoToken.T_COMPOSICAO, TipoToken.T_HERANCA, TipoToken.T_IMPLEMENTACAO)) {
            declaracaoRelacionamento();
        } 
        else if (match(TipoToken.T_NEWLINE)) {
            // Ignora
        } 
        else if (!isAtEnd()){
            System.err.println("Token inesperado ignorado: " + avancar().lexema);
        }
    }

    // Regra: DeclaracaoClasse -> 'class' ID '{' ... '}'
    private void declaracaoClasse() {
        Token nomeClasse = consumir(TipoToken.T_ID, "Esperado nome da classe.");
        ClasseUML classe = new ClasseUML(nomeClasse.lexema);
        
        // Abre chaves
        if (match(TipoToken.T_OPEN_BRACE)) {
            // Nova linha opcional após {
            match(TipoToken.T_NEWLINE);

            // Enquanto não fechar chaves e não acabar o arquivo
            while (!check(TipoToken.T_CLOSE_BRACE) && !isAtEnd()) {
                declaracaoMembro(classe);
            }
            
            consumir(TipoToken.T_CLOSE_BRACE, "Esperado '}' para fechar a classe.");
        }
        
        // Adiciona a classe completa (com membros) à lista
        classes.add(classe);
        
        // Consome a nova linha final
        match(TipoToken.T_NEWLINE);
    }

    private void declaracaoMembro(ClasseUML classe) {
        // 1. Visibilidade (opcional)
        String visibilidade = "public"; // padrão
        if (match(TipoToken.T_PUBLIC)) visibilidade = "+";
        else if (match(TipoToken.T_PRIVATE)) visibilidade = "-";
        else if (match(TipoToken.T_PROTECTED)) visibilidade = "#";
        else if (match(TipoToken.T_PACKAGE)) visibilidade = "~";

        // 2. Nome do membro
        Token nome = consumir(TipoToken.T_ID, "Esperado nome do atributo ou método.");

        // 3. Decisão: É Método '(' ou Atributo ':' ?
        
        // CASO MÉTODO: Se tiver parenteses
        if (match(TipoToken.T_OPEN_PAREN)) {
            // (Ignorando parâmetros por enquanto para simplificar)
            while (!check(TipoToken.T_CLOSE_PAREN) && !isAtEnd()) {
                avancar(); 
            }
            consumir(TipoToken.T_CLOSE_PAREN, "Esperado ')' após parâmetros.");

            // Tipo de retorno opcional (ex: : void)
            String tipoRetorno = "void";
            if (match(TipoToken.T_COLON)) {
                 Token tipo = consumir(TipoToken.T_ID, "Esperado tipo de retorno.");
                 tipoRetorno = tipo.lexema;
            }

            classe.metodos.add(new MetodoUML(visibilidade, nome.lexema, tipoRetorno));
        }
        // CASO ATRIBUTO: Se tiver dois pontos ou terminar a linha
        else {
            String tipo = "String"; // Tipo padrão se não especificado
            if (match(TipoToken.T_COLON)) {
                Token tokenTipo = consumir(TipoToken.T_ID, "Esperado tipo do atributo.");
                tipo = tokenTipo.lexema;
            }
            
            classe.atributos.add(new AtributoUML(visibilidade, nome.lexema, tipo));
        }

        // Consome a quebra de linha obrigatória após cada membro
        match(TipoToken.T_NEWLINE);
    }

    // Regra: DeclaracaoRelacionamento -> ID Operador ID (':' Label)?
    private void declaracaoRelacionamento() {
        Token classeOrigem = consumir(TipoToken.T_ID, "Esperado ID da classe de origem.");
        Token operador = avancar();
        Token classeDestino = consumir(TipoToken.T_ID, "Esperado ID da classe de destino.");
        
        String label = "";
        
        // Verifica se tem dois pontos ':'
        if (match(TipoToken.T_COLON)) {
            // Lógica NOVA: Lê tudo até o final da linha como label
            StringBuilder sb = new StringBuilder();
            while (!check(TipoToken.T_NEWLINE) && !isAtEnd()) {
                sb.append(avancar().lexema).append(" ");
            }
            label = sb.toString().trim();
        }
        
        relacionamentos.add(new RelacionamentoUML(
            classeOrigem.lexema, 
            classeDestino.lexema, 
            operador.tipo, 
            label
        ));

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

    private void tratarTitulo() {
        // O título é tudo o que vem depois de 'title' até o fim da linha
        StringBuilder sb = new StringBuilder();
        while (!check(TipoToken.T_NEWLINE) && !isAtEnd()) {
            sb.append(avancar().lexema).append(" ");
        }
        System.out.println("TÍTULO DO DIAGRAMA: " + sb.toString().trim());
        match(TipoToken.T_NEWLINE);
    }
}