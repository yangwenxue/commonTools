package com.ywx.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertyUtils {

	/** 配置文件拥有者 */
	private static Properties p = new Properties();

	/**
	 * 类初始化
	 */
	static {
		InputStream is = PropertyUtils.class.getResourceAsStream("/com/ywx/config/application.properties");
		//String filePath = System.getProperty("user.dir") + File.separator + "application.properties";//用于打包后
		//System.out.println("配置文件路径为：" + filePath);//用于打包后

		try {
			// is = new FileInputStream(filePath);//用于打包后
			// 读取配置参数
			p.load(is);

			// 关闭数据流
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * 获得配置属性值
	 * 
	 * @param key
	 *            配置属性
	 * @return 配置属性值
	 */
	public static String getProperty(String key) {
		return p.getProperty(key);
	}

	/**
	 * 获得配置属性值
	 * 
	 * @param key
	 *            配置属性
	 * @param defaultValue
	 *            默认值
	 * @return 配置属性值
	 */
	public static String getProperty(String key, String defaultValue) {
		return p.getProperty(key, defaultValue);
	}
}
