package com.example.NodeManager;

import com.example.MatrixMultiplication.MatrixDistributor;
import android.graphics.Matrix;
import android.os.SystemClock;
import android.util.Log;

import com.example.saket.distributedmm.MyActivity;
import com.example.MatrixMultiplication.*;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;


/**
 * Created by JMS on 4/23/2016.
 */


/*
 * Manages the current phone operations
 * Master or slave determined here and operations performed based on that
 */


public class NodeManager{
    public ArrayList<String> myMACList;
    String myMAC;
    MatrixDistributor distributor;
    ExecutorService executor;
    long myElapsedTime;
    public static ArrayBlockingQueue<byte[]> ExtraMsgs;


    public NodeManager(){
        myElapsedTime = 0;
        myMACList = MyActivity.mainLayer.getAllPeers();
        Log.d("size: ", Integer.toString(MyActivity.mainLayer.getNumberOfPeers()));
        myMAC = MyActivity.mainLayer.getDeviceID();
        distributor = new MatrixDistributor();
        executor = Executors.newFixedThreadPool(6);
        ExtraMsgs = new ArrayBlockingQueue<byte[]>(1000);
    }

    //launch background thread that listens for any need for computation
    public void waitComputation(){
        Log.d("Start waiting thread ", "STARTED");
        TaskComputationCompute tcc = new TaskComputationCompute(myMAC);
        executor.submit(tcc);
    }

    // Wrong time unless startComputation is called before this
    public long getMiliElapsedTime(){
        return myElapsedTime;
    }

    //do start if matrices are received
    //wait for task if no matrices
    public int[][] startComputation(int[][] matrix1, int[][] matrix2) {
        long startTime = SystemClock.elapsedRealtime();

        Log.d("MACLIST Size: ", Integer.toString(myMACList.size()));

        myMACList = MyActivity.mainLayer.getAllPeers();
        int numNodes = myMACList.size();
        for(int i = 0; i < numNodes; i++){  //added this
            Log.d("MAC address", Integer.toString(i) + ": " + myMACList.get(i));
        }
        Log.d("MACLIST Size: ", Integer.toString(numNodes));    //moved this from beginning to here
        ArrayList<int[][]> matrices = new ArrayList<>();
        matrices.add(matrix1);
        matrices.add(matrix2);
        ArrayList<MatrixMultiplier> splitInput = distributor.split(matrices, numNodes);

        ArrayList<MatrixMultiplier> results = new ArrayList<MatrixMultiplier>();
        ArrayList<Future<MatrixMultiplier>> requests = new ArrayList<Future<MatrixMultiplier>>();
        int n = 0;

        for (Executable e : splitInput) {
            //would send messages to nodes and they would execute and send back MatrixMultiplier, which you would then add to an ArrayList
            //Executable implements Serializable, so you need to serialize MatrixMultipliers into byte arrays and send them along with destination nodes in datagram packets (node id before object)

            Sendable s = new Sendable(e, "COMPUTE", myMAC, myMACList.get(n));

            if(myMAC.equals(myMACList.get(n))){
                results.add((MatrixMultiplier)e.execute());
            }
            else {
                //create new threads per call waiting on listening;
                Log.d("TASK SENT TO :" , myMACList.get(n));
                TaskComputationTrigger ctest = new TaskComputationTrigger(s, myMAC, myMACList.get(n));
                requests.add(executor.submit(ctest));
            }
            Log.d("Created new channel:  ", myMACList.get(n));
            n++;
        }
        //wait for all packets to be received
        for(Future<MatrixMultiplier> ret : requests){
            try {
                Log.d("Wait for ", " ALL");
                MatrixMultiplier result = ret.get();
                if(!results.contains(result)){
                    results.add(result);
                }   //added check if in results already due to multiple broadcasts
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        //Executable i = Executable.deserialize();
        //results.add(i.execute());   //simulate sending of data
        int[][] answer = (int[][]) distributor.combine(results); //run this once you have received splitInput.size() responses back (including self) and have added all responses to ArrayList
        long endTime = SystemClock.elapsedRealtime();
        myElapsedTime = endTime - startTime;
        //then display C to app
        return answer;
    }


}

//public class NodeManager {
//    ArrayList<String> MACList;
//    ArrayList<Task> TaskList;
//    Distributable distributor;
//    String my_MAC;
//
//
//    boolean master;
//
//    public NodeManager(){
//        master = false;
//        my_MAC = getMAC();
//    }
//
//    public void initNodeManager(){
//        if(requestedCalculation()) {
//            master = true;
//            executeMasterNodeManager();
//        }
//        executeSlaveNodeManager;
//    }
//
//    public void startMasterNodeManger(){
//        int numNodes = MACList.size();
//        ArrayList<int[][]> matrices = new ArrayList<>();
//        matrices = getRequestedMatrices();
//        ArrayList<Executable> splitInput = distributor.split(matrices, numNodes);
//        ArrayList results = new ArrayList<>();
//        int n = 0;
//        for(Executable e : splitInput){
//            //would send messages to nodes and they would execute and send back MatrixMultiplier, which you would then add to an ArrayList
//            //Executable implements Serializable, so you need to serialize MatrixMultipliers into byte arrays and send them along with destination nodes in datagram packets (node id before object)
//            byte[] data = e.serialize();
//            String head_str = "Task: " + MACList.get(n);
//            btye[] header = head_str.getBytes();
//            byte[] packet = new byte[header.length + data.length];
//            System.arraycopy(header, 0, packet, 0, header.length);
//            System.arraycopy(data, 0, packet, header.length, data.length);
//            MainLayer.broadcast(packet);
//            Executable i = Executable.deserialize(data);
//            results.add(i.execute());   //simulate sending of data
//        }
//        int[][] C = (int[][]) distributor.combine(results); //run this once you have received splitInput.size() responses back (including self) and have added all responses to ArrayList
//        //then display C to app
//    }
//
//    public void executeSlaveNodeManager(){
//        boolean taskRecv = false;
//
//
//
//
//        //wait for input
//        while(!taskRecv){
//
//        }
//        MatrixMultiplier matrices = new MatrixMultiplier(m1, m2, aStart, aEnd){
//            result = matrices.compute();
//        }
//    }

    /*public static void main(String[] args){
        Distributable distributor = new MatrixDistributor();
        int numNodes = 6;   //would be received from network manager
        int[][] A = new int[][] {{1,2,3}, {4,5,6}};     //would be received from app
        int[][] B = new int[][] {{7,8,9}, {10, 11, 12}, {13, 14, 15}};    //would be received from app
        ArrayList<int[][]> matrices = new ArrayList<>();
        matrices.add(A);
        matrices.add(B);
        ArrayList<Executable> splitInput = distributor.split(matrices, numNodes);
        ArrayList results = new ArrayList<>();
        for(Executable e : splitInput){
            //would send messages to nodes and they would execute and send back MatrixMultiplier, which you would then add to an ArrayList
            //Executable implements Serializable, so you need to serialize MatrixMultipliers into byte arrays and send them along with destination nodes in datagram packets (node id before object)
            byte[] data = e.serialize();
            Executable i = Executable.deserialize(data);
            results.add(i.execute());   //simulate sending of data


        }
        int[][] C = (int[][]) distributor.combine(results); //run this once you have received splitInput.size() responses back (including self) and have added all responses to ArrayList
        //then display C to app


        //example use of message detection
        int myID = 0;
        ArrayList<Executable> tempresults = new ArrayList<>();
        while(true) {
            DatagramPacket data = getDatagramFromNetworkManager();  //blocking
            Executable e = Executable.deserialize(data.getData());

            if (e.executeMessage(myID)) {   //I'm the destination and it hasn't been executed yet
                Executable c = (Executable) e.execute();
                c.setNodes(c.originNode, c.originNode); //send back to the origin
                //then send c.serialize() to network manager
            } else if (e.combineMessage(myID)) {//I'm the destination, the origin of the computation, and it has been executed
                tempresults.add(e);
                //once results is full, call distributor.combine(results) and send to app
            } else {
                //toss message
            }
        }

        //example use of message sending
        int originNode = 0; //or which ever node starts the computation
        ArrayList<Executable> splitup = distributor.split(new ArrayList<int[][]>(), 6); //arraylist of executables from splitting arraylist of two matrices into 6 pieces (for the 6 nodes)
        int i = 0;
        for(Executable e : splitup){
            e.setNodes(i, originNode);  //i can be any destination node
            //then send e.serialize() to the network manager to be packaged up and sent
        }
    }*/

