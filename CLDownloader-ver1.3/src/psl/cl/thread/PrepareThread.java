package psl.cl.thread;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import psl.cl.model.TaskOverview;

/**
 * 获取总页数线程
 * */
public class PrepareThread implements Runnable {
	//UI线程的display
	private Display display;
	//任务描述类
	private TaskOverview task;
	//退出标志
	private boolean cancel;
	//结束消息
	private String finishStr = "任务结束";
	
	public PrepareThread(Display display, TaskOverview task){
		this.display = display;
		this.task = task;
		this.cancel =false;
	}
	
	@Override
	public void run() {
		//获取任务页数参数
		int pageCount = task.getPageCount();
		if(pageCount == 0){
			//如果配置参数为0，则代表全部抓取，需要获取总页数
			while(!cancel){
				try {
					Document doc = Jsoup.connect(task.getBaseUrl() + "/thread0806.php?fid=" + task.fid()).get();
					Elements pages = doc.getElementsByClass("pages");
					
					if(!pages.isEmpty()){
						String page = pages.get(0).text();
						pageCount = Integer.valueOf(page.subSequence(page.indexOf("/") + 1, page.indexOf("t") - 1).toString());
					}else{
						System.out.println("目标网页解析错误，请检查！");
					}
					
					break;
				} catch (IllegalArgumentException e){
					System.out.println("输入的网址不正确，请检查！");
					break;
				} catch (UnknownHostException e){
					System.out.println("输入的网址不正确，请检查！");
					break;
				} catch (Exception e) {
					// 连接错误
					System.out.println(e.getClass().getName() + ":" + e.getMessage());
				} 
			}
		}
		
		//设置任务总页数(0-100)，无论是否从网络获取都进行此步骤，防止用户设置无效参数
		//如果用户设置大于100，则设置为100，如果小于0，则设置成0，否则表示参数有效，直接设置
		task.setPageCount(pageCount);
		//更新进度条最大值
		updateProgressBar(task.getPageCount());
		
		System.out.println("任务页数：" + task.getPageCount());

		//初始化任务线程
		int threadCount = Integer.parseInt(task.getProperties().getProperty("ThreadCount", "5"));
		Thread[] taskThread = new Thread[threadCount];
		LoadDataThread loadRunnable = new LoadDataThread(display, task);
		for(int i = 0; i < threadCount; i++){
			taskThread[i] = new Thread(loadRunnable);
		}
		//总页数获取完毕，如果当前线程没有中断，即代表程序没有收到退出指令，启动任务
		if(!Thread.interrupted()){
			for(int i = 0; i < taskThread.length; i++){
				taskThread[i].start();
			}
		}
		
		//循环检测任务线程状态
		boolean isDone = false;
		while(!isDone){
			for(int i = 0; i < taskThread.length; i++){
				if(taskThread[i].isAlive()){
					if(cancel){
						//接收到退出指令，中断存活的任务线程
						for(int j = 0; j < taskThread.length; j++){
							if(taskThread[j].isAlive()) taskThread[j].interrupt();
						}
						cancel = false;
					}
					isDone = false;
					break;
				}else{
					isDone = true;
				}
			}
		}
		
		//所有任务线程结束，重置task
		if(task.currentPage() != 0){
			finishStr = "任务终止";
		}
		task.getHTMLFile().setFinishedPages(task.finishedPages());
		final int size = task.getHTMLFile().size();
		System.out.println("=================================" + finishStr + "，共记录<" + size + ">条结果=================================");
		
		if(!display.isDisposed()){
			display.syncExec(new Runnable() {
				
				@Override
				public void run() {
					Shell shell = display.getShells()[0];
					
					Control[] children = shell.getChildren();
					for(int i = 0; i < children.length; i++){
						if(children[i] instanceof Browser){
							((Browser)children[i]).setText(task.getHTMLFile().html());
							break;
						}
					}
					
					if(size > 0){
						FileDialog saveFile = new FileDialog(shell, SWT.SAVE);
						saveFile.setFilterExtensions(new String[]{"*.html"});
						
						String fileName = saveFile.open();
						if(fileName != null){
							try {
								task.persist(fileName);
							} catch (FileNotFoundException e) {
								// TODO Auto-generated catch block
								MessageBox alert = new MessageBox(shell, SWT.OK|SWT.ICON_ERROR);
								alert.setMessage("保存文件时发生错误，请检查该文件是否已经存在？\n如果文件已经存在，请确认文件不是只读状态！");
								alert.open();
								e.printStackTrace();
							} catch (UnsupportedEncodingException e){
								MessageBox alert = new MessageBox(shell, SWT.OK|SWT.ICON_ERROR);
								alert.setMessage("保存文件时发生错误，不支持UTF-8编码！");
								alert.open();
								e.printStackTrace();
							}
						}
					}
					
					for(int i = 0; i < children.length; i++){
						//任务结束，复位各个组件
						if(children[i] instanceof Button){
							//开始按钮复位
							Button btn = (Button)children[i];
							String text = btn.getText();
							if("正在停止".equals(text) || "取消".equals(text)){
								btn.setText("开始");
							}
						}else if(children[i] instanceof ProgressBar){
							//进度条隐藏
							ProgressBar progress = (ProgressBar)children[i];
							progress.setSelection(progress.getMinimum());
							progress.setVisible(false);
						}
					}
				}
			});
		}
	}
	
	public void cancelTask(){
		this.cancel = true;
	}
	
	private void updateProgressBar(final int max){
		if(!display.isDisposed()){
			display.syncExec(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					Control[] children = display.getShells()[0].getChildren();
					for(int i = 0; i < children.length; i++){
						if(children[i] instanceof ProgressBar){
							((ProgressBar)children[i]).setMaximum(max);
							break;
						}
					}
				}
			});
		}
	}
}
