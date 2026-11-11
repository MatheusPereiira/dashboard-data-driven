package com.academic.dashboard.mapper;

import com.academic.dashboard.dto.AnalisePreditivaDTO;
import com.academic.dashboard.model.AnalisePreditiva;

public final class AnaliseMapper {

    private AnaliseMapper() {
    }

    public static AnalisePreditivaDTO toDTO(AnalisePreditiva entity) {
        if (entity == null) {
            return null;
        }
        return new AnalisePreditivaDTO(
                entity.getId(),
                entity.getAlunoId(),
                entity.getDataAtualizacao(),
                entity.getIndiceRendimentoAcademico(),
                entity.getFrequenciaGlobal(),
                entity.getScoreRiscoEvasaoReprovacao(),
                entity.getClassificacaoRisco(),
                entity.getAlertasPreditivos()
        );
    }

    public static AnalisePreditiva toEntity(AnalisePreditivaDTO dto) {
        if (dto == null) {
            return null;
        }
        return new AnalisePreditiva(
                dto.getId(),
                dto.getAlunoId(),
                dto.getDataAtualizacao(),
                dto.getIndiceRendimentoAcademico(),
                dto.getFrequenciaGlobal(),
                dto.getScoreRiscoEvasaoReprovacao(),
                dto.getClassificacaoRisco(),
                dto.getAlertasPreditivos()
        );
    }
}

