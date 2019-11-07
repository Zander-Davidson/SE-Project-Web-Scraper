package tools;

import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class CatalogScraper extends AbstractWebScraper {

	public static JSONArray JSON_COURSE_LIST = new JSONArray();
	private ArrayList<Element> fSubject = new ArrayList<Element>();

	public CatalogScraper() {
		super("chrome/78.0.3904.70", "https://catalog.uark.edu/undergraduatecatalog/coursesofinstruction/");
	}

	/**
	 * Instantiates a series of web scrapers to pull data from UARK website to store
	 * in JSONObjects. Requires some very particular parsing, so this will probably
	 * not hold up if the catalog or course page HTML hierarchies are changed in the
	 * future
	 */
	@Override
	protected void scrapeData() {

		// *[@id="textcontainer"]/div/table/tbody
		fSubject = getDocument().select("#textcontainer > div > table > tbody > tr");

		// loop through the subject list on UARK catalog site
		for (int i = 1; i < fSubject.size(); i++) {
			// each subject contains a link to a page with the courses of that subject =>
			// instantiate an anonymous instance of an AbstractWebScraper for a subject and
			// scrape the data for its courses
			AbstractWebScraper courseScraper = new AbstractWebScraper("chrome/78.0.3904.70",
					"https://catalog.uark.edu" + fSubject.get(i).selectFirst("a").attr("href")) {

				@Override
				protected void scrapeData() {
					// a list of all the courses in the html page for a subject
					ArrayList<Element> course = getDocument().select("#courseinventorycontainer > div > div");

					// loop through each course to scrape, putting data into JSON mappings as we go
					// along
					for (int j = 1; j < course.size(); j++) {
						String basicInfo = course.get(j).selectFirst("strong").text();
						JSONObject jsonCourse = new JSONObject();

						// System.out.println(basicInfo); // show live progress

						// subject and number get their own fields for easier search implementation
						jsonCourse.put("Subject", basicInfo.substring(0, 4)); // e.g. ACCT
						jsonCourse.put("Number", basicInfo.substring(5, basicInfo.indexOf(". "))); // e.g. 2013

						// unique identifier, for easy linking to prereqs (subject code + course #, eg:
						// ACCT 2013)
						jsonCourse.put("UID", basicInfo.substring(0, basicInfo.indexOf(". ")));

						basicInfo = basicInfo.substring(basicInfo.indexOf(". ") + 1).stripLeading();
						jsonCourse.put("Name", basicInfo.substring(0, basicInfo.indexOf(". ")));

						basicInfo = basicInfo.substring(basicInfo.indexOf(". ") + 1).stripLeading();
						jsonCourse.put("Hours", basicInfo.substring(0, basicInfo.indexOf(" Hour")));

						Element description = course.get(j).select("p:eq(1)").get(0);
						jsonCourse.put("Description", description.text());

						// prerequisites are stored in an array of html elements within the description;
						// pop elements off until the list is empty and collect the UIDs of the prereq
						// courses
						JSONArray jsonPrereqs = new JSONArray();
						Elements htmlPrereqs = description.children();
						while (!htmlPrereqs.isEmpty()) {
							if (htmlPrereqs.get(0).hasAttr("href")) {
								jsonPrereqs.add(htmlPrereqs.select("a").get(0).text());
							}
							htmlPrereqs.remove(0);
						}
						jsonCourse.put("Prerequisites", jsonPrereqs);

						JSON_COURSE_LIST.add(jsonCourse);
					}
				}
			};
		}

		// pass the completed JSON course list to the file-writing method
		JSONObject json = new JSONObject();
		json.put("Courses", JSON_COURSE_LIST);
		writeJSON("src\\main\\resources\\courses.json", json);
	}
}
