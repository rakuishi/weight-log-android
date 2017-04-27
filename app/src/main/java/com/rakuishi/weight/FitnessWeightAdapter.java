package com.rakuishi.weight;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
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
        void onDataPointClicked(DataPoint dataPoint);

        void onSpinnerItemSelected(int amount);
    }

    private Context context;
    private LayoutInflater inflater;
    private List<DataPoint> dataPoints = new ArrayList<>();
    private DateFormat dateFormat = getDateInstance();
    private Callback callback;
    private int spinnerSelectedPosition;

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
            case 1:
                return 1;
            default:
                return 2;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case 0:
                return new SpinnerViewHolder(inflater.inflate(R.layout.view_spinner, parent, false));
            case 1:
                return new ChartViewHolder(inflater.inflate(R.layout.view_chart, parent, false));
            default:
                return new DataViewHolder(inflater.inflate(R.layout.view_fitness_weight, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int viewType = getItemViewType(position);

        switch (viewType) {
            case 0: {
                ((SpinnerViewHolder) holder).render();
                break;
            }
            case 1: {
                ((ChartViewHolder) holder).render(dataPoints);
                break;
            }
            default: {
                ((DataViewHolder) holder).render(dataPoints.get(position - 2));
                break;
            }
        }
    }

    @Override
    public int getItemCount() {
        return dataPoints.isEmpty() ? 0 : dataPoints.size() + 2;
    }

    public void setDataPoints(List<DataPoint> dataPoints) {
        this.dataPoints = dataPoints;
        notifyDataSetChanged();
    }

    private int getAmount(int position) {
        switch (position) {
            case 0:
                return 1;
            case 1:
                return 2;
            case 2:
                return 3;
            default:
                return 6;
        }
    }

    private ArrayAdapter<String> getSpinnerArrayAdapter() {
        String items[] = {getDateRange(getAmount(0)), getDateRange(getAmount(1)), getDateRange(getAmount(2)), getDateRange(getAmount(3))};
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

        // スピナーの初期設定時に `onItemSelected()` が発火するのを防ぐ
        private int spinnerSelectedCount;
        Spinner spinner;

        public SpinnerViewHolder(View itemView) {
            super(itemView);
            spinner = (Spinner) itemView.findViewById(R.id.spinner);
        }

        public void render() {
            spinnerSelectedCount = 0;
            spinner.setAdapter(getSpinnerArrayAdapter());
            spinner.setSelection(spinnerSelectedPosition);
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (++spinnerSelectedCount > 1) {
                        spinnerSelectedPosition = position;
                        callback.onSpinnerItemSelected(getAmount(position));
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }
    }

    class ChartViewHolder extends RecyclerView.ViewHolder {

        LineChart chart;

        public ChartViewHolder(View itemView) {
            super(itemView);
            chart = (LineChart) itemView.findViewById(R.id.chart);
            chart.getDescription().setEnabled(false);
            chart.setDrawGridBackground(false);
            chart.getAxisRight().setEnabled(false);
            chart.getLegend().setEnabled(false);

            chart.setPinchZoom(false);
            chart.setTouchEnabled(false);

            chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
            chart.getXAxis().setAxisLineWidth(1f);
            chart.getXAxis().setAxisLineColor(context.getResources().getColor(R.color.divider));
            chart.getXAxis().setLabelCount(4);
            chart.getXAxis().setTextSize(12f);
            chart.getXAxis().setTextColor(context.getResources().getColor(R.color.secondary_text));
            chart.getXAxis().setDrawGridLines(false);

            chart.getAxisLeft().setAxisLineWidth(1f);
            chart.getAxisLeft().setAxisLineColor(context.getResources().getColor(R.color.divider));
            chart.getAxisLeft().setGridLineWidth(0.5f);
            chart.getAxisLeft().setGridColor(context.getResources().getColor(R.color.divider));
            chart.getAxisLeft().setTextSize(12f);
            chart.getAxisLeft().setTextColor(context.getResources().getColor(R.color.secondary_text));
            chart.getAxisLeft().setGranularity(0.25f);
            chart.getAxisLeft().setLabelCount(4);
        }

        public void render(List<DataPoint> points) {
            ArrayList<Entry> values = new ArrayList<>();

            int i = 0;
            for (DataPoint point : points) {
                if (point.getDataType().getFields().size() != 1) {
                    continue;
                }

                Field field = point.getDataType().getFields().get(0);
                values.add(new Entry(i, Float.valueOf(point.getValue(field).toString())));
                i++;
            }

            ArrayList<ILineDataSet> dataSets = new ArrayList<>();
            dataSets.add(getLineDataSet(values));
            LineData lineData = new LineData(dataSets);
            chart.setData(lineData);
            chart.invalidate();
        }

        public LineDataSet getLineDataSet(List<Entry> entries) {
            LineDataSet lineDataSet = new LineDataSet(entries, "");
            lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            lineDataSet.setColor(context.getResources().getColor(R.color.colorPrimary));
            lineDataSet.setFillColor(context.getResources().getColor(R.color.colorPrimary));
            lineDataSet.setCubicIntensity(0.2f);
            lineDataSet.setLineWidth(2f);
            lineDataSet.setDrawCircles(false);
            lineDataSet.setDrawValues(false);
            lineDataSet.setDrawFilled(true);
            return lineDataSet;
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

        public void render(DataPoint dataPoint) {
            if (dataPoint.getDataType().getFields().size() == 1) {
                // DataType com.google.weight の標準単位は kg
                // https://developers.google.com/fit/android/data-types#public_data_types
                Field field = dataPoint.getDataType().getFields().get(0);
                weightTextView.setText(dataPoint.getValue(field).toString() + context.getString(R.string.unit_kg));
                dateTextView.setText(dateFormat.format(dataPoint.getTimestamp(TimeUnit.MILLISECONDS)));
                itemView.setOnClickListener(v -> callback.onDataPointClicked(dataPoint));
            }
        }
    }
}
