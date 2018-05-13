package ru.profapp.RanobeReader;


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ru.profapp.RanobeReader.Common.StringResources;
import ru.profapp.RanobeReader.DAO.DatabaseDao;
import ru.profapp.RanobeReader.Helpers.MyLog;
import ru.profapp.RanobeReader.Helpers.RanobeKeeper;
import ru.profapp.RanobeReader.Models.Chapter;
import ru.profapp.RanobeReader.Models.Ranobe;
import ru.profapp.RanobeReader.Models.TextChapter;

public class ChapterRecyclerViewAdapter extends
        RecyclerView.Adapter<ChapterRecyclerViewAdapter.ViewHolder> {

    private final Ranobe mRanobe;
    private final List<Chapter> mValues;
    private final Context mContext;
    private Drawable downloadDoneImage;

    ChapterRecyclerViewAdapter(List<Chapter> chapterList, Context Context, Ranobe ranobe) {
        // hide payed chapters
        List<Chapter> newList = new ArrayList<>();
        if (RanobeKeeper.getInstance().getHideUnavailableChapters()) {
            for (Chapter item : chapterList) {
                if (item.getCanRead()) {
                    newList.add(item);
                }
            }
            mValues = newList;
        } else {
            mValues = chapterList;
        }
        mRanobe = ranobe;
        mContext = Context;
    }

    void setDownloadDoneImage(Drawable downloadDoneImage) {

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
        holder.mChapterItem = mValues.get(position);
        holder.mTextView.setText(mValues.get(position).getTitle());

        if (!holder.mChapterItem.getCanRead()) {
            holder.mTextView.setBackgroundColor(Color.GRAY);
            holder.mImageButton.setVisibility(View.INVISIBLE);
        } else {
            holder.mTextView.setOnClickListener(v -> {

                if (RanobeKeeper.getInstance().getRanobe() == null || !Objects.equals(
                        holder.mChapterItem.getRanobeUrl(),
                        RanobeKeeper.getInstance().getRanobe().getUrl())) {

                    Ranobe ranobe = new Ranobe();
                    ranobe.setUrl(holder.mChapterItem.getRanobeUrl());
                    try {
                        ranobe.updateRanobe(mContext);
                    } catch (Exception ignored) {
                        ranobe = mRanobe;
                    }

                    RanobeKeeper.getInstance().setRanobe(ranobe);
                }
                if(RanobeKeeper.getInstance().getRanobe() !=null){
                    Intent intent = new Intent(v.getContext(), ChapterTextActivity.class);
                    intent.putExtra("ChapterIndex", holder.getAdapterPosition());

                    v.getContext().startActivity(intent);
                }


            });

            holder.mImageButton.setOnClickListener(v -> {

                if (holder.mChapterItem.getDownloaded()) {

                    try {
                        new Thread() {
                            @Override
                            public void run() {
                                DatabaseDao.getInstance(mContext).getTextDao().delete(
                                        holder.mChapterItem.getUrl());
                            }

                        }.start();
                        holder.mImageButton.setImageDrawable(holder.downloadImage);
                        holder.mChapterItem.setDownloaded(false);
                        holder.mChapterItem.setText("");
                    } catch (Exception ignored) {
                    }

                } else {
                    Pair<String, Boolean> res = getText(holder.mChapterItem, holder.mContext);

                    if (res.first != null && !res.first.isEmpty()) {
                        if (res.second) {
                            holder.mImageButton.setImageDrawable(downloadDoneImage);
                            holder.mChapterItem.setDownloaded(true);
                        } else {
                            Toast.makeText(mContext, "Loading error", Toast.LENGTH_SHORT).show();
                        }
                        holder.mChapterItem.setText(res.first);
                    }
                }
            });

        }

        if (holder.mChapterItem.getReaded()) {
            holder.mView.setBackgroundColor( ContextCompat.getColor(mContext, R.color.colorPrimaryDark));
        }

        AsyncTask.execute(() -> {

            try {
                if (holder.mChapterItem.getDownloaded()) {
                    ((AppCompatActivity) mContext).runOnUiThread(() -> holder.mImageButton.setImageDrawable(downloadDoneImage));
                } else {
                    TextChapter chapter = DatabaseDao.getInstance(
                            mContext).getTextDao().getTextByChapterUrl(
                            holder.mChapterItem.getUrl());
                    if (chapter != null) {
                        String chapterText = chapter.getText();

                        if (!chapter.getText().equals("")) {

                            holder.mChapterItem.setText(chapterText);
                            holder.mChapterItem.setDownloaded(true);
                            ((AppCompatActivity) mContext).runOnUiThread(() -> holder.mImageButton.setImageDrawable(downloadDoneImage));

                        }
                    }
                }
            } catch (Exception e) {
                MyLog.SendError(StringResources.LogType.WARN,
                        ChapterRecyclerViewAdapter.class.toString(), "", e);

            }
        });
    }

    private Pair<String, Boolean> getText(Chapter chapter, Context context) {
        ChapterTextActivity chapterText = new ChapterTextActivity();
        Boolean res = chapterText.GetChapterText(chapter, context);
        return new Pair<>(chapterText.mCurrentChapter.getText(), res);
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        final TextView mTextView;
        final ImageButton mImageButton;
        final Context mContext;
        final Drawable downloadImage;
        Chapter mChapterItem;

        ViewHolder(View view) {
            super(view);
            mView = view;
            mTextView = view.findViewById(R.id.id);
            mContext = view.getContext();
            mImageButton = view.findViewById(R.id.imageButton_download);
            downloadImage = mImageButton.getDrawable();
        }

    }
}
