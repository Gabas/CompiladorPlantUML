import java.util.List;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class App {
    public static void main(String[] args) throws Exception {
        
        // 1. Definição do arquivo de entrada
        String nomeArquivo = "teste.txt";
        String codigoFonte;

        try {
            // Lê o conteúdo do arquivo para uma String
            codigoFonte = Files.readString(Paths.get(nomeArquivo));
        } catch (IOException e) {
            System.err.println("Erro ao ler o arquivo '" + nomeArquivo + "': " + e.getMessage());
            return;
        }

        System.out.println("=============================================");
        System.out.println("   COMPILADOR PLANTUML -> SVG (Iniciado)   ");
        System.out.println("=============================================\n");

        // 2. ANÁLISE LÉXICA
        System.out.println("--- 1. Análise Léxica ---");
        AnalisadorLexico lexico = new AnalisadorLexico(codigoFonte);
        List<Token> tokens = lexico.scanTokens();
        
        // (Opcional) Imprimir tokens para conferência
        // for (Token t : tokens) System.out.println(t);
        System.out.println("Tokens gerados: " + tokens.size());
        System.out.println("Status: OK\n");

        // 3. ANÁLISE SINTÁTICA
        System.out.println("--- 2. Análise Sintática ---");
        AnalisadorSintatico sintatico = new AnalisadorSintatico(tokens);
        sintatico.parse();
        
        System.out.println("Classes encontradas: " + sintatico.classes.size());
        System.out.println("Relacionamentos encontrados: " + sintatico.relacionamentos.size());
        
        // Exibe as classes lidas (resumo)
        for (ClasseUML c : sintatico.classes) {
            System.out.println(" > Classe: " + c.nome + " (" + c.atributos.size() + " atributos, " + c.metodos.size() + " métodos)");
        }
        System.out.println("Status: OK\n");

        // 4. ANÁLISE SEMÂNTICA (A novidade!)
        System.out.println("--- 3. Análise Semântica ---");
        AnalisadorSemantico semantico = new AnalisadorSemantico(sintatico.classes, sintatico.relacionamentos);
        boolean temErrosSemanticos = semantico.analisar();

        if (temErrosSemanticos) {
            System.err.println("\n[AVISO] Foram encontrados erros semânticos. O diagrama pode conter inconsistências.");
            // Você pode optar por parar aqui com 'return;' se quiser ser rigoroso.
        } else {
            System.out.println("Status: OK (Nenhuma inconsistência encontrada)\n");
        }

        // 5. GERAÇÃO DE CÓDIGO (SVG)
        System.out.println("--- 4. Geração de Código (SVG) ---");
        try {
            // AGORA PASSAMOS O TÍTULO PARA O CONSTRUTOR!
            GeradorSVG gerador = new GeradorSVG(sintatico.classes, sintatico.relacionamentos, sintatico.titulo);
            gerador.gerarArquivo("diagrama.svg");
            System.out.println("Arquivo 'diagrama.svg' gerado com sucesso! Título: " + sintatico.titulo);
        } catch (IOException e) {
            System.err.println("Erro ao gravar o arquivo SVG: " + e.getMessage());
        }

        System.out.println("\n=============================================");
        System.out.println("   PROCESSO CONCLUÍDO   ");
        System.out.println("=============================================");
    }
}