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
@Table(name = "sub_topic")
public class SubTopic {

    @Id
    private long id;

    @NotNull
    private long parentId;

    @NotNull
    private String name;

    private SubTopic() {
    }

    public SubTopic(long id, long parentId, String name) {
        this.id = id;
        this.parentId = parentId;
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getParentId() {
        return parentId;
    }

    public void setParentId(long parentId) {
        this.parentId = parentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
