package com.ywx;

import java.sql.SQLException;

import com.ywx.count.YXDataCount;

public class CountYingXiang {
	public static void main(String[] args) {
		try {
			YXDataCount dataCount = new YXDataCount();
			dataCount.countYingXiangData();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
