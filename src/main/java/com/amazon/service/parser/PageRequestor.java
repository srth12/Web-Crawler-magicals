package com.amazon.service.parser;

import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.amazon.dao.FileHandler;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class PageRequestor {

	final static Logger logger = Logger.getLogger(PageRequestor.class);
	private static String HOST_NAME = "http://www.amazon.in/";

	public static void main(String[] args) {
		PageRequestor.getHTML();
	}

	/**
	 * This function will get the page mentioned in the url_path file
	 */
	public static void getHTML() {
		// URL url = new URL("http://google.com");
		// URLConnection connection = url.openConnection();
		// connection.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible;
		// MSIE 6.0; Windows NT 5.1; .NET CLR 1.0.3705; .NET CLR 1.1.4322; .NET
		// CLR 1.2.30703)");
		WebClient client = new WebClient();
		client.getOptions().setJavaScriptEnabled(false);
		client.getOptions().setCssEnabled(false);
		FileHandler file = new FileHandler();
		try {
			Properties fileData = file.readFile("");
			String url = fileData.get("url1").toString();
			HtmlPage page = client.getPage(url);
			// System.out.println(page.asXml());
			file.writeFile("Catogary Page.txt", page.asXml());
			logger.debug("file updated. file name is:" + "Catogary Page.txt");
			List<HtmlElement> items = (List<HtmlElement>) page.getByXPath("//ul[@class='nav_cat_links']/li/a");
			if (items.isEmpty()) {
				System.out.println("No items found !");
			} else {
				for (HtmlElement item : items) {
					// System.out.println(item.asXml());
					HtmlAnchor itemAnchor = (HtmlAnchor) item;
					String itemName = itemAnchor.asText();
					String itemUrl = itemAnchor.getHrefAttribute();
					System.out.println(String.format("Name : %s Url : %s ", itemName, itemUrl));

				}
			}

		} catch (Exception e) {
			logger.error("Failed to get page",e);
			System.err.println("Failed to get page");
			e.printStackTrace();
		}

	}
}
