package jasmin.core;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.jar.*;

/**
 * @author Yang Guo this CommandLoader loads all the x86 command encapsuled in classes from a given package in
 *         this
 *         order. - file system: subfolders of the folder where jasmin is in - other jars in the folder - the
 *         own jar
 */
public class CommandLoader {
	
	/**
	 * the package where the classes are to be found
	 */
	private String defaultpackage;
	
	/**
	 * the super class for all the commands
	 */
	private Class<?> type;
	
	/**
	 * the Hashtable where you can get all the commands
	 */
	private Hashtable<String, Object> commands;
	
	/**
	 * the dataspace to initialize the commands with
	 */
	private DataSpace dataspace;
	
	/**
	 * 
	 */
	static String classPath() {
		String classPath = System.getProperty("java.class.path");
		String ret = classPath.split(File.pathSeparator)[0];
		return ret;
	}
	
	/**
	 * look where the jar is located if starting from one
	 * 
	 * @return the absolute path of the jar or the class jasmin is starting from
	 */
	static File getLocation() {
		File location = (new File(classPath())).getParentFile();
		if (location == null) {
			return new File(".");
		}
		return location;
	}
	
	/**
	 * @param newdataspace
	 *        which dataspace the commandloader is linked to
	 * @param defaultpackage
	 *        the package all the commands are in
	 * @param type
	 *        the super class of the commands
	 */
	public CommandLoader(DataSpace newdataspace,
			String defaultpackage, Class<?> type) {
		this.dataspace = newdataspace;
		this.type = type;
		this.defaultpackage = defaultpackage;
		this.commands = new Hashtable<String, Object>();
		System.out.println("CommandLoader loading...");
		URL url = getClass().getResource("../../" + defaultpackage.replace(".", "/"));
		File file = null;
		if (url != null) {
			System.out.println("looks like you are not starting from a jar-package");
			file = new File(url.getPath());
			addDir(file);
			System.out.println("... done with directories");
		} else {
			System.out.println("looks like you are starting from a jar-package");
			file = getLocation();
			addJar(file);
			System.out.println("... done with jars\n");
		}
	}
	
	/**
	 * adds new locations for command classes
	 * 
	 * @param file
	 *        location where the .class files containing the command are
	 * @return how many commands are loaded from this directory
	 */
	public int addDir(File file) {
		if (!file.exists()) {
			return 0;
		}
		String[] names = getNames(getFiles(file, ".class"));
		loadClass(file, names);
		return 1;
	}
	
	/**
	 * add a jar to search in
	 * 
	 * @param startjar
	 * @return how many commands are loaded from this jar
	 */
	public int addJar(File startjar) {
		if (!startjar.exists()) {
			return 0;
		}
		File self = new File(classPath());
		String[] names = getJarEntries(self);
		loadClass(self, names);
		
		return 1;
	}
	
	/**
	 * load found command classes into the hashtable and link them with dataspace. thanks to URLClassLoader it
	 * does not make any difference whether it is a jar or a folder
	 * 
	 * @param file
	 *        directory to load from
	 * @param names
	 *        class names
	 * @return currently always returns 0
	 */
	private int loadClass(File file, String[] names) {
		URL[] url = null;
		int counter = 0;
		url = new URL[1];
		try {
			url[0] = file.toURI().toURL();
		} catch (MalformedURLException e) {
		}
		
		URLClassLoader loader = new URLClassLoader(url);
		for (String name : names) {
			try {
				Class<?> commandclass = loader.loadClass(defaultpackage + "." + name);
				Object object = commandclass.newInstance();
				if (this.type.isInstance(object)) {
					JasminCommand command = (JasminCommand) object;
					command.setDataSpace(dataspace);
					if (command.getID() != null) {
						for (String ID : command.getID()) {
							if (commands.get(ID) == null) {
								commands.put(ID, command);
								counter++;
								// System.out.println(ID);
							}
						}
					}
				} else {
					System.out.println(object.getClass() + " is not a JasminCommand");
				}
			} catch (ClassNotFoundException e) {
				// System.out.println(name+" is not in the package "+defaultpackage);
			} catch (InstantiationException e) {
				System.out.println("instantiation error of " + name);
			} catch (IllegalAccessException e) {
				System.out.println("illegal access of " + name);
			}
		}
		System.out.println("+ " + String.valueOf(counter) + "\tcommand(s) from:\t" + url[0]);
		return 0;
	}
	
	/**
	 * search a jar file for possible commands
	 * 
	 * @param file
	 *        he jar file
	 * @return an array of strings which are potential command classes
	 */
	private String[] getJarEntries(File file) {
		ArrayList<String> entries = new ArrayList<String>();
		String packagename = defaultpackage.replace(".", "/") + "/";
		try {
			JarInputStream jis = new JarInputStream(new BufferedInputStream(new FileInputStream(file)));
			JarEntry je = jis.getNextJarEntry();
			
			while (je != null) {
				if (je.getName().toLowerCase().endsWith(".class") && je.getName().startsWith(packagename)) {
					entries.add(je.getName().replaceAll(".class", "").replace(packagename, ""));
				}
				je = jis.getNextJarEntry();
			}
			
		} catch (FileNotFoundException e) {
			System.out.println("jar file not found");
		} catch (IOException e) {
			System.out.println("jar file cannot be opened");
		}
		String[] entryArray = new String[entries.size()];
		for (int i = 0; i < entries.size(); i++) {
			entryArray[i] = entries.get(i);
		}
		return entryArray;
		
	}
	
	/**
	 * @return a list of all avaiable command mnemonics
	 */
	public String[] getMnemoList() {
		Enumeration<String> enumeration = commands.keys();
		String[] result = new String[commands.size()];
		int i = 0;
		while (enumeration.hasMoreElements()) {
			result[i++] = enumeration.nextElement();
		}
		return result;
	}
	
	/**
	 * checks if a mnemo exist
	 */
	public boolean commandExists(String mnemo) {
		return (commands.get(mnemo) == null) ? false : true;
	}
	
	/**
	 * @param mnemo
	 * @return the command object
	 */
	public Object getCommand(String mnemo) {
		return commands.get(mnemo);
	}
	
	/**
	 * get recursively the contents of a folder and its subfolders which a specified extension
	 * 
	 * @param dir
	 *        folder
	 * @param extension
	 *        specified extension
	 * @return an array of files of potential command classes
	 */
	private File[] getFiles(File dir, String extension) {
		ArrayList<File> files = new ArrayList<File>();
		getFilesEmbedded(dir, files, extension);
		File[] filearray = new File[files.size()];
		for (int i = 0; i < files.size(); i++) {
			filearray[i] = files.get(i);
		}
		return filearray;
	}
	
	/**
	 * embedded method to provide for the recursive getFiles() method
	 */
	private void getFilesEmbedded(File dir, ArrayList<File> files, String extension) {
		if (dir.isDirectory()) {
			System.out.println("  ~ searching dir: " + dir);
			for (File subfile : dir.listFiles()) {
				getFilesEmbedded(subfile, files, extension);
			}
		}
		if (dir.isFile() && dir.getPath().toLowerCase().endsWith(extension)) {
			files.add(dir);
		}
	}
	
	/**
	 * @param files
	 *        array of files
	 * @return corresponding array of strings
	 */
	private String[] getNames(File[] files) {
		String[] names = new String[files.length];
		for (int i = 0; i < files.length; i++) {
			names[i] = files[i].getName().replace(".class", "");
		}
		return names;
	}
	
}
