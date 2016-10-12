package com.google.android.gms.samples.vision.ocrreader;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Wes on 10/11/2016.
 */

public class PopupFragment extends Fragment {
    private Activity mParent = null;
    private ArrayList<String> links;
    private ListView list;

    @Override
    public void onAttach(Activity parent) {
        super.onAttach(parent);
        mParent = parent;
    }

    @Override
    public View onCreateView(LayoutInflater inflator, ViewGroup container, Bundle savedInstanceState) {

        Log.w("PopupFragment", "Creating..." + savedInstanceState);

        Bundle arguments = this.getArguments();

        if(arguments != null) {
            links = arguments.getStringArrayList("array");
        }

        return inflator.inflate(R.layout.popup_fragment, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        list = (ListView) getView().findViewById(R.id.linksList);

        if(links == null) {
            Log.e("PopupFragment", "Links ArrayList was null. Error in sending data");
            links = new ArrayList<>();
        }

        list.setAdapter(new LinksAdapter(mParent, android.R.layout.simple_list_item_1, links));
    }

}
