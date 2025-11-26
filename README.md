
# Compilador PlantUML para SVG

Este projeto é um compilador desenvolvido em Java que traduz diagramas de classe escritos na linguagem PlantUML para gráficos vetoriais no formato SVG.

O objetivo é automatizar a visualização de diagramas UML a partir de código textual, implementando todas as etapas clássicas de um compilador: Análise Léxica, Sintática, Semântica e Geração de Código.

## 🚀 Funcionalidades

- Análise Léxica Completa: Reconhece palavras-chave, símbolos, operadores complexos (ex: <|--, *--) e ignora comentários.

- Parser Recursivo Descendente: Valida a gramática do PlantUML e constrói uma Árvore Sintática Abstrata (AST) em memória.

- Verificação Semântica: Garante a integridade referencial do diagrama (ex: verifica se uma classe usada em um relacionamento foi declarada).

- Gerador de SVG Inteligente:
    - Layout Radial: Posiciona a classe mais conectada no centro e distribui as outras ao redor para minimizar cruzamento de linhas.
    - Conexões Precisas: As linhas conectam-se às bordas das caixas (não ao centro), garantindo um visual limpo.
    - Estilização: Classes com sombras, ícones de visibilidade e pontas de seta corretas (herança, composição, agregação).

## 📂 Estrutura do Projeto

```text
/src
  ├── App.java                 # Classe principal (Ponto de Entrada)
  ├── AnalisadorLexico.java    # Transforma texto bruto em Tokens
  ├── AnalisadorSintatico.java # Transforma Tokens em Objetos (AST)
  ├── AnalisadorSemantico.java # Valida regras lógicas do diagrama
  ├── GeradorSVG.java          # Transforma a AST em arquivo .svg
  ├── Token.java               # Definição da estrutura do Token
  ├── TipoToken.java           # Enumeração dos tipos de tokens
  ├── ClasseUML.java           # Modelo de dados para Classes
  ├── AtributoUML.java         # Modelo de dados para Atributos
  ├── MetodoUML.java           # Modelo de dados para Métodos
  └── RelacionamentoUML.java   # Modelo de dados para Relacionamentos
/diagrama.svg                  # Arquivo de saída gerado
/teste.txt                     # Arquivo de entrada (código PlantUML)
```


## 🛠️ Como Executar

### Pré-requisitos

- Java JDK 11 ou superior instalado.

- Um editor de código (VS Code, IntelliJ, Eclipse) ou terminal.

### Passo a Passo

1. Clone o repositório ou baixe os arquivos.

2. Certifique-se de que o arquivo `teste.txt` está na raiz do projeto com o código PlantUML que deseja converter.

3. Compile o projeto:
   ```bash
   javac -d bin src/*.java
   ```


Execute o compilador:

java -cp bin App


O arquivo diagrama.svg será gerado na raiz do projeto. Abra-o em qualquer navegador web (Chrome, Edge, Firefox) para visualizar o resultado.

## 📝 Exemplo de Entrada (teste.txt)

```text
@startuml
title Exemplo de Sistema
class Usuario {
  - nome: String
  + login()
}
class Sistema {
  + autenticar()
}
Usuario --> Sistema : acessa >
@enduml
```