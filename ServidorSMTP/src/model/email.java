package model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;

public class email {
	
	private String de;
	private ArrayList<String> para;
	private String asunto;
	private String mensaje;
	private String id;
	
	public email() {
		super();
		this.para = new ArrayList();
	}
	
	public email(String de, String para, String cc, String asunto, String mensaje) {
		super();
		this.para = new ArrayList();
		this.de = de.trim();
		this.para.add(para.trim());
		this.asunto = asunto.trim();
		this.mensaje = mensaje;
	}

	public String getDe() {
		return de;
	}

	public void setDe(String de) {
		this.de = de.trim();
	}

	public ArrayList getTodosPara() {
		return para;
	}

	public void setPara(ArrayList para) {
		this.para = para;
	}
	
	public void addPara (String nuevoPara){
		this.para.add(nuevoPara.trim());
	}
	
	public String getAsunto() {
		return asunto;
	}

	public void setAsunto(String asunto) {
		this.asunto = asunto.trim();
	}

	public String getMensaje() {
		return mensaje;
	}

	public void setMensaje(String mensaje) {
		this.mensaje = mensaje;
	}

	public String toString() {
		return mensaje;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
}