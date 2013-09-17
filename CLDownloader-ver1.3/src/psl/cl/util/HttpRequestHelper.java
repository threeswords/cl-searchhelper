package psl.cl.util;

import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Http的POST请求辅助类
 * */
public class HttpRequestHelper {
	public static final String CHARACTERS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

	/**
	 * 格式化表单数据，根据encType将表单数据转换成指定的字符串
	 * @param values 表单数据，name-value格式，不能为null
	 * @param encType 表单数据编码格式，必须是以下值之一：
	 * @param split 如果encType为multipart/form-data，则必须指定此值用于分隔参数，一串长随机字符串
	 * <ul>
	 * 	<li>application/x-www-form-urlencoded:在发送前编码所有字符（默认）</li>
	 * 	<li>multipart/form-data:不对字符编码，在使用包含文件上传控件的表单时，必须使用该值</li>
	 * 	<li>text/plain:空格转换为 "+" 加号，但不对特殊字符编码</li>
	 * </ul>
	 * 	
	 * @return 返回指定编码格式后的字符串	，如果encType有误或者为null，则按照默认encType编码，如发生错误
	 * 则返回空字符串，not null.											 
	 * */
	public static String formatFormData(Map<String, String> values, String encType, String split){
		StringBuffer rqStr = new StringBuffer();
		if("multipart/form-data".equalsIgnoreCase(encType)){
			//构建请求正文字符串，格式为分隔字符串-参数描述-空行-参数值，注意为\r\n
			Set<Map.Entry<String, String>> entrySet = values.entrySet();
			Iterator<Map.Entry<String, String>> iter = entrySet.iterator();
			while(iter.hasNext()){
				Map.Entry<String, String> entry = iter.next();
				rqStr.append("--" + split + "\r\n");
				rqStr.append("Content-Disposition:form-data;name=\"" + entry.getKey() + "\"\r\n");
				rqStr.append("\r\n");
				rqStr.append(entry.getValue() + "\r\n");
			}
			rqStr.append("--" + split + "--");
		}else if("text/plain".equalsIgnoreCase(encType)){
			
		}else{
			//默认格式
		}
		return rqStr.toString();
	}
	
	/**
	 * 发送POST请求表单
	 * @param action 请求地址，必须为绝对地址
	 * @param values 表单数据，name-value格式
	 * @param encType 表单数据编码格式
	 * @see #formatRequestForm(Map, String)
	 * */
	public static InputStream postForm(String action, Map<String, String> values, String encType){
		return null;
	}
	
	/**
	 * 生成一个随机分隔字符串，包含IE特有标识:---------------------------7d
	 * */
	public static String generateSplitString(){
		StringBuffer splitStr = new StringBuffer();
		splitStr.append("-------------------------7d");		//起始包含字段，该字段为IE特有标示
		Random random = new Random();
		for(int i = 0; i < 12; i++){
			int position = random.nextInt(CHARACTERS.length());
			splitStr.append(CHARACTERS.charAt(position));
		}
		
		return splitStr.toString();
	}
	
}
