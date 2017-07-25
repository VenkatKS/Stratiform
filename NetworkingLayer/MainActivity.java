package com.example.venkat.wifidirecttest;

import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.net.DatagramPacket;

public class MainActivity extends AppCompatActivity {
    PrimaryCommunicationLayer mainLayer;
    public static int dataSpeed = 50000; // 10 bps

    WifiP2pManager mManager;   //the required infrastructure for the madapp app
    WifiP2pManager.Channel mChannel;
    Container CommunicationFIFO;

    boolean packetRecv = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        mainLayer = new PrimaryCommunicationLayer();
        mainLayer.initialize(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Thread recth = new Thread(new recx(this));
        recth.start();

        while(true)
        {
            byte[] chunkBuf = (new String("phone 1")).getBytes();
            mainLayer.broadcast(chunkBuf);
            break;
        }
    }


    class recx implements Runnable
    {
        Context appContext;

        public recx(Context appctx)
        {
            appContext = appctx;
        }
        @Override
        public void run() {
            byte [] recvBuf = mainLayer.receiveBlocking();
            Integer len = recvBuf.length;
            String item = new String(recvBuf);
            Log.d("New Receive: ", item);
        }
    }
}
