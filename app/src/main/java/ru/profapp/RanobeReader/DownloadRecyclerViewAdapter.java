package ru.profapp.RanobeReader;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import java.util.List;

import ru.profapp.RanobeReader.Models.Chapter;

public class DownloadRecyclerViewAdapter extends
        RecyclerView.Adapter<DownloadRecyclerViewAdapter.ViewHolder> {

    private final List<Chapter> mValues;
    private final Context mContext;

    DownloadRecyclerViewAdapter(List<Chapter> chapterList,
            Context Context) {

        mValues = chapterList;
        mContext = Context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.chapter_item_download, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mChapterItem = mValues.get(position);
        holder.mCheckBox.setText(holder.mChapterItem.getTitle());

        if (holder.mChapterItem.getCanRead()) {
            holder.mCheckBox.setEnabled(true);

            holder.mCheckBox.setChecked(holder.mChapterItem.getChecked());
            if(holder.mChapterItem.getDownloaded() ){
                holder.mCheckBox.setTextColor(mContext.getResources().getColor(R.color.colorAccent));
            }else {
                holder.mCheckBox.setTextColor(mContext.getResources().getColor(R.color.webViewText));
            }

            holder.mCheckBox.setOnClickListener(v -> {

                holder.mCheckBox.setChecked(!holder.mChapterItem.getChecked());
                holder.mChapterItem.setChecked(!holder.mChapterItem.getChecked());

            });
        } else {
            holder.mCheckBox.setEnabled(false);
        }

    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        final CheckBox mCheckBox;
        final Context mContext;
        Chapter mChapterItem;

        ViewHolder(View view) {
            super(view);
            mView = view;
            mCheckBox = view.findViewById(R.id.checkBox);
            mContext = view.getContext();

        }

    }
}
