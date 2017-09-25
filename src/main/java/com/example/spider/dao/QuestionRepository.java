package com.example.spider.dao;

import com.example.spider.domain.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

/**
 * @author LiuQi
 * @version 1.0 Create on  2017/7/27
 */
@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    List<Question> findByParentId(long parentId);

    @Override
    <S extends Question> List<S> save(Iterable<S> entities);

//    <S extends Question> List<S> saveOrUpdate(Iterable<S> entities);

//    Question saveOrUpdate(Question question);

    @Override
    void deleteAllInBatch();

    void deleteAllById(Collection<Long> ids);

    void deleteByParentId(long parentId);
}
