package servidor;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import controlador.GestionBD;

/**
 * SERVIDOR de GymApp. Es un programa aparte: se arranca ANTES que la
 * aplicacion (Run As > Java Application sobre esta clase).
 *
 * Es el unico que se conecta a Firebase (con assets/firebase/
 * credenciales-firebase.json). La aplicacion de escritorio (el cliente) le
 * pide las cosas por un Socket TCP en formato JSON.
 *
 * Funcionamiento:
 *  1. Se conecta a Firebase.
 *  2. Abre un ServerSocket en el PUERTO y espera (accept) a que se conecte
 *     un cliente.
 *  3. Cada conexion la atiende un hilo nuevo (HiloCliente), asi puede
 *     atender a varios clientes a la vez. Mientras, vuelve a esperar.
 */
public class Servidor {

	// Tiene que ser el mismo puerto que el de la clase Cliente
	public static final int PUERTO = 5000;

	public static void main(String[] args) {
		GestionBD gestionBD = new GestionBD();
		if (gestionBD.accesoFirebase() == null) {
			System.out.println("No se ha podido conectar con Firebase: el servidor no arranca");
			return;
		}

		// Solo para cargar los datos iniciales en Firebase (workouts del JSON,
		// usuarios del CSV e historicos del XML). Ya estan cargados.
		// gestionBD.datosInicialesFB();

		// try con recursos: el ServerSocket se cierra solo si hay un error
		try (ServerSocket servidor = new ServerSocket(PUERTO)) {
			System.out.println("Servidor GymApp esperando clientes en el puerto " + PUERTO);

			while (true) {
				// accept() se queda esperando hasta que un cliente se conecta
				Socket socket = servidor.accept();
				HiloCliente hilo = new HiloCliente(socket, gestionBD);
				hilo.start();
			}
		} catch (IOException e) {
			// Por ejemplo si el puerto ya esta ocupado (el servidor ya estaba abierto)
			System.out.println("Error en el servidor: " + e.getMessage());
		}
	}
}
