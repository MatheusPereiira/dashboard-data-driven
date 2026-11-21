package com.academic.dashboard.controller;

import com.academic.dashboard.dto.AlunoDTO;
import com.academic.dashboard.dto.DashboardPerformanceDTO;
import com.academic.dashboard.exception.BusinessValidationException;
import com.academic.dashboard.exception.GlobalExceptionHandler;
import com.academic.dashboard.exception.ResourceNotFoundException;
import com.academic.dashboard.model.StatusSituacao;
import com.academic.dashboard.service.AlunoService;
import com.academic.dashboard.service.DesempenhoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ControllersTest {

    private static final String ID = "6740f1c2a1b2c3d4e5f60789";

    private AlunoService alunoService;
    private DesempenhoService desempenhoService;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        alunoService = mock(AlunoService.class);
        desempenhoService = mock(DesempenhoService.class);

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mvc = MockMvcBuilders
                .standaloneSetup(new AlunoController(alunoService), new DesempenhoController(desempenhoService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void criarAlunoRetorna201ComLocation() throws Exception {
        AlunoDTO salvo = new AlunoDTO(ID, "Ana", "2024001", "Eng. Software", 4, 60.0, 80.0);
        when(alunoService.criar(any())).thenReturn(salvo);

        mvc.perform(post("/api/v1/alunos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Ana","matricula":"2024001","curso":"Eng. Software",
                                 "semestreAtual":4,"mediaMinimaAprovacao":60.0}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/v1/alunos/" + ID))
                .andExpect(jsonPath("$.id").value(ID));
    }

    @Test
    void payloadInvalidoRetorna400ComErrosPorCampo() throws Exception {
        mvc.perform(post("/api/v1/alunos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"\",\"mediaMinimaAprovacao\":105}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.nome").exists())
                .andExpect(jsonPath("$.fieldErrors.mediaMinimaAprovacao").exists());

        verify(alunoService, never()).criar(any());
    }

    @Test
    void identificadorComOperadorNoSqlRetorna422SemConsultarBanco() throws Exception {
        mvc.perform(get("/api/v1/alunos/{id}", "$ne"))
                .andExpect(status().isUnprocessableEntity());

        verify(alunoService, never()).buscarPorId(any());
    }

    @Test
    void alunoInexistenteRetorna404() throws Exception {
        when(alunoService.obterDashboard(ID)).thenThrow(new ResourceNotFoundException("Aluno", ID));

        mvc.perform(get("/api/v1/alunos/{id}/dashboard", ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.path").value("/api/v1/alunos/" + ID + "/dashboard"));
    }

    @Test
    void excluirAlunoRetorna204() throws Exception {
        mvc.perform(delete("/api/v1/alunos/{id}", ID))
                .andExpect(status().isNoContent());

        verify(alunoService).excluir(ID);
    }

    @Test
    void registrarDisciplinaRetorna201ComDashboard() throws Exception {
        when(desempenhoService.registrar(any())).thenReturn(new DashboardPerformanceDTO(null, List.of(), null, 1, 0));

        mvc.perform(post("/api/v1/disciplinas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"alunoId":"%s","nomeDisciplina":"Banco de Dados","semestreLetivo":"2026.2",
                                 "totalAulasSemestre":60,"quantidadeFaltas":12,"nota1":5.0}
                                """.formatted(ID)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalDisciplinasMatriculadas").value(1));
    }

    @Test
    void regraDeNegocioVioladaRetorna422() throws Exception {
        when(desempenhoService.registrar(any()))
                .thenThrow(new BusinessValidationException("A quantidade de faltas nao pode ser superior ao total de aulas."));

        mvc.perform(post("/api/v1/disciplinas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"alunoId":"%s","nomeDisciplina":"Calculo I","semestreLetivo":"2026.2",
                                 "totalAulasSemestre":10,"quantidadeFaltas":10,"nota1":5.0}
                                """.formatted(ID)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void listarComFiltroDeStatus() throws Exception {
        when(desempenhoService.listarPorAluno(eq(ID), isNull(), eq(StatusSituacao.ATENCAO_FALTA)))
                .thenReturn(List.of());

        mvc.perform(get("/api/v1/disciplinas/aluno/{id}", ID).param("statusSituacao", "ATENCAO_FALTA"))
                .andExpect(status().isOk());
    }

    @Test
    void statusDesconhecidoNoFiltroRetorna400() throws Exception {
        mvc.perform(get("/api/v1/disciplinas/aluno/{id}", ID).param("statusSituacao", "QUALQUER"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Valor invalido para o parametro 'statusSituacao'."));
    }
}
