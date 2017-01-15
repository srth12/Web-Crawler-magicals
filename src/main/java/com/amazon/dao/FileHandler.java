package com.amazon.dao;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

public class FileHandler {

	/**
	 * This function will help read 
	 * @param fileName
	 * @param field
	 * @return 
	 */
	public Properties readFile(String fileName){
		String defaultVal="./resources/url_paths.data";
		fileName=fileName.length()==0?defaultVal:fileName;
		Properties properties = new Properties();
		InputStream file = null;
		try{
			file = new FileInputStream(fileName);
			properties.load(file);
			
		}catch(IOException e){
			System.err.println("error reading url_paths.data file");
			e.printStackTrace();
		}finally{
			try {
				file.close();
			} catch (IOException e) {
				System.err.println("Can't close file");
				e.printStackTrace();
			}
		}
		return properties;
	}
	
	public void writeFile(String fileName, String data){
		try {
			OutputStream writer = new FileOutputStream(fileName);
			writer.write(data.getBytes());
			writer.flush();
		} catch (FileNotFoundException e) {
			System.err.println("Failed to find file");
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("Failed to write file");
			e.printStackTrace();
		}
		
	}
}
