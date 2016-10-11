package com.google.android.gms.samples.vision.ocrreader;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * Created by Wes on 10/11/2016.
 */

public class PopupFragment extends Fragment {
    private Activity mParent = null;

    @Override
    public void onAttach(Activity parent) {
       mParent = parent;
    }

    @Override
    public View onCreateView(LayoutInflater inflator, ViewGroup container, Bundle savedInstanceState) {

        Bundle linksToShow = getArguments();

        ListView listView = (ListView) mParent.findViewById (R.id.linksList);
        String[] values = null;

        ArrayAdapter<String>  adapter = new ArrayAdapter<String>(mParent, android.R.layout.simple_list_item_1, android.R.id.text1, values);

        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                /* TODO make links open a browser */
            }
        });

        return inflator.inflate(R.layout.popup_fragment, container, false);
    }


}
