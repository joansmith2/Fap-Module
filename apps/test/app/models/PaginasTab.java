
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
public class PaginasTab extends Model {
	// Código de los atributos
	
	
	public String nombre;
	
	
	@OneToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	@JoinTable(name="paginastab_tpaginas")
	public List<TablaPaginasTab> tpaginas;
	
	
	public PaginasTab (){
		init();
	}
	

	public void init(){
		
		
						if (tpaginas == null)
							tpaginas = new ArrayList<TablaPaginasTab>();
						
	}
		
	

// === MANUAL REGION START ===
			
// === MANUAL REGION END ===
	
	
	}
		