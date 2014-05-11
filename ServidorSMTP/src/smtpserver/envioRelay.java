package smtpserver;

import javax.swing.JOptionPane;

import util.SMTPenvio;
import model.email;

public class envioRelay {

	// Valores para conectarnos al servidor
	String ipServidor;
	int puertoServidor;
	
	String emailPara = "";
	String enviadoOK;
	
	public boolean enviar(email email){
		
		try{
			emailPara = (String) email.getTodosPara().get(0); 
			
			ipServidor = emailPara.substring(emailPara.indexOf("@")+1);
			puertoServidor = 25;
			
			// Creamos la conexion
			SMTPenvio smtpenvio = new SMTPenvio(ipServidor, puertoServidor, email);
	
			// Conectamos y enviamos el email
			enviadoOK = smtpenvio.enviarEmail();
			
			if (!enviadoOK.equals("OK"))
				System.out.println("Error: "+enviadoOK + ". En el envio a: " + emailPara);
			else 
				return true;
			
		} catch (Exception e) {
			System.out.println("No se ha podido enviar el email en Relay " + emailPara);
		}

		return false;
	}
	
}
