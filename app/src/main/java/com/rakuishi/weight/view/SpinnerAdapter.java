package com.rakuishi.weight.view;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.v7.widget.ThemedSpinnerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.rakuishi.weight.R;

public class SpinnerAdapter extends ArrayAdapter<String> implements ThemedSpinnerAdapter {

    private final ThemedSpinnerAdapter.Helper helper;

    public SpinnerAdapter(@NonNull Context context, String[] objects) {
        super(context, R.layout.view_spinner_text1, objects);
        helper = new ThemedSpinnerAdapter.Helper(context);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View view;

        if (convertView == null) {
            // Inflate the drop down using the helper's LayoutInflater
            LayoutInflater inflater = helper.getDropDownViewInflater();
            view = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
        } else {
            view = convertView;
        }

        TextView textView = (TextView) view.findViewById(android.R.id.text1);
        textView.setText(getItem(position));

        return view;
    }

    @Override
    public Resources.Theme getDropDownViewTheme() {
        return helper.getDropDownViewTheme();
    }

    @Override
    public void setDropDownViewTheme(Resources.Theme theme) {
        helper.setDropDownViewTheme(theme);
    }
}
