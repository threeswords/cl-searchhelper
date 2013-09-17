package psl.cl.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

/**
 * 任务描述类，描述了待处理任务，包含当前待处理页码，总页数，开始页码
 * */
public class TaskOverview {
	private static final String DEFAULT_CHARSET = "UTF-8";
	//配置文件
	private Properties properties;
	//网页文件模型
	private HTMLFile htmlFile;
	//任务总页数
	private int pageCount;
	//当前任务页码
	private int currentPage;
	//开始页码
	private int startPage;
	//任务的BaseUrl
	private String baseUrl;
	
	/**
	 * 构造函数
	 * @param properties 程序配置参数
	 * @param baseUrl 待抓取网站的BaseURL
	 * */
	public TaskOverview(Properties properties, String baseUrl){
		this.properties = properties;
		this.baseUrl = baseUrl;
		
		this.startPage = Integer.parseInt(properties.getProperty("StartPage", "1"));
		this.pageCount = Integer.parseInt(properties.getProperty("PageCount", "0"));
		//初始化网页文件模型
		this.htmlFile = new HTMLFile(DEFAULT_CHARSET);
		//初始状态，待处理页码为开始页码
		this.currentPage = this.startPage;
	}
	
	/**
	 * 获取当前任务处理的URL
	 * */
	public String getBaseUrl(){
		return this.baseUrl;
	}
	
	/**
	 * 获取当前配置文件
	 * */
	public Properties getProperties(){
		return this.properties;
	}
	
	/**
	 * 返回当前任务抓取的版块id
	 * */
	public int fid(){
		return Integer.parseInt(properties.getProperty("fid", "15"));
	}
	
	/**
	 * 设置总页数
	 * @param pageCount 总页数(0-100)
	 * */
	public void setPageCount(int pageCount){
		if(pageCount > 100){
			//匿名用户只允许访问前100页
			this.pageCount = 100;
			return;
		}else if(pageCount < 0){
			this.pageCount = 0;
			return;
		}
		this.pageCount = pageCount;
	}
	
	/**
	 * 返回当前任务总页数
	 * */
	public int getPageCount(){
		return this.pageCount;
	}
	
	
	/**
	 * 获取当前待处理的页面页码，该方法为同步方法(synchronized)
	 * @return 返回当前待处理页面的页码，如果所有页面均已处理完毕，则返回0
	 * */
	public synchronized int currentPage(){
		if(currentPage > 0 && currentPage <= pageCount){
			//仍有待处理页，返回当前待处理页，并且待处理页递增
			return currentPage++;
		}
		return 0;
	}
	
	/**
	 * 获取一个HTMLFile用以保存当前任务产生的数据
	 * @return 返回一个代表HTML文档的文件模型对象
	 * */
	public HTMLFile getHTMLFile(){
		return this.htmlFile;
	}
	
	/**
	 * 返回抓取完成的页数
	 * */
	public int finishedPages(){
		return currentPage - 1;
	}
	
	/**
	 * 固化任务结果，生成文件
	 * @param fileName 生成的文件名称
	 * */
	public void persist(String fileName) throws FileNotFoundException, UnsupportedEncodingException{
		String realName = "";
		if(fileName.endsWith(".html") || fileName.endsWith(".htm")){
			realName = fileName;
		}else{
			realName = fileName + ".html";
		}
		PrintWriter writer = new PrintWriter(new File(realName), DEFAULT_CHARSET);
		writer.print(htmlFile.html());
		writer.close();
	}
	
}
