package com.google.android.gms.samples.vision.ocrreader;

import android.graphics.Color;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.CategorySeries;
import org.achartengine.model.SeriesSelection;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class DetailActivity extends AppCompatActivity  {

    /** Colors to be used for the pie slices. */
    private static int[] COLORS = new int[] { Color.GREEN, Color.BLUE, Color.MAGENTA, Color.CYAN };
    /** The main series that will include all the data. */
    private CategorySeries mSeries = new CategorySeries("");
    /** The main renderer for the main dataset. */
    private DefaultRenderer mRenderer = new DefaultRenderer();
    /** The chart view that displays the data. */
    private GraphicalView mChartView;
    /** The info that the chart shows. */
    private ArrayList<ChartData> chartInfo;
    /** Allows information to be added when this activity is first created */
    private boolean onStart = false;

    @Override
    protected void onRestoreInstanceState(Bundle savedState) {
        super.onRestoreInstanceState(savedState);
        mSeries = (CategorySeries) savedState.getSerializable("current_series");
        mRenderer = (DefaultRenderer) savedState.getSerializable("current_renderer");
        chartInfo = (ArrayList<ChartData>) savedState.getSerializable("current_data");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("current_series", mSeries);
        outState.putSerializable("current_renderer", mRenderer);
        outState.putSerializable("current_data", chartInfo);
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

        /**************************
         TODO: fill the array list with real data instead of this junk

         */

        if(extras != null) {
            chartInfo = new ArrayList<>();
            HashMap<String, Integer> categories = (HashMap<String, Integer>) extras.get("categories");
            Set<String> types = categories.keySet();
            Iterator<String> iter = types.iterator();
            while (iter.hasNext()) {
                String categoryName = iter.next();
                ArrayList<String> ingredients = new ArrayList<>();
                for (int i = 0; i < MainActivity.ingredientDatabase.size(); i++) {
                    if (MainActivity.ingredientDatabase.get(i).type.equals(categoryName)) {
                        ingredients.add(MainActivity.ingredientDatabase.get(i).name);
                    }
                }
                chartInfo.add(new ChartData(categoryName, categories.get(categoryName), ingredients));
            }

            onStart = true;
        }

        if(chartInfo == null) {
            chartInfo = new ArrayList<>();

            for (int x = 0; x < 10; x++) {
                ArrayList<String> links = new ArrayList<>();
                links.add("Click Google! " + x);
                links.add("Click Facebook! " + x );
                links.add("Click Facebook! " + x );
                links.add("Click Facebook! " + x );
                links.add("Click Facebook! " + x );
                links.add("Click Facebook! " + x );
                links.add("Click Facebook! " + x );
                links.add("Click Facebook! " + x );
                links.add("Click Facebook! " + x );
                links.add("Click Facebook! " + x );
                links.add("Click Facebook! " + x );links.add("Click Facebook! " + x );
                links.add("Click Facebook! " + x );
                links.add("Click Facebook! " + x );
                links.add("Click Facebook! " + x );


                chartInfo.add(new ChartData("Element " + (char) ('A' + x), x, links));
            }
            onStart = true;
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

                        /* Old unused code to determine the selection
                        Toast.makeText(
                                DetailActivity.this,
                                "Chart data point index " + seriesSelection.getPointIndex() + " selected"
                                        + " point value=" + seriesSelection.getValue(), Toast.LENGTH_SHORT).show();
                        */
                        //Update Fragment
                        PopupFragment frag = (PopupFragment) getFragmentManager().findFragmentById(R.id.mainActivityFragment);

                        frag.update(chartInfo.get(seriesSelection.getPointIndex()).links);
                    }
                }
            });
            layout.addView(mChartView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
        } else {
            mChartView.repaint();
        }

        if(onStart) {
            Log.d("DetailActivity", "Reloaded Data");
            for (ChartData cd : chartInfo) {
                mSeries.add(cd.name, cd.amount);
                SimpleSeriesRenderer renderer = new SimpleSeriesRenderer();
                renderer.setColor(COLORS[(mSeries.getItemCount() - 1) % COLORS.length]);
                mRenderer.addSeriesRenderer(renderer);

                mChartView.repaint();
            }
            onStart = false;
        }
    }
}

class ChartData implements Serializable {
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