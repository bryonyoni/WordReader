package com.color.wordreader.Adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.color.wordreader.Activities.ReaderActivity;
import com.color.wordreader.Constants;
import com.color.wordreader.Models.Book;
import com.color.wordreader.R;

import java.util.List;

public class MyBooksRecyclerAdapter extends RecyclerView.Adapter<MyBooksRecyclerAdapter.ViewHolder>{
    private List<Book> mBooks;
    private Activity mActivity;

    public MyBooksRecyclerAdapter(List<Book> books, Activity acc){
        this.mActivity = acc;
        this.mBooks = books;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View recipeView = inflater.inflate(R.layout.my_book_item, viewGroup, false);
        return new MyBooksRecyclerAdapter.ViewHolder(recipeView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Book book = mBooks.get(position);

        holder.bookThumbnailImageView.setImageBitmap(book.getBookCover());
        holder.bookTitleTextView.setText(book.getBookName());

        holder.percentageBarView.setProgress(book.percentageForCompletion());
        holder.percentageTextView.setText(book.percentageForCompletion()+"%");

        holder.bookThumbnailImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mActivity, ReaderActivity.class);
                intent.putExtra(Constants.BOOK_ID, book.getStoragePos());
                mActivity.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mBooks.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        ImageView bookThumbnailImageView;
        TextView bookTitleTextView;
        TextView percentageTextView;
        ProgressBar percentageBarView;

        ViewHolder(View itemView) {
            super(itemView);

            bookThumbnailImageView = itemView.findViewById(R.id.bookThumbnailImageView);
            bookTitleTextView = itemView.findViewById(R.id.bookTitleTextView);
            percentageTextView = itemView.findViewById(R.id.percentageTextView);
            percentageBarView = itemView.findViewById(R.id.percentageBarView);

        }
    }



}
