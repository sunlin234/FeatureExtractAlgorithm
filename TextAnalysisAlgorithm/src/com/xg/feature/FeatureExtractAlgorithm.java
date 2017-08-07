package com.xg.feature;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FeatureExtractAlgorithm {
	public static final String posCorpusPath = "data/merge-split-filter-pos";
	public static final String negCorpusPath = "data/merge-split-filter-neg";
	public static Map<String, Integer> posCorpusWordMap = new HashMap<String, Integer>();
	public static Map<String, Integer> negCorpusWordMap = new HashMap<String, Integer>();
	
	public static int posLineCount;
	public static int negLineCount;
	
	public static Map<String, Double> wordMutualInfoMap = new HashMap<String, Double>();
	public static Map<String, Double> wordInfoGainMap = new HashMap<String, Double>();
	
	static {
		loadCorpusWord(posCorpusPath, 1, posCorpusWordMap);
		loadCorpusWord(negCorpusPath, 0, negCorpusWordMap);
		extendWordMap();
	}
	//统计 词（1-包含，0-不包含）-类别（1-褒义，0-贬义）对应矩阵
	public static void loadCorpusWord(String path, int value, Map<String, Integer> wordMap)
	{
		BufferedReader fileBr = null;
		try {
			fileBr = new BufferedReader(new InputStreamReader(new FileInputStream(new File(path))));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		String line = null;
		try {
			for(; (line = fileBr.readLine()) != null;){  
				String[] splitWords = line.split(" ");
				List<String> wordlist = Arrays.asList(splitWords);
				Set<String> wordSet = new HashSet<String>();
				wordSet.addAll(wordlist);
				for (String string : wordSet) {
					if(string.equals("#"))
						continue;
					if(wordMap.containsKey(string)){
						int count = wordMap.get(string);
						wordMap.put(string, count+1);
					}
					else{
						wordMap.put(string, 1);
					}
				}
				//计数
				if(1 == value){
					posLineCount++;
				}else if( 0 == value){
					negLineCount++;
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//褒贬义类互相扩展词集合，使两map set一致
	public static void extendWordMap()
	{
		Set<String> posSet = posCorpusWordMap.keySet();
		System.out.println("posMap-size:"+posCorpusWordMap.size());
		System.out.println("negMap-size:"+negCorpusWordMap.size());
		for (String string : posSet) {
			if(!negCorpusWordMap.containsKey(string)){
				negCorpusWordMap.put(string, 0);
			}
		}
		Set<String> negSet = negCorpusWordMap.keySet();
		System.out.println("new-negMap size:"+negSet.size());
		for (String string : negSet) {
			if(!posCorpusWordMap.containsKey(string)){
				posCorpusWordMap.put(string, 0);
			}
		}
		System.out.println("new-posMap size:"+posCorpusWordMap.size());
	}
	
	//计算词的互信息，并对map排序
	public static void wordMutualInfo(){
		Set<String> posSet = posCorpusWordMap.keySet();
		for (String word : posSet) {
			double value = mutualInfoformula(negLineCount - negCorpusWordMap.get(word),posLineCount-posCorpusWordMap.get(word),
					negCorpusWordMap.get(word), posCorpusWordMap.get(word));			
			wordMutualInfoMap.put(word, value);
			if(value>0.005)
			System.out.println(word+" "+value);
		}
		
	}
	//计算词的信息增益，并对map排序
	public static void wordInfoGain(){
		Set<String> posSet = posCorpusWordMap.keySet();
		for (String word : posSet) {
			double value = InfoGainFormula(negLineCount - negCorpusWordMap.get(word),posLineCount-posCorpusWordMap.get(word),
					negCorpusWordMap.get(word), posCorpusWordMap.get(word));			
			wordInfoGainMap.put(word, value);
			if(value>0.005)
			System.out.println(word+" "+value);
		}
	}
	
	//列属于类别
	public static double mutualInfoformula(int a00, int a01, int a10 ,int a11)
	{
		double N = a00 + a01 + a10 + a11;
		return   (double)(a11/N)*Math.log(a11*N/(a11+a10)/(a11+a01))/Math.log(2)
				+(double)(a10/N)*Math.log(a10*N/(a10+a11)/(a10+a00))/Math.log(2)
				+(double)(a01/N)*Math.log(a01*N/(a01+a11)/(a01+a00))/Math.log(2)
				+(double)(a00/N)*Math.log(a00*N/(a00+a01)/(a00+a10))/Math.log(2);
	}
	
	public static double InfoGainFormula(int a00, int a01, int a10 ,int a11)
	{
		double N = a00 + a01 + a10 + a11;
		double p0 = (a10+a00)/N;
		double p1 = (a11+a01)/N;
		double info1 = - p0*Math.log(p0)/Math.log(2)
					   - p1*Math.log(p1)/Math.log(2);
		double et0 = a01 + a00;
		double et1 = a11 + a10;
		double info2 = - et1/N*(a11/et1*Math.log(a11/et1)/Math.log(2) + a10/et1*Math.log(a10/et1)/Math.log(2))
					   - et0/N*(a01/et0*Math.log(a01/et0)/Math.log(2) + a00/et0*Math.log(a00/et0)/Math.log(2));
		return (info1 - info2);
	}
	
	public static void main(String[] args) {
		wordMutualInfo();
		System.out.println("-----------------------------");
		wordInfoGain();
		System.out.println(InfoGainFormula(70, 1, 0, 99));
//		System.out.println(mutualInfoformula(35, 50, 35, 50));
//		System.out.println(mutualInfoformula(70, 0, 1, 99));
	}
}
