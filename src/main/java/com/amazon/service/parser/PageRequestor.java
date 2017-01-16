package com.amazon.service.parser;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import com.amazon.cache.RedisCache;
import com.amazon.dao.FileHandler;
import com.amazon.dto.DataAccess;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSpan;

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
		client.getOptions().setJavaScriptEnabled(false);
		client.getOptions().setCssEnabled(false);
		insertData = new DataAccess();
		redis = new RedisCache();
	}

	public static void main(String[] args) {
		PageRequestor requestor = new PageRequestor();
		requestor.insertData.dropTable(CATAG_URLS);
		// requestor.getCatagList();
		requestor.getAllProducts();
	}

	/**
	 * This function will get the page mentioned in the url_path file
	 */
	public void getCatagList() {
		// URL url = new URL("http://google.com");
		// URLConnection connection = url.openConnection();
		// connection.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible;
		// MSIE 6.0; Windows NT 5.1; .NET CLR 1.0.3705; .NET CLR 1.1.4322; .NET
		// CLR 1.2.30703)");
		
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

	public void insertCatagUrls(List<HtmlElement> items) {
		

		if (items.isEmpty()) {
			System.out.println("No items found !");
		} else {
			for (HtmlElement item : items) {
				// System.out.println(item.asXml());
				HtmlAnchor itemAnchor = (HtmlAnchor) item;
				String itemName = itemAnchor.asText();
				String itemUrl = itemAnchor.getHrefAttribute();
				if(!itemUrl.contains("amazon.in") || !itemUrl.contains("amazon.com")){
//					itemUrl = HOST_NAME + itemUrl;
				}
				List<String> fields = Arrays.asList(CATAG_URLS, "catag_name", "is_active");
				List<String> values = Arrays.asList("'" +  itemUrl + "'",
						"'" + itemName.replace("'", "/") + "'", "'1'");
				redis.insertDataToList(CATAG_URLS,  itemUrl);
				insertData.insertData(CATAG_URLS, fields, values);
				// System.out.println(String.format("Name : %s Url : %s ",
				// itemName, itemUrl));

			}
		}
	}

	public void getAllProducts() {

		ExecutorService executor = Executors.newFixedThreadPool(50);
		int i = 0;
		for (String url : redis.getAllDataFromList(CATAG_URLS)) {
			try {
				FileHandler file = new FileHandler();
				if(url.startsWith("http://www.amazon.in/http://www.amazon.in/")){
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
				} else {
					items = (List<HtmlElement>) page.getByXPath("//div[@id='productDescription']");
					if (items.size() > 0) {
						String desc = items.get(0).asText();
						items = (List<HtmlElement>) page.getByXPath("//input[@name='ASIN']");
						HtmlInput input = (HtmlInput) items.get(0);
						String asin = input.getValueAttribute();
						items = (List<HtmlElement>) page.getByXPath("//span[@id='productTitle']");
						HtmlSpan item = (HtmlSpan) items.get(0);
						String title = item.asText();
						item = (HtmlSpan) page.getByXPath("//span[@id='priceblock_ourprice']").get(0);
						String our_price = item.asText();

						List<String> columnNames = Arrays.asList("ASIN", "NAME", "URL", "DESCRIPTION", "IS_ACTIVE",
								"ACTUAL_PRICE");
						List<String> values = Arrays.asList("'" + asin + "'", "'" + title + "'", "'" + url + "'",
								"'" + desc + "'", "'" + "1" + "'", "'" + our_price.trim() + "'");
						insertData.insertData(PRODUCTS, columnNames, values);

					}
				}
				System.out.println(url);
			} catch (Exception e) {
				System.err.println("Bad url");
				logger.error("Bad url", e);
				List<String> columnNames = Arrays.asList("FULL_URL", "is_active");
				List<String> values = Arrays.asList("'" + url + "'", "'" + "1" + "'");
				insertData.insertData(BAD_URL_LIST, columnNames, values);
				e.printStackTrace();
			}
		}
		client.close();

	}
}
