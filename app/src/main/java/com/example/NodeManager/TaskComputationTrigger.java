package com.example.NodeManager;

import android.graphics.Matrix;
import android.util.Log;

import com.example.MatrixMultiplication.*;
import com.example.saket.distributedmm.MyActivity;

import org.w3c.dom.Node;

import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.Callable;

/**
 * Created by JMS on 4/23/2016.
 */
public class TaskComputationTrigger implements Callable<MatrixMultiplier>{
    Sendable myS;
    String myID;
    String myDestination;

    public TaskComputationTrigger(Sendable s, String id, String destination){
        myS = s;
        myID = id;
        myDestination = destination;
    }

    @Override
    public MatrixMultiplier call(){
        MatrixMultiplier result = null;

        try{
            MyActivity.mainLayer.broadcast(myS.serialize(), myDestination);
        } catch(IOException e){
            e.printStackTrace();
        }
        boolean received_result = false;

        byte[] data = new byte[1];

        while(!received_result) {
            Log.d("WAITING: ", "RESULTS");

            byte[] result_raw = NodeManager.ExtraMsgs.poll();
            int c =0;
            while(result_raw == null){
                //Log.d("Trying:", "ACQUIRE result other " + Integer.toString(c));
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                result_raw = NodeManager.ExtraMsgs.poll();
                c++;
            }

            Sendable in = Sendable.deserialize(result_raw);
            if(in.isResult() && in.destination.equals(myID)) {
                Log.d("RECIVED RESULT ", "Proceed to return");
                received_result = true;
                result = (MatrixMultiplier) in.getExecutable();
            }
        }

        return result;
    }
}
