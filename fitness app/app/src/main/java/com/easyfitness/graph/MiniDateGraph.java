package com.easyfitness.graph;

import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;

import com.easyfitness.R;
import com.easyfitness.utils.DateConverter;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.IMarker;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.EntryXComparator;
import com.github.mikephil.charting.utils.Utils;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.TimeZone;

public class MiniDateGraph {

    private LineChart mChart = null;
    private String mChartName = null;
    private Context mContext = null;

    public MiniDateGraph(Context context, LineChart chart, String name) {
        mChart = chart;
        mChartName = name;
        mChart.getDescription().setEnabled(false);
        mChart.setDoubleTapToZoomEnabled(false);
        mChart.setHorizontalScrollBarEnabled(false);
        mChart.setVerticalScrollBarEnabled(false);
        mChart.setAutoScaleMinMaxEnabled(false);
        mChart.setDrawBorders(false);
        mChart.setViewPortOffsets(6f, 6f, 6f, 6f);
        mChart.animateY(1000, Easing.EaseInOutBack);
        mChart.setClickable(false);

        mChart.getAxisRight().setDrawLabels(false);
        mChart.getAxisLeft().setDrawLabels(false);
        mChart.getLegend().setEnabled(false);
        mChart.setPinchZoom(false);
        mChart.setDescription(null);
        mChart.setTouchEnabled(false);
        mChart.setDoubleTapToZoomEnabled(false);
        mChart.setNoDataText(context.getString(R.string.no_chart_data_available));

        mContext = context;
        Legend l = mChart.getLegend();
        l.setEnabled(false);

        XAxis xAxis = mChart.getXAxis();
        xAxis.setDrawLabels(false);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(ColorTemplate.getHoloBlue());
        xAxis.setDrawAxisLine(false);
        xAxis.setDrawGridLines(false);
        xAxis.setCenterAxisLabels(false);
        xAxis.setGranularity(1);

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setEnabled(false);
        leftAxis.setDrawZeroLine(false);
        leftAxis.setDrawLabels(false);
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        leftAxis.setTextColor(ColorTemplate.getHoloBlue());
        leftAxis.setDrawGridLines(false);
        leftAxis.setGranularityEnabled(false);

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
        set1.setCircleRadius(0f);
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
        data.setDrawValues(false);

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

    public void setZoom(zoomType z) {
        switch (z) {
            case ZOOM_ALL:
                mChart.fitScreen();
                break;
            case ZOOM_WEEK:
                mChart.fitScreen();
                if (mChart.getData() != null) {
                    mChart.setVisibleXRangeMaximum((float) 7);
                    mChart.moveViewToX(mChart.getData().getXMax() + (1 - 7));
                }
                break;
            case ZOOM_MONTH:
                mChart.fitScreen();
                if (mChart.getData() != null) {
                    mChart.setVisibleXRangeMaximum((float) 30);
                    mChart.moveViewToX(mChart.getData().getXMax() + (float) (1 - 30));
                }
                break;
            case ZOOM_YEAR:
                mChart.fitScreen();
                if (mChart.getData() != null) {
                    mChart.setVisibleXRangeMaximum((float) 365);
                    mChart.moveViewToX(mChart.getData().getXMax() + (float) (1 - 365));
                }
                break;
        }
        mChart.invalidate();
    }

    public enum zoomType {ZOOM_ALL, ZOOM_YEAR, ZOOM_MONTH, ZOOM_WEEK}
}
