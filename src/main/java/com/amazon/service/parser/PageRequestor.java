package com.amazon.service.parser;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.amazon.cache.RedisCache;
import com.amazon.dao.FileHandler;
import com.amazon.dto.DataAccess;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSpan;

/**
 * 
 * @author Sarath Babu
 *
 *This class will request and store all the urls and product details
 *from amazon.in website
 */
public class PageRequestor {

	public static final String PRODUCTS = "PRODUCTS";
	public static final String BAD_URL_LIST = "BAD_URL_LIST";
	public static RedisCache redis;
	public static final String CATAG_URLS = "catag_url";
	final static Logger logger = Logger.getLogger(PageRequestor.class);
	private static String HOST_NAME = "http://www.amazon.in/";
	private WebClient client;
	private DataAccess insertData;

	public PageRequestor() {
		client = new WebClient();
		client.addRequestHeader("User-Agent",
				"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.90 Safari/537.36");
		client.getOptions().setJavaScriptEnabled(false);
		client.getOptions().setCssEnabled(false);
		insertData = new DataAccess();
		redis = new RedisCache();
	}

	public static void main(String[] args) {
		PageRequestor requestor = new PageRequestor();
		requestor.insertData.dropTable(CATAG_URLS);
		 requestor.getCatagList();
		requestor.getAllProducts();
	}

	/**
	 * This function will get the page mentioned in the url_path file
	 */
	public void getCatagList() {

		FileHandler file = new FileHandler();
		try {
			Properties fileData = file.readFile("");
			String url = fileData.get("url1").toString();
			HtmlPage page = client.getPage(url);
			// System.out.println(page.asXml());
			file.writeFile("Catogary Page.txt", page.asXml());
			logger.debug("file updated. file name is:" + "Catogary Page.txt");
			List<HtmlElement> items = (List<HtmlElement>) page.getByXPath("//ul[@class='nav_cat_links']/li/a");

			insertCatagUrls(items);

		} catch (Exception e) {
			logger.error("Failed to get page", e);
			System.err.println("Failed to get page");
			e.printStackTrace();
		}

	}
/**
 * It will insert the fetched category urls form category page
 * @param items
 */
	public void insertCatagUrls(List<HtmlElement> items) {

		if (items.isEmpty()) {
			System.out.println("No items found !");
		} else {
			for (HtmlElement item : items) {
				// System.out.println(item.asXml());
				HtmlAnchor itemAnchor = (HtmlAnchor) item;
				String itemName = itemAnchor.asText();
				String itemUrl = itemAnchor.getHrefAttribute();
				if (!itemUrl.startsWith("http://www.amazon.in/") || !itemUrl.startsWith("http://www.amazon.com/")) {
					itemUrl = HOST_NAME + itemUrl;
				}
				List<String> fields = Arrays.asList(CATAG_URLS, "catag_name", "is_active");
				List<String> values = Arrays.asList("'" + itemUrl + "'", "'" + itemName.replace("'", "/") + "'", "'1'");
				redis.insertDataToList(CATAG_URLS, itemUrl);
				insertData.insertData(CATAG_URLS, fields, values);

			}
		}
	}

	/**
	 * This function will fetch the product details form the amazon website.
	 * If the fetching process fails in between, the url will be inserted to the
	 * table- 'BAD_URL_LIST'
	 * If it is a success, data will be populated to the table- 'products'
	 * 
	 */
	public void getAllProducts() {

		int i = 0;
		for (String url : redis.getAllDataFromList(CATAG_URLS)) {
			try {
				FileHandler file = new FileHandler();
				if (url.startsWith("http://www.amazon.in/http://www.amazon.in/")) {
					url = url.replace("http://www.amazon.in/http://www.amazon.in/", "http://www.amazon.in/");
				}
				HtmlPage page = client.getPage(url);
				file.writeFile((i++) + ".xml", page.asXml());
				List<HtmlElement> items = (List<HtmlElement>) page.getByXPath("//div[@id='mainResults']//li");
				if (items.size() != 0) {
					items = (List<HtmlElement>) page.getByXPath("//div[@id='mainResults']//li//a[@title]");
					insertCatagUrls(items);
					HtmlAnchor itemAnchor = (HtmlAnchor) items.get(0);
					System.out.println(itemAnchor.asText());
				}
				items = (List<HtmlElement>) page.getByXPath("//div[@id='productDescription']");
				if (items.isEmpty()) {
					items = (List<HtmlElement>) page.getByXPath("//div[@id='productDescription']/p");
				}
				if (items.size() > 0) {
					String desc = items.get(0).asText();
					desc = desc.substring(0, desc.length() > 2997 ? 2997 : desc.length());
					desc = cleanDirts(desc);
					if (desc == null || desc.length() == 0) {
						desc = "<No Data Found>";
					}
					items = (List<HtmlElement>) page.getByXPath("//input[@name='ASIN']");
					HtmlInput input = (HtmlInput) items.get(0);
					String asin = input.getValueAttribute();
					if (asin == null || asin.length() == 0) {
						asin = "<No Data Found>";
					}
					items = (List<HtmlElement>) page.getByXPath("//span[@id='productTitle']");
					HtmlSpan item = (HtmlSpan) items.get(0);
					String title = item.asText();
					title = cleanDirts(title);
					if (title == null || title.length() == 0) {
						title = "<No Data Found>";
					}
					item = (HtmlSpan) page.getByXPath("//span[@id='priceblock_ourprice']").get(0);
					String our_price = item.asText();
					if (our_price == null || our_price.length() == 0) {
						our_price = "<No Data Found>";
					}
					String is_active = (asin + title + url + desc + our_price).contains("<No Data Found>") ? "0" : "1";
					List<String> columnNames = Arrays.asList("ASIN", "NAME", "URL", "DESCRIPTION", "IS_ACTIVE",
							"ACTUAL_PRICE");
					List<String> values = Arrays.asList("'" + asin + "'", "'" + title + "'", "'" + url + "'",
							"'" + desc + "'", "'" + is_active + "'", "'" + our_price.trim() + "'");
					insertData.insertData(PRODUCTS, columnNames, values);
					System.out.println("Inserted: " + url);
				}

			} catch (Exception e) {
				System.err.println("Bad url:" + url);
				logger.error("Bad url", e);
				List<String> columnNames = Arrays.asList("FULL_URL", "is_active");
				List<String> values = Arrays.asList("'" + url + "'", "'" + "1" + "'");
				insertData.insertData(BAD_URL_LIST, columnNames, values);
				e.printStackTrace();
			}
		}
		client.close();

	}

	public String cleanDirts(String str) {
		str = str.replace("'", " ");
		str = str.replace("&", "and");
		str = str.replace("\"", " ");
		return str;
	}
}
