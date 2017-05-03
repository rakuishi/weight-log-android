package com.rakuishi.weight.fragment;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import org.threeten.bp.LocalDateTime;

public class DatePickerDialogFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    public interface OnDataSetListener {
        void onDateSet(android.widget.DatePicker view, int year, int monthValue, int dayOfMonth);
    }

    private static final String DATETIME = "localDateTime";
    private OnDataSetListener callback;

    public static DatePickerDialogFragment newInstance(LocalDateTime localDateTime) {
        DatePickerDialogFragment fragment = new DatePickerDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(DATETIME, localDateTime);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LocalDateTime localDateTime = (LocalDateTime) getArguments().getSerializable(DATETIME);
        if (localDateTime == null) {
            throw new IllegalStateException("This fragment must be set localDateTime. Use `newInstance()` for creating new fragment instance.");
        }

        if (getActivity() instanceof DatePickerDialogFragment.OnDataSetListener) {
            callback = (DatePickerDialogFragment.OnDataSetListener) getActivity();
        } else {
            throw new IllegalStateException("The parent activity must set DatePickerDialogFragment.OnDataSetListener");
        }

        return new DatePickerDialog(
                getContext(),
                this,
                localDateTime.getYear(),
                // `getMonthValue()` は 1-12 を返すが、DatePickerDialog には 0-11 を登録する
                localDateTime.getMonthValue() - 1,
                localDateTime.getDayOfMonth()
        );
    }

    @Override
    public void onDateSet(android.widget.DatePicker view, int year, int month, int dayOfMonth) {
        callback.onDateSet(view, year, month + 1, dayOfMonth);
    }
}
