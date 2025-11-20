public class AtributoUML {
    public final String visibilidade; // +, -, #
    public final String nome;
    public final String tipo;

    public AtributoUML(String visibilidade, String nome, String tipo) {
        this.visibilidade = visibilidade;
        this.nome = nome;
        this.tipo = tipo;
    }

    @Override
    public String toString() {
        return "   Atributo: " + visibilidade + " " + nome + " : " + tipo;
    }
}