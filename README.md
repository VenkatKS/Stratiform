# Stratiform

## Abstract
Stratiform is a framework for the dissemination of computational burden among peers in a temporarily connected mobile ad-hoc network. The idea behind Stratiform is to take a socialized approach to computational burden in a mobile community; when there is an abundance of idle devices and a computationally over-burdened peer within the ad-hoc network, Stratiform would step in and share the burden among all of the connected peers, allowing for quicker turn-around time for the requested computation. On the active device, Stratiform takes a parallelizable task and splits it into smaller tasks that are easier to compute; once the split is done, Stratiform disseminates the tasks to connected idle devices. The idle devices finish their smaller computations and transmit the individual results back to the requesting instance of Stratiform, which combines the parts into the full solution. This behavior takes advantage of every device in the network, allowing for the maximization of computation throughput of the entire network. Stratiform shines when it comes to problems that are more quickly solved in a massively-parallel architecture, such as a graphics processing unit (GPU). Rather than limiting the computation of such problems to just one device and one GPU, Stratiform seeks to treat the entire network like a large GPU consisting of individual GPUs, allowing for such tasks to finish more efficiently.

## Architecture
Stratiform utilizes WiFi Direct in order to create a local ad-hoc network between Android devices. Stratiform monitors the status of all devices in this network, and registers devices when they connect into the network. Stratiform provides a dual-layered approach, where the user-task runs on-top of this aforementioned Stratiform layer. Each user task provides Stratiform with a Map-Reduce architecture that Stratiform utilizes in order to break down and recombine user work, similar to the paradigm for massive parallelization in a Hadoop cluster.

<p align="center">
<img src="https://github.com/VenkatKS/Stratiform/blob/master/documentation/architecture_docs/stratiform_architecture.png?raw=true" alt="Stratiform Architecture" width="50%" height="50%"/>
</p>

## Demo
I opted to evaluate the performance of Stratiform using a matrix multiplication example. A NxM and a MxA matrix, where N, M and A are specified by the user, is multiplied using Stratiform. Our examples all make N = M = A. The time taken for a solution to be found for the matrix multiplication for various sized matrices and for various number of nodes is featured in Table 1. Stratiform shines when the amount of data is large and the network contains a large number of peer nodes. The overhead for transmission only allows Stratiform to be faster than locally processing the data if the matrix is over 100x100. The specified performance rates was taken in a low-traffic area. Latency rates, and thus Stratiform results, might vary depending on where Stratiform is used.

The demonstration matrix multiply code can be found under /app/src/main/java/com/example/MatrixMultiplication. The remaining code is for the framework.
