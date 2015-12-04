package fr.thumbnailsdb.bktree;

import fr.thumbnailsdb.distance.LevenshteinDistance;

import java.io.IOException;
import java.util.HashMap;



public class TestBKTree {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {


		String[] wordList = new String[] {
				"remote",
				"barn",
				"book",
				"glass",
				"chair",
				"closet",
				"skull",
				"giraffe",
				"boat",
				"soda"
		};
		
		BKTree<String> bkTree = new BKTree<String>(new LevenshteinDistance());
		
		for (String word : wordList) {
			bkTree.add(word);
		}
		
		HashMap<String, Double> queryMap = bkTree.query("bark", 2);
		System.out.println(queryMap);
		
		String searchTerm = "temotw";
		System.out.println("Best match for search '"+searchTerm+"' = "+bkTree.findBestWordMatchWithDistance(searchTerm));
		
		searchTerm = "garage";
		System.out.println("Best match for search '"+searchTerm+"' = "+bkTree.findBestWordMatchWithDistance(searchTerm));
		
	}		
}