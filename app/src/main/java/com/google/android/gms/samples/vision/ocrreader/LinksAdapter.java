package com.google.android.gms.samples.vision.ocrreader;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Wes on 10/12/2016.
 */

public class LinksAdapter extends ArrayAdapter<String> {

    private Context con;
    private int layoutID;
    private List<String> links;

    public LinksAdapter(Context context, int layoutResourceID, List<String> links) {
        super(context, layoutResourceID, links);

        this.con = context;
        this.layoutID = layoutResourceID;
        this.links = links;
    }

    @SuppressLint("DefaultLocale")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        TextView tv = new TextView(con);

        if (row == null) {
            LayoutInflater inflater = ((Activity) con).getLayoutInflater();
            row = inflater.inflate(layoutID, parent, false);

            tv = (TextView) row.findViewById(android.R.id.text1);
            tv.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;

            row.setTag(tv);
        } else {
            tv = (TextView) row.getTag();
        }

        String link = links.get(position);

        tv.setText(link);

        return row;
    }

}
