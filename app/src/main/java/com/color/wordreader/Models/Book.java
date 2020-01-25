package com.color.wordreader.Models;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.List;

public class Book {
    private String mBookName;
    private String mBookUrl;
    private Bitmap mBookCover;
    private int mStoragePos;
    private String mBookPath;
    private List<Word> mBookWords = new ArrayList<>();
    private Long mCurrentWordId = 0L;

    private int numberOfPagesToLoad = 0;
    private int lastPrintedPage = 0;

    public Book(){}

    public Book(List<Word> allWords){
        this.mBookWords = allWords;
    }



    public List<Word> getSentenceWords() {
        return mBookWords;
    }

    public void setSentenceWords(List<Word> mSentenceWords) {
        this.mBookWords = mSentenceWords;
    }

    public void addSentencewords(List<Word> mSentenceWords){
        this.mBookWords.addAll(mSentenceWords);
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



    public Bitmap getBookCover() {
        return mBookCover;
    }

    public void setBookCover(Bitmap mBookCover) {
        this.mBookCover = mBookCover;
    }


    public Long getCurrentWordId() {
        return mCurrentWordId;
    }

    public void setCurrentWordId(Long mCurrentWordId) {
        this.mCurrentWordId = mCurrentWordId;
    }


    public int percentageForCompletion(){
        if(mCurrentWordId!=0) {
            return (int)(((double)mCurrentWordId / (double) mBookWords.size()) * 100);
        }
        return 0;
    }

    public int getStoragePos() {
        return mStoragePos;
    }

    public void setStoragePos(int mStoragePos) {
        this.mStoragePos = mStoragePos;
    }


    public Word getCurrentWord(){
        return mBookWords.get((int)(long) mCurrentWordId);
    }

    public Word getNextWord(){
        return mBookWords.get((int)(long) mCurrentWordId+1);
    }

    public Word getNextWordAndUpdatePos(){
        mCurrentWordId+=1;
        checkForHyphens();
        if(mBookWords.size()<= mCurrentWordId){
            mCurrentWordId = 0L;
            return mBookWords.get((int)(long) 0);
        }
        return mBookWords.get((int)(long) mCurrentWordId);
    }

    private void checkForHyphens(){
        Word currWord = mBookWords.get((int)(long)mCurrentWordId);
        Word nextWord = mBookWords.get((int)(long)mCurrentWordId+1);

        if(currWord.getWord().endsWith("-")){
            //if it ends with a hyphen
            String newWord = currWord.getWord()+nextWord.getWord();
            String hyphenLessWord = newWord.replace("-","");
            mBookWords.get((int)(long)mCurrentWordId+1).setWord(hyphenLessWord);
            mCurrentWordId+=1;
        }else if(nextWord.getWord().startsWith("-")){
            String newWord = currWord.getWord()+nextWord.getWord();
            String hyphenLessWord = newWord.replace("-","");
            mBookWords.get((int)(long)mCurrentWordId+1).setWord(hyphenLessWord);
            mCurrentWordId+=1;
        }
    }

    public boolean isLastWord(){
        return (mCurrentWordId+1) == mBookWords.size();
    }

    public String getBookPath() {
        return mBookPath;
    }

    public void setBookPath(String mBookPath) {
        this.mBookPath = mBookPath;
    }

    public int getNumberOfPagesToLoad() {
        return numberOfPagesToLoad;
    }

    public void setNumberOfPagesToLoad(int numberOfPagesToLoad) {
        this.numberOfPagesToLoad = numberOfPagesToLoad;
    }

    public int getLastPrintedPage() {
        return lastPrintedPage;
    }

    public void setLastPrintedPage(int lastPrintedPage) {
        this.lastPrintedPage = lastPrintedPage;
    }

    public Word getLastWord() {
        return mBookWords.get((int)(long)mCurrentWordId-1);
    }
}
