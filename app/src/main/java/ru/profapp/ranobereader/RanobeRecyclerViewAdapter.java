package ru.profapp.ranobereader;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

import ru.profapp.ranobereader.Common.Constans;
import ru.profapp.ranobereader.DAO.Database;
import ru.profapp.ranobereader.Models.Ranobe;
import ru.profapp.ranobereader.RanobeRecyclerFragment.OnListFragmentInteractionListener;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Ranobe} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class RanobeRecyclerViewAdapter extends
        RecyclerView.Adapter<RanobeRecyclerViewAdapter.ViewHolder> {

    private final List<Ranobe> mValues;
    private final OnListFragmentInteractionListener mListener;
    private Context mContext;
    Constans.FragmentType mFragmentType;


    public RanobeRecyclerViewAdapter(List<Ranobe> items,
            OnListFragmentInteractionListener listener, Constans.FragmentType fragmentType) {
        mValues = items;
        mListener = listener;
        mFragmentType=fragmentType;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.ranobe_item, parent, false);
        mContext = parent.getContext();

        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mTitleView.setText(mValues.get(position).getTitle());

        Glide.with(mContext)
                .load(mValues.get(position).getImage()).apply(
                new RequestOptions().placeholder(R.drawable.ic_adb_black_24dp).error(
                        R.drawable.ic_error_outline_black_24dp)).into(holder.mImageView);

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(mContext, RanobeInfoActivity.class);
                intent.putExtra("RanobeInfo", holder.mItem);
                intent.putExtra("FragmentType", mFragmentType.name());
                mContext.startActivity(intent);


            }
        });


    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        final TextView mTitleView;
        final ImageView mImageView;
        Ranobe mItem;

        ViewHolder(View view) {
            super(view);
            mView = view;

            mTitleView = view.findViewById(R.id.ranobeTitle);
            mImageView = view.findViewById(R.id.imageView);



        }

    }

}
