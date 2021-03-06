package services.comunicacionesInternas;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.tools.corba.common.WSDLUtils;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;

import config.InjectorConfig;
import controllers.fap.AgenteController;
import es.gobcan.platino.servicios.organizacion.DatosBasicosPersonaItem;
import es.gobcan.platino.servicios.organizacion.UnidadOrganicaCriteriaItem;
import es.gobcan.platino.servicios.organizacion.UnidadOrganicaItem;
import es.gobcan.platino.servicios.registro.Asunto;
import es.gobcan.platino.servicios.sfst.FirmaService;
import platino.PlatinoProxy;
import platino.PlatinoSecurityUtils;
import play.modules.guice.InjectSupport;
import properties.FapProperties;
import properties.PropertyPlaceholder;
import messages.Messages;
import models.Agente;
import models.AsientoCIFap;
import models.ListaUris;
import models.RespuestaCIFap;
import models.ReturnUnidadOrganicaFap;
import services.GestorDocumentalService;
import services.VerificarDatosServiceException;
import services.genericos.ServiciosGenericosService;
import services.platino.PlatinoFirmaServiceImpl;
import services.platino.PlatinoGestorDocumentalService;
import swhiperreg.ciservices.ArrayOfString;
import swhiperreg.ciservices.CIServices;
import swhiperreg.ciservices.CIServicesSoap;
import swhiperreg.ciservices.NuevoAsiento;
import swhiperreg.ciservices.ReturnComunicacionInterna;
import swhiperreg.ciservices.ReturnComunicacionInternaAmpliada;
import swhiperreg.entradaservices.EntradaServices;
import swhiperreg.entradaservices.ReturnEntrada;
import swhiperreg.service.ArrayOfReturnUnidadOrganica;
import utils.WSUtils;
import utils.GestorDocumentalUtils;

@InjectSupport
public class ComunicacionesInternasServiceImpl implements ComunicacionesInternasService{

	private CIServicesSoap comunicacionesServices;
	private PropertyPlaceholder propertyPlaceholder;
	private PlatinoGestorDocumentalService platinoGestorDocumental;
	private ServiciosGenericosService genericosService;
	
	private final String URIPROCEDIMIENTO;
	private final String TIPO_TRANSPORTE;
	public final String USUARIOHIPERREG;
	public final String PASSWORDHIPERREG;
	
	@Inject
	public ComunicacionesInternasServiceImpl (PropertyPlaceholder propertyPlaceholder, ServiciosGenericosService genericosService, PlatinoGestorDocumentalService platinoGestorDocumental){
		this.propertyPlaceholder = propertyPlaceholder;
		URL wsdlURL = ComunicacionesInternasService.class.getClassLoader().getResource("wsdl/CIServices.wsdl");
		comunicacionesServices = new CIServices(wsdlURL).getCIServicesSoap();
		WSUtils.configureEndPoint(comunicacionesServices, getEndPoint());
		
		USUARIOHIPERREG = FapProperties.get("fap.platino.registro.username");
		PASSWORDHIPERREG = FapProperties.get("fap.platino.registro.password");
		URIPROCEDIMIENTO = FapProperties.get("fap.platino.security.procedimiento.uri");
		TIPO_TRANSPORTE = FapProperties.get("fap.platino.registro.tipoTransporte");
		
	    Map<String, String> headers = null;
        if ((URIPROCEDIMIENTO != null) && (URIPROCEDIMIENTO.compareTo("undefined") != 0)) {
        	headers = new HashMap<String, String>();
        	headers.put("uriProcedimiento", URIPROCEDIMIENTO);		
        }
		
        WSUtils.configureSecurityHeaders(comunicacionesServices, propertyPlaceholder, headers);
        PlatinoProxy.setProxy(comunicacionesServices, propertyPlaceholder);
        this.platinoGestorDocumental = new PlatinoGestorDocumentalService(propertyPlaceholder);
        this.genericosService = genericosService;
	}
	
	private String getEndPoint() {
		return propertyPlaceholder.get("fap.services.comunicaciones.internas.url");
	}
	
	private boolean hasConnection() {
		boolean hasConnection = false;
		try {
			
			if (USUARIOHIPERREG != null && PASSWORDHIPERREG != null && !"undefined".equals(USUARIOHIPERREG) && !"undefined".equals(PASSWORDHIPERREG))
				hasConnection = genericosService.validarUsuario(USUARIOHIPERREG, PASSWORDHIPERREG);
			play.Logger.info("El servicio tiene conexion con " + getEndPoint() + "?: "+hasConnection);
		}catch(Exception e){
			e.printStackTrace();
			play.Logger.info("El servicio no tiene conexion con " + getEndPoint());
		}
		return hasConnection; 
	}
	
	private String encriptarPassword(String password){
        try {
            return PlatinoSecurityUtils.encriptarPasswordComunicacionesInternas(password);
        } catch (Exception e) {
            throw new RuntimeException("Error encriptando la contraseña");
        }	    
	}
	
	public boolean isConfigured(){
	    return hasConnection();
	}
	
	@Override
    public void mostrarInfoInyeccion() {
		if (isConfigured())
			play.Logger.info("El servicio de Comunicaciones Internas ha sido inyectado con Hiperreg y está operativo.");
		else
			play.Logger.info("El servicio de Comunicaciones Internas ha sido inyectado con Hiperreg y NO está operativo.");
    }

	/**
	 * Método que crea un nuevo asiento de una comunicación interna.
	 * Parámetros obligatorios: (userId, password, resumen y si se especifica asunto no es necesario
	 * especificar unidad orgánica de destino en caso contrario si es obligatorio.  
	 */
	@Override
	public RespuestaCIFap crearNuevoAsiento(AsientoCIFap asientoFap) throws ComunicacionesInternasServiceException {
		
		ArrayOfString listaUris = null;
		try{
			listaUris = obtenerUriPlatino(asientoFap.uris);
		}catch(Exception e){
			play.Logger.error("Se ha producido el error obteniendo las uris de los documentos de platino: " + e.getMessage());
			throw new ComunicacionesInternasServiceException("No se ha podido recuperar las uris de los documentos de platino");
		}
		
		ReturnComunicacionInterna respuesta= null;
		try{	        	 
			respuesta = comunicacionesServices.nuevoAsiento(asientoFap.observaciones, 
					asientoFap.resumen,
					asientoFap.numeroDocumentos,
					asientoFap.interesado,
					asientoFap.unidadOrganicaDestino.codigo,
					asientoFap.asuntoCodificado,
					asientoFap.userId,
					encriptarPassword(asientoFap.password),
					asientoFap.tipoTransporte,
					listaUris);
			
		}
		catch(Exception e1){
			play.Logger.error("Se ha producido un error al enviar la comunicación interna: " + e1.getMessage());
			throw new ComunicacionesInternasServiceException("Se ha producido un error al enviar la comunicación interna");
		}
		
		RespuestaCIFap respuestafap = null;
		try {
			respuestafap = ComunicacionesInternasUtils.respuestaComunicacionInterna2respuestaComunicacionInternaFap(respuesta);
		}catch(Exception e2){
			play.Logger.error("Se ha producido un error al parsear la respuesta de la comunicación interna: " + e2.getMessage());
			throw new ComunicacionesInternasServiceException("Se ha producido un error al parsear la respuesta de la comunicación interna");
		}
		
		return respuestafap;
	}
	
	/**
	 * Método que crea un nuevo asiento de una comunicación interna.
	 * Parámetros obligatorios: (userId, password, resumen, si se especifica asunto no es necesario
	 * especificar unidad orgánica de destino en caso contrario si es obligatorio y unidad orgánica de origen.  
	 */
	@Override
	public RespuestaCIFap crearNuevoAsientoAmpliado(AsientoCIFap asientoAmpliadoFap) throws ComunicacionesInternasServiceException{
		
		ArrayOfString listaUris = null;
		try{
			listaUris = obtenerUriPlatino(asientoAmpliadoFap.uris);
		}catch(Exception e){
			play.Logger.error("Se ha producido el error obteniendo las uris de los documentos de platino: " + e.getMessage());
			throw new ComunicacionesInternasServiceException("No se ha podido recuperar las uris de los documentos de platino");
		}
		
		ReturnComunicacionInternaAmpliada respuesta = null;
		try{
			respuesta =	comunicacionesServices.nuevoAsientoAmpliado(
					asientoAmpliadoFap.observaciones, 
					asientoAmpliadoFap.resumen,
					asientoAmpliadoFap.numeroDocumentos,
					asientoAmpliadoFap.interesado,
					asientoAmpliadoFap.unidadOrganicaDestino.codigo,
					asientoAmpliadoFap.asuntoCodificado,
					asientoAmpliadoFap.userId,
					encriptarPassword(asientoAmpliadoFap.password),
					asientoAmpliadoFap.tipoTransporte,
					listaUris,
					asientoAmpliadoFap.unidadOrganicaOrigen.codigo);	
		}
		catch(Exception e1){
			play.Logger.error("Se ha producido un error al enviar la comunicación interna: " + e1.getMessage());
			throw new ComunicacionesInternasServiceException("Se ha producido un error al enviar la comunicación interna");
		}
		
		RespuestaCIFap respuestafap = null;
		try {
			respuestafap = ComunicacionesInternasUtils.respuestaComunicacionInternaAmpliada2respuestaComunicacionInternaAmpliadaFap(respuesta);
		}catch (Exception e2){
			play.Logger.error("Se ha producido un error al parsear la respuesta de la comunicación interna: " + e2.getMessage());
			throw new ComunicacionesInternasServiceException("Se ha producido un error al parsear la respuesta de  la comunicación interna");
		}
		
		return respuestafap;
	}
	
	/**
	 * Método que comprueba si un documento está en platino, que en caso contrario si existe en el aed de aciisi lo sube a platino,
	 * y devuelve la uri del aed de platino.
	 * @param uris
	 * @return
	 * @throws ComunicacionesInternasServiceException 
	 */
	private ArrayOfString obtenerUriPlatino(List<ListaUris> uris) throws ComunicacionesInternasServiceException {
		ArrayOfString listaUris = null;
		
		for (int i = 0; i < uris.size(); i++){
			String uriPlatino = platinoGestorDocumental.obtenerURIPlatino(uris.get(i).uri, PlatinoGestorDocumentalService.class);
			if ((uriPlatino != null) && (!uriPlatino.isEmpty())) {
				if (listaUris == null)
					listaUris = new ArrayOfString();
				listaUris.getString().add(uriPlatino);
			}
			else {
				play.Logger.error("Error al obtener la uri de platino del documento con uri "+ uris.get(i).uri);
				throw new ComunicacionesInternasServiceException("Error al obtener la uri de platino del documento con uri "+ uris.get(i).uri);
			}
		}
		
		return listaUris;
	}

}
