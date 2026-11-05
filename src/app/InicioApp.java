package app;

import backup.ProcesoBackup;
import controlador.GestionVistas;

/**
 * Arranque de la APLICACION (el cliente). Antes hay que arrancar el servidor
 * (servidor.Servidor): la aplicacion ya no se conecta a Firebase, se lo pide
 * todo al servidor por socket (clase Cliente).
 *
 * Este main tambien es el del proceso secundario del backup: la aplicacion
 * se lanza a si misma con ProcessBuilder y el argumento "backup"
 * (GestionVistas.lanzarBackup). Con ese argumento no se abren ventanas: solo
 * se hace el backup y el proceso termina.
 */
public class InicioApp {

	public static void main(String[] args) {
		if (args.length > 0 && args[0].equals("backup")) {
			// Proceso secundario: java ... app.InicioApp backup
			ProcesoBackup.hacerBackup();
		} else {
			// Aplicacion normal
			GestionVistas vista = new GestionVistas();
			vista.abrirInicio();
		}
	}

}
