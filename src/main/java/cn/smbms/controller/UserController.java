package cn.smbms.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.mysql.jdbc.StringUtils;

import cn.smbms.pojo.Bill;
import cn.smbms.pojo.Role;
import cn.smbms.pojo.User;
import cn.smbms.service.RedisService;
import cn.smbms.service.bill.BillService;
import cn.smbms.service.provider.ProviderService;
import cn.smbms.service.role.RoleService;
import cn.smbms.service.user.UserService;
import cn.smbms.tools.Constants;
import cn.smbms.tools.PageSupport;

@Controller
@RequestMapping("/user")
public class UserController {

	@Resource
	private UserService userService;
	@Resource
	private RoleService roleService;
	@Autowired
	private ProviderService providerService;
	@Autowired
	private BillService billService;
	@Autowired
	private RedisService redisService;
	
	private Logger logger = Logger.getLogger(UserController.class);
	
//		if(method != null && method.equals("add")){
//			//增加操作
//			this.add(request, response);
//		}else if(method != null && method.equals("query")){
//			this.query(request, response);
//		}else if(method != null && method.equals("getrolelist")){
//			this.getRoleList(request, response);
//		}else if(method != null && method.equals("ucexist")){
//			this.userCodeExist(request, response);
//		}else if(method != null && method.equals("deluser")){
//			this.delUser(request, response);
//		}else if(method != null && method.equals("view")){
//			this.getUserById(request, response,"userview.jsp");
//		}else if(method != null && method.equals("modify")){
//			this.getUserById(request, response,"usermodify.jsp");
//		}else if(method != null && method.equals("modifyexe")){
//			this.modify(request, response);
//		}else if(method != null && method.equals("pwdmodify")){
//			this.getPwdByUserId(request, response);
//		}else if(method != null && method.equals("savepwd")){
//			this.updatePwd(request, response);
//		}
	
	@RequestMapping(value="/welcome")
	public String welcome() {
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				logger.info("Timer执行中========================");
				long start = System.currentTimeMillis(); //记录定时器执行起始时间
				String roleJsonList = null; // 接收角色列表的字符串
				try {
					if (!redisService.hasKey(Constants.ROLE_LIST)) {
						roleJsonList = JSON.toJSONString(roleService.getRoleList());
						redisService.set(Constants.ROLE_LIST,roleJsonList ,5*3600L);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				String billJsonList = null; // 接收订单列表的字符串
				try {
					if (!redisService.hasKey(Constants.BILL_LIST)) {
						billJsonList = JSON.toJSONString(billService.getBillList(new Bill()));
						redisService.set(Constants.BILL_LIST, billJsonList,1800L);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				String providerJsonList = null; // 接收订单列表的字符串
				try {
					if (redisService.hasKey(Constants.PROVIDER_LIST)) {
						providerJsonList = JSON.toJSONString(providerService.getProviderList("",""));
						redisService.set(Constants.PROVIDER_LIST,providerJsonList ,1800L);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				List<User> userList = null;
				String timeStamp = null;
				try {
					if (!redisService.hasKey(Constants.USER_LIST + ":ing")) {
						userList = userService.getUserList(null,0,0,0);
						timeStamp = String.valueOf(System.currentTimeMillis());
						if (redisService.hsetInPage(Constants.USER_LIST + ":" + timeStamp,userList)) {
							redisService.set(Constants.USER_LIST + ":ing", timeStamp, 2 * 3600l);
							redisService.expire(Constants.USER_LIST + ":" + timeStamp, 2 * 3600l);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				logger.info("Timer执行结束========================用时（毫秒）：" + (System.currentTimeMillis() - start));
				timer.cancel();
			}
		},0);//由于是在欢迎界面，所以不用延时，越快越好
		return "login";
	}
	
	@RequestMapping("/dologin")
	public String doLogin(HttpServletRequest request) {
		System.out.println("login ============ " );
		//获取用户名和密码
		String userCode = request.getParameter("userCode");
		String userPassword = request.getParameter("userPassword");
		//调用service方法，进行用户匹配
		User user;
		try {
			user = userService.login(userCode,userPassword);
		} catch (Exception e) {
			e.printStackTrace();
			request.setAttribute("loginInfo", e.getMessage());
			return "failure";
		}
		if(null != user){//登录成功
			//放入session
			request.getSession().setAttribute(Constants.USER_SESSION, user);
			// begin Timer coding
//			Timer timer = new Timer();
//			timer.schedule(new TimerTask() {
//
//				@Override
//				public void run() {
//					while (true) {
//						System.out.println("Test Timer*******************************************************************");
//						try {
//							Thread.sleep(1000);
//						} catch (InterruptedException e) {
//							e.printStackTrace();
//						}
//					}
//				}
//			}, 0);
			// end Timer coding
			
			//页面跳转（frame.jsp）
			return "jsp/frame";
		}else{
			//页面跳转（login.jsp）带出提示信息--转发
			request.setAttribute("error", "用户名或密码不正确");
			return "login";
		}
	}
	
	@RequestMapping("/dologout")
	public String doLogout(HttpServletRequest request) {
		//清除session
		request.getSession().removeAttribute(Constants.USER_SESSION);
		return "login";
	}
	
	@RequestMapping("/updatepwd")
	public String updatePwd(HttpServletRequest request) {
		
		Object o = request.getSession().getAttribute(Constants.USER_SESSION);
		String newpassword = request.getParameter("newpassword");
		boolean flag = false;
		if(o != null && !StringUtils.isNullOrEmpty(newpassword)){
			try {
				flag = userService.updatePwd(((User)o).getId(),newpassword);
			} catch (Exception e) {
				e.printStackTrace();
				request.setAttribute("info", e.getMessage());
				return "failure";
			}
			if(flag){
				request.setAttribute(Constants.SYS_MESSAGE, "修改密码成功,请退出并使用新密码重新登录！");
				request.getSession().removeAttribute(Constants.USER_SESSION);//session注销
			}else{
				request.setAttribute(Constants.SYS_MESSAGE, "修改密码失败！");
			}
		}else{
			request.setAttribute(Constants.SYS_MESSAGE, "修改密码失败！");
		}
		return "jsp/pwdmodify";
	}
	
	@RequestMapping(value="/getpwdbyuserid", produces="application/json;charset=utf-8")
	@ResponseBody
	public String getPwdByUserId(HttpServletRequest request) {
		Object o = request.getSession().getAttribute(Constants.USER_SESSION);
		String oldpassword = request.getParameter("oldpassword");
		Map<String, String> resultMap = new HashMap<String, String>();
		
		if(null == o ){//session过期
			resultMap.put("result", "sessionerror");
		}else if(StringUtils.isNullOrEmpty(oldpassword)){//旧密码输入为空
			resultMap.put("result", "error");
		}else{
			String sessionPwd = ((User)o).getUserPassword();
			if(oldpassword.equals(sessionPwd)){
				resultMap.put("result", "true");
			}else{//旧密码输入不正确
				resultMap.put("result", "false");
			}
		}
		return JSON.toJSONString(resultMap);
	}
	
	@RequestMapping("/modify")
	public String modify(@ModelAttribute User user, Model model, HttpServletRequest request) {
		user.setModifyBy(((User)request.getSession().getAttribute(Constants.USER_SESSION)).getId());
		user.setModifyDate(new Date());
		try {
			if(userService.modify(user)){
				try {
					String preTimeStamp = redisService.get(Constants.USER_LIST + ":ing");
					List<User> userList = userService.getUserList(null,0,0,0);
					String newTimeStamp = String.valueOf(System.currentTimeMillis());
					if (redisService.hsetInPage(Constants.USER_LIST + ":" + newTimeStamp,userList)) {
						redisService.set(Constants.USER_LIST + ":ing", newTimeStamp, 2 * 3600l);
						redisService.expire(Constants.USER_LIST + ":" + newTimeStamp, 2 * 3600l);
						redisService.expire(Constants.USER_LIST + ":" + preTimeStamp, 5l);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				return "redirect:/user/query";
			}else{
				return "jsp/usermodify";
			}
		} catch (Exception e) {
			e.printStackTrace();
			model.addAttribute("info", e.getMessage());
			return "failure";
		}
	}
	
	@RequestMapping("/getuserbyid")
	public String getUserById(String uid, Model model,String method) {
		if(!StringUtils.isNullOrEmpty(uid)){
			//调用后台方法得到user对象
			User user = null;
			try {
				user = userService.getUserById(uid);
			} catch (Exception e) {
				e.printStackTrace();
				model.addAttribute("info", e.getMessage());
				return "failure";
			}
			model.addAttribute("user", user);
			if(method != null && method.equals("view")){
				return "jsp/userview";
			} else /*if(method != null && method.equals("modify")) */{
				return "jsp/usermodify";
			}
		} else {
			model.addAttribute("info", "用户编号为空，请重试！");
			return "failure";
		}
	}
	
	@RequestMapping(value="/deluser", produces="application/json;charset=utf-8")
	@ResponseBody
	public String delUser(String uid, Model model) {
		Integer delId = 0;
		try{
			delId = Integer.parseInt(uid);
		}catch (Exception e) {
			delId = 0;
		}
		HashMap<String, String> resultMap = new HashMap<String, String>();
		if(delId <= 0){
			resultMap.put("delResult", "notexist");
		}else{
			try {
				if(userService.deleteUserById(delId)){
					Timer timer = new Timer();
					timer.schedule(new TimerTask() {
						@Override
						public void run() {
							try {
								String preTimeStamp = redisService.get(Constants.USER_LIST + ":ing");
								List<User> userList = userService.getUserList(null,0,0,0);
								String newTimeStamp = String.valueOf(System.currentTimeMillis());
								if (redisService.hsetInPage(Constants.USER_LIST + ":" + newTimeStamp,userList)) {
									redisService.set(Constants.USER_LIST + ":ing", newTimeStamp, 2 * 3600l);
									redisService.expire(Constants.USER_LIST + ":" + newTimeStamp, 2 * 3600l);
									redisService.expire(Constants.USER_LIST + ":" + preTimeStamp, 5l);
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}, 0);
					resultMap.put("delResult", "true");
				}else{
					resultMap.put("delResult", "false");
				}
			} catch (Exception e) {
				e.printStackTrace();
				resultMap.put("delResult", "failed");
				return JSON.toJSONString(resultMap);
			}
		}
		//把resultMap转换成json对象输出
		return JSON.toJSONString(resultMap);
	}
	
	@RequestMapping(value="/ucexist", produces="application/json;charset=utf-8")
	@ResponseBody
	public String userCodeExist(String userCode) {
		//判断用户账号是否可用
		HashMap<String, String> resultMap = new HashMap<String, String>();
		if(StringUtils.isNullOrEmpty(userCode)){
			//userCode == null || userCode.equals("")
			resultMap.put("userCode", "exist");
		}else{
			User user = null;
			try {
				user = userService.selectUserCodeExist(userCode);
			} catch (Exception e) {
				e.printStackTrace();
				resultMap.put("userCode", "failed");
				return JSON.toJSONString(resultMap);
			}
			if(null != user){
				resultMap.put("userCode","exist");
			}else{
				resultMap.put("userCode", "noexist");
			}
		}
		//把resultMap转为json字符串以json的形式输出
		//把resultMap转为json字符串 输出
		return JSON.toJSONString(resultMap);
	}
	
	@RequestMapping(value="/getrolelist", produces="application/json;charset=utf-8")
	@ResponseBody
	public String getRoleList() {
		String roleJsonList = null; // 接收角色列表的字符串
		try {
			roleJsonList = redisService.get(Constants.ROLE_LIST);
			if (roleJsonList == null || "".equals(roleJsonList)) {
				roleJsonList = JSON.toJSONString(roleService.getRoleList());
				redisService.set(Constants.ROLE_LIST, roleJsonList ,5*3600L);
			}
		} catch (Exception e) {
			e.printStackTrace();
//			return "failed"; //原来的js文件素材有处理方法了
		}
		
//		try {
//			roleList = roleService.getRoleList();
//		} catch (Exception e) {
//			e.printStackTrace();
//			return "failed";
//		}
		//把roleList转换成json对象输出
		return roleJsonList;
	}
	
	@RequestMapping("/query")
	public String query(Model model, @RequestParam(value="queryname",required=false) String queryUserName, 
						@RequestParam(value="queryUserRole",required=false)String temp, 
						@RequestParam(value="pageIndex",required=false)String pageIndex) {
		//查询用户列表
		int queryUserRole = 0;
		List<User> userList = null;
		//设置页面容量
    	int pageSize = Constants.pageSize;
    	//当前页码
    	int currentPageNo = 1;
		/**
		 * http://localhost:8090/SMBMS/userlist.do
		 * ----queryUserName --NULL
		 * http://localhost:8090/SMBMS/userlist.do?queryname=
		 * --queryUserName ---""
		 */
		System.out.println("queryUserName servlet--------"+queryUserName);  
		System.out.println("queryUserRole servlet--------"+queryUserRole);  
		System.out.println("query pageIndex--------- > " + pageIndex);
		if(queryUserName == null){
			queryUserName = "";
		}
		if(temp != null && !temp.equals("")){
			queryUserRole = Integer.parseInt(temp);
		}
		
    	if(pageIndex != null){
    		try{
    			currentPageNo = Integer.valueOf(pageIndex);
    		}catch(NumberFormatException e){
    			model.addAttribute("info", e.getMessage());
				return "failure";
    		}
    	}	
    	//总数量（表）	
    	int totalCount = 0;
		try {
			totalCount = userService.getUserCount(queryUserName,queryUserRole);
		} catch (Exception e) {
			e.printStackTrace();
			model.addAttribute("info", e.getMessage());
			return "failure";
		}
    	//总页数
    	PageSupport pages=new PageSupport();
    	pages.setCurrentPageNo(currentPageNo);
    	pages.setPageSize(pageSize);
    	pages.setTotalCount(totalCount);
    	
    	int totalPageCount = pages.getTotalPageCount();
    	
    	//控制首页和尾页
    	if(currentPageNo < 1){
    		currentPageNo = 1;
    	}else if(currentPageNo > totalPageCount){
    		currentPageNo = totalPageCount;
    	}
		try {
//			String timerStamp = redisService.get(Constants.USER_LIST + ":ing");
//			logger.info("使用redis的userList=================================");
//			userList = (List<User>) redisService.hget(Constants.USER_LIST + ":" + timerStamp, String.valueOf(currentPageNo));
			userList = userService.getUserList(queryUserName,queryUserRole,currentPageNo, pageSize);
		} catch (Exception e) {
			e.printStackTrace();
			model.addAttribute("info", e.getMessage());
			return "failure";
		}
		model.addAttribute("userList", userList);
		
		List<Role> roleList = null;
		String roleJsonList = null; // 接收角色列表的字符串
		try {
			roleJsonList = redisService.get(Constants.ROLE_LIST);
			if (roleJsonList != null && !"".equals(roleJsonList)) {
				roleList = JSON.parseArray(roleJsonList, Role.class);
			} else {
				roleList = roleService.getRoleList();
				redisService.set(Constants.ROLE_LIST, JSON.toJSONString(roleList), 5*3600L);
			}
		} catch (Exception e) {
			e.printStackTrace();
			model.addAttribute("info", e.getMessage());
			return "failure";
		}
		model.addAttribute("roleList", roleList);
		model.addAttribute("queryUserName", queryUserName);
		model.addAttribute("queryUserRole", queryUserRole);
		model.addAttribute("totalPageCount", totalPageCount);
		model.addAttribute("totalCount", totalCount);
		model.addAttribute("currentPageNo", currentPageNo);
		return "jsp/userlist";
	}
	
	@RequestMapping("/add")
	public String add(User user, Model model,HttpServletRequest request) {
		System.out.println("add()================");
		user.setCreationDate(new Date());
		user.setCreatedBy(((User)request.getSession().getAttribute(Constants.USER_SESSION)).getId());
		
		try {
			if(userService.add(user)){
				return "redirect:/user/query";
			}else{
				return "jsp/useradd";
			}
		} catch (Exception e) {
			e.printStackTrace();
			model.addAttribute("info", e.getMessage());
			return "failure";
		}
	}
	
	/**
	 * 跳转页面的公共方法1
	 * @return
	 */
	@RequestMapping("/forwardtojsp1")
	public String forwardtojsp1(String url) {
		return "jsp/" + url;
	}
	
	/**
	 * 跳转页面的公共方法2
	 * @return
	 */
	@RequestMapping("/forwardtojsp2")
	public String login(String url){
		return url;
	}
	
	/**
	 * 书本的json测试========
	 * @param id
	 * @return
	 */
	@RequestMapping(value="/testjson", produces="application/json;charset=utf-8")
	@ResponseBody // 用了response的话，这个标注等于没用。这个注解和上面的produces都是针对返回值的
	public void view(String id, HttpServletResponse response) {
		String cjson = "";
		response.setCharacterEncoding("utf-8");
		PrintWriter out = null;
		try {
			out = response.getWriter();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if (null == id || "".equals(id)) {
			out.print("nodata");
		} else {
			try {
				User user = userService.getUserById(id);
				cjson = JSON.toJSONString(user);
			} catch (Exception e) {
				e.printStackTrace();
				out.print("failed");
			}
			out.print(cjson);
			out.flush();
			out.close();
		}
	}
}
