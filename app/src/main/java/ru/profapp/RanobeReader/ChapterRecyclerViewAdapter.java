package ru.profapp.RanobeReader;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;
import java.util.Objects;

import ru.profapp.RanobeReader.Common.RanobeConstans;
import ru.profapp.RanobeReader.Helpers.RanobeKeeper;
import ru.profapp.RanobeReader.Models.Chapter;
import ru.profapp.RanobeReader.Models.Ranobe;

public class ChapterRecyclerViewAdapter extends
        RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Ranobe mRanobe;
    private final List<Chapter> mValues;
    private final Context mContext;

    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;

    ChapterRecyclerViewAdapter(List<Chapter> chapterList,
            Context Context, Ranobe ranobe) {

        mValues = chapterList;
        mRanobe = ranobe;
        mContext = Context;

    }

    @Override
    public int getItemViewType(int position) {
        if (mValues.get(position) == null) {
            return VIEW_TYPE_LOADING;
        } else {
            return VIEW_TYPE_ITEM;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_ITEM: {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.chapter_item, parent, false);
                return new ViewHolder(view);
            }
            default:
            case VIEW_TYPE_LOADING: {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_loading,
                        parent, false);
                return new LoadingViewHolder(view);
            }

        }

    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof ViewHolder) {
            ((ViewHolder) holder).mChapterItem = mValues.get(position);
            ((ViewHolder) holder).mTextView.setText(mValues.get(position).getTitle());

            if (!((ViewHolder) holder).mChapterItem.getCanRead()) {
                ((ViewHolder) holder).mView.setBackgroundColor(Color.GRAY);
            } else {
                ((ViewHolder) holder).mTextView.setOnClickListener(v -> {

                    if (RanobeKeeper.getInstance().getRanobe() == null || !Objects.equals(
                            ((ViewHolder) holder).mChapterItem.getRanobeUrl(),
                            RanobeKeeper.getInstance().getRanobe().getUrl())) {

                        Ranobe ranobe = new Ranobe();
                        ranobe.setUrl(((ViewHolder) holder).mChapterItem.getRanobeUrl());
                        if ((RanobeKeeper.getInstance().getFragmentType() != null
                                && RanobeKeeper.getInstance().getFragmentType()
                                != RanobeConstans.FragmentType.History)) {
                            try {
                                ranobe.updateRanobe(mContext);
                            } catch (Exception ignored) {
                                ranobe = mRanobe;
                            }
                        } else {
                            ranobe = mRanobe;
                        }

                        RanobeKeeper.getInstance().setRanobe(ranobe);
                    }
                    if (RanobeKeeper.getInstance().getRanobe() != null) {
                        Intent intent = new Intent(v.getContext(), ChapterTextActivity.class);
                        intent.putExtra("ChapterIndex", holder.getAdapterPosition());

                        v.getContext().startActivity(intent);
                    }

                });

            }

            if (((ViewHolder) holder).mChapterItem.getReaded()) {
                ((ViewHolder) holder).mView.setBackgroundColor(
                        ContextCompat.getColor(mContext, R.color.colorPrimaryDark));
            }

        } else if (holder instanceof LoadingViewHolder) {
            ((LoadingViewHolder) holder).progressBar.setIndeterminate(true);
        }
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
        final Context mContext;
        Chapter mChapterItem;

        ViewHolder(View view) {
            super(view);
            mView = view;
            mTextView = view.findViewById(R.id.id);
            mContext = view.getContext();

        }

    }

    class LoadingViewHolder extends RecyclerView.ViewHolder {
        final ProgressBar progressBar;

        LoadingViewHolder(View view) {
            super(view);
            progressBar = view.findViewById(R.id.progressBar);
        }
    }
}
