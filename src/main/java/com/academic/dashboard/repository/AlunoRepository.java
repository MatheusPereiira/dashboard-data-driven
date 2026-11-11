package com.academic.dashboard.repository;

import com.academic.dashboard.model.Aluno;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AlunoRepository extends MongoRepository<Aluno, String> {

    Optional<Aluno> findByMatricula(String matricula);

    boolean existsByMatricula(String matricula);
}

