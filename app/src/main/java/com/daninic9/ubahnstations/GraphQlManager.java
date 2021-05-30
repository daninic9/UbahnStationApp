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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

public class GraphQlManager {

    private final Context context;
    private final ApolloClient apolloClient;
    private int listAddSize;
    private int stationsShown = 0;

    private final List<Integer> idList = new ArrayList<>();

    public GraphQlManager(Context context) {
        this.context = context;

        apolloClient = ApolloClient.builder()
                .serverUrl(Objects.requireNonNull(getGraphqlUrl()))
                .build();
    }

    private String getGraphqlUrl() {
        try {
            InputStream rawResource = context.getResources().openRawResource(R.raw.config);
            Properties properties = new Properties();
            properties.load(rawResource);
            listAddSize = Integer.parseInt(properties.getProperty("listsize"));
            return properties.getProperty("graphqlserver_url");
        } catch (Resources.NotFoundException e) {
            Logger.e("Unable to find the config file: " + e.getMessage());
        } catch (IOException e) {
            Logger.e("Failed to open config file.");
        }
        return null;
    }

    public void initList(int max) {
        List<Integer> toDelete = new ArrayList<>();
        if (!idList.isEmpty()) {
            for (Integer id : idList) {
                getStationContent(id);
                toDelete.add(id);
                stationsShown ++;
                if (stationsShown >= max + listAddSize) {
                    break;
                }
            }
            idList.removeAll(toDelete);
        }
    }

    public void addMore() {
        initList(stationsShown);
    }

    public void cleanList() {
        idList.clear();
        stationsShown = 0;
    }

    public void queryGetAll() {
        apolloClient.query(new GetAllStationsQuery())
                .enqueue(new ApolloCall.Callback<GetAllStationsQuery.Data>() {
                    @Override
                    public void onResponse(@NotNull Response<GetAllStationsQuery.Data> response) {
                        if (response.getData() != null) {
                            for (GetAllStationsQuery.Station station : response.getData().search.stations) {
                                if (station.primaryEvaId != null) {
                                    idList.add(station.primaryEvaId);
                                }
                            }
                            initList(0);
                        }
                    }
                    @Override
                    public void onFailure(@NotNull ApolloException e) {
                        Logger.e("Error " + e.getLocalizedMessage());
                        ((MainActivity) context).sendWarningError();
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

