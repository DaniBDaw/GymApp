package backup;

import java.util.ArrayList;

import controlador.Cliente;
import controlador.GestorFicheros;
import modelo.Usuario;
import modelo.Workout;

/**
 * BACKUP (lo que hace el proceso secundario).
 *
 * No tiene main: en el proyecto solo hay dos (servidor.Servidor y
 * app.InicioApp). La aplicacion lanza OTRO proceso con ProcessBuilder
 * (GestionVistas.lanzarBackup) arrancando su propio main con el argumento
 * "backup": java ... app.InicioApp backup. Ese main ve el argumento y, en vez
 * de abrir las ventanas, llama a hacerBackup(). Asi el backup se ejecuta en
 * otro proceso y la aplicacion no se queda esperando.
 *
 *  1. Pide los datos al servidor por socket, igual que la aplicacion
 *     (Cliente): todos los usuarios, todos los workouts y el historico de
 *     cada usuario.
 *  2. Guarda los ficheros en la carpeta GymAppBackups del Escritorio del
 *     ordenador (fuera del proyecto). Los anteriores se sobreescriben:
 *       - usuarios.dat   -> usuarios (binario, SIN historico)
 *       - workouts.json  -> workouts con ejercicios y series
 *       - historico.json -> SOLO el historico de cada usuario
 *  3. Escribe un mensaje con el resultado (System.out). La aplicacion lo lee
 *     y lo muestra.
 */
public class ProcesoBackup {

	public static void hacerBackup() {
		Cliente cliente = new Cliente();
		GestorFicheros gestorFicheros = new GestorFicheros();

		// 1. Datos del servidor
		ArrayList<Usuario> usuarios = cliente.obtenerUsuarios();
		ArrayList<Workout> workouts = cliente.obtenerWorkouts();
		if (usuarios == null || workouts.isEmpty()) {
			System.out.println("ERROR: no se han podido descargar los datos del servidor");
			return;
		}
		for (Usuario usuario : usuarios) {
			usuario.setHistorico(cliente.obtenerHistoricoUsuario(usuario.getId()));
		}

		// 2. Ficheros (si uno falla, los siguientes no se intentan)
		boolean correcto = gestorFicheros.guardarBackupUsuarios(usuarios)
				&& gestorFicheros.guardarBackupWorkouts(workouts) && gestorFicheros.guardarBackupHistorico(usuarios);

		// 3. Mensaje con el resultado
		if (correcto) {
			System.out.println("OK: backup de " + usuarios.size() + " usuarios y " + workouts.size()
					+ " workouts en " + GestorFicheros.CARPETA_BACKUPS);
		} else {
			System.out.println("ERROR: no se han podido escribir los ficheros de backup");
		}
	}
}
