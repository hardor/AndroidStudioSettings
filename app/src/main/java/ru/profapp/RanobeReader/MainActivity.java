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
import android.view.View;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import io.fabric.sdk.android.Fabric;
import ru.profapp.RanobeReader.Common.RanobeConstans;
import ru.profapp.RanobeReader.Common.StringResources;
import ru.profapp.RanobeReader.Common.ThemeUtils;
import ru.profapp.RanobeReader.Helpers.RanobeKeeper;
import ru.profapp.RanobeReader.Models.Ranobe;

public class MainActivity extends AppCompatActivity
        implements
        RanobeRecyclerFragment.OnListFragmentInteractionListener,
        SearchFragment.OnFragmentInteractionListener,
        NavigationView.OnNavigationItemSelectedListener

{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        initSettingPreference();
        ThemeUtils.onActivityCreateSetTheme(this,true);

        setContentView(R.layout.activity_main);

        MobileAds.initialize(this, getString(R.string.app_admob_id));
        AdView adView = findViewById(R.id.adView);
        //  AdRequest adRequest = new AdRequest.Builder().build();
        AdRequest adRequest = new AdRequest.Builder().addTestDevice("sdfsdf").build();
        adView.loadAd(adRequest);

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
                e.printStackTrace();
                Crashlytics.logException(e);
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
                settingPref.getBoolean( getApplicationContext().getString(R.string.pref_general_hide_chapter), false));


        ThemeUtils.setTheme( settingPref.getBoolean(
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

        if (id == R.id.nav_favorite) {
            fragment = RanobeRecyclerFragment.newInstance(
                    RanobeConstans.FragmentType.Favorite.name());
            setTitle(getResources().getText(R.string.favorite));
        } else if (id == R.id.nav_rulate) {
            fragment = RanobeRecyclerFragment.newInstance(
                    RanobeConstans.FragmentType.Rulate.name());
            setTitle(getResources().getText(R.string.tl_rulate_name));
        } else if (id == R.id.nav_ranoberf) {
            fragment = RanobeRecyclerFragment.newInstance(
                    RanobeConstans.FragmentType.Ranoberf.name());
            setTitle(getResources().getText(R.string.ranobe_rf));
        } else if (id == R.id.nav_manage) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_search) {
            fragment = SearchFragment.newInstance();
            setTitle(getResources().getText(R.string.search));
        }

        if (fragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.mainFrame, fragment);
            ft.commit();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onListFragmentInteraction(Ranobe item) {

    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
