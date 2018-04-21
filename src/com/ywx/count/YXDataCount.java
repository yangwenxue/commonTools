package com.ywx.count;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.CellRangeAddress;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.ywx.db.YXDataBaseConnection;
import com.ywx.entity.StudyInfo;
import com.ywx.utils.PropertyUtils;

public class YXDataCount {

	private final static String NEW_LINE = "\r\n";

	private static String MSG0 = "业务通";
	private static String MSG1 = "业务不通";
	private static String MSG2 = "放射通，无超声。";
	private static String MSG3 = "无放射，超声通。";
	private static String MSG4 = "数据通";
	private static String MSG5 = "数据不通";

	/**
	 * US 超声 <br/>
	 * DS 放射DS <br/>
	 * RF 放射RF <br/>
	 * DX 放射DX <br/>
	 * DR 放射DR <br/>
	 * CT 放射CT <br/>
	 * CR 放射CR
	 */

	private Connection conn = null;

	public YXDataCount() {
		YXDataBaseConnection db = new YXDataBaseConnection();
		conn = db.getConnection();
	}

	public void countYingXiangData() throws SQLException {

		deleteOldFile();
		Set<StudyInfo> set = new HashSet<StudyInfo>();
		String startTime = PropertyUtils.getProperty("startTime");
		String endTime = PropertyUtils.getProperty("endTime");
		String executeSQL = getExecuteSQLApplicationCount();
		// 如果不配置时间
		if ("true".equals(PropertyUtils.getProperty("isOpenDateCount"))) {
			executeSQL = getExecuteSQLApplicationCountByTime();
		}
		executeSQL = executeSQL.replace("startTime", "'" + startTime + "'");
		executeSQL = executeSQL.replace("endTime", "'" + endTime + "'");
		PreparedStatement pstmt = null;
		pstmt = conn.prepareStatement(executeSQL);
		System.out.println("需要执行的SQL:" + executeSQL);
		ResultSet rs = pstmt.executeQuery(executeSQL);
		StudyInfo studyInfo = null;
		while (rs.next()) {
			studyInfo = new StudyInfo();
			String zxqh = rs.getString("XZQH");
			String yymc = rs.getString("YYMC");
			String jgdm = rs.getString("JGDM");
			String sblx = rs.getString("SBLX");
			String sqsl = rs.getString("SQSL");
			// System.out.println(zxqh + ":" + yymc + ":" + jgdm + ":" + sblx +
			// ":" + sqsl);
			studyInfo.setXzArea(zxqh);// 行政区划
			studyInfo.setHospital(yymc);// 医院名称
			studyInfo.setOrgCoe(jgdm);// 机构代码
			// 设置影像数据归档情况
			studyInfo.setFileSituation(getFileSituationMsg(studyInfo));
			// 设置业务是否通
			if (sqsl != null && Integer.parseInt(sqsl) > 0) {
				studyInfo.setDataBus(MSG0 + "[" + Integer.parseInt(sqsl) + "]");
			} else {
				studyInfo.setDataBus(MSG1 + "[" + Integer.parseInt(sqsl) + "]");
			}
			// if (studyInfo.getXzArea() != null &&
			// !"".equals(studyInfo.getXzArea()) && studyInfo.getOrgCoe() !=
			// null && !"".equals(studyInfo.getOrgCoe())) {
			// }
			set.add(studyInfo);
		}

		String path = PropertyUtils.getProperty("YXtxtTargetFile");
		try {
			writeDataToTxt(set, path);
			writeDataToExcel(set);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 设置影像数据归档情况.
	 */
	private String getFileSituationMsg(StudyInfo studyInfo) throws SQLException {
		String msg = null;
		Set<String> deviceSet = null;
		String executeSQL = getExecuteSQLGUIDANG();
		//不需要根据时间查询
		if("true".equals(PropertyUtils.getProperty("isOpenDateCount"))){
			String startTime = PropertyUtils.getProperty("startTime");
			String endTime = PropertyUtils.getProperty("endTime");
			executeSQL = getExecuteSQLGUIDANGByTIME();//getExecuteSQLGUIDANGByTIME
			executeSQL = executeSQL.replace("startTime", "'" + startTime + "'");
			executeSQL = executeSQL.replace("endTime", "'" + endTime + "'");
		}
		PreparedStatement pstmt = null;

		deviceSet = new HashSet<String>();
		executeSQL = executeSQL.replace("?", "'" + studyInfo.getOrgCoe() + "'");
		pstmt = conn.prepareStatement(executeSQL);
		ResultSet rs = pstmt.executeQuery(executeSQL);
		while (rs.next()) {
			String deviceName = rs.getString("deviceName");
			deviceSet.add(deviceName);
		}

		// 无放射，超声通。
		if (deviceSet.contains("US") && !deviceSet.contains("DS") && !deviceSet.contains("RF") && !deviceSet.contains("DX") && !deviceSet.contains("DR")
				&& !deviceSet.contains("CT") && !deviceSet.contains("CR")) {
			msg = MSG3 + deviceSet;
		}

		// 放射通，无超声。
		if (!deviceSet.contains("US")
				&& (deviceSet.contains("DS") || deviceSet.contains("RF") || deviceSet.contains("DX") || deviceSet.contains("DR") || deviceSet.contains("CT") || deviceSet
						.contains("CR"))) {
			msg = MSG2 + deviceSet;
		}

		// 数据通
		if (deviceSet.contains("US")
				&& (deviceSet.contains("DS") || deviceSet.contains("RF") || deviceSet.contains("DX") || deviceSet.contains("DR") || deviceSet.contains("CT") || deviceSet
						.contains("CR"))) {
			msg = MSG4 + deviceSet;
		}
		// 数据不通
		if (deviceSet == null || deviceSet.size() == 0) {
			msg = MSG5;
		}
		return msg;
	}

	/**
	 * 删除原来old文件.
	 * 
	 */
	private void deleteOldFile() {
		String excelPath = PropertyUtils.getProperty("YXexcelTargetFile");
		String txtPath = PropertyUtils.getProperty("YXtxtTargetFile");
		File excelFile = new File(excelPath);
		File txtFile = new File(txtPath);
		if (excelFile.exists()) {
			boolean b = excelFile.delete();
			if (b) {
				System.out.println("删除老excel文件成功！");
			}
		}
		if (txtFile.exists()) {
			boolean b = txtFile.delete();
			if (b) {
				System.out.println("删除老txt文件成功！");
			}
		}
	}

	/**
	 * 写到excel.
	 * 
	 * @throws IOException
	 */
	public static void writeDataToExcel(Set<StudyInfo> set) throws IOException {
		// 创建HSSFWorkbook对象
		HSSFWorkbook wb = new HSSFWorkbook();
		// 创建HSSFSheet对象
		HSSFSheet sheet = wb.createSheet("影像数据统计");

		// 创建HSSFRow对象
		HSSFRow row = sheet.createRow(0);
		// 创建HSSFCell对象
		// HSSFCell cell = row.createCell(0);
		// 设置单元格的值
		row.createCell(0).setCellValue("行政区划");
		row.createCell(1).setCellValue("医院名称");
		row.createCell(2).setCellValue("机构代码");
		row.createCell(3).setCellValue("影像数据归档情况");
		row.createCell(4).setCellValue("影像诊断开展情况");
		// 创建第二行
		// ////////////////////
		Iterator<StudyInfo> itor = set.iterator();
		HSSFRow dataRow = null;
		int index = 1;
		while (itor.hasNext()) {
			StudyInfo studyInfo = itor.next();
			dataRow = sheet.createRow(index);
			for (int j = 0; j < 5; j++) {
				if (j == 0) {
					dataRow.createCell(j).setCellValue(studyInfo.getXzArea());
				}
				if (j == 1) {
					dataRow.createCell(j).setCellValue(studyInfo.getHospital());
				}
				if (j == 2) {
					dataRow.createCell(j).setCellValue(studyInfo.getOrgCoe());
				}
				if (j == 3) {
					dataRow.createCell(j).setCellValue(studyInfo.getFileSituation());
				}
				if (j == 4) {
					dataRow.createCell(j).setCellValue(studyInfo.getDataBus());
				}
			}
			index++;
		}
		// 输出Excel文件
		String path = PropertyUtils.getProperty("YXexcelTargetFile");
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

	public void writeDataToTxt(Set<StudyInfo> set, String path) throws IOException {
		File targetFile = new File(path);
		BufferedWriter bw = null;
		if (!targetFile.exists()) {
			try {
				targetFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			targetFile.delete();
		}
		try {
			bw = new BufferedWriter(new FileWriter(targetFile, true));
			bw.write("行政区划------医院名称------机构代码------影像数据归档情况------影像诊断开展情况");
			bw.write(NEW_LINE);
			for (StudyInfo s : set) {
				System.out.println(s);
				bw.write(s.toString()); // 写入文件
				bw.write(NEW_LINE);
				bw.flush(); // 将存储在管道中的数据强制刷新出去
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (bw != null) {
					bw.close();
				}
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}

	}

	/**
	 * 根据时间参数统计申请业务量.
	 * @since JDK 1.8
	 */
	public static String getExecuteSQLApplicationCountByTime() {
		String SQL = "select NVL((select p.ssqy from providerinfo p where p.ptid = t.providerid),t.providerid) as XZQH,"
				+ "(select d.departmentname from department d where d.departmentid = t.hospitalcode) as YYMC,"
				+ "'' || t.hospitalcode as JGDM,t.devicetypename as SBLX,count(t.providerid) as SQSL "
				+ "from studyinfo_archive t where t.reporttime between to_date(startTime, 'yyyy-mm-dd hh24:mi:ss') and to_date(endTime, 'yyyy-mm-dd hh24:mi:ss') "
				+ "and t.studystatus = 70 and t.disableflag = 0 group by t.providerid, t.hospitalname, t.hospitalcode, t.devicetypename";
		return SQL;
	}
	
	/**
	 * 不根据时间统计申请业务量.
	 */
	public static String getExecuteSQLApplicationCount() {
		String SQL = "select NVL((select p.ssqy from providerinfo p where p.ptid = t.providerid),t.providerid) as XZQH,"
				+ "(select d.departmentname from department d where d.departmentid = t.hospitalcode) as YYMC,"
				+ "'' || t.hospitalcode as JGDM,t.devicetypename as SBLX,count(t.providerid) as SQSL "
				+ "from studyinfo_archive t where t.studystatus = 70 and t.disableflag = 0 group by t.providerid, t.hospitalname, t.hospitalcode, t.devicetypename";
		return SQL;
	}

	/**
	 * 根据时间查询影像数据归档.
	 */
	public static String getExecuteSQLGUIDANGByTIME() {
		String SQL = "select distinct T.DEVICETYPENAME as deviceName "
				+ "from STUDYINFO_ARCHIVE t where T.HOSPITALCODE=? and t.providerid not in ('03-013-kaili-YX') "
				+ "and t.applyreceivetime between to_date(startTime, 'yyyy-mm-dd hh24:mi:ss') "
				+ "and to_date(endTime, 'yyyy-mm-dd hh24:mi:ss')";
		return SQL;
	}
	public static String getExecuteSQLGUIDANG() {
		String SQL = "select distinct T.DEVICETYPENAME as deviceName "
				+ "from STUDYINFO_ARCHIVE t where T.HOSPITALCODE=? and t.providerid not in ('03-013-kaili-YX')";
		return SQL;
	}


	public static void main(String[] args) {
		try {
			writeDataToExcel(null);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
