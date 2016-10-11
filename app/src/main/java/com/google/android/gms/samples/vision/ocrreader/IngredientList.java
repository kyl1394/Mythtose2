package com.google.android.gms.samples.vision.ocrreader;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.UserDictionary;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.textservice.SentenceSuggestionsInfo;
import android.view.textservice.SpellCheckerSession;
import android.view.textservice.SuggestionsInfo;
import android.view.textservice.TextInfo;
import android.view.textservice.TextServicesManager;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.google.android.gms.common.api.CommonStatusCodes;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class IngredientList extends AppCompatActivity {
    private TextView textValue;

    private ArrayList<String> ingredientDatabase = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ingredientDatabase.add("wheat flour");
        ingredientDatabase.add("durum flour");
        ingredientDatabase.add("niacin");
        ingredientDatabase.add("ferrous sulfate [iron]");
        ingredientDatabase.add("thiamin mononitrate [vitamin b1]");
        ingredientDatabase.add("riboflavin [vitamin b2]");
        ingredientDatabase.add("folic acid");
        ingredientDatabase.add("whey");
        ingredientDatabase.add("milkfat");
        ingredientDatabase.add("sodium triphosphate");
        ingredientDatabase.add("citric acid");
        ingredientDatabase.add("lactic acid");
        ingredientDatabase.add("sodium phosphate");
        ingredientDatabase.add("calcium phosphate");
        ingredientDatabase.add("paprika");
        ingredientDatabase.add("turmeric");
        ingredientDatabase.add("annatto");
        ingredientDatabase.add("enzymes");
        ingredientDatabase.add("cheese cultures");

        ingredientDatabase.add("chicken broth");
        ingredientDatabase.add("cooked chicken meat");
        ingredientDatabase.add("carrots");
        ingredientDatabase.add("semolina wheat");
        ingredientDatabase.add("wheat flour");
        ingredientDatabase.add("egg whites");
        ingredientDatabase.add("celery");
        ingredientDatabase.add("modified food starch");
        ingredientDatabase.add("water");
        ingredientDatabase.add("corn protein");
        ingredientDatabase.add("chicken fat");
        ingredientDatabase.add("salt");
        ingredientDatabase.add("carrot puree");
        ingredientDatabase.add("potassium chloride");
        ingredientDatabase.add("onion powder");
        ingredientDatabase.add("sugar");
        ingredientDatabase.add("soy protein isolate");
        ingredientDatabase.add("tomato extract");
        ingredientDatabase.add("sodium phosphate");
        ingredientDatabase.add("garlic powder");
        ingredientDatabase.add("parsley");
        ingredientDatabase.add("citric acid");
        ingredientDatabase.add("natural flavor");
        ingredientDatabase.add("spice");
        ingredientDatabase.add("chives");
        ingredientDatabase.add("beta carotene");


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ingredient_list);
        HashMap<String, Double> potentialIngredients = new HashMap<>();
        ArrayList<String> finalIngredients = new ArrayList<>();

        String text = null;
        String testString = "";
        textValue = (TextView)findViewById(R.id.text_value);
        if (OcrDetectorProcessor.candidates != null) {
            ArrayList<String> candidates = OcrDetectorProcessor.candidates;
            ArrayList<String> spelledCorrectly = new ArrayList<>();
            ArrayList<String> needsCorrected = new ArrayList<>();

            for (int candidateIndex = 0; candidateIndex < candidates.size(); candidateIndex++) {
                String[] candidate = candidates.get(candidateIndex).split(" ");

                for (int candidateIngredientIndex = 0; candidateIngredientIndex < candidate.length; candidateIngredientIndex++) {
                    double maxSimilarity = -1;
                    String mostSimilarWord = "";
                    int numWordsAdded = 0;
                    for (int wordIndex = 0; wordIndex < ingredientDatabase.size(); wordIndex++) {
                        double temp = similarity(candidate[candidateIngredientIndex], ingredientDatabase.get(wordIndex));
                        double addNextWord = 0;
                        numWordsAdded = 0;

                        for (int i = candidateIngredientIndex; i < candidate.length; i++) {
                            String multiWordIngredient = candidate[candidateIngredientIndex];
                            multiWordIngredient += " " + candidate[i];

                            addNextWord = similarity(multiWordIngredient, ingredientDatabase.get((wordIndex)));
                            if (addNextWord > temp) {
                                temp = addNextWord;
                                numWordsAdded++;
                            } else if (addNextWord < temp) {
                                break;
                            }
                        }

                        if (temp > maxSimilarity) {
                            maxSimilarity = temp;
                            mostSimilarWord = ingredientDatabase.get(wordIndex);
                        }
                    }

                    if ((!potentialIngredients.containsKey(mostSimilarWord) || potentialIngredients.get(mostSimilarWord) < maxSimilarity) && maxSimilarity > 0.45) {
                        potentialIngredients.put(mostSimilarWord, maxSimilarity);
                    }

                    candidateIngredientIndex += numWordsAdded;
                }
            }

            text = potentialIngredients.keySet().toString();
        } else {
            text = "Unable to find ingredient list.";
        }

        textValue.setText(text);
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
