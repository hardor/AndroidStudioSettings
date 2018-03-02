package ru.profapp.RanobeReader;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.crash.FirebaseCrash;

import java.util.List;

import ru.profapp.RanobeReader.DAO.DatabaseDao;
import ru.profapp.RanobeReader.Models.Chapter;
import ru.profapp.RanobeReader.Models.Ranobe;

public class ChapterRecyclerViewAdapter extends
        RecyclerView.Adapter<ChapterRecyclerViewAdapter.ViewHolder> {

    private final List<Chapter> mValues;
    private final Context mContext;
    Drawable downloadImage, downloadDoneImage;
    private Ranobe mRanobe;

    public ChapterRecyclerViewAdapter(Ranobe ranobe, Context Context) {
        mRanobe = ranobe;
        mValues = ranobe.getChapterList();
        mContext = Context;
    }

    public ChapterRecyclerViewAdapter(List<Chapter> chapterList, Context Context) {
        mValues = chapterList;
        mContext = Context;
    }

    public void setDownloadImage(Drawable downloadImage) {
        this.downloadImage = downloadImage;
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
                intent.putExtra("RanobeInfo", mRanobe);
                v.getContext().startActivity(intent);
            }
        });

        new Thread() {
            @Override
            public void run() {

                try {
                    String chapterText = DatabaseDao.getInstance(
                            mContext).getTextDao().getTextByChapterUrl(
                            holder.mItem.getUrl()).getText();

                    if (chapterText != null && !chapterText.equals("")) {
                        holder.mItem.setText(chapterText);
                        holder.mImageButton.setImageDrawable(downloadDoneImage);
                    }

                }catch (Exception e){
                    e.printStackTrace();
//                    FirebaseCrash.report(e);
                }
            }
        }.start();

        holder.mImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String text = getText(holder.mItem, holder.mContext);

                holder.mImageButton.setImageDrawable(downloadDoneImage);
                holder.mItem.setDownloaded(true);
                holder.mItem.setText(text);

            }
        });
        if (!holder.mItem.getCanRead()) {
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
        final Context mContext;
        Chapter mItem;

        ViewHolder(View view) {
            super(view);
            mView = view;
            mIdView = (TextView) view.findViewById(R.id.id);
            mContext = view.getContext();
            mImageButton = (ImageButton) view.findViewById(R.id.imageButton_download);
        }

    }
}
