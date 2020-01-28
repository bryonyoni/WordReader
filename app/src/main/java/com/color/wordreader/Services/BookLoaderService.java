package com.color.wordreader.Services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.color.wordreader.Activities.MainActivity;
import com.color.wordreader.Constants;
import com.color.wordreader.Models.Book;
import com.color.wordreader.Models.Word;
import com.color.wordreader.R;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BookLoaderService extends Service {
    private static final int NOTIF_ID = 1;
    private static final String NOTIF_CHANNEL_ID = "Channel_Id";
    private final String TAG = BookLoaderService.class.getSimpleName();
    private Book mBookToLoad;
    private Context mContext;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("BookLoaderService", "Service Started!");

        mContext = this.getApplicationContext();

        int intentBook = intent.getExtras().getInt(Constants.BOOK_ID);
        mBookToLoad = new DatabaseManager(this).loadSpecificBook(intentBook);

        startForegroundNotification();
        startLoadingAllThePages();

        return super.onStartCommand(intent, flags, startId);
    }

    private void startForegroundNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        startForeground(NOTIF_ID, new NotificationCompat.Builder(this,
                NOTIF_CHANNEL_ID) // don't forget create a notification channel first
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_stat_loading_book)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("Loading "+ mBookToLoad.getBookName())
                .setContentIntent(pendingIntent)
                .build());
    }

    private void startLoadingAllThePages(){
        int lastPrintedPage = mBookToLoad.getLastPrintedPage();
        String pdfUrl = mBookToLoad.getBookPath();
        Log.e(TAG,"Last Printed page: "+lastPrintedPage);
        Log.e(TAG,"Total pages to print: "+mBookToLoad.getNumberOfPagesToLoad());
        Log.e(TAG, "Book url: "+pdfUrl);

        try {
            PdfReader reader = new PdfReader(pdfUrl);
            int n = reader.getNumberOfPages();

            for(int i = mBookToLoad.getLastPrintedPage(); i <= mBookToLoad.getNumberOfPagesToLoad(); i++){
                String parsedText = loadSpecificPage(reader,i);
                mBookToLoad.setLastPrintedPage(i);

                int lastLoadedWordPos = mBookToLoad.getSentenceWords().size()-1;
                List<Word> allTheWordsForLoadedBook = loadAllWordsAndSentences(parsedText);

                if(!allTheWordsForLoadedBook.isEmpty()) {
                    mBookToLoad.addSentencewords(allTheWordsForLoadedBook);
                    new DatabaseManager(mContext).storeBooksNewlyLoadedWords(mBookToLoad, lastLoadedWordPos);
                }
            }
            reader.close();
            finishEverything(true);

        } catch (IOException e) {
            e.printStackTrace();
            finishEverything(false);
        }
    }

    private void finishEverything(boolean isEverythingSuccessful){
        if(isEverythingSuccessful){
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(Constants.FINISHED_LOADING_BOOK));
            new DatabaseManager(mContext).setLoadingBook(mBookToLoad.getStoragePos(),false);

            NotificationManager nMgr = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
            if (nMgr != null) {
                nMgr.cancel(NOTIF_ID);
            }

        }else{
            NotificationManager nMgr = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
            if (nMgr != null) {
                nMgr.cancel(NOTIF_ID);
            }

            Intent notificationIntent = new Intent(this, MainActivity.class);

            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                    notificationIntent, 0);

            startForeground(NOTIF_ID, new NotificationCompat.Builder(this, NOTIF_CHANNEL_ID) // don't forget create a notification channel first
                    .setOngoing(false)
                    .setSmallIcon(R.drawable.ic_stat_loading_book)
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText("Failed to Load "+ mBookToLoad.getBookName())
                    .setContentIntent(pendingIntent)
                    .build());
        }
    }

    private String loadSpecificPage(PdfReader reader, int pageNumber) throws IOException {
        String newParsedText   = new StringBuilder()
                .append(PdfTextExtractor.getTextFromPage(reader, pageNumber).trim())
                .append("\n")
                .toString(); //Extracting the content from the different pages

        return newParsedText;
    }

    private List<Word> loadAllWordsAndSentences(String everything){
        List<Word> allTheWordsForLoadedBook = new ArrayList<>();

        String currentWord = "";
        for (int i = 0; i < everything.length(); i++){
            char c = everything.charAt(i);

            if(c==' '){
                //the word is over,
                if(!currentWord.equals("")) {

                    Word word = new Word();
                    word.setWord(currentWord.trim());
                    allTheWordsForLoadedBook.add(word);
                }
                currentWord = "";
            }else{
                currentWord = String.format("%s%s", currentWord, Character.toString(c));
            }
        }

        Log.e("BookLoaderService", "We've got: "+ allTheWordsForLoadedBook.size());
        return allTheWordsForLoadedBook;

    }

}
