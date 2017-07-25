package com.example.MatrixMultiplication;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Created by Venkat on 5/1/16.
 */
public class Sendable implements Serializable {

    byte[] serializedExecutable;
    enum message_type{
        COMPUTE,
        RESULT
    };
    message_type type;
    public String origin;
    public String destination;


    public Sendable(Executable e, String typeString, String origin, String destination){
        this.serializedExecutable = e.serialize();
        if("COMPUTE".equals(typeString.toUpperCase())){
            type = message_type.COMPUTE;
        } else{
            type = message_type.RESULT;
        }
        this.origin = origin;
        this.destination = destination;
    }

    public Executable getExecutable(){
        return Executable.deserialize(serializedExecutable);
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

    public static Sendable deserialize(byte[] data){
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        Sendable object = null;
        try{
            ObjectInputStream is = new ObjectInputStream(in);
            object = (Sendable) is.readObject();
        } catch (IOException e){
            e.printStackTrace();
        } catch (ClassNotFoundException e){
            e.printStackTrace();
        }

        return object;
    }



    public boolean isCompute(){
        return type == message_type.COMPUTE;
    }

    public boolean isResult(){
        return type == message_type.RESULT;
    }


}
