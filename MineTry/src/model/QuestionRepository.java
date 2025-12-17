package model;


import java.util.List;
import java.util.Optional;

import enums.QuestionLevel;


public interface QuestionRepository {
List<Question> findAll();
List<Question> findByLevel(QuestionLevel level);
Optional<Question> randomByLevel(QuestionLevel level);
}