package tools;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class AbstractWebScraper {

	private final String fBrowserVersion; // as of 10-31-19 chrome version is: chrome/78.0.3904.70
	private String fUrl; // url to scrape from
	private final Document fDocument; // Jsoup Document object from html

	public AbstractWebScraper(String browserVersion, String url) throws IOException {
		fBrowserVersion = browserVersion;
		fUrl = url;
		fDocument = Jsoup.connect(fUrl).userAgent(fBrowserVersion).get(); // connect to web through browser and build
																			// Document object with html file at the url
	}

	protected String getUrl() {
		return fUrl;
	}

	protected Document getDocument() {
		return fDocument;
	}

	/**
	 * writes and saves the html file from the specified url
	 * 
	 * @param htmlDestination: the folder in which to save the file
	 * @param htmlName:        the new name of the file
	 */
	public void writeHtml(String htmlDestination, String htmlName) {
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
			System.out.println("Error while writing html file (src\\main\\java\\tools\\AbstractWebScraper.java)");
		}
	}
}
