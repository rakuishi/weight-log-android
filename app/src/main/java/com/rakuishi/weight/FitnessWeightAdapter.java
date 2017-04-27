package com.rakuishi.weight;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.Field;

import org.threeten.bp.LocalDateTime;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.text.DateFormat.getDateInstance;

public class FitnessWeightAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface Callback {
        void onClickDataPoint(DataPoint dataPoint);
    }

    private Context context;
    private LayoutInflater inflater;
    private List<DataPoint> dataPoints = new ArrayList<>();
    private DateFormat dateFormat = getDateInstance();
    private Callback callback;

    public FitnessWeightAdapter(Context context, Callback callback) {
        this.context = context;
        this.callback = callback;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getItemViewType(int position) {
        switch (position) {
            case 0:
                return 0;
            default:
                return 1;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case 0:
                return new SpinnerViewHolder(inflater.inflate(R.layout.view_spinner, parent, false));
            default:
                return new DataViewHolder(inflater.inflate(R.layout.view_fitness_weight, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int viewType = getItemViewType(position);

        switch (viewType) {
            case 0: {
                SpinnerViewHolder holder2 = (SpinnerViewHolder) holder;
                holder2.spinner.setAdapter(getSpinnerArrayAdapter());
                break;
            }
            default: {
                DataPoint dataPoint = dataPoints.get(position - 1);
                DataViewHolder holder1 = (DataViewHolder) holder;

                if (dataPoint.getDataType().getFields().size() == 1) {
                    // DataType com.google.weight の標準単位は kg
                    // https://developers.google.com/fit/android/data-types#public_data_types
                    Field field = dataPoint.getDataType().getFields().get(0);
                    holder1.weightTextView.setText(dataPoint.getValue(field).toString() + context.getString(R.string.unit_kg));
                    holder1.dateTextView.setText(dateFormat.format(dataPoint.getTimestamp(TimeUnit.MILLISECONDS)));
                    holder1.itemView.setOnClickListener(v -> callback.onClickDataPoint(dataPoint));
                }
                break;
            }
        }
    }

    @Override
    public int getItemCount() {
        return dataPoints.isEmpty() ? 0 : dataPoints.size() + 1;
    }

    public void setDataPoints(List<DataPoint> dataPoints) {
        this.dataPoints = dataPoints;
        notifyDataSetChanged();
    }

    private ArrayAdapter<String> getSpinnerArrayAdapter() {
        String items[] = {getDateRange(1), getDateRange(2), getDateRange(3), getDateRange(6)};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return adapter;
    }

    private String getDateRange(int amount) {
        LocalDateTime localDateTime = LocalDateTime.now();
        String start = LocalDateTimeUtil.formatLocalizedDate(localDateTime);
        String end = LocalDateTimeUtil.formatLocalizedDate(localDateTime.minusMonths(amount));
        return String.format(context.getString(R.string.date_range_format), 30 * amount, start, end);
    }

    class SpinnerViewHolder extends RecyclerView.ViewHolder {

        Spinner spinner;

        public SpinnerViewHolder(View itemView) {
            super(itemView);
            spinner = (Spinner) itemView.findViewById(R.id.spinner);
        }
    }

    class DataViewHolder extends RecyclerView.ViewHolder {

        TextView weightTextView;
        TextView dateTextView;

        public DataViewHolder(View itemView) {
            super(itemView);
            weightTextView = (TextView) itemView.findViewById(R.id.weight_text_view);
            dateTextView = (TextView) itemView.findViewById(R.id.date_text_view);
        }
    }
}
