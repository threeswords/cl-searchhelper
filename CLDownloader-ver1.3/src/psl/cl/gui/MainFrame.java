package psl.cl.gui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import psl.cl.function.DownloadFunction;
import psl.cl.model.TaskOverview;
import psl.cl.thread.PrepareThread;
import psl.cl.util.ResourceUtil;

/**
 * UI窗体
 * */
public class MainFrame{
	//配置文件路径
	private static final String USER_PROPERTIES = ResourceUtil.obtainJarPath() + "/CLDownloader.properties";

	private static final String DEFAULT_PROPERTIES = "/res/CLDownloader.properties";
	
	protected Display display;
	//主窗体
	protected Shell mWindow;
	//输入框
	protected Text mInputLine;
	//确认按钮
	protected Button mSubmit;
	//进度条
	protected ProgressBar mBar;
	//结果显示框
	protected Browser mLinks;
	//配置文件
	protected Properties properties;
	//打开按钮
	protected Button mOpen;
	//配置按钮
	protected Button mConfig;

	/**
	 * 构造方法，初始化所包含的组件
	 * */
	public MainFrame(){
		//载入配置文件
		properties = new Properties();
		//载入用户配置文件
		InputStream is = null;
		try {
			//读取用户配置，如果没有，则is为null，不需要考虑关闭问题
			is = new FileInputStream(USER_PROPERTIES);
		} catch (FileNotFoundException e) {
			System.out.println("没有用户配置文件，载入默认配置！");
		}
		//is为null，则载入默认配置文件
		if(is == null) is = getClass().getResourceAsStream(DEFAULT_PROPERTIES);
		
		try {
			//读取配置文件
			if(is == null){
				throw new IOException("默认配置文件缺失！");
			}
			properties.load(is);
		} catch (IOException e) {
			System.out.println("载入配置文件失败：" + e.getMessage());
		} finally{
			if(is != null){
				try {
					is.close();
				} catch (IOException e) {
					System.out.println("配置文件输入流关闭错误：" + e.getMessage());
				}
			}
		}
		
		//初始化组件
		initWidgets();
		
	}
	
	/**
	 * 显示当前窗体，创建事件处理队列，该方法会阻塞当前线程，直至当前窗体调用dispose()
	 * */
	public void open(){
		mWindow.open();
		
		//当前窗体没有关闭，则循环事件处理
		while(!mWindow.isDisposed()){
			if(!display.readAndDispatch()) display.sleep();
		}
		
		//当前窗体被关闭，则上面的循环终止，关闭display
		display.dispose();
	}
	
	/**
	 * 初始化各个组件
	 * */
	private void initWidgets(){
		//====================================实例化各个组件=============================
		display = Display.getDefault();
		/*窗口*/
		mWindow = new Shell(display);
		mWindow.setText("草榴社区搜索工具");
		mWindow.setSize(900, 600);
		GridLayout layout = new GridLayout();
		layout.numColumns = 4;
		mWindow.setLayout(layout);
		
		/*输入框*/
		mInputLine = new Text(mWindow, SWT.SINGLE|SWT.BORDER);
		mInputLine.setText("http://cl.cn.mu");
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.grabExcessHorizontalSpace = true;
		mInputLine.setLayoutData(data);
		
		/*开始按钮*/
		mSubmit = new Button(mWindow, SWT.PUSH);
		data = new GridData();
		data.widthHint = 60;
		mSubmit.setLayoutData(data);
		mSubmit.setText("开始");
		
		/*打开按钮*/
		mOpen = new Button(mWindow, SWT.PUSH);
		data = new GridData();
		data.widthHint = 60;
		mOpen.setLayoutData(data);
		mOpen.setText("打开");
		
		/*配置按钮*/
		mConfig = new Button(mWindow, SWT.PUSH);  
		data = new GridData();
		data.widthHint = 60;
		mConfig.setLayoutData(data);
		mConfig.setText("配置");
		
		/*显示容器Browser*/
		mLinks = new Browser(mWindow, SWT.NONE|SWT.BORDER);
		data = new GridData();
		data.horizontalSpan = 4;
		data.grabExcessHorizontalSpace = true;
		data.grabExcessVerticalSpace = true;
		data.horizontalAlignment = GridData.FILL;
		data.verticalAlignment = GridData.FILL;
		mLinks.setLayoutData(data);
		mLinks.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
		mLinks.setText("<html></html>");
		//添加一个浏览器的Java函数，可被js调用
		new DownloadFunction (mLinks, "dlTorrent", properties);
		
		/*进度条*/
		mBar = new ProgressBar(mWindow, SWT.SMOOTH);
		data = new GridData();
		data.horizontalSpan = 4;
		data.grabExcessHorizontalSpace = true;
		data.horizontalAlignment = GridData.FILL;
		mBar.setLayoutData(data);
		mBar.setVisible(false);
		
		//====================================添加事件响应==============================
		/*输入框回车响应*/
		mInputLine.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent arg0) {
				// TODO Auto-generated method stub
				if(arg0.character == '\r'){
					Event e = new Event();
					e.widget = mSubmit;
					mSubmit.notifyListeners(SWT.Selection, e);
				}
			}
		});
		
		/*打开按钮点击响应*/
		mOpen.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				FileDialog dialog = new FileDialog(mWindow, SWT.OPEN);
				dialog.setFilterExtensions(new String[]{"*.htm;*.html"});
				String fileName = dialog.open();
				if(fileName != null){
					try {
						Document doc = Jsoup.parse(new File(fileName), "utf-8");
						mLinks.setText(doc.html());
					} catch (IOException e) {
						MessageBox msg = new MessageBox(mWindow, SWT.OK|SWT.ICON_ERROR);
						msg.setMessage("载入文件错误：" + e.getMessage());
					}
				}
			}
		});
		
		/*配置按钮点击响应*/
		mConfig.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				new ConfigFrame(mWindow, properties).open();
			}
		});
		
		/*开始按钮点击响应*/
		mSubmit.addSelectionListener(new OnButtonClickListener());
	}
	
	/**
	 * 内部类，Button点击事件监听器
	 * */
	private class OnButtonClickListener extends SelectionAdapter{
		TaskOverview task = null;
		PrepareThread taskRunnable = null;
		Thread prepareThread = null;
		
		@Override
		public void widgetSelected(SelectionEvent arg0) {
			Button source = (Button)arg0.widget;
			if("开始".equals(source.getText())){
				source.setText("取消");
				//显示进度条
				mBar.setVisible(true);
				//创建新任务、线程
				task = new TaskOverview(properties, mInputLine.getText());
				taskRunnable = new PrepareThread(display, task);
				prepareThread = new Thread(taskRunnable);
				//复位bowser内容
				mLinks.setText("<html></html>");
				//启动线程
				prepareThread.start();
			}else if("取消".equals(source.getText())){
				source.setText("正在停止");
				//中断预备监视线程
				prepareThread.interrupt();
				//退出任务线程
				taskRunnable.cancelTask();
			}
		}
		
	}
}