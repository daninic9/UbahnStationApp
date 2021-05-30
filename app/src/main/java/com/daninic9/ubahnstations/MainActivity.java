package com.daninic9.ubahnstations;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;

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
    private RecyclerView stations_listview;
    private SwipeRefreshLayout swipeRefreshLayout;

    private GraphQlManager graphQlManager;

    public static List<GetStationInfoQuery.StationWithEvaId> stationList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_main);


        FormatStrategy formatStrategy = PrettyFormatStrategy.newBuilder()
                .showThreadInfo(false)  // (Optional) Whether to show thread info or not. Default true
                .tag("UbahnStationsLogger")   // (Optional) Global tag for every log. Default PRETTY_LOGGER
                .build();
        Logger.addLogAdapter(new AndroidLogAdapter(formatStrategy));
        Logger.i("------------------ STARTING APP ------------------");

        declareViews();

        swipeRefreshLayout.setRefreshing(true);
        graphQlManager = new GraphQlManager(this);
        graphQlManager.run();

    }

    private void declareViews() {
        stations_listview = findViewById(R.id.stations_listview);
        stations_listview.setLayoutManager(new LinearLayoutManager(this));

        swipeRefreshLayout = findViewById(R.id.swipeContainer_stations);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            if (!stationList.isEmpty()) {
                stationList.clear();
            }
            graphQlManager.run();
        });
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
    }


    public void infoUpdate() {
        if(!stationList.isEmpty()) {
            runOnUiThread(() -> {
                stationCustomAdapter = new StationCustomAdapter(stationList, this);
                stationCustomAdapter.setClickListener((view, position) -> {
                    if (!swipeRefreshLayout.isRefreshing()) {
                        Logger.d("Clicked " + stationCustomAdapter.getItem(position).name);
                    }

                });
                stations_listview.setAdapter(stationCustomAdapter);
                swipeRefreshLayout.setRefreshing(false);
            });
        }
    }
}
