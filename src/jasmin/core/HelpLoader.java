package jasmin.core;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.jar.*;

/**
 * this class loads on start all .htm-files in the folder [language] in the folder where jasmin is
 * located (without subfolders), or if starting from a jar, all .htm files contained in the respective
 * folder inside it.
 * 
 * @author Yang Guo
 */
public class HelpLoader {
	
	/**
	 * the hashtable where the context help is loaded into
	 */
	private Hashtable<String, String> helpcache;
	
	/**
	 * the currently chosen language
	 */
	private String language;
	
	private String helproot = "help";
	
	/**
	 * the constructor, loads the .htm files located in the [language] folder
	 * 
	 * @param language
	 *        relative path. would be "help/en" currently
	 */
	public HelpLoader(String language) {
		System.out.println("HelpLoader loading...");
		this.language = language;
		this.helpcache = new Hashtable<>();
		init();
		System.out.println("... done\n");
	}
	
	/**
	 * does the same as the constructor
	 * 
	 * @param language
	 *        relative path. would be "help/en" currently
	 */
	public void reInit(String language) {
		System.out.println("HelpLoader reloading...");
		if (this.language == null) {
			System.out.println("language == null! Do you have an empty ~/.jasmin? Delete it!"
				+ " And please tell us about this at jasmin@lrr.in.tum.de");
		} else if (this.language.equals(language)) {
			System.out.println("but the same language all over again? nope!");
			return;
		}
		helpcache.clear();
		this.language = language;
		this.helpcache = new Hashtable<>();
		init();
		System.out.println("... done\n");
	}
	
	/**
	 * @param mnemo
	 *        the command the context help has to refer to
	 * @return the html code for the context help
	 */
	public String get(String mnemo) {
		return helpcache.get(mnemo.toLowerCase());
	}
	
	/**
	 * @param mnemo
	 *        the mnemo to check on
	 * @return if a context help exists for that mnemo
	 */
	public boolean exists(String mnemo) {
		return helpcache.containsKey(mnemo.toLowerCase());
	}
	
	/**
	 * for debugging.
	 * 
	 * @return a String array containing all mnemos for which help files exist
	 */
	public String[] getMnemoList() {
		return helpcache.keySet().toArray(new String[helpcache.size()]);
	}
	
	/**
	 * initialization. load all files.
	 */
	private void init() {
		URL home = getClass().getResource("..");
		File local;
		if (home != null) {
			System.out.println("looks like you are not starting from a jar-package");
			local = new File(home.getPath() + "/" + helproot + "/" + language + "/");
			System.out.println("local: " + local.getAbsolutePath());
			initFile(local);
			try {
				local = new File(new File(home.getPath()).getParentFile().getParentFile(), helproot + "/"
					+ language);
				System.out.println("local: " + local.getAbsolutePath());
				initFile(local);
			} catch (Exception e) {
				System.out.println("but not loading from the developer workbench?!");
			}
			
		} else {
			System.out.println("looks like you are starting from a jar-package");
			local = CommandLoader.getLocation();
			initJar(local);
		}
		List<String> l = getLanguages();
		for (String aL : l) {
			System.out.println("language found: " + aL);
		}
		
	}
	
	/**
	 * load a specified directory filled with help files (outside a jar)
	 * 
	 * @param local
	 *        the directory
	 */
	private void initFile(File local) {
		if (!local.exists()) {
			return;
		}
		int counter = 0;
		if (local.isDirectory()) {
			for (File file : local.listFiles()) {
				if (file.getName().toLowerCase().endsWith(".htm") && file.isFile()) {
					String mnemo = file.getName().toLowerCase().replace(".htm", "");
					if (!exists(mnemo)) {
						addToCache(file, mnemo);
						counter++;
					}
				}
			}
		}
		try {
			System.out.println("+ " + String.valueOf(counter) + "\thelp text(s) from:\t"
				+ local.getCanonicalPath());
		} catch (IOException ignored) {
		}
		
	}
	
	/**
	 * load from all jars inside the [local] folder (w/o subfolders)
	 * 
	 * @param local
	 *        the folder to look for the jars
	 */
	private void initJar(File local) {
		File jar = new File(CommandLoader.classPath());
		loadFromJar(jar);
	}
	
	/**
	 * load from the specified jar filled with help files in the [language] directory in the jar
	 * 
	 * @param file
	 *        the jar file
	 */
	private void loadFromJar(File file) {
		if (file.getName().toLowerCase().endsWith(".jar") && file.isFile()) {
			try {
				int counter = 0;
				JarInputStream jis;
				JarEntry je;
				counter = 0;
				jis = new JarInputStream(new BufferedInputStream(new FileInputStream(file)));
				je = jis.getNextJarEntry();
				
				while (je != null) {
					String mnemo = trimEntryName(je);
					if (je.getName().toLowerCase().matches(helproot + "/" + language + "/.*.htm")
						&& !exists(mnemo)) {
						addToCache(jis, mnemo);
						counter++;
					}
					je = jis.getNextJarEntry();
				}
				jis.close();
				
				System.out.println("+ " + String.valueOf(counter) + "\thelp text(s) from:\t"
					+ file.getCanonicalPath());
			} catch (IOException ignored) {
			}
		}
	}
	
	/**
	 * load from specified JarInputStream
	 * 
	 * @param fis
	 *        Jar Input Stream
	 * @param mnemo
	 *        the name for the text file
	 */
	private void addToCache(JarInputStream fis, String mnemo) {
		String text = "";
		byte[] buf = new byte[4096];
		int i = 0;
		try {
			while ((i = fis.read(buf)) != -1) {
				text += new String(buf, 0, i);
			}
		} catch (IOException ignored) {
		}
		helpcache.put(mnemo, text);
	}
	
	private static String trimEntryName(JarEntry je) {
		String[] s = je.getName().toLowerCase().replace(".htm", "").split("/");
		return s[s.length - 1];
	}
	
	/**
	 * Loads from specified file
	 * 
	 * @param file
	 * @param mnemo
	 *        the name for the text file
	 */
	private void addToCache(File file, String mnemo) {
		BufferedInputStream fis;
		try {
			fis = new BufferedInputStream(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			System.out.println("Warning! File '" + file + "' not found!");
			return;
		}
		String text = "";
		byte[] buf = new byte[4096];
		int i = 0;
		try {
			while ((i = fis.read(buf)) != -1) {
				text += new String(buf, 0, i);
			}
			fis.close();
		} catch (IOException ignored) {
		}
		helpcache.put(mnemo, text);
	}
	
	/**
	 * automatically generate non-existing help html files for existing commands in the CommandLoader
	 * 
	 * @param cl
	 *        the CommandLoader where you look for the existing commands
	 */
	public void createHelpText(CommandLoader cl) {
		File dir = new File((getClass().getResource("..").getPath()));
		dir = new File(dir, language);
		if (!dir.exists() && !dir.mkdirs()) {
			System.out.println("Failed to create " + dir.getAbsolutePath());
			return;
		}
		for (String mnemo : cl.getMnemoList()) {
			if (!exists(mnemo)) {
				File file = new File(dir, mnemo + ".htm");
				try {
					file.createNewFile();
					BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(file));
					PrintStream ps = new PrintStream(os);
					ps.println("<html>\n<head>\n<title>" + mnemo + "\n</title>\n</head>\n<body>");
					ps.println("Command: " + mnemo.toUpperCase() + "<br>");
					ps.println("arguments: <br>");
					ps.println("effects: <br>");
					ps.println("flags to be set: <br>");
					ps.println("approx. clockcycles: <br>");
					ps.println("misc: <br>");
					ps.println("</body>\n</html>");
					ps.flush();
					ps.close();
				} catch (IOException e) {
					System.out.println("failed to create " + file.getAbsolutePath());
				}
			}
		}
	}
	
	/**
	 * adds to the list only if the String s has not alreay been added
	 * 
	 * @param paths
	 *        the list to add to
	 * @param s
	 *        the String to add
	 */
	public void addToList(List<String> paths, String s) {
		for (String path : paths) {
			if (path.equals(s)) {
				return;
			}
		}
		paths.add(s);
	}
	
	/**
	 * scans for possible help directories (languages)
	 * 
	 * @return a list of possible language directories
	 */
	public List<String> getLanguages() {
		List<String> paths = new ArrayList<>();
		URL home = getClass().getResource("..");
		File local;
		if (home != null) {
			System.out.println("looks like you are not starting from a jar-package");
			try {
				local = new File(new File(home.getPath()).getParentFile().getParentFile(), helproot);
				System.out.println("local: " + local.getAbsolutePath());
				for (File s : local.listFiles()) {
					if (s.isDirectory() && (s.listFiles().length > 0)) {
						addToList(paths, s.getName());
					}
				}
			} catch (Exception e) {
				System.out.println("but not loading from the developer workbench?!");
			}
			
		} else {
			System.out.println("looks like you are starting from a jar-package");
			local = CommandLoader.getLocation();
			File helpDir = new File(local, helproot);
			if (helpDir.exists() && helpDir.isDirectory() && (helpDir.listFiles().length > 0)) {
				for (File s : helpDir.listFiles()) {
					if (s.isDirectory()) {
						addToList(paths, s.getName());
					}
				}
			}
			
			File jar = new File(CommandLoader.classPath());
			for (File file : local.listFiles()) {
				try {
					if (!jar.getCanonicalPath().equals(file.getCanonicalPath())) {
						searchJarPath(file, paths);
					}
				} catch (IOException ignored) {
				}
				searchJarPath(jar, paths);
			}
			
		}
		return paths;
	}
	
	/**
	 * scans inside a jar for possible language directories
	 * 
	 * @param file
	 *        the jar file
	 * @param paths
	 *        the list to add to
	 * @param helproot
	 *        the root directory of all help directories
	 */
	private void searchJarPath(File file, List<String> paths) {
		if (file.getName().toLowerCase().endsWith(".jar") && file.isFile()) {
			try {
				JarInputStream jis;
				JarEntry je;
				jis = new JarInputStream(new BufferedInputStream(new FileInputStream(file)));
				je = jis.getNextJarEntry();

				while (je != null) {
					if (je.getName().startsWith(helproot)) {
						String[] name = je.getName().split("/");
						if (name.length > 1) {
							addToList(paths, name[1]);
						}
					}
					je = jis.getNextJarEntry();
				}
				jis.close();
			} catch (IOException ignored) {
			}
		}
	}
}
