import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GeradorSVG {

    private final List<ClasseUML> classes;
    private final List<RelacionamentoUML> relacionamentos;

    public GeradorSVG(List<ClasseUML> classes, List<RelacionamentoUML> relacionamentos) {
        this.classes = classes;
        this.relacionamentos = relacionamentos;
    }

    public void gerarArquivo(String caminhoArquivo) throws IOException {
        StringBuilder svg = new StringBuilder();
        
        // --- NOVO: Lógica de Centralidade ---
        // 1. Contar conexões por classe
        Map<String, Integer> conexoes = new HashMap<>();
        for (ClasseUML c : classes) conexoes.put(c.nome, 0);
        
        for (RelacionamentoUML r : relacionamentos) {
            conexoes.put(r.classeOrigem, conexoes.getOrDefault(r.classeOrigem, 0) + 1);
            conexoes.put(r.classeDestino, conexoes.getOrDefault(r.classeDestino, 0) + 1);
        }

        // 2. Ordenar classes: As mais conectadas primeiro
        Collections.sort(classes, (c1, c2) -> {
            int qtd1 = conexoes.getOrDefault(c1.nome, 0);
            int qtd2 = conexoes.getOrDefault(c2.nome, 0);
            return qtd2 - qtd1; // Ordem decrescente
        });
        
        // --- Configuração do Canvas ---
        int larguraBox = 220;
        int larguraCanvas = 1400; // Canvas maior para o círculo
        int alturaCanvas = 1200;
        int centroX = larguraCanvas / 2;
        int centroY = alturaCanvas / 2;
        int raioX = 550; // Raio horizontal da elipse
        int raioY = 450; // Raio vertical da elipse

        svg.append(String.format("<svg width=\"%d\" height=\"%d\" xmlns=\"http://www.w3.org/2000/svg\">\n", larguraCanvas, alturaCanvas));
        
        // --- DEFINIÇÕES E ESTILOS (Mantidos iguais) ---
        svg.append("<defs>\n");
        svg.append("<filter id=\"sombra\" x=\"0\" y=\"0\" width=\"200%\" height=\"200%\">\n");
        svg.append("<feOffset result=\"offOut\" in=\"SourceAlpha\" dx=\"3\" dy=\"3\" />\n");
        svg.append("<feGaussianBlur result=\"blurOut\" in=\"offOut\" stdDeviation=\"2\" />\n");
        svg.append("<feBlend in=\"SourceGraphic\" in2=\"blurOut\" mode=\"normal\" />\n");
        svg.append("</filter>\n");

        // Marcadores
        svg.append("<marker id=\"seta_open\" markerWidth=\"12\" markerHeight=\"12\" refX=\"9\" refY=\"3\" orient=\"auto\">\n");
        svg.append("<path d=\"M0,0 L0,6 L9,3 z\" fill=\"black\" />\n"); 
        svg.append("</marker>\n");

        svg.append("<marker id=\"seta_heranca\" markerWidth=\"16\" markerHeight=\"16\" refX=\"14\" refY=\"7\" orient=\"auto\">\n");
        svg.append("<path d=\"M0,0 L14,7 L0,14 L0,0\" fill=\"white\" stroke=\"black\" />\n"); 
        svg.append("</marker>\n");

        svg.append("<marker id=\"seta_agregacao\" markerWidth=\"18\" markerHeight=\"12\" refX=\"16\" refY=\"5\" orient=\"auto\">\n");
        svg.append("<path d=\"M0,5 L8,0 L16,5 L8,10 z\" fill=\"white\" stroke=\"black\" />\n"); 
        svg.append("</marker>\n");
        
        svg.append("<marker id=\"seta_composicao\" markerWidth=\"18\" markerHeight=\"12\" refX=\"16\" refY=\"5\" orient=\"auto\">\n");
        svg.append("<path d=\"M0,5 L8,0 L16,5 L8,10 z\" fill=\"black\" stroke=\"black\" />\n"); 
        svg.append("</marker>\n");
        svg.append("</defs>\n");

        svg.append("<style>\n");
        svg.append(".texto { font-family: Arial, sans-serif; font-size: 12px; fill: #333; }\n");
        svg.append(".titulo { font-family: Arial, sans-serif; font-size: 14px; font-weight: bold; fill: black; }\n");
        svg.append(".box { fill: #fff; stroke: #333; stroke-width: 1; filter: url(#sombra); }\n");
        svg.append(".header-box { fill: #f0f0f0; stroke: #333; stroke-width: 1; }\n");
        svg.append(".linha { stroke: #333; stroke-width: 1.5; }\n");
        svg.append(".label-bg { fill: white; opacity: 0.9; }\n"); 
        svg.append("</style>\n");
        
        // 1. Calcular Posições (LAYOUT RADIAL)
        for (int i = 0; i < classes.size(); i++) {
            ClasseUML classe = classes.get(i);
            
            // Cálculos de altura da caixa
            int alturaHeader = 30;
            int alturaAtributos = (classe.atributos.size() * 15) + 10;
            int alturaMetodos = (classe.metodos.size() * 15) + 10;
            int alturaTotal = alturaHeader + alturaAtributos + alturaMetodos + 5;
            
            classe.width = larguraBox;
            classe.height = alturaTotal;

            int x, y;
            
            if (i == 0) {
                // A classe mais conectada vai no CENTRO
                x = centroX - (larguraBox / 2);
                y = centroY - (alturaTotal / 2);
            } else {
                // As outras formam um círculo ao redor
                // (i - 1) para pular a central
                double angulo = 2 * Math.PI * (i - 1) / (classes.size() - 1);
                
                // Distribui em uma elipse
                x = (int) (centroX + raioX * Math.cos(angulo)) - (larguraBox / 2);
                y = (int) (centroY + raioY * Math.sin(angulo)) - (alturaTotal / 2);
            }
            
            classe.x = x;
            classe.y = y;

            // Desenha a Classe
            svg.append(String.format("<rect x=\"%d\" y=\"%d\" width=\"%d\" height=\"%d\" class=\"box\" />\n", x, y, larguraBox, alturaTotal));
            svg.append(String.format("<rect x=\"%d\" y=\"%d\" width=\"%d\" height=\"%d\" class=\"header-box\" />\n", x, y, larguraBox, alturaHeader));
            svg.append(String.format("<text x=\"%d\" y=\"%d\" class=\"titulo\" text-anchor=\"middle\">%s</text>\n", x + larguraBox/2, y + 20, classe.nome));
            svg.append(String.format("<line x1=\"%d\" y1=\"%d\" x2=\"%d\" y2=\"%d\" class=\"linha\" />\n", x, y + alturaHeader, x + larguraBox, y + alturaHeader));

            int cursorY = y + alturaHeader + 15;
            for (AtributoUML atr : classe.atributos) {
                String icon = atr.visibilidade.equals("-") ? "-" : "+";
                String texto = String.format("%s %s : %s", icon, atr.nome, atr.tipo);
                svg.append(String.format("<text x=\"%d\" y=\"%d\" class=\"texto\">%s</text>\n", x + 10, cursorY, texto));
                cursorY += 15;
            }

            if (!classe.metodos.isEmpty()) {
                svg.append(String.format("<line x1=\"%d\" y1=\"%d\" x2=\"%d\" y2=\"%d\" class=\"linha\" />\n", x, cursorY - 5, x + larguraBox, cursorY - 5));
                cursorY += 10;
            }

            for (MetodoUML met : classe.metodos) {
                String icon = met.visibilidade.equals("-") ? "-" : "+";
                String texto = String.format("%s %s() : %s", icon, met.nome, met.tipoRetorno);
                svg.append(String.format("<text x=\"%d\" y=\"%d\" class=\"texto\" style=\"font-style:italic\">%s</text>\n", x + 10, cursorY, texto));
                cursorY += 15;
            }
        }

        // 2. Desenhar Relacionamentos (Borda a Borda)
        for (RelacionamentoUML rel : relacionamentos) {
            ClasseUML origem = buscarClasse(rel.classeOrigem);
            ClasseUML destino = buscarClasse(rel.classeDestino);

            if (origem != null && destino != null) {
                // Centros
                int cx1 = origem.x + origem.width / 2;
                int cy1 = origem.y + origem.height / 2;
                int cx2 = destino.x + destino.width / 2;
                int cy2 = destino.y + destino.height / 2;

                int dx = cx2 - cx1;
                int dy = cy2 - cy1;
                int startX, startY, endX, endY;

                // Lógica simples para decidir de qual lado sair
                if (Math.abs(dx) > Math.abs(dy)) {
                    if (dx > 0) { // Destino à direita
                        startX = origem.x + origem.width; 
                        startY = cy1;
                        endX = destino.x; 
                        endY = cy2;
                    } else { // Destino à esquerda
                        startX = origem.x; 
                        startY = cy1;
                        endX = destino.x + destino.width; 
                        endY = cy2;
                    }
                } else { // Destino em cima ou embaixo
                    if (dy > 0) { // Destino embaixo
                        startX = cx1;
                        startY = origem.y + origem.height;
                        endX = cx2;
                        endY = destino.y; 
                    } else { // Destino em cima
                        startX = cx1;
                        startY = origem.y; 
                        endX = cx2;
                        endY = destino.y + destino.height; 
                    }
                }

                String markerEnd = obterMarcador(rel.tipoRelacionamento);
                String dashArray = (rel.tipoRelacionamento == TipoToken.T_IMPLEMENTACAO || rel.tipoRelacionamento == TipoToken.T_LINK) ? "5,5" : "0";

                svg.append(String.format("<line x1=\"%d\" y1=\"%d\" x2=\"%d\" y2=\"%d\" class=\"linha\" stroke-dasharray=\"%s\" marker-end=\"url(#%s)\" />\n", 
                        startX, startY, endX, endY, dashArray, markerEnd));

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

        try (FileWriter writer = new FileWriter(caminhoArquivo)) {
            writer.write(svg.toString());
        }
    }

    private ClasseUML buscarClasse(String nome) {
        for (ClasseUML c : classes) {
            if (c.nome.equals(nome)) return c;
        }
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