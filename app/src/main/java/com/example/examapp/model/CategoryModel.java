package com.example.examapp.model;

public class CategoryModel {
    private String docId;
    private String name;
    private int noOfTests;

    public CategoryModel() {
        // Constructor mặc định cần thiết cho Firebase
    }

    public CategoryModel(String docId, String name, int noOfTests) {
        this.docId = docId;
        this.name = name;
        this.noOfTests = noOfTests;
    }

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNoOfTests() {
        return noOfTests;
    }

    public void setNoOfTests(int noOfTests) {
        this.noOfTests = noOfTests;
    }
}
