package com.easyfitness.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ListView;

public class ExpandedListView extends ListView {
    private ViewGroup.LayoutParams params;
    private int oldCount = 0;

    public ExpandedListView(Context context) {
        super(context);
    }

    public ExpandedListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ExpandedListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int heightSpec = heightMeasureSpec;

        if (getLayoutParams().height == ViewGroup.LayoutParams.WRAP_CONTENT) {
            heightSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        }

        super.onMeasure(widthMeasureSpec, heightSpec);
    }

}
