package ru.profapp.RanobeReader;

import android.content.Context;
import android.content.SharedPreferences;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.util.List;

import io.fabric.sdk.android.Fabric;
import ru.profapp.RanobeReader.Common.RanobeConstans;
import ru.profapp.RanobeReader.Common.ThemeUtils;
import ru.profapp.RanobeReader.DAO.DatabaseDao;
import ru.profapp.RanobeReader.Helpers.RanobeKeeper;
import ru.profapp.RanobeReader.Models.Chapter;
import ru.profapp.RanobeReader.Models.Ranobe;

public class RanobeInfoActivity extends AppCompatActivity {

    private static final String TAG = "RanobeInfoActivity";
    private Ranobe mCurrentRanobe;
    private Context mContext;
    private FloatingActionButton favoriteButton;
    private Drawable borderImage;
    private Drawable fillImage;
    private ChapterRecyclerViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        ThemeUtils.onActivityCreateSetTheme(this,true);
        setContentView(R.layout.activity_ranobe_info);

        int loadedChapterCount = RanobeConstans.chapterCount;

        MobileAds.initialize(this, "ca-app-pub-1483315001492731~8531946315");
        AdView adView = findViewById(R.id.adView);
      //  AdRequest adRequest = new AdRequest.Builder().build();
        AdRequest adRequest = new AdRequest.Builder().addTestDevice("sdfsdf").build();

        adView.loadAd(adRequest);
        Button loadButton = findViewById(R.id.loadButton);
        borderImage = getResources().getDrawable(R.drawable.ic_favorite_border_black_24dp);
        fillImage = getResources().getDrawable(R.drawable.ic_favorite_black_24dp);
        Drawable downloadDoneImage = getResources().getDrawable(
                R.drawable.ic_cloud_done_black_24dp);
        NestedScrollView nestedScrollView = findViewById(R.id.ranobe_info_NestedScrollView);

        FloatingActionButton fab = findViewById(R.id.fav_toTop_fab);
        fab.setOnClickListener(view -> nestedScrollView.scrollTo(0, 0));

        Toolbar toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        favoriteButton = findViewById(R.id.fav_fab);
        TextView aboutTextView = findViewById(R.id.text_about);
        TextView additionalInfoTextView = findViewById(R.id.text_info);
        CardView infoCard = findViewById(R.id.ranobe_info_card);
        CardView descriptionCard = findViewById(R.id.ranobe_description_card);

        mContext = getApplicationContext();
        mCurrentRanobe = RanobeKeeper.getInstance().getRanobe();
        RanobeConstans.FragmentType fragmentType = RanobeConstans.FragmentType.valueOf(
                getIntent().getStringExtra("FragmentType"));
        getSupportActionBar().setTitle(mCurrentRanobe.getTitle());
        initFavoriteButton();
        favoriteButton.setOnClickListener(v -> {
            Drawable.ConstantState state =
                    favoriteButton.getDrawable().getConstantState();
            if (state.equals(borderImage.getConstantState())) {
                favoriteButton.setImageDrawable(fillImage);
                mCurrentRanobe.setFavorited(true);

                AsyncTask.execute(() -> {
                    DatabaseDao.getInstance(mContext).getRanobeDao().insert(mCurrentRanobe);
                    DatabaseDao.getInstance(mContext).getChapterDao().insertAll(
                            mCurrentRanobe.getChapterList().toArray(
                                    new Chapter[mCurrentRanobe.getChapterList().size()]));
                });

            } else {
                favoriteButton.setImageDrawable(borderImage);
                mCurrentRanobe.setFavorited(false);
                AsyncTask.execute(
                        () -> DatabaseDao.getInstance(mContext).getRanobeDao().delete(mCurrentRanobe));
            }

        });

        if (!mCurrentRanobe.getWasUpdated()) {
            mCurrentRanobe.updateRanobe(mContext);
        }

        if (mCurrentRanobe.getDescription() != null) {
            descriptionCard.setVisibility(View.VISIBLE);
        }
        if (mCurrentRanobe.getAdditionalInfo() != null) {
            infoCard.setVisibility(View.VISIBLE);
        }
        ImageView imageView = findViewById(R.id.main_logoimage);
        RequestOptions myOptions = new RequestOptions()
                .placeholder(R.drawable.ic_adb_black_24dp)
                .error(R.drawable.ic_error_outline_black_24dp)
                .fitCenter();

        Glide.with(mContext).load(mCurrentRanobe.getImage())
                .apply(myOptions)
                .into(imageView);

        aboutTextView.setText(mCurrentRanobe.getDescription());
        additionalInfoTextView.setText(mCurrentRanobe.getAdditionalInfo());

        SharedPreferences sPref = getSharedPreferences(
                mCurrentRanobe.getUrl().replaceAll("[^a-zA-Z0-9]", ""),
                MODE_PRIVATE);

        List<Chapter> recycleChapterList = mCurrentRanobe.getChapterList();
        int size = recycleChapterList.size();
        if (size > loadedChapterCount) {
            loadButton.setVisibility(View.VISIBLE);
            loadButton.setOnClickListener(v -> Toast.makeText(mContext, "dfsdfsdfsd", Toast.LENGTH_SHORT).show());
        }

        recycleChapterList = recycleChapterList.subList(0, Math.min(loadedChapterCount,size));
        if (sPref != null) {
            Object[] allReadedChapters = sPref.getAll().keySet().toArray();

            for (Object readed : allReadedChapters) {
                for (Chapter chapter : recycleChapterList) {
                    if (chapter.getUrl().equals(readed.toString())) {
                        chapter.setReaded(true);
                        break;
                    }
                }
            }
        }

        RecyclerView recyclerView = findViewById(R.id.chapter_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ChapterRecyclerViewAdapter(recycleChapterList, mContext);

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
        AsyncTask.execute(() -> {
            if (DatabaseDao.getInstance(mContext).getRanobeDao().IsRanobeFavorite(
                    mCurrentRanobe.getUrl())
                    != null) {
                favoriteButton.setImageDrawable(fillImage);
            } else {
                favoriteButton.setImageDrawable(borderImage);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
    }

}
