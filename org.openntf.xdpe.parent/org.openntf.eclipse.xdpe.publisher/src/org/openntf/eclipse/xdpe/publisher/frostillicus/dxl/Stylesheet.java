package org.openntf.eclipse.xdpe.publisher.frostillicus.dxl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.codec.binary.Base64;

import com.ibm.xsp.extlib.util.ExtLibUtil;
import com.raidomatic.xml.XMLNode;

//import org.apache.commons.codec.binary.Base64;
import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.DxlImporter;


public class Stylesheet extends AbstractDXLDesignNote {
	private static final long serialVersionUID = -3543549758559295423L;

	public Stylesheet(String databaseDocumentId, String designDocumentId) throws Exception {
		super(databaseDocumentId, designDocumentId);
	}

	public String getContent() throws XPathExpressionException, UnsupportedEncodingException, IOException {
		String fileData = this.getRootNode().selectSingleNode("/stylesheetresource/filedata").getTextContent();

		return new String(Base64.decodeBase64(fileData), "UTF-8");
		// return new String(Base64.decodeBase64(fileData), "UTF-8");
	}

	public void setContent(String content) throws XPathExpressionException {
		XMLNode dataNode = this.getRootNode().selectSingleNode("/stylesheetresource/filedata");
		dataNode.setTextContent(Base64.encodeBase64String(content.getBytes()).replace("\r", ""));
		
	}

	public static String create(String databaseDocumentId, String name) throws Exception {
		DxlImporter importer = null;
		try {
			// Designer is case-sensitive too
			if (!name.endsWith(".css")) {
				name = name + ".css";
			}

			InputStream is = Stylesheet.class
					.getResourceAsStream("/org/openntf/eclipse/xdpe/publisher/frostillicus/dxl/stylesheet.xml");
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			StringBuilder xmlBuilder = new StringBuilder();
			while (reader.ready()) {
				xmlBuilder.append(reader.readLine());
				xmlBuilder.append("\n");
			}
			is.close();
			String xml = xmlBuilder.toString().replace("name=\"\"", "name=\"" + FNVUtil.xmlEncode(name) + "\"");

			importer = ExtLibUtil.getCurrentSession().createDxlImporter();
			importer.setDesignImportOption(DxlImporter.DXLIMPORTOPTION_REPLACE_ELSE_CREATE);
			importer.setReplicaRequiredForReplaceOrUpdate(false);
			Document databaseDoc = ExtLibUtil.getCurrentDatabase().getDocumentByUNID(databaseDocumentId);
			Database foreignDB = ExtLibUtil.getCurrentSessionAsSignerWithFullAccess()
					.getDatabase(databaseDoc.getItemValueString("Server"), databaseDoc.getItemValueString("FilePath"));
			importer.importDxl(xml, foreignDB);

			Document importedDoc = foreignDB.getDocumentByID(importer.getFirstImportedNoteID());
			return importedDoc.getUniversalID();
		} catch (Exception e) {
			e.printStackTrace();
			if (importer != null) {
				System.out.println(importer.getLog());
			}
		}
		return null;
	}
}
