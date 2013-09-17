package psl.cl.util;

public class ResourceUtil {
	//单例模式
	private static ResourceUtil util = null;
	
	/**构造函数私有化*/
	private ResourceUtil(){};
	
	/**
	 * 创建一个对象，单例模式
	 * */
	public static ResourceUtil createInstance(){
		if(util == null){
			util = new ResourceUtil();
		}
		return util;
	}
	/**
	 * 获取当前运行的Jar包所在路径，如果当前运行的程序没有打包成jar文件，则返回类所在的根目录(一般为bin\)
	 * @return jar包的话就返回当前jar包保存的路径，否则返回类的根路径，获取失败则返回空字符串("")，not null
	 * */
	public static String obtainJarPath(){
		String path = "";
		Class<?> clazz = createInstance().getClass();
		String url = clazz.getResource("ResourceUtil.class").toString();
		String clazzName = clazz.getName();
		String resourcePath = clazzName.replace('.', '/') + ".class";
		if(url.startsWith("file:")){
			//文件路径
			path = url.substring(url.indexOf("file:/") + "file:/".length(), url.indexOf(resourcePath) - 1);
		}else if(url.startsWith("jar:file:/")){
			//jar路径
			path = url.substring(url.indexOf("jar:file:/") + "jar:file:/".length(), url.indexOf(resourcePath) - 1);
			path = path.substring(0, path.lastIndexOf("/"));			
		}else{
			System.out.println("无法获取当前路径！");
		}
		
		return path;
	}
}
