package com.color.wordreader.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator;

import android.animation.Animator;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.color.wordreader.Constants;
import com.color.wordreader.Models.Book;
import com.color.wordreader.R;
import com.color.wordreader.Services.DatabaseManager;
import com.google.android.material.card.MaterialCardView;

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

    private int mAnimationDuration = 300;
    private int mAnimationDurationFast = 150;
    private int mAnimationDurationSlow = 450;
    private boolean isPlaying = false;
    private Book mViewingBook;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reader);

        ButterKnife.bind(this);
        mContext = this.getApplicationContext();

        pauseView.setOnClickListener(this);

        int intent = getIntent().getExtras().getInt(Constants.BOOK_ID);
        mViewingBook = new DatabaseManager(mContext).loadSpecificBook(intent);

        time = new DatabaseManager(mContext).getReadingSpeed();
        translationPos = new DatabaseManager(mContext).getTransPos();

        Log.e(TAG, "Gotten Time: "+time);
        Log.e(TAG, "Gotten translation position: "+translationPos);

        setTranslationPos();
        setBooksData();
        setTouchActivator();
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
    }

    @Override
    public void onClick(View view) {
        if(view.equals(pauseView)){
            if(!isPlaying) playWordReader();
            else pauseWord();
        }
    }

    private void setBooksData(){
        currentWordTextView.setText(mViewingBook.getCurrentWord().getWord());

        bookThumbnailImageView.setImageBitmap(mViewingBook.getBookCover());
        bookTitleTextView.setText(mViewingBook.getBookName());

        percentageBarView.setProgress(mViewingBook.percentageForCompletion());
        bookProgressValueTextView.setText(mViewingBook.percentageForCompletion()+"%");

        bookProgressBigProgressBar.setProgress(mViewingBook.percentageForCompletion());
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if(!hasFocus){
            pauseWord();
        }
    }


    private void startReader(){
        wordTimerBackgroundTask w = new wordTimerBackgroundTask();
        w.execute();
    }

    private int time = 500;

    private class wordTimerBackgroundTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... strings) {
            while (isPlaying) {
                try {
                    if(mViewingBook.getCurrentWord().getWord().length()>8){
                        Thread.sleep(time+200);
                    }else{
                        Thread.sleep(time);
                    }
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


    private void nextWord(){
        if(!isDownSwipingBoi){
            mViewingBook.getNextWordAndUpdatePos();

            currentWordTextView.setText(mViewingBook.getCurrentWord().getWord());
            percentageBarView.setProgress(mViewingBook.percentageForCompletion());
            bookProgressValueTextView.setText(mViewingBook.percentageForCompletion()+"%");

            bookProgressBigProgressBar.setProgress(mViewingBook.percentageForCompletion());


        }

    }


    @Bind(R.id.speedView) View SpeedView;
    @Bind(R.id.speedContainerCardView) MaterialCardView speedContainerCardView;
    @Bind(R.id.speedBarView) View speedBarView;
    @Bind(R.id.speedTextView) TextView speedTextView;


    private int prevPos = 0;
    private int y_deltaBoi;
    private boolean isDownSwipingBoi = false;
    private GestureDetector swipeTopGestureDetector;

    private void setTouchActivator(){
        swipeTopGestureDetector = new GestureDetector(this, new MySwipeBackMainGestureListener());

        SpeedView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (swipeTopGestureDetector.onTouchEvent(motionEvent)) {
                    ShowSpeedContainerCardView();
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
    }

    class MySwipeBackMainGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent event) {
            final int Y = (int) event.getRawY();

            RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams) speedContainerCardView.getLayoutParams();
            y_deltaBoi = Y - lParams.topMargin;


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

    }

    private void setTranslationPos(){
        speedBarView.setTranslationY(translationPos);
        double percentage = ((DatabaseManager.dpToPx(300)-(double)translationPos)/DatabaseManager.dpToPx(300))*100;
        speedTextView.setText((int)percentage+"%");

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
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                }).start();
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
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                }).start();
        new DatabaseManager(mContext).setReadingSpeed(time, translationPos);
        oldIVal = 0;
    }



}
