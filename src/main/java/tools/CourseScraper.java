package tools;

import java.io.IOException;

public class CourseScraper extends AbstractWebScraper {

	public CourseScraper() throws IOException {
		super("chrome/78.0.3904.70", "https://catalog.uark.edu/undergraduatecatalog/coursesofinstruction/");
	}
}
