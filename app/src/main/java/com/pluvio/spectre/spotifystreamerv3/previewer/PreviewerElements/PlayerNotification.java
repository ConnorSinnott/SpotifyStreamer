package com.pluvio.spectre.spotifystreamerv3.previewer.PreviewerElements;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.widget.RemoteViews;

import com.pluvio.spectre.spotifystreamerv3.R;
import com.pluvio.spectre.spotifystreamerv3.activities.MainActivity;
import com.pluvio.spectre.spotifystreamerv3.previewer.PreviewController;

/**
 * Created by Spectre on 8/15/2015.
 */
public class PlayerNotification {

    private final String LOG_TAG = "Notification";

    private Context mContext;

    private boolean isVisible = false;

    public PlayerNotification(Context context) {
        mContext = context;
    }

    public void updateNotification() {

        // Check preferences to see if notifications are allowed
        if (!PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean(mContext.getString(R.string.pref_notification_key), true))
            return;

        isVisible = true;

        Intent intent_Play = new Intent("PreviewPlayerIntent");
        intent_Play.putExtra(PreviewController.PlayerControls.REQUEST_KEY, PreviewController.PlayerControls.REQUEST_TOGGLE_PLAY);

        Intent intent_Next = new Intent("PreviewPlayerIntent");
        intent_Next.putExtra(PreviewController.PlayerControls.REQUEST_KEY, PreviewController.PlayerControls.REQUEST_NEXT_TRACK);

        Intent intent_Previous = new Intent("PreviewPlayerIntent");
        intent_Previous.putExtra(PreviewController.PlayerControls.REQUEST_KEY, PreviewController.PlayerControls.REQUEST_PREVIOUS_TRACK);

        Intent intent_Restore = new Intent(mContext, MainActivity.class);
        intent_Restore.setAction(Intent.ACTION_MAIN);
        intent_Restore.addCategory(Intent.CATEGORY_LAUNCHER);
        intent_Restore.putExtra(PreviewController.PlayerControls.REQUEST_KEY, true);

        PendingIntent pendingI_Play = PendingIntent.getBroadcast(mContext, 0, intent_Play, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent pendingI_Next = PendingIntent.getBroadcast(mContext, 1, intent_Next, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent pendingI_Prev = PendingIntent.getBroadcast(mContext, 2, intent_Previous, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent pendingI_Rest = PendingIntent.getActivity(mContext, 3, intent_Restore, PendingIntent.FLAG_UPDATE_CURRENT);

        RemoteViews contentView = new RemoteViews(mContext.getPackageName(), R.layout.notification_default);
        contentView.setViewVisibility(R.id.notification_next, PreviewController.hasNext() ? View.VISIBLE : View.INVISIBLE);
        contentView.setViewVisibility(R.id.notification_last, PreviewController.hasPrevious() ? View.VISIBLE : View.INVISIBLE);
        contentView.setTextViewText(R.id.notification_song, PreviewController.getTrackName());
        contentView.setImageViewBitmap(R.id.notification_icon, PreviewController.getAlbumArt());
        contentView.setOnClickPendingIntent(R.id.notification_play, pendingI_Play);
        contentView.setOnClickPendingIntent(R.id.notification_last, pendingI_Prev);
        contentView.setOnClickPendingIntent(R.id.notification_next, pendingI_Next);
        contentView.setOnClickPendingIntent(R.id.notification_icon, pendingI_Rest);

        NotificationManager manager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification playerNotification = new NotificationCompat.Builder(mContext)
                .setSmallIcon(R.drawable.play_button)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setContent(contentView)
                .build();

        manager.notify(1, playerNotification);

    }

    public void clearNotification() {
        NotificationManager manager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(1);
        isVisible = false;
    }

    public boolean isVisible() {
        return isVisible;
    }

}
