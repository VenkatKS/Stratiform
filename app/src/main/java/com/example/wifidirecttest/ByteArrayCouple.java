package com.example.wifidirecttest;

/**
 * Created by Venkat on 4/21/16.
 */
public class ByteArrayCouple
{
    byte[] data;
    int size;
    public ByteArrayCouple(int size)
    {
        data = new byte[size];
        this.size = size;
    }
}
