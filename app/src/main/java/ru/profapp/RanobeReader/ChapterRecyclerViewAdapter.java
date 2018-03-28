package ru.profapp.RanobeReader;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ru.profapp.RanobeReader.DAO.DatabaseDao;
import ru.profapp.RanobeReader.Helpers.RanobeKeeper;
import ru.profapp.RanobeReader.Models.Chapter;
import ru.profapp.RanobeReader.Models.Ranobe;
import ru.profapp.RanobeReader.Models.TextChapter;

public class ChapterRecyclerViewAdapter extends
        RecyclerView.Adapter<ChapterRecyclerViewAdapter.ViewHolder> {

    private final List<Chapter> mValues;
    private final Context mContext;
    private Drawable downloadDoneImage;

    ChapterRecyclerViewAdapter(List<Chapter> chapterList, Context Context) {
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
            mValues=chapterList;
        }

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
        holder.mItem = mValues.get(position);
        holder.mIdView.setText(mValues.get(position).getTitle());

        holder.mView.setOnClickListener(v -> {

            if( RanobeKeeper.getInstance().getRanobe()==null || !Objects.equals(holder.mItem.getRanobeUrl(),
                    RanobeKeeper.getInstance().getRanobe().getUrl())){

                Ranobe ranobe = new Ranobe();
                ranobe.setUrl(holder.mItem.getRanobeUrl());
                ranobe.updateRanobe(mContext);
                RanobeKeeper.getInstance().setRanobe(ranobe);

            }
            Intent intent = new Intent(v.getContext(), ChapterText.class);
            intent.putExtra("ChapterIndex", holder.getAdapterPosition());
            v.getContext().startActivity(intent);
        });

        new Thread() {
            @Override
            public void run() {

                try {
                    TextChapter chapter = DatabaseDao.getInstance(
                            mContext).getTextDao().getTextByChapterUrl(holder.mItem.getUrl());
                    if (chapter != null) {
                        String chapterText = chapter.getText();

                        if (!chapter.getText().equals("")) {
                            holder.mItem.setText(chapterText);
                            holder.mImageButton.setImageDrawable(downloadDoneImage);
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    Crashlytics.logException(e);
                }
            }
        }.start();

        holder.mImageButton.setOnClickListener(v -> {

            String text = getText(holder.mItem, holder.mContext);

            holder.mImageButton.setImageDrawable(downloadDoneImage);
            holder.mItem.setDownloaded(true);
            holder.mItem.setText(text);

        });
        if (!holder.mItem.getCanRead()) {
            holder.mView.setBackgroundColor(Color.GRAY);
            holder.mImageButton.setVisibility(View.INVISIBLE);
        }
        if (holder.mItem.getReaded()) {
            holder.mView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorPrimaryDark));
        }

    }

    private String getText(Chapter chapter, Context context) {
        ChapterText chapterText = new ChapterText();
        chapterText.GetChapterText(chapter, context);
        return chapterText.mCurrentChapter.getText();
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        final TextView mIdView;
        final ImageButton mImageButton;
        final Context mContext;
        Chapter mItem;

        ViewHolder(View view) {
            super(view);
            mView = view;
            mIdView = view.findViewById(R.id.id);
            mContext = view.getContext();
            mImageButton = view.findViewById(R.id.imageButton_download);
        }

    }
}
