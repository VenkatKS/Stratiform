package com.example.MatrixMultiplication;

import java.util.ArrayList;


//outputs matrix C, gets results as MatrixMultiplier objects, gets input as arraylist of two matricies A and B, splits input up into MatrixMultiplier objects
public class MatrixDistributor extends Distributable<int[][], MatrixMultiplier, ArrayList<int[][]>, MatrixMultiplier> {

    private int rows;

    //example use
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

    @Override
    public ArrayList<MatrixMultiplier> split(ArrayList<int[][]> input, int numNodes) {
        ArrayList<MatrixMultiplier> results = new ArrayList<>();
        int[][] A = input.get(0);
        int[][] B = input.get(1);
        this.rows = A.length;
        if(rows >= numNodes){
            int rowsPerNode = rows / numNodes; //need to check if rowsPerNode < numNodes, rowsPerNode >= numNodes
            //create and add all sub-matrices to results ArrayList
            for(int i = 0; i < numNodes - 1; i++){
                int[][] subset = new int[rowsPerNode][];
                int start = i*rowsPerNode;
                //create sub-matrix
                for(int k = 0; k < rowsPerNode; k++) {
                    subset[k] = A[start + k];
                }
                results.add(new MatrixMultiplier(subset, B, start, start + rowsPerNode - 1 ));
            }
            //do last node separately in case rows is not divisible by numNodes
            int start = (numNodes - 1) * rowsPerNode;
            int subset[][] = new int[rows-start][];
            for(int k = 0; k < rows-start; k++){
                subset[k] = A[start + k];
            }
            results.add(new MatrixMultiplier(subset, B, start, rows-1));
        } else {    //fewer rows than nodes, give as many nodes single rows as possible
            for(int i = 0; i < rows; i++){
                int subset[][] = new int[1][];
                subset[0] = A[i];
                results.add(new MatrixMultiplier(subset, B, i, i));
            }
        }

        return results;
    }

    @Override
    public int[][] combine(ArrayList<MatrixMultiplier> pieces) {
        int[][] result = new int[rows][]; //ok since you always run split before combine
        for(MatrixMultiplier submatrix : pieces){
            int counter = 0;
            for(int i = submatrix.start; i <= submatrix.end; i++){
                result[i] = submatrix.C[counter];
                counter++;
            }
        }
        return result;
    }


}
