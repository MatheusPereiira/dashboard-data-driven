package com.academic.dashboard.validation;

import com.academic.dashboard.dto.AlunoDTO;
import com.academic.dashboard.dto.DisciplinaDesempenhoDTO;
import com.academic.dashboard.exception.BusinessValidationException;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;
import java.util.regex.Pattern;

public final class InputSecurityValidator {

    private static final long MAX_CSV_BYTES = 5L * 1024L * 1024L;
    private static final Pattern SAFE_ID_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]{1,64}$");
    private static final Pattern NOSQL_OPERATOR_PATTERN = Pattern.compile(".*[${}\\[\\]].*");
    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<[^>]*>");

    private static final Set<String> ALLOWED_CSV_CONTENT_TYPES = Set.of(
            "text/csv",
            "application/csv",
            "application/vnd.ms-excel",
            "text/plain",
            "application/octet-stream"
    );

    private InputSecurityValidator() {
    }

    public static void validarIdentificador(String id, String rotulo) {
        if (id == null || id.isBlank()) {
            throw new BusinessValidationException("O " + rotulo + " e obrigatorio.");
        }
        String trimmed = id.trim();
        if (NOSQL_OPERATOR_PATTERN.matcher(trimmed).matches() || !SAFE_ID_PATTERN.matcher(trimmed).matches()) {
            throw new BusinessValidationException("Formato invalido para " + rotulo + ".");
        }
    }

    public static void sanitizarAlunoDTO(AlunoDTO dto) {
        if (dto == null) {
            throw new BusinessValidationException("Os dados do aluno sao obrigatorios.");
        }
        dto.setNome(sanitizarTexto(dto.getNome(), "nome"));
        dto.setMatricula(sanitizarTexto(dto.getMatricula(), "matricula"));
        dto.setCurso(sanitizarTexto(dto.getCurso(), "curso"));
    }

    public static void sanitizarDisciplinaDTO(DisciplinaDesempenhoDTO dto) {
        if (dto == null) {
            throw new BusinessValidationException("Os dados da disciplina sao obrigatorios.");
        }
        if (dto.getAlunoId() != null) {
            validarIdentificador(dto.getAlunoId(), "identificador do aluno");
            dto.setAlunoId(dto.getAlunoId().trim());
        }
        dto.setNomeDisciplina(sanitizarTexto(dto.getNomeDisciplina(), "nome da disciplina"));
        dto.setSemestreLetivo(sanitizarTexto(dto.getSemestreLetivo(), "semestre letivo"));
    }

    public static String sanitizarParametroFiltro(String valor, String rotulo) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return sanitizarTexto(valor, rotulo);
    }

    public static void validarArquivoCsv(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessValidationException("O arquivo CSV enviado esta vazio ou ausente.");
        }
        if (file.getSize() > MAX_CSV_BYTES) {
            throw new BusinessValidationException("O arquivo CSV excede o tamanho maximo permitido de 5MB.");
        }
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".csv")) {
            throw new BusinessValidationException("Extensao de arquivo invalida. Envie apenas arquivos .csv.");
        }
        String contentType = file.getContentType();
        if (contentType != null && !contentType.isBlank()) {
            String baseContentType = contentType.split(";")[0].trim().toLowerCase();
            if (!ALLOWED_CSV_CONTENT_TYPES.contains(baseContentType)) {
                throw new BusinessValidationException("Content-Type nao suportado para importacao CSV: " + baseContentType);
            }
        }
    }

    public static String sanitizarTexto(String valor, String rotulo) {
        if (valor == null) {
            return null;
        }
        String semTagsHtml = HTML_TAG_PATTERN.matcher(valor).replaceAll("");
        String limpo = semTagsHtml.trim().replaceAll("\\s+", " ");
        if (NOSQL_OPERATOR_PATTERN.matcher(limpo).matches()) {
            throw new BusinessValidationException("O campo '" + rotulo + "' contem caracteres nao permitidos.");
        }
        return limpo;
    }
}
