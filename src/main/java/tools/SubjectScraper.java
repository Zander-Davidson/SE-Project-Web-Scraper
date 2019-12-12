package tools;

public class SubjectScraper extends AbstractWebScraper {

	public SubjectScraper(String browserVersion, String url) {
		super("chrome/78.0.3904.70", "https://catalog.uark.edu/undergraduatecatalog/coursesofinstruction/");
	}

	@Override
	protected void scrapeData() {

	}

}
