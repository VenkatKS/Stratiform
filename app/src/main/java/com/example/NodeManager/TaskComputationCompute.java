package com.example.NodeManager;

import android.util.Log;

import com.example.MatrixMultiplication.Executable;
import com.example.MatrixMultiplication.MatrixMultiplier;
import com.example.MatrixMultiplication.Sendable;
import com.example.saket.distributedmm.MyActivity;

import java.io.IOException;
import java.util.concurrent.Callable;

/**
 * Created by JMS on 4/23/2016.
 */
public class TaskComputationCompute implements Callable<MatrixMultiplier>{

    String myID;

    public TaskComputationCompute(String id){
        myID = id;
    }

    @Override
    public MatrixMultiplier call(){
        Log.d("Waiting ", "Task receiver ready");
        String command = null;
        String target_id = null;
        while(true){
            MyActivity.nodeManager.myMACList = MyActivity.mainLayer.getAllPeers();
            Log.d("RECEIVED: ", "TRYING");
            byte[] recv_raw = new byte[100];
            try{
                recv_raw = MyActivity.mainLayer.receiveBlocking();
            } catch(IOException e) {e.printStackTrace();}

            Sendable s = Sendable.deserialize(recv_raw);
            if(s.isCompute() && s.destination.equals(myID)){
                Executable e = s.getExecutable();
                MatrixMultiplier x = (MatrixMultiplier) e.execute();
                Sendable r = new Sendable(x, "RESULT", myID, s.origin);

                try{
                    MyActivity.mainLayer.broadcast(r.serialize(), s.origin);
                } catch(IOException f){
                    f.printStackTrace();
                }

                Log.d("SENT COMMAND: ", "Result Sent back");
            }
            else{
                try {
                    NodeManager.ExtraMsgs.put(recv_raw);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }


    }


}
