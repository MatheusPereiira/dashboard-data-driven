# Especificação Técnica e Backlog de Sprint: Sistema Pessoal de Monitoramento de Desempenho, Retenção e Risco Acadêmico (Data-Driven)

> **Diretriz Arquitetural e Regra de Negócio:** O sistema opera exclusivamente sob demanda com dados reais fornecidos pelo próprio estudante, sem utilização de massas de dados fictícias (mock/seed) ou painéis estáticos. O discente registra suas disciplinas, avaliações parciais/finais, volume de faltas e carga horária. Após a submissão, o backend persiste os registros no **MongoDB**, executa o processamento analítico e preditivo e consolida automaticamente o **Dashboard de Desempenho Acadêmico**, apresentando indicadores, projeções e alertas de risco de reprovação ou evasão.

---

## 1. Modelagem de Dados (Coleções MongoDB)

Seguindo a arquitetura em camadas adotada no projeto de referência `todolist_example` (`model`, `dto`, `mapper`, `repository`, `service`, `controller`, `exception`), a persistência no **MongoDB** estrutura-se em três documentos principais:

### 1.1. `Aluno` (`@Document(collection = "alunos")`)
Representa o perfil acadêmico do estudante e os parâmetros normativos de sua instituição de ensino.
- `id` (`String` — ObjectId do MongoDB)
- `nome` (`String`)
- `matricula` (`String`)
- `curso` (`String` — ex.: *Engenharia de Software*)
- `semestreAtual` (`Integer` — ex.: `3`)
- `mediaMinimaAprovacao` (`Double` — ex.: `6.0` ou `7.0`, parametrizável pelo usuário)
- `frequenciaMinimaExigida` (`Double` — padrão `75.0%`)

### 1.2. `DisciplinaDesempenho` (`@Document(collection = "disciplinas_desempenho")`)
Armazena os registros de avaliações, ausências e indicadores calculados por componente curricular.
- `id` (`String`)
- `alunoId` (`String` — referência à entidade `Aluno`)
- `nomeDisciplina` (`String` — ex.: *Estrutura de Dados*)
- `semestreLetivo` (`String` — ex.: `2026.1`)
- `totalAulasSemestre` (`Integer` — ex.: `80` aulas)
- `quantidadeFaltas` (`Integer` — informado pelo aluno)
- `percentualFrequencia` (`Double` — calculado automaticamente ou informado na entrada)
- `nota1` (`Double` — informada pelo aluno)
- `nota2` (`Double` — informada pelo aluno; opcional enquanto a avaliação não ocorrer)
- `mediaAtual` (`Double` — calculada automaticamente)
- `notaNecessariaParaPassar` (`Double` — pontuação mínima necessária nas avaliações pendentes para aprovação)
- `faltasRestantesPermitidas` (`Integer` — margem de ausências remanescente antes da reprovação por frequência)
- `statusSituacao` (`String` — `APROVADO`, `REGULAR`, `ATENCAO_NOTA`, `ATENCAO_FALTA`, `RISCO_CRITICO`, `REPROVADO`)

### 1.3. `AnalisePreditiva` (`@Document(collection = "analises_preditivas")`)
Documento consolidado e atualizado automaticamente a cada operação de criação, atualização ou remoção de notas e faltas, servindo como fonte primária para o Dashboard.
- `id` (`String`)
- `alunoId` (`String`)
- `dataAtualizacao` (`LocalDateTime`)
- `indiceRendimentoAcademico` (`Double` — média global das disciplinas do aluno)
- `frequenciaGlobal` (`Double` — taxa média de presença acumulada)
- `scoreRiscoEvasaoReprovacao` (`Double` — escala de `0%` a `100%`, processada via camada de serviço / motor preditivo em Python)
- `classificacaoRisco` (`String` — `BAIXO`, `MODERADO`, `ALTO`, `CRITICO`)
- `alertasPreditivos` (`List<String>` — diagnósticos objetivos gerados automaticamente, tais como:
  - *"Atenção em Cálculo I: limite de ausências em 20% (restam 4 faltas permitidas)."*
  - *"Para atingir a média 6.0 em Banco de Dados, é necessário obter nota mínima de 7.5 na Avaliação 2."*)

---

## 2. Fluxo Operacional e Regras de Negócio

1. **Onboarding e Parametrização do Perfil:**
   - O estudante registra Nome, Curso, Semestre e Média Mínima exigida pela instituição (ex.: `6.0`).
   - O painel é inicializado sem registros pré-populados, aguardando a entrada de dados reais.
2. **Entrada de Dados Acadêmicos (Manual ou Processamento em Lote):**
   - **Cadastro Individual via Interface:** Inserção direta de disciplina (ex.: *Algoritmos*, `Nota 1 = 5.0`, `Faltas = 12`, `Total de Aulas = 60`).
   - **Importação via Arquivo CSV (Spring Batch):** Submissão de planilha contendo o histórico ou boletim parcial do semestre para processamento em lote e persistência estruturada no MongoDB.
3. **Processamento Analítico e Atualização em Tempo Real:**
   - Qualquer mutação nos registros de `DisciplinaDesempenho` aciona o recálculo síncrono de `AnalisePreditiva` (integrado ao serviço preditivo em Python) e retorna o estado atualizado para a interface.
   - Visões disponibilizadas no Dashboard:
     - **Indicadores-Chave (KPIs):** Índice de Rendimento Acadêmico (IRA), Frequência Global (%), Score de Risco de Reprovação e Total de Disciplinas em Estado de Alerta.
     - **Visualização 1 (Gráfico de Barras):** Desempenho por disciplina comparado à linha de corte da média mínima institucional.
     - **Visualização 2 (Gráfico de Progresso / Barras Horizontais):** Taxa de consumo do limite de faltas por disciplina em relação ao teto regulamentar de 25% de ausências.
     - **Visualização 3 (Gráfico de Dispersão ou Radar):** Correlação entre rendimento de notas e assiduidade.
     - **Painel de Diagnósticos e Metas:** Lista estruturada de recomendações contendo a pontuação necessária para aprovação e o saldo de faltas disponível por disciplina.

---

## 3. Stack Tecnológica e Arquitetura

- **Backend:** Java 25 + Spring Boot 4 (Gradle)
  - `spring-boot-starter-webmvc` (Exposição de APIs RESTful)
  - `spring-boot-starter-validation` (Validação declarativa com `@Valid`, `@NotBlank`, `@Min`, `@Max`)
  - `spring-boot-starter-data-mongodb` (Persistência orientada a documentos no MongoDB)
  - `spring-boot-starter-batch` (Processamento em lote de arquivos CSV submetidos pelo usuário)
- **Motor Preditivo / IA:** Python (`FastAPI` ou execução integrada) responsável por avaliar os vetores de notas e frequência, estimar a probabilidade de reprovação e emitir recomendações acionáveis.
- **Frontend:** Aplicação Web (React ou HTML/JS com Chart.js) orientada à produtividade, combinando formulários de lançamento rápido com atualização reativa das visualizações.

---

## 4. Backlog da Sprint (Plano de Execução)

### Epic 1: Infraestrutura e Configuração Base (Spring Boot + MongoDB)
- [x] **Task 1.1:** Estruturar o projeto Gradle no diretório raiz (`retention-dashboard`) aderente ao padrão arquitetural do `todolist_example`.
- [x] **Task 1.2:** Configurar a conexão com o **MongoDB** no `application.properties`, garantindo ambiente limpo sem scripts de seed.
- [x] **Task 1.3:** Implementar `GlobalExceptionHandler` (`@RestControllerAdvice`) para padronização de respostas de erro e validação de payloads.

### Epic 2: Camada de Domínio, Persistência e Contratos (Models, DTOs, Mappers, Repositories)
- [x] **Task 2.1:** Mapear as entidades `@Document` (`Aluno`, `DisciplinaDesempenho` e `AnalisePreditiva`) e enums (`StatusSituacao`, `ClassificacaoRisco`) no pacote `model/`.
- [x] **Task 2.2:** Definir os objetos de transferência (`AlunoDTO`, `DisciplinaDesempenhoDTO`, `AnalisePreditivaDTO` e `DashboardPerformanceDTO`) com regras de Bean Validation no pacote `dto/`.
- [x] **Task 2.3:** Implementar os conversores (`AlunoMapper`, `DisciplinaMapper` e `AnaliseMapper`) no pacote `mapper/` utilizando métodos estáticos (`toDTO` / `toEntity`).
- [x] **Task 2.4:** Criar as interfaces `AlunoRepository`, `DisciplinaDesempenhoRepository` e `AnalisePreditivaRepository` estendendo `MongoRepository`.

### Epic 3: Camada de Serviço e Motor de Cálculo Preditivo (`service/`)
- [x] **Task 3.1:** Implementar `AlunoService` para gerenciamento do ciclo de vida do perfil discente.
- [x] **Task 3.2:** Implementar `DesempenhoService` contemplando:
  - Recebimento e validação dos dados de notas, faltas e carga horária.
  - Cálculo automático de percentual de frequência, média parcial/final, nota necessária para aprovação e saldo de faltas permitido.
  - Orquestração automática da atualização de `AnalisePreditiva` a cada operação de escrita ou exclusão.
- [x] **Task 3.3:** Integrar o serviço de cálculo preditivo em **Python** ao fluxo de consolidação de risco.

### Epic 4: Processamento em Lote via Spring Batch (`batch/`)
- [x] **Task 4.1:** Implementar Job do **Spring Batch** para leitura, validação e persistência no MongoDB de arquivos CSV contendo o boletim ou histórico escolar enviado pelo estudante.

### Epic 5: Camada de Apresentação REST (`controller/`)
- [ ] **Task 5.1:** Implementar `AlunoController` (`/api/v1/alunos`):
  - `POST /api/v1/alunos`: Cadastro de perfil discente e parâmetros institucionais (`201 Created`).
  - `GET /api/v1/alunos/{id}`: Consulta de dados cadastrais e parâmetros do estudante (`200 OK`).
  - `PUT /api/v1/alunos/{id}`: Atualização de curso, semestre, média mínima ou frequência exigida (`200 OK`).
  - `GET /api/v1/alunos/{id}/dashboard`: Recuperação consolidada do `DashboardPerformanceDTO` contendo perfil, lista de disciplinas, indicadores globais e `AnalisePreditiva` (`200 OK`).
- [ ] **Task 5.2:** Implementar `DesempenhoController` (`/api/v1/disciplinas`):
  - `POST /api/v1/disciplinas`: Registro de nova disciplina com notas, faltas e carga horária, acionando recálculo preditivo e retornando o dashboard atualizado (`201 Created`).
  - `PUT /api/v1/disciplinas/{id}`: Atualização parcial ou integral de notas e faltas com recálculo síncrono de indicadores (`200 OK`).
  - `GET /api/v1/disciplinas/aluno/{alunoId}`: Listagem de disciplinas vinculadas ao estudante com suporte a filtro por `semestreLetivo` e `statusSituacao` (`200 OK`).
  - `DELETE /api/v1/disciplinas/{id}`: Exclusão de registro de disciplina seguida de reprocessamento imediato da `AnalisePreditiva` (`204 No Content` ou retorno do estado consolidado).
- [ ] **Task 5.3:** Implementar `BatchImportController` (`/api/v1/importacoes`):
  - `POST /api/v1/importacoes/boletim/{alunoId}`: Endpoint `multipart/form-data` para recepção do arquivo CSV do estudante, disparo do Job do **Spring Batch** e retorno do relatório de processamento (registros importados, rejeitados e dashboard recalculado).
- [ ] **Task 5.4:** Padronizar contratos HTTP e cabeçalhos de resposta (`ResponseEntity`, códigos de status semânticos, versionamento de rotas e documentação de payloads).

### Epic 6: Validações de Segurança, Integridade de Domínio e Tratamento de Exceções (`security/`, `validation/`, `exception/`)
- [x] **Task 6.1:** Implementar validações de entrada (Bean Validation) nos DTOs de requisição:
  - Restrições de intervalo numérico para `nota1` e `nota2` (`@DecimalMin("0.0")`, `@DecimalMax("10.0")`).
  - Restrições para `mediaMinimaAprovacao` (`0.0` a `10.0`) e `frequenciaMinimaExigida` (`0.0` a `100.0`).
  - Validação de obrigatoriedade e tamanho máximo de campos textuais (`@NotBlank`, `@Size`) para mitigar payloads excessivos.
- [ ] **Task 6.2:** Implementar validação de invariantes de negócio e consistência cruzada na camada de serviço/validador customizado:
  - Impedir que `quantidadeFaltas` seja negativa ou superior a `totalAulasSemestre`.
  - Impedir que `totalAulasSemestre` seja menor ou igual a zero.
  - Prevenir duplicidade de cadastro da mesma disciplina no mesmo `semestreLetivo` para o mesmo `alunoId`.
  - Validar formato de identificadores (`ObjectId`) antes de consultas ao MongoDB para evitar exceções de conversão não tratadas.
- [ ] **Task 6.3:** Implementar blindagem e validação de segurança no upload de arquivos CSV (Spring Batch):
  - Validação estrita de extensão (`.csv`), Content-Type (`text/csv`) e limite máximo de tamanho de arquivo (`max-file-size`).
  - Sanitização de células do CSV contra **CSV Injection / Formula Injection** (bloqueio ou escape de valores iniciados por `=`, `+`, `-`, `@`).
  - Rejeição controlada de linhas malformadas via `SkipPolicy` / `ExecutionListener` sem derrubar a aplicação.
- [ ] **Task 6.4:** Configurar políticas de segurança da API REST:
  - Configuração explícita de **CORS** (`WebMvcConfigurer`) restrita às origens autorizadas do Frontend.
  - Sanitização de strings de entrada para prevenção de **XSS** (Cross-Site Scripting) e **NoSQL Injection** em filtros de consulta do MongoDB.
  - Padronização de respostas de erro no `GlobalExceptionHandler` ocultando *stack traces* internas e detalhes sensíveis de infraestrutura em respostas `400 Bad Request`, `404 Not Found`, `422 Unprocessable Entity` e `500 Internal Server Error`.

### Epic 7: Interface do Usuário e Visualização de Dados (`frontend/`)
- [ ] **Task 7.1:** Estruturar arquitetura do Frontend e camada de integração HTTP:
  - Configurar cliente HTTP centralizado (`fetch` ou `axios`) apontando para `/api/v1` com interceptadores de erro e tratamento padronizado de mensagens vindas do `GlobalExceptionHandler`.
  - Implementar gerenciamento de estado reativo do estudante ativo (`alunoId`, perfil institucional, lista de disciplinas e `AnalisePreditiva`).
- [ ] **Task 7.2:** Desenvolver módulo de Onboarding e Parametrização do Estudante:
  - Formulário de cadastro/edição de perfil (Nome, Matrícula, Curso, Semestre Atual, Média Mínima de Aprovação e Frequência Mínima Exigida).
  - Persistência da sessão/identificação do aluno ativo e transição fluida do estado vazio (*Empty State*) para o painel operacional.
- [ ] **Task 7.3:** Desenvolver módulo de Lançamento Rápido e Gestão de Disciplinas:
  - Formulário com validação client-side espelhando as regras de negócio (bloqueio de notas fora de `0–10`, validação em tempo real de `Faltas <= Total de Aulas`).
  - Tabela/Listagem interativa de disciplinas cursadas com ações rápidas de edição inline de notas/faltas, exclusão com confirmação e badges visuais de `statusSituacao` (`APROVADO`, `REGULAR`, `ATENCAO_NOTA`, `ATENCAO_FALTA`, `RISCO_CRITICO`, `REPROVADO`).
- [ ] **Task 7.4:** Desenvolver componente de Importação de Boletim via CSV:
  - Área de *Drag-and-Drop* / seleção de arquivo `.csv` com validação prévia de extensão e tamanho no navegador.
  - Indicador de progresso de envio e exibição do resumo de importação retornado pelo endpoint do Spring Batch.
- [ ] **Task 7.5:** Implementar Painel de KPIs e Visualizações Gráficas (Data-Driven):
  - **Cards de Indicadores Globais:** IRA (Média Geral), Frequência Global (%), Score de Risco Preditivo (`0%–100%` com codificação semântica de cor por `classificacaoRisco`) e Total de Disciplinas em Alerta.
  - **Gráfico Comparativo de Notas (Barras):** Notas atuais por disciplina sobrepostas à linha de referência da `mediaMinimaAprovacao`.
  - **Gráfico de Consumo de Limite de Faltas (Barras Horizontais / Progresso):** Percentual de faltas utilizadas vs. margem restante (`faltasRestantesPermitidas`) até o limite de reprovação por frequência.
  - **Gráfico de Correlação Nota × Frequência (Radar ou Dispersão):** Visão multidimensional de equilíbrio acadêmico do estudante.
- [ ] **Task 7.6:** Implementar Painel de Diagnósticos e Metas Preditivas e refinamento de UX:
  - Renderização estruturada dos `alertasPreditivos` e simulador/indicador de `notaNecessariaParaPassar` por disciplina.
  - Estados visuais de carregamento (*Skeleton / Loading States*), notificações de feedback (*Toasts* de sucesso/erro) e layout responsivo para desktop e dispositivos móveis.
