package com.easyfitness.graph;

import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;

import com.easyfitness.R;
import com.easyfitness.utils.DateConverter;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.IMarker;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.DefaultValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.EntryXComparator;
import com.github.mikephil.charting.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.TimeZone;

public class DateGraph {

    private LineChart mChart = null;
    private String mChartName = null;
    private Context mContext = null;

    public DateGraph(Context context, LineChart chart, String name) {
        mChart = chart;
        mChartName = name;
        mChart.setDoubleTapToZoomEnabled(true);
        mChart.setHorizontalScrollBarEnabled(true);
        mChart.setVerticalScrollBarEnabled(true);
        mChart.setAutoScaleMinMaxEnabled(true);
        mChart.setDrawBorders(true);
        mChart.setNoDataText(context.getString(R.string.no_chart_data_available));
        mChart.setExtraOffsets(0, 0, 0, 10);

        IMarker marker = new DateGraphMarkerView(mChart.getContext(), R.layout.graph_markerview, mChart);
        mChart.setMarker(marker);

        mContext = context;
        Legend l = mChart.getLegend();
        l.setEnabled(false);
        l.setTextSize(12);

        XAxis xAxis = mChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(ColorTemplate.getHoloBlue());
        xAxis.setTextSize(14);
        xAxis.setDrawAxisLine(true);
        xAxis.setDrawGridLines(true);
        xAxis.setCenterAxisLabels(false);
        xAxis.setGranularity(1);
        xAxis.setValueFormatter(new ValueFormatter() {

            private final SimpleDateFormat mFormat = new SimpleDateFormat("dd-MMM");

            @Override
            public String getFormattedValue(float value) {
                mFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
                Date tmpDate = new Date((long) DateConverter.nbMilliseconds(value));
                return mFormat.format(tmpDate);
            }
        });

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        leftAxis.setTextColor(ColorTemplate.getHoloBlue());
        leftAxis.setDrawGridLines(true);
        leftAxis.setGranularityEnabled(true);
        leftAxis.setGranularity((float) 0.5);
        leftAxis.setTextSize(12);
        leftAxis.resetAxisMinimum();

        mChart.getAxisRight().setEnabled(false);
    }

    public void draw(ArrayList<Entry> entries) {
        mChart.clear();
        if (entries.isEmpty()) {
            return;
        }

        Collections.sort(entries, new EntryXComparator());

        LineDataSet set1 = new LineDataSet(entries, mChartName);
        set1.setLineWidth(3f);
        set1.setCircleRadius(4f);
        set1.setDrawFilled(true);
        if (Utils.getSDKInt() >= 18) {
            Drawable drawable = ContextCompat.getDrawable(mContext, R.drawable.fade_blue);
            set1.setFillDrawable(drawable);
        } else {
            set1.setFillColor(ColorTemplate.getHoloBlue());
        }
        set1.setFillAlpha(100);
        set1.setColor(mContext.getResources().getColor(R.color.toolbar_background));
        set1.setCircleColor(mContext.getResources().getColor(R.color.toolbar_background));
        LineData data = new LineData(set1);
        data.setValueTextColor(mContext.getResources().getColor(R.color.cardview_title_color));
        data.setValueTextSize(12f);
        data.setValueFormatter(new DefaultValueFormatter(2));
        mChart.setData(data);

        mChart.invalidate();

    }

    private String arrayToString(ArrayList<Entry> entries) {
        StringBuilder output = new StringBuilder();
        String delimiter = "\n";
        for (int i = 0; i < entries.size(); i++) {
            output.append(entries.get(i).getY()).append(" / ").append(entries.get(i).getX()).append(delimiter);
        }

        return output.toString();
    }

    public LineChart getChart() {
        return mChart;
    }

    public void setZoom(ZoomType z) {
        mChart.fitScreen();
        switch (z) {
            case ZOOM_ALL:

                break;
            case ZOOM_WEEK:
                if (mChart.getData() != null) {
                    mChart.setVisibleXRangeMaximum((float) 7);
                    mChart.moveViewToX(mChart.getData().getXMax() + (7));
                }
                break;
            case ZOOM_MONTH:
                if (mChart.getData() != null) {
                    mChart.setVisibleXRangeMaximum((float) 30);
                    mChart.moveViewToX(mChart.getData().getXMax() - (float) (30));
                }
                break;
            case ZOOM_YEAR:
                if (mChart.getData() != null) {
                    mChart.setVisibleXRangeMaximum((float) 365);
                    mChart.moveViewToX(mChart.getData().getXMax() - (float) (365));
                }
                break;
        }
        mChart.invalidate();
    }

    public void setGraphDescription(String description) {
        Description desc = new Description();
        desc.setText(description);
        desc.setTextSize(12);
        mChart.setDescription(desc);
    }

}
