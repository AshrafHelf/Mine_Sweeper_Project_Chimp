package model;

import java.util.List;
import java.util.Optional;

public interface QuestionRepository {
    List<Question> findAll();
    List<Question> findByLevel(QuestionLevel level);
    Optional<Question> randomByLevel(QuestionLevel level);

    void add(Question q);
    boolean update(Question q);          // match by q.getId()
    boolean deleteById(String id);

    void save();
    void reload();                       // re-read file into cache
}
