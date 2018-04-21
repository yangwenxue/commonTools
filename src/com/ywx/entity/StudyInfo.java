package com.ywx.entity;

public class StudyInfo {
	private String xzArea;
	private String hospital;
	private String orgCoe;
	private String fileSituation;
	private String dataBus;

	public String getXzArea() {
		return xzArea;
	}

	public void setXzArea(String xzArea) {
		this.xzArea = xzArea;
	}

	public String getHospital() {
		return hospital;
	}

	public void setHospital(String hospital) {
		this.hospital = hospital;
	}

	public String getOrgCoe() {
		return orgCoe;
	}

	public void setOrgCoe(String orgCoe) {
		this.orgCoe = orgCoe;
	}

	public String getFileSituation() {
		return fileSituation;
	}

	public void setFileSituation(String fileSituation) {
		this.fileSituation = fileSituation;
	}

	public String getDataBus() {
		return dataBus;
	}

	public void setDataBus(String dataBus) {
		this.dataBus = dataBus;
	}

	@Override
	public String toString() {
		return "[行政区划=" + xzArea + "; 医院名称=" + hospital + "; 机构代码=" + orgCoe + "; 影像数据归档情况=" + fileSituation + "; 影像诊断开展情况=" + dataBus + "]";
	}
}
