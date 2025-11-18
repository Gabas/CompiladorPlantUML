import java.util.List;
import java.util.ArrayList;

// (Você pode adicionar classes para Atributo e Metodo depois)
// Por enquanto, vamos focar em classes e relacionamentos.

public class ClasseUML {
    public final String nome;
    // TODO: Adicionar List<Atributo> atributos;
    // TODO: Adicionar List<Metodo> metodos;

    public ClasseUML(String nome) {
        this.nome = nome;
    }

    @Override
    public String toString() {
        return "ClasseUML [Nome: " + nome + "]";
    }
}