package ru.profapp.RanobeReader.Adapters;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.Date;
import java.util.List;

import ru.profapp.RanobeReader.JsonApi.Rulate.RulateComment;
import ru.profapp.RanobeReader.R;

public class CommentsRecyclerViewAdapter extends
        RecyclerView.Adapter<CommentsRecyclerViewAdapter.ViewHolder> {

    private final List<RulateComment> mValues;
    private final Context mContext;

    public CommentsRecyclerViewAdapter(List<RulateComment> comments, Context Context) {

        mValues = comments;

        mContext = Context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_comment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mBodyTextView.setText(holder.mItem.getBody());
        holder.mDateView.setText(String.format("%s %s", holder.mItem.getAuthor(),
                DateFormat.getDateFormat(mContext).format(new Date((long)holder.mItem.getTime()*1000))));

        Glide.with(mContext)
                .load(mValues.get(position).getAvatar()).apply(
                new RequestOptions()
                        .placeholder(R.drawable.ic_adb_black_24dp)
                        .error(R.drawable.ic_error_outline_black_24dp)
                        .fitCenter()
        ).into(holder.mImageView);
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        final TextView mBodyTextView, mDateView;
        final ImageView mImageView;
        final Context mContext;
        RulateComment mItem;

        ViewHolder(View view) {
            super(view);
            mView = view;
            mBodyTextView = view.findViewById(R.id.body);
            mDateView = view.findViewById(R.id.date);
            mContext = view.getContext();
            mImageView = view.findViewById(R.id.icon);
        }

    }
}
