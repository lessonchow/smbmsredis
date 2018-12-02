package cn.smbms.pojo;

import java.io.Serializable;
import java.util.Date;

public class Provider implements Serializable {
	
	private Integer id;   //id
	private String proCode; //渚涘簲鍟嗙紪鐮�
	private String proName; //渚涘簲鍟嗗悕绉�
	private String proDesc; //渚涘簲鍟嗘弿杩�
	private String proContact; //渚涘簲鍟嗚仈绯讳汉
	private String proPhone; //渚涘簲鍟嗙數璇�
	private String proAddress; //渚涘簲鍟嗗湴鍧�
	private String proFax; //渚涘簲鍟嗕紶鐪�
	private Integer createdBy; //鍒涘缓鑰�
	private Date creationDate; //鍒涘缓鏃堕棿
	private Date modifyDate;//鏇存柊鏃堕棿
	private Integer modifyBy; //鏇存柊鑰�
	private String idPicPath; // 涓汉璇佷欢鐓�
	private String workPicPath; // 宸ヤ綔璇佷欢鐓�
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getProCode() {
		return proCode;
	}
	public void setProCode(String proCode) {
		this.proCode = proCode;
	}
	public String getProName() {
		return proName;
	}
	public void setProName(String proName) {
		this.proName = proName;
	}
	public String getProDesc() {
		return proDesc;
	}
	public void setProDesc(String proDesc) {
		this.proDesc = proDesc;
	}
	public String getProContact() {
		return proContact;
	}
	public void setProContact(String proContact) {
		this.proContact = proContact;
	}
	public String getProPhone() {
		return proPhone;
	}
	public void setProPhone(String proPhone) {
		this.proPhone = proPhone;
	}
	public String getProAddress() {
		return proAddress;
	}
	public void setProAddress(String proAddress) {
		this.proAddress = proAddress;
	}
	public String getProFax() {
		return proFax;
	}
	public void setProFax(String proFax) {
		this.proFax = proFax;
	}
	public Integer getCreatedBy() {
		return createdBy;
	}
	public void setCreatedBy(Integer createdBy) {
		this.createdBy = createdBy;
	}
	public Date getCreationDate() {
		return creationDate;
	}
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}
	public Integer getModifyBy() {
		return modifyBy;
	}
	public void setModifyBy(Integer modifyBy) {
		this.modifyBy = modifyBy;
	}
	public Date getModifyDate() {
		return modifyDate;
	}
	public void setModifyDate(Date modifyDate) {
		this.modifyDate = modifyDate;
	}
	public String getIdPicPath() {
		return idPicPath;
	}
	public void setIdPicPath(String idPicPath) {
		this.idPicPath = idPicPath;
	}
	public String getWorkPicPath() {
		return workPicPath;
	}
	public void setWorkPicPath(String workPicPath) {
		this.workPicPath = workPicPath;
	}
	
	
}
