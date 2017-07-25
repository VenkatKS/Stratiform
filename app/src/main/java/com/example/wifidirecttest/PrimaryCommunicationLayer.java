package com.example.wifidirecttest;

import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Venkat on 4/21/16.
 */
public class PrimaryCommunicationLayer
{
    SecondLevelCommunicationLayer RecieverLayer;
    public static int dataSpeed = 50000; // 10 bps

    WifiP2pManager mManager;   //the required infrastructure for the madapp app
    WifiP2pManager.Channel mChannel;
    Container CommunicationFIFO;

    IntentFilter mIntentFilter;


    public void initialize(AppCompatActivity mainActivity)
    {
        WifiManager nManager;
        CommunicationFIFO = new Container();
        Bundle extras = mainActivity.getIntent().getExtras();
        if (extras != null)
        {
            //userId = extras.getString(Constants.USER_ID_KEY);
        } else
        {
            //userId = getApplicationContext().getSharedPreferences("ViewStreamPrefs", 0).getString("userId",null);
        }

        mManager = (WifiP2pManager) mainActivity.getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(mainActivity, mainActivity.getMainLooper(), null);
        RecieverLayer = new SecondLevelCommunicationLayer(mManager, mChannel, mainActivity, CommunicationFIFO);

        //Assign the handlers for the intents
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
    }


    public void broadcast(byte[] array, String destination) throws IOException
    {
        LowerLevelWrapper nextMessage = new LowerLevelWrapper(array, destination, SecondLevelCommunicationLayer.getMyID(), LowerLevelWrapper.beaconMsgType.USER);

        CommunicationFIFO.putNextPacket(nextMessage.serialize());
    }

    public void tcpBroadcast(byte[] array, String destination) throws IOException
    {
        LowerLevelWrapper nextMessage = new LowerLevelWrapper(array, destination, SecondLevelCommunicationLayer.getMyID(), LowerLevelWrapper.beaconMsgType.TCPFollowUp);
        CommunicationFIFO.putNextPacket(nextMessage.serialize());
    }
    public byte[] receiveBlocking() throws IOException
    {
        while(CommunicationFIFO.getRxFifoSize() == 0) {}
        byte[] msg = CommunicationFIFO.getNextPacket();

        return msg;
    }

    public ArrayList<String> getAllPeers()
    {
        return new ArrayList<String>(SecondLevelCommunicationLayer.getPeersMAC());
    }

    public String getDeviceID()
    {
        return SecondLevelCommunicationLayer.getMyID();
    }

    public int getNumberOfPeers()
    {
        return SecondLevelCommunicationLayer.getPeersMAC().size();
    }

}
