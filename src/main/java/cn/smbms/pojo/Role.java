package cn.smbms.pojo;

import java.io.Serializable;
import java.util.Date;

public class Role implements Serializable {
	
	private Integer id;   //id
	private String roleCode; //瑙掕壊缂栫爜
	private String roleName; //瑙掕壊鍚嶇О
	private Integer createdBy; //鍒涘缓鑰�
	private Date creationDate; //鍒涘缓鏃堕棿
	private Integer modifyBy; //鏇存柊鑰�
	private Date modifyDate;//鏇存柊鏃堕棿
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getRoleCode() {
		return roleCode;
	}
	public void setRoleCode(String roleCode) {
		this.roleCode = roleCode;
	}
	public String getRoleName() {
		return roleName;
	}
	public void setRoleName(String roleName) {
		this.roleName = roleName;
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
	
}
