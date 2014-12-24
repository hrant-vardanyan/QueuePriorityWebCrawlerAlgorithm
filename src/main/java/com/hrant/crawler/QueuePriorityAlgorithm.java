package com.hrant.crawler;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.hrant.dao.OlxDAO;
import com.hrant.model.UrlEntry;
import com.hrant.utils.Constants;

/*
 * QueuePriorityAlgorithm Crawler
 * Author: Hrant Vardanyan
 */
public class QueuePriorityAlgorithm {

	private static final Logger LOGGER = Logger.getLogger(QueuePriorityAlgorithm.class);

	private Set<String> used;
	private PriorityBlockingQueue<UrlEntry> priorityQueue;

	/*
	 * Simple testing
	 */
	public static void main(String[] args) throws IOException {

		QueuePriorityAlgorithm queuePriorityAlgorithm = new QueuePriorityAlgorithm(10);

		LinkedList<String> queue = new LinkedList<>();
		queue.add("http://www.cypherincorporated.co.in/");
		queuePriorityAlgorithm.queuePriorityAlgorithmLogic(queue);

	}

	// Constructor
	public QueuePriorityAlgorithm(int threadCount) {

	}

	/*
	 * QueuePriorityAlgorithm main logic
	 */
	public void queuePriorityAlgorithmLogic(LinkedList<String> firstSeed) {
		used = new HashSet<>();
		used.addAll(firstSeed);

		LinkedList<UrlEntry> seedUrls = new LinkedList<>();

		for (String seedUrl : firstSeed) {
			UrlEntry seedEntry = new UrlEntry();
			seedEntry.setInter(false);
			seedEntry.setScore(1);
			seedEntry.setUrl(seedUrl);
			seedUrls.add(seedEntry);
		}

		priorityQueue = new PriorityBlockingQueue<>(1, new SortQueueViaScore());

		// This loop runs, until no new seed was generated
		while (!seedUrls.isEmpty()) {
			getUrls(seedUrls);
			LOGGER.info("I assume you've finished the tasks");

			// Extract urls from priority queue
			while (!priorityQueue.isEmpty()) {
				// Get parent url and his children from priority queue
				UrlEntry parentUrlEntry = priorityQueue.poll();
				LinkedList<UrlEntry> childUrlEntries = parentUrlEntry.getChildren();

				// Computing final score for parent according to 12.a(b)
				double intraSum = 0;
				double interSum = 0;
				int intraCount = 0;
				int interCount = 0;

				// Get numbrer of inter/intra children, and sum of their scores
				for (UrlEntry childUrlEntry : childUrlEntries) {
					if (childUrlEntry.isInter()) {
						interCount++;
						interSum = interSum + childUrlEntry.getScore();
					} else {
						intraCount++;
						intraSum = intraSum + childUrlEntry.getScore();
					}

					seedUrls.add(childUrlEntry);
				}

				// Compute parent score according to 12.a(b)
				parentUrlEntry.setScore(interCount * interSum + intraCount * intraSum);
				parentUrlEntry.setChildren(null);

				// Save parent in database
				try {
					OlxDAO.getInstance().addIfNotExist(parentUrlEntry);
				} catch (Exception e) {
					LOGGER.error("error with storing db", e);
				}

			}
		}

	}

	/*
	 * Gets all urls from page
	 */
	private Set<String> getAllLinksInPage(String pageUrl) throws IOException {
		// Get page data
		Document docOfPage = Jsoup.connect(pageUrl).ignoreContentType(true).userAgent(Constants.BROWSER)
				.timeout(Constants.TIMEOUT).get();

		// Set to hold all gathered urls
		// And check for them being unique (HashSet class holds only unique
		// objects)
		Set<String> linkSet = new HashSet<>();

		// Add '/' if url does not end with '/'
		if (!pageUrl.endsWith("/")) {
			pageUrl = pageUrl + "/";
		}

		String domain = getDomain(pageUrl);
		// Extract protocol
		String protocol = StringUtils.substringBefore(pageUrl, "//");

		// Get all 'a' tags
		Elements aEl = docOfPage.select("a");
		String link = "";

		// For each 'a' tag
		for (Element a : aEl) {
			// Extract its url
			String href = a.attr("href");

			// Check if url is empty
			if (!StringUtils.isEmpty(href)) {
				// Check if url is extended or not
				if (href.startsWith("http")) {
					// Protocol exists, valid url
					link = href;
				} else {
					if (href.startsWith("//")) {
						// No protocol, add protocol to url
						link = protocol + href;
					} else if (href.startsWith("/")) {
						// No domain, add domain
						link = domain + href;
					}
				}
			}

			if (!StringUtils.isEmpty(link)) {
				// Url is not empty, filter socials and add to set
				if (!link.contains("facebook") && !link.contains("twitter") && !link.contains("linkedin")) {
					linkSet.add(link);
				}
			}
		}

		return linkSet;
	}

	/*
	 * Read initial urls from text file
	 */
	public static List<String> readLinksInTXT(Path inputPath) {
		File file = new File(inputPath.toString());
		List<String> linkList = new ArrayList<>();

		try {
			FileReader reader = new FileReader(file);
			char[] chars = new char[(int) file.length()];
			reader.read(chars);
			String content = new String(chars);
			String linksArray[] = content.split("\\r?\\n");
			List<String> linkListWithEmptyLines = Arrays.asList(linksArray);

			for (String link : linkListWithEmptyLines) {
				if (!StringUtils.isEmpty(link)) {
					link = java.net.URLDecoder.decode(link, "UTF-8");
					linkList.add(link);
				}
			}

			reader.close();
		} catch (IOException e) {
			LOGGER.error("Exception getting data from  " + inputPath, e);
		}

		return linkList;
	}

	/*
	 * Multitread method for crawling
	 */
	private synchronized void getUrls(final LinkedList<UrlEntry> seedUrls) {

		ExecutorService fixedThreadPool = Executors.newFixedThreadPool(10);

		while (!seedUrls.isEmpty()) {
			LOGGER.info("size of seed urls: " + seedUrls.size());
			final UrlEntry currSeedUrlEntry = seedUrls.poll();
			final String currSeedUrl = currSeedUrlEntry.getUrl();
			// Extract domain
			final String domain = getDomain(currSeedUrl);

			fixedThreadPool.submit(new Runnable() {
				public void run() {
					try {

						Set<String> childUrls = getAllLinksInPage(currSeedUrl);
						LinkedList<UrlEntry> fetchedList = new LinkedList<>();
						// Will be used for computing score for parent url
						// (currUrl)
						// according to 10.a(11.a)
						double sumChildScore = 0;

						// For every child
						for (String currentChildUrl : childUrls) {
							// Check if child url was already scrapped
							if (!used.add(currentChildUrl)) {
								continue;
							}

							// Create child url
							UrlEntry childUrlEntry = new UrlEntry();

							// Separate fetched URLs into two types
							// Separate Seed_URL score into two parts
							// For child url, it is checked is he inter or
							// intra,
							// And score is assigned accordingly
							double score = 0.33333333;
							boolean isInter = false;
							if (!currentChildUrl.contains(domain)) {
								score = 0.66666666;
								isInter = true;
							}

							// Finish UrlEntry object creation for child
							childUrlEntry.setInter(isInter);
							childUrlEntry.setScore(score);
							childUrlEntry.setUrl(currentChildUrl);
							childUrlEntry.setParent(currSeedUrl);

							// Add to fetched list
							fetchedList.add(childUrlEntry);

							// Add child score to all children score
							sumChildScore += score;
						}

						// Compute parent score according to 10.a (11.a)
						// This score will be used by PriorityQueue to sort
						// parents
						double tempParentScore = sumChildScore / currSeedUrlEntry.getScore();

						// Set score and children to parent url
						currSeedUrlEntry.setScore(tempParentScore);
						currSeedUrlEntry.setChildren(fetchedList);

						// Store url in priorit queue, sorted by its current
						// score
						priorityQueue.add(currSeedUrlEntry);
					} catch (IOException e) {
						LOGGER.error("error ", e);
					}
				}
			});

		}
		fixedThreadPool.shutdown();

		try {
			fixedThreadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	private String getDomain(String url) {

		String domain = "";
		// Check if url contains domain name
		Matcher domMatcher = Constants.REGEX_DOMAIN.matcher(url);

		if (domMatcher.find()) {
			// Save domain name
			domain = domMatcher.group(1);
		} else if (domain.endsWith("/")) {
			// Save domain for extended urls
			domain = StringUtils.substringBeforeLast(domain, "/");
		}

		return domain;
	}

}
