package com.rakuishi.weight;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.widget.TimePicker;

import org.threeten.bp.LocalDateTime;

public class TimePickerDialogFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

    private static final String DATETIME = "localDateTime";

    public static TimePickerDialogFragment newInstance(LocalDateTime localDateTime) {
        TimePickerDialogFragment fragment = new TimePickerDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(DATETIME, localDateTime);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LocalDateTime localDateTime = (LocalDateTime) getArguments().getSerializable(DATETIME);

        TimePickerDialog.OnTimeSetListener callback;
        if (getActivity() instanceof TimePickerDialog.OnTimeSetListener) {
            callback = (TimePickerDialog.OnTimeSetListener) getActivity();
        } else {
            throw new IllegalStateException("The parent activity must set TimePickerDialog.OnTimeSetListener.");
        }

        return new TimePickerDialog(
                getContext(),
                callback,
                localDateTime.getHour(),
                localDateTime.getMinute(),
                true
        );
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

    }
}
