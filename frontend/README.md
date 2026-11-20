# Frontend - Dashboard de Desempenho, Retenção e Risco Acadêmico (Data-Driven)

Interface web moderna, reativa e orientada a dados construída para acompanhamento de rendimento estudantil, controle de assiduidade e visualizações preditivas de evasão e retenção.

---

## 🚀 Tecnologias Utilizadas

- **Framework:** [React 18](https://react.dev/) com [TypeScript](https://www.typescriptlang.org/)
- **Build Tool:** [Vite](https://vitejs.dev/)
- **Estilização:** [Tailwind CSS](https://tailwindcss.com/) com design escuro (*dark mode*), efeitos de vidro (*glassmorphism*) e animações fluidas
- **Visualização de Dados e Gráficos:** [Recharts](https://recharts.org/) (Gráficos de barras, consumo de faltas e radar multidimensional)
- **Ícones:** [Lucide React](https://lucide.dev/)
- **Comunicação HTTP:** API REST nativa com tipagem estrita e tratamento de erros do `GlobalExceptionHandler` do Spring Boot

---

## 📋 Funcionalidades Implementadas

1. **Gestão e Parametrização do Estudante (Onboarding):**
   - Cadastro e edição do discente (Nome, Matrícula, Curso, Semestre Atual).
   - Definição dos parâmetros normativos da instituição: **Média Mínima de Aprovação** e **Frequência Mínima Exigida (%)**.
   - Seletor rápido de alunos no cabeçalho com persistência do último discente visualizado via `localStorage`.

2. **Indicadores Globais em Tempo Real (KPIs):**
   - **Índice de Rendimento Acadêmico (IRA):** Média global das notas com comparação visual da linha de corte.
   - **Frequência Global (%):** Taxa de presença agregada no semestre.
   - **Score Preditivo de Risco (IA / Python):** Probabilidade de retenção/evasão (`0%` a `100%`) com badges semânticos (`BAIXO`, `MODERADO`, `ALTO`, `CRÍTICO`) e pulso animado em caso crítico.
   - **Total de Disciplinas em Alerta:** Contagem imediata de matérias que requerem atenção urgente.

3. **Painel de Diagnósticos e Metas Preditivas:**
   - Lista acionável de alertas gerados pelo motor preditivo (`alertasPreditivos`).
   - Apresentação da pontuação necessária nas avaliações pendentes para aprovação (`notaNecessariaParaPassar`).
   - Saldo exato de faltas restantes permitidas antes da reprovação por frequência (`faltasRestantesPermitidas`).

4. **Visualizações Analíticas Interativas (Gráficos):**
   - **Desempenho por Disciplina vs Linha de Corte:** Gráfico de barras comparando as notas à média mínima institucional.
   - **Consumo do Limite de Ausências:** Gráfico de barras empilhadas exibindo faltas acumuladas vs margem restante até o teto de 25%.
   - **Radar Multidimensional:** Correlação integrada entre rendimento acadêmico e assiduidade.

5. **Gestão de Componentes Curriculares:**
   - Tabela detalhada com busca textual por nome de matéria e filtros por semestre e situação (`APROVADO`, `REGULAR`, `ATENCAO_NOTA`, `ATENCAO_FALTA`, `RISCO_CRITICO`, `REPROVADO`).
   - Modal de lançamento e edição com validações automáticas client-side (`Faltas <= Total de Aulas`, notas entre `0.0` e `10.0`) e simulação em tempo real de média e frequência.
   - Exclusão com confirmação e recálculo síncrono.

6. **Importação em Lote via CSV (Spring Batch):**
   - Modal com área *Drag-and-Drop* para upload do boletim escolar.
   - Botão para download do arquivo modelo (`modelo-boletim.csv`).
   - Exibição do relatório de processamento do Spring Batch (registros lidos, importados, rejeitados e avisos).

---

## 🛠️ Como Executar o Frontend

### Pré-requisitos
- Node.js versão 18+ (recomendado Node 20+)
- Backend Spring Boot em execução na porta `8080`

### Instalação das dependências
```bash
npm install
```

### Iniciar o servidor de desenvolvimento (Vite)
```bash
npm run dev
```

A aplicação estará disponível em:
👉 **http://localhost:5173**

*(A porta 5173 já está liberada nas regras de CORS do backend Spring Boot).*

### Build para Produção
```bash
npm run build
```
Os arquivos otimizados serão gerados na pasta `dist/`.

---

## 📁 Estrutura de Arquivos

```text
frontend/
├── src/
│   ├── components/
│   │   ├── AlertasBanner.tsx     # Alertas e diagnósticos preditivos
│   │   ├── AlunoModal.tsx        # Modal de cadastro/edição de perfil
│   │   ├── ChartsSection.tsx     # Gráficos Recharts (Barras e Radar)
│   │   ├── CsvImportModal.tsx    # Upload Drag & Drop do CSV do Spring Batch
│   │   ├── DisciplinaModal.tsx   # Modal de lançamento de notas e faltas
│   │   ├── DisciplinasTable.tsx  # Tabela com busca, filtros e ações
│   │   ├── EmptyState.tsx        # Telas de onboarding limpas
│   │   ├── KpiCards.tsx          # Cards de indicadores globais e risco
│   │   ├── Navbar.tsx            # Cabeçalho e seletor de estudantes
│   │   └── Toast.tsx             # Notificações flutuantes
│   ├── services/
│   │   └── api.ts                # Cliente HTTP para a API REST (/api/v1)
│   ├── types/
│   │   └── api.ts                # Tipagens TypeScript dos contratos DTO
│   ├── App.tsx                   # Componente raiz e orquestrador de estado
│   ├── index.css                 # Estilos Tailwind e animações
│   └── main.tsx                  # Ponto de entrada React
├── index.html
├── package.json
├── tailwind.config.js
└── vite.config.ts
```
