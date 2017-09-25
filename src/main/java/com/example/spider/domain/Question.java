package com.example.spider.domain;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * @author LiuQi
 * @version 1.0 Create on  2017/7/27
 */
@Entity
@Table(name = "question")
public class Question {

    @Id
    private long id;

    @Column
    private long parentId;

    @Lob
    private String title;

    @Column
    private String author;

    @Lob
    private String description;

    @Column
    private int commentCount;

    @Column
    private int answerCount;

    @Column
    private int focusCount;

    @Column
    private int viewCount;

    @Column
    @ElementCollection(targetClass = String.class)
    @CollectionTable(name = "tags")
    private Set<String> tags = new HashSet<>();

    private Question() {
    }

    public Question(long id, long parentId) {
        this.id = id;
        this.parentId = parentId;
    }

    public void addTag(String tag) {
        this.tags.add(tag);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    public long getParentId() {
        return parentId;
    }

    public void setParentId(long parentId) {
        this.parentId = parentId;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    public int getAnswerCount() {
        return answerCount;
    }

    public void setAnswerCount(int answerCount) {
        this.answerCount = answerCount;
    }

    public int getFocusCount() {
        return focusCount;
    }

    public void setFocusCount(int focusCount) {
        this.focusCount = focusCount;
    }

    public int getViewCount() {
        return viewCount;
    }

    public void setViewCount(int viewCount) {
        this.viewCount = viewCount;
    }
}
