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
		

#### Inference network
* Arguments:
	* **k**: Integer, return top k results.
	* **comrepssed**: Boolean, retrieve query from compressed or uncompressed file.

* Inference network (output trecrun files according to the assignment required belief functions and query operators, including SUM, AND, OR, MAX, UNORDERED WINDOW, ORDERED WINDOW)

		mvn exec:java@InferenceNetworkQuery -Dexec.args="{k} {compressed}"
