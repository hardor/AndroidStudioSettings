package ru.profapp.RanobeReader;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import io.fabric.sdk.android.Fabric;
import ru.profapp.RanobeReader.Common.RanobeConstans;
import ru.profapp.RanobeReader.Common.StringResources;
import ru.profapp.RanobeReader.Common.ThemeUtils;
import ru.profapp.RanobeReader.Helpers.MyLog;
import ru.profapp.RanobeReader.Helpers.RanobeKeeper;

public class MainActivity extends AppCompatActivity
        implements
        RanobeRecyclerFragment.OnListFragmentInteractionListener,
        SearchFragment.OnFragmentInteractionListener,
        NavigationView.OnNavigationItemSelectedListener

{

    private final Thread.UncaughtExceptionHandler ExceptionHandler =
            (th, ex) -> MyLog.SendError(StringResources.LogType.WARN, "MainActivity",
                    "Uncaught exception", ex);
    private AdView adView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set up Crashlytics, disabled for debug builds
        Crashlytics crashlyticsKit = new Crashlytics.Builder()
                .core(new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build())
                .build();

        Fabric.with(this, crashlyticsKit, new Crashlytics());
        initSettingPreference();
        ThemeUtils.onActivityCreateSetTheme(this, true);

        Thread.setDefaultUncaughtExceptionHandler(ExceptionHandler);
        setContentView(R.layout.activity_main);

        MobileAds.initialize(this, getString(R.string.app_admob_id));
        adView = findViewById(R.id.adView);

        AdRequest.Builder adRequest = new AdRequest.Builder();

        if (BuildConfig.DEBUG) {
            adRequest.addTestDevice("sdfsdf");
        }

        adView.loadAd(adRequest.build());

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.mainFrame,
                RanobeRecyclerFragment.newInstance(RanobeConstans.FragmentType.Favorite.name()));
        ft.commit();
        setTitle(getResources().getText(R.string.favorite));

        FloatingActionButton floatingActionButton = findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(view -> {
            try {
                RecyclerView ranobeListview = findViewById(R.id.ranobeListView);
                ranobeListview.scrollToPosition(0);
            } catch (NullPointerException e) {
                MyLog.SendError(StringResources.LogType.WARN, MainActivity.class.toString(), "", e);

            }

        });

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

    }

    private void initSettingPreference() {
        PreferenceManager.setDefaultValues(this, R.xml.pref_general, false);
        SharedPreferences settingPref = PreferenceManager.getDefaultSharedPreferences(
                getApplicationContext());
        RanobeKeeper.getInstance().setChapterTextSize(
                settingPref.getInt(
                        getApplicationContext().getString(R.string.pref_general_text_size), 13));

        RanobeKeeper.getInstance().setHideUnavailableChapters(
                settingPref.getBoolean(
                        getApplicationContext().getString(R.string.pref_general_hide_chapter),
                        false));
        RanobeKeeper.getInstance().setAutoSaveText(
                settingPref.getBoolean(
                        getApplicationContext().getString(R.string.pref_general_auto_save),
                        false));

        ThemeUtils.setTheme(settingPref.getBoolean(
                getApplicationContext().getString(R.string.pref_general_app_theme), false));


    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            setTitle(getResources().getText(R.string.action_settings));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        Fragment fragment = null;

        switch (id) {
            case R.id.nav_favorite:
                fragment = RanobeRecyclerFragment.newInstance(
                        RanobeConstans.FragmentType.Favorite.name());
                setTitle(getResources().getText(R.string.favorite));
                break;
            case R.id.nav_rulate:
                fragment = RanobeRecyclerFragment.newInstance(
                        RanobeConstans.FragmentType.Rulate.name());
                setTitle(getResources().getText(R.string.tl_rulate_name));
                break;
            case R.id.nav_ranoberf:
                fragment = RanobeRecyclerFragment.newInstance(
                        RanobeConstans.FragmentType.Ranoberf.name());
                setTitle(getResources().getText(R.string.ranobe_rf));
                break;
            case R.id.nav_manage: {
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.nav_search:
                fragment = SearchFragment.newInstance();
                setTitle(getResources().getText(R.string.search));
                break;
            case R.id.nav_chapters:
                //  fragment = SavedChaptersFragment.newInstance();
                fragment = RanobeRecyclerFragment.newInstance(
                        RanobeConstans.FragmentType.History.name());
                setTitle(getResources().getText(R.string.saved_chapters));

                break;
            case R.id.nav_send: {
                Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto", "admin@profapp.ru", null));
                startActivity(Intent.createChooser(intent, "Choose an Email client :"));

                break;
            }
        }

        if (fragment != null) {
            adView.loadAd(new AdRequest.Builder().build());

            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.mainFrame, fragment);
            ft.commit();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

}
