package org.opentech;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by manan on 12-05-2015.
 */
public class ConnectionDetect {
    private Context context;

    public ConnectionDetect(Context context) {
        this.context = context;
    }

    public boolean isConnecting() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo[] info = cm.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;

                    }

                }
            }

        }
        return false;
    }
}

