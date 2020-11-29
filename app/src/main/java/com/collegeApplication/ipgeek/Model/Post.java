package com.collegeApplication.ipgeek.Model;

public class Post {
    private String postid, question, publisher, topic, askedby, date, questionimage;

    public Post() {
    }

    public Post(String postid, String question, String publisher, String topic, String askedby, String date, String questionimage) {
        this.postid = postid;
        this.question = question;
        this.publisher = publisher;
        this.topic = topic;
        this.askedby = askedby;
        this.date = date;
        this.questionimage = questionimage;
    }

    public String getPostid() {
        return postid;
    }

    public void setPostid(String postid) {
        this.postid = postid;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getAskedby() {
        return askedby;
    }

    public void setAskedby(String askedby) {
        this.askedby = askedby;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getQuestionimage() {
        return questionimage;
    }

    public void setQuestionimage(String questionimage) {
        this.questionimage = questionimage;
    }
}
