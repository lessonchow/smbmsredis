package cn.smbms.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.mysql.jdbc.StringUtils;

import cn.smbms.pojo.Provider;
import cn.smbms.pojo.User;
import cn.smbms.service.RedisService;
import cn.smbms.service.provider.ProviderService;
import cn.smbms.tools.Constants;

@Controller
@RequestMapping("/provider")
public class ProviderController {

	@Resource
	private ProviderService providerService;
	@Autowired
	private RedisService redisService;
	
//		String method = request.getParameter("method");
//		if(method != null && method.equals("query")){
//			this.query(request,response);
//		}else if(method != null && method.equals("add")){
//			this.add(request,response);
//		}else if(method != null && method.equals("view")){
//			this.getProviderById(request,response,"providerview.jsp");
//		}else if(method != null && method.equals("modify")){
//			this.getProviderById(request,response,"providermodify.jsp");
//		}else if(method != null && method.equals("modifysave")){
//			this.modify(request,response);
//		}else if(method != null && method.equals("delprovider")){
//			this.delProvider(request,response);
//		}

	@RequestMapping(value="/delprovider", produces="application/json;charset=utf-8")
	@ResponseBody
	public String delProvider(String proid,HttpServletRequest request, HttpServletResponse response) {
		HashMap<String, String> resultMap = new HashMap<String, String>();
		if(!StringUtils.isNullOrEmpty(proid)){
			int flag = 0;
			try {
				flag = providerService.deleteProviderById(proid);
			} catch (Exception e) {
				e.printStackTrace();
				resultMap.put("delResult", "failed");
				return JSONArray.toJSONString(resultMap);
			}
			if(flag == 0){//删除成功
				Timer timer = new Timer();
				timer.schedule(new TimerTask() {

					@Override
					public void run() {
						String providerJsonList = null; // 接收订单列表的字符串
						try {
							providerJsonList = JSON.toJSONString(providerService.getProviderList("", ""));
							redisService.set(Constants.PROVIDER_LIST,providerJsonList ,1800L);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}, 0);
				resultMap.put("delResult", "true");
			}else if(flag == -1){//删除失败
				resultMap.put("delResult", "false");
			}else if(flag > 0){//该供应商下有订单，不能删除，返回订单数
				resultMap.put("delResult", String.valueOf(flag));
			}
		}else{
			resultMap.put("delResult", "notexit");
		}
		//把resultMap转换成json对象输出
		return JSON.toJSONString(resultMap);
	}
	
	@RequestMapping("/modify")
	public String modify(Provider provider, Model model, HttpServletRequest request) {
		
		provider.setModifyBy(((User)request.getSession().getAttribute(Constants.USER_SESSION)).getId());
		provider.setModifyDate(new Date());
		boolean flag = false;
		try {
			flag = providerService.modify(provider);
		} catch (Exception e) {
			e.printStackTrace();
			model.addAttribute("info", e.getMessage());
			return "failure";
		}
		if(flag){
			String providerJsonList = null; // 接收订单列表的字符串
			try {
				providerJsonList = JSON.toJSONString(providerService.getProviderList("", ""));
				redisService.set(Constants.PROVIDER_LIST,providerJsonList ,1800L);
			} catch (Exception e) {
				e.printStackTrace();
				request.setAttribute("info", "修改成功，但是加载信息出错，错误信息如下：" + e.getMessage());
				return "failure";
			}
			return "redirect:/provider/query";
		}else{
			return "jsp/providermodify";
		}
	}
	
	@RequestMapping("/getproviderbyid")
	public String getProviderById(String proid, String method, Model model) {
		if(!StringUtils.isNullOrEmpty(proid)){
			Provider provider = null;
			try {
				provider = providerService.getProviderById(proid);
			} catch (Exception e) {
				e.printStackTrace();
				model.addAttribute("info", e.getMessage());
				return "failure";
			}
			model.addAttribute("provider", provider);
			if(method != null && method.equals("view")){
				return "jsp/providerview";
			}else {
				return "jsp/providermodify";
			}
		} else {
			model.addAttribute("info", "供应商编号为空，请重试！");
			return "failure";
		}
	}
	
	@RequestMapping("/add")
	public String add(Provider provider, HttpServletRequest request) {
		provider.setCreatedBy(((User)request.getSession().getAttribute(Constants.USER_SESSION)).getId());
		provider.setCreationDate(new Date());
		boolean flag = false;
		try {
			flag = providerService.add(provider);
		} catch (Exception e) {
			e.printStackTrace();
			request.setAttribute("info", e.getMessage());
			return "failure";
		}
		if(flag){
			String providerJsonList = null; // 接收订单列表的字符串
			try {
				providerJsonList = JSON.toJSONString(providerService.getProviderList("", ""));
				redisService.set(Constants.PROVIDER_LIST,providerJsonList ,1800L);
			} catch (Exception e) {
				e.printStackTrace();
				request.setAttribute("info", "添加成功，但是加载信息出错，错误信息如下：" + e.getMessage());
				return "failure";
			}
			return "redirect:/provider/query";
		}else{
			return "provideradd";
		}
	}
	
	@RequestMapping("/query")
	public String query(String queryProName, String queryProCode, Model model) {
		if(StringUtils.isNullOrEmpty(queryProName)){
			queryProName = "";
		}
		if(StringUtils.isNullOrEmpty(queryProCode)){
			queryProCode = "";
		}
		List<Provider> providerList = new ArrayList<Provider>();
		String providerJsonList = null; // 接收订单列表的字符串
		try {
			if ("".equals(queryProName) && "".equals(queryProCode)) {
				providerJsonList = redisService.get(Constants.PROVIDER_LIST);
				if (providerJsonList != null || !"".equals(providerJsonList)) {
					providerList = JSON.parseArray(providerJsonList, Provider.class);
				} else {
					providerList = providerService.getProviderList(queryProName,queryProCode);
					redisService.set(Constants.PROVIDER_LIST,providerJsonList ,1800L);
				}
			} else {
				providerList = providerService.getProviderList(queryProName,queryProCode);
			}
		} catch (Exception e) {
			e.printStackTrace();
			model.addAttribute("info", e.getMessage());
			return "failure";
		}
		model.addAttribute("providerList", providerList);
		model.addAttribute("queryProName", queryProName);
		model.addAttribute("queryProCode", queryProCode);
		return "jsp/providerlist";
	}
	
	/**
	 * 跳转页面的公共方法1
	 * @return
	 */
	@RequestMapping("/forwardtojsp1")
	public String forwardtojsp1(String url) {
		return "jsp/" + url;
	}
}
