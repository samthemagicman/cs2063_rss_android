package ca.unb.mobiledev.rss;

import android.app.NotificationManager;
import android.content.Context;

import androidx.core.app.NotificationCompat;

public class Notifier
{
    NotificationCompat.Builder m_builder;
    NotificationManager m_manager;

    int m_lastUsedNotificationId = 0;

    public Notifier(Context activityContext)
    {
        m_builder = new NotificationCompat.Builder(activityContext);
        m_builder.setSmallIcon(R.drawable.common_google_signin_btn_icon_light);

        m_manager = (NotificationManager) activityContext.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public void postNotification(String titleText, String infoText)
    {
        m_builder.setContentTitle(titleText);
        m_builder.setContentText(infoText);

        int notificationID = m_lastUsedNotificationId; //Allows you to update the notification later on.
        m_manager.notify(notificationID, m_builder.build());
    }
}
