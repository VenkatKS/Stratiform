package com.example.wifidirecttest;

import java.net.DatagramPacket;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * Created by Venkat on 4/29/16.
 */
public class TCPConnections implements Delayed
{
    public static String destinationID;
    public static String sourceID;
    public static String messageID;

    public DatagramPacket payLoad;


    private long expiryTime;

    public TCPConnections(DatagramPacket payLoad, String destinationID, String msgID, long delay)
    {
        this.destinationID = destinationID;
        this.payLoad = payLoad;
        this.expiryTime = System.currentTimeMillis() + delay;
        this.sourceID = SecondLevelCommunicationLayer.getMyID();
        this.messageID = msgID;

    }

    @Override
    public long getDelay(TimeUnit timeUnit)
    {
        long diff = expiryTime - System.currentTimeMillis();
        return timeUnit.convert(diff, TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(Delayed delayed)
    {
        if (this.expiryTime < ((TCPConnections) delayed).expiryTime) {
            return -1;
        }
        if (this.expiryTime > ((TCPConnections) delayed).expiryTime) {
            return 1;
        }
        return 0;
    }

    @Override
    public boolean equals(Object other)
    {
        if (!other.getClass().equals(this.getClass())) return false;

        TCPConnections otherClass = (TCPConnections) other;

        if (otherClass.destinationID.equals(this.destinationID) && otherClass.sourceID.equals(this.sourceID) && otherClass.messageID.equals(this.messageID)) return true;

        return false;

    }
}
