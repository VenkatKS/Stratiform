package com.example.wifidirecttest;

import java.util.LinkedList;
import java.util.concurrent.Semaphore;

/**
 * Created by Venkat on 11/9/14.
 */
public class Container
{

    LinkedList<byte[]> RxFIFO = new LinkedList<byte[]>();  //contains upto 1000 items. store this in the stack
    LinkedList<byte[]> TxFIFO = new LinkedList<byte[]>();  //contains upto 1000 items. store this in the stack

    Semaphore RxMutex = new Semaphore(1);
    Semaphore TxMutex = new Semaphore(1);

    public Container()
    {

    }

    public byte[] getNextPacket()
    {
        try
        {
            RxMutex.acquire();
        } catch (InterruptedException e)
        {
            return null;
        }
        if (RxFIFO.size() == 0)
        {
            RxMutex.release();
            return null;
        }
        byte[] nextPacket = RxFIFO.remove();
        RxMutex.release();

        return nextPacket;
    }

    public void putNextPacket(byte[] data)
    {
        while(true)
        {
            try {
                TxMutex.acquire();
                break;
            } catch (InterruptedException e) {
                continue;
            }
        }

        TxFIFO.add(data);

        TxMutex.release();
    }


    public void newPacketReceived(byte[] data)
    {
        while(true)
        {
            try {
                RxMutex.acquire();
                break;
            } catch (InterruptedException e) {
                continue;
            }
        }

        RxFIFO.add(data);

        RxMutex.release();
    }

    public byte[] getNextPacketToBroadcast()
    {
        while(true)
        {
            try {
                TxMutex.acquire();
                break;
            } catch (InterruptedException e) {
                continue;
            }
        }

        if (TxFIFO.size() == 0)
        {
            TxMutex.release();
            return null;
        }

        byte[] nextBroadcast = TxFIFO.remove();
        TxMutex.release();

        return nextBroadcast;
    }

    public int getRxFifoSize()
    {
        return RxFIFO.size();
    }

    public int getTxFifoSize()
    {
        return TxFIFO.size();
    }
}