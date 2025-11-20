public class MetodoUML {
    public final String visibilidade;
    public final String nome;
    public final String tipoRetorno;

    public MetodoUML(String visibilidade, String nome, String tipoRetorno) {
        this.visibilidade = visibilidade;
        this.nome = nome;
        this.tipoRetorno = tipoRetorno;
    }

    @Override
    public String toString() {
        return "   Metodo: " + visibilidade + " " + nome + "() : " + tipoRetorno;
    }
}