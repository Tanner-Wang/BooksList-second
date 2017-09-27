package com.example.android.bookslist;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Administrator on 2017/9/24.
 */

public class NetworkStateBroadcastReceiver extends BroadcastReceiver {
    private OnNetworkStateReadyListener mListener;

    public NetworkStateBroadcastReceiver(){}

    public NetworkStateBroadcastReceiver(OnNetworkStateReadyListener listener){
        this.mListener = listener;
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        mListener.onNetworkStateReady();
    }

    public interface OnNetworkStateReadyListener{
        void onNetworkStateReady();
    }
}
