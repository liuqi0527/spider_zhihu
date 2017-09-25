package com.example.spider.dao;

import com.example.spider.domain.SubTopic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author LiuQi
 * @version 1.0 Create on  2017/7/25
 */

@Repository
public interface SubTopicRepository extends JpaRepository<SubTopic, Long> {

    @Override
    List<SubTopic> findAll();

    List<SubTopic> findByParentId(long parentId);

    SubTopic findById(long id);

    @Override
    void deleteAllInBatch();

    @Override
    <S extends SubTopic> List<S> save(Iterable<S> entities);
}
