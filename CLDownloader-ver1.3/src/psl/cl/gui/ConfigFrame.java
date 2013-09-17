package psl.cl.gui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import psl.cl.util.ResourceUtil;

public class ConfigFrame {
	//配置窗口的父窗口
	protected Shell parent;
	//配置窗口
	protected Shell configWindow;
	//线程数
	protected Label label_threadCount;
	protected Text threadCount;
	//起始页
	protected Label label_startPage;
	protected Text startPage;
	//总页数
	protected Label label_pageCount;
	protected Text pageCount;
	//抓取版块
	protected Label label_fid;
	protected Text fid;
	//种子存放路径
	protected Label label_path;
	protected Text path;
	//配置
	protected Properties properties;
	
	public ConfigFrame(Shell parent, Properties properties){
		this.parent = parent;
		this.properties = properties;
		
		initWidgets();
	}
	
	public void open(){
		this.configWindow.open();
	}
	
	private void initWidgets(){
		//初始化各个组件
		/*窗口*/
		configWindow = new Shell(parent, SWT.APPLICATION_MODAL|SWT.TITLE|SWT.CLOSE|SWT.BORDER);
		configWindow.setText("配置");
		Rectangle rect = parent.getBounds();
//		configWindow.setBounds(rect.x + rect.width / 2 - 75,rect.y + rect.height / 2 - 100 , 200, 145);
		configWindow.setLocation(rect.x + rect.width / 2 - 75,rect.y + rect.height / 2 - 100);
		GridLayout layout = new GridLayout(2, true);
		configWindow.setLayout(layout);
		
		/*线程数*/
		label_threadCount = new Label(configWindow, SWT.NONE);
		threadCount = new Text(configWindow, SWT.SINGLE|SWT.BORDER);
		label_threadCount.setText("线程数");
		threadCount.setTextLimit(3);
		threadCount.setText(properties.getProperty("ThreadCount"));
		threadCount.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		/*起始页*/
		label_startPage = new Label(configWindow, SWT.NONE);
		startPage = new Text(configWindow, SWT.SINGLE|SWT.BORDER);
		label_startPage.setText("起始页");
		startPage.setTextLimit(3);
		startPage.setText(properties.getProperty("StartPage"));
		startPage.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		/*总页数*/
		label_pageCount = new Label(configWindow, SWT.NONE);
		pageCount = new Text(configWindow, SWT.SINGLE|SWT.BORDER);
		label_pageCount.setText("总页数(0:全部)");
		pageCount.setTextLimit(3);
		pageCount.setText(properties.getProperty("PageCount"));
		pageCount.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		/*抓取版块*/
		label_fid = new Label(configWindow, SWT.NONE);
		fid = new Text(configWindow, SWT.SINGLE|SWT.BORDER);
		label_fid.setText("抓取板块");
		fid.setTextLimit(3);
		fid.setText(properties.getProperty("fid"));
		fid.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		/*种子存放路径*/
		label_path = new Label(configWindow, SWT.NONE);
		path = new Text(configWindow, SWT.SINGLE|SWT.BORDER);
		label_path.setText("种子存放路径");
		path.setEditable(false);
		path.setText(properties.getProperty("TorrentPath", ""));
		path.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		configWindow.pack();
		
		/*Text组件Listener*/
		Listener listener = new Listener() {
			
			@Override
			public void handleEvent(Event e) {
				// TODO Auto-generated method stub
				switch(e.type){
				case SWT.FocusIn:
					Text source = (Text)e.widget;
					source.selectAll();
					break;
				case SWT.Verify:
					e.doit = "0123456789".indexOf(e.text) != -1;
					break;
				}
			}
		};
		
		/*事件处理*/
		threadCount.addListener(SWT.FocusIn, listener);
		threadCount.addListener(SWT.Verify, listener);
		startPage.addListener(SWT.FocusIn, listener);
		startPage.addListener(SWT.Verify, listener);
		pageCount.addListener(SWT.FocusIn, listener);
		pageCount.addListener(SWT.Verify, listener);
		fid.addListener(SWT.FocusIn, listener);
		fid.addListener(SWT.Verify, listener);
		
		path.addMouseListener(new org.eclipse.swt.events.MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				DirectoryDialog dialog = new DirectoryDialog(configWindow);
				String directory = dialog.open();
				if(directory != null) path.setText(directory);
			}
		});
		
		configWindow.addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent arg0) {
				// TODO Auto-generated method stub
				properties.setProperty("ThreadCount", threadCount.getText());
				properties.setProperty("StartPage", startPage.getText());
				properties.setProperty("PageCount", pageCount.getText());
				properties.setProperty("fid", fid.getText());
				properties.setProperty("TorrentPath", path.getText());
				
				try {
					properties.store(new FileOutputStream(new File(ResourceUtil.obtainJarPath() + "/CLDownloader.properties")), null);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		
	}
}
