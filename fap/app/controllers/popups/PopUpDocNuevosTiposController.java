package controllers.popups;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;

import messages.Messages;
import messages.Messages.MessageType;
import models.Documento;
import models.SolicitudGenerica;
import services.GestorDocumentalService;
import services.aed.ProcedimientosService;
import services.filesystem.TipoDocumentoGestorDocumental;
import tags.ComboItem;
import config.InjectorConfig;
import controllers.gen.popups.PopUpDocNuevosTiposControllerGen;
import es.gobcan.eadmon.gestordocumental.ws.tiposdocumentos.dominio.TipoDocumento;

public class PopUpDocNuevosTiposController extends PopUpDocNuevosTiposControllerGen {

	public static List<ComboItem> documento_tipo() {
		List<ComboItem> result = new ArrayList<ComboItem>();
		Map <String, Long> parametrosUrl = (Map<String, Long>)tags.TagMapStack.top("idParams");
		SolicitudGenerica solicitud = getSolicitudGenerica(parametrosUrl.get("idSolicitud"));
		GestorDocumentalService gestorDocumental = InjectorConfig.getInjector().getInstance(GestorDocumentalService.class);
		List <TipoDocumentoGestorDocumental> tiposDocumentos = gestorDocumental.getListTiposDocumentosAportadosCiudadano (solicitud.verificacion.tramiteNombre);
		for (TipoDocumentoGestorDocumental tDoc: tiposDocumentos){
			result.add(new ComboItem(tDoc.getUri(), tDoc.getDescripcion()));
		}
		return result;
	}
	
	public static void editar(Long idSolicitud, Long idDocumento, Documento documento) {
		checkAuthenticity();
		if (!permiso("editar")) {
			Messages.error("No tiene suficientes privilegios para acceder a esta solicitud");
		}
		Documento dbDocumento = PopUpDocNuevosTiposController.getDocumento(idSolicitud, idDocumento);
		SolicitudGenerica solicitud = getSolicitudGenerica(idSolicitud);
		
		PopUpDocNuevosTiposController.PopUpDocNuevosTiposBindReferences(documento);

		if (!Messages.hasErrors()) {
			PopUpDocNuevosTiposController.PopUpDocNuevosTiposValidateCopy("editar", dbDocumento, documento);

		}

		if (!Messages.hasErrors()) {
			PopUpDocNuevosTiposController.editarValidateRules(dbDocumento, documento);
		}
		if (!Messages.hasErrors()) {
			dbDocumento.save();
			solicitud.verificacion.fechaUltimaActualizacion = new DateTime();
			solicitud.save();
			log.info("Acción Editar de página: " + "gen/popups/PopUpDocNuevosTipos.html" + " , intentada con éxito");
		} else
			log.info("Acción Editar de página: " + "gen/popups/PopUpDocNuevosTipos.html" + " , intentada sin éxito (Problemas de Validación)");
		PopUpDocNuevosTiposController.editarRender(idSolicitud, idDocumento);
	}
	
	public static Documento getDocumento(Long idSolicitud, Long idDocumento) {
		Documento documento = null;

		if (idSolicitud == null) {
			if (!Messages.messages(MessageType.FATAL).contains("Falta parámetro idSolicitud"))
				Messages.fatal("Falta parámetro idSolicitud");
		}

		if (idDocumento == null) {
			if (!Messages.messages(MessageType.FATAL).contains("Falta parámetro idDocumento"))
				Messages.fatal("Falta parámetro idDocumento");
		}
		if (idSolicitud != null && idDocumento != null) {
			documento = Documento.find("select documento from SolicitudGenerica solicitud join solicitud.verificacion.nuevosDocumentos documento where solicitud.id=? and documento.id=?", idSolicitud, idDocumento).first();
			if (documento == null){
				documento = Documento.findById(idDocumento);
				if(documento == null)
					Messages.fatal("Error al recuperar Documento");
			}
		}
		return documento;
	}
	
}
