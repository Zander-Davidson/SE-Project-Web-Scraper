package tools;

import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class CatalogScraper extends AbstractWebScraper {

	public static JSONArray JSON_COURSE_LIST = new JSONArray();
	public static JSONArray JSON_SUBJECT_LIST = new JSONArray();
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
			JSONObject subject = new JSONObject();

			String subjectName = fSubject.get(i).selectFirst("a").text();
			subject.put("subjectCode", subjectName.substring(subjectName.length() - 5, subjectName.length() - 1));
			subject.put("subjectName", subjectName);
			JSON_SUBJECT_LIST.add(subject);

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
						jsonCourse.put("subject", basicInfo.substring(0, 4)); // e.g. ACCT
						String number = basicInfo.substring(5, basicInfo.indexOf(". "));
						jsonCourse.put("number", number); // e.g. 2013

						if (number.charAt(0) == '1') {
							jsonCourse.put("level", "freshman");
						} else if (number.charAt(0) == '2') {
							jsonCourse.put("level", "sophomore");
						} else if (number.charAt(0) == '3') {
							jsonCourse.put("level", "junior");
						} else if (number.charAt(0) == '4') {
							jsonCourse.put("level", "senior");
						} else if (Integer.parseInt(number.substring(0, 1)) > 4) {
							jsonCourse.put("level", "graduate");
						} else {
							jsonCourse.put("level", "other");
						}

						jsonCourse.put("honors", number.contains("H") ? 1 : 0);
						jsonCourse.put("lab", number.contains("L") ? 1 : 0);
						if (number.contains("M")) {
							jsonCourse.put("honors", 1);
							jsonCourse.put("lab", 1);
						}
						jsonCourse.put("drill", number.contains("C") ? 1 : 0);
						jsonCourse.put("variable", number.contains("V") ? 1 : 0);

						// unique identifier, for easy linking to prereqs (subject code + course #, eg:
						// ACCT2013)
						jsonCourse.put("id", basicInfo.substring(0, basicInfo.indexOf(". ")).replaceAll(" ", ""));

						basicInfo = basicInfo.substring(basicInfo.indexOf(". ") + 1).stripLeading();
						jsonCourse.put("name", basicInfo.substring(0, basicInfo.indexOf(". ")));

						basicInfo = basicInfo.substring(basicInfo.indexOf(". ") + 1).stripLeading();
						basicInfo = basicInfo.substring(0, basicInfo.indexOf(" Hour"));
						while (basicInfo.contains(" ")) {
							basicInfo = basicInfo.substring(basicInfo.indexOf(' ') + 1);
						}
						jsonCourse.put("minhours", basicInfo.substring(0,
								basicInfo.contains("-") ? basicInfo.indexOf('-') : basicInfo.length()));
						basicInfo = basicInfo.substring(basicInfo.contains("-") ? basicInfo.indexOf('-') + 1 : 0);
						jsonCourse.put("maxhours", basicInfo);

						Element description = course.get(j).select("p:eq(1)").get(0);
						jsonCourse.put("description", description.text());

						// prerequisites are stored in an array of html elements within the description;
						// pop elements off until the list is empty and collect the UIDs of the prereq
						// courses
						JSONArray jsonPrereqs = new JSONArray();
						Elements htmlPrereqs = description.children();
						while (!htmlPrereqs.isEmpty()) {
							if (htmlPrereqs.get(0).hasAttr("href")) {
								jsonPrereqs.add(htmlPrereqs.select("a").get(0).text().replaceAll(" ", ""));
							}
							htmlPrereqs.remove(0);
						}
						jsonCourse.put("prerequisites", jsonPrereqs);

						JSON_COURSE_LIST.add(jsonCourse);
					}
				}
			};
		}

		// pass the completed JSON course list to the file-writing method
		JSONObject jsonCourses = new JSONObject();
		jsonCourses.put("Courses", JSON_COURSE_LIST);
		writeJSON("src\\main\\resources\\courses.json", jsonCourses);

		JSONObject jsonSubjects = new JSONObject();
		jsonSubjects.put("Subjects", JSON_SUBJECT_LIST);
		writeJSON("src\\main\\resources\\subjects.json", jsonSubjects);
	}
}
