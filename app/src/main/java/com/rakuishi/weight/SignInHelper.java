package com.rakuishi.weight;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class SignInHelper {

    public static void onConnectionFail(Context context) {
        Toast.makeText(context, R.string.failed_to_connect, Toast.LENGTH_LONG).show();

        Intent intent = SplashActivity.create(context);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }
}
