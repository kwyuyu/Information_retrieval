package index;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import CompressUtility.Compression;
import CompressUtility.CompressionFactory;
import CompressUtility.Compressors;

public class IndexBuilder {
    private Map<Integer, String> sceneIdMap; 
    private Map<Integer, String> playIdMap;
    private Map<String, PostingList> invertedLists;
    private Map<Integer, Integer> docLengths;
	private Compressors compression;

	// document vector: map[docId][term] = count
    private Map<Integer, Map<String, Integer>> documentTermFrequency;


	public IndexBuilder() {
	    sceneIdMap = new HashMap<Integer, String>();
	    playIdMap = new HashMap<Integer, String>();
	    invertedLists = new HashMap<String, PostingList>();
	    docLengths = new HashMap<Integer, Integer>();

        documentTermFrequency = new HashMap<Integer, Map<String , Integer>>();

	}
    private void parseFile(String filename) {
        JSONParser parser = new JSONParser();
        try {
        	// fetch the scenes
            JSONObject jsonObject = (JSONObject) parser.parse(new FileReader(filename));
            JSONArray scenes = (JSONArray) jsonObject.get("corpus");
            // iterate over the scenes
            for (int idx = 0; idx < scenes.size(); idx++) {
                JSONObject scene = (JSONObject) scenes.get(idx);
                // start document ids at 1, not 0
                int docId = idx + 1;
                // record the external scene and play identifiers
                String sceneId = (String) scene.get("sceneId");
                sceneIdMap.put(docId, sceneId);
                String playId = (String) scene.get("playId");
                playIdMap.put(docId, playId);
                
                String text = (String) scene.get("text");
                String[] words = text.split("\\s+");
                //record the document length
                docLengths.put(docId, words.length);

                // term frequency for cur document
                Map<String, Integer> termFreq = new HashMap<>();

                // iterate over the terms in the scene
                for (int pos = 0; pos < words.length; pos++) {
                	String word = words[pos];
                	invertedLists.putIfAbsent(word, new PostingList());
                	invertedLists.get(word).add(docId, pos+1);

                    termFreq.put(word, termFreq.getOrDefault(word, 0) + 1);
                 }

                // add current term frequency to document vector
                documentTermFrequency.put(docId, termFreq);
            }
        } catch (ParseException e) {
        	// actually do something when bad things happen...
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveStringMap(String fileName, Map<Integer, String> map) {
        List<String> lines = new ArrayList<>();
        map.forEach((k,v) -> lines.add(k + " " + v));
        try {
            Path file = Paths.get(fileName);
            Files.write(file, lines, Charset.forName("UTF-8"));
        }  catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveDocumentTermFrequency(String fileName, Map<Integer, Map<String, Integer>> map) {
	    List<String> lines = new ArrayList<>();
	    for (int docId: map.keySet()) {
	        Map<String, Integer> termFreq = map.get(docId);
	        for (String term: termFreq.keySet()) {
                lines.add(docId + " " + term + " " + termFreq.get(term));
            }
        }
	    try {
	        Path file = Paths.get(fileName);
	        Files.write(file, lines, Charset.forName("UTF-8"));
        } catch (IOException e) {
	        e.printStackTrace();
        }
    }

    private void saveDocumentTermFrequencyJson(String fileName, Map<Integer, Map<String, Integer>> map) {
	    JSONObject docTermFreqJson = new JSONObject();
	    for (int docId: map.keySet()) {
	        Map<String, Integer> termFreq = map.get(docId);
	        JSONObject termFreqJson = new JSONObject();
	        for (String term: termFreq.keySet()) {
	            termFreqJson.put(term, termFreq.get(term));
            }
	        docTermFreqJson.put(docId, termFreqJson);
        }
	    try {
	        FileWriter fileWriter = new FileWriter(fileName);
	        fileWriter.write(docTermFreqJson.toJSONString());
	        fileWriter.flush();
        }
	    catch (IOException e) {
	        e.printStackTrace();
        }
    }

    private void saveDocLengths(String fileName) {
        List<String> lines = new ArrayList<>();
        docLengths.forEach((k,v) -> lines.add(k + " " + v));
        try {
            Path file = Paths.get(fileName);
            Files.write(file, lines, Charset.forName("UTF-8"));
        }  catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveInvertedLists(String lookupName, String invListName) {
        long offset = 0;
        try {
            PrintWriter lookupWriter = new PrintWriter(lookupName, "UTF-8");
            RandomAccessFile invListWriter = new RandomAccessFile(invListName, "rw");
            Compression comp = CompressionFactory.getCompressor(compression);

            for (Map.Entry<String, PostingList> entry : invertedLists.entrySet()) {
                String term = entry.getKey();
                PostingList postings = entry.getValue();
                int docTermFreq = postings.documentCount();
                int collectionTermFreq = postings.termFrequency();
                Integer [] posts = postings.toIntegerArray();
                ByteBuffer byteBuffer = ByteBuffer.allocate(posts.length * 8);
                comp.encode(posts, byteBuffer);
                // only write the bytes we used (as may be fewer than capacity)
                byte [] array = byteBuffer.array();
                invListWriter.write(array, 0, byteBuffer.position());
                long bytesWritten = invListWriter.getFilePointer() - offset;
                lookupWriter.println(term + " " + offset + " " + bytesWritten + " " + docTermFreq + " " + collectionTermFreq);
                offset = invListWriter.getFilePointer();
            }
            invListWriter.close();
            lookupWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public void buildIndex(String sourcefile, boolean compress) {
	    File diskDir = new File("disk");
	    if (!diskDir.exists()) {
	        diskDir.mkdir();
        }

    	this.compression = compress ? Compressors.VBYTE : Compressors.EMPTY;
    	String lookupFile = compress ? "disk/lookupCompressed.txt" : "disk/lookup.txt";
    	String invFile = compress ? "disk/invListCompressed" : "disk/invList";
    	String lookupDocVecFile = compress ? "disk/lookupDocVecCompressed.txt" : "disk/lookupDocVec.txt";
    	String docVecFile = compress ? "disk/docVecCompressed" : "disk/docVec";
        parseFile(sourcefile);
        // refactor the hardcoded names...
        saveStringMap("disk/sceneId.txt", sceneIdMap);
        saveStringMap("disk/playIds.txt", playIdMap);
        saveDocLengths("disk/docLength.txt");
        saveInvertedLists(lookupFile, invFile);
        saveDocumentTermFrequency("disk/docTermFreq.txt", documentTermFrequency);
        saveDocumentTermFrequencyJson("disk/docTermFreq.json", documentTermFrequency);
    }
 }
