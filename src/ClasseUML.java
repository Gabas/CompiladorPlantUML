import java.util.List;
import java.util.ArrayList;

public class ClasseUML {
    public final String nome;
    public final List<AtributoUML> atributos = new ArrayList<>();
    public final List<MetodoUML> metodos = new ArrayList<>();

    public ClasseUML(String nome) {
        this.nome = nome;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Classe: ").append(nome).append("\n");
        
        for (AtributoUML atr : atributos) {
            sb.append(atr.toString()).append("\n");
        }
        for (MetodoUML met : metodos) {
            sb.append(met.toString()).append("\n");
        }
        return sb.toString();
    }
}