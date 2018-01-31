Test Impact Analysis tool for Acceptance Tests
==============================================

Test Impact Analysis (TIA) is a modern way of speeding up the test automation phase of a build. It works by analyzing the call-graph of the source code to work out which tests should be run after a change to  code. This greatly benefit the testing time to reduce to only "relevant" tests where the code change impact is calculated.

However, this concept is mainly applied on the Unit testing level. Microsoft has done some extensive research on this approach but that is available only in Visual Studio environment. 

On the unit level, it is easy to compute the changes because all links between the code change and tests are available through any modern IDE. This is difficult to realise the change impact in the Continuous Integration environment where various components, which are developed independently, are "glued" to work together to execute an end-user scenario. On this level, it is not obvious to link the test scenarios with the code changes in the individual components.   

This project is an initiative to extend TIA over the integration level. It has two parts:

* **JvmSniffer**: A tool to capture method calls of the target program unit when tests are executing. This is checked in [here](http://man-cisrv-1:8000/tia/JvmSniffer). See details on Readme in the repo.
* **IntegrationTestImpactAnalysis**: This tool explained below.

### Assumptions

This project is created with the following assumptions:

* The target program unit and integration test repositories are available in GIT.
* Integration tests are available as Cucumber scenarios.
* Each Cucumber scenario is tagged, such that a combination of tags represent one scenario.
* The test repo contains a file called "tag" that lists tags representing scenarios in one line. See example [here](http://man-cisrv-1:8000/tia/DBINB-AFTNAdapterAcceptanceTests)
* JvmSniffer tool is configured and hosted on the target host (the server where the target program is running).

### Limitations

* The target program has to be run with debugging options on. This limitation is coming from Java that cannot hook into the target JVM unless it is running in the debugging mode. This also means that port 9865 is open. See JvmSniffer's Readme for details.
* At the moment, only changes in the methods are detected. It is possible to extend it to detect changes in the fields (e.g., static final). The *DiffJ* tool (included in the :JavaDiff module in this project) is capable to note those changes.
* The cucumber report is produced only for those scenarios that has been executed.  

### How does it work?

This tool performs the following tasks:

#### Analyse Target Repo

1. Checks out the target program's repo.
2. Obtains the list of Java files changed in the last commit. 
3. Checks out the previous version of each Java file changed.
4. Extracts information on which methods are changed in the last commit.

#### Analyse Test Repo

1. Checks out the target program's test repo.
2. Reads the list of tags from file called **tag** in the root. Each line is a space delimited list of tags representing one scenario. 
3. Obtains the list of method trace files from the target host for each scenario. If the trace file is not available, the scenario will be marked for execution. 
5. Marks the scenarios whose method trace contains any method changes in the last commit.

#### Run Tests

For each marked scenario:
1. Starts JVM Sniffer on the target host.
1. Runs Cucumber tests for the scenario.
1. Stops JVM Sniffer. 

Note that stopping JVM Sniffer produces the method trace file for all methods called during the test execution. This file will be used in the next commit to remarking scenarios. 

Modules
=======

* **GitDiff**: responsible for managing GIT repositories (cloning, getting change log, getting previous versions, ...)
* **JavaDiff**: responsible for computing differences in Java files and extracting list of method changes. This code is borrowed from [DiffJ](https://mvnrepository.com/artifact/org.incava/diffj) and changed as required.
* **TiaPlayer**: Main module. ***com.ueas.tia.player.TiaPlayer*** is the main class to run.

