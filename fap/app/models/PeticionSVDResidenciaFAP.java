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
public class PeticionSVDResidenciaFAP extends PeticionSVDFAP {
	// Código de los atributos

	public void init() {
		super.init();

		postInit();
	}

	// === MANUAL REGION START ===
	
	private final String codigoCertificado = "VDRSFWS02";
	
    public PeticionSVDResidenciaFAP() {
		init();
		this.getAtributos().setCodigoCertificado(codigoCertificado);
	}

	// === MANUAL REGION END ===

}