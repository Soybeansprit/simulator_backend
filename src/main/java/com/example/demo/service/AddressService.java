package com.example.demo.service;

import org.springframework.stereotype.Service;

@Service
public class AddressService {
 
	public final static String MODEL_FILE_PATH= "D:\\workspace\\modelFile\\";
	public final static String UPPAAL_PATH="D:\\tools\\uppaal-4.1.24\\uppaal-4.1.24\\bin-Windows\\";
	public final static String IFD_FILE_PATH="D:\\workspace\\ifdFile\\";
	public final static String IFD_FILE_NAME="ifd.dot";
	public final static String SIMULATE_RESULT_FILE_PATH="D:\\workspace\\resultFile\\";
	public final static String SYSTEM="window";

//	public final static String MODEL_FILE_PATH= "/root/TAPs-Simulator/data/modelFile/";
//	public final static String UPPAAL_PATH="/root/TAPs-Simulator/tool/uppaal64-4.1.24/bin-Linux/";
//	public final static String IFD_FILE_PATH="/root/TAPs-Simulator/data/ifdFile/";
//	public final static String IFD_FILE_NAME="ifd.dot";
//	public final static String SIMULATE_RESULT_FILE_PATH="/root/TAPs-Simulator/data/resultFile/";
//	public final static String SYSTEM="linux";


	public static String changed_model_file_Name;
	public static String best_model_file_name;
	public static String getBest_model_file_name() {
		return best_model_file_name;
	}
	public static void setBest_model_file_name(String initModelFileName) {
		AddressService.best_model_file_name = initModelFileName.substring(0, initModelFileName.lastIndexOf(".xml"))+"-scenario-best.xml";
	}
	private AddressService() {
		
	}
	public final static void setChangedModelFileName(String initModelFileName) {
		changed_model_file_Name=initModelFileName.substring(0, initModelFileName.lastIndexOf(".xml"))+"-changed.xml";
	}
}
