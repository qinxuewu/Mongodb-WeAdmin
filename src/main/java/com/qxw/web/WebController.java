package com.qxw.web;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.model.Sorts;
import com.qxw.mongodb.MongoSdkBase;
import com.qxw.utils.ByteConvKbUtils;
import com.qxw.utils.JsonFormatTool;
import com.qxw.utils.Res;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

/**
 * mongodb web
 * @author qxw
 * @data 2018年11月20日下午1:03:32
 */
@Controller
@RequestMapping("mongo")
public class WebController {
	 private Logger logger = LoggerFactory.getLogger(getClass());
	@Value("${login.username}")
	private String username;
	@Value("${login.password}")
	private String password;

	@Autowired
	private MongoSdkBase mongoSdkBase;
	/**
	 * 需要过滤的表
	 */
	private final static String[] TAVLEARR = {"system.indexes"};

    
    /**
     * 模拟登录
     * @param 
     * @return
     */
    @ResponseBody
    @RequestMapping("/login")
    public Res login(String uname,String pwd,HttpServletRequest request) {

    	if(StringUtils.isEmpty(uname)||StringUtils.isEmpty(pwd)){
    		return Res.error("账号密码不能为空");
    	}
    	if(username.equals(uname)){
    			if(password.equals(pwd)){
    				request.getSession().setAttribute("username", username);
    				return Res.ok();
    			}else{
    				return Res.error("密码错误");
    			}
    	}else{
    		return Res.error("账号不存在");
    	}
    }
    /**
     * 数据源列表
     * @return
     */
    @ResponseBody
    @RequestMapping("/index")
    public Res index() {
        List<String> listNames =mongoSdkBase.getDbList();
        //系统表过滤
        listNames.remove("admin");
        listNames.remove("local");
        logger.info(listNames.toString());
        return Res.ok().put("listNames", listNames);
    }

    /**
     * 数据库对应的数据集合列表
     * @param dbName
     * @return
     */
    @ResponseBody
    @RequestMapping("/db")
    public Res db(String dbName) {

    	if(StringUtils.isEmpty(dbName)){
    		return Res.error("dbName参数不能为空");
    	}
    	if("undefined".equals(dbName)){
			return Res.error("请关闭所有的iframe后在执行F5");
		}
        MongoDatabase mogo = mongoSdkBase.getMongoDb(dbName);
        //获取所有集合的名称
        MongoIterable<String> collectionNames = mogo.listCollectionNames();
        MongoCursor<String> i = collectionNames.iterator();
        List<JSONObject> listNames = new ArrayList<JSONObject>();
        while (i.hasNext()) {
            String tableName = i.next();
			if(!Arrays.asList(TAVLEARR).contains(tableName)) {
				JSONObject t = new JSONObject();
				t.put("tableName", tableName);
				BasicDBObject obj = mongoSdkBase.getStats(dbName, tableName);
				t.put("size", ByteConvKbUtils.getPrintSize(obj.getInt("size")));
				listNames.add(t);
			}

        }
        return Res.ok().put("listNames", listNames);
    }

    /***
     * 集合下的数据列表
     * @param pageNum
     * @param pageSize
     * @param parame 查询条件 json字符串
     * @return
     */
    @ResponseBody
    @RequestMapping("/getCollection")
    public Res getCollection(@RequestParam(value = "p", defaultValue = "1") int pageNum,@RequestParam(value = "s", defaultValue = "10") int pageSize,
    		String dbName,String tableName,String parame) {
    	if(StringUtils.isEmpty(dbName)||StringUtils.isEmpty(tableName)){
    		return Res.error("dbName,tableName参数不能为空");
    	}
        BasicDBObject query = new BasicDBObject();
		BasicDBObject group = new BasicDBObject();
        if(!StringUtils.isEmpty(parame)){
        	JSONObject obj=JSONObject.parseObject(parame);
        	 Set<String> kyes=obj.keySet();
        	 kyes.forEach(key->{
        		 	if(key.equals("$group")){
						group.put(key,obj.get(key));
					}else {
						query.put(key, obj.get(key));
					}
        	  });
        }

        MongoCollection<Document> table = mongoSdkBase.getColl(dbName,tableName);
		JSONObject data=null;
		if(group.size()==0){
			data=mongoSdkBase.getPage(table, query, Sorts.descending("_id"), pageNum, pageSize);
		}else{
			data=mongoSdkBase.getGroupPage(table,query,group,Sorts.descending("_id"), pageNum, pageSize);
		}

        //获取集合的所有key
        Document obj = mongoSdkBase.getColl(dbName,tableName).find().first();
        Map<String, Object> m = new HashMap<String, Object>(16);
        m.put("data", data);
        if(obj!=null) {
        	m.put("keys", obj.keySet());
        }
        return Res.ok(m);
    }     
   
    /**
     * 删除集合
     * @param dbName
     * @param tableName
     * @param id  主键
     * @return
     */
    @ResponseBody
    @RequestMapping("/deleteCollection")
    public Res deleteCollection(String dbName,String tableName, String id){
    	if(StringUtils.isEmpty(dbName)||StringUtils.isEmpty(tableName)||StringUtils.isEmpty(id)){
    		return Res.error("dbName,tableName,id,参数不能为空");
    	}
    	int count=mongoSdkBase.deleteOne(mongoSdkBase.getColl(dbName,tableName),id);
        return count>0?Res.ok():Res.error("删除失败");
    }

    /**
     * 更新集合
     * @param tableName
     * @param parame  json字符串
     * @return
     */
    @ResponseBody
    @RequestMapping("/updateCollection")
    public Res updateCollection(String dbName,String tableName, String parame){
    	if(StringUtils.isEmpty(dbName)||StringUtils.isEmpty(tableName)||StringUtils.isEmpty(parame)){
    		return Res.error("dbName,tableName,parame,参数不能为空");
    	}
    	JSONObject info=JSONObject.parseObject(parame);
    	String id=info.getString("_id");
    	boolean falg=mongoSdkBase.updateOne(mongoSdkBase.getColl(dbName,tableName), id, info);
        return falg==true?Res.ok():Res.error("更新失败");
    }
    
    /**
     * 添加集合
     * @param dbName
     * @param tableName
     * @param parame  json字符串
     * @return
     */
    @ResponseBody
    @RequestMapping("/saveCollection")
    public Res saveCollection(String dbName,String tableName, String parame){
    	if(StringUtils.isEmpty(dbName)||StringUtils.isEmpty(tableName)||StringUtils.isEmpty(parame)){
    		return Res.error("dbName,tableName,parame,参数不能为空");
    	}
    	JSONObject info=JSONObject.parseObject(parame);
    	String id=mongoSdkBase.insertOne(mongoSdkBase.getColl(dbName,tableName), info);
        return StringUtils.isEmpty(id)?Res.error("添加失败"):Res.ok();
    }
    
 
    /**
     * 根据ID查询集合
     * @param dbName
     * @param tableName
     * @param id
     * @return
     */
    @ResponseBody
    @RequestMapping("/findOne")
    public Res findOne(String dbName,String tableName, String id){
    	if(StringUtils.isEmpty(dbName)||StringUtils.isEmpty(tableName)||StringUtils.isEmpty(id)){
    		return Res.error("dbName,tableName,id,参数不能为空");
    	}
    	String result=mongoSdkBase.seleteOne(mongoSdkBase.getColl(dbName,tableName), id);
    
        return Res.ok().put("data", JSONObject.parseObject(result));
    }
    
  
    /**
 	 * 导出清单
 	 * @param request
 	 * @param response
 	 */
	@RequestMapping("/exportList")
 	public  void exportList(HttpServletRequest request,HttpServletResponse response,String dbName,String tableName,String parame) {
		// 读取字符编码
		String csvEncoding = "UTF-8";
				// 设置响应
		response.setCharacterEncoding(csvEncoding);
	    response.setContentType("application/json; charset=" + csvEncoding);
		response.setHeader("Pragma", "public");
		response.setHeader("Cache-Control", "max-age=30");
		OutputStream os=null;
		try {
			 BasicDBObject query = new BasicDBObject();
		      if(!StringUtils.isEmpty(parame)){
		        	JSONObject obj=JSONObject.parseObject(parame);
		        	 Set<String> kyes=obj.keySet();
		        	  kyes.forEach(key->{
		        		    query.put(key,obj.get(key));
		        	  });
		      }
		    List<JSONObject> list=mongoSdkBase.getAll(mongoSdkBase.getColl(dbName, tableName), query, Sorts.descending("_id"));
		
			response.setHeader("Content-Disposition", "attachment; filename=\"" + tableName+".json" + "\"");			 
			// 写出响应
			os=response.getOutputStream();
			
			os.write(JsonFormatTool.formatJson(list.toString()).getBytes("GBK"));
			os.flush();
			
		} catch (Exception e) {
			logger.error("导出异常：tableName:{},{}",tableName,e);
			e.printStackTrace();
		}finally{
			if(os!=null){
				try {
					os.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
	}



}
