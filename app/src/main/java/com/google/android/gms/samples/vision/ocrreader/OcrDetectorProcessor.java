/*
 * Copyright (C) The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.android.gms.samples.vision.ocrreader;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.util.SparseArray;

import com.google.android.gms.samples.vision.ocrreader.ui.camera.GraphicOverlay;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import static com.google.android.gms.samples.vision.ocrreader.OcrCaptureActivity.TextBlockObject;

/**
 * A very simple Processor which receives detected TextBlocks and adds them to the overlay
 * as OcrGraphics.
 */
public class OcrDetectorProcessor implements Detector.Processor<TextBlock> {

    public static Set<Ingredient> matchedIngreds;

    private GraphicOverlay<OcrGraphic> mGraphicOverlay;
    private Context context;

    OcrDetectorProcessor(GraphicOverlay<OcrGraphic> ocrGraphicOverlay, Context context) {
        mGraphicOverlay = ocrGraphicOverlay;
        this.context = context;
    }

    public static ArrayList<String> candidates = new ArrayList<>();
    private HashMap<String, Integer> categories;

    /**
     * Called by the detector to deliver detection results.
     * If your application called for it, this could be a place to check for
     * equivalent detections by tracking TextBlocks that are similar in location and content from
     * previous frames, or reduce noise by eliminating TextBlocks that have not persisted through
     * multiple detections.
     */
    private int candidateSize = -1;
    @Override
    public void receiveDetections(Detector.Detections<TextBlock> detections) {
        //mGraphicOverlay.clear();
        if (candidates.size() > candidateSize) {
            candidateSize = candidates.size();
            Snackbar.make(mGraphicOverlay, "Found " + candidates.size() + "/15",
                    Snackbar.LENGTH_LONG)
                    .show();
        }
        SparseArray<TextBlock> items = detections.getDetectedItems();
        boolean shouldAdd = false;
        String lowercaseValue = "";
        for (int i = 0; i < items.size(); ++i) {
            TextBlock item = items.valueAt(i);
            lowercaseValue += item.getValue().toLowerCase() + ",";
            //OcrGraphic graphic = new OcrGraphic(mGraphicOverlay, item);
            //mGraphicOverlay.add(graphic);
        }

        if (lowercaseValue.contains("ingredients")) {
            shouldAdd = true;
        }

        if (shouldAdd) {
            candidates.add(lowercaseValue);
            if (candidates.size() == 15) {
                String text = "asdf";
                Intent data = new Intent(context, DetailActivity.class);
                matchedIngreds = matchIngredientsToDatabase();
                categories = getNumCategories(matchedIngreds);
                data.putExtra("categories", categories);
                data.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(data);
            }
        }
    }

    /**
     * Frees the resources associated with this detection processor.
     */
    @Override
    public void release() {
        mGraphicOverlay.clear();
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

    private String[] blacklist = {"with", "contains", "of", "henz", "heinz", "hunz", "the", "less", "than", "kraft", "company", "2%", "an", "and", "for", "added"};
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

                loop:
                for (int candidateIngredientIndex = 0; candidateIngredientIndex < candidate.length; candidateIngredientIndex++) {
                    for (int i = 0; i < blacklist.length; i++) {
                        if (similarity(candidate[candidateIngredientIndex], blacklist[i]) > 0.75) {
                            continue loop;
                        }
                    }

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

                    if ((!potentialIngredients.containsKey(mostSimilar) || potentialIngredients.get(mostSimilar) < maxSimilarity) && maxSimilarity > 0.55) {
                        potentialIngredients.put(mostSimilar, maxSimilarity);
                    }

                    candidateIngredientIndex += numWordsAdded;
                }
            }

            return potentialIngredients.keySet();
        }

        return null;
    }

    private ArrayList<String> getIngredientsFromDatabase() {
        DatabaseReference fbDbRef = FirebaseDatabase.getInstance().getReference("0");
        final ArrayList<String> ingredientList = new ArrayList<>();

        fbDbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Iterator iter = snapshot.getChildren().iterator();
                while (iter.hasNext()) {
                    DataSnapshot snap = (DataSnapshot) iter.next();
                    ingredientList.add(snap.getKey());
                }
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
                System.out.println("The read failed: " + firebaseError.getMessage());
                return;
            }
        });

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
