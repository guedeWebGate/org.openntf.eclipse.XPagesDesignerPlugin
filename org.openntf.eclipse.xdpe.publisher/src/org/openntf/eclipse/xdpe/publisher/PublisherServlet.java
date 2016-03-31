package org.openntf.eclipse.xdpe.publisher;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ibm.commons.util.io.json.JsonJavaFactory;
import com.ibm.commons.util.io.json.JsonJavaObject;
import com.ibm.commons.util.io.json.JsonParser;
import com.ibm.designer.domino.napi.NotesAPIException;
import com.ibm.designer.domino.napi.NotesDatabase;
import com.ibm.designer.domino.napi.NotesNote;
import com.ibm.designer.domino.napi.NotesSession;
import com.ibm.designer.domino.napi.design.FileAccess;
import com.ibm.domino.osgi.core.context.ContextInfo;
import com.ibm.xsp.http.FileUploadRequestWrapper;
import com.ibm.xsp.http.UploadedFile;
import com.ibm.xsp.http.fileupload.DefaultFileItemFactory;

import lotus.domino.ACL;
import lotus.domino.Database;
import lotus.domino.DxlImporter;
import lotus.domino.NotesException;
import lotus.domino.Session;

public class PublisherServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			System.out.println(ContextInfo.getUserSession().getEffectiveUserName());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		super.doGet(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			String tmpDir = System.getProperty("java.io.tmpdir") + File.separator + "xspupload";
			FileUploadRequestWrapper wrapper = new FileUploadRequestWrapper(request, tmpDir, 1000000000, new DefaultFileItemFactory());
			System.out.println(ContextInfo.getUserSession().getEffectiveUserName());
			System.out.println("Content-Type:"+request.getContentType());
			Map mpCurrent = wrapper.getParameterMap();
			for (Object key : mpCurrent.keySet()) {
				System.out.println("pm map: " + key);
			}
			String instructions = (String) mpCurrent.get("instructions");
			UploadedFile ulf = (UploadedFile) mpCurrent.get("package");
			Session userSession = ContextInfo.getUserSession();
			System.out.println("Instructions ->" + instructions);
			System.out.println(ulf.getServerFile().getAbsolutePath());
			JsonJavaObject json = null;
			JsonJavaFactory factory = JsonJavaFactory.instanceEx;
			json = (JsonJavaObject) JsonParser.fromJson(factory, instructions);
			String targetDb = json.getString("targetNsf");
			System.out.println(targetDb);
			buildDatabase(targetDb, ulf.getServerFile(), userSession);
			response.getWriter().println("my user is " + userSession.getEffectiveUserName());
			response.getWriter().close();
			wrapper.cleanup();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void buildDatabase(String targetDb, File serverFile, Session userSession) throws NotesException, IOException, NotesAPIException {
		Database db = userSession.getDatabase("", targetDb, false);
		if (db == null) {
			System.out.println("DB Does not Exists");
			db = userSession.getDbDirectory("").createDatabase(targetDb, true);
			DxlImporter importer = userSession.createDxlImporter();
			importer.setDesignImportOption(DxlImporter.DXLIMPORTOPTION_REPLACE_ELSE_CREATE);

			// generate DXL
			String dxl = generateDXL("NewDB", db.getFilePath(), db.getReplicaID());

			// import DXL
			importer.importDxl(dxl, db);

			// set ACL: Default to Manager
			ACL acl = db.getACL();
			acl.getFirstEntry().setLevel(ACL.LEVEL_MANAGER);
			acl.save();

		} else {
			// TODO: check if I'm allowed
		}
		JarFile jarFile = new JarFile(serverFile);
		Enumeration<JarEntry> entries = jarFile.entries();
		List<XPagesEntry> xpEntries = new ArrayList<XPagesEntry>();
		List<JavaCodeEntry> jcEntries = new ArrayList<JavaCodeEntry>();
		List<JarEntry> allClasses = new ArrayList<JarEntry>();
		List<JarEntry> allWCFiles = new ArrayList<JarEntry>();
		while (entries.hasMoreElements()) {
			JarEntry entry = entries.nextElement();
			if (entry.getName().endsWith(".xsp")) {
				xpEntries.add(new XPagesEntry(entry));
			}
			if (entry.getName().endsWith(".class")) {
				allClasses.add(entry);
			}
			if ((entry.getName().startsWith("WebContent/") && !entry.getName().startsWith("WebContent/WEB-INF"))
					|| (entry.getName().startsWith("WebContent/WEB-INF") && !entry.getName().startsWith("WebContent/WEB-INF/classes"))) {
				allWCFiles.add(entry);
			}
			if (entry.getName().endsWith(".java") && !entry.getName().startsWith("Generated")) {
				jcEntries.add(new JavaCodeEntry(entry));
			}
		}
		for (JarEntry classEntry : allClasses) {
			String className = classEntry.getName().replace("/", ".");
			for (XPagesEntry xpEntry : xpEntries) {
				String xpClassName = xpEntry.getXPagesClassName();
				if (className.contains(xpClassName + ".") || className.contains(xpClassName + "$")) {
					xpEntry.addClass(classEntry);
				}
			}
			className = classEntry.getName();
			for (JavaCodeEntry jcEntry : jcEntries) {
				String codeName = jcEntry.getJavaCodeFileName().replace(".java", "");
				if (className.contains(codeName + ".") || className.contains(codeName + "$")) {
					jcEntry.addClass(classEntry);
				}
			}
		}
		NotesSession nSession = new NotesSession();
		NotesDatabase nDatabase = nSession.getDatabaseByPath(targetDb);
		nDatabase.open();
		for (XPagesEntry xpEntry : xpEntries) {
			System.out.println(xpEntry.getXPagesName() + " " + xpEntry.getClasses().size());
			String xpagesName = xpEntry.getXPagesName().substring(1);
			NotesNote nFile = FileAccess.getFileByPath(nDatabase, xpagesName);
			if (nFile == null) {
				nFile = nDatabase.createNote();
				nFile.initAsFile("gC~4K");
				nFile.setItemText("$TITLE", xpagesName);
			}
			nFile.setItemAsTextList("$ClassIndexItem", xpEntry.getClassPathElements());
			InputStream is = jarFile.getInputStream(xpEntry.getXpages());
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			com.ibm.commons.util.io.StreamUtil.copyStream(is, bos);
			FileAccess.saveData(nFile, xpagesName, bos.toByteArray());
			int nCount = 0;
			for (JarEntry jent : xpEntry.getClasses()) {
				InputStream isClass = jarFile.getInputStream(jent);
				ByteArrayOutputStream bosClass = new ByteArrayOutputStream();
				com.ibm.commons.util.io.StreamUtil.copyStream(isClass, bosClass);
				FileAccess.saveByteData(nFile, xpagesName, bosClass.toByteArray(), "$ClassData" + nCount, "$ClassSize" + nCount);
				nCount++;

			}
			nFile.recycle();
		}
		for (JarEntry je : allWCFiles) {
			String name = je.getName().substring("WebContent/".length());
			System.out.println("WC-NAME ===>" + name);
			NotesNote nFile = FileAccess.getFileByPath(nDatabase, name);
			if (nFile == null) {
				nFile = nDatabase.createNote();
				nFile.initAsFile("~C4g");
				nFile.setItemText("$TITLE", name);
				nFile.setExtendedFlags("w");
			}
			InputStream is = jarFile.getInputStream(je);
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			com.ibm.commons.util.io.StreamUtil.copyStream(is, bos);
			FileAccess.saveData(nFile, name, bos.toByteArray());
		}
		for (JavaCodeEntry jcEntry : jcEntries) {
			System.out.println(jcEntry.getJavaCodeFileName() + " " + jcEntry.getClasses().size());
			NotesNote nFile = FileAccess.getFileByPath(nDatabase, jcEntry.getJavaCodeFileName());
			if (nFile == null) {
				nFile = nDatabase.createNote();
				nFile.initAsFile("34567Cg~[");
				nFile.setItemText("$TITLE", jcEntry.getJavaCodeFileName());
			}
			nFile.setItemAsTextList("$ClassIndexItem", jcEntry.getClassPathElements());
			InputStream is = jarFile.getInputStream(jcEntry.getJavaCode());
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			com.ibm.commons.util.io.StreamUtil.copyStream(is, bos);
			FileAccess.saveData(nFile, jcEntry.getJavaCodeFileName(), bos.toByteArray());
			int nCount = 0;
			for (JarEntry jent : jcEntry.getClasses()) {
				InputStream isClass = jarFile.getInputStream(jent);
				ByteArrayOutputStream bosClass = new ByteArrayOutputStream();
				com.ibm.commons.util.io.StreamUtil.copyStream(isClass, bosClass);
				FileAccess.saveByteData(nFile, jcEntry.getJavaCodeFileName(), bosClass.toByteArray(), "$ClassData" + nCount, "$ClassSize" + nCount);
				nCount++;

			}
			nFile.recycle();
		}

		nDatabase.recycle();
		nSession.recycle();
	}

	private void createFile(File nFile, String name, String database, byte[] arr, String fileTypeSequence) throws NotesAPIException {

	}

	private static String generateDXL(final String dbTitle, final String dbPath, final String dbReplicaId) {

		StringBuilder str = new StringBuilder();

		str.append("<?xml version='1.0' encoding='utf-8'?>");
		str.append("<!DOCTYPE database SYSTEM 'xmlschemas/domino_8_5_3.dtd'>");
		str.append("<database xmlns='http://www.lotus.com/dxl' version='8.5' maintenanceversion='3.0' ");
		str.append("replicaid='");
		str.append(dbReplicaId);
		str.append("' path='");
		str.append(dbPath);
		str.append("' title='");
		str.append(dbTitle);
		str.append("' allowstoredforms='false' ");
		str.append("usejavascriptinpages='false' increasemaxfields='true' showinopendialog='false'>");
		str.append("<databaseinfo dbid='");
		str.append(dbReplicaId);
		str.append("' odsversion='51' ");
		str.append("numberofdocuments='0'></databaseinfo>");
		str.append("<note default='true' class='icon'>");
		str.append("<noteinfo noteid='11e'>");
		str.append("</noteinfo>");
		str.append("<item name='IconBitmap' summary='true'>");
		str.append("<rawitemdata type='6'>");
		str.append("AiAgAQAA///////wD///gAH//gAAf/wAAD/4AAAf8AAAD+AAAAfgAAAHwAAAA8AAAAPAAAADgAAA");
		str.append("AYAAAAGAAAABgAAAAYAAAAGAAAABgAAAAYAAAAHAAAADwAAAA8AAAAPgAAAH4AAAB/AAAA/4AAAf");
		str.append("/AAAP/4AAH//gAH///AP//////8AAAAAAAAAAAAAAAAAAAAAAAAAAAAAiIiIiAAAAAAAAAAAAAAI");
		str.append("jPZmZm/IgAAAAAAAAAAIjGZmZmZmZsiAAAAAAAAAjGZmZmZmZmZmyAAAAAAACMZmZmZmZmZmZmyA");
		str.append("AAAAAIxmZmZmZmZmZmZmyAAAAAjGZmZmZmZmZmZmZmyAAAAIZmbyL2byL2byL2ZmgAAAjGZmIiJm");
		str.append("IiJmIiJmZsgAAIZmZiIiZiIiZiIiZmZoAADGZmYiImYiImYiImZmbAAI9mZmIiJmIiJmIiJmZm+A");
		str.append("CGZmZiIiZiIiZiIiZmZmgAhmZmYiImYiImYiImZmZoAIZmZmIiJmIiJmIiJmZmaACGZvIiIiZiIi");
		str.append("ZiIiIvZmgAhmYiIiImYiImYiIiImZoAIZm8iIi9m8i9m8iIi9maACPZmZmZmZmZmZmZmZmZvgADG");
		str.append("ZmbyL2byL2byL2ZmbAAAj2ZmIiJmIiJmIiJmZvgAAIxmZiIiZiIiZiIiZmbIAAAI9mbyL2byL2by");
		str.append("L2ZvgAAACMZmZmZmZmZmZmZmbIAAAACMZmZmZmZmZmZmZsgAAAAACMZmZmZmZmZmZmyAAAAAAACM");
		str.append("9mZmZmZmZm/IAAAAAAAACIz2ZmZmZm/IgAAAAAAAAAAIiMZmZmyIgAAAAAAAAAAAAACIiIiIAAAA");
		str.append("AAAAUEECICABAAD/////+A4DgA==");
		str.append("</rawitemdata></item>");
		str.append("<item name='$Daos'><text>0</text></item>");
		str.append("<item name='$TITLE'><text>");
		str.append(dbTitle);
		str.append("</text></item>");
		str.append("<item name='$Flags'><text>7f</text></item>");
		str.append("<item name='$FlagsNoRefresh'><text/></item></note>");
		str.append("<view xmlns='http://www.lotus.com/dxl' version='8.5' maintenanceversion='3.0' ");
		str.append("replicaid='");
		str.append(dbReplicaId);
		str.append("' showinmenu='true' publicaccess='false' default='true' noviewformat='true'>");
		str.append("<noteinfo noteid='11a' sequence='1'></noteinfo>");
		str.append("<code event='selection'><formula>SELECT @All</formula></code>");
		str.append("<item name='$FormulaClass'><text>1</text></item></view>");
		str.append("</database>");

		return str.toString();

	}

}
