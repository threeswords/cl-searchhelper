package psl.cl.model;

import java.io.IOException;
import java.io.InputStream;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class HTMLFile {
	//HTML文档模型
	private Document doc;
	//HTML预设表格
	private Element table;
	//总行数
	private int size;
	
	/**
	 * 创建一个指定编码的HTML文档
	 * @param charset 文档字符集
	 * */
	public HTMLFile(String charset){
		//初始化doc对象
		InputStream is = getClass().getResourceAsStream("/res/result.html");
		
		try {
			doc = Jsoup.parse(is, "utf-8", "http://cl.cn.mu");
		} catch (IOException e) {
			System.out.println("创建结果HTML文档模型失败：" + e.getMessage());
		} finally{
			if(is != null){
				try {
					is.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		table = doc.select("#vtable").first();
	}
	
	/**
	 * 同步方法，在预设表格中新增一行，加入数据
	 * */
	public synchronized void insert(String...values){
		if(values.length > 1){
			Element row = table.appendElement("tr").addClass(values[0]);
			for(int i = 1; i < values.length; i++){
				Element cell = row.appendElement("td");
				if(i == (values.length - 1)) cell.attr("style", "width:100px");			//最后一个单元格固定宽度
				cell.append(values[i]);
			}
			size++;
		}
	}
	
	/**
	 * 返回当前表格数据行数
	 * */
	public int size(){
		return size;
	}
	
	/**
	 * 隐藏字段，存储页面数量
	 * */
	public void setFinishedPages(int pages){
		doc.getElementById("pages").val(String.valueOf(pages));
	}
	
	/**
	 * 输出该html文件
	 * */
	public String html(){
		return doc.html();
	}
	
}
