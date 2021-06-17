package com.example.demo.service;

import org.springframework.stereotype.Service;

@Service
public class AddressService {
 
	public final static String MODEL_FILE_PATH= "D:\\workspace";
	public final static String UPPAAL_PATH="D:\\tools\\uppaal-4.1.24\\uppaal-4.1.24\\bin-Windows";
	public final static String IFD_FILE_NAME="ifd.dot";
	public static String changed_model_file_Name;
	private AddressService() {
		
	}
	public final static void setChangedModelFileName(String initModelFileName) {
		changed_model_file_Name=initModelFileName.substring(0, initModelFileName.lastIndexOf(".xml"))+"-changed.xml";
	}
}
