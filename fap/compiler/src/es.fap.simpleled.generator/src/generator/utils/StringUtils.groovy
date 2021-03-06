package generator.utils

import java.text.Normalizer;
import java.util.regex.Pattern;

class StringUtils {

	private static Pattern patternNoAccent = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
	
	public static String noAccents(String string) {
		String temp = Normalizer.normalize(string, Normalizer.Form.NFD);
		return patternNoAccent.matcher(temp).replaceAll("");
	}
	
	public static String noBlank(String string){
		return string.replaceAll("\\s+", "_");
	}
	
	public static String id(String string){
		String tmp = noAccents(string)
		tmp = noBlank(tmp)
		return tmp;
	}
	
	public static String firstLower(String str) {
		if (str == null || str.equals("")) return str;
		return str.substring(0, 1).toLowerCase() + str.substring(1, str.length());
	}
	
	public static String firstUpper(String str) {
		if (str == null || str.equals("")) return str;
		return str.substring(0, 1).toUpperCase() + str.substring(1, str.length());
	}
	
	public static void appendln(StringBuffer sb, Object content){
		sb.append(content.toString());
		sb.append("\n");
	}
	
	public static boolean isFirstUpper(String str){
		if(str == null) return false;
		return str.charAt(0).isUpperCase();
	}
	
	/*
	* Devuelve una lista bien formada de parámetros, en forma de String en el que cada parámetro va separado por una coma.
	* Ejemplo:
	* 		Entrada:  "Long id"  "Solicitud solicitud"  "Documento doc"
	* 		Salida:   "Long id, Solicitud solicitud, Documento doc"
	* @param params	Lista variable de parámetros. Cada elemento puede ser un String o un List<String>.
	*/
	public static String params(Object ... params){
		String result = "";
		for (Object p: params){
			if (p instanceof String){
				p = [ p ];
			}
			if (p instanceof List<String>){
				for (String s: p){
					if (s != null && !s.trim().equals("")){ 
						if (!result.equals("")){
							result += ", ";
						}
						result += s;
					}
				}
			}
		}
		return result;
	}
	
	public static String getRandomName () {
		Random random = new Random();
		return Long.toHexString(random.nextLong());
	}
}
