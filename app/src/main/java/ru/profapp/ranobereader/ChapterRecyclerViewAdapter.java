package ru.profapp.ranobereader;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import ru.profapp.ranobereader.DAO.Database;
import ru.profapp.ranobereader.Models.Chapter;
import ru.profapp.ranobereader.Models.TextChapter;

public class ChapterRecyclerViewAdapter extends
        RecyclerView.Adapter<ChapterRecyclerViewAdapter.ViewHolder> {

    private final List<Chapter> mValues;
    Drawable downloadImage, downloadDoneImage;

    public ChapterRecyclerViewAdapter(List<Chapter> items) {
        mValues = items;
    }

    public Drawable getDownloadImage() {
        return downloadImage;
    }

    public void setDownloadImage(Drawable downloadImage) {
        this.downloadImage = downloadImage;
    }

    public Drawable getDownloadDoneImage() {
        return downloadDoneImage;
    }

    public void setDownloadDoneImage(Drawable downloadDoneImage) {
        this.downloadDoneImage = downloadDoneImage;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.chapter_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mIdView.setText(mValues.get(position).getTitle());

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(v.getContext(), ChapterText.class);
                intent.putExtra("ChapterInfo", holder.mItem);
                v.getContext().startActivity(intent);
            }
        });

        holder.mImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final TextChapter textChapter = new TextChapter(holder.mItem.getUrl(),
                        getText(holder.mItem, holder.mContext));
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        Database.getInstance(holder.mView.getContext()).getTextDao().insert(textChapter);
                    }
                });
                holder.mImageButton.setImageDrawable(downloadDoneImage);

            }
        });
        if (! holder.mItem.getCanRead()){
            holder.mIdView.setBackgroundColor(Color.GRAY);
        }

    }

    private String getText(Chapter chapter, Context context) {
        ChapterText chapterText = new ChapterText();
        chapterText.GetChapterText(chapter, context);
        return chapterText.mChapter.getText();

    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        final TextView mIdView;
        final ImageButton mImageButton;
        Chapter mItem;
        final Context mContext;


        ViewHolder(View view) {
            super(view);
            mView = view;
            mIdView = (TextView) view.findViewById(R.id.id);
            mContext = view.getContext();
            mImageButton = (ImageButton) view.findViewById(R.id.imageButton_download);
        }

    }
}
