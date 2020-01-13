package com.color.wordreader.Models;

import java.util.ArrayList;
import java.util.List;

public class Book {
    private Integer mPos;
    private String mBookName;
    private String mBookUrl;
    private List<Word> mBookWords = new ArrayList<>();

    public Book(){}

    public Book(Integer pos, List<Word> allWords){
        this.mPos = pos;
        this.mBookWords = allWords;
    }

    public Integer getPos() {
        return mPos;
    }

    public void setPos(Integer mPos) {
        this.mPos = mPos;
    }



    public List<Word> getSentenceWords() {
        return mBookWords;
    }

    public void setSentenceWords(List<Word> mSentenceWords) {
        this.mBookWords = mSentenceWords;
    }

    public String getBookName() {
        return mBookName;
    }

    public void setBookName(String mBookName) {
        this.mBookName = mBookName;
    }

    public String getBookUrl() {
        return mBookUrl;
    }

    public void setBookUrl(String mBookUrl) {
        this.mBookUrl = mBookUrl;
    }
}
