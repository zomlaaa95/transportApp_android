package services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.RemoteMessage;
import com.transportapp.lazar.transportapp.R;
import com.transportapp.lazar.transportapp.activities.RequestActivity;

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        showNotification(remoteMessage.getData().get("message"));
    }

    private void showNotification(String message) {
        Intent i = new Intent(this, RequestActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // to do
        String[] itemInfo = message.split("\\|");

        i.putExtra("id", itemInfo[0]);
        i.putExtra("name", itemInfo[1]);
        i.putExtra("description", itemInfo[2]);
        i.putExtra("image", itemInfo[3]);

        i.putExtra("bids", true);

        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,i,PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setAutoCancel(true)
                .setContentTitle("The Hammer")
                .setContentText("Your bid for item " + itemInfo[1] + " has been passed!")
                .setSmallIcon(R.drawable.logo)
                .setContentIntent(pendingIntent);

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        manager.notify(0,builder.build());
    }
}
