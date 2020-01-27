package com.color.wordreader.Activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.interpolator.view.animation.FastOutLinearInInterpolator;
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.color.wordreader.Constants;
import com.color.wordreader.Models.Book;
import com.color.wordreader.Models.Word;
import com.color.wordreader.R;
import com.color.wordreader.Services.DatabaseManager;
import com.google.android.material.card.MaterialCardView;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ReaderActivity extends AppCompatActivity implements View.OnClickListener{
    private final String TAG = ReaderActivity.class.getSimpleName();
    private Context mContext;

    @Bind(R.id.currentWordTextView) TextView currentWordTextView;
    @Bind(R.id.playImageView) ImageView playImageView;

    @Bind(R.id.bookDetailsRelativeLayout) RelativeLayout bookDetailsRelativeLayout;
    @Bind(R.id.bookThumbnailImageView) ImageView bookThumbnailImageView;
    @Bind(R.id.bookTitleTextView) TextView bookTitleTextView;
    @Bind(R.id.bookProgressValueTextView) TextView bookProgressValueTextView;
    @Bind(R.id.bookProgressBigRelativeLayout) RelativeLayout bookProgressBigRelativeLayout;

    @Bind(R.id.percentageBarView) ProgressBar percentageBarView;
    @Bind(R.id.bookProgressBigProgressBar) ProgressBar bookProgressBigProgressBar;
    @Bind(R.id.pauseView) View pauseView;
    @Bind(R.id.wordsRemainingTextView) TextView wordsRemainingTextView;

    private int mAnimationDuration = 300;
    private int mAnimationDurationFast = 150;
    private int mAnimationDurationSlow = 450;
    private boolean isPlaying = false;
    private Book mViewingBook;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(new DatabaseManager(getApplicationContext()).isDarkThemeEnabled()){
            setContentView(R.layout.activity_reader_dark);
        }else{
            setContentView(R.layout.activity_reader);
        }


        ButterKnife.bind(this);
        mContext = this.getApplicationContext();

        pauseView.setOnClickListener(this);
        bookThumbnailImageView.setOnClickListener(this);

        int intent = getIntent().getExtras().getInt(Constants.BOOK_ID);
        mViewingBook = new DatabaseManager(mContext).loadSpecificBook(intent);

        time = new DatabaseManager(mContext).getReadingSpeed();
        translationPos = new DatabaseManager(mContext).getTransPos();

        Log.e(TAG, "Gotten Time: "+time);
        Log.e(TAG, "Gotten translation position: "+translationPos);
        Log.e(TAG,"Book size: "+mViewingBook.getSentenceWords().size());
        Log.e(TAG, "Current word so far: "+mViewingBook.getCurrentWordId());

        setTranslationPos();
        setBooksData();
        setTouchActivator();
        setFastForwardTouchActivator();
    }


    private void pauseWord(){
        isPlaying = false;
        bookDetailsRelativeLayout.setVisibility(View.VISIBLE);
        playImageView.setVisibility(View.VISIBLE);

        playImageView.animate().alpha(1f).translationY(0).setDuration(mAnimationDurationFast).start();

        bookProgressBigRelativeLayout.animate().alpha(0f).setDuration(mAnimationDuration).start();
        bookDetailsRelativeLayout.animate().alpha(1f).setDuration(mAnimationDuration).start();

        currentWordTextView.animate().translationY(DatabaseManager.dpToPx(-50)).scaleY(0.7f).scaleX(0.7f)
                .setInterpolator(new LinearOutSlowInInterpolator()).setDuration(mAnimationDuration)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        playImageView.setAlpha(1f);
                        playImageView.setTranslationY(0);

                        currentWordTextView.setTranslationY(DatabaseManager.dpToPx(-50));
                        currentWordTextView.setScaleX(0.7f);
                        currentWordTextView.setScaleY(0.7f);

                        bookProgressBigRelativeLayout.setAlpha(0f);
                        bookProgressBigRelativeLayout.setVisibility(View.GONE);

                        bookDetailsRelativeLayout.setAlpha(1f);

                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                }).start();

        new DatabaseManager(mContext).updateBookProgress(mViewingBook);

        wordsRemainingTextView.animate().alpha(1f).setDuration(mAnimationDuration)
                .setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                wordsRemainingTextView.setAlpha(1f);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        }).start();

        setWordsRemainingTextView();


    }

    private void playWordReader(){
        isPlaying = true;
        bookProgressBigRelativeLayout.setVisibility(View.VISIBLE);

        playImageView.animate().alpha(0f).translationY(DatabaseManager.dpToPx(20)).setDuration(mAnimationDurationFast).start();

        bookProgressBigRelativeLayout.animate().alpha(1f).setDuration(mAnimationDuration).start();
        bookDetailsRelativeLayout.animate().alpha(0f).setDuration(mAnimationDuration).start();

        currentWordTextView.animate().translationY(0).scaleY(1f).scaleX(1f).setInterpolator(new LinearOutSlowInInterpolator()).setDuration(mAnimationDuration)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        playImageView.setAlpha(0f);
                        playImageView.setTranslationY(DatabaseManager.dpToPx(20));
                        playImageView.setVisibility(View.GONE);

                        currentWordTextView.setTranslationY(0);
                        currentWordTextView.setScaleX(1f);
                        currentWordTextView.setScaleY(1f);

                        bookProgressBigRelativeLayout.setAlpha(1f);
                        bookDetailsRelativeLayout.setAlpha(0f);
                        bookDetailsRelativeLayout.setVisibility(View.GONE);

                        startReader();

                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                }).start();

        wordsRemainingTextView.animate().alpha(0f).setDuration(mAnimationDuration)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        wordsRemainingTextView.setAlpha(0f);
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                }).start();
    }

    @Override
    public void onClick(View view) {
        if(view.equals(pauseView)){
            if(!isPlaying){
                playWordReader();
                if(isBookOptionsShowing) hideBookOptions();
            }
            else pauseWord();
        }else if(view.equals(bookThumbnailImageView)){
            if(!isBookOptionsShowing){
                showBookOptions();
                if(isPlaying) pauseWord();
            }else{
                hideBookOptions();
            }
        }
    }

    private void setBooksData(){
        currentWordTextView.setText(mViewingBook.getCurrentWord().getWord());

        bookThumbnailImageView.setImageBitmap(mViewingBook.getBookCover());
        bookTitleTextView.setText(mViewingBook.getBookName());

        percentageBarView.setProgress(mViewingBook.percentageForCompletion());
        bookProgressValueTextView.setText(mViewingBook.percentageForCompletion()+"%");

        bookProgressBigProgressBar.setProgress(mViewingBook.percentageForCompletion());


        wordsRemainingTextView.animate().alpha(1f).setDuration(mAnimationDuration)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        wordsRemainingTextView.setAlpha(1f);
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                }).start();

        setWordsRemainingTextView();
    }

    private void setWordsRemainingTextView(){
        int wordsSoFar = (int)(long)mViewingBook.getCurrentWordId();
        int totalWords = mViewingBook.getSentenceWords().size();

        NumberFormat format = NumberFormat.getInstance(Locale.US);

        double wordCount = 60000/time;
        int timeTaken = (int)(totalWords/wordCount);
        int hrs = timeTaken/60;
        int min = timeTaken%60;

        if(hrs!=0){
            if(hrs==1){
                wordsRemainingTextView.setText(String.format("%s / %s words \n %d:%d", format.format(wordsSoFar), format.format(totalWords), hrs, min));

            }else wordsRemainingTextView.setText(String.format("%s / %s words \n %d:%d", format.format(wordsSoFar), format.format(totalWords), hrs, min));

        }else{
            wordsRemainingTextView.setText(String.format("%s / %s words \n %d min", format.format(wordsSoFar), format.format(totalWords), min));
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if(!hasFocus){
            pauseWord();
        }
    }



    wordTimerBackgroundTask w;
    private void startReader(){
//        if(w!=null) w.cancel(false);
        w = new wordTimerBackgroundTask();
        w.execute();
    }

    private void restartReader(){
        if(isPlaying) {
            isPlaying = false;

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    isPlaying = true;
                    startReader();
                }
            }, 700);
        }
    }

    private int time = 500;

    private class wordTimerBackgroundTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... strings) {
            while (isPlaying) {
                try {
                    String currentWord = mViewingBook.getCurrentWord().getWord();
                    int sleepPlus = 0;
                    if(currentWord.length()>12){
                        sleepPlus = 200;
                    }if(currentWord.contains(".")){
                        sleepPlus =500;
                    }else
                    if(currentWord.contains("-") || currentWord.contains(",")
                            || currentWord.contains(";")|| currentWord.contains(":")|| currentWord.contains("!")){
                        sleepPlus = 300;
                    }
                    Thread.sleep(time+sleepPlus);
                    publishProgress("");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return "executed";
        }

        @Override
        protected void onProgressUpdate(String... text) {
            nextWord();
        }

        @Override
        protected void onPostExecute(String result) {
        }

    }

    private boolean isLoadingMoreWords = false;
    private void nextWord(){
        if(!isDownSwipingBoi && isPlaying){
            mViewingBook.getNextWordAndUpdatePos();

            String nextWord = mViewingBook.getCurrentWord().getWord();
            String finalWord = nextWord.replaceAll("\n"," ")
                    .replaceAll("\t"," ")
                    .replaceAll("-","")
                    .replaceAll(" ", "");
            currentWordTextView.setText(finalWord);

            percentageBarView.setProgress(mViewingBook.percentageForCompletion());
            bookProgressValueTextView.setText(mViewingBook.percentageForCompletion()+"%");

            bookProgressBigProgressBar.setProgress(mViewingBook.percentageForCompletion());
            new DatabaseManager(mContext).addReadCount();
            if(mViewingBook.isLastWord()){
                pauseWord();
            }

            if(mViewingBook.getNumberOfPagesToLoad()!=0) {
                if (!isLoadingMoreWords && mViewingBook.getCurrentWordId() + 200 >= mViewingBook.getSentenceWords().size()) {
//                    loadMoreWordsIfYouCan();
                    isLoadingMoreWords = true;
                    new loadMoreOfTheBookBackgoundTask().execute();
                    new DatabaseManager(mContext).updateBookProgress(mViewingBook);
                }
            }
        }

    }




    @Bind(R.id.speedView) View SpeedView;
    @Bind(R.id.speedContainerCardView) MaterialCardView speedContainerCardView;
    @Bind(R.id.speedBarView) View speedBarView;
    @Bind(R.id.speedTextView) TextView speedTextView;
    @Bind(R.id.wpmTextView) TextView wpmTextView;
    private int prevPos = 0;
    private int y_deltaBoi;
    private boolean isDownSwipingBoi = false;
    private GestureDetector swipeTopGestureDetector;
    private GestureDetector doubleTapGestureDetector;
    private  GestureDetector doubleTapNextGestureDetector;

    private void setTouchActivator(){
        swipeTopGestureDetector = new GestureDetector(this, new MySwipeBackMainGestureListener());
        doubleTapGestureDetector = new GestureDetector(this, new DoubleTapGestureListener());
        doubleTapNextGestureDetector = new GestureDetector(this, new DoubleTapNextGestureListener());

        SpeedView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (swipeTopGestureDetector.onTouchEvent(motionEvent)) {
                    return true;
                }
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    if (isDownSwipingBoi) {
                        isDownSwipingBoi = false;

                    }
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if(!isDownSwipingBoi) hideSpeedContainerCardView();
                        }
                    },300);
                }

                return false;
            }
        });

        restartSentenceView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return doubleTapGestureDetector.onTouchEvent(motionEvent);
            }
        });

        skipNextSentenceView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return doubleTapNextGestureDetector.onTouchEvent(motionEvent);
            }
        });
    }

    class MySwipeBackMainGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent event) {
            final int Y = (int) event.getRawY();

            RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams) speedContainerCardView.getLayoutParams();
            y_deltaBoi = Y - lParams.topMargin;

            ShowSpeedContainerCardView();
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            final int Y = (int) e2.getRawY();

            updateWordSpeed2(Y-y_deltaBoi, false);
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            Log.d(TAG,"velocityY-"+velocityY);
            isDownSwipingBoi = false;
//            updateWordSpeed2(translationPos+70, true);
            return false;

        }

    }





    private int translationPos = DatabaseManager.dpToPx(150);
    private int oldIVal = 0;
    private void updateWordSpeed2(int i, boolean shouldAnimate) {
        Log.e(TAG,"Value of i: "+i);

        int valToAdd = i-oldIVal;
        translationPos+=valToAdd;

        Log.e(TAG, "New trans position: "+translationPos);

        if(translationPos<=DatabaseManager.dpToPx(300) && translationPos>=DatabaseManager.dpToPx(0)){
            Log.e(TAG, "Value i fits bounds: 0-"+DatabaseManager.dpToPx(300));
            double percentage = ((DatabaseManager.dpToPx(300)-(double)translationPos)/DatabaseManager.dpToPx(300))*100;
            double zeroToOne = (((double)translationPos)/DatabaseManager.dpToPx(300));

            if(!shouldAnimate) {
                speedBarView.setTranslationY(translationPos);
            }else{
                speedBarView.animate().setDuration(mAnimationDurationFast).setInterpolator(new LinearOutSlowInInterpolator())
                        .translationY(translationPos).setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        speedBarView.setTranslationY(translationPos);
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                }).start();
            }


            speedTextView.setText((int)percentage+"%");
            time = (int) (Constants.MIN_READING_TIME + (zeroToOne *(Constants.MAX_READING_TIME - Constants.MIN_READING_TIME)));
        }else{
            Log.e(TAG, "Value of i doesnt fit bounds 0-"+DatabaseManager.dpToPx(300));

        }
        oldIVal = i;

        double wordCount = 60000/time;
        wpmTextView.setText(String.format("~%d words/min", (int) wordCount));
    }

    private void setTranslationPos(){
        speedBarView.setTranslationY(translationPos);
        double percentage = ((DatabaseManager.dpToPx(300)-(double)translationPos)/DatabaseManager.dpToPx(300))*100;
        speedTextView.setText((int)percentage+"%");

        double wordCount = 60000/time;
        wpmTextView.setText(String.format("~%d words/min", (int) wordCount));
    }

    private void ShowSpeedContainerCardView(){
        speedContainerCardView.animate().translationX(0).alpha(1f).setDuration(mAnimationDurationFast)
                .setInterpolator(new LinearOutSlowInInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        speedContainerCardView.setTranslationX(0);
                        speedContainerCardView.setAlpha(1f);

                        wpmTextView.setAlpha(1f);
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                }).start();

        wpmTextView.animate().alpha(1f).setDuration(mAnimationDuration).start();
    }

    private void hideSpeedContainerCardView(){
        speedContainerCardView.animate().translationX(DatabaseManager.dpToPx(-70)).alpha(0f).setDuration(mAnimationDurationSlow)
                .setInterpolator(new LinearOutSlowInInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        speedContainerCardView.setTranslationX(DatabaseManager.dpToPx(-70));
                        speedContainerCardView.setAlpha(0f);

                        wpmTextView.setAlpha(0f);
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                }).start();
        wpmTextView.animate().alpha(0f).setDuration(mAnimationDuration).start();

        new DatabaseManager(mContext).setReadingSpeed(time, translationPos);
        oldIVal = 0;

        setWordsRemainingTextView();
    }




    @Bind(R.id.restartSentenceView) View restartSentenceView;
    @Bind(R.id.forwardImageView) ImageView forwardImageView;
    @Bind(R.id.rewindImageView) ImageView rewindImageView;
    @Bind(R.id.skipNextSentenceView) View skipNextSentenceView;

    private int getStartingPosOfCurrentOrPrevSentence(){
        for(int i = mViewingBook.getCurrentWordId().intValue(); i>0; i-- ){
            Word prevWord = mViewingBook.getSentenceWords().get(i-1);
            if(prevWord.getWord().endsWith(".")){
                //its a last word of sentence
                if(mViewingBook.getCurrentWordId().intValue()-1 != i){
                    //its the first try, the sentence just begun, we skip backwards
                    return  i;
                }
            }
        }
        return 0;
    }

    private class DoubleTapGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }
        // event when double tap occurs
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            pulseBackWard();
            mViewingBook.setCurrentWordId((long)getStartingPosOfCurrentOrPrevSentence());
            setBooksData();

            restartReader();
            return true;
        }
    }

    private class DoubleTapNextGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }
        // event when double tap occurs
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            float x = e.getX();
            float y = e.getY();

            Log.d("Double Tap", "Tapped at: (" + x + "," + y + ")");

            pulseFastForward();
            mViewingBook.setCurrentWordId((long)getStartingPosOfNextSentence());
            setBooksData();

            restartReader();

            return true;
        }
    }

    private int getStartingPosOfNextSentence(){
        for(int i = mViewingBook.getCurrentWordId().intValue(); i<mViewingBook.getSentenceWords().size(); i++){
            Word nextWord = mViewingBook.getSentenceWords().get(i+1);
            if(nextWord.getWord().endsWith(".")){
                //the next word is the last word of current sentence
                return i+2;
            }

        }

        return 0;
    }

    private void pulseFastForward(){
        final float alpha = 1f;

        final int duration = 60;
        forwardImageView.animate().alpha(alpha).setDuration(duration).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                forwardImageView.setAlpha(alpha);
                forwardImageView.animate().alpha(0f).setDuration(duration).setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        forwardImageView.setAlpha(0f);
                        forwardImageView.animate().alpha(alpha).setDuration(duration).setListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animator) {

                            }

                            @Override
                            public void onAnimationEnd(Animator animator) {
                                forwardImageView.setAlpha(alpha);
                                forwardImageView.animate().alpha(0f).setDuration(mAnimationDurationSlow).setListener(new Animator.AnimatorListener() {
                                    @Override
                                    public void onAnimationStart(Animator animator) {

                                    }

                                    @Override
                                    public void onAnimationEnd(Animator animator) {
                                        forwardImageView.setAlpha(0f);
                                    }

                                    @Override
                                    public void onAnimationCancel(Animator animator) {

                                    }

                                    @Override
                                    public void onAnimationRepeat(Animator animator) {

                                    }
                                }).start();
                            }

                            @Override
                            public void onAnimationCancel(Animator animator) {

                            }

                            @Override
                            public void onAnimationRepeat(Animator animator) {

                            }
                        }).start();
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                }).start();
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        }).start();
    }

    private void pulseBackWard(){
        final float alpha = 1f;
        final int duration = 60;
        rewindImageView.animate().alpha(1f).setDuration(duration).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                rewindImageView.setAlpha(0f);
                rewindImageView.animate().alpha(0f).setDuration(duration).setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        rewindImageView.setAlpha(1f);
                        rewindImageView.animate().alpha(1f).setDuration(duration).setListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animator) {

                            }

                            @Override
                            public void onAnimationEnd(Animator animator) {
                                rewindImageView.setAlpha(0f);
                                rewindImageView.animate().alpha(0f).setDuration(mAnimationDurationSlow).setListener(new Animator.AnimatorListener() {
                                    @Override
                                    public void onAnimationStart(Animator animator) {

                                    }

                                    @Override
                                    public void onAnimationEnd(Animator animator) {
                                        rewindImageView.setAlpha(0f);
                                    }

                                    @Override
                                    public void onAnimationCancel(Animator animator) {

                                    }

                                    @Override
                                    public void onAnimationRepeat(Animator animator) {

                                    }
                                }).start();
                            }

                            @Override
                            public void onAnimationCancel(Animator animator) {

                            }

                            @Override
                            public void onAnimationRepeat(Animator animator) {

                            }
                        }).start();
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                }).start();
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        }).start();
    }



    @Bind(R.id.bookOptionsLinearLayout) LinearLayout bookOptionsLinearLayout;
    @Bind(R.id.restartBookLinearLayout) LinearLayout restartBookLinearLayout;
    @Bind(R.id.viewOriginalLinearLayout) LinearLayout viewOriginalLinearLayout;
    @Bind(R.id.separatorView) View separatorView;
    private boolean isBookOptionsShowing = false;

    private void showBookOptions(){
        isBookOptionsShowing = true;
//        bookOptionsLinearLayout.setVisibility(View.VISIBLE);
//        bookDetailsRelativeLayout.setTranslationY(DatabaseManager.dpToPx(110));

        restartSentenceView.setVisibility(View.GONE);
        skipNextSentenceView.setVisibility(View.GONE);
        separatorView.setVisibility(View.VISIBLE);

        Animation anim = new Animation(){
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) bookDetailsRelativeLayout.getLayoutParams();
                layoutParams.bottomMargin = (int)((1f - interpolatedTime) * (float)DatabaseManager.dpToPx(-120));
                bookDetailsRelativeLayout.setLayoutParams(layoutParams);
            }
        };
        anim.setDuration(mAnimationDuration);
        anim.setInterpolator(new LinearOutSlowInInterpolator());
        bookDetailsRelativeLayout.startAnimation(anim);


        restartBookLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                restartBook();
            }
        });
        viewOriginalLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                open_File(mViewingBook.getBookPath());
            }
        });
    }

    private void hideBookOptions(){
        isBookOptionsShowing = false;

        Animation anim = new Animation(){
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                Log.e(TAG,"interpolatedTime: "+interpolatedTime);
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) bookDetailsRelativeLayout.getLayoutParams();
                layoutParams.bottomMargin = (int)(interpolatedTime* (float)DatabaseManager.dpToPx(-120));
                bookDetailsRelativeLayout.setLayoutParams(layoutParams);
            }
        };
        anim.setDuration(mAnimationDuration);
        anim.setInterpolator(new LinearOutSlowInInterpolator());
        bookDetailsRelativeLayout.startAnimation(anim);

        restartSentenceView.setVisibility(View.VISIBLE);
        skipNextSentenceView.setVisibility(View.VISIBLE);
        separatorView.setVisibility(View.GONE);
    }
    @Override
    public void onBackPressed(){
        if(isBookOptionsShowing){
            hideBookOptions();
        }else{
            super.onBackPressed();
        }
    }

    private void restartBook(){
        AlertDialog alertDialog = new AlertDialog.Builder(ReaderActivity.this).create();
        alertDialog.setTitle("Restart your book from the beginning?");
        alertDialog.setMessage("Are you sure you want to continue?");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "YES.",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mViewingBook.setCurrentWordId(0L);
                        Toast.makeText(mContext, "Your book has been restarted.",Toast.LENGTH_SHORT).show();

                        setBooksData();
                        hideBookOptions();
                        dialog.dismiss();
                    }
                });
        alertDialog.show();


    }

    public void open_File(String filename) {
        try{
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(filename));
            startActivity(browserIntent);
        }catch (Exception e){
            e.printStackTrace();
            Toast.makeText(mContext, "Something went wrong.", Toast.LENGTH_SHORT).show();
        }

    }



    private void loadMoreWordsIfYouCan(){
        int lastPrintedPage = mViewingBook.getLastPrintedPage();
        String pdfUrl = mViewingBook.getBookUrl();
        try {
            String parsedText="";
            PdfReader reader = new PdfReader(pdfUrl);
            int n = reader.getNumberOfPages();

//            if(n<lastPrintedPage+5){
//                parsedText = loadSpecificPages(parsedText,reader,lastPrintedPage,n);
//            }else{
//                parsedText = loadSpecificPages(parsedText,reader,lastPrintedPage,lastPrintedPage+5);
//            }

            parsedText = loadSpecificPages(parsedText,reader,lastPrintedPage,mViewingBook.getNumberOfPagesToLoad());
            mViewingBook.setLastPrintedPage(mViewingBook.getNumberOfPagesToLoad());

            reader.close();


            loadAllWordsAndSentences(parsedText);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class loadMoreOfTheBookBackgoundTask extends AsyncTask<String, Void, String> {


        @Override
        protected String doInBackground(String... strings) {
            loadMoreWordsIfYouCan();
            return "executed";
        }

        @Override
        protected void onPostExecute(String result) {
            isLoadingMoreWords = false;
        }

    }

    private String loadSpecificPages(String parsedText, PdfReader reader, int lastprintedPage, int finalPrintedPage) throws IOException {
        for (int i = lastprintedPage; i <=finalPrintedPage ; i++) {
            parsedText   = new StringBuilder()
                    .append(parsedText)
                    .append(PdfTextExtractor.getTextFromPage(reader, i + 1).trim())
                    .append("\n")
                    .toString(); //Extracting the content from the different pages
        }
        return parsedText;
    }

    private void loadAllWordsAndSentences(String everything){
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

        int lastLoadedWordPos = mViewingBook.getSentenceWords().size()-1;
        mViewingBook.addSentencewords(allTheWordsForLoadedBook);

        if(!allTheWordsForLoadedBook.isEmpty()) {
            new DatabaseManager(mContext).storeBooksNewlyLoadedWords(mViewingBook, lastLoadedWordPos);
        }

        Log.e("MainActivity", "We've got: "+ allTheWordsForLoadedBook.size());
    }




    private int x_deltaBoi;
    private boolean isRightSwipingBoi = false;
    @Bind(R.id.forwardBookSentenceView) View forwardBookSentenceView;
    @Bind(R.id.animatorWordTextView) TextView animatorWordTextView;
    @Bind(R.id.animatorWord2TextView) TextView animatorWord2TextView;

    private GestureDetector swipeLeftGestureDetector;

    private void setFastForwardTouchActivator(){
        swipeLeftGestureDetector = new GestureDetector(this, new MySwipeFastForwardMainGestureListener());

        forwardBookSentenceView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (swipeLeftGestureDetector.onTouchEvent(motionEvent)) {
                    return true;
                }
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    if (isRightSwipingBoi) {
                        isRightSwipingBoi = false;

                    }
                    stopWinding();
                }

                return false;
            }
        });

    }

    class MySwipeFastForwardMainGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent event) {
            final int X= (int) event.getRawX();

            RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams) forwardBookSentenceView.getLayoutParams();
            x_deltaBoi = X - lParams.leftMargin;

            pauseWithoutShowing();
            isRightSwipingBoi = true;
            isRewinding = true;

            windWordsBackgroundTask v = new windWordsBackgroundTask();
            v.execute();

            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            final int X = (int) e2.getRawX();

            updateRewindSpeed(X-x_deltaBoi);
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            isRightSwipingBoi = false;
            return false;

        }

    }


    private void updateRewindSpeed(int val){
        Log.e(TAG," rewind speed: "+ val);

        if(Math.abs(val)<400) {
            int finalValue = 400 - Math.abs(val);

            float alpha = (float)Math.abs(val)/(float)400;
            int translation = (int)(((float)Math.abs(val)/(float)400)*100);

            if (val < 0) {
                //were rewinding
                forwardImageView.setVisibility(View.GONE);
                rewindImageView.setVisibility(View.VISIBLE);

                rewindImageView.setAlpha(alpha);
                rewindImageView.setTranslationX(DatabaseManager.dpToPx(-70)-translation);

                windDuration = -(finalValue);
            } else {
                //were forwarding
                forwardImageView.setVisibility(View.VISIBLE);
                rewindImageView.setVisibility(View.GONE);

                forwardImageView.setAlpha(alpha);
                forwardImageView.setTranslationX(DatabaseManager.dpToPx(70)+translation);

                windDuration = finalValue;
            }
        }

        Log.e(TAG," wind Duration speed: "+ windDuration);
    }

    private void pauseWithoutShowing(){
        isPlaying = false;
        new DatabaseManager(mContext).updateBookProgress(mViewingBook);

    }

    private void fastForwardWords(String nextWord){
        currentWordTextView.setText(nextWord);

    }

    private void stopWinding(){
        Log.e(TAG, "Stopping winding");
        isRewinding = false;
        isPlaying = true;


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startReader();
            }
        },900);

        forwardImageView.animate().alpha(0f).translationX(DatabaseManager.dpToPx(70)).setInterpolator(new LinearOutSlowInInterpolator())
                .setDuration(mAnimationDuration).start();

        rewindImageView.animate().alpha(0f).translationX(DatabaseManager.dpToPx(-70)).setInterpolator(new LinearOutSlowInInterpolator())
                .setDuration(mAnimationDuration).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                forwardImageView.setVisibility(View.GONE);
                rewindImageView.setVisibility(View.GONE);

                rewindImageView.setAlpha(0f);
                rewindImageView.setTranslationX(DatabaseManager.dpToPx(-70));

                forwardImageView.setAlpha(0f);
                forwardImageView.setTranslationX(DatabaseManager.dpToPx(70));
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        }).start();

    }

    private int windDuration = 400;
    private boolean isRewinding = false;
    private class windWordsBackgroundTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... strings) {
            while (isRewinding) {
                try {
                    Thread.sleep(Math.abs(windDuration));
                    publishProgress("");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return "executed";
        }

        @Override
        protected void onProgressUpdate(String... text) {
            if(windDuration<0 && mViewingBook.getCurrentWordId()-1>0 ){
                fastForwardWords(mViewingBook.getNextWord().getWord());
                mViewingBook.setCurrentWordId(mViewingBook.getCurrentWordId()-1);
            }else{
                if(mViewingBook.getCurrentWordId()+1 != mViewingBook.getSentenceWords().size()) {
                    fastForwardWords(mViewingBook.getLastWord().getWord());
                    mViewingBook.setCurrentWordId(mViewingBook.getCurrentWordId() + 1);
                }
            }
            new DatabaseManager(mContext).updateBookProgress(mViewingBook);
        }

        @Override
        protected void onPostExecute(String result) {
            String nextWord = mViewingBook.getCurrentWord().getWord();
            String finalWord = nextWord.replaceAll("\n"," ")
                    .replaceAll("\t"," ")
                    .replaceAll("-","")
                    .replaceAll(" ", "");
            currentWordTextView.setText(finalWord);

            percentageBarView.setProgress(mViewingBook.percentageForCompletion());
            bookProgressValueTextView.setText(mViewingBook.percentageForCompletion()+"%");

            bookProgressBigProgressBar.setProgress(mViewingBook.percentageForCompletion());
            new DatabaseManager(mContext).addReadCount();
            if(mViewingBook.isLastWord()){
                pauseWord();
            }

            if(mViewingBook.getNumberOfPagesToLoad()!=0) {
                if (mViewingBook.getCurrentWordId() + 200 >= mViewingBook.getNumberOfPagesToLoad()) {
                    loadMoreWordsIfYouCan();
                    new DatabaseManager(mContext).updateBookProgress(mViewingBook);
                }
            }
        }

    }



}
