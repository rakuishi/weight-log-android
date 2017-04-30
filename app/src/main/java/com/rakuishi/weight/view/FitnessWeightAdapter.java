package com.rakuishi.weight.view;

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
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.Field;
import com.rakuishi.weight.R;
import com.rakuishi.weight.util.LocalDateTimeUtil;

import org.threeten.bp.LocalDateTime;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashMap;
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
        ArrayList<Entry> entries;
        float max;
        float min;

        public ChartViewHolder(View itemView) {
            super(itemView);
            entries = new ArrayList<>();
            max = 0f;
            min = 0f;

            chart = (LineChart) itemView.findViewById(R.id.chart);
            chart.getDescription().setEnabled(false);
            chart.setDrawGridBackground(false);
            chart.getAxisRight().setEnabled(false);
            chart.getLegend().setEnabled(false);
            chart.setExtraLeftOffset(4f);

            chart.setPinchZoom(false);
            chart.setTouchEnabled(false);

            chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
            chart.getXAxis().setAxisLineWidth(1f);
            chart.getXAxis().setAxisLineColor(context.getResources().getColor(R.color.divider));
            chart.getXAxis().setLabelCount(5, true);
            chart.getXAxis().setTextSize(12f);
            chart.getXAxis().setTextColor(context.getResources().getColor(R.color.secondary_text));
            chart.getXAxis().setDrawGridLines(false);
            chart.getXAxis().setValueFormatter(new AxisValueFormatter());

            chart.getAxisLeft().setAxisLineWidth(1f);
            chart.getAxisLeft().setAxisLineColor(context.getResources().getColor(R.color.divider));
            chart.getAxisLeft().setGridLineWidth(0.5f);
            chart.getAxisLeft().setGridColor(context.getResources().getColor(R.color.divider));
            chart.getAxisLeft().setTextSize(12f);
            chart.getAxisLeft().setTextColor(context.getResources().getColor(R.color.secondary_text));
            chart.getAxisLeft().setGranularity(0.5f);
            chart.getAxisLeft().setLabelCount(5, true);
        }

        public void render(List<DataPoint> points) {
            if (points == null || points.isEmpty()) {
                return;
            }

            entries = new ArrayList<>();
            max = 0f;
            min = 0f;

            // 基準日
            HashMap<String, Entry> entryHashMap = new HashMap<>();

            for (int i = points.size() - 1; i >= 0; i--) {
                DataPoint point = dataPoints.get(i);
                if (point.getDataType().getFields().size() != 1) {
                    continue;
                }

                LocalDateTime localDateTime = LocalDateTimeUtil.from(point.getTimestamp(TimeUnit.MILLISECONDS));
                String date = LocalDateTimeUtil.formatSimpleLocalDate(localDateTime);
                if (!entryHashMap.containsKey(date)) {
                    Field field = point.getDataType().getFields().get(0);
                    Entry entry = new Entry(i, Float.valueOf(point.getValue(field).toString()), date);
                    entryHashMap.put(date, entry);
                }
            }

            LocalDateTime basisLocalDateTime = LocalDateTime.now().minusMonths(getAmount(spinnerSelectedPosition));

            for (int i = 0; i <= 30 * getAmount(spinnerSelectedPosition); i++) {
                LocalDateTime localDateTime = basisLocalDateTime.plusDays(i);
                String date = LocalDateTimeUtil.formatSimpleLocalDate(localDateTime);

                if (entryHashMap.containsKey(date)) {
                    Entry entry = entryHashMap.get(date);
                    entry.setX(entries.size());
                    entry.setData(date);
                    entries.add(entry);

                    max = (max == 0.f) ? entry.getY() : Math.max(max, entry.getY());
                    min = (min == 0.f) ? entry.getY() : Math.min(min, entry.getY());

                } else if (!entries.isEmpty()) {
                    // HashMap にない場合は直前のデータを再利用し、数字は上書きする
                    Entry entry = entries.get(entries.size() - 1);
                    entry.setX(entries.size());
                    entry.setData(date);
                    entries.add(entry);

                    max = Math.max(max, entry.getY());
                    min = Math.min(min, entry.getY());
                }
            }

            if (entries.isEmpty()) {
                return;
            }

            ArrayList<ILineDataSet> dataSets = new ArrayList<>();
            dataSets.add(getLineDataSet(entries));
            LineData lineData = new LineData(dataSets);

            chart.setData(lineData);
            chart.getAxisLeft().setAxisMaximum((float) Math.ceil(max) + 1f);
            chart.getAxisLeft().setAxisMinimum((float) Math.floor(min) - 1f);
            chart.invalidate();
        }

        public LineDataSet getLineDataSet(List<Entry> entries) {
            LineDataSet lineDataSet = new LineDataSet(entries, "");
            lineDataSet.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
            lineDataSet.setColor(context.getResources().getColor(R.color.colorPrimary));
            lineDataSet.setFillColor(context.getResources().getColor(R.color.colorPrimary));
            lineDataSet.setLineWidth(2f);
            lineDataSet.setDrawCircles(false);
            lineDataSet.setDrawValues(false);
            lineDataSet.setDrawFilled(true);
            return lineDataSet;
        }

        public class AxisValueFormatter implements IAxisValueFormatter {

            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                int position = (int) value;
                return (entries == null || position >= entries.size())
                        ? ""
                        : (String) entries.get(position).getData();
            }
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
