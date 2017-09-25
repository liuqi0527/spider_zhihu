package com.example.spider.domain;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

/**
 * @author LiuQi
 * @version 1.0 Create on  2017/7/25
 */

@Entity
@Table(name = "main_topic")
public class MainTopic {

    @Id
    private long id;

    @NotNull
    private String name;

    private MainTopic() {
    }

    public MainTopic(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
