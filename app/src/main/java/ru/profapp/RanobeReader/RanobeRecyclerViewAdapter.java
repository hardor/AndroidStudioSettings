package ru.profapp.RanobeReader;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import ru.profapp.RanobeReader.Common.OnLoadMoreListener;
import ru.profapp.RanobeReader.Common.RanobeConstans;
import ru.profapp.RanobeReader.Common.StringResources;
import ru.profapp.RanobeReader.Helpers.RanobeKeeper;
import ru.profapp.RanobeReader.Models.Chapter;
import ru.profapp.RanobeReader.Models.Ranobe;
import ru.profapp.RanobeReader.RanobeRecyclerFragment.OnListFragmentInteractionListener;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Ranobe} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
class RanobeRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<Ranobe> mValues;
    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;
    private final int VIEW_TYPE_GROUP_TITLE = 2;
    private Context mContext;
    private RanobeConstans.FragmentType mFragmentType;
    private OnLoadMoreListener onLoadMoreListener;

    private boolean isLoading;

    private int visibleThreshold = 7;
    private int lastVisibleItem, totalItemCount;

    public RanobeRecyclerViewAdapter(RecyclerView recyclerView, List<Ranobe> items,
            RanobeConstans.FragmentType fragmentType) {
        mValues = items;
        mFragmentType = fragmentType;
        final LinearLayoutManager linearLayoutManager =
                (LinearLayoutManager) recyclerView.getLayoutManager();
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                totalItemCount = linearLayoutManager.getItemCount();
                lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
                if (!isLoading && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                    if (onLoadMoreListener != null) {
                        onLoadMoreListener.onLoadMore();
                    }
                    isLoading = true;
                }
            }
        });

    }

    @Override
    public int getItemViewType(int position) {
        if (mValues.get(position) == null) {
            return VIEW_TYPE_LOADING;
        } else if (Objects.equals(mValues.get(position).getRanobeSite(),
                StringResources.Title_Site)) {
            return VIEW_TYPE_GROUP_TITLE;
        } else {
            return VIEW_TYPE_ITEM;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        if (viewType == VIEW_TYPE_ITEM) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ranobe_item,
                    parent, false);
            return new RanobeViewHolder(view);
        } else if (viewType == VIEW_TYPE_LOADING) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_loading,
                    parent, false);
            return new LoadingViewHolder(view);
        } else if (viewType == VIEW_TYPE_GROUP_TITLE) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_title,
                    parent, false);
            return new TitleViewHolder(view);
        }

        return null;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof RanobeViewHolder) {
            ((RanobeViewHolder) holder).mItem = mValues.get(position);
            ((RanobeViewHolder) holder).mTitleView.setText(mValues.get(position).getTitle());

            if(mValues.get(position).getReadyDate() != null){
                long diff = (new Date().getTime()- mValues.get(position).getReadyDate().getTime())/1000/60;

                int numOfDays = (int) (diff / ( 60 * 24));
                int hours = (int) (diff /60 -numOfDays*24);
                int minutes = (int) (diff %  60);

                String updateTime = mContext.getString(R.string.update_time, numOfDays, hours, minutes);

                ((RanobeViewHolder) holder).mUpdateTime.setText(updateTime);
            }


            Glide.with(mContext)
                    .load(mValues.get(position).getImage()).apply(
                    new RequestOptions()
                            .placeholder(R.drawable.ic_adb_black_24dp)
                            .error(R.drawable.ic_error_outline_black_24dp)
                            .fitCenter()
            ).into(((RanobeViewHolder) holder).mImageView);

            ((RanobeViewHolder) holder).mView.setOnClickListener(v -> {
                Intent intent = new Intent(mContext, RanobeInfoActivity.class);
                RanobeKeeper.getInstance().setRanobe(((RanobeViewHolder) holder).mItem);
                intent.putExtra("FragmentType", mFragmentType.name());

                mContext.startActivity(intent);

            });

            List<Chapter> chapterList = ((RanobeViewHolder) holder).mItem.getChapterList();

            // hide payed chapters
            List<Chapter> newList = new ArrayList<>();
            if (RanobeKeeper.getInstance().getHideUnavailableChapters()) {
                for (Chapter item : chapterList) {
                    if (item.getCanRead()) {
                        newList.add(item);
                    }
                }
            } else {
                newList=chapterList;
            }
            ChapterRecyclerViewAdapter adapter = new ChapterRecyclerViewAdapter(
                    newList.subList(0, Math.min(4, newList.size())), mContext);
            ((RanobeViewHolder) holder).mChaptersListView.setAdapter(adapter);

        } else if (holder instanceof LoadingViewHolder) {
            LoadingViewHolder loadingViewHolder = (LoadingViewHolder) holder;
            loadingViewHolder.progressBar.setIndeterminate(true);
        } else if (holder instanceof TitleViewHolder) {
            TitleViewHolder titleViewHolder = (TitleViewHolder) holder;
            titleViewHolder.mTextView.setText(mValues.get(position).getTitle());
        }
    }

    @Override
    public int getItemCount() {
        return mValues == null ? 0 : mValues.size();
    }

    void setLoaded() {
        isLoading = false;
    }

    void setOnLoadMoreListener(OnLoadMoreListener mOnLoadMoreListener) {
        this.onLoadMoreListener = mOnLoadMoreListener;
    }

    class LoadingViewHolder extends RecyclerView.ViewHolder {
        ProgressBar progressBar;

        LoadingViewHolder(View view) {
            super(view);
            progressBar = view.findViewById(R.id.progressBar);
        }
    }

    class TitleViewHolder extends RecyclerView.ViewHolder {
        TextView mTextView;
        TitleViewHolder(View view) {
            super(view);
            mTextView = view.findViewById(R.id.group_title);
        }
    }

    class RanobeViewHolder extends RecyclerView.ViewHolder {
        View mView;
        ImageView mImageView;
        Ranobe mItem;
        RecyclerView mChaptersListView;
        private TextView mTitleView, mUpdateTime;


        RanobeViewHolder(View view) {
            super(view);
            mView = view;
            mTitleView = view.findViewById(R.id.ranobeTitle);
            mImageView = view.findViewById(R.id.imageView);
            mUpdateTime = view.findViewById(R.id.ranobeUpdateTime);
            mChaptersListView = view.findViewById(R.id.list_chapter_list);
            mChaptersListView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        }

    }

}
