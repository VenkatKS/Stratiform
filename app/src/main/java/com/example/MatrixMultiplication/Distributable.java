package com.example.MatrixMultiplication;

import java.util.ArrayList;


public abstract class Distributable <O, C extends Executable<C>, I, S extends Executable<C>>{   //output type, pre-combined output type, input type, subdivided input type
                                                                                                //running execute on type S, turns it into type C
                                                                                                //type C needs to be Executable so it can be deserialized and serialized
    //in many cases C will be the same as S, but not always

    public abstract ArrayList<S> split(I input, int numNodes);
    //call split on an object, and then send messages with elements of ArrayList returned (and destination node) to all nodes
    //number of elements in ArrayList may be less than total number of nodes, depending on input size, so wait for the output.size() messages back (including from self)

    public abstract O combine(ArrayList<C> pieces);
    //combine pre-combined outputs in messages into an ArrayList, and call combine on it
}
