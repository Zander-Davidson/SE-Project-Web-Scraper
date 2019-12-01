package tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Scanner;

import org.json.simple.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class AbstractWebScraper {

	private String fBrowserVersion; // as of 10-31-19 chrome version is: chrome/78.0.3904.70
	private String fUrl; // url to scrape from
	private Document fDocument; // Jsoup Document object from html

	public AbstractWebScraper(String browserVersion, String url) {
		fBrowserVersion = browserVersion;
		fUrl = url;

		BufferedReader br = null;
		String content = null;
		try {
			Scanner scanner = new Scanner(new URL(fUrl).openConnection().getInputStream());
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				content = content + line;
			}

			fDocument = Jsoup.parse(content);
			scrapeData();
		} catch (Exception e) {
			System.out.println("Error occurred while trying to scrape data from " + fUrl
					+ " in tools.AbstractWebScraper.AbstractWebScraper()");
			e.printStackTrace();
		}

//		try {
//			// connect to web through browser and build Jsoup Document object with html file
//			// at the url
//			fDocument = Jsoup.connect(fUrl).userAgent(fBrowserVersion).execute().parse();
//		} catch (IOException e) {
//			System.out.println("Error occurred while trying to connect to " + fUrl
//					+ " in tools.AbstractWebScraper.AbstractWebScraper()");
//			e.printStackTrace();
//		}

	}

	protected String getUrl() {
		return fUrl;
	}

	protected Document getDocument() {
		return fDocument;
	}

	protected abstract void scrapeData();

	/**
	 * writes and saves the html file from the specified url
	 * 
	 * @param htmlDestination: the folder in which to save the file
	 * @param htmlName:        the new name of the file
	 */
	protected void writeHTML(String htmlDestination, String htmlName) {
		FileWriter htmlFileWriter;
		try {
			// create new file in specified directory
			htmlFileWriter = new FileWriter(new File(htmlDestination, htmlName));

			// create PrintWriter and print html to the newly-created file
			PrintWriter htmlPrintWriter = new PrintWriter(htmlFileWriter);
			htmlPrintWriter.print(fDocument.html());

			// clean-up
			htmlPrintWriter.close();
		} catch (IOException e) {
			System.out.println("Error while writing html file (tools.AbstractWebScraper.writeHTML()");
			e.printStackTrace();
		}
	}

	/**
	 * writes and saves a JSONObject to a file with path/name fileName
	 * 
	 * @param fileName: file path and name, e.g. src/main/resources/file.json
	 * @param json
	 */
	protected void writeJSON(String fileName, JSONObject json) {
		try (FileWriter file = new FileWriter(fileName)) {

			// use Jackson PrettyPrinter to format the JSON nicely (so that it isn't all in
			// one ugly line)

			file.write(new ObjectMapper().writeValueAsString(json).replace('\\', '"'));
			file.flush();

			// file.write(new
			// ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(json));
			// file.flush();

		} catch (IOException e) {
			System.out.println("Error while writing JSON file (tools.AbstractWebScraper.writeJSON()");
			e.printStackTrace();
		}
	}
}
