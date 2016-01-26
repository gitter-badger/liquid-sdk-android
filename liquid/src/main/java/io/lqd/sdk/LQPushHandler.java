/**
 * Copyright 2014-present Liquid Data Intelligence S.A.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.lqd.sdk;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;

/**
 * BroadcastReceiver that handles GCM intents.
 *
 *
 */
public class LQPushHandler extends BroadcastReceiver {

    private static final String RECVD_REGID_FROM_GOOGLE = "com.google.android.c2dm.intent.REGISTRATION";
    private static final String RECVD_C2DM_MSG_FROM_GOOGLE = "com.google.android.c2dm.intent.RECEIVE";
    private static final String PROPERTY_REG_ID = "registration_id";
    private static final String LIQUID_MESSAGE_EXTRA = "lqd_message";
    private static final String LIQUID_PUSH_ID_EXTRA = "lqd_id";
    private static final String LIQUID_SOUND_EXTRA = "lqd_sound";
    private static final String LIQUID_TITLE_EXTRA = "lqd_title";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(RECVD_REGID_FROM_GOOGLE)) {
            handleRegistration(intent);
        } else if (action.equals(RECVD_C2DM_MSG_FROM_GOOGLE)) {
            handleNotification(context, intent);
        }
    }

    private void handleNotification(Context context, Intent intent) {
        String message = intent.getStringExtra(LIQUID_MESSAGE_EXTRA);
        int push_id = getPushId(intent);
        int icon = getAppIconInt(context);
        Uri sound = getPushSound(intent, context);
        String title = getPushTitle(intent, context);
        Intent appIntent = getIntent(context);
        PendingIntent contentIntent = PendingIntent.getActivity(context.getApplicationContext(), 0, appIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        sendNotification(context, contentIntent, icon, push_id, title, message, sound);
    }

    private void handleRegistration(Intent intent) {
        String registrationID = intent.getStringExtra(PROPERTY_REG_ID);
        if(registrationID != null && registrationID.length() > 0) {
            LQLog.infoVerbose("Push registration id received: " + registrationID);
            Liquid.getInstance().setGCMregistrationID(registrationID);
        }
    }


    protected static void registerDevice(final Context context, final String senderID) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                final String registrationId;
                try {
                    int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
                    if (resultCode != ConnectionResult.SUCCESS) {
                        LQLog.error("Google Play Services are not installed.");
                    }

                    final GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
                    registrationId = gcm.register(senderID);
                    if(registrationId != null && registrationId.length() > 0) {
                        LQLog.infoVerbose("Push registration id received: " + registrationId);
                        Liquid.getInstance().setGCMregistrationID(registrationId);
                    }
                } catch (IOException e) {
                    LQLog.error("Cannot register for GCM.");
                } catch (NoClassDefFoundError e) {
                    LQLog.error("Google Play Services lib cannot be found.");
                }
                return null;
            }
        }.execute(null, null, null);
    }

    @SuppressWarnings("deprecation")
    private void sendNotification(Context c, PendingIntent intent, int icon, int push_id, String title, String body, Uri sound) {
        NotificationManager nm = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(c)
                .setSmallIcon(icon)
                .setTicker(body)
                .setContentText(body)
                .setWhen(System.currentTimeMillis())
                .setContentTitle(title)
                .setContentIntent(intent);
        if (sound != null)
            builder.setSound(sound);
        Notification n = builder.build();
        n.flags |= Notification.FLAG_AUTO_CANCEL;
        nm.notify(push_id, n);
    }

    private static int getPushId(Intent intent) {
        try {
            return Integer.parseInt(intent.getStringExtra(LIQUID_PUSH_ID_EXTRA));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static String getPushTitle(Intent intent, Context context) {
        String title = intent.getStringExtra(LIQUID_TITLE_EXTRA);
        if(title == null)
            return getAppName(context);
        return title;
    }

    private static Uri getPushSound(Intent intent, Context context) {
        String sound = intent.getStringExtra(LIQUID_SOUND_EXTRA);
        if(sound == null)
            return null;
        if("default".equals(sound))
            return Settings.System.DEFAULT_NOTIFICATION_URI;
        return Uri.parse("android.resource://" + context.getPackageName() + "/raw/" + sound);
    }


    private static String getAppName(Context context) {
        PackageManager manager = context.getPackageManager();
        try {
            ApplicationInfo appinfo = manager.getApplicationInfo(context.getPackageName(), 0);
            return manager.getApplicationLabel(appinfo).toString();
        } catch (NameNotFoundException e) {
            return "";
        }
    }

    private static int getAppIconInt(Context context) {
        PackageManager manager = context.getPackageManager();
        try {
            ApplicationInfo appinfo = manager.getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);

            Bundle b = appinfo.metaData;
            if(b != null && b.getInt("io.lqd.sdk.notification_icon",0) > 0)
                return b.getInt("io.lqd.sdk.notification_icon");

            return appinfo.icon;
        } catch (NameNotFoundException e) {
            return android.R.drawable.sym_def_app_icon;
        }
    }

    private static Intent getIntent(Context context) {
        PackageManager manager = context.getPackageManager();
        return manager.getLaunchIntentForPackage(context.getPackageName());
    }

    public static boolean isLiquidPush(Intent intent) {
        return intent.getStringExtra(LIQUID_MESSAGE_EXTRA) != null;
    }

}
