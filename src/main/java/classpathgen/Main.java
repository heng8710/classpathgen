package classpathgen;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

/**
 *默认路径层次是这样：
 *     ...../app/
 *     ...../app/xxxmain.jar
 *     ...../app/other_config_file
 *     ...../lib/all_dependency_jar
 *     ...../lib/ThisClass.jar【此类所在的jar包】
 *---------------------------------------------------
 *win7 ok。
 *ubuntu ok
 */
public class Main {

	public static void main(final String[] args) throws URISyntaxException {
		final boolean isWindows = isWindows();
		final Path basePath;
		if(args == null || args.length ==0){
			//这里认为classpath【.】就是本类所在的jar包（lib目录下）的上一次，也就是lib目录 下。
			//【jar:file:/F:/ftp/dailijob/lib/classpathgen-0.0.1-SNAPSHOT.jar!/classpathgen/】
			//【jar:file:/data/application/dailijob/lib/classpathgen-0.0.1-SNAPSHOT.jar!/classpathgen/】
			final String s = Main.class.getResource("").toString();
			final String jarFilePath = s.substring(isWindows? 10: 9, s.lastIndexOf("!"));
			/*System.out.println("jarFilePath :"+ jarFilePath);*/
			final Path libPath = Paths.get(jarFilePath).getParent();
			final File libFile = libPath.toFile();
			if(!libFile.isDirectory() || !"lib".equals(libFile.getName())){
				throw new IllegalStateException(String.format("[%s]目录结构层次不正确，应该是lib目录的",libFile));
			}
			basePath = libPath.getParent();
		}else{
			System.out.println(Arrays.toString(args));
			basePath = Paths.get(new URI(args[0]));
		}	
		final File[] brothersOfLib = basePath.toFile().listFiles();
		File mainJarFile = null;
		int mainJarFileCounter = 0;
		for(final File f: brothersOfLib){
			if(f.getName().toLowerCase().endsWith(".jar")){
				//与lib同层的文件中，不能有多个jar文件，只能有一个jar文件，而且这个jar文件必须是main主执行文件。
				mainJarFile = f;
				mainJarFileCounter++;
			}
		}
		
		if(mainJarFileCounter > 1){
			throw new IllegalStateException("与lib目录同层的jar文件太多了，只能有且只有一个主执行的jar文件");
		}
		if(mainJarFileCounter == 0){
			throw new IllegalStateException("与lib目录同层必须有一个主执行的jar");
		}
		final Set<File> dependenceJarFileSet = new TreeSet<File>(new Comparator<File>(){
			@Override
			public int compare(final File o1, final File o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		
		
		final File[] libJarFiles = basePath.resolve("lib").toFile().listFiles();
		for(final File f: libJarFiles){
			if(f.getName().toLowerCase().endsWith(".jar")){
				dependenceJarFileSet.add(f);
			}
		}
		//全部以绝对路径来。
		final StringBuilder classPathStringBuilder = new StringBuilder();
		final char splitter = isWindows? ';': ':';
		System.out.println("splitter="+ splitter);
		classPathStringBuilder.append('\"').append('.').append(splitter).append(basePath.toFile().getAbsolutePath()).append(splitter).append(mainJarFile.getAbsolutePath());
		for(final File f: dependenceJarFileSet){
			classPathStringBuilder.append(splitter).append(f.getAbsolutePath());
		}
		classPathStringBuilder.append('\"');
		final StringBuilder command = new StringBuilder();
		command.append("java -classpath ").append(classPathStringBuilder).append("        ");
		System.out.println("运行命令（后面要补上启动类名xxx.Yyy）：\r\n" + command);
	}

	
	static boolean isWindows(){
		final String os = System.getProperty("os.name");  
		if(os.toLowerCase().startsWith("win")){  
		  return true;
		}
		return false;
	}
}
