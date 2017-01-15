package com.amazon.service.parser;

public class ProductFetcher implements Runnable {

	private String catagUrl;
	private int threadId;
	public ProductFetcher(String catagUrl, int threadId) {
		this.catagUrl = catagUrl;
		this.threadId = threadId;
	}

	public void run() {

	}

}
