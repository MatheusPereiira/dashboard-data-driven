package com.academic.dashboard.mapper;

import com.academic.dashboard.dto.DisciplinaDesempenhoDTO;
import com.academic.dashboard.model.DisciplinaDesempenho;

import java.util.Collections;
import java.util.List;

public final class DisciplinaMapper {

    private DisciplinaMapper() {
    }

    public static DisciplinaDesempenhoDTO toDTO(DisciplinaDesempenho entity) {
        if (entity == null) {
            return null;
        }
        return new DisciplinaDesempenhoDTO(
                entity.getId(),
                entity.getAlunoId(),
                entity.getNomeDisciplina(),
                entity.getSemestreLetivo(),
                entity.getTotalAulasSemestre(),
                entity.getQuantidadeFaltas(),
                entity.getPercentualFrequencia(),
                entity.getNota1(),
                entity.getNota2(),
                entity.getMediaAtual(),
                entity.getNotaNecessariaParaPassar(),
                entity.getFaltasRestantesPermitidas(),
                entity.getStatusSituacao()
        );
    }

    public static DisciplinaDesempenho toEntity(DisciplinaDesempenhoDTO dto) {
        if (dto == null) {
            return null;
        }
        return new DisciplinaDesempenho(
                dto.getId(),
                dto.getAlunoId() != null ? dto.getAlunoId().trim() : null,
                dto.getNomeDisciplina() != null ? dto.getNomeDisciplina().trim() : null,
                dto.getSemestreLetivo() != null ? dto.getSemestreLetivo().trim() : null,
                dto.getTotalAulasSemestre(),
                dto.getQuantidadeFaltas(),
                dto.getPercentualFrequencia(),
                dto.getNota1(),
                dto.getNota2(),
                dto.getMediaAtual(),
                dto.getNotaNecessariaParaPassar(),
                dto.getFaltasRestantesPermitidas(),
                dto.getStatusSituacao()
        );
    }

    public static List<DisciplinaDesempenhoDTO> toDTOList(List<DisciplinaDesempenho> entities) {
        if (entities == null || entities.isEmpty()) {
            return Collections.emptyList();
        }
        return entities.stream()
                .map(DisciplinaMapper::toDTO)
                .toList();
    }
}

