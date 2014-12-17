package com.hrant.crawler;

import java.util.Comparator;

import com.hrant.model.UrlEntry;

/*
 * SortQueueViaScore
 * Author: Hrant Vardanyan
 */

	// Interface for sorting the UrlEntry objects by score
public class SortQueueViaScore implements Comparator<UrlEntry> {

	@Override
	public int compare(UrlEntry uE1, UrlEntry uE2) {
		return Double.compare(uE2.getScore(), uE1.getScore());
	}

}
