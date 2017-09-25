package com.example.spider.dao;

import com.example.spider.domain.MainTopic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author LiuQi
 * @version 1.0 Create on  2017/7/25
 */

@Repository
public interface MainTopicRepository extends JpaRepository<MainTopic, Long> {

    @Override
    List<MainTopic> findAll();

    @Override
    MainTopic findOne(Long id);

    @Override
    void deleteAllInBatch();

    @Override
    <S extends MainTopic> List<S> save(Iterable<S> entities);
}
