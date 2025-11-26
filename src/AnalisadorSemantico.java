import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AnalisadorSemantico {

    private final List<ClasseUML> classes;
    private final List<RelacionamentoUML> relacionamentos;
    private boolean temErros = false;

    public AnalisadorSemantico(List<ClasseUML> classes, List<RelacionamentoUML> relacionamentos) {
        this.classes = classes;
        this.relacionamentos = relacionamentos;
    }

    /**
     * Executa as validações semânticas.
     * @return true se houver erros, false se estiver tudo certo.
     */
    public boolean analisar() {
        System.out.println("Verificando consistência do diagrama...");
        
        // 1. Tabela de Símbolos (apenas nomes das classes)
        Set<String> nomesClasses = new HashSet<>();
        
        // Passo 1: Verificar duplicidade de classes
        for (ClasseUML c : classes) {
            if (nomesClasses.contains(c.nome)) {
                erro("A classe '" + c.nome + "' foi declarada mais de uma vez.");
            } else {
                nomesClasses.add(c.nome);
            }
        }

        // Passo 2: Verificar integridade dos relacionamentos
        for (RelacionamentoUML rel : relacionamentos) {
            // Verifica a origem
            if (!nomesClasses.contains(rel.classeOrigem)) {
                erro("Relacionamento inválido: A classe de origem '" + rel.classeOrigem + "' não foi definida.");
            }

            // Verifica o destino
            if (!nomesClasses.contains(rel.classeDestino)) {
                erro("Relacionamento inválido: A classe de destino '" + rel.classeDestino + "' não foi definida.");
            }
            
            // Verifica auto-relacionamento (opcional, mas bom aviso)
            // if (rel.classeOrigem.equals(rel.classeDestino)) {
            //    aviso("A classe '" + rel.classeOrigem + "' se relaciona com ela mesma.");
            // }
        }

        if (!temErros) {
            System.out.println("Sucesso: Nenhuma inconsistência semântica encontrada.");
        }

        return temErros;
    }

    private void erro(String mensagem) {
        System.err.println("ERRO SEMÂNTICO: " + mensagem);
        temErros = true;
    }
}