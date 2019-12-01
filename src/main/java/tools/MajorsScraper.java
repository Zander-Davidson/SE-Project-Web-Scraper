package tools;

import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;

public class MajorsScraper extends AbstractWebScraper {

	public static JSONArray JSON_MAJORS_LIST = new JSONArray();
	private ArrayList<Node> fMajor;

	public MajorsScraper() {
		super("chrome/78.0.3904.70",
				"https://catalog.uark.edu/undergraduatecatalog/fieldsofstudy/#fieldsalphabeticallytext");
	}

	@Override
	protected void scrapeData() {
		// *[@id="fieldsalphabeticallytextcontainer"]/div/p[2]
		fMajor = new ArrayList<Node>();
		for (Node child : getDocument().selectFirst("#fieldsalphabeticallytextcontainer > div > h1")
				.nextElementSibling().childNodesCopy()) {
			if (child.hasAttr("href")) {
				fMajor.add(child);
			}
		}

		for (Node major : fMajor) {

			AbstractWebScraper degreeProgramsScraper = new AbstractWebScraper("chrome/78.0.3904.70",
					major.attr("href").substring(0, 4).equals("http") ? major.attr("href")
							: "https://catalog.uark.edu" + major.attr("href")) {

				private ArrayList<Element> fPrograms;

				@Override
				protected void scrapeData() {
					fPrograms = getDocument().select("#tabs > ul > li");

					for (int j = 1; j < fPrograms.size()
							&& !getDocument().select(fPrograms.get(j).select("a").attr("href") + "container")
									.select("table").isEmpty(); j++) {

						ArrayList<Element> fRequirements = null;

						try {
							fRequirements = getDocument()
									.select(fPrograms.get(j).select("a").attr("href") + "container > table").get(0)
									.getElementsByTag("td");
						} catch (IndexOutOfBoundsException e) {
							System.out.println("caught!!");
							fRequirements = getDocument()
									.select(fPrograms.get(j).select("a").attr("href") + "container").get(0)
									.selectFirst("table").getElementsByTag("td");
						}

						JSONArray jsonRequirements = new JSONArray();

						while (fRequirements.size() > 2) {
							Element requirement = fRequirements.remove(0).selectFirst("td");
							if (requirement.className().equals("codecol")) {
								jsonRequirements.add(requirement.select("a").text());
							}
//							if (!requirement.text().equals("Year Total:")
//									&& fRequirements.get(0).className().equals("hourscol")
//									&& !requirement.className().equals("hourscol")) {
//
//								if (requirement.select("a").hasText()) {
//									jsonRequirements.add(requirement.select("a").text());
//								} else {
//									jsonRequirements.add(requirement.text());
//								}
//							}
						}

						JSONObject jsonProgram = new JSONObject();

						jsonProgram.put("Name", getDocument()
								.select(fPrograms.get(j).select("a").attr("href") + "tab > a").get(0).text());
						System.out.println(getDocument().select(fPrograms.get(j).select("a").attr("href") + "tab > a")
								.get(0).text());
						jsonProgram.put("Requirements", jsonRequirements);
						JSON_MAJORS_LIST.add(jsonProgram);
					}
				}

			};
		}

		JSONObject json = new JSONObject();
		json.put("Programs", JSON_MAJORS_LIST);
		writeJSON("src\\main\\resources\\programs.json", json);
	}

}
