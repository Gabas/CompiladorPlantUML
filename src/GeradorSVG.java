import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GeradorSVG {

    private final List<ClasseUML> classes;
    private final List<RelacionamentoUML> relacionamentos;
    private final String titulo; // <--- NOVO CAMPO

    // Construtor atualizado para receber o Título
    public GeradorSVG(List<ClasseUML> classes, List<RelacionamentoUML> relacionamentos, String titulo) {
        this.classes = classes;
        this.relacionamentos = relacionamentos;
        this.titulo = titulo;
    }

    public void gerarArquivo(String caminhoArquivo) throws IOException {
        StringBuilder svg = new StringBuilder();
        
        // Lógica de Centralidade (Mantida igual)
        Map<String, Integer> conexoes = new HashMap<>();
        for (ClasseUML c : classes) conexoes.put(c.nome, 0);
        for (RelacionamentoUML r : relacionamentos) {
            conexoes.put(r.classeOrigem, conexoes.getOrDefault(r.classeOrigem, 0) + 1);
            conexoes.put(r.classeDestino, conexoes.getOrDefault(r.classeDestino, 0) + 1);
        }
        Collections.sort(classes, (c1, c2) -> conexoes.getOrDefault(c2.nome, 0) - conexoes.getOrDefault(c1.nome, 0));
        
        // Canvas
        int larguraBox = 220;
        int larguraCanvas = 1400; 
        int alturaCanvas = 1200;
        int centroX = larguraCanvas / 2;
        int centroY = alturaCanvas / 2;
        int raioX = 550; 
        int raioY = 450; 

        svg.append(String.format("<svg width=\"%d\" height=\"%d\" xmlns=\"http://www.w3.org/2000/svg\">\n", larguraCanvas, alturaCanvas));
        
        // Definições (Mantidas iguais)
        svg.append("<defs>\n");
        svg.append("<filter id=\"sombra\" x=\"0\" y=\"0\" width=\"200%\" height=\"200%\">\n");
        svg.append("<feOffset result=\"offOut\" in=\"SourceAlpha\" dx=\"3\" dy=\"3\" />\n");
        svg.append("<feGaussianBlur result=\"blurOut\" in=\"offOut\" stdDeviation=\"2\" />\n");
        svg.append("<feBlend in=\"SourceGraphic\" in2=\"blurOut\" mode=\"normal\" />\n");
        svg.append("</filter>\n");
        svg.append("<marker id=\"seta_open\" markerWidth=\"12\" markerHeight=\"12\" refX=\"9\" refY=\"3\" orient=\"auto\"><path d=\"M0,0 L0,6 L9,3 z\" fill=\"black\" /></marker>\n");
        svg.append("<marker id=\"seta_heranca\" markerWidth=\"16\" markerHeight=\"16\" refX=\"14\" refY=\"7\" orient=\"auto\"><path d=\"M0,0 L14,7 L0,14 L0,0\" fill=\"white\" stroke=\"black\" /></marker>\n");
        svg.append("<marker id=\"seta_agregacao\" markerWidth=\"18\" markerHeight=\"12\" refX=\"16\" refY=\"5\" orient=\"auto\"><path d=\"M0,5 L8,0 L16,5 L8,10 z\" fill=\"white\" stroke=\"black\" /></marker>\n");
        svg.append("<marker id=\"seta_composicao\" markerWidth=\"18\" markerHeight=\"12\" refX=\"16\" refY=\"5\" orient=\"auto\"><path d=\"M0,5 L8,0 L16,5 L8,10 z\" fill=\"black\" stroke=\"black\" /></marker>\n");
        svg.append("</defs>\n");

        svg.append("<style>\n");
        svg.append(".texto { font-family: Arial, sans-serif; font-size: 12px; fill: #333; }\n");
        svg.append(".titulo { font-family: Arial, sans-serif; font-size: 14px; font-weight: bold; fill: black; }\n");
        // Estilo novo para o título principal
        svg.append(".titulo-diagrama { font-family: Arial, sans-serif; font-size: 24px; font-weight: bold; fill: #2c3e50; }\n");
        svg.append(".box { fill: #fff; stroke: #333; stroke-width: 1; filter: url(#sombra); }\n");
        svg.append(".header-box { fill: #f0f0f0; stroke: #333; stroke-width: 1; }\n");
        svg.append(".linha { stroke: #333; stroke-width: 1.5; }\n");
        svg.append(".label-bg { fill: white; opacity: 0.9; }\n"); 
        svg.append("</style>\n");
        
        // --- DESENHAR TÍTULO DO DIAGRAMA ---
        if (titulo != null && !titulo.isEmpty()) {
            svg.append(String.format("<text x=\"%d\" y=\"40\" class=\"titulo-diagrama\" text-anchor=\"middle\">%s</text>\n", centroX, titulo));
        }

        // 1. Classes
        for (int i = 0; i < classes.size(); i++) {
            ClasseUML classe = classes.get(i);
            int alturaHeader = 30;
            int alturaTotal = alturaHeader + (classe.atributos.size() * 15) + 10 + (classe.metodos.size() * 15) + 10 + 5;
            classe.width = larguraBox;
            classe.height = alturaTotal;
            int x, y;
            if (i == 0) {
                x = centroX - (larguraBox / 2);
                y = centroY - (alturaTotal / 2);
            } else {
                double angulo = 2 * Math.PI * (i - 1) / (classes.size() - 1);
                x = (int) (centroX + raioX * Math.cos(angulo)) - (larguraBox / 2);
                y = (int) (centroY + raioY * Math.sin(angulo)) - (alturaTotal / 2);
            }
            classe.x = x; classe.y = y;

            svg.append(String.format("<rect x=\"%d\" y=\"%d\" width=\"%d\" height=\"%d\" class=\"box\" />\n", x, y, larguraBox, alturaTotal));
            svg.append(String.format("<rect x=\"%d\" y=\"%d\" width=\"%d\" height=\"%d\" class=\"header-box\" />\n", x, y, larguraBox, alturaHeader));
            svg.append(String.format("<text x=\"%d\" y=\"%d\" class=\"titulo\" text-anchor=\"middle\">%s</text>\n", x + larguraBox/2, y + 20, classe.nome));
            svg.append(String.format("<line x1=\"%d\" y1=\"%d\" x2=\"%d\" y2=\"%d\" class=\"linha\" />\n", x, y + alturaHeader, x + larguraBox, y + alturaHeader));

            int cursorY = y + alturaHeader + 15;
            for (AtributoUML atr : classe.atributos) {
                String icon = atr.visibilidade.equals("-") ? "-" : "+";
                svg.append(String.format("<text x=\"%d\" y=\"%d\" class=\"texto\">%s %s : %s</text>\n", x + 10, cursorY, icon, atr.nome, atr.tipo));
                cursorY += 15;
            }
            if (!classe.metodos.isEmpty()) {
                svg.append(String.format("<line x1=\"%d\" y1=\"%d\" x2=\"%d\" y2=\"%d\" class=\"linha\" />\n", x, cursorY - 5, x + larguraBox, cursorY - 5));
                cursorY += 10;
            }
            for (MetodoUML met : classe.metodos) {
                String icon = met.visibilidade.equals("-") ? "-" : "+";
                svg.append(String.format("<text x=\"%d\" y=\"%d\" class=\"texto\" style=\"font-style:italic\">%s %s() : %s</text>\n", x + 10, cursorY, icon, met.nome, met.tipoRetorno));
                cursorY += 15;
            }
        }

        // 2. Relacionamentos
        for (RelacionamentoUML rel : relacionamentos) {
            ClasseUML origem = buscarClasse(rel.classeOrigem);
            ClasseUML destino = buscarClasse(rel.classeDestino);
            if (origem != null && destino != null) {
                int cx1 = origem.x + origem.width / 2;
                int cy1 = origem.y + origem.height / 2;
                int cx2 = destino.x + destino.width / 2;
                int cy2 = destino.y + destino.height / 2;
                int dx = cx2 - cx1;
                int dy = cy2 - cy1;
                int startX, startY, endX, endY;

                if (Math.abs(dx) > Math.abs(dy)) {
                    if (dx > 0) { startX = origem.x + origem.width; startY = cy1; endX = destino.x; endY = cy2; } 
                    else { startX = origem.x; startY = cy1; endX = destino.x + destino.width; endY = cy2; }
                } else { 
                    if (dy > 0) { startX = cx1; startY = origem.y + origem.height; endX = cx2; endY = destino.y; } 
                    else { startX = cx1; startY = origem.y; endX = cx2; endY = destino.y + destino.height; }
                }

                String markerEnd = obterMarcador(rel.tipoRelacionamento);
                String dashArray = (rel.tipoRelacionamento == TipoToken.T_IMPLEMENTACAO || rel.tipoRelacionamento == TipoToken.T_LINK) ? "5,5" : "0";
                svg.append(String.format("<line x1=\"%d\" y1=\"%d\" x2=\"%d\" y2=\"%d\" class=\"linha\" stroke-dasharray=\"%s\" marker-end=\"url(#%s)\" />\n", startX, startY, endX, endY, dashArray, markerEnd));

                if (rel.label != null && !rel.label.isEmpty()) {
                    int mx = (startX + endX) / 2;
                    int my = (startY + endY) / 2;
                    int textWidth = rel.label.length() * 7;
                    svg.append(String.format("<rect x=\"%d\" y=\"%d\" width=\"%d\" height=\"14\" class=\"label-bg\" />\n", mx - textWidth/2, my - 10, textWidth));
                    svg.append(String.format("<text x=\"%d\" y=\"%d\" class=\"texto\" fill=\"blue\" text-anchor=\"middle\">%s</text>\n", mx, my, rel.label));
                }
            }
        }
        svg.append("</svg>");
        try (FileWriter writer = new FileWriter(caminhoArquivo)) { writer.write(svg.toString()); }
    }

    private ClasseUML buscarClasse(String nome) {
        for (ClasseUML c : classes) if (c.nome.equals(nome)) return c;
        return null;
    }
    
    private String obterMarcador(TipoToken tipo) {
        switch (tipo) {
            case T_HERANCA: return "seta_heranca";
            case T_IMPLEMENTACAO: return "seta_heranca";
            case T_AGREGACAO: return "seta_agregacao";
            case T_COMPOSICAO: return "seta_composicao";
            case T_ASSOCIACAO: return "seta_open";
            default: return "none";
        }
    }
}