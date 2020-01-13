package com.color.wordreader.Models;


public class Word {
    private Integer mPos;
    private String mWord;

    public Word(){}

    public Word(String word, Integer pos){
        this.mPos = pos;
        this.mWord = word;
    }


    public Integer getPos() {
        return mPos;
    }

    public void setPos(Integer mPos) {
        this.mPos = mPos;
    }


    public String getWord() {
        return mWord;
    }

    public void setWord(String mWord) {
        this.mWord = mWord;
    }


}
