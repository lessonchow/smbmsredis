package cn.smbms.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.mysql.jdbc.StringUtils;

import cn.smbms.pojo.Bill;
import cn.smbms.pojo.Provider;
import cn.smbms.pojo.User;
import cn.smbms.service.RedisService;
import cn.smbms.service.bill.BillService;
import cn.smbms.service.provider.ProviderService;
import cn.smbms.tools.Constants;

@Controller
@RequestMapping("/bill")
public class BillController{

	@Resource
	private BillService billService;
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
//			this.getBillById(request,response,"billview.jsp");
//		}else if(method != null && method.equals("modify")){
//			this.getBillById(request,response,"billmodify.jsp");
//		}else if(method != null && method.equals("modifysave")){
//			this.modify(request,response);
//		}else if(method != null && method.equals("delbill")){
//			this.delBill(request,response);
//		}else if(method != null && method.equals("getproviderlist")){
//			this.getProviderlist(request,response);
//		}
		
	
	@RequestMapping(value="/getproviderlist", produces="application/json;charset=utf-8")
	@ResponseBody
	public Object getProviderlist() throws ServletException, IOException {
		
		System.out.println("getproviderlist ========================= ");
		String providerJsonList = null; // 接收供应商列表的字符串
		try {
			providerJsonList = redisService.get(Constants.PROVIDER_LIST);
			if (providerJsonList == null || "".equals(providerJsonList)) {
				providerJsonList = JSON.toJSONString(providerService.getProviderList("",""));
				redisService.set(Constants.PROVIDER_LIST, providerJsonList ,1800L);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
//		List<Provider> providerList = new ArrayList<Provider>();
//		try {
//			providerList = providerService.getProviderList("","");
//		} catch (Exception e) {
//			e.printStackTrace();
//			return "failed";
//		}
		//把providerList转换成json对象输出到billmodify.js的AJAX
		return providerJsonList;
	}
	
	@RequestMapping(value="/getbillbyid")
	public String getBillById(Model model, String billid, String method) {
		if(!StringUtils.isNullOrEmpty(billid)){
			Bill bill = null;
			try {
				bill = billService.getBillById(billid);
			} catch (Exception e) {
				e.printStackTrace();
				model.addAttribute("info", e.getMessage());
				return "failure";
			}
			model.addAttribute("bill", bill);
			if(method != null && method.equals("view")){
				return "jsp/billview";
			} else {
				return "jsp/billmodify";
			}
		} else {
			model.addAttribute("info", "订单编号为空，请重试！");
			return "failure";
		}
	}
	
	@RequestMapping(value="/modify")
	public String modify(@ModelAttribute Bill bill, HttpServletRequest request, HttpServletResponse response) {
		System.out.println("modify===============");
		bill.setModifyBy(((User)request.getSession().getAttribute(Constants.USER_SESSION)).getId());
		bill.setModifyDate(new Date());
		boolean flag = false;
		try {
			flag = billService.modify(bill);
		} catch (Exception e) {
			e.printStackTrace();
			request.setAttribute("info", e.getMessage());
			return "failure";
		}
		if(flag){
			String billJsonList = null; // 接收订单列表的字符串
			try {
				billJsonList = JSON.toJSONString(billService.getBillList(new Bill()));
				redisService.set(Constants.BILL_LIST, billJsonList,1800L);
			} catch (Exception e) {
				e.printStackTrace();
				request.setAttribute("info", "修改成功，但是加载信息出错，错误信息如下：" + e.getMessage());
				return "failure";
			}
			return "redirect:/bill/query";
		}else{
			return "jsp/billmodify";
		}
	}
	
	@RequestMapping(value="/delbill", produces="application/json;charset=utf-8")
	@ResponseBody
	public Object delBill(String billid, HttpServletRequest request, HttpServletResponse response) {
		HashMap<String, String> resultMap = new HashMap<String, String>();
		if(!StringUtils.isNullOrEmpty(billid)){
			boolean flag = false;
			try {
				flag = billService.deleteBillById(billid);
			} catch (Exception e) {
				e.printStackTrace();
				resultMap.put("delResult", "failed");
				return JSON.toJSONString(resultMap);
			}
			if(flag){//删除成功
				Timer timer = new Timer();
				timer.schedule(new TimerTask() {
					
					@Override
					public void run() {
						String billJsonList = null; // 接收订单列表的字符串
						try {
							billJsonList = JSON.toJSONString(billService.getBillList(new Bill()));
							redisService.set(Constants.BILL_LIST, billJsonList,1800L);
						} catch (Exception e) {
							e.printStackTrace();
//							request.setAttribute("info", "删除成功，但是加载信息出错，错误信息如下：" + e.getMessage());
//							return "failure";
						}
						timer.cancel();
					}
				}, 0);
				resultMap.put("delResult", "true");
			}else{//删除失败
				resultMap.put("delResult", "false");
			}
		}else{
			resultMap.put("delResult", "notexit");
		}
		//把resultMap转换成json对象输出
		return JSON.toJSONString(resultMap);
	}
	
	@RequestMapping("/add")
	public String add(@ModelAttribute Bill bill, HttpServletRequest request, HttpServletResponse response) {
		System.out.println("add===============");
		bill.setCreatedBy(((User)request.getSession().getAttribute(Constants.USER_SESSION)).getId());
		bill.setCreationDate(new Date());
		boolean flag = false;
		try {
			flag = billService.add(bill);
		} catch (Exception e) {
			e.printStackTrace();
			request.setAttribute("info", e.getMessage());
			return "failure";
		}
		System.out.println("add flag -- > " + flag);
		if(flag){
			String billJsonList = null; // 接收订单列表的字符串
			try {
				billJsonList = JSON.toJSONString(billService.getBillList(new Bill()));
				redisService.set(Constants.BILL_LIST, billJsonList,1800L);
			} catch (Exception e) {
				e.printStackTrace();
				request.setAttribute("info", "添加成功，但是加载信息出错，错误信息如下：" + e.getMessage());
				return "failure";
			}
			return "redirect:/bill/query";
		}else{
			return "jsp/billadd";
		}
	}
	
	@RequestMapping("/query")
	public String query(String queryProductName,String queryProviderId,String queryIsPayment, Model model) {
		List<Provider> providerList = new ArrayList<Provider>();
		String providerJsonList = null; // 接收供应商列表的字符串
		try {
			providerJsonList = redisService.get(Constants.PROVIDER_LIST);
			if (providerJsonList != null && !"".equals(providerJsonList)) {
				providerList = JSON.parseArray(providerJsonList, Provider.class);
			} else {
				providerList = providerService.getProviderList("","");
				redisService.set(Constants.PROVIDER_LIST, JSON.toJSONString(providerList) ,1800L);
			}
		} catch (Exception e) {
			e.printStackTrace();
			model.addAttribute("info", e.getMessage());
			return "failure";
		}
		model.addAttribute("providerList", providerList);

		List<Bill> billList = new ArrayList<Bill>();
		Bill bill = new Bill();
		if(StringUtils.isNullOrEmpty(queryProductName)){
			queryProductName = "";
		}
		bill.setProductName(queryProductName);
		
		if(StringUtils.isNullOrEmpty(queryIsPayment)){
			bill.setIsPayment(0);
		}else{
			bill.setIsPayment(Integer.parseInt(queryIsPayment));
		}
		
		if(StringUtils.isNullOrEmpty(queryProviderId)){
			bill.setProviderId(0);
		}else{
			bill.setProviderId(Integer.parseInt(queryProviderId));
		}
		
		try {
			if ("".equals(bill.getProductName()) && bill.getIsPayment() == 0 && bill.getProviderId() == 0) {
				String billJsonList = null; // 接收订单列表的字符串
				billJsonList = redisService.get(Constants.BILL_LIST);
				if (billJsonList != null && !"".equals(billJsonList)) {
					billList = JSON.parseArray(billJsonList, Bill.class);
				} else {
					billList = billService.getBillList(bill);
					redisService.set(Constants.BILL_LIST, JSON.toJSONString(billList),1800L);
				}
			} else {
				billList = billService.getBillList(bill);
			}
		} catch (Exception e) {
			e.printStackTrace();
			model.addAttribute("info", e.getMessage());
			return "failure";
		}
		model.addAttribute("billList", billList);
		model.addAttribute("queryProductName", queryProductName);
		model.addAttribute("queryProviderId", queryProviderId);
		model.addAttribute("queryIsPayment", queryIsPayment);
		return "jsp/billlist";
	}
	
	/**
	 * 跳转页面的公共方法1
	 * @return
	 */
	@RequestMapping("/forwardtojsp1")
	public String forwardtojsp1(String url) {
		return "jsp/" + url;
	}
	
	public static void main(String[] args) {
		System.out.println(new BigDecimal("23.235").setScale(2,BigDecimal.ROUND_HALF_DOWN));
	}


}
