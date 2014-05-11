package smtp;


import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.io.*;

import smtp.MensajesRespuesta;

public class ServidorPop {
	
	// Conexion servidor
	//TODO:Cambiar puerto a 110
	private static int puerto = 110;
	private static Socket sConex = null;

    private static BufferedReader reciboCliente;
    private static PrintWriter envioCliente;
    
    //TODO:Cambiar a ruta definitiva 
    private static String PATH = "/home/ftp/redes/bandeja";

    public static void main(String args[]) {
    	ServerSocket sServ;
	
    	try {
    		// crear el socket de escucha
    		boolean restartConnection  = false;
    		
    		while(!restartConnection){
    			sServ = new ServerSocket(puerto);
	
				// aceptar una conexion y crear el socket para la transmision con el cliente
				sConex = sServ.accept();
				//System.out.println(MensajesRespuesta.READY);
	
				// inicializamos los buffers para leer y escribir
				try {
					envioCliente = new PrintWriter(sConex.getOutputStream(), true);
	    			reciboCliente = new BufferedReader(new InputStreamReader(sConex.getInputStream()));
				}catch (IOException e) {
					e.printStackTrace();
				}
				responder(MensajesRespuesta.READY);
	
	    		// flag para indicar que el cliente cierra la conexion
	    		boolean closeConnection = false;
	    		
	    		// Variable para guardar el siguiente comando del cliente
	    		String siguienteLineaCliente = ""; 
	    		//Inicializar variables
	    		String usuario = "";
	    		String user_cliente = "";
	    		String pass_cliente = "";
	    		Map<String, String> credenciales = new HashMap<String, String>();
	    		credenciales.put("jlgalindo", "1234");
	    		credenciales.put("scastellote", "1234");
	    		credenciales.put("scantalapiedra", "1234");
	    		
	    		boolean flag_pass = false;
	
	    		while(!closeConnection) {
	    			siguienteLineaCliente = obtenerDato();
	    			
	    			if(siguienteLineaCliente.startsWith(MensajesRespuesta.QUIT)){
						responder(MensajesRespuesta.OK + " Signing off");
						usuario = "";
			    		user_cliente = "";
			    		pass_cliente = "";
			    		flag_pass = false;
			    		restartConnection = false;
			    		closeConnection = true;
			    		sServ.close();
					}else if(siguienteLineaCliente.startsWith(MensajesRespuesta.STOP)){
						closeConnection = true;
						restartConnection = true;
					}else if(usuario.equals("")){
	    				if (siguienteLineaCliente.startsWith(MensajesRespuesta.USER)) {
	    					responder(MensajesRespuesta.PASS_REQ);
	    					flag_pass = true;
	    					user_cliente = siguienteLineaCliente.substring(5);
	    				}else if(siguienteLineaCliente.startsWith(MensajesRespuesta.PASS) && flag_pass == true){
	    					boolean auth = false;
	    					//Proceso de autenticacion
	    					pass_cliente = siguienteLineaCliente.substring(5);
	    					if(pass_cliente.equals(credenciales.get(user_cliente))){
	    						auth = true;
	    					}else{
	    						auth = false;
	    					}
	    					if(auth == true){
	    						responder(MensajesRespuesta.LOGIN_OK);
	    						usuario = user_cliente;
	    					}else{
	    						responder(MensajesRespuesta.LOGIN_FAILED);
	    						flag_pass = false;
	    					}
	    				}else{
	    					responder(MensajesRespuesta.NOT_LOGGED);
	    					flag_pass = false;
	    				}
	    			}else{
	    				File dir = new File(PATH + usuario);
	    				String[] ficheros = dir.list();
		    			if (siguienteLineaCliente.startsWith(MensajesRespuesta.STAT)) {
		    				//Estado del servidor
		    				int num_mensajes = 0;
		    				long tamano_mensajes = 0;
		    				if (ficheros != null){
		    					for (int x=0;x<ficheros.length;x++){
		    						num_mensajes++;
		    						String mensaje = PATH + usuario + "/" + ficheros[x];
		    						File fichero = new File(mensaje);
		    						tamano_mensajes = tamano_mensajes + fichero.length();
		    					}
	    					}
							responder(MensajesRespuesta.OK + " " + num_mensajes + " " + tamano_mensajes);
						}else if(siguienteLineaCliente.startsWith(MensajesRespuesta.LIST)){
							responder(MensajesRespuesta.OK);
							if (ficheros != null){
		    					for (int x=0;x<ficheros.length;x++){
		    						String mensaje = PATH + usuario + "/" + ficheros[x];
		    						File fichero = new File(mensaje);
		    						responder(ficheros[x] + " " + fichero.length());
		    					}
							}
							responder(".");
						}else if(siguienteLineaCliente.startsWith(MensajesRespuesta.RETR)){
							String id_mensaje = siguienteLineaCliente.substring(5);
							String mensaje = PATH + usuario + "/" + id_mensaje;
							
							File fichero = new File(mensaje);
							if(fichero.exists()){
								responder(MensajesRespuesta.OK);
								try {
							       FileReader fr = new FileReader (mensaje);
							       BufferedReader br = new BufferedReader(fr);
							 
							       String linea;
							       while((linea=br.readLine())!=null){
							      	 responder(linea);
							       }
							       fr.close();
							    }
							    catch(Exception e){
							       e.printStackTrace();
							    }
								//responder(".");
							}else{
								responder(MensajesRespuesta.MENS_NOT_FOUND);
							}
						}else if(siguienteLineaCliente.startsWith(MensajesRespuesta.DELE)){
							String id_mensaje = siguienteLineaCliente.substring(5);
							String mensaje = PATH + usuario + "/" + id_mensaje;
							
							File fichero = new File(mensaje);
							if(fichero.exists()){
								fichero.delete();
								responder(MensajesRespuesta.MENS_DELE);
							}else{
								responder(MensajesRespuesta.MENS_NOT_DELE);
							}
						}else{
							responder(MensajesRespuesta.ERROR);
						}
	    			}
	    		}
	    		try {
	    			sConex.close();
	    		}catch (IOException e) {
	    			e.printStackTrace();
	    		}
	    		// cerrar el socket de escucha
	    		sServ.close();
			}		
    		
    	} catch(IOException e) {
    		System.out.println("Ha ocurrido un error: " + e);
    	}
    	
    } // main
    
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
    		System.out.println(message);

    	return message;
    }
    
	private static void responder (String command){
		if (!sConex.isClosed()){
//			envioCliente.println(command+MensajesRespuesta.CMD_CRLF);
			envioCliente.println(command);
		}
		System.out.println(command);
		return;
	}
    
}

