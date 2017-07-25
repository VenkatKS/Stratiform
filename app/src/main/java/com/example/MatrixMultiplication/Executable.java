package com.example.MatrixMultiplication;

import android.util.Log;

import java.io.*;


public abstract class Executable<C> implements Serializable{

    public int destNode;
    public int originNode;
    private boolean executed;

    public void setNodes(int destNode, int originNode){ //destNode is nodeID of phone that will run execute, originNode is nodeID of phone that initiated calculation (split and combine data)
        this.destNode = destNode;
        this.originNode = originNode;
    }

    public boolean executeMessage (int myID){   //check to see if I should run execute on this Executable
        return myID == destNode && !executed;
    }

    public boolean combineMessage (int myID){   //check to see if this is a return message and I should add to an ArrayList to be combined later
        return myID == originNode && myID == destNode && executed;
    }

    public byte[] serialize(){
        byte[] data = null;
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ObjectOutputStream os = new ObjectOutputStream(outputStream);
            os.writeObject(this);
            data = outputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }

    public static Executable deserialize(byte[] data){
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        Executable object = null;
        try{
            ObjectInputStream is = new ObjectInputStream(in);
            object = (Executable) is.readObject();
        } catch (IOException e){
            e.printStackTrace();
        } catch (ClassNotFoundException e){
            e.printStackTrace();
        }

        return object;
    }

    public C execute(){
        executed = true;
        Log.d("EXECUTE: ", "Starting execution of multiplication");
        return executeComp();
    }

    protected abstract C executeComp();    //needed if execution returns object of different type
    //simply send a message with result of execute (along with destination node)

}
