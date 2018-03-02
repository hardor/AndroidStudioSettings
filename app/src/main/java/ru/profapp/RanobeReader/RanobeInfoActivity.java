package ru.profapp.RanobeReader;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import ru.profapp.RanobeReader.Common.Constans;
import ru.profapp.RanobeReader.DAO.DatabaseDao;
import ru.profapp.RanobeReader.Models.Chapter;
import ru.profapp.RanobeReader.Models.Ranobe;

public class RanobeInfoActivity extends AppCompatActivity {

    Ranobe mCurrentRanobe;
    Context mContext;
    ImageView mImageView;
    Toolbar mToolbar;
    Constans.FragmentType mFragmentType;
    TextView mAboutTextView, mAdditionalInfoTextView;
    FloatingActionButton favoriteButton;
    Drawable borderImage, fillImage, downloadImage, downloadDoneImage;
    CardView infoCard, descriptionCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranobe_info);

        borderImage = getResources().getDrawable(R.drawable.ic_favorite_border_black_24dp);
        fillImage = getResources().getDrawable(R.drawable.ic_favorite_black_24dp);
        downloadImage = getResources().getDrawable(R.drawable.ic_cloud_download_black_24dp);
        downloadDoneImage = getResources().getDrawable(R.drawable.ic_cloud_done_black_24dp);
        NestedScrollView nestedScrollView =  findViewById(R.id.ranobe_info_NestedScrollView);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fav_toTop_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nestedScrollView.scrollTo(0,0);
            }
        });

        mToolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        favoriteButton = findViewById(R.id.fav_fab);
        mAboutTextView = findViewById(R.id.text_about);
        mAdditionalInfoTextView = findViewById(R.id.text_info);
        infoCard = findViewById(R.id.ranobe_info_card);
        descriptionCard = findViewById(R.id.ranobe_description_card);

        mContext = getApplicationContext();
        mCurrentRanobe = getIntent().getParcelableExtra("RanobeInfo");
        mFragmentType = Constans.FragmentType.valueOf(getIntent().getStringExtra("FragmentType"));
        getSupportActionBar().setTitle(mCurrentRanobe.getTitle());
        initFavoriteButton();
        favoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Drawable.ConstantState state =
                        favoriteButton.getDrawable().getConstantState();
                if (state.equals(borderImage.getConstantState())) {
                    favoriteButton.setImageDrawable(fillImage);
                    mCurrentRanobe.setFavorited(true);

                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            DatabaseDao.getInstance(mContext).getRanobeDao().insert(mCurrentRanobe);
                            DatabaseDao.getInstance(mContext).getChapterDao().insertAll(
                                    mCurrentRanobe.getChapterList().toArray(
                                            new Chapter[mCurrentRanobe.getChapterList().size()]));
                        }
                    });

                } else {
                    favoriteButton.setImageDrawable(borderImage);
                    mCurrentRanobe.setFavorited(false);
                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            DatabaseDao.getInstance(mContext).getRanobeDao().delete(mCurrentRanobe);
                        }
                    });
                }

            }
        });

        mCurrentRanobe.updateRanobe(mContext);

        if (mCurrentRanobe.getDescription() != null) {
            descriptionCard.setVisibility(View.VISIBLE);
        }
        if (mCurrentRanobe.getAdditionalInfo() != null) {
            infoCard.setVisibility(View.VISIBLE);
        }
        mImageView = findViewById(R.id.main_logoimage);
        Glide.with(mContext).load(mCurrentRanobe.getImage()).into(mImageView);

        mAboutTextView.setText(mCurrentRanobe.getDescription());
        mAdditionalInfoTextView.setText(mCurrentRanobe.getAdditionalInfo());
        RecyclerView recyclerView = findViewById(R.id.chapter_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        ChapterRecyclerViewAdapter adapter = new ChapterRecyclerViewAdapter(mCurrentRanobe,mContext);
        adapter.setDownloadImage(downloadImage);
        adapter.setDownloadDoneImage(downloadDoneImage);
        recyclerView.setAdapter(adapter);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initFavoriteButton() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                if (DatabaseDao.getInstance(mContext).getRanobeDao().IsRanobeFavorite(
                        mCurrentRanobe.getUrl())
                        != null) {
                    favoriteButton.setImageDrawable(fillImage);
                } else {
                    favoriteButton.setImageDrawable(borderImage);
                }
            }
        });
    }

}
