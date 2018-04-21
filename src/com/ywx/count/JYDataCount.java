package com.ywx.count;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import com.ywx.db.JYDataBaseConnection;
import com.ywx.entity.JianYan;
import com.ywx.entity.StudyInfo;
import com.ywx.utils.PropertyUtils;

public class JYDataCount {

	private Connection conn = null;

	public JYDataCount() {
		JYDataBaseConnection db = new JYDataBaseConnection();
		conn = db.getConnection();
	}

	public void count() throws SQLException {

		Set<JianYan> set = new HashSet<JianYan>();
		JianYan jianyan;

		Statement pstmt = null;
		String executeSQL = getExecuteSQL2();
		pstmt = conn.createStatement();
		System.out.println("需要执行的SQL:" + executeSQL);
		ResultSet rs = pstmt.executeQuery(executeSQL);
		while (rs.next()) {
			jianyan = new JianYan();
			String jgdm = rs.getString("jgdm");
			String jgdmCount = rs.getString("jgdmCount");
			System.out.println(jgdm);
			System.out.println("统计检验数据...");
			jianyan.setOrgCode(jgdm);
			jianyan.setCount(jgdmCount);
			set.add(jianyan);
		}
		try {
			writeDataToExcel(set);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void writeDataToExcel(Set<JianYan> set) throws IOException {
		// 创建HSSFWorkbook对象
		HSSFWorkbook wb = new HSSFWorkbook();
		// 创建HSSFSheet对象
		HSSFSheet sheet = wb.createSheet("检验数据统计");

		// 创建HSSFRow对象
		HSSFRow row = sheet.createRow(0);
		// 创建HSSFCell对象
		// HSSFCell cell = row.createCell(0);
		// 设置单元格的值
		row.createCell(0).setCellValue("机构代码");
		row.createCell(1).setCellValue("数量");

		// 创建第二行
		// ////////////////////
		Iterator<JianYan> itor = set.iterator();
		HSSFRow dataRow = null;
		int index = 1;
		while (itor.hasNext()) {
			JianYan jianYan = itor.next();
			dataRow = sheet.createRow(index);
			for (int j = 0; j < 2; j++) {
				if (j == 0) {
					dataRow.createCell(j).setCellValue(jianYan.getOrgCode());
				}
				if (j == 1) {
					dataRow.createCell(j).setCellValue(jianYan.getCount());
				}
			}
			index++;
		}
		// 输出Excel文件
		String path = PropertyUtils.getProperty("JYexcelTargetFile");
		File targetFile = new File(path);
		if (!targetFile.exists()) {
			try {
				targetFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		FileOutputStream output = new FileOutputStream(path);
		wb.write(output);
		output.flush();
	}

	public static String getExecuteSQL1() {
		return "select * FROM QUERY_LISREPORT_V T";
	}

	public static String getExecuteSQL2() {
		return "select T.YLJGDM as jgdm,count(T.YLJGDM) jgdmCount from QUERY_LISREPORT_V T WHERE T.YLJGDM IS not NULL AND T.YLJGDM != '' group by T.YLJGDM";
	}

}
