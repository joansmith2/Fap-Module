package services.filesystem;

import java.util.ArrayList;
import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

import es.gobcan.aciisi.portafirma.ws.dominio.ListaDocumentosAedType;
import es.gobcan.aciisi.portafirma.ws.dominio.ListaDocumentosType;
import es.gobcan.aciisi.portafirma.ws.dominio.ObtenerEstadoSolicitudResponseType;
import es.gobcan.aciisi.portafirma.ws.dominio.ObtenerEstadoSolicitudType;
import es.gobcan.aciisi.portafirma.ws.dominio.PrioridadEnumType;
import es.gobcan.aciisi.portafirma.ws.dominio.TipoSolicitudEnumType;
import models.Agente;
import models.ResolucionFAP;
import models.SolicitudFirmaPortafirma;
import services.PortafirmaFapService;
import services.PortafirmaFapServiceException;
import services.responses.PortafirmaCrearSolicitudResponse;
import tags.ComboItem;

public class FileSystemPortafirmaImpl implements PortafirmaFapService {
	
	final static String VERSION = "0.1";

	@Override
	public PortafirmaCrearSolicitudResponse crearSolicitudFirma(ResolucionFAP resolucion) throws PortafirmaFapServiceException{
		// TODO: ¿Qué devolvemos en este mock?
		PortafirmaCrearSolicitudResponse response = new PortafirmaCrearSolicitudResponse();
		response.setIdSolicitud("fakeSolicitud");
		response.setComentarios("fakeComentarios");
		return response;
	}

	@Override
	public PortafirmaCrearSolicitudResponse crearSolicitudFirma(SolicitudFirmaPortafirma solicitudFirmaPortafirma) throws PortafirmaFapServiceException {
		PortafirmaCrearSolicitudResponse response = new PortafirmaCrearSolicitudResponse();
		response.setIdSolicitud("fakeSolicitud");
		response.setComentarios("fakeComentarios");
		return response;
	}
	
	@Override
    public boolean entregarSolicitudFirma(String idSolicitante, String idSolicitud, String comentario) {
        return true;
    }

    @Override
	public void eliminarSolicitudFirma(SolicitudFirmaPortafirma solicitudFirmaPortafirma) throws PortafirmaFapServiceException {
		// TODO Auto-generated method stub

	}

	@Override
	public String obtenerVersion() throws PortafirmaFapServiceException {
		return VERSION;
	}

	@Override
	public boolean comprobarSiSolicitudFirmada(SolicitudFirmaPortafirma solicitudFirmaPortafirma) throws PortafirmaFapServiceException {
		return true;
		//return false;
	}

	public boolean isConfigured() {
		// No necesita configuración
		return true;
	}
	
	@Override
	public void mostrarInfoInyeccion() {
		if (isConfigured())
			play.Logger.info("El servicio de Portafirma ha sido inyectado con FileSystem y está operativo.");
		else
			play.Logger.info("El servicio de Portafirma ha sido inyectado con FileSystem y NO está operativo.");
	}

	@Override
	public List<ComboItem> obtenerUsuariosAdmitenEnvio() throws PortafirmaFapServiceException {
		List<ComboItem> listaCombo = new ArrayList<ComboItem>();
		List<Agente> listaJefes = Agente.find("select agente from Agente agente").fetch();
		for (Agente agente: listaJefes) {
			listaCombo.add(new ComboItem(agente.username, agente.username+" - "+agente.name));
		}
		return listaCombo;
		
	}

	@Override
	public String obtenerEstadoFirma(SolicitudFirmaPortafirma solicitudFirmaPortafirma) throws PortafirmaFapServiceException {
//		ObtenerEstadoSolicitudResponseType estadoSolicitudFirma = new ObtenerEstadoSolicitudResponseType();
//		estadoSolicitudFirma.setEstado("Rechazada");
//		return estadoSolicitudFirma;
		return null;
	}

}
