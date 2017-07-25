package com.example.wifidirecttest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Random;

/**
 * Created by Venkat on 4/29/16.
 */
public class LowerLevelWrapper implements Serializable
{
    String destinationMAC;
    String originMAC;

    String messageID;

    private beaconMsgType msg;

    byte[] payLoad;

    boolean needsTCP;

    public String ackMessageID;

    enum beaconMsgType
    {
        ACK,
        MAC,
        USER,
        TCPFollowUp
    };

    public LowerLevelWrapper(byte[] dataPayLoad, String destinationMAC, String originMAC, beaconMsgType messageType)
    {
        try {
            this.payLoad = Arrays.copyOf(dataPayLoad, dataPayLoad.length);
        }
        catch(Exception e)
        {
            this.payLoad = null;
            /* Special message */
        }

        BigInteger msgID = new BigInteger(256, new Random());
        this.messageID = msgID.toString();
        this.destinationMAC = destinationMAC;
        this.msg = messageType;
        this.originMAC = originMAC;
        // This message requires tcp support if the destination is not null or if it's not a networking beacon message
        this.needsTCP = (!(isMAC() || isAck())) && (destinationMAC != null);
    }


    public boolean isAck()
    {
        return (msg == beaconMsgType.ACK);
    }

    public boolean isMAC()
    {
        return (msg == beaconMsgType.MAC);
    }

    public beaconMsgType getMsgType()
    {
        return msg;
    }

    public byte[] serialize() throws IOException
    {
        final ByteArrayOutputStream BinaryOutput = new ByteArrayOutputStream();
        final ObjectOutputStream ObjectOutput = new ObjectOutputStream(BinaryOutput);
        ObjectOutput.writeObject(this);
        final byte[] serialized = BinaryOutput.toByteArray();

        return serialized;
    }

    public static LowerLevelWrapper deserialize(byte[] objectData) throws IOException, ClassNotFoundException
    {
        ByteArrayInputStream BinaryInput = new ByteArrayInputStream(objectData);
        ObjectInputStream ObjectOutput = new ObjectInputStream(BinaryInput);
        LowerLevelWrapper nextMessage = (LowerLevelWrapper) ObjectOutput.readObject();

        return nextMessage;
    }
}
