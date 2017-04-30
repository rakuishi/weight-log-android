package com.rakuishi.weight.util;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.rakuishi.weight.R;
import com.rakuishi.weight.activity.MainActivity;

public class SignInHelper {

    public static void fail(Context context) {
        Toast.makeText(context, R.string.failed_to_connect, Toast.LENGTH_LONG).show();

        Intent intent = MainActivity.create(context);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }
}
