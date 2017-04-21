package com.rakuishi.weight;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.Field;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.text.DateFormat.getDateInstance;

public class FitnessWeightAdapter extends RecyclerView.Adapter<FitnessWeightAdapter.ViewHolder> {

    private Context context;
    private LayoutInflater inflater;
    private List<DataPoint> dataPoints = new ArrayList<>();
    private DateFormat dateFormat = getDateInstance();

    public FitnessWeightAdapter(Context context) {
        this.context = context;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(inflater.inflate(R.layout.view_fitness_weight, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        DataPoint dataPoint = dataPoints.get(position);

        if (dataPoint.getDataType().getFields().size() == 1) {
            // DataType com.google.weight の標準単位は kg
            // https://developers.google.com/fit/android/data-types#public_data_types
            Field field = dataPoint.getDataType().getFields().get(0);
            holder.weightTextView.setText(dataPoint.getValue(field).toString() + context.getString(R.string.unit_kg));
            holder.dateTextView.setText(dateFormat.format(dataPoint.getTimestamp(TimeUnit.MILLISECONDS)));
        }
    }

    @Override
    public int getItemCount() {
        return dataPoints.size();
    }

    public void setDataPoints(List<DataPoint> dataPoints) {
        this.dataPoints = dataPoints;
        notifyDataSetChanged();
    }

    public void clear() {
        dataPoints.clear();
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView weightTextView;
        TextView dateTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            weightTextView = (TextView) itemView.findViewById(R.id.weight_text_view);
            dateTextView = (TextView) itemView.findViewById(R.id.date_text_view);
        }
    }
}
