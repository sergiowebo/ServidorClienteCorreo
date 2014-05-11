package util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Iterator;
import java.util.StringTokenizer;

import model.email;

public class SMTPenvio {

	// Constantes utilizadas en la conexion
	private final static String RFC_220 = "220";
	private final static String RFC_HELO = "HELO ";
	private final static String RFC_250 = "250";
	private final static String RFC_MAIL_FROM = "MAIL FROM: ";
	private final static String RFC_RCPT_TO = "RCPT TO: ";
	private final static String RFC_DATA = "DATA";
	private final static String RFC_354 = "354";
	private final static String RFC_QUIT = "QUIT";
	private static final String RFC_221 = "221";
	private static final String RFC_CRLF = "\r\n";
	
	// Variables utilizadas en la conexion
	private int puerto;
	private String servidor;
	private Socket conexion;
	
	// Entrada y salida de datos
	private BufferedReader entrada;
	private PrintWriter salida;
	
	// Email a enviar
	private email emailParaEnviar;
	
	// Constructor de la clase que incializa los parametros que vamos a enviar
	public SMTPenvio(String servidor, int puerto, email em){
		super();
		this.servidor = servidor;
		this.puerto = puerto;
		this.emailParaEnviar = em;
	}
	
	public String enviarEmail(){
		
		try {
			// Creamos la conexion con el servidor SMTP
			creaConexion();
			
			/* Cuando nos conectamos al sevidor recibimos un mensaje de bienvenida.
			 * A diferencia del resto de comandos este se recibe sin necesidad de enviar algo.
			 *  - Codigo del mensaje 220
			 */
			// Recibimos el mensaje
			String respuesta = entrada.readLine();
			System.out.println("- Servidor: "+respuesta);
			
			// Comprobamos si es correcto
			if (!respuesta.startsWith(RFC_220)){
				throw new Exception(respuesta.substring(4));
			}
			
			// HELO
			enviarDatoRespuesta(RFC_HELO + getMyPrivateIP(), RFC_250);
			
			// MAIL FROM:
			enviarDatoRespuesta(RFC_MAIL_FROM + emailParaEnviar.getDe(), RFC_250);
			
			// RCPT TO: (Todas las veces necesarias)
			Iterator<String> i = emailParaEnviar.getTodosPara().iterator();
			while(i.hasNext()){
				// TODO: Puede que algunos destinatarios sean incorrectos, pero el email continua para los correctos.
				enviarDatoRespuesta(RFC_RCPT_TO + "<" + i.next()+">", RFC_250);
			}
			
			// DATA
			enviarDatoRespuesta(RFC_DATA, RFC_354);
			
			// CABECERA + MENSAJE
			enviarDatoRespuesta(emailParaEnviar.getMensaje(), RFC_250);
	
			// Cerramos la conexion
			closeSMTP();
			
		} catch (Exception e) {
			System.out.println("Ha ocurrido un error inesperado");
			System.out.println("Finalizamos la conexion");
			closeSMTP();
			return e.getMessage();
		}
		return "OK";
	}
	
	/* 
	 * Envia el comando al servidor, obtiene la respuesta y la compara con el resultado esperado
	 *  - Sino es el resultado esperado lanza excepcion con el mensaje del servidor
	 *  - Si es el mensaje esperado no pasa nada y se continua 
	 */
	private void enviarDatoRespuesta(String dato, String codigoRFC) throws Exception{
		// Enviamos el dato
		System.out.println("- Cliente: "+dato);
		salida.println(dato);
		
		// Obtenemos la respuesta
		String respuesta = entrada.readLine();
		System.out.println("- Servidor: "+respuesta);

		// Comprobamos si es correcta
		if (!respuesta.startsWith(codigoRFC)){
			throw new Exception(respuesta.substring(4));
		}
	}
	
	/*
	 * Crea e inicializa las variables de la conexion
	 */
	private void creaConexion() throws Exception{
		// Creamos la conexion con el servidor SMTP
		conexion = new Socket(servidor, puerto);
		System.out.println("Iniciando conexion: "+ servidor+":"+puerto);
		
		// Iniciamos los buffers de lectura - escritura
		entrada = new BufferedReader(new InputStreamReader(conexion.getInputStream()));
		salida = new PrintWriter(conexion.getOutputStream(), true);
	}
	

	/* Cerramos la conexion
	 *  Primero a nivel de SMTP
	 */
	private void closeSMTP() {
		try {
			enviarDatoRespuesta(RFC_QUIT, RFC_221);
			closeSocket();
		} catch (Exception e) {
			System.out.println("No se ha podido cerrar la conexion via SMTP: " + e.getMessage());
			closeSocket();
		}
	}
	
	/* Cerramos la conexion
	 *  Luego a nivel de socket
	 */
	private void closeSocket() {
		try {
			conexion.close();
			entrada = null;
			salida = null;
			System.out.println("Cerramos la conexion");
		} catch (IOException e) {
			System.out.println("Error cerrando socket");
		}
	}
	
	// Destructor. Cierra la conexion si algo va mal
	protected void finalize() throws Throwable {
		try {
			System.out.println("Cerramos el socket");
			closeSocket();
		} catch (Exception e){
			System.out.println("Error finalizando");
		}
		super.finalize();
	}
	
	/*
	 * Devuelve la IP privada donde se ejecuta el cliente
	 */
	private String getMyPrivateIP(){
		try{
			InetAddress ownIP=InetAddress.getLocalHost();
			return(ownIP.getHostAddress());
		}catch (Exception e){
			System.out.println("Exception caught ="+e.getMessage());
		}
		return "";
	}
}
