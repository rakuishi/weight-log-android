package com.rakuishi.weight.view;

import android.content.Context;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.gms.fitness.data.DataPoint;
import com.rakuishi.weight.R;
import com.rakuishi.weight.util.DataPointUtil;
import com.rakuishi.weight.util.DensityUtil;
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
    }

    private final static int VIEW_TYPE_CHART = 1;
    private final static int VIEW_TYPE_SHADOW = 2;
    private final static int VIEW_TYPE_DATA_POINT = 3;

    private Context context;
    private LayoutInflater inflater;
    private List<DataPoint> dataPoints = new ArrayList<>();
    private DateFormat dateFormat = getDateInstance();
    private Callback callback;
    private int amount;

    public FitnessWeightAdapter(Context context, Callback callback, int amount) {
        this.context = context;
        this.callback = callback;
        this.amount = amount;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return VIEW_TYPE_CHART;
        } else if (position == 1 || position == getItemCount() - 1) {
            return VIEW_TYPE_SHADOW;
        } else {
            return VIEW_TYPE_DATA_POINT;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_CHART:
                return new ChartViewHolder(inflater.inflate(R.layout.view_chart, parent, false));
            case VIEW_TYPE_SHADOW:
                return new ShadowViewHolder(inflater.inflate(R.layout.view_shadow_spacer, parent, false));
            default:
                return new DataViewHolder(inflater.inflate(R.layout.view_fitness_weight, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int viewType = getItemViewType(position);

        switch (viewType) {
            case VIEW_TYPE_CHART:
                ((ChartViewHolder) holder).render(dataPoints);
                break;
            case VIEW_TYPE_SHADOW:
                break;
            default:
                int dataPosition = position - 2;
                ((DataViewHolder) holder).render(dataPoints.get(dataPosition), dataPosition == 0);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return dataPoints.isEmpty()
                ? 0
                : dataPoints.size() + 3; // chart + shadow + shadow
    }

    public void setDataPoints(List<DataPoint> dataPoints) {
        this.dataPoints = dataPoints;
        notifyDataSetChanged();
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    private class ChartViewHolder extends RecyclerView.ViewHolder {

        private static final int MAX_X_AXIS_LABEL_COUNT = 5;

        TextView weightTextView;
        TextView dateTextView;
        TextView vsPercentTextView;
        TextView vsDateTextView;

        LineChart chart;
        ArrayList<Entry> entries;
        float max;
        float min;

        ChartViewHolder(View itemView) {
            super(itemView);
            entries = new ArrayList<>();
            max = 0f;
            min = 0f;

            weightTextView = (TextView) itemView.findViewById(R.id.last_weight_text_view);
            dateTextView = (TextView) itemView.findViewById(R.id.last_date_text_view);
            vsPercentTextView = (TextView) itemView.findViewById(R.id.vs_percent_text_view);
            vsDateTextView = (TextView) itemView.findViewById(R.id.vs_date_text_view);

            chart = (LineChart) itemView.findViewById(R.id.chart);

            chart.setNoDataText(context.getString(R.string.not_enough_data));
            chart.setNoDataTextColor(ContextCompat.getColor(context, R.color.secondary_text));
            Paint paint = chart.getPaint(Chart.PAINT_INFO);
            paint.setTextSize(DensityUtil.dp2Px(context, 14f));

            chart.getDescription().setEnabled(false);
            chart.setDrawGridBackground(false);
            chart.getAxisRight().setEnabled(false);
            chart.getLegend().setEnabled(false);
            chart.setPadding((int) DensityUtil.px2Dp(context, 4), 0, 0, 0);

            chart.setPinchZoom(false);
            chart.setTouchEnabled(false);

            chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
            chart.getXAxis().setAxisLineWidth(1f);
            chart.getXAxis().setAxisLineColor(ContextCompat.getColor(context, R.color.divider));
            chart.getXAxis().setLabelCount(MAX_X_AXIS_LABEL_COUNT, true);
            chart.getXAxis().setTextSize(12f);
            chart.getXAxis().setTextColor(ContextCompat.getColor(context, R.color.secondary_text));
            chart.getXAxis().setDrawGridLines(false);
            chart.getXAxis().setValueFormatter(new AxisValueFormatter());

            chart.getAxisLeft().setAxisLineWidth(1f);
            chart.getAxisLeft().setAxisLineColor(ContextCompat.getColor(context, R.color.white));
            chart.getAxisLeft().setGridLineWidth(1f);
            chart.getAxisLeft().setGridColor(ContextCompat.getColor(context, R.color.divider));
            chart.getAxisLeft().setTextSize(12f);
            chart.getAxisLeft().setTextColor(ContextCompat.getColor(context, R.color.secondary_text));
            chart.getAxisLeft().setLabelCount(4, false);
        }

        void render(List<DataPoint> points) {
            if (points == null || points.isEmpty()) {
                return;
            }

            renderTextView(points);
            renderChart(points);
        }

        void renderTextView(List<DataPoint> points) {
            // データは降順に格納されている
            DataPoint firstDataPoint = points.get(points.size() - 1);
            DataPoint lastDataPoint = points.get(0);

            if (!DataPointUtil.hasValue(firstDataPoint) || !DataPointUtil.hasValue(lastDataPoint)) {
                return;
            }

            float firstValue = DataPointUtil.getValue(firstDataPoint);
            float lastValue = DataPointUtil.getValue(lastDataPoint);

            if (firstValue == 0f) {
                return;
            }

            float percent = (lastValue / firstValue - 1) * 100;

            weightTextView.setText(String.format(context.getString(R.string.unit_kg_format), lastValue));
            dateTextView.setText(dateFormat.format(lastDataPoint.getTimestamp(TimeUnit.MILLISECONDS)));
            vsPercentTextView.setText(String.format(context.getString(R.string.vs_percent_format), percent));
            vsDateTextView.setText(String.format(context.getString(R.string.vs_date_format), dateFormat.format(firstDataPoint.getTimestamp(TimeUnit.MILLISECONDS))));
        }

        void renderChart(List<DataPoint> points) {
            entries = new ArrayList<>();
            max = 0f;
            min = 0f;

            // 基準日
            HashMap<String, Entry> entryHashMap = new HashMap<>();

            for (int i = points.size() - 1; i >= 0; i--) {
                DataPoint point = points.get(i);
                if (!DataPointUtil.hasValue(point)) {
                    continue;
                }

                LocalDateTime localDateTime = LocalDateTimeUtil.from(point.getTimestamp(TimeUnit.MILLISECONDS));
                String date = LocalDateTimeUtil.formatSimpleLocalDate(localDateTime);
                if (!entryHashMap.containsKey(date)) {
                    Entry entry = new Entry(i, DataPointUtil.getValue(point), date);
                    entryHashMap.put(date, entry);
                }
            }

            long days = 30 * amount;
            LocalDateTime basisLocalDateTime = LocalDateTime.now().minusDays(days);

            for (int i = 0; i <= days; i++) {
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
                    Entry prevEntry = entries.get(entries.size() - 1);
                    Entry entry = new Entry(entries.size(), prevEntry.getY());
                    entry.setData(date);
                    entries.add(entry);

                    max = Math.max(max, entry.getY());
                    min = Math.min(min, entry.getY());
                }
            }

            // データ数が 1 個の時はグラフ描画が満足に行えないため描画を見送る
            if (entryHashMap.size() < 2 || entries.size() < 2) {
                return;
            }

            ArrayList<ILineDataSet> dataSets = new ArrayList<>();
            dataSets.add(getLineDataSet(entries));
            LineData lineData = new LineData(dataSets);

            chart.setData(lineData);
            chart.getXAxis().setLabelCount(Math.min(MAX_X_AXIS_LABEL_COUNT, entries.size()), true);
            chart.getAxisLeft().setAxisMaximum((float) Math.ceil(max) + 0.5f);
            chart.getAxisLeft().setAxisMinimum((float) Math.floor(min) - 0.5f);
            chart.invalidate();
        }

        LineDataSet getLineDataSet(List<Entry> entries) {
            LineDataSet lineDataSet = new LineDataSet(entries, "");
            lineDataSet.setMode(LineDataSet.Mode.LINEAR);
            lineDataSet.setColor(ContextCompat.getColor(context, R.color.colorPrimary));
            lineDataSet.setFillColor(ContextCompat.getColor(context, R.color.colorPrimary));
            lineDataSet.setLineWidth(2f);
            lineDataSet.setDrawCircles(false);
            lineDataSet.setDrawValues(false);
            lineDataSet.setDrawFilled(true);
            return lineDataSet;
        }

        class AxisValueFormatter implements IAxisValueFormatter {

            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                // データ項目が少ない場合は、指定した position 以外も入ってくる可能性があり、マイナス値も考慮する
                int position = (int) value;
                return (entries == null || position < 0 || position >= entries.size())
                        ? ""
                        : (String) entries.get(position).getData();
            }
        }
    }

    private class DataViewHolder extends RecyclerView.ViewHolder {

        TextView weightTextView;
        TextView dateTextView;
        View divider;

        DataViewHolder(View itemView) {
            super(itemView);
            weightTextView = (TextView) itemView.findViewById(R.id.weight_text_view);
            dateTextView = (TextView) itemView.findViewById(R.id.date_text_view);
            divider = itemView.findViewById(R.id.divider);
        }

        void render(DataPoint dataPoint, boolean isFirst) {
            if (DataPointUtil.hasValue(dataPoint)) {
                // DataType com.google.weight の標準単位は kg
                // https://developers.google.com/fit/android/data-types#public_data_types
                float value = DataPointUtil.getValue(dataPoint);
                String text = String.format(context.getString(R.string.unit_kg_format), value);
                weightTextView.setText(text);
                dateTextView.setText(dateFormat.format(dataPoint.getTimestamp(TimeUnit.MILLISECONDS)));
                itemView.setOnClickListener(v -> callback.onDataPointClicked(dataPoint));
                divider.setVisibility(isFirst ? View.VISIBLE : View.GONE);
            }
        }
    }

    private class ShadowViewHolder extends RecyclerView.ViewHolder {

        ShadowViewHolder(View itemView) {
            super(itemView);
        }
    }
}
