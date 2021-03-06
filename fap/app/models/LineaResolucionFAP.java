package models;

import java.util.*;
import javax.persistence.*;
import play.Logger;
import play.db.jpa.JPA;
import play.db.jpa.Model;
import play.data.validation.*;
import org.joda.time.DateTime;
import models.*;
import messages.Messages;
import validation.*;
import audit.Auditable;
import java.text.ParseException;
import java.text.SimpleDateFormat;

// === IMPORT REGION START ===

// === IMPORT REGION END ===

@Entity
public class LineaResolucionFAP extends FapModel {
	// Código de los atributos

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public SolicitudGenerica solicitud;

	@ValueFromTable("estadoLineaResolucion")
	@FapEnum("enumerado.fap.gen.EstadoLineaResolucionEnum")
	public String estado;

	public Double puntuacionBaremacion;

	@Moneda
	public Double importeTotal;

	@Moneda
	public Double importeConcedido;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public Documento docBaremacion;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public Documento docEvaluacionCompleto;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public Documento docEvaluacionCompletoConComentarios;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public Registro registro;

	public Boolean notificada;

	@Transient
	public String visibleNotificada;

	public Boolean generadoOficio;

	@Transient
	public String visibleGeneradoOficio;

	@Transient
	public String importeTotal_formatFapTabla;

	@Transient
	public String importeConcedido_formatFapTabla;

	// Getter del atributo del tipo moneda
	public String getImporteTotal_formatFapTabla() {
		return format.FapFormat.formatMoneda(importeTotal);
	}

	// Getter del atributo del tipo moneda
	public String getImporteConcedido_formatFapTabla() {
		return format.FapFormat.formatMoneda(importeConcedido);
	}

	public LineaResolucionFAP() {
		init();
	}

	public void init() {

		if (docBaremacion == null)
			docBaremacion = new Documento();
		else
			docBaremacion.init();

		if (docEvaluacionCompleto == null)
			docEvaluacionCompleto = new Documento();
		else
			docEvaluacionCompleto.init();

		if (docEvaluacionCompletoConComentarios == null)
			docEvaluacionCompletoConComentarios = new Documento();
		else
			docEvaluacionCompletoConComentarios.init();

		if (registro == null)
			registro = new Registro();
		else
			registro.init();

		if (notificada == null)
			notificada = false;

		if (generadoOficio == null)
			generadoOficio = false;

		postInit();
	}

	// === MANUAL REGION START ===

	/**
	 * Calcula la lista de firmantes para la resolución
	 * @param agente
	 * @return
	 */
	public List<Firmante> calcularFirmantes() {
		Firmantes firmantes = new Firmantes();
		List<Agente> agentes = Agente.find("select agente from Agente agente join agente.roles rol where rol = 'gestor'").fetch();
		for (Agente agente : agentes) {
			Firmante firmante = new Firmante(agente);
			firmantes.todos.add(firmante);
		}

		return firmantes.todos;
	}

	public String getVisibleNotificada() {
		if (notificada)
			return "Sí";
		return "No";
	}

	public String getVisibleGeneradoOficio() {
		if (generadoOficio)
			return "Sí";
		return "No";
	}

	// === MANUAL REGION END ===

}
