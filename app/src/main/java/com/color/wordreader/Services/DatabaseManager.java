package com.color.wordreader.Services;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import com.color.wordreader.Constants;
import com.color.wordreader.Models.Book;
import com.color.wordreader.Models.Word;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class DatabaseManager {
    private Context mContext;
    private final String ALL_BOOKS = "ALL_BOOKS";
    private final String BOOK_WORDS = "BOOK_WORDS";
    private final String BOOK_TITLE = "BOOK_TITLE";
    private final String BOOK_URL = "BOOK_URL";
    private final String READING_SPEED = "READING_SPEED";
    private final String BOOK_COVER = "BOOK_COVER";
    private final String BOOK_PATH = "BOOK_PATH";
    private final String CURRENT_BOOK_WORD = "CURRENT_BOOK_WORD";
    private final String BOOK_SIZE = "BOOK_SIZE";
    private final String BOOK_POSITION = "BOOK_POSITION";
    private final String TRANS_POS = "TRANS_POS";

    public DatabaseManager(Context context){
        this.mContext = context;
    }

    public List<Book> loadAllStoredBooks(){
        List<Book> allMyBooks = new ArrayList<>();
        SharedPreferences pref = mContext.getSharedPreferences(ALL_BOOKS, MODE_PRIVATE);
        int numberOfBooksStored = pref.getInt(ALL_BOOKS, -1);

        if(numberOfBooksStored!=-1){
            for(int i=0; i<=numberOfBooksStored; i++){
                String title = pref.getString(i+BOOK_TITLE,"");
                String url = pref.getString(i+BOOK_URL,"");
                Bitmap cover = decodeBitmapFromStorage(pref.getString(i+BOOK_COVER,""));
                Long currentWord = pref.getLong(i+CURRENT_BOOK_WORD,0);
                Long size = pref.getLong(i+BOOK_SIZE,0);
                String path = pref.getString(i+BOOK_PATH,"");
                int position = pref.getInt(i+BOOK_POSITION,i);
                List<Word> bookWords = new ArrayList<>();

                for(int w = 0; w <=size; w++){
                    String wordString = pref.getString(i+BOOK_WORDS+w,"");
                    if(!wordString.equals("")){
                        Word word = new Word();
                        word.setWord(wordString);
                        bookWords.add(word);
                    }
                }

                Book loadedBook = new Book();
                loadedBook.setBookCover(cover);
                loadedBook.setBookName(title);
                loadedBook.setBookUrl(url);
                loadedBook.setBookPath(path);
                loadedBook.setCurrentWordId(currentWord);
                loadedBook.setStoragePos(position);
                loadedBook.setSentenceWords(bookWords);

                allMyBooks.add(loadedBook);

            }
        }

        return allMyBooks;
    }

    public Book loadSpecificBook(int i){
        SharedPreferences pref = mContext.getSharedPreferences(ALL_BOOKS, MODE_PRIVATE);

        String title = pref.getString(i+BOOK_TITLE,"");
        String url = pref.getString(i+BOOK_URL,"");
        Bitmap cover = decodeBitmapFromStorage(pref.getString(i+BOOK_COVER,""));
        Long currentWord = pref.getLong(i+CURRENT_BOOK_WORD,0);
        Long size = pref.getLong(i+BOOK_SIZE,0);
        String path = pref.getString(i+BOOK_PATH,"");
        int position = pref.getInt(i+BOOK_POSITION,i);
        List<Word> bookWords = new ArrayList<>();

        for(int w = 0; w <=size; w++){
            String wordString = pref.getString(i+BOOK_WORDS+w,"");
            if(!wordString.equals("")){
                Word word = new Word();
                word.setWord(wordString);
                bookWords.add(word);
            }
        }

        Book loadedBook = new Book();
        loadedBook.setBookCover(cover);
        loadedBook.setBookName(title);
        loadedBook.setBookUrl(url);
        loadedBook.setBookPath(path);
        loadedBook.setCurrentWordId(currentWord);
        loadedBook.setStoragePos(position);
        loadedBook.setSentenceWords(bookWords);

        return loadedBook;
    }

    public void updateBookProgress(Book book){
        SharedPreferences pref = mContext.getSharedPreferences(ALL_BOOKS, MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putLong(book.getStoragePos()+CURRENT_BOOK_WORD, book.getCurrentWordId());
        editor.apply();

    }

    public void setReadingSpeed(int speed, int transPos){
        SharedPreferences pref = mContext.getSharedPreferences(READING_SPEED, MODE_PRIVATE);
        pref.edit().putInt(READING_SPEED,speed).apply();
        pref.edit().putInt(TRANS_POS, transPos).apply();
    }

    public int getReadingSpeed(){
        SharedPreferences pref = mContext.getSharedPreferences(READING_SPEED, MODE_PRIVATE);
        int time = pref.getInt(READING_SPEED, Constants.MIN_READING_TIME);
        if(time < Constants.MIN_READING_TIME || time > Constants.MAX_READING_TIME){
            return Constants.MIN_READING_TIME;
        }
        return pref.getInt(READING_SPEED, Constants.MIN_READING_TIME);
    }

    public int getTransPos(){
        SharedPreferences pref = mContext.getSharedPreferences(READING_SPEED, MODE_PRIVATE);
       int transPos = pref.getInt(TRANS_POS,dpToPx(300));
       if(transPos < 0 || transPos > dpToPx(300)){
           return dpToPx(300);
       }
        return pref.getInt(TRANS_POS, dpToPx(300));
    }

    public void storeNewBook(Book book){
        SharedPreferences pref = mContext.getSharedPreferences(ALL_BOOKS, MODE_PRIVATE);
        int numberOfBooksStored = pref.getInt(ALL_BOOKS, -1);
        int newNumberOfBooksStored;

        if(numberOfBooksStored == -1) newNumberOfBooksStored = 0;
        else newNumberOfBooksStored = numberOfBooksStored+1;

        SharedPreferences.Editor editor = pref.edit();

        editor.putString(newNumberOfBooksStored+BOOK_TITLE, book.getBookName());
        editor.putString(newNumberOfBooksStored+BOOK_URL, book.getBookUrl());
        editor.putString(newNumberOfBooksStored+BOOK_COVER, encodeBitmapForStorage(book.getBookCover()));
        editor.putLong(newNumberOfBooksStored+CURRENT_BOOK_WORD, book.getCurrentWordId());
        editor.putLong(newNumberOfBooksStored+BOOK_SIZE, book.getSentenceWords().size());
        editor.putString(newNumberOfBooksStored+BOOK_PATH,book.getBookPath());
        editor.putInt(newNumberOfBooksStored+BOOK_POSITION, newNumberOfBooksStored);

        for(Word word: book.getSentenceWords()){
            editor.putString(newNumberOfBooksStored+BOOK_WORDS+book.getSentenceWords().indexOf(word), word.getWord());
        }

        editor.putInt(ALL_BOOKS, newNumberOfBooksStored);

        editor.apply();

    }

    private String encodeBitmapForStorage(Bitmap bitmap){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos);
        return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
    }

    private Bitmap decodeBitmapFromStorage(String image){
        byte[] decodedByteArray = android.util.Base64.decode(image, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.length);
    }


    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }
}
