package com.daninic9.ubahnstations;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
import android.widget.ListView;

import com.daninic9.ubahnstations.stations.StationCustomAdapter;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.FormatStrategy;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.PrettyFormatStrategy;

import java.util.ArrayList;
import java.util.List;

//        The app presents the user with a list of UBahn stations (underground stations in Germany).
//        The list shows the name, lat, lon and a small image of each station.
//        The user can filter the list using a string filter at the top of the list.
//        Spinners are shown when the user executes a filter search (this an asynchronous request to the API).
//        You can use the publicly available Graphql API at:
//        https://bahnql.herokuapp.com/graphql


public class MainActivity extends AppCompatActivity {

    private StationCustomAdapter stationCustomAdapter;
    private ListView stations_listview;
    private SwipeRefreshLayout SwipeRefreshLayout;

    public static List<GetStationInfoQuery.StationWithEvaId> stationList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FormatStrategy formatStrategy = PrettyFormatStrategy.newBuilder()
                .showThreadInfo(false)  // (Optional) Whether to show thread info or not. Default true
                .tag("UbahnStationsLogger")   // (Optional) Global tag for every log. Default PRETTY_LOGGER
                .build();
        Logger.addLogAdapter(new AndroidLogAdapter(formatStrategy));
        Logger.i("------------------ STARTING APP ------------------");

        declareViews();

        GraphQlManager graphQlManager = new GraphQlManager(this);
        graphQlManager.run();

    }

    private void declareViews() {
        stations_listview = findViewById(R.id.stations_listview);
        SwipeRefreshLayout = findViewById(R.id.swipeContainer_stations);
    }


    public void infoUpdate() {
        Logger.d("weee" + stationList.size());
        if(!stationList.isEmpty()) {
            runOnUiThread(() -> {
                stationCustomAdapter = new StationCustomAdapter(stationList, this);
                stations_listview.setAdapter(stationCustomAdapter);
                SwipeRefreshLayout.setRefreshing(false);
            });
        }
    }
}
