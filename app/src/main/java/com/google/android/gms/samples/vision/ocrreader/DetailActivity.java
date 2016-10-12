package com.google.android.gms.samples.vision.ocrreader;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Color;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.CategorySeries;
import org.achartengine.model.SeriesSelection;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;

import java.util.ArrayList;
import java.util.List;

public class DetailActivity extends AppCompatActivity {

    /** Colors to be used for the pie slices. */
    private static int[] COLORS = new int[] { Color.GREEN, Color.BLUE, Color.MAGENTA, Color.CYAN };
    /** The main series that will include all the data. */
    private CategorySeries mSeries = new CategorySeries("");
    /** The main renderer for the main dataset. */
    private DefaultRenderer mRenderer = new DefaultRenderer();
    /** Button for adding entered data to the current series. */
    private Button mAdd;
    /** Edit text field for entering the slice value. */
    private EditText mValue;
    /** The chart view that displays the data. */
    private GraphicalView mChartView;
    /** */
    private ArrayList<ChartData> chartInfo;

    @Override
    protected void onRestoreInstanceState(Bundle savedState) {
        super.onRestoreInstanceState(savedState);
        //mSeries = (CategorySeries) savedState.getSerializable("current_series");
        //mRenderer = (DefaultRenderer) savedState.getSerializable("current_renderer");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //outState.putSerializable("current_series", mSeries);
        //outState.putSerializable("current_renderer", mRenderer);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        float val = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 14, metrics);
        mRenderer.setLabelsTextSize(val);
        mRenderer.setZoomButtonsVisible(true);
        mRenderer.setShowLegend(false);
        mRenderer.setStartAngle(180);
        mRenderer.setDisplayValues(true);

        Bundle extras = getIntent().getExtras();

        if(extras != null) {

        }

        if(chartInfo == null) {
            chartInfo = new ArrayList<>();

            for (int x = 0; x < 10; x++) {
                ArrayList<String> links = new ArrayList<String>();
                links.add("<a href=\"google.com\">Click Google! " + x + "</a>");
                links.add("<a href=\"facebook.com\">Click Facebook! +" + x + "</a>");
                chartInfo.add(new ChartData("Element " + (char) ('A' + x), x, links));
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mChartView == null) {
            LinearLayout layout = (LinearLayout) findViewById(R.id.chart);
            mChartView = ChartFactory.getPieChartView(this, mSeries, mRenderer);
            mRenderer.setClickEnabled(true);
            mChartView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SeriesSelection seriesSelection = mChartView.getCurrentSeriesAndPoint();
                    if (seriesSelection == null) {
                        Toast.makeText(DetailActivity.this, "No chart element selected", Toast.LENGTH_SHORT)
                                .show();
                    } else {
                        for (int i = 0; i < mSeries.getItemCount(); i++) {
                            mRenderer.getSeriesRendererAt(i).setHighlighted(i == seriesSelection.getPointIndex());
                        }
                        mChartView.repaint();

                        /* TODO Add an intent */
                        Toast.makeText(
                                DetailActivity.this,
                                "Chart data point index " + seriesSelection.getPointIndex() + " selected"
                                        + " point value=" + seriesSelection.getValue(), Toast.LENGTH_SHORT).show();

                        //Remvove old Fragment
                        Fragment frag = getFragmentManager().findFragmentByTag("main fragment");
                        //NULL POINTER EXCEPTION ON FRAG
                        getFragmentManager().beginTransaction().remove(frag).commit();


                        //Attach new Fragment

                        Bundle infoBundle = new Bundle();
                        frag = new PopupFragment();
                        infoBundle.putStringArrayList("array", chartInfo.get(seriesSelection.getPointIndex()).links);
                        frag.setArguments(infoBundle);
                        getFragmentManager().beginTransaction().replace(R.id.mainActivityFragment, frag, "main fragment").commit();

                    }
                }
            });
            layout.addView(mChartView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                    ViewGroup.LayoutParams.FILL_PARENT));
        } else {
            mChartView.repaint();
        }

        for(ChartData cd: chartInfo) {
            mSeries.add(cd.name, cd.amount);
            SimpleSeriesRenderer renderer = new SimpleSeriesRenderer();
            renderer.setColor(COLORS[(mSeries.getItemCount() - 1) % COLORS.length]);
            mRenderer.addSeriesRenderer(renderer);

            mChartView.repaint();
        }
    }

    protected class ChartData {
        public String name;

        public int amount;

        public ArrayList<String> links;

        public ChartData() {
            this("", 0, new ArrayList<String>());
        }

        public ChartData(String chartName, int chartAmount, ArrayList<String> chartLinks) {
            name = chartName;
            amount = chartAmount;
            links = chartLinks;
        }
    }
}
