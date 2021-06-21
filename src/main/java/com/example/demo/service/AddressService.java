package com.example.demo.service;

import org.springframework.stereotype.Service;

@Service
public class AddressService {
 
	public final static String MODEL_FILE_PATH= "/root/TAPs/workspace/environmentModel/";
	public final static String UPPAAL_PATH="/root/TAPs/workspace/uppaal64-4.1.24/bin-Linux/";
	public final static String IFD_FILE_PATH="/root/TAPs/workspace/ifd/";
	public final static String IFD_FILE_NAME="ifd.dot";
	public final static String SIMULATE_RESULT_FILE_PATH="/root/TAPs/workspace/simulationResult/";


	public static String changed_model_file_Name;
	private AddressService() {
		
	}
	public final static void setChangedModelFileName(String initModelFileName) {
		changed_model_file_Name=initModelFileName.substring(0, initModelFileName.lastIndexOf(".xml"))+"-changed.xml";
	}
}
