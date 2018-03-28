package ru.profapp.RanobeReader;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ru.profapp.RanobeReader.Common.StringResources;
import ru.profapp.RanobeReader.DAO.DatabaseDao;
import ru.profapp.RanobeReader.Models.Notify;

/**
 * Created by Ruslan on 26.03.2018.
 */

public class NotificationActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notify);

        setTitle(getResources().getString(R.string.notifications));

        int notificationId = getIntent().getIntExtra(StringResources.EXTRA_ITEM_ID, -1);

        if (notificationId != -1) {
            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.cancel(notificationId);
        }
        Context mContext = getApplicationContext();
        DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(mContext);
        new Thread() {

            @Override
            public void run() {
                List<Notify> notifyList = DatabaseDao.getInstance(
                        getApplicationContext()).getNotifyDao().getAllNotify();

                ListView lvMain = findViewById(R.id.notify_listview);

                ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();
                HashMap<String, String> map;

                for (Notify notify : notifyList) {
                    map = new HashMap<>();
                    map.put("Title", notify.getTitle());
                    map.put("Message", notify.getMessage());
                    map.put("Date", dateFormat.format(notify.getDate()));
                    arrayList.add(map);
                }

                SimpleAdapter adapter = new SimpleAdapter(mContext, arrayList,
                        R.layout.item_notify,
                        new String[]{"Title", "Message", "Date"},
                        new int[]{R.id.notify_title, R.id.notify_message,
                                R.id.notify_date});

                runOnUiThread(() -> lvMain.setAdapter(adapter));

            }
        }.start();

    }


}
