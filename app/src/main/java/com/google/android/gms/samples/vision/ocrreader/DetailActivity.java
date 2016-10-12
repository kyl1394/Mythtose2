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
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.CategorySeries;
import org.achartengine.model.SeriesSelection;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

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

        Set<Ingredient> ingreds = matchIngredientsToDatabase();
        HashMap<String, Integer> categories = getNumCategories(ingreds);

        if(categories.size() != 0) {
            /* ** Main code to add to pie chart */
            Set<String> types = categories.keySet();
            Iterator<String> iter = types.iterator();
            while (iter.hasNext()) {
                String name = iter.next();
                mSeries.add(name, categories.get(name));
                SimpleSeriesRenderer renderer = new SimpleSeriesRenderer();
                renderer.setColor(COLORS[(mSeries.getItemCount() - 1) % COLORS.length]);
                mRenderer.addSeriesRenderer(renderer);
            }

            updateChart();
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

    private HashMap<String,Integer> getNumCategories(Set<Ingredient> ingreds) {
        Iterator<Ingredient> ingredIter = ingreds.iterator();
        HashMap<String, Integer> toReturn = new HashMap<>();

        while (ingredIter.hasNext()) {
            Ingredient ingred = ingredIter.next();
            int add = -1;
            if (toReturn.containsKey(ingred.type)) {
                add = toReturn.get(ingred.type) + 1;
            } else {
                add = 1;
            }

            toReturn.put(ingred.type, add);
        }

        return toReturn;
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateChart();
    }

    private void updateChart() {
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

//        for(ChartData cd: chartInfo) {
//            mSeries.add(cd.name, cd.amount);
//            SimpleSeriesRenderer renderer = new SimpleSeriesRenderer();
//            renderer.setColor(COLORS[(mSeries.getItemCount() - 1) % COLORS.length]);
//            mRenderer.addSeriesRenderer(renderer);
//
//            mChartView.repaint();
//        }
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

    private Set<Ingredient> matchIngredientsToDatabase() {
        ArrayList<Ingredient> ingredientDatabase = MainActivity.ingredientDatabase;

        HashMap<Ingredient, Double> potentialIngredients = new HashMap<>();
        ArrayList<String> finalIngredients = new ArrayList<>();

        String text = null;
        String testString = "";

        if (OcrDetectorProcessor.candidates != null) {
            ArrayList<String> candidates = OcrDetectorProcessor.candidates;
            ArrayList<String> spelledCorrectly = new ArrayList<>();
            ArrayList<String> needsCorrected = new ArrayList<>();

            for (int candidateIndex = 0; candidateIndex < candidates.size(); candidateIndex++) {
                String[] candidate = candidates.get(candidateIndex).split(" ");

                for (int candidateIngredientIndex = 0; candidateIngredientIndex < candidate.length; candidateIngredientIndex++) {
                    double maxSimilarity = -1;
                    Ingredient mostSimilar = null;
                    int numWordsAdded = 0;
                    for (int wordIndex = 0; wordIndex < ingredientDatabase.size(); wordIndex++) {
                        double temp = similarity(candidate[candidateIngredientIndex], ingredientDatabase.get(wordIndex).name);
                        double addNextWord = 0;
                        numWordsAdded = 0;

                        for (int i = candidateIngredientIndex; i < candidate.length; i++) {
                            String multiWordIngredient = candidate[candidateIngredientIndex];
                            multiWordIngredient += " " + candidate[i];

                            addNextWord = similarity(multiWordIngredient, ingredientDatabase.get((wordIndex)).name);
                            if (addNextWord > temp) {
                                temp = addNextWord;
                                numWordsAdded++;
                            } else if (addNextWord < temp) {
                                break;
                            }
                        }

                        if (temp > maxSimilarity) {
                            maxSimilarity = temp;
                            mostSimilar = ingredientDatabase.get(wordIndex);
                        }
                    }

                    if ((!potentialIngredients.containsKey(mostSimilar) || potentialIngredients.get(mostSimilar) < maxSimilarity) && maxSimilarity > 0.45) {
                        potentialIngredients.put(mostSimilar, maxSimilarity);
                    }

                    candidateIngredientIndex += numWordsAdded;
                }
            }

            return potentialIngredients.keySet();
        }

        return null;
    }

    boolean finished = false;
    boolean reached = false;
    private ArrayList<String> getIngredientsFromDatabase() {
        DatabaseReference fbDbRef = FirebaseDatabase.getInstance().getReference("0");
        final ArrayList<String> ingredientList = new ArrayList<>();
        finished = false;

        fbDbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                reached = true;
                Iterator iter = snapshot.getChildren().iterator();
                while (iter.hasNext()) {
                    DataSnapshot snap = (DataSnapshot) iter.next();
                    ingredientList.add(snap.getKey());
                }
                finished = true;
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
                System.out.println("The read failed: " + firebaseError.getMessage());
                return;
            }
        });

        while (!finished) {
            // You must give this loop something to do otherwise running it out of debug mode will just cause it to hang
            // for some unknown reason...
            System.out.println("loading...");
        }

        finished = false;
        return ingredientList;
    }

    private static double similarity(String s1, String s2) {
        String longer = s1, shorter = s2;
        if (s1.length() < s2.length()) { // longer should always have greater length
            longer = s2; shorter = s1;
        }
        int longerLength = longer.length();
        if (longerLength == 0) { return 1.0; /* both strings are zero length */ }
        return (longerLength - editDistance(longer, shorter)) / (double) longerLength;
    }

    private static int editDistance(String s1, String s2) {
        s1 = s1.toLowerCase();
        s2 = s2.toLowerCase();

        int[] costs = new int[s2.length() + 1];
        for (int i = 0; i <= s1.length(); i++) {
            int lastValue = i;
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0)
                    costs[j] = j;
                else {
                    if (j > 0) {
                        int newValue = costs[j - 1];
                        if (s1.charAt(i - 1) != s2.charAt(j - 1))
                            newValue = Math.min(Math.min(newValue, lastValue),
                                    costs[j]) + 1;
                        costs[j - 1] = lastValue;
                        lastValue = newValue;
                    }
                }
            }
            if (i > 0)
                costs[s2.length()] = lastValue;
        }
        return costs[s2.length()];
    }
}
