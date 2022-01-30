package ca.unb.mobiledev.rss;

import android.app.NotificationManager;
import android.content.Context;

import androidx.core.app.NotificationCompat;

public class Notifier
{
    NotificationCompat.Builder m_builder;
    Context m_context;

    public Notifier(Context activityContext)
    {
        m_context = activityContext;
        m_builder = new NotificationCompat.Builder(activityContext);
        m_builder.setSmallIcon(R.drawable.common_google_signin_btn_icon_light);
        //builder.setContentTitle("New Items posted to your rss feed");
        //builder.setContentTitle("The Item XXX has been added - Maybe I should also update you on price changes?");
    }

    public void postNotification(String titleText, String infoText)
    {
        m_builder.setContentTitle(titleText);
        m_builder.setContentText(infoText);

        NotificationManager notificationManager = (NotificationManager)m_context.getSystemService(Context.NOTIFICATION_SERVICE);

        int notificationID = 0; //Allows you to update the notification later on.
        notificationManager.notify(notificationID, m_builder.build());
    }
}
