## Dependencies
* Install **maven**: `brew install maven` or download from [website](https://maven.apache.org/download.cgi).

## Build the code
* Build the maven project and compile.
		
		mvn package
		mvn compile

## Run the code
#### Build index
* Uncompressed index:

		mvn exec:java@BuildIndex -Dexec.args="shakespeare-scenes.json false"

* Compressed index:

		mvn exec:java@BuildIndex -Dexec.args="shakespeare-scenes.json true"
		

#### Online cluster
* Arguments:
	* **threshold**: Integer, threashold.
	* **linkage**: String, linkage type, including single, complete, average, averagegroup
	* **comrepssed**: Boolean, retrieve query from compressed or uncompressed file.

* Online cluster:

		mvn exec:java@OnlineCluster -Dexec.args="{threshold} {linkage} {compressed}"
		
* Run experiment (mean linkage from threshold 0.05 to 0.95):

		mvn exec:java@OnlineCluster