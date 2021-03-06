package controllers;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityTransaction;

import org.joda.time.DateTime;

import com.google.gson.Gson;

import messages.Messages;
import models.JsonPeticionModificacion;
import models.Participacion;
import models.Registro;
import models.RegistroModificacion;
import models.SolicitudGenerica;
import play.db.jpa.JPA;
import play.db.jpa.Model;
import play.mvc.Util;
import tables.TableRecord;
import tags.ComboItem;
import utils.ModelUtils;
import utils.PeticionModificacion;
import utils.PeticionModificacion.ValorCampoModificado;
import controllers.fap.JustificacionFapController;
import controllers.fap.ModificacionFAPController;
import controllers.gen.ActivarModificacionSolicitudesControllerGen;
import enumerado.fap.gen.EstadosModificacionEnum;
import enumerado.fap.gen.TiposParticipacionEnum;

public class ActivarModificacionSolicitudesController extends ActivarModificacionSolicitudesControllerGen {
	
	public static List<ComboItem> fechaARestaurar() {
		List<ComboItem> result = new ArrayList<ComboItem>();
		Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
		if ((ids == null) || (ids.get("idSolicitud") == null))
			return result;
		
		SolicitudGenerica solicitud = getSolicitudGenerica(ids.get("idSolicitud"));
		int i=1;
		ComboItem ultimoRegistrado = null;
		if ((solicitud.registro != null) && (solicitud.registro.fasesRegistro.registro) && (!solicitud.registroModificacion.isEmpty()))
			ultimoRegistrado = new ComboItem(solicitud.registroModificacion.get(0).id.toString(), solicitud.registro.informacionRegistro.fechaRegistro.toString("dd/MM/yyyy")+" Presentación Inicial");
		for (RegistroModificacion rm: solicitud.registroModificacion){
			if ((rm.registro != null) && (rm.registro.fasesRegistro.registro))
				ultimoRegistrado = new ComboItem(rm.id.toString(), rm.registro.informacionRegistro.fechaRegistro.toString("dd/MM/yyyy")+" "+String.valueOf(i)+"º Modificación");
			i++;
		}
		result.add(ultimoRegistrado);
		return result;
	}
	
	@Util
	// Este @Util es necesario porque en determinadas circunstancias crear(..) llama a editar(..).
	public static void formRestaurarModificacion(Long idSolicitud, SolicitudGenerica solicitud, String restaurarBtn) {
		checkAuthenticity();
		Long idRegistroModificacion = null;
		if (!permisoFormRestaurarModificacion("editar")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}

		if (!Messages.hasErrors()) {
			Long idRecuperar = null;
			if (!solicitud.fechaARestaurarStr.isEmpty())
			 idRegistroModificacion = Long.parseLong(solicitud.fechaARestaurarStr);
			if (idRegistroModificacion != null) {
				SolicitudGenerica dbSolicitud = SolicitudGenerica.findById(idSolicitud);
				boolean recuperarPresentacion = true;
				for (RegistroModificacion rm: dbSolicitud.registroModificacion){
					//Comprobar si se han creado elementos nuevos que haya que borrar
					if ((rm.estado.equals(EstadosModificacionEnum.expirada.name())) ||(rm.estado.equals(EstadosModificacionEnum.enCurso.name()))){
						rm.enRecuperacion = true;
						idRecuperar = rm.id;
						ModelUtils.restaurarBorrados(rm.id, idSolicitud);
						ModelUtils.restaurarSolicitud(idRecuperar, idSolicitud, false);
					}
					rm.enRecuperacion = false;
				}			
				for (RegistroModificacion rm: dbSolicitud.registroModificacion){
					if (rm.estado.equals(EstadosModificacionEnum.expirada.name()) || rm.estado.equals(EstadosModificacionEnum.enCurso.name())){
						rm.enRecuperacion = true;
						ModelUtils.eliminarCreados(rm.id, idSolicitud);
					}
					rm.enRecuperacion = false;
				}
				ModelUtils.finalizarDeshacerModificacion(idSolicitud);
				if (!Messages.hasErrors()) {
					try {
						ModificacionFAPController.invoke(ModificacionFAPController.class, "postRestaurarSolicitud", idSolicitud);
					}catch (Throwable e1) {
						log.error("Hubo un problema al invocar el métodos postRestaurarSolicitud: "+e1);
						Messages.error("Error al postrestaurarlasolicitud");
					}
					if ((dbSolicitud.solicitante.isPersonaFisica()) && (!dbSolicitud.solicitante.representado)){ //esto es si es física
						Participacion par = Participacion.find("select participacion from Participacion participacion where participacion.tipo=? and participacion.solicitud.id=?", TiposParticipacionEnum.representante.name(), idSolicitud).first();
						if (par != null){
							par.delete();
						}
					}
				}
				
				log.info("Acción Editar de página: " + "gen/ActivarModificacionSolicitudes/ActivarModificacionSolicitudes.html" + " , intentada con éxito");
			} else {
				Messages.error("Hubo un fallo al intentar recuperar el Registro correspondiente");
			}
		} else
			log.info("Acción Editar de página: " + "gen/ActivarModificacionSolicitudes/ActivarModificacionSolicitudes.html" + " , intentada sin éxito (Problemas de Validación)");
		ActivarModificacionSolicitudesController.formRestaurarModificacionRender(idSolicitud);
	}

	
	public static void tablatablaModificaciones(Long idSolicitud) {

		java.util.List<RegistroModificacion> rows = RegistroModificacion.find("select registroModificacion from SolicitudGenerica solicitud join solicitud.registroModificacion registroModificacion where solicitud.id=?", idSolicitud).fetch();

		List<RegistroModificacion> rowsFiltered = new ArrayList<RegistroModificacion>();
		Map<String, Long> ids = (Map<String, Long>) tags.TagMapStack.top("idParams");
		for (RegistroModificacion registroModificacion : rows) {
			Map<String, Object> vars = new HashMap<String, Object>();
			vars.put("registroModificacion", registroModificacion);
			if (secure.checkAcceso("filaTablaModificaciones", "leer", ids, vars)) {
				rowsFiltered.add(registroModificacion);
			}
		}

		tables.TableRenderResponse<RegistroModificacion> response = new tables.TableRenderResponse<RegistroModificacion>(rowsFiltered, false, false, false, "crearYEditarModificacionSolicitud", "", "adminOrGestorOrRevisor", getAccion(), ids);

		for (TableRecord<RegistroModificacion> registroModificacion : response.rows) {
			if ((registroModificacion.objeto.estado.equals("enCurso")) || (registroModificacion.objeto.estado.equals("expirada"))) {
				registroModificacion.permisoEditar = true;
			} else {
				registroModificacion.permisoEditar = false;
				registroModificacion.permisoLeer = false;
			}
		}
			
		renderJSON(response.toJSON("fechaCreacion", "fechaRegistro", "fechaCancelacion", "fechaLimite", "estadoValue", "registro.justificante.enlaceDescarga", "id"));
	}

}
