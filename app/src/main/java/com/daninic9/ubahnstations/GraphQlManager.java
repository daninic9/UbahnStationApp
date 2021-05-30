package com.daninic9.ubahnstations;

import android.content.Context;
import android.content.res.Resources;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.orhanobut.logger.Logger;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

public class GraphQlManager implements Runnable {

    private final Context context;
    private boolean isAlive = true;
    private ApolloClient apolloClient;
    private int listSize;

    public GraphQlManager(Context context) {
        this.context = context;
    }

    @Override
    public void run() {
        apolloClient = ApolloClient.builder()
                .serverUrl(Objects.requireNonNull(getGraphqlUrl()))
                .build();

        queryGetAll();
    }

    private String getGraphqlUrl() {
        try {
            InputStream rawResource = context.getResources().openRawResource(R.raw.config);
            Properties properties = new Properties();
            properties.load(rawResource);
            listSize = Integer.parseInt(properties.getProperty("listsize"));
            return properties.getProperty("graphqlserver_url");
        } catch (Resources.NotFoundException e) {
            Logger.e("Unable to find the config file: " + e.getMessage());
        } catch (IOException e) {
            Logger.e("Failed to open config file.");
        }
        return null;
    }

    private void queryGetAll() {
        apolloClient.query(new GetAllStationsQuery())
                .enqueue(new ApolloCall.Callback<GetAllStationsQuery.Data>() {
                    @Override
                    public void onResponse(@NotNull Response<GetAllStationsQuery.Data> response) {
                        if (response.getData() != null) {
                            int count = 0;
                            for (GetAllStationsQuery.Station station : response.getData().search.stations) {
                                if (station.primaryEvaId != null) {
                                    getStationContent(station.primaryEvaId);
                                    count++;
                                }
                                if (count >= listSize) {
                                    break;
                                }
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NotNull ApolloException e) {
                        Logger.e("Error " + e.getLocalizedMessage());
                        queryGetAll();
                    }
                });
    }

    private void getStationContent(int id) {
        apolloClient.query(new GetStationInfoQuery(id))
                .enqueue(new ApolloCall.Callback<GetStationInfoQuery.Data>() {
                    @Override
                    public void onResponse(@NotNull Response<GetStationInfoQuery.Data> response) {
                        if (response.getData() != null && response.getData().stationWithEvaId != null) {
                            MainActivity.stationList.add(response.getData().stationWithEvaId);
                            Logger.d(Objects.requireNonNull(response.getData().stationWithEvaId).name);
                            ((MainActivity) context).infoUpdate();
                        }
                    }

                    @Override
                    public void onFailure(@NotNull ApolloException e) {
                        Logger.e("Error " + e.getLocalizedMessage());
                    }
                });
    }


}

