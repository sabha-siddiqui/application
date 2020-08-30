package com.easyfitness.graph;

import android.content.Context;
import android.graphics.Canvas;
import android.widget.TextView;

import com.easyfitness.R;
import com.easyfitness.utils.DateConverter;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.TimeZone;

import static android.text.format.DateFormat.getDateFormat;

public class BarGraphMarkerView extends MarkerView {

    private TextView tvContent;
    private TextView tvDate;
    private DecimalFormat mFormat = new DecimalFormat("#.##");
    private Chart lineChart = null;
    private int uiScreenWidth;
    private MPPointF mOffset;

    public BarGraphMarkerView(Context context, int layoutResource, Chart chart) {
        super(context, layoutResource);
        tvContent = findViewById(R.id.tvContent);
        tvDate = findViewById(R.id.tvDate);
        uiScreenWidth = getResources().getDisplayMetrics().widthPixels;
        lineChart = chart;
    }
    @Override
    public void refreshContent(Entry e, Highlight highlight) {

        DateFormat dateFormat3 = getDateFormat(getContext().getApplicationContext());
        dateFormat3.setTimeZone(TimeZone.getTimeZone("GMT"));
        tvDate.setText(dateFormat3.format(new Date((long) DateConverter.nbMilliseconds(e.getX()))));
        tvContent.setText(mFormat.format(e.getY()));
        super.refreshContent(e, highlight);
    }

    @Override
    public MPPointF getOffset() {

        if (mOffset == null) {
            mOffset = new MPPointF(-(getWidth() / 2), -getHeight());
        }

        return mOffset;
    }

    @Override
    public void draw(Canvas canvas, float posX, float posY) {
        int lineChartWidth = 0;
        int lineChartHeight = 0;
        float offsetX = getOffset().getX();
        float offsetY = getOffset().getY();

        float width = getWidth();
        float height = getHeight();

        if (lineChart != null) {
            lineChartWidth = lineChart.getWidth();
            lineChartHeight = lineChart.getHeight();
        }
        if (posX + offsetX < 0) {
            offsetX = -posX;
        } else if (posX + width + offsetX > lineChartWidth) {
            offsetX = lineChartWidth - posX - width;
        }
        posX += offsetX;
        if (posY + offsetY < 0) {
            posY = posY + 20;
        } else if (posY + height + offsetY > lineChartHeight) {
            posY += lineChartHeight - posY - height;
        } else {
            posY += offsetY;
        }
        canvas.translate(posX, posY);
        draw(canvas);
        canvas.translate(-posX, -posY);
    }
}
