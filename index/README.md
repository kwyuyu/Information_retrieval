.## Dependencies
* Java:
	* Installing ***maven***: `brew install maven` or download from [website](https://maven.apache.org/download.cgi).

* Python: To plotting the time consuming for compression hypothesis.
	* `pip3 install -r requirements.txt`


## Building the code
* Build the maven project and compile.
			
		mvn package
		mvn compile


## Running the code
#### Command
```
mvn exec:java -Dexec.mainClass=Assignment1 -Dexec.args={arguments}
```
Arguments can combine multiple tasks, but before doing data sanity check or query, it is necessary to build the index. All the instructions below are the commands that need to be filled in `{arguments}`.

#### Arguments
* **-build \<no_arg\>**: create inverted list and write the inverted list into disk.
* **-com \<boolean\>**: compression option, true or false.
* **-file \<String\>**: file name.
* **-numV \<int\>**: number of vocabulary.
* **-numQ \<int\>**: number of query.
* **-k \<int\>**: top k result.
* **-sanity \<one or two\>**: 
	* **\<one\>**: check if the data we write into disk is correct (ex.`-sanity one -com true -file shakespeare-scenes.json`).
	* **\<two\>**: sanity check between compress and uncompress (ex. `-sanity two`).
* **-query \<int\>**: query number.
	* **Query 1**: Randomly select **-numV** terms and do this **-numQ** times from compress option **-com** binary file. In the end, the program will uutput a file *query/randomQueryFreq.txt* to record each term's term freqeuncy and document frequency. (ex. `-query 1 -numV 7 -numQ 100 -com true`)

	* **Query 2**: Randomly select **-numV** terms and do this **-numQ** times from compression option **-com** binary file. Each term will be compared with all terms in inverted list and find the term with the highest dice coefficient score. In the end, the program will produce two files *query/randomQuery1Compress.txt* and *query/randomeQuery2Compress.txt* (here we assume the compression option is true, oppositely, the file name will end with *Uncompress*). The first file contains **-numQ** lines, each line contains **-numV** random selected terms. The second file contains **-numQ** lines, each line contains **-numV** pairs of terms which has highest score. (ex. `-query  2 -numV 7 -numQ 100 -com true`)
	
	* **Query 3**: query the file **-file** with top **-k** results from compress option **-com** binary file. (ex. `-query 3 -k 5 -file query/randomQuery2Uncompress.txt`)
	
	* **Query 4**: query random selected **-numV** terms **-numQ** times with top **-k** result from compress option **-com** binary file. (ex. `-query 4 -k 5 -numV 7 -numQ 100 -com true`)
	
	* **Query 5**: experiment the compression hypothesis from existing files. Two files will be generated, which are *query/compHyp7Terms.txt* and *query/compHyp14Terms.txt*. (ex. `-query 5`)
	
	* **Query 6**: experiment the compression hypothesis by generating random terms, and generate two files *query/queryTimeCompress.txt* and *query/queryTimeUncompress.txt*. Each line of these two file contains number of 7 temrs queries and the time consumed. Also, the program will call the python scripts to generate an image *img/compressionHypothesis.png*, which is the time consumed comparison of compression hypothesis. (ex. `-query 6`)




#### Evaluations and experiments
* Before running the evaluations and experiemnts, it is necessary to build the inverted list and write it into disk for both compress and uncompress index: `-build -com true -file <path to shakespeare-scenes.json>` and `-build -com false -file <path to shakespeare-scenes.json>`

* Evaluations and experiments:
	* Compare the vocabulary of the two indexes (terms and counts) to ensure they are identical.: `-sanity two`
	
	* Randomly select 7 terms from the vocabulary. Record the selected terms, their term frequency and document frequency. Do this 100 times: `-query 1 -numV 7 -numQ 100 -com false`
	
	* Using Dice's coefficient (see section 6.2.1 and page 201), identify the highest scoring two word phrase for each of the 7 terms in your set of 100: `-query 2 -numV 7 -numQ 100 -com false`

	* Using your Retrieval API and the 100 sets of 7 terms as your queries, perform a timing experiment to examine the compression hypothesis. Repeat the experiment using the 100 sets of 14 terms (I decide to use top 5 result in this experiment): `-query 5`

