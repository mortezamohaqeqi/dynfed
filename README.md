# dynfed
The *DynFed* scheduling algorithm proposed in the RTCSA 2021 paper.

### What is this repository for? ###

* This is the soruce code for timing analysis of real-time DAG tasks. The program generates a number of random task sets, and simulates the system execution, in terms of timing properties, for one hyper-period. A number of key timing parameters are collected. 

### How do I get set up? ###

The project is compiled using Maven. To compile, one needs to:

* Install [Java](https://openjdk.java.net/install/)
* Install [Maven](https://maven.apache.org/install.html)
* Open a command prompt, change directory to the main directory of the project (i.e., the v1.1 directory)
* Enter the following command:
* mvn -U clean javafx:run

(tested only on Windows)

### Other details ###
 * **main()** function is in application.test.Tester
