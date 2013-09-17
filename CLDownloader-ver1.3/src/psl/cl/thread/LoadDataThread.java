package psl.cl.thread;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ProgressBar;
import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;
import org.jsoup.select.Selector;

import psl.cl.model.TaskOverview;

/**
 * 任务处理线程，根据任务描述类TaskOverview处理页面，当一个页面处理完毕之后更新进度条
 * */
public class LoadDataThread implements Runnable {
	//如果线程需要操作UI组件，则必须要持有Display引用
	private Display display;
	//任务描述类
	private TaskOverview task;
	//UI组件之进度条
	private ProgressBar mBar;
	//UI组件之结果面板
//	private Browser mLinks;
	
	public LoadDataThread(Display display, TaskOverview task){
		this.display =display;
		this.task = task;
		
		if(!display.isDisposed()){
			display.syncExec(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					//取得进度条组件
					Control[] children = LoadDataThread.this.display.getShells()[0].getChildren();
					for(int i = 0; i < children.length; i++){
						if(children[i] instanceof ProgressBar){
							mBar = (ProgressBar)children[i];
						}
//						else if(children[i] instanceof Browser){
//							mLinks = (Browser)children[i];
//						}
					}
				}
			});
		}
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		int currentPage = 0;
		while((currentPage = task.currentPage()) > 0 && !display.isDisposed() && !Thread.currentThread().isInterrupted()){
			//当任务完成或者窗口被关闭后，结束本线程
			System.out.println("============================正在处理第<" + currentPage + ">页 By 线程"+ Thread.currentThread().getId() + "=================================");
			do{
				URL url = null;
				InputStream is = null;
				Document doc = null;
				try {
					url = new URL(task.getBaseUrl() + "/thread0806.php?fid=" + task.fid() + "&page=" + currentPage);
					is = url.openStream();
					doc = Jsoup.parse(is, "GBK", task.getBaseUrl());
					
					traverseNodes(doc, currentPage);
					
					break;
				} catch (Exception e) {
					// TODO Auto-generated catch block
					System.out.println("获取第" + currentPage + "页记录失败，正在重新获取！" + e.getMessage());
				} finally{
					try {
						if(is != null) is.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}while(true);
					
			//更新进度条
			if(!display.isDisposed()){
				display.syncExec(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						if(!mBar.isDisposed())	mBar.setSelection(mBar.getSelection() + 1);
					}
				});
			}
		}
	}
	
	/**
	 * 遍历所有所有链接，将符合条件的链接加入结果文件
	 * */
	private void traverseNodes(Document doc, int currentPage){
		Element ajaxtable = doc.getElementById("ajaxtable");
		Elements rows = Selector.select("tr", ajaxtable);
		for(Element row: rows){
			//取得该行下所有单元格
			Elements cells = row.select("td");
			if(cells.size() >= 5){
				//5个单元格，表示该行属于内容表格行，取得第二个单元格
				Element cell = cells.get(1);
				//判断单元格是否包含"公告"，如果是，则表示该行为公告行，排除!
				if(cell.select(":contains(公告)").isEmpty()){
					//取得单元格名称和链接地址
					Element link = cell.select("a").first();
					if(link != null){
						String href = link.attr("href");
						if(href.startsWith("htm_data")){
							String title = link.text().toUpperCase();
							href = link.absUrl("href");
//							System.out.println(title + "[" + href + "]");
							String a = "<a href=\"" + href + "\" target=_blank>" + title.toUpperCase() + "</a>";
							String dl = "<a href=\"javascript:void(0);\" class=\"download\">下载</a>";
							task.getHTMLFile().insert("page" + currentPage, a, dl);

							//实时更新，最好不要开启，非常占用资源，而且很慢
							/*
							if(!display.isDisposed()){
								display.syncExec(new Runnable() {
									
									@Override
									public void run() {
										// TODO Auto-generated method stub
										StringBuffer buffer = new StringBuffer();
										buffer.append("var row = $('<tr></tr>'); \r\n");
										buffer.append("var cell = $('<td>" +	a + "</td>'); \r\n");
										buffer.append("var dl = $(\"<td><a onclick='torrent(" +
												"$(this).parent().parent().children().children().attr(\\\"href\\\")" +
												")' href='javascript:void(0);' class='download'>下载</a></td>\"); \r\n");
										buffer.append("row.append(cell); \r\n");
										buffer.append("row.append(dl); \r\n");
										buffer.append("$('#vtable').append(row); \r\n");
										buffer.append("$('tr:even').css('background-color', '#f0f0f0')");
										
										mLinks.execute(buffer.toString());
									}
								});
							}
							*/
						}
					}
				}
			}
		}
	}
	
}
