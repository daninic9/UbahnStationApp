package com.daninic9.ubahnstations;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Objects;


public class StationCustomAdapter extends RecyclerView.Adapter<StationCustomAdapter.ViewHolder> {
    private final List<GetStationInfoQuery.StationWithEvaId> dataSet;
    private final LayoutInflater inflater;
    private final Context context;

    private ItemClickListener mClickListener;

    public StationCustomAdapter(List<GetStationInfoQuery.StationWithEvaId> data, Context context) {
        this.inflater = LayoutInflater.from(context);
        this.dataSet = data;
        this.context = context;
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_station, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ViewHolder holder, int position) {
        GetStationInfoQuery.StationWithEvaId station = dataSet.get(position);
        holder.stationnameTextview.setText(station.name());
        String location = "(" + Objects.requireNonNull(station.location()).latitude() + "," +
                Objects.requireNonNull(station.location()).longitude() + ")";
        holder.locTextview.setText(location);

        if (station.picture() != null && !Objects.requireNonNull(station.picture()).url().isEmpty()) {
            new Thread(() -> {
                try {
                    URL newUrl = new URL(Objects.requireNonNull(station.picture()).url());
                    Bitmap bitmap = BitmapFactory.decodeStream(newUrl.openConnection().getInputStream());
                    ((MainActivity) context).runOnUiThread(() -> holder.stationImage.setImageBitmap(bitmap));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

        } else {
            holder.stationImage.setImageResource(R.drawable.noimage);
        }

    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    // View lookup cache
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView stationnameTextview;
        TextView locTextview;
        ImageView stationImage;
        ConstraintLayout itemContainer;

        public ViewHolder(View view) {
            super(view);
            stationnameTextview = view.findViewById(R.id.stationname_textview);
            locTextview = view.findViewById(R.id.loc_textview);
            stationImage = view.findViewById(R.id.station_image);
            itemContainer = view.findViewById(R.id.itemContainer);
            itemContainer.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // convenience method for getting data at click position
    public GetStationInfoQuery.StationWithEvaId getItem(int id) {
        return dataSet.get(id);
    }

    // allows clicks events to be caught
    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
    
}


