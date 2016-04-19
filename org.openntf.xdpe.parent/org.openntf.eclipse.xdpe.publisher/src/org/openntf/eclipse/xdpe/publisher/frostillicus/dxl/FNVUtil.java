package org.openntf.eclipse.xdpe.publisher.frostillicus.dxl;

public class FNVUtil {

	public static String xmlEncode(String text) {
		StringBuilder result = new StringBuilder();

		for(int i = 0; i < text.length(); i++) {
			char currentChar = text.charAt(i);
			if(!((currentChar >= 'a' && currentChar <= 'z') || (currentChar >= 'A' && currentChar <= 'Z') || (currentChar >= '0' && currentChar <= '9'))) {
				result.append("&#" + (int)currentChar + ";");
			} else {
				result.append(currentChar);
			}
		}

		return result.toString();
	}
}
