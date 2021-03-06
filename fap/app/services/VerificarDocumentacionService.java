package services;

import java.util.ArrayList;
import java.util.List;

import properties.FapProperties;

import messages.Messages;
import verificacion.ObligatoriedadDocumentosFap;
import models.DocumentoExterno;
import models.TableKeyValue;
import models.Documento;
import models.Tramite;

/* Clase encargada de verificar que los documentos obligatorios, han sido aportados por el usuario.
 * Si esto no se cumple, mostrará los mensajes de error pertinentes acerca de aquellos tipos de documentos
 * que no ha aportado, para que pueda hacerlo, antes de proseguir con la fase en la que estaba
 */

public class VerificarDocumentacionService {

	// Clase que contendrá, en principio, todos los documentos obligatorios de dicho trámite
	//                    , y finalmente, todos los documentos obligatorios que el usuario NO ha aportado
	private static ObligatoriedadDocumentosFap docObligatoriedad;
	
	// Lista con todos los documentos que el usuario ha aportado
	private static List<Documento> lstDocumentosSubidos=new ArrayList<Documento>();
	
	// Lista con todos los documentos que el usuario dice haber aportado externamente
	private static List<DocumentoExterno> lstDocumentosExternos=new ArrayList<DocumentoExterno>();

	// Constructor de la clase, que tiene dos parametros:
	// 		* tramite: De tipo Tramite, y especifica el trámite que se está utilizando, para saber a partir de él los documentos obligatorios
	// 		* lstDocumentosSubidos: Lista que contiene los documentos que el usuario a aportado   
	//      * lstDocumentosExternos: Lista que contiene los documentos que el usuario dice que ha aportado externamente a la aplicacion
	public VerificarDocumentacionService(Tramite tramite, List<Documento> lstDocumentosSubidos, List<DocumentoExterno> lstDocumentosExternos) {
		// Se calculan los documentos obligatorios a adjuntar, a raíz del trámite que nos llega por parámetros
		this.docObligatoriedad=new ObligatoriedadDocumentosFap(tramite);
		// Guardamos en una variable local a la clase, la lista de documentos que el usuario ha aportado, para despues comparar
		// con la lista de documentos obligatorios al trámite (docObligatoriedad), y así ver si falta o no algun documento obligatorio a aportar 
		this.lstDocumentosSubidos=lstDocumentosSubidos;
		if (FapProperties.getBoolean("fap.documentacion.documentosExternos"))
			this.lstDocumentosExternos=lstDocumentosExternos;
	}
	
	// Constructor de la clase, que tiene dos parametros:
	// 		* tramite: De tipo String, y servirá para especificas el trámite que se está utilizando, para saber a partir de él los documentos obligatorios
	//			** Es de tipo String, ya que después se conocerá el trámite en sí, consultando en la base de datos por el nombre del trámite que será este string
	// 		* lstDocumentosSubidos: Lista que contiene los documentos que el usuario a aportado 
	//      * lstDocumentosExternos: Lista que contiene los documentos que el usuario dice que ha aportado externamente a la aplicacion
	public VerificarDocumentacionService(String strTramite, List<Documento> lstDocumentosSubidos, List<DocumentoExterno> lstDocumentosExternos) {
		// Se calculan los documentos obligatorios a adjuntar, a raíz del trámite que nos llega por parámetros
		this.docObligatoriedad=new ObligatoriedadDocumentosFap(strTramite);
		// Guardamos en una variable local a la clase, la lista de documentos que el usuario ha aportado, para despues comparar
		// con la lista de documentos obligatorios al trámite (docObligatoriedad), y así ver si falta o no algun documento obligatorio a aportar 
		this.lstDocumentosSubidos=lstDocumentosSubidos;
		if (FapProperties.getBoolean("fap.documentacion.documentosExternos"))
			this.lstDocumentosExternos=lstDocumentosExternos;
	}

	public VerificarDocumentacionService(Tramite tramite, List<Documento> lstDocumentosSubidos) {
		this.docObligatoriedad=new ObligatoriedadDocumentosFap(tramite);
		this.lstDocumentosSubidos=lstDocumentosSubidos;
	}
	
	public VerificarDocumentacionService(String tramite, List<Documento> lstDocumentosSubidos) {
		this.docObligatoriedad=new ObligatoriedadDocumentosFap(tramite);
		this.lstDocumentosSubidos=lstDocumentosSubidos;
	}
	
	/**
	 * Función que se encarga de corroborar que se han aportado todos los documentos necesarios, en caso negativo
	 * prepara los errores correspondientes para mostrarle al usuario
	 */
		
	public static void preparaPresentacionTramite(List<String> lstObligatoriosCondicionadosAutomatico) {
		// Se recalculan los documentos necesarios de obligatoriedad CONDICIONADOS_AUTOMATICOS, para filtrar los que ya han sido aportados
		excluirObligatoriosCondicionadosAutomaticosAportados(lstObligatoriosCondicionadosAutomatico);
		// Se comprueba los tipos de obligatoriedad documentos que el usuario ya ha aportado, para quedarse con los que NO ha aportado
		for (Documento doc : lstDocumentosSubidos) {
			comprobarFirmas(doc);
			if (doc.tipo != null) {
				String tipo = eliminarVersionUri(doc.tipo);
				// Si recorriendo todos los documentos que el usuario ha aportado
				// Es posible eliminar de la lista de obligatorios, el documento en cuestión,
				// es que es síntoma de que el documento estaba aportado.
				// Sino, por el contrario, el tipo de obligatoriedad documento no se borrará de nuestra lista de 
				// documentos obligatorios, ya que el usuario no lo ha aportado. De esta forma
				// la variable 'docObligatoriedad', después de este proceso, sólo contendrá
				// aquellos tipos de obligatoriedad de documentos que el usuario NO ha aportado y lo debería haber hecho.
				if(docObligatoriedad.imprescindibles.remove(tipo))
					continue;
				else if(docObligatoriedad.obligatorias.remove(tipo)) 
					continue;
				else if(docObligatoriedad.automaticas.remove(tipo)) 
					continue;
			}
		}
		
		if (FapProperties.getBoolean("fap.documentacion.documentosExternos")){
			// Hacemos lo mismo que para los documentos aportados, pero ahora con los documentos Externos, que son los documentos que el usuario dice haber aportado en otro órgano
			// Se comprueba los tipos de obligatoriedad documentos que el usuario dice haber aportado externamente, para quedarse con los que NO ha aportado
			for (DocumentoExterno doc : lstDocumentosExternos) {
				if (doc.tipo != null) {
					String tipo = eliminarVersionUri(doc.tipo);
					// Si recorriendo todos los documentos que el usuario dice haber aportado
					// Es posible eliminar de la lista de obligatorios, el documento en cuestión,
					// es que es síntoma de que el documento estaba aportado.
					// Sino, por el contrario, el tipo de obligatoriedad documento no se borrará de nuestra lista de 
					// documentos obligatorios, ya que el usuario no lo ha aportado. De esta forma
					// la variable 'docObligatoriedad', después de este proceso, sólo contendrá
					// aquellos tipos de obligatoriedad de documentos que el usuario NO ha aportado y lo debería haber hecho.
					if(docObligatoriedad.imprescindibles.remove(tipo))
						continue;
					else if(docObligatoriedad.obligatorias.remove(tipo)) 
						continue;
					else if(docObligatoriedad.automaticas.remove(tipo)) 
						continue;
				}
			}
		}

		// Una vez que se tienen todos los tipos de obligatoriedad de documentos obligatorios que el usuario NO ha aportado
		// Se generaran los mensajes de error correspondientes, para que el usuario se percate y subsane
		// este situación, para poder continuar con la fase en la que estaba inmerso.
		
		// Se comprueba si existen documentos de obligatoriedad IMPRESCINDIBLE NO APORTADOS
		if (!docObligatoriedad.imprescindibles.isEmpty()) {
			for (String uri : docObligatoriedad.imprescindibles) {
				TableKeyValue descripcionTabla = (TableKeyValue) TableKeyValue.find("byKLike", "%" + uri + "%").first();
				String descripcion = "No encontrado el Tipo de Documento Imprescindible";
				if (descripcionTabla != null)
					descripcion=descripcionTabla.value;
				else
					play.Logger.error("No encontrado el tipo de documento en BBDD con uri: "+uri);
				// Si NO ha aportado un determinado tipo de obligatoriedad de documento que sí debería haberlo hecho, se genera el error correspondiente
				Messages.error("Error: Pagina Documentación falta el documento \""+ descripcion + "\"");
			}
		}
		// Se comprueba si existen documentos de obligatoriedad OBLIGATORIOS NO APORTADOS
		if (!docObligatoriedad.obligatorias.isEmpty()) {
			for (String uri : docObligatoriedad.obligatorias) {
				TableKeyValue descripcionTabla = (TableKeyValue) TableKeyValue.find("byKLike", "%" + uri + "%").first();
				String descripcion = "No encontrado el Tipo de Documento Obligatorio";
				if (descripcionTabla != null)
					descripcion=descripcionTabla.value;
				else
					play.Logger.warn("No encontrado el tipo de documento en BBDD con uri: "+uri);
				// Si NO ha aportado un determinado tipo de obligatoriedad de documento que sí debería haberlo hecho, se genera el error correspondiente
				Messages.warning("Aviso: Pagina Documentación falta el documento \""+ descripcion + "\"");
			}
		}
		// Se comprueba si existen documentos de obligatoriedad AUTOMATICOS NO APORTADOS
		if (!docObligatoriedad.automaticas.isEmpty()) {
			for (String uri : docObligatoriedad.automaticas) {
				TableKeyValue descripcionTabla = (TableKeyValue) TableKeyValue.find("byKLike", "%" + uri + "%").first();
				String descripcion = "No encontrado el Tipo de Documento Automatico";
				if (descripcionTabla != null)
					descripcion=descripcionTabla.value;
				else
					play.Logger.warn("No encontrado el tipo de documento en BBDD con uri: "+uri);
				// Si NO ha aportado un determinada obligatoriedad de documento que sí debería haberlo hecho, se genera el error correspondiente
				Messages.warning("Aviso: Pagina Documentación falta el documento \""+ descripcion + "\"");
			}
		}
	}

	/* Función que se encarga de meter en la clase que gestiona todos los documentos obligatorios a aportar,
	 * los de obligatoriedad CONDICIONADOS_AUTOMÁTICOS, que el usuario no ha checkeado. Por ello, elimina previamente todos los que
	 * pudiera tener la clase al inicializarse (que lo hace a raíz del trámite), para meter aquellos que se
	 * han calculado previamente, en el controlador que llama a esta Clase. Esto es porque los CONDICIONADOS_AUTOMATICOS
	 * pueden variar entre aplicaciones distintas (dependiendo de las reglas de negocio), por ello a priori no se
	 * sabe qué documentos son los de esta obligatoriedad, y mucho menos cuales son los que el usuario no ha aportado exactamente.
	 * El que llame a este Servicio de VerificarDocumentacionService será el encargado de pasarle como argumento 
	 * a la función 'preparaPresentacionTramite', esa lista de documentos de obligatoriedad CONDICIONADOS_AUTOMATICOS, que el usuario
	 * debe aportar por diversos motivos y no ha hecho, y que tendrá que calcular.
	*/

	static void excluirObligatoriosCondicionadosAutomaticosAportados(List<String> lstExcludeNoVersion){
		docObligatoriedad.automaticas.clear();
		if(!((lstExcludeNoVersion==null) || (lstExcludeNoVersion.size()<1))){
			for (String tipo : lstExcludeNoVersion) {
				docObligatoriedad.automaticas.add(tipo);
			}
		}
	}

	// Para eliminar de la URI, la Versión, que no hará falta en el proceso de verificación de documentación
	static String eliminarVersionUri(String uri){
		return uri.substring(0,uri.length()-4);
	}
	
    public boolean comprobarFirmasDocumentos() {
        List<Documento> docs = lstDocumentosSubidos;
        return comprobarFirmasDocumentos(docs);
    }


    static public boolean comprobarFirmasDocumentos(List<Documento> docs) {
    	boolean firmados = true;
        for (Documento doc : docs) {
            if(!comprobarFirmas(doc)) {
               firmados = false;
            }
        }
        return firmados;
    }

    public static boolean comprobarFirmas(Documento doc) {
		play.Logger.info("Comprobando firmantes de %s", doc);
        boolean salida = true;
        if (propertyAnexosFirmadosActiva() && !docOtroExpediente(doc) && !firmaEsValida(doc)) {
			String descripcion = (doc.descripcionVisible != null) ? "\"" + doc.descripcionVisible + "\"": "aportado";
			Messages.error(String.format("El documento %s no ha sido firmado.", descripcion));
			salida = false;
		}

		return salida;
	}

    private static boolean firmaEsValida(Documento doc) {
        return !doc.getFirma().isEmpty();
    }

    private static boolean documentoFirmadoPorTodos(Documento doc) {
        return (doc.firmantes != null) && (doc.firmantes.hanFirmadoTodos());
    }

    private static Boolean propertyAnexosFirmadosActiva() {
        return FapProperties.getBoolean("fap.documentacion.comprobarAnexosFirmados");
    }
    
    private static Boolean docOtroExpediente(Documento doc){
    	return ((doc != null) && (doc.refAed));
    }
}

