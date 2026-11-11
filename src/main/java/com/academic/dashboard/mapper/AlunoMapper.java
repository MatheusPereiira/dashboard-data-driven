package com.academic.dashboard.mapper;

import com.academic.dashboard.dto.AlunoDTO;
import com.academic.dashboard.model.Aluno;

public final class AlunoMapper {

    private AlunoMapper() {
    }

    public static AlunoDTO toDTO(Aluno entity) {
        if (entity == null) {
            return null;
        }
        return new AlunoDTO(
                entity.getId(),
                entity.getNome(),
                entity.getMatricula(),
                entity.getCurso(),
                entity.getSemestreAtual(),
                entity.getMediaMinimaAprovacao(),
                entity.getFrequenciaMinimaExigida()
        );
    }

    public static Aluno toEntity(AlunoDTO dto) {
        if (dto == null) {
            return null;
        }
        Double frequenciaMinima = dto.getFrequenciaMinimaExigida() != null
                ? dto.getFrequenciaMinimaExigida()
                : 75.0;
        return new Aluno(
                dto.getId(),
                dto.getNome() != null ? dto.getNome().trim() : null,
                dto.getMatricula() != null ? dto.getMatricula().trim() : null,
                dto.getCurso() != null ? dto.getCurso().trim() : null,
                dto.getSemestreAtual(),
                dto.getMediaMinimaAprovacao(),
                frequenciaMinima
        );
    }
}

