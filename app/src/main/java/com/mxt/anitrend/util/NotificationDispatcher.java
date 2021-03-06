package com.mxt.anitrend.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.NotificationTarget;
import com.mxt.anitrend.R;
import com.mxt.anitrend.api.structure.UserNotification;
import com.mxt.anitrend.event.NotificationClickListener;
import com.mxt.anitrend.view.index.activity.NotificationActivity;

import java.util.List;
import java.util.Locale;

/**
 * Created by Maxwell on 1/22/2017.
 */

public final class NotificationDispatcher {

    private static final String NOTIFICATION_GROUP_KEY = "anitrend_notification_group";
    private static int NOTIFICATION_ID = 59;

    private static PendingIntent contentIntent(Context context){
        Intent activityStart = new Intent(context, NotificationActivity.class);

        // PendingIntent.FLAG_UPDATE_CURRENT will update notification
        NOTIFICATION_ID++;
        return PendingIntent.getActivity(context, NOTIFICATION_ID, activityStart, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static String getNotificationSound(Context context) {
        DefaultPreferences defaultPreferences = new DefaultPreferences(context);
        return defaultPreferences.getNotificationsSound();
    }

    public static void clearAllNotifications(Context context) {
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    public static void createNotification(Context context, List<UserNotification> notifications) {

        ApiPreferences apiPreferences = new ApiPreferences(context);

        for (UserNotification model: notifications) {

            String url_load;

            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.remote_notification_v2);
            NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(context)
                    .setSmallIcon(R.drawable.ic_new_releases_white_24dp)
                    .setSound(Uri.parse(getNotificationSound(context)))
                    .setContentIntent(contentIntent(context))
                    .setAutoCancel(true)
                    .setPriority(Notification.PRIORITY_DEFAULT);

            remoteViews.setImageViewResource(R.id.notification_img, R.drawable.ic_launcher);
            //remoteViews.setCharSequence(R.id.notification_time, "setText", String.format(Locale.getDefault(), "%s ", DateTimeConverter.getCurrentTime()));
            if(model.getObject_type() != NotificationClickListener.TYPE_AIRING) {
                url_load = model.getUser().getImage_url_lge();
                switch (model.getObject_type()){
                    case NotificationClickListener.TYPE_COMMENT_FORUM:
                        nBuilder.setContentTitle(model.getUser().getDisplay_name());
                        nBuilder.setContentText(String.format("%s", model.getComment().getComment()));
                        remoteViews.setCharSequence(R.id.notification_subject, "setText", context.getString(R.string.notification_user_comment_forum));
                        remoteViews.setCharSequence(R.id.notification_header, "setText", model.getUser().getDisplay_name());
                        remoteViews.setCharSequence(R.id.notification_content, "setText", String.format("%s", model.getComment().getComment()));
                        break;
                    case NotificationClickListener.TYPE_LIKE_FORUM:
                        nBuilder.setContentTitle(model.getUser().getDisplay_name());
                        nBuilder.setContentText(String.format("%s", model.getMeta_value()));
                        remoteViews.setCharSequence(R.id.notification_subject, "setText", context.getString(R.string.notification_user_like_forum));
                        remoteViews.setCharSequence(R.id.notification_header, "setText", model.getUser().getDisplay_name());
                        remoteViews.setCharSequence(R.id.notification_content, "setText", String.format("%s", model.getMeta_value()));
                        break;
                    case NotificationClickListener.TYPE_LIKE_ACTIVITY:
                        nBuilder.setContentTitle(model.getUser().getDisplay_name());
                        nBuilder.setContentText(String.format("%s", model.getMeta_value()));
                        remoteViews.setCharSequence(R.id.notification_subject, "setText", context.getString(R.string.notification_user_like_activity));
                        remoteViews.setCharSequence(R.id.notification_header, "setText", model.getUser().getDisplay_name());
                        remoteViews.setCharSequence(R.id.notification_content, "setText", String.format("%s", model.getMeta_value()));
                        break;
                    case NotificationClickListener.TYPE_REPLY_ACTIVITY:
                        nBuilder.setContentTitle(model.getUser().getDisplay_name());
                        nBuilder.setContentText(String.format("%s", model.getMeta_value()));
                        remoteViews.setCharSequence(R.id.notification_subject, "setText", context.getString(R.string.notification_user_reply_activity));
                        remoteViews.setCharSequence(R.id.notification_header, "setText", model.getUser().getDisplay_name());
                        remoteViews.setCharSequence(R.id.notification_content, "setText", String.format("%s", model.getMeta_value()));
                        break;
                    case NotificationClickListener.TYPE_REPLY_FORUM:
                        nBuilder.setContentTitle(model.getUser().getDisplay_name());
                        nBuilder.setContentText(String.format("%s", model.getComment().getComment()));
                        remoteViews.setCharSequence(R.id.notification_subject, "setText", context.getString(R.string.notification_user_reply_forum));
                        remoteViews.setCharSequence(R.id.notification_header, "setText", model.getUser().getDisplay_name());
                        remoteViews.setCharSequence(R.id.notification_content, "setText", String.format("%s", model.getComment().getComment()));
                        break;
                    case NotificationClickListener.TYPE_FOLLOW_ACTIVITY:
                        nBuilder.setContentTitle(model.getUser().getDisplay_name());
                        nBuilder.setContentText(String.format("%s", model.getMeta_value()));
                        remoteViews.setCharSequence(R.id.notification_subject, "setText", context.getString(R.string.notification_user_follow_activity));
                        remoteViews.setCharSequence(R.id.notification_header, "setText", model.getUser().getDisplay_name());
                        remoteViews.setCharSequence(R.id.notification_content, "setText", String.format("%s", model.getMeta_value()));
                        break;
                    case NotificationClickListener.TYPE_DIRECT_MESSAGE:
                        nBuilder.setContentTitle(model.getUser().getDisplay_name());
                        nBuilder.setContentText(String.format("%s", model.getMeta_value()));
                        remoteViews.setCharSequence(R.id.notification_subject, "setText", context.getString(R.string.notification_user_activity_message));
                        remoteViews.setCharSequence(R.id.notification_header, "setText", model.getUser().getDisplay_name());
                        remoteViews.setCharSequence(R.id.notification_content, "setText", String.format("%s", model.getMeta_value()));
                        break;
                    case NotificationClickListener.TYPE_LIKE_ACTIVITY_REPLY:
                        nBuilder.setContentTitle(model.getUser().getDisplay_name());
                        nBuilder.setContentText(String.format("%s", model.getMeta_value()));
                        remoteViews.setCharSequence(R.id.notification_subject, "setText", context.getString(R.string.notification_user_like_activity));
                        remoteViews.setCharSequence(R.id.notification_header, "setText", model.getUser().getDisplay_name());
                        remoteViews.setCharSequence(R.id.notification_content, "setText", String.format("%s", model.getMeta_value()));
                        break;
                    case NotificationClickListener.TYPE_MENTION_ACTIVITY:
                        nBuilder.setContentTitle(model.getUser().getDisplay_name());
                        nBuilder.setContentText(String.format("%s", model.getMeta_value()));
                        remoteViews.setCharSequence(R.id.notification_subject, "setText", context.getString(R.string.notification_user_activity_mention));
                        remoteViews.setCharSequence(R.id.notification_header, "setText", model.getUser().getDisplay_name());
                        remoteViews.setCharSequence(R.id.notification_content, "setText", String.format("%s", model.getMeta_value()));
                        break;
                    case NotificationClickListener.TYPE_LIKE_FORUM_COMMENT:
                        nBuilder.setContentTitle(model.getUser().getDisplay_name());
                        nBuilder.setContentText(String.format("%s", model.getMeta_value()));
                        remoteViews.setCharSequence(R.id.notification_subject, "setText", context.getString(R.string.notification_user_like_comment));
                        remoteViews.setCharSequence(R.id.notification_header, "setText", model.getUser().getDisplay_name());
                        remoteViews.setCharSequence(R.id.notification_content, "setText", String.format("%s", model.getMeta_value()));
                        break;
                    default:
                        nBuilder.setContentTitle(context.getString(R.string.notification_default));
                        nBuilder.setContentText(String.format("%s", model.getMeta_value()));
                        remoteViews.setCharSequence(R.id.notification_subject, "setText", context.getString(R.string.notification_default));
                        if(model.getUser() != null)
                            remoteViews.setCharSequence(R.id.notification_header, "setText", model.getUser().getDisplay_name());
                        else
                            remoteViews.setCharSequence(R.id.notification_header, "setText", "Unknown Origin");
                        remoteViews.setCharSequence(R.id.notification_content, "setText", String.format("%s", model.getMeta_value()));
                        break;
                }
            } else {
                url_load = model.getSeries().getImage_url_lge();

                nBuilder.setContentTitle(context.getString(R.string.notification_series));
                switch (apiPreferences.getTitleLanguage()) {
                    case "romaji":
                        nBuilder.setContentText(context.getString(R.string.notification_episode_short, model.getMeta_data(), model.getSeries().getTitle_romaji()));
                        remoteViews.setCharSequence(R.id.notification_content, "setText", context.getString(R.string.notification_episode_short, model.getMeta_data(), model.getSeries().getTitle_romaji()));
                        remoteViews.setCharSequence(R.id.notification_header, "setText", model.getSeries().getTitle_romaji());
                        break;
                    case "english":
                        nBuilder.setContentText(context.getString(R.string.notification_episode_short, model.getMeta_data(), model.getSeries().getTitle_english()));
                        remoteViews.setCharSequence(R.id.notification_content, "setText", context.getString(R.string.notification_episode_short, model.getMeta_data(), model.getSeries().getTitle_english()));
                        remoteViews.setCharSequence(R.id.notification_header, "setText", model.getSeries().getTitle_english());
                        break;
                    case "japanese":
                        nBuilder.setContentText(context.getString(R.string.notification_episode_short, model.getMeta_data(), model.getSeries().getTitle_japanese()));
                        remoteViews.setCharSequence(R.id.notification_content, "setText", context.getString(R.string.notification_episode_short, model.getMeta_data(), model.getSeries().getTitle_japanese()));
                        remoteViews.setCharSequence(R.id.notification_header, "setText", model.getSeries().getTitle_japanese());
                        break;
                }


                remoteViews.setCharSequence(R.id.notification_subject, "setText", context.getString(R.string.notification_series));
            }


            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                nBuilder.setGroup(NOTIFICATION_GROUP_KEY);

            nBuilder.setCustomBigContentView(remoteViews);
            final Notification notification = nBuilder.build();
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(NOTIFICATION_ID, notification);

            NotificationTarget notificationTarget = new NotificationTarget(
                    context,
                    remoteViews,
                    R.id.notification_img,
                    notification,
                    NOTIFICATION_ID);

            Glide.with(context).load(url_load)
                    .asBitmap().centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .into(notificationTarget);
        }

    }
}
