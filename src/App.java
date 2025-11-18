import java.util.List;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class App {
    public static void main(String[] args) throws Exception {
        
        String nomeArquivo = "teste.txt";
        String codigoTeste;

        try {
            // Lendo o teste.txt
            codigoTeste = Files.readString(Paths.get(nomeArquivo));

        } catch (IOException e) {
            System.err.println("Erro ao ler o arquivo '" + nomeArquivo + "': " + e.getMessage());
            return; // Encerra o programa se não conseguir ler o arquivo
        }

        System.out.println("--- Testando Analisador Léxico ---");
        System.out.println("Arquivo Lido: " + nomeArquivo + "\n");
        // System.out.println("Código Fonte:\n" + codigoTeste);
        System.out.println("--- Tokens Gerados ---");

        AnalisadorLexico lexico = new AnalisadorLexico(codigoTeste);
        List<Token> tokensGerados = lexico.scanTokens();

        for (Token token : tokensGerados) {
            System.out.println(token);
        }
        
        System.out.println("--- Fim dos Tokens ---");

        // --- INÍCIO DA ANÁLISE SINTÁTICA ---
        System.out.println("\n--- Iniciando Analisador Sintático ---");
        AnalisadorSintatico sintatico = new AnalisadorSintatico(tokensGerados);
        sintatico.parse(); // Chama o método principal

        // Imprime a AST (os objetos) que o parser criou
        System.out.println("\n--- Classes Encontradas ---");
        for (ClasseUML classe : sintatico.classes) {
            System.out.println(classe);
        }

        System.out.println("\n--- Relacionamentos Encontrados ---");
        for (RelacionamentoUML rel : sintatico.relacionamentos) {
            System.out.println(rel);
        }
    }
}