package smtpserver;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static java.nio.file.StandardOpenOption.*;

import model.email;
import model.usuario;

import smtpserver.MensajesRespuesta;

public class ServidorSMTP implements Runnable{
	
	// Conexion servidor
	//private static ServerSocket sServ;
	private static int puerto = 25;
	private static Socket sConex = null;

	// Buffers para enviar y recibir al cliente
    private static BufferedReader reciboCliente;
    private static PrintWriter envioCliente;

    // Usuarios
	private static ArrayList<usuario> usuarios = new ArrayList();	
	
	// Varibales para introducir el contenido en el fichero 
	private static email emailRecibido = new email();
    
	public ServidorSMTP(Socket incoming) {
		this.sConex = incoming;
	}
	
    public static void ejecuta() {
       	// Creamos los usuarios
		creaUsuariosDemo();
		
    	try {
    		// crear el socket de escucha
    		//sServ = new ServerSocket(puerto);

			// aceptar una conexion y crear el socket para la transmision con el cliente
			System.out.println("Servidor STMP: IP " + getMyPrivateIP() + " " + " Puerto " + puerto);
			System.out.println("----------------------");
			System.out.println("Esperando peticiones");
			//sConex = sServ.accept();

			// inicializamos los buffers para leer y escribir
			envioCliente = new PrintWriter(sConex.getOutputStream(), true);
    		reciboCliente = new BufferedReader(new InputStreamReader(sConex.getInputStream()));

    		// Se envia el mensaje de bienvenida al cliente
			responder(MensajesRespuesta.RFC_220_SERVERUP);
    		
    		// flag para indicar que el cliente cierra la conexion
    		boolean closeConnection = false;
    		
    		// Variable para guardar el siguiente comando del cliente
    		String siguienteLinaCliente = ""; 
    		
    		// Flags para el orden del mensaje
    		boolean envioData = false;
			String emailCompleto = "";
			boolean envioHelo = false;
			boolean envioFrom = false;
			boolean envioTo = false;
			
			// Path donde se deja el correo 
			String Path = "c:/..../..../";  

			// Comienza el bucle hasta que se cierra la conexion
    		while(!closeConnection) {
    			
    			// Leemos dato del cliente
    			siguienteLinaCliente = obtenerDato();

    			// Se utiliza para almacenar todas las lineas del mensaje
    			if (envioData) {
    				emailCompleto += siguienteLinaCliente + "\r\n";
				}  		
    			
    			// HELO
    			if (siguienteLinaCliente.startsWith(MensajesRespuesta.RFC_HELO) ||
    					siguienteLinaCliente.startsWith(MensajesRespuesta.RFC_EHLO)) {
    				
    				// Miramos si han incluido algo ademas del HELO
    				if (siguienteLinaCliente.length() > 5){
    					responder(MensajesRespuesta.RCC_250_SERVERREADY.replace("<domain>", siguienteLinaCliente.substring(5)));
    					envioHelo = true;
    				} else {
    					responder(MensajesRespuesta.RFC_501_HELO);
    				}
    				
    			// MAIL FROM:
    			} else if (siguienteLinaCliente.startsWith(MensajesRespuesta.RFC_MAIL_FROM)) {    			
	    				if (envioHelo){
		    				try{
			    				String mailFrom = siguienteLinaCliente.substring(11).trim();
			    				
			    				if (esEmail(mailFrom)){
			    					emailRecibido.setDe(mailFrom);
			    					envioFrom = true;
			    					responder(MensajesRespuesta.RFC_250_OK);
		    					} else {
		    						responder(MensajesRespuesta.RFC_501);
		    					}
			    				
							} catch (Exception e){
								responder(MensajesRespuesta.RFC_501);
							}
	    				} else {
	    					responder(MensajesRespuesta.RFC_503_HELO);
	    				}
				
	    		// RCPT TO:
				} else if (siguienteLinaCliente.startsWith(MensajesRespuesta.RFC_RCPT_TO) ) {
					if (envioFrom){
						try{
							String rcpTo = siguienteLinaCliente.substring(
									siguienteLinaCliente.indexOf("<")+1, siguienteLinaCliente.indexOf(">"));
														
							// Validamos el correo y tambien sabremos a quien va dirigido
							envioTo = true;
							if (esEmail(rcpTo)){
								if(esNuestraIP(rcpTo, getMyPrivateIP())){
									if (existeUsuario(rcpTo)){
										envioTo = true;
										emailRecibido.addPara(rcpTo);
										responder(MensajesRespuesta.RFC_250_OK);
									} else {
										// No existe el usuario
										responder(MensajesRespuesta.RFC_550.replace(
												"<usuario>",
												rcpTo.substring(0,rcpTo.indexOf("@"))));
									}
								} else {
									// No es nuestra IP
									//responder(MensajesRespuesta.RFC_554);
									envioTo = true;
									emailRecibido.addPara(rcpTo);
									responder(MensajesRespuesta.RFC_250_OK_RELAY);
								}
							} else {
								// Email incorrecto
								responder(MensajesRespuesta.RFC_501);
							}
	    					
						} catch (Exception e){
	    					responder(MensajesRespuesta.RFC_501);
						}
						
					} else {
						// No se respeta el orden de la secuencia de envío de las tramas 
						if (envioHelo)
							responder(MensajesRespuesta.RFC_503_HELO);
						else if (envioFrom)
							responder(MensajesRespuesta.RFC_503_MAILFROM);
						else
							responder(MensajesRespuesta.RFC_503);
					}	
					
				// DATA
				} else if (siguienteLinaCliente.startsWith("DATA"))  {
					if (envioTo==true) {
						if (emailRecibido.getTodosPara().isEmpty()){
							responder(MensajesRespuesta.RFC_554_none);
						} else {
							envioData=true;
							responder(MensajesRespuesta.RFC_354_DATASTART);
						}
					} else {
						// No se respeta el orden de la secuencia de envío de las tramas 
						if (envioHelo)
							responder(MensajesRespuesta.RFC_503_HELO);
						else if (envioFrom)
							responder(MensajesRespuesta.RFC_503_MAILFROM);
						else if (envioTo)
							responder(MensajesRespuesta.RFC_503_RCPTTO);
						else
							responder(MensajesRespuesta.RFC_503);
					}
				
				// Fin de DATA <CRLF>.<CRLF>
				} else if (siguienteLinaCliente.equals("."))  {
					emailRecibido.setMensaje(emailCompleto);
						
					// Guardar fichero;
					guardaEmail(emailRecibido);

					responder(MensajesRespuesta.RFC_250_OK);
					
					// Vacio ArrayList almacenaTo y reseteo los flags
		    		envioData = false;
					emailCompleto = "";
					envioFrom = false;
					envioTo = false;
					emailRecibido = new email();
					
		 		} else if (siguienteLinaCliente.startsWith(MensajesRespuesta.RFC_QUIT)) {
    				closeConnection=true;
    				
				} else {
					if (!envioData)
						responder(MensajesRespuesta.RFC_500);
					// no hace nada
				}
    		}

    		try {
				sConex.close();
			}catch (IOException e) {
				e.printStackTrace();
			}
    		// cerrar el socket de escucha
    		//sServ.close();
    		
    	} catch(IOException e) {
    		System.out.println("Ha ocurrido un error: " + e);
    	}
    	
    }
    
    private static String obtenerDato(){
    	String message = "";

    	try {
    		message = reciboCliente.readLine();
    		message.toUpperCase();
    	}catch (IOException e) {
    		//e.printStackTrace();
    		System.out.println("Error - Empezamos la conexion de nuevo");
    		message = "";
			return "";
    	}

    	if (!message.isEmpty())
    		System.out.println("[Cliente]" + message);

    	return message;
    }
    
	private static void responder (String command){
		if (!sConex.isClosed())
			envioCliente.println(command);
		System.out.println("[Servidor]" + command);
		return;
	}
	
	/*
	 * Devuelve la IP privada donde se ejecuta el cliente
	 */
	private static String getMyPrivateIP(){
		try{
			InetAddress ownIP=InetAddress.getLocalHost();
			return(ownIP.getHostAddress());
		}catch (Exception e){
			System.out.println("Exception caught ="+e.getMessage());
		}
		return "";
	}
	
    // Crea los usuarios del servidor
	public static void creaUsuariosDemo(){
		creaUsuario("jlgalindo","1234");
		creaUsuario("scantalapiedra","1234");
		creaUsuario("scastellote","1234");
		
		// Recorremos el arrayList de los usuarios y los mostramos
		System.out.println("----------------------");
		System.out.println("Los usuarios del sistema son:");
		Iterator<usuario> i = usuarios.iterator();
		while(i.hasNext()){
			usuario tmpUsuario = i.next();
			System.out.println("Nombre: " + tmpUsuario.getNombre() + " Password: " +tmpUsuario.getPass());
		}
		System.out.println("----------------------");
	}
	
    public static void creaUsuario (String nombre, String pass){
    	usuarios.add(new usuario(nombre, pass));
    	File directorio = new File("/home/ftp/redes/bandeja/"+nombre);
    	if (!directorio.exists())
    		directorio.mkdir();
    }
    
    public static void guardaEmail(email emailEnviado){
    	try {
    		Iterator<String> i = emailEnviado.getTodosPara().iterator();
    		
    		while(i.hasNext()){
    			try {
    				String tempUser = i.next();
    				
    				// Decidimos si el email es para nuestro servidor o para el relay
    				if (!esNuestraIP(tempUser, getMyPrivateIP())){
    					email emRelay = new email(emailEnviado.getDe(), tempUser, "", "", emailEnviado.getMensaje());
    					
    		    		envioRelay envRel = new envioRelay();
    		    		envRel.enviar(emRelay);
    					
    				} else {
		    			//String tempUser = i.next();
		    			tempUser = tempUser.substring(0, tempUser.indexOf("@"));
		    			
		    			String sDirectorio = "/home/ftp/redes/bandeja/"+tempUser;
		    			String nombreFichero = "";
		    			File f = new File(sDirectorio);
		    			
		    			if (f.exists()){ // Directorio existe 
		    				File[] ficheros = f.listFiles();
		    				nombreFichero = ficheros.length+1 + "";
		    			}
		    			
		    			File archivo = new File (sDirectorio + "/" +nombreFichero);
		    			if (!archivo.exists()){
		    				BufferedWriter bw = new BufferedWriter(new FileWriter(archivo));
		    				bw.write(emailEnviado.getMensaje());            
		    				bw.close();
		    			} else {
		    				System.out.println("El archivo ya existe");
		    			}
    				}
    			} catch (Exception e){
    				
    			}
			
    		}
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }
    
	/*
	 *  Método que valída el formato de una dirección MAIL TO: 
	 */
    public static boolean esEmail(String correo) {
        if (correo.matches("[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?\\.)+[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?")){
        	return true;
        }       
        return false;
    }
    
    /*
     * Nos dice si esta IP es de nuestro servidor
     */
    public static boolean esNuestraIP (String correo, String servidor){
    	String ipCorreo = correo;
    	String ipServidor = servidor;
    	try{
    		ipCorreo = ipCorreo.substring(ipCorreo.indexOf("@")+1);
    		if (ipCorreo.equals(ipServidor))
    			return true;
    		else 
    			return false;
    	} catch (Exception e) {
    		return false;
    	}
    }
    
    /*
     * Nos dice si el usuario existe en nuestro sistema
     */
    public static boolean existeUsuario (String correo) {
    	try {
    		String usuarioCorreo = correo.substring(0, correo.indexOf("@"));
    		Iterator<usuario> i = usuarios.iterator();
    		
    		while(i.hasNext()){
    			usuario tempUs = i.next();
    			if (tempUs.getNombre().equals(usuarioCorreo))
    				return true;
    		}
    		return false;
    	} catch (Exception e) {
    		return false;
    	}
    }

	@Override
	public void run() {
		// TODO Auto-generated method stub
		ejecuta();
	}
    
}

