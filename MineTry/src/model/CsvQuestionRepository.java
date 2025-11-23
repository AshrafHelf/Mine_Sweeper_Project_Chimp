package model;



import model.*;

import java.util.*;
import java.util.stream.Collectors;


public class CsvQuestionRepository implements QuestionRepository {
private final String path;
private final List<Question> cache = new ArrayList<>();


public CsvQuestionRepository(String path) {
this.path = path;
this.cache.addAll(CsvUtils.readQuestions(path));
}


@Override public List<Question> findAll() { return List.copyOf(cache); }
@Override public List<Question> findByLevel(QuestionLevel level) {
return cache.stream().filter(q->q.getLevel()==level).collect(Collectors.toList());
}
@Override public Optional<Question> randomByLevel(QuestionLevel level) {
var list = findByLevel(level);
if (list.isEmpty()) return Optional.empty();
return Optional.of(list.get(new Random().nextInt(list.size())));
}
}