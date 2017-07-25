package com.example.wifidirecttest;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import com.example.NodeManager.NodeManager;
import com.example.saket.distributedmm.MyActivity;

import java.io.IOException;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Random;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class SecondLevelCommunicationLayer extends BroadcastReceiver {

    private static WifiP2pManager mManager;
    private static Channel mChannel;
    private static Activity mActivity;
    private static WifiManager CommonManager;   //Common non-p2p version of the wireless manager
    PeerListListener myPeerListListener;

    private static ArrayList<WifiP2pDevice> peersList = new ArrayList<WifiP2pDevice>();
    private static WifiP2pDevice groupOwner;

    private static Container mCont;

    private static PowerManager powerManager;

    private static PowerManager.WakeLock powerGrabber;

    //METRICS
    private static AtomicLong packetSentCount = new AtomicLong(0);
    private static AtomicLong byteSentCount = new AtomicLong(0);
    private static AtomicBoolean sentAccessLock = new AtomicBoolean(false);

    private static AtomicLong packetRxCount = new AtomicLong(0);
    private static AtomicLong byteRxCount = new AtomicLong(0);
    private static AtomicBoolean rxAccessLock = new AtomicBoolean(false);

    // My mac address
    private static String MyNetworkAddress;
    private static ArrayList<String> CONNECTED_PEERS =  new ArrayList<String>();

    // TCP Infrastructure
    static DelayQueue<TCPConnections> ActiveTCPConnections = new DelayQueue<TCPConnections>();
    final static int TCPDelayMS = 1000;

    public static String getMyID()
    {
        return new String(MyNetworkAddress);
    }

    public static ArrayList<String> getPeerList()
    {
        return new ArrayList<String>(CONNECTED_PEERS);
    }

    public static int getNumPeers()
    {
        return CONNECTED_PEERS.size();
    }

    //private static int socketPort = 15270;
    public SecondLevelCommunicationLayer(WifiP2pManager manager, Channel channel,
                                         Activity activity, Container items) {
        super();
        this.mManager = manager;
        this.mChannel = channel;
        this.mActivity = activity;
        this.CommonManager = (WifiManager) mActivity.getSystemService(Context.WIFI_SERVICE);
        mCont = items;

        BigInteger peerID = new BigInteger(256, new Random());
        SecondLevelCommunicationLayer.MyNetworkAddress = peerID.toString();
        CONNECTED_PEERS.add(MyNetworkAddress);
        //MyActivity.nodeManager = new NodeManager();
        //MyActivity.nodeManager.myMACList = new ArrayList<String>(CONNECTED_PEERS);
        for (int i = 0; i < 50; i++)
        {
            LowerLevelWrapper firstMacBroadcast = new LowerLevelWrapper(null, null, getMyID(), LowerLevelWrapper.beaconMsgType.MAC);

            Log.d("AppInfo", "My ID: " + MyNetworkAddress);
            try {
                mCont.putNextPacket(firstMacBroadcast.serialize());
            } catch (IOException e) {
                Log.e("AppInfo", "Could not send first MAC broadcast");
            }
        }

        Broadcast_Init();
        Receiver_Init();



    }

    public static ArrayList<String> getPeersMAC()
    {
        return CONNECTED_PEERS;
    }

    public static void clearMetrics(){
        packetSentCount.set(0);
        packetRxCount.set(0);
        byteRxCount.set(0);
        byteSentCount.set(0);
    }
    public static Context getAppContext(){
        return mActivity;
    }
    public static WifiP2pDevice getGroupOwner(){
        return groupOwner;
    }
    public static ArrayList<WifiP2pDevice> getPeers(){
        return peersList;
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        //Start the next intent.
                    }
                    @Override
                    public void onFailure(int reasonCode) {
                        //Something went wrong.
                        Toast.makeText(mActivity, "Discovery failed for some reason.", Toast.LENGTH_SHORT);
                    }
                });
            } else {
                Toast.makeText(mActivity, "Error: WiFi Direct is not enabled.", Toast.LENGTH_SHORT).show();
            }
        } else {
            if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
                if (mManager != null) {
                    mManager.requestPeers(mChannel, new PeerListListener() {
                        @Override
                        public void onPeersAvailable(WifiP2pDeviceList peers) {
                            peersList.clear();  //Remove the previous list of peers and repopulate it
                            peersList.addAll(peers.getDeviceList());
                            Log.d("MadApp", String.format("PeerListListener: %d peers available, updating device list", peers.getDeviceList().size()));

                            for(WifiP2pDevice k: peersList){
                                if(k.isGroupOwner()){
                                    groupOwner = k;
                                    Log.d("MadApp", "Group Owner Identified: "+k.deviceName);
                                }
                            }

                            if (peersList.size() == 0) {
                                Log.d("MadApp", "No devices found");
                                return;
                            }


                        }
                    });
                }
            } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
                // Respond to new connection or disconnections
            } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
                // Respond to this device's wifi state changing
            }
        }
    }

    //Counter operations
    public static long getPacketSent(){
        //while(sentAccessLock.get()==true){}  //spin while resource is locked.
        return packetSentCount.get();
    }
    public static long getBytesSent(){
        //while(sentAccessLock.get()==true){}  //spin while resource is locked.
        return byteSentCount.get();
    }
    public static long getPacketsRx(){
        //while(rxAccessLock.get()==true){}  //spin while resource is locked.
        return packetRxCount.get();
    }
    public static long getBytesRx(){
        // while(rxAccessLock.get()==true){}  //spin while resource is locked.
        return byteRxCount.get();
    }

    public static void resetAllMetrics(){
        while(rxAccessLock.get()==true || sentAccessLock.get()==true){}  //spin while resources are locked.
        rxAccessLock.set(true);
        sentAccessLock.set(true);  //lock the resources, not needed since only accessed by main thread, but implemented for
        // sake of symmetry
        packetSentCount.set(0);
        byteSentCount.set(0);
        packetRxCount.set(0);
        byteRxCount.set(0);
        rxAccessLock.set(false);
        sentAccessLock.set(false); //release the resource
    }

    public static void resetSentMetrics(){
        while(sentAccessLock.get()==true){}  //spin while resources are locked.

        sentAccessLock.set(true);  //lock the resources, not needed since only accessed by main thread, but implemented for
        // sake of symmetry
        packetSentCount.set(0);
        byteSentCount.set(0);
        sentAccessLock.set(false); //release the resource
    }
    public static void resetRxMetrics(){
        while(rxAccessLock.get()==true){}  //spin while resources are locked.
        rxAccessLock.set(true);  //lock the resources, not needed since only accessed by main thread, but implemented for sake of symmetry

        packetRxCount.set(0);
        byteRxCount.set(0);
        rxAccessLock.set(false);
    }

    public static void Stay_Awake(){
        powerManager = (PowerManager) mActivity.getSystemService(Context.POWER_SERVICE);
        powerGrabber = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "MyWakelockTag");

        powerGrabber.acquire();

    }

    public static void Go_Sleep(){
        powerGrabber.release();
    }
    public static void Broadcast_Init(){   //Initalization function for the broadcaster, call this before broadcasting anything
        Log.d("Process", "Broadcast service initiated.");
        Thread broadProcess = new Thread(new Broadcaster());
        Thread TCPProcess = new Thread(new TCPEnforcer());
        broadProcess.start();
        TCPProcess.start();
    }

    public static void Receiver_Init(){   //Initialize this if you want the thread to give you items
        Thread processRecv = new Thread(new Receiver());
        processRecv.start();
        Log.d("Message", "Recv Process Started.");
    }


    private static class Receiver implements Runnable
    {
        DatagramSocket serverSocket;
        byte[] RxArray;

        @Override
        public void run()
        {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
            try
            {
                WifiManager.MulticastLock lock = CommonManager.createMulticastLock("dk.aboaya.pingpong");
                lock.acquire();
                serverSocket = new DatagramSocket(Information.RECEIVER_PORT);
                while (true)
                {
                    RxArray = new byte[serverSocket.getReceiveBufferSize()];
                    DatagramPacket rxPacket = new DatagramPacket(RxArray, serverSocket.getReceiveBufferSize());
                    serverSocket.receive(rxPacket);
                    if (rxPacket.getData() != null)
                    {
                        try
                        {
                            LowerLevelWrapper nextMessage = LowerLevelWrapper.deserialize(rxPacket.getData());
                            LowerLevelWrapper.beaconMsgType messageKind = nextMessage.getMsgType();

                            switch(messageKind)
                            {
                                case ACK:
                                    TCPConnections removeConnection = new TCPConnections(rxPacket, nextMessage.destinationMAC, nextMessage.ackMessageID, TCPDelayMS);
                                    if (nextMessage.originMAC.equals(getMyID())) break;
                                    if (ActiveTCPConnections.remove(removeConnection) == true) Log.d("AppInfo", "Removed connection for " + nextMessage.ackMessageID + " (" + nextMessage.messageID + ")");
                                    else Log.d("AppInfo", "Failed to remove connection for " + nextMessage.ackMessageID + " (" + nextMessage.messageID + ")");
                                    break;
                                case MAC:
                                    Log.d("AppInfo", "Device MAC: " + nextMessage.originMAC);
                                    if (!CONNECTED_PEERS.contains(nextMessage.originMAC))
                                    {
                                        CONNECTED_PEERS.add(nextMessage.originMAC);
                                        LowerLevelWrapper nextAnnouncement = new LowerLevelWrapper(null, null, MyNetworkAddress, LowerLevelWrapper.beaconMsgType.MAC);
                                        mCont.putNextPacket(nextAnnouncement.serialize());
                                        Log.d("AppInfo", "New Phone added. " + nextMessage.originMAC);
                                        //MyActivity.nodeManager.myMACList = new ArrayList<String>(CONNECTED_PEERS);  //tentative
                                    }
                                    break;
                                case TCPFollowUp:
                                case USER:
                                    if (nextMessage.destinationMAC!= null && (nextMessage.getMsgType() == LowerLevelWrapper.beaconMsgType.USER || nextMessage.getMsgType() == LowerLevelWrapper.beaconMsgType.TCPFollowUp) && !nextMessage.originMAC.equals(getMyID()))
                                    {
                                        LowerLevelWrapper ackMessage = new LowerLevelWrapper(null, nextMessage.originMAC, getMyID(), LowerLevelWrapper.beaconMsgType.ACK);
                                        ackMessage.ackMessageID = new String(nextMessage.messageID);
                                        mCont.putNextPacket(ackMessage.serialize());
                                    }
                                    mCont.newPacketReceived(nextMessage.payLoad);
                                    Log.d("AppInfo", "New Packet (" + rxPacket.getLength()+"): " + nextMessage.messageID);
                                    break;
                            }

                        }
                        catch(ClassNotFoundException e)
                        {
                            continue;
                        }
                        catch(IOException e)
                        {
                            continue;
                        }
                    }
                }
            }
            catch(Exception e)
            {
                Log.e("AppInfo", "Receive Thread Error: " + e);
            }
        }
    }

    private static class Broadcaster implements Runnable
    {
        DatagramSocket UDPSocket;
        @Override
        public void run()
        {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
            try
            {
                UDPSocket = new DatagramSocket(Information.BROADCASTER_PORT);
                UDPSocket.setReuseAddress(true);
                UDPSocket.setBroadcast(true);
                Log.d("AppInfo", "Broadcast Socket Initialized");

                while(true)
                {
                    while(mCont.getTxFifoSize() == 0) {}
                    byte[] nextBroadcastPacket = mCont.getNextPacketToBroadcast();
                    if (nextBroadcastPacket == null) continue;
                    DatagramPacket nextPacket = new DatagramPacket(nextBroadcastPacket, nextBroadcastPacket.length, SecondLevelCommunicationLayer.getBroadcastIP(), Information.RECEIVER_PORT);
                    /* Check and see if TCP Connections are needed */
                    try
                    {
                        LowerLevelWrapper nextMessage = LowerLevelWrapper.deserialize(nextBroadcastPacket);
                        Log.d("AppInfo", "Sending data (type " + nextMessage.getMsgType() + ") ID: " + nextMessage.messageID);

                        if (nextMessage.destinationMAC!= null && nextMessage.getMsgType() == LowerLevelWrapper.beaconMsgType.USER)
                        {
                            TCPConnections nextConnection = new TCPConnections(nextPacket, nextMessage.destinationMAC, nextMessage.messageID, TCPDelayMS);
                            ActiveTCPConnections.add(nextConnection);
                            Log.d("AppInfo", "Added new connection for " + nextConnection.messageID);
                        }
                    }
                    catch(ClassNotFoundException e)
                    {
                            /* No TCP */
                    }
                    catch(IOException i)
                    {
                            /* No TCP */
                    }
                    UDPSocket.send(nextPacket);
                }
            }
            catch(Exception e)
            {
                Log.e("AppInfo", "Broadcast Thread Error: " + e);
            }

        }
    }

    public static class TCPEnforcer implements Runnable
    {
        @Override
        public void run()
        {
            while(true)
            {
                try
                {
                    /* Take out the next TCP connection that isn't done yet */
                    TCPConnections nextActive = ActiveTCPConnections.take();
                    if (nextActive == null) continue;
                    LowerLevelWrapper msgAgain = LowerLevelWrapper.deserialize(nextActive.payLoad.getData());

                    /* Wrap it up into a new package */
                    LowerLevelWrapper nextMessage = new LowerLevelWrapper(msgAgain.payLoad, msgAgain.destinationMAC, SecondLevelCommunicationLayer.getMyID(), LowerLevelWrapper.beaconMsgType.TCPFollowUp);

                    /* Rebroadcast it */
                    mCont.putNextPacket(nextMessage.serialize());

                    /* Add the tcp connection back */
                    TCPConnections again = new TCPConnections(nextActive.payLoad, nextActive.destinationID, nextMessage.messageID, TCPDelayMS);
                    ActiveTCPConnections.add(again);
                    Log.d("AppInfo", "Rebroadcasting");
                }
                catch(Exception e)
                {
                    continue;
                }


            }
        }
    }
    public static int getPacketPort(){
        return Information.RECEIVER_PORT;
    }
    public static InetAddress getBroadcastIP(){
        /*try {
            return getBroadcastAddress();
        } catch (IOException e) {
            e.printStackTrace();

        }*/
        InetAddress addr = null;
        try {
            addr = InetAddress.getByName("192.168.49.255");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return addr;
    }

    private static String getDottedDecimalIP(byte[] ipAddr) {
        //convert to dotted decimal notation:
        String ipAddrStr = "";
        for (int i=0; i<ipAddr.length; i++) {
            if (i > 0) {
                ipAddrStr += ".";
            }
            ipAddrStr += ipAddr[i]&0xFF;
        }
        return ipAddrStr;
    }

    private static byte[] getLocalIPAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        if (inetAddress instanceof Inet4Address) { // fix for Galaxy Nexus. IPv4 is easy to use :-)
                            return inetAddress.getAddress();
                        }
                        //return inetAddress.getHostAddress().toString(); // Galaxy Nexus returns IPv6
                    }
                }
            }
        } catch (SocketException ex) {
        } catch (NullPointerException ex) {
        }
        return null;
    }
}