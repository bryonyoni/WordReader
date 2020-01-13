package com.color.wordreader.Services;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

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
    private final String BOOK_COVER = "BOOK_COVER";
    private final String CURRENT_BOOK_WORD = "CURRENT_BOOK_WORD";
    private final String BOOK_SIZE = "BOOK_SIZE";
    private final String BOOK_POSITION = "BOOK_POSITION";

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
                loadedBook.setCurrentWordId(currentWord);
                loadedBook.setStoragePos(position);
                loadedBook.setSentenceWords(bookWords);

                allMyBooks.add(loadedBook);

            }
        }

        return allMyBooks;
    }

    public void updateBookProgress(Book book){
        SharedPreferences pref = mContext.getSharedPreferences(ALL_BOOKS, MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putLong(book.getStoragePos()+CURRENT_BOOK_WORD, book.getCurrentWordId());
        editor.apply();

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
}
