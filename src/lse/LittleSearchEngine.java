package lse;

import java.io.*;
import java.util.*;

/**
 * This class builds an index of keywords. Each keyword maps to a set of pages in
 * which it occurs, with frequency of occurrence in each page.
 *
 */
public class LittleSearchEngine {
	
	/**
	 * This is a hash table of all keywords. The key is the actual keyword, and the associated value is
	 * an array list of all occurrences of the keyword in documents. The array list is maintained in 
	 * DESCENDING order of frequencies.
	 */
	HashMap<String,ArrayList<Occurrence>> keywordsIndex;
	
	/**
	 * The hash set of all noise words.
	 */
	HashSet<String> noiseWords;
	
	/**
	 * Creates the keyWordsIndex and noiseWords hash tables.
	 */
	public LittleSearchEngine() {
		keywordsIndex = new HashMap<String,ArrayList<Occurrence>>(1000,2.0f);
		noiseWords = new HashSet<String>(100,2.0f);
	}
	
	/**
	 * Scans a document, and loads all keywords found into a hash table of keyword occurrences
	 * in the document. Uses the getKeyWord method to separate keywords from other words.
	 * 
	 * @param docFile Name of the document file to be scanned and loaded
	 * @return Hash table of keywords in the given document, each associated with an Occurrence object
	 * @throws FileNotFoundException If the document file is not found on disk
	 */
	public HashMap<String,Occurrence> loadKeywordsFromDocument(String docFile) 
	throws FileNotFoundException {
		
		HashMap<String, Occurrence> map = new HashMap<String, Occurrence>();
		if(docFile == null){
			throw new FileNotFoundException();
		}else{
			Scanner sc = new Scanner(new File(docFile));
			while(sc.hasNext()){
				String key = getKeyword(sc.next());
				if(key != null){
					if(!map.containsKey(key)){
						Occurrence smth = new Occurrence(docFile,1);
						map.put(key, smth);
					}
					else{
						map.get(key).frequency++;
					}
				}
			}
			sc.close();
		}
		return map;
	}
	
	/**
	 * Merges the keywords for a single document into the master keywordsIndex
	 * hash table. For each keyword, its Occurrence in the current document
	 * must be inserted in the correct place (according to descending order of
	 * frequency) in the same keyword's Occurrence list in the master hash table. 
	 * This is done by calling the insertLastOccurrence method.
	 * 
	 * @param kws Keywords hash table for a document
	 */
	public void mergeKeywords(HashMap<String,Occurrence> kws) {
		for (String key : kws.keySet()) {
		    if(!keywordsIndex.containsKey(key)){
				ArrayList<Occurrence> smth = new ArrayList<Occurrence>();
		    	smth.add(kws.get(key));
		    	keywordsIndex.put(key, smth);
		    }
		    else{
		    	keywordsIndex.get(key).add(kws.get(key));
		    	insertLastOccurrence(keywordsIndex.get(key));
		    }
		}
	}
	
	/**
	 * Given a word, returns it as a keyword if it passes the keyword test,
	 * otherwise returns null. A keyword is any word that, after being stripped of any
	 * trailing punctuation, consists only of alphabetic letters, and is not
	 * a noise word. All words are treated in a case-INsensitive manner.
	 * 
	 * Punctuation characters are the following: '.', ',', '?', ':', ';' and '!'
	 * 
	 * @param word Candidate word
	 * @return Keyword (word without trailing punctuation, LOWER CASE)
	 */
	public String getKeyword(String word) {
		
		word = word.toLowerCase();
		while(word.length() != 0){
			char index = word.charAt(word.length()-1);
			if(index == '.' || index == ',' || index == '?' || index == ':' || index == ';' || index == '!'){
				word = word.substring(0, word.length()-1);
			}else{
				break;
			}
		}
		for(int i = 0; i < word.length(); i++){
			if(!Character.isLetter(word.charAt(i))){
				return null;
			}
		}
		if(noiseWords.contains(word)){
			return null;
		}
		if(word.isEmpty()){
			return null;
		}
		return word;
	}
	
	/**
	 * Inserts the last occurrence in the parameter list in the correct position in the
	 * list, based on ordering occurrences on descending frequencies. The elements
	 * 0..n-2 in the list are already in the correct order. Insertion is done by
	 * first finding the correct spot using binary search, then inserting at that spot.
	 * 
	 * @param occs List of Occurrences
	 * @return Sequence of mid point indexes in the input list checked by the binary search process,
	 *         null if the size of the input list is 1. This returned array list is only used to test
	 *         your code - it is not used elsewhere in the program.
	 */
	public ArrayList<Integer> insertLastOccurrence(ArrayList<Occurrence> occs) {
		
		ArrayList<Integer> midpoints = new ArrayList<Integer>();
		int target = occs.size()-1;
		Occurrence temp = occs.get(target);
		int high = occs.size()-2;
		int low = 0;
		int mid = 0;
		if(occs.size() <= 1 ){
			return null;
		}
		else{
			while (low <= high) {
				mid=(low+high)/2;
				midpoints.add(mid);
				if (occs.get(target).frequency < occs.get(mid).frequency) {   
					low = mid+1;  
					if (high <= mid){
						mid = mid + 1;
					}
				} 
				else if(occs.get(target).frequency > occs.get(mid).frequency) {		 
					high = mid-1;   
				}
				else if (occs.get(target).frequency == occs.get(mid).frequency) {  
					break;
					}
				}
			}
		occs.add(mid, temp);
		occs.remove(occs.size()-1);
		return midpoints;
	}
	
	/**
	 * This method indexes all keywords found in all the input documents. When this
	 * method is done, the keywordsIndex hash table will be filled with all keywords,
	 * each of which is associated with an array list of Occurrence objects, arranged
	 * in decreasing frequencies of occurrence.
	 * 
	 * @param docsFile Name of file that has a list of all the document file names, one name per line
	 * @param noiseWordsFile Name of file that has a list of noise words, one noise word per line
	 * @throws FileNotFoundException If there is a problem locating any of the input files on disk
	 */
	public void makeIndex(String docsFile, String noiseWordsFile) 
	throws FileNotFoundException {
		// load noise words to hash table
		Scanner sc = new Scanner(new File(noiseWordsFile));
		while (sc.hasNext()) {
			String word = sc.next();
			noiseWords.add(word);
		}
		
		// index all keywords
		sc = new Scanner(new File(docsFile));
		while (sc.hasNext()) {
			String docFile = sc.next();
			HashMap<String,Occurrence> kws = loadKeywordsFromDocument(docFile);
			mergeKeywords(kws);
		}
		sc.close();
	}
	
	/**
	 * Search result for "kw1 or kw2". A document is in the result set if kw1 or kw2 occurs in that
	 * document. Result set is arranged in descending order of document frequencies. (Note that a
	 * matching document will only appear once in the result.) Ties in frequency values are broken
	 * in favor of the first keyword. (That is, if kw1 is in doc1 with frequency f1, and kw2 is in doc2
	 * also with the same frequency f1, then doc1 will take precedence over doc2 in the result. 
	 * The result set is limited to 5 entries. If there are no matches at all, result is null.
	 * 
	 * @param kw1 First keyword
	 * @param kw1 Second keyword
	 * @return List of documents in which either kw1 or kw2 occurs, arranged in descending order of
	 *         frequencies. The result size is limited to 5 documents. If there are no matches, returns null.
	 */
	public ArrayList<String> top5search(String kw1, String kw2) {
		
		ArrayList<String> top5 = new ArrayList<String>();
		ArrayList<Occurrence> OCkw1 = new ArrayList<Occurrence>();
		ArrayList<Occurrence> OCkw2 = new ArrayList<Occurrence>();
		ArrayList<Occurrence> combine = new ArrayList<Occurrence>();
		if(keywordsIndex.containsKey(kw1)){
			OCkw1 = keywordsIndex.get(kw1);
		}
		if(keywordsIndex.containsKey(kw2)){
			OCkw2 = keywordsIndex.get(kw2);
		}
		if(OCkw1.isEmpty() && OCkw2.isEmpty()){
			return null;
		}
		int list1 = 0;
		int list2 = 0;
		while((list1 < OCkw1.size() || list2 < OCkw2.size())){
			if(list1 == OCkw1.size() || OCkw1.isEmpty()){
				combine.add(OCkw2.get(list2));
				list2++;
			}
			else if(list2 == OCkw2.size() || OCkw2.isEmpty()){
				combine.add(OCkw1.get(list1));
				list1++;
			}
			else if(list1 < OCkw1.size() && list2 < OCkw2.size()){
				if(OCkw1.get(list1).frequency >= OCkw2.get(list2).frequency){
					combine.add(OCkw1.get(list1));
					list1++;	
				}
				else if(OCkw1.get(list1).frequency < OCkw2.get(list2).frequency){
					combine.add(OCkw2.get(list2));
					list2++;	
				}
			}
		}
		int count = 0;
		for(int i = 0; i < combine.size(); i++){
			if(count != 5){
				String doc = combine.get(i).document;
				if(!top5.contains(doc)){
					top5.add(doc);
					count++;
				}
			}
		}
		return top5;
	}
}