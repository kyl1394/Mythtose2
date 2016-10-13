package com.google.android.gms.samples.vision.ocrreader;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * This is the fragment used to display the information that each part of the pie chart will show.
 * Created by Wes on 10/11/2016.
 */

public class PopupFragment extends Fragment {

    public static final String LINKS_ARGUMENT = "array";

    private Context mParent = null;
    private ArrayList<String> links;
    private LinksAdapter adapter;

    @Override
    public void onAttach(Context parent) {
        super.onAttach(parent);
        mParent = parent;
    }

    @Override
    public View onCreateView(LayoutInflater inflator, ViewGroup container, Bundle savedInstanceState) {
        Bundle arguments = this.getArguments();

        if(arguments != null) {
            links = arguments.getStringArrayList(LINKS_ARGUMENT);
        }

        return inflator.inflate(R.layout.popup_fragment, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        ListView list = (ListView) getView().findViewById(R.id.linksList);

        if(links == null || links.size() == 0) {
            Log.e("PopupFragment", "Links ArrayList was empty.");
            links = new ArrayList<>();
        }
        adapter = new LinksAdapter(mParent, android.R.layout.simple_list_item_1, links);
        list.setAdapter(adapter);
    }

    public void update(ArrayList<String> newList) {
        links.clear();
        links.addAll(newList);
        Log.d("New List size", ""+newList.size());

//        list.setAdapter(new LinksAdapter(mParent, android.R.layout.simple_list_item_1, links));
        adapter.notifyDataSetChanged();

    }

}
