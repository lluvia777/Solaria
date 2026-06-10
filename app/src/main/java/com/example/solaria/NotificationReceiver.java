package com.example.solaria;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.core.app.NotificationCompat;

    public class NotificationReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            double uvIndex = intent.getDoubleExtra("uv_index", 0);
            String riskLevel = RecommendationEngine.getRiskLevel(uvIndex);
            String reapplyRule = RecommendationEngine.getReapplyRule(uvIndex);

            // Uygulamayı açacak intent
            Intent openAppIntent = new Intent(context, MainActivity.class);
            openAppIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            PendingIntent pendingIntent = PendingIntent.getActivity(
                    context, 0, openAppIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            // Bildirimi oluştur
            NotificationCompat.Builder builder = new NotificationCompat.Builder(
                    context, NotificationHelper.CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle("Güneş Kremi Zamanı! ☀️")
                    .setContentText(reapplyRule)
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText("UV Seviyesi: " + uvIndex + " (" + riskLevel + ")\n" + reapplyRule))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true);

            NotificationManager manager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.notify(NotificationHelper.NOTIFICATION_ID, builder.build());
            }

            // Bir sonraki bildirimi yeniden zamanla
            NotificationHelper helper = new NotificationHelper(context);
            helper.scheduleNotification(uvIndex);
        }
    }

