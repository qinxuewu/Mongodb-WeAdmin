package com.qxw.utils;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 

  *　　　
  * @author Levan
  * @time 2017年11月20日上午11:25:41
  * @describe  导出csv并弹出下载框提示~
 */
public class CsvUtils {
	/** CSV文件列分隔符 */
	public static final String CSV_COLUMN_SEPARATOR = ",";

	/** CSV文件列分隔符 */
	public static final String CSV_RN = "\r\n";
	public final static Logger logger = LoggerFactory.getLogger(CsvUtils.class);
	public final static String MSIE="MSIE";
	public final static String MOZILLA="Mozilla";
	/**
	 * 数据初始化
	 * @param data 数据库查出来的数据
	 * @param displayColNames csv表头
	 * @param matchColNames  data中的key ，可以说是数据库字段了,原本为”0001”类型的数据在excel中打开会被默认改变为”1”的数据。 解决方法 :key前加"'"用于特殊处理；
	 * @param 例如 输入列名为"num"数字为 001，则传入的key值为"-num",保证输出为字符串
	 * @return
	 */
	public static String formatCsvData(List<Map<String, Object>> data,String displayColNames, String matchColNames) {
		StringBuffer buf = new StringBuffer();
		String[] displayColNamesArr = null;
		String[] matchColNamesMapArr = null;
		displayColNamesArr = displayColNames.split(",");
		matchColNamesMapArr = matchColNames.split(",");

		// 输出列头
		for (int i = 0; i < displayColNamesArr.length; i++) {
			buf.append(displayColNamesArr[i]).append(CSV_COLUMN_SEPARATOR);
		}
		buf.append(CSV_RN);
		if (null != data) {
			// 输出数据
			for (int i = 0; i < data.size(); i++) {
				for (int j = 0; j < matchColNamesMapArr.length; j++) {
					//处理list<Map>中 value=null的数据
					Object object = data.get(i).get(matchColNamesMapArr[j]);
					if(object==null){
						object = data.get(i).get(matchColNamesMapArr[j].substring(1));
					}
					if(object==null){
						buf.append(CSV_COLUMN_SEPARATOR);
					}else{
						if(matchColNamesMapArr[j].startsWith("-")){
							buf.append("\t" +object.toString()).append(CSV_COLUMN_SEPARATOR);
						}else{
							buf.append(object).append(CSV_COLUMN_SEPARATOR);
						}
					}
				}
				buf.append(CSV_RN);
			}
		}
		logger.info("csv file Initialize successfully");
		return buf.toString();
	}
	
	/**
	 * 导出
	 * @param fileName 文件名
	 * @param content 内容
	 * @param request
	 * @param response
	 * @throws IOException
	 */
 	public static void exportCsv(String fileName, String content,HttpServletRequest request, HttpServletResponse response){

		// 读取字符编码
		String csvEncoding = "UTF-8";
		// 设置响应
		response.setCharacterEncoding(csvEncoding);
		response.setContentType("text/csv; charset=" + csvEncoding);
		response.setHeader("Pragma", "public");
		response.setHeader("Cache-Control", "max-age=30");
		OutputStream os=null;
		final String userAgent = request.getHeader("USER-AGENT");
		try {
			if(StringUtils.contains(userAgent,MSIE)){
				//IE浏览器
				 fileName = URLEncoder.encode(fileName,"UTF8");
	        }else if(StringUtils.contains(userAgent,MOZILLA)){
	        	//google,火狐浏览器
	        	 fileName = new String(fileName.getBytes(), "ISO8859-1");
	        }else{
	        	//其他浏览器
	        	 fileName = URLEncoder.encode(fileName,"UTF8");
	        }
			response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");			 
			// 写出响应
			os=response.getOutputStream();
			os.write(content.getBytes("GBK"));
			os.flush();
			
		} catch (Exception e) {
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
 	
 	

 	/**
 	 * 穿件csv文件
 	 * @param filePath  目录
 	 * @param fileName  文件名
 	 * @param colNames  表头
 	 * @return
 	 */
 	public static File createFileAndColName(String filePath, String fileName,  String[] colNames){  
        File csvFile = new File(filePath, fileName);  
        PrintWriter pw = null;  
        try {             
            pw = new PrintWriter(csvFile, "GBK");         
            StringBuffer sb = new StringBuffer();  
            for(int i=0; i<colNames.length; i++){  
                if( i<colNames.length-1 ) {
                	  sb.append(colNames[i]+",");  
                } else{
                	  sb.append(colNames[i]+"\r\n");  
                }  
                  
                  
            }  
            pw.print(sb.toString());  
            pw.flush();  
            pw.close();  
            return csvFile;           
        } catch (Exception e) {  
            e.printStackTrace();  
        }                 
        return null;  
    }  
      
 	/**
 	 * 向指定的csv文件追加数据
 	 * @param csvFile
 	 * @param sb
 	 */
    public static void appendDate(File csvFile,  StringBuffer sb){         
        try {  
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(csvFile, true), "GBK"), 1024);       
            bw.write(sb.toString());  
            bw.flush();  
            bw.close();          
        } catch (Exception e) {  
            e.printStackTrace();  
        }                  
    }  
 	

 	
 	public static void main(String[] args) {
 	    String[] colNames = {"第一列","第二列","第三列","第四列"};  
        File csvFile = createFileAndColName("E:\\eclipseWork\\.metadata\\.plugins\\org.eclipse.wst.server.core\\tmp1\\wtpwebapps\\shakeExternal\\impCsv", "test.csv", colNames);  
 		StringBuffer sb=new StringBuffer();
 		sb.append("组织机构,手机号,姓名,工号");
 		sb.append("\r\n");
 		appendDate(csvFile, sb);
	}

}