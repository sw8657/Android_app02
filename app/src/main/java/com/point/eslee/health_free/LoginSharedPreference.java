package com.point.eslee.health_free;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by eslee on 2017-02-19.
 */

public class LoginSharedPreference {
    public static void setLogin(Activity ctx, String userName){
        SharedPreferences.Editor editer = ctx.getPreferences(Context.MODE_PRIVATE).edit();
        editer.putString("user_name",userName);
        editer.commit();
    }

    public static void logout(Activity ctx){
        SharedPreferences.Editor editer = ctx.getPreferences(Context.MODE_PRIVATE).edit();
        editer.putString("user_name","");
        editer.commit();
    }

    public static boolean isLogin(Activity ctx){
        return ctx.getPreferences(Context.MODE_PRIVATE).getString("user_name","").equals("") == false;
    }
}
