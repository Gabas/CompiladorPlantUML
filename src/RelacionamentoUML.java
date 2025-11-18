public class RelacionamentoUML {
    public final String classeOrigem;
    public final String classeDestino;
    public final TipoToken tipoRelacionamento; // Ex: T_ASSOCIACAO, T_HERANCA
    public final String label; // Ex: "matriculado em"

    public RelacionamentoUML(String origem, String destino, TipoToken tipo, String label) {
        this.classeOrigem = origem;
        this.classeDestino = destino;
        this.tipoRelacionamento = tipo;
        this.label = label;
    }

    @Override
    public String toString() {
        return String.format("Relacionamento [De: %s, Para: %s, Tipo: %s, Label: '%s']",
                classeOrigem, classeDestino, tipoRelacionamento.name(), label);
    }
}