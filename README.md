# Sistema Pessoal de Monitoramento de Desempenho, Retenção e Risco Acadêmico (Data-Driven)

Aplicação web orientada a dados (*Data-Driven*) para acompanhamento individual de rendimento universitário, controle de assiduidade e análise preditiva de risco de reprovação ou evasão escolar.

---

## 1. Visão Geral

Diferente de painéis administrativos estáticos ou sistemas baseados em massas de dados fictícias (*mock/seed*), esta plataforma opera exclusivamente a partir dos dados reais fornecidos pelo próprio estudante.

O discente configura os parâmetros normativos de sua instituição de ensino (média mínima de aprovação e percentual mínimo de frequência) e registra suas disciplinas, notas parciais/finais, carga horária semestral e quantidade de faltas — manualmente via formulário ou em lote por meio de importação de arquivo CSV processado via **Spring Batch**.

A cada inserção, atualização ou exclusão de registro acadêmico, o backend persiste os dados no **MongoDB**, executa o motor de cálculo e análise preditiva (integrado a serviço em **Python**) e atualiza em tempo real o **Dashboard de Desempenho Acadêmico**, fornecendo:
- **Indicadores Globais (KPIs):** Índice de Rendimento Acadêmico (IRA), Frequência Global (%), Score Preditivo de Risco de Reprovação/Evasão (`0%` a `100%`) e total de disciplinas em estado de alerta.
- **Projeções Acadêmicas Automáticas:** Pontuação mínima necessária nas avaliações subsequentes para aprovação e margem exata de faltas restantes antes do limite regulamentar de reprovação por frequência.
- **Visualizações Analíticas:** Gráficos comparativos de notas vs. média institucional, taxa de consumo do limite de ausências e correlação entre rendimento e assiduidade.

---

## 2. Stack Tecnológica

### Backend
- **Linguagem:** Java 25 (LTS)
- **Framework Principal:** Spring Boot 4 (`4.0.0-M1`)
- **Módulos Spring:**
  - `spring-boot-starter-web`: Exposição de APIs RESTful e suporte a upload `multipart/form-data`.
  - `spring-boot-starter-data-mongodb`: Persistência NoSQL orientada a documentos no MongoDB com criação automática de índices.
  - `spring-boot-starter-validation`: Validação declarativa de contratos de entrada (`Jakarta Bean Validation`).
  - `spring-boot-starter-batch`: Processamento em lote de boletins e históricos escolares em formato CSV.
- **Automação de Build:** Gradle `9.3.1` (com Gradle Wrapper integrado ao repositório).

### Banco de Dados
- **MongoDB:** Armazenamento não relacional estruturado nas coleções `alunos`, `disciplinas_desempenho` e `analises_preditivas`.

### Motor Preditivo e Inteligência Analítica
- **Python (FastAPI / Serviço Integrado):** Processamento de vetores de notas e frequência para estimação probabilística de risco acadêmico e geração de diagnósticos acionáveis.

### Frontend
- **Interface Web:** Aplicação cliente (React ou HTML5/CSS3/JavaScript ES6+ com Chart.js) com atualização reativa das visualizações e validação client-side.

---

## 3. Arquitetura do Projeto

O projeto adota arquitetura em camadas com separação estrita de responsabilidades entre domínio, contratos de transferência, mapeamento, persistência, regras de negócio e exposição HTTP:

```text
dashboard_data_driven/
├── gradle/wrapper/
├── src/
│   ├── main/
│   │   ├── java/com/academic/dashboard/
│   │   │   ├── RetentionDashboardApplication.java
│   │   │   ├── batch/          # Jobs, Steps, Readers, Processors e Writers do Spring Batch (CSV)
│   │   │   ├── config/         # Configuracoes de CORS, Beans e infraestrutura
│   │   │   ├── controller/     # Endpoints RESTful (/api/v1/*)
│   │   │   ├── dto/            # Objetos de transferencia de dados e regras de Bean Validation
│   │   │   ├── exception/      # Excecoes de dominio e GlobalExceptionHandler (@RestControllerAdvice)
│   │   │   ├── mapper/         # Conversores estaticos entre Entidades e DTOs (toDTO / toEntity)
│   │   │   ├── model/          # Entidades @Document do MongoDB e Enums de dominio
│   │   │   ├── repository/     # Interfaces Spring Data MongoDB (MongoRepository)
│   │   │   └── service/        # Regras de negocio, calculos academicos e integracao Python
│   │   └── resources/
│   │       └── application.properties
│   └── test/
├── build.gradle
├── settings.gradle
├── gradlew
├── gradlew.bat
└── task.md
```

---

## 4. Modelagem de Dados (MongoDB)

### 4.1. Coleção `alunos` (`Aluno`)
Representa o perfil acadêmico do estudante e os critérios de avaliação da sua instituição.

| Campo | Tipo | Descrição |
| :--- | :--- | :--- |
| `id` | `String` | Identificador único do documento (`ObjectId`). |
| `nome` | `String` | Nome completo do estudante. |
| `matricula` | `String` | Registro acadêmico único (índice `unique`). |
| `curso` | `String` | Nome do curso de graduação. |
| `semestreAtual` | `Integer` | Período letivo atual do estudante (`1` a `20`). |
| `mediaMinimaAprovacao` | `Double` | Nota de corte para aprovação direta na instituição (ex.: `6.0` ou `7.0`). |
| `frequenciaMinimaExigida` | `Double` | Percentual mínimo de presença exigido (padrão: `75.0%`). |

### 4.2. Coleção `disciplinas_desempenho` (`DisciplinaDesempenho`)
Armazena os inputs de notas e faltas de cada matéria e os indicadores calculados pelo sistema.

| Campo | Tipo | Descrição |
| :--- | :--- | :--- |
| `id` | `String` | Identificador único do documento (`ObjectId`). |
| `alunoId` | `String` | Referência indexada ao documento `Aluno`. |
| `nomeDisciplina` | `String` | Nome do componente curricular. |
| `semestreLetivo` | `String` | Identificação do período letivo (ex.: `2026.1`). |
| `totalAulasSemestre` | `Integer` | Carga horária total da disciplina em número de aulas. |
| `quantidadeFaltas` | `Integer` | Total de ausências acumuladas pelo estudante. |
| `percentualFrequencia` | `Double` | Taxa de presença calculada automaticamente (`0.0%` a `100.0%`). |
| `nota1` | `Double` | Nota da primeira avaliação (`0.0` a `10.0`). |
| `nota2` | `Double` | Nota da segunda avaliação (`0.0` a `10.0`, opcional enquanto pendente). |
| `mediaAtual` | `Double` | Média calculada com base nas avaliações registradas. |
| `notaNecessariaParaPassar` | `Double` | Pontuação necessária para atingir a `mediaMinimaAprovacao`. |
| `faltasRestantesPermitidas` | `Integer` | Saldo de ausências disponível antes da reprovação por frequência. |
| `statusSituacao` | `StatusSituacao` | `APROVADO`, `REGULAR`, `ATENCAO_NOTA`, `ATENCAO_FALTA`, `RISCO_CRITICO`, `REPROVADO`. |

### 4.3. Coleção `analises_preditivas` (`AnalisePreditiva`)
Documento consolidado e atualizado automaticamente a cada modificação de notas ou faltas do estudante.

| Campo | Tipo | Descrição |
| :--- | :--- | :--- |
| `id` | `String` | Identificador único do documento (`ObjectId`). |
| `alunoId` | `String` | Referência única ao estudante (`unique`). |
| `dataAtualizacao` | `LocalDateTime` | Data e hora do último processamento analítico. |
| `indiceRendimentoAcademico` | `Double` | Média global de desempenho nas disciplinas matriculadas. |
| `frequenciaGlobal` | `Double` | Percentual médio de assiduidade no semestre. |
| `scoreRiscoEvasaoReprovacao` | `Double` | Probabilidade estimada de risco acadêmico (`0.0%` a `100.0%`). |
| `classificacaoRisco` | `ClassificacaoRisco` | `BAIXO`, `MODERADO`, `ALTO`, `CRITICO`. |
| `alertasPreditivos` | `List<String>` | Lista de diagnósticos e recomendações geradas automaticamente. |

---

## 5. Contratos da API REST (`/api/v1`)

### Perfil e Dashboard Consolidado (`AlunoController`)
- `POST /api/v1/alunos` — Cadastra o estudante e seus parâmetros institucionais (`201 Created`).
- `GET /api/v1/alunos/{id}` — Retorna os dados cadastrais do estudante (`200 OK`).
- `PUT /api/v1/alunos/{id}` — Atualiza parâmetros do perfil acadêmico (`200 OK`).
- `GET /api/v1/alunos/{id}/dashboard` — Retorna o `DashboardPerformanceDTO` contendo perfil, disciplinas, métricas globais e análise preditiva (`200 OK`).

### Gestão de Desempenho por Disciplina (`DesempenhoController`)
- `POST /api/v1/disciplinas` — Registra disciplina, calcula indicadores e atualiza a análise preditiva (`201 Created`).
- `PUT /api/v1/disciplinas/{id}` — Atualiza notas ou faltas e reprocessa o dashboard em tempo real (`200 OK`).
- `GET /api/v1/disciplinas/aluno/{alunoId}` — Lista as disciplinas do estudante com suporte a filtros por `semestreLetivo` e `statusSituacao` (`200 OK`).
- `DELETE /api/v1/disciplinas/{id}` — Remove disciplina e recalcula os indicadores globais (`204 No Content`).

### Importação em Lote via Spring Batch (`BatchImportController`)
- `POST /api/v1/importacoes/boletim/{alunoId}` — Recebe arquivo CSV (`multipart/form-data`), executa o Job do **Spring Batch** e retorna o resumo de processamento junto ao dashboard atualizado (`200 OK`).

---

## 6. Validações de Segurança e Tratamento de Erros

- **Validação de Payloads (`Bean Validation`):** Restrições declarativas em `AlunoDTO` e `DisciplinaDesempenhoDTO` garantindo notas entre `0.0` e `10.0`, frequência entre `0.0%` e `100.0%`, carga horária positiva e limites de caracteres.
- **Invariantes de Domínio:** Verificação de consistência cruzada impedindo que `quantidadeFaltas` ultrapasse `totalAulasSemestre` ou que uma mesma disciplina seja duplicada no mesmo semestre letivo.
- **Proteção no Upload CSV:** Limite de requisição configurado em `5MB`, validação de extensão/MIME type e sanitização contra *CSV Injection / Formula Injection*.
- **Respostas de Erro Padronizadas (`GlobalExceptionHandler`):** Estrutura uniforme via `ErrorResponseDTO` para códigos HTTP `400`, `404`, `413`, `422` e `500`, sem exposição de *stack traces* internas (`server.error.include-stacktrace=never`).

---

## 7. Pré-Requisitos e Execução Local

### Pré-Requisitos
1. **Java JDK 25** instalado e configurado no `PATH` (`java -version`).
2. **MongoDB** em execução local (`mongodb://localhost:27017`) ou instância configurada no **MongoDB Atlas**.
   - *Nota:* Não é necessário criar o banco `retention_dashboard_db` ou as coleções manualmente; o Spring Data MongoDB realiza a criação do banco, das coleções e dos índices automaticamente.

### Variáveis de Ambiente (Opcional)
O arquivo `src/main/resources/application.properties` possui valores padrão para desenvolvimento local e aceita sobrescrita via variáveis de ambiente:

| Variável | Valor Padrão | Descrição |
| :--- | :--- | :--- |
| `MONGODB_URI` | `mongodb://localhost:27017/retention_dashboard_db` | String de conexão do MongoDB local ou Atlas. |
| `PYTHON_SERVICE_URL` | `http://localhost:8000/api/v1/predict` | Endpoint do serviço Python de análise preditiva. |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:3000,http://localhost:5173,http://127.0.0.1:5500` | Origens permitidas para requisições do Frontend. |

### Comandos de Compilação e Execução

No Windows (PowerShell / CMD):
```powershell
# Compilar o projeto
.\gradlew.bat compileJava

# Executar os testes automatizados
.\gradlew.bat test

# Iniciar a aplicacao Spring Boot (porta 8080)
.\gradlew.bat bootRun
```

No Linux / macOS:
```bash
# Compilar o projeto
./gradlew compileJava

# Executar os testes automatizados
./gradlew test

# Iniciar a aplicacao Spring Boot (porta 8080)
./gradlew bootRun
```

---

## 8. Fluxo de Branches e Especificação da Sprint

- **`main`:** Branch estável contendo as entregas homologadas.
- **`develop`:** Branch de integração contínua da dupla durante a Sprint.
- O detalhamento técnico completo das tarefas e épicos encontra-se no arquivo `task.md`.

