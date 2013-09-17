package psl.cl.function;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.BrowserFunction;
import org.eclipse.swt.widgets.Display;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import psl.cl.util.HttpRequestHelper;

public class DownloadFunction extends BrowserFunction{
	private static final String DEFAULT_TORRENT_PATH = "D:" + File.separator + "Torrent";
	//下载列表
	private ArrayList<String> downloadList;
	//配置文件
	private Properties properties;
	
	public DownloadFunction(Browser browser, String name, Properties properties) {
		super(browser, name);

		this.downloadList = new ArrayList<String>();
		this.properties = properties; 
	}

	@Override
	public Object function(final Object[] arguments) {
		//传入参数不正确，直接返回null
		if(!(arguments[0] instanceof String)){
			return null;
		}
		
		final String url = (String)arguments[0];
//		final String title = (String)arguments[1];
		//传入的参数不匹配，直接返回null;
		if(!url.startsWith("http://cl.cn.mu/htm_data")){
			return null;
		}
		
		if (downloadList.contains(url)) return "Repeat download.";
		
		//加入下载列表，因为多线程操作，存在线程安全性问题待解决
		downloadList.add(url);
		/*开启一个下载线程*/
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Document doc = Jsoup.connect(url).get();
					Elements links = doc.select("a");
					String linkUrl = "";
					for(Element link:links){
						if(link.text().startsWith("http://www.rmdown.com/link.php")){
							linkUrl = link.text();
							break;
						}
					}
					
					if(!"".equals(linkUrl)){
						//下载链接找到，开始下载
						doc = Jsoup.connect(linkUrl).get();
						String ref = doc.select("input[name=ref]").val();
						String reff = doc.select("input[name=reff]").val();
						String submit = doc.select("input[name=submit]").val();
						
						if("".equals(ref) || "".equals(reff) || "".equals(submit)){
							throw new IOException("下载页面解析错误");
						}
						
						URL downloadUrl = new URL("http://www.rmdown.com/download.php");
						HttpURLConnection conn = (HttpURLConnection) downloadUrl.openConnection();
						//设置请求参数
						String split = HttpRequestHelper.generateSplitString();
						conn.setRequestMethod("POST");
						conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + split);
						conn.setDoOutput(true);
						Map<String, String> form = new TreeMap<String, String>();
						form.put("ref", ref);
						form.put("reff", reff);
						form.put("submit", submit);
						String requestPayLoad = HttpRequestHelper.formatFormData(form, "multipart/form-data", split);
						OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream(), "utf-8");
						writer.write(requestPayLoad);
						writer.close();
						
						String fileName = (String) arguments[1];
						fileName = fileName.replaceAll("[/\\|:>\\?\\*<]", "");
						
						InputStream is = null;
						DataOutputStream dos = null;
						try{
							String torrentPath = properties.getProperty("TorrentPath", DEFAULT_TORRENT_PATH); 
							is = conn.getInputStream();
							dos = new DataOutputStream(new FileOutputStream(
									torrentPath + File.separator + fileName + ".torrent"));
							byte[] data = new byte[1024];
							int count;
							while((count = is.read(data)) != -1){
								dos.write(data, 0, count);
							}
						}finally {
							if(is != null){
								try{
									is.close();
								}finally{
									if(dos != null)	dos.close();
								}
							}
							
						}
						
						//更新过程
						Display display = Display.getDefault();
						if(!display.isDisposed()){
							display.syncExec(new Runnable() {
								@Override
								public void run() {
									getBrowser().execute("$(\"a[href='" + url + "']\").parents('tr').find('a.downloading')" +
											".attr('class', 'downloaded').text('完成')");
								}
							});
						}
					}else{
						//下载链接未找到，提示尝试手动
						Display display = Display.getDefault();
						if(!display.isDisposed()){
							display.syncExec(new Runnable() {
								@Override
								public void run() {
									getBrowser().execute("$(\"a[href='" + url + "']\").parents('tr').find('a.downloading')" +
											".attr('class', 'failure').text('失败')");
								}
							});
						}
					}
					
				} catch (IOException e) {
					//连接错误
					System.out.println(e.getMessage());
					
					Display display = Display.getDefault();
					if(!display.isDisposed()){
						display.syncExec(new Runnable() {
							@Override
							public void run() {
								getBrowser().execute("$(\"a[href='" + url + "']\").parents('tr').find('a.downloading')" +
										".attr('class', 'download').text('重试').click(dlClick)");
							}
						});
					}
					
				} finally {
					downloadList.remove(url);
				}
			}
		}).start();
		
		return "SUCCESS";
	}
	
}
