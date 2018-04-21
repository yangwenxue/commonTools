package com.ywx;

import java.sql.SQLException;

import com.ywx.count.JYDataCount;

public class CountJianyan {
	public static void main(String[] args) {
		JYDataCount jyDataCount = new JYDataCount();
		try {
			jyDataCount.count();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
