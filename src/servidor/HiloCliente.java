package servidor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import controlador.AdaptadorFecha;
import controlador.GestionBD;
import modelo.Historico;
import modelo.Usuario;
import modelo.Workout;

/**
 * Hilo que atiende UNA conexion de un cliente (el Servidor crea uno nuevo
 * por cada conexion, asi varios clientes pueden pedir cosas a la vez).
 *
 *  1. Lee la peticion del cliente: una linea con un JSON.
 *  2. Mira la "accion" y llama al metodo de GestionBD que toca (Firebase).
 *  3. Contesta con otra linea JSON. Siempre lleva "ok": true o false.
 *  4. Cierra la conexion y el hilo termina.
 *
 * Acciones (C = crear, R = leer, U = actualizar, D = borrar):
 *  LOGIN (R), EXISTE_EMAIL (R), REGISTRO (C), ACTUALIZAR_DATOS (U),
 *  CAMBIAR_PASSWORD (U), ACTUALIZAR_NIVEL (U), BORRAR_USUARIO (D),
 *  OBTENER_USUARIOS (R), OBTENER_WORKOUTS (R), OBTENER_HISTORICO (R),
 *  GUARDAR_HISTORICO (C), BORRAR_HISTORICO (D)
 */
public class HiloCliente extends Thread {

	private Socket socket;
	private GestionBD gestionBD;

	// Gson que sabe pasar las fechas (LocalDate) a texto y al reves
	private Gson gson = AdaptadorFecha.crearGson();

	public HiloCliente(Socket socket, GestionBD gestionBD) {
		this.socket = socket;
		this.gestionBD = gestionBD;
	}

	@Override
	public void run() {
		// try con recursos: el socket se cierra solo al terminar
		try (Socket conexion = socket) {
			BufferedReader entrada = new BufferedReader(
					new InputStreamReader(conexion.getInputStream(), StandardCharsets.UTF_8));
			PrintWriter salida = new PrintWriter(
					new OutputStreamWriter(conexion.getOutputStream(), StandardCharsets.UTF_8), true);

			String linea = entrada.readLine();
			// null: el cliente solo ha comprobado si el servidor esta encendido
			if (linea == null) {
				return;
			}

			JsonObject respuesta;
			try {
				// JSON (texto) -> JsonObject
				JsonObject peticion = gson.fromJson(linea, JsonObject.class);
				respuesta = atenderPeticion(peticion);
			} catch (Exception e) {
				// Peticion mal hecha (falta un dato, JSON roto...)
				System.out.println("Peticion incorrecta: " + e.getMessage());
				respuesta = crearRespuesta(false);
				respuesta.addProperty("mensaje", "Petición incorrecta");
			}
			salida.println(respuesta.toString());

		} catch (IOException e) {
			System.out.println("Error con un cliente: " + e.getMessage());
		}
	}

	/**
	 * Llama al metodo que toca segun la accion de la peticion.
	 */
	private JsonObject atenderPeticion(JsonObject peticion) {
		String accion = peticion.get("accion").getAsString();
		System.out.println("Peticion recibida: " + accion);

		switch (accion) {
		case "LOGIN":
			return login(peticion);
		case "EXISTE_EMAIL":
			return existeEmail(peticion);
		case "REGISTRO":
			return registro(peticion);
		case "ACTUALIZAR_DATOS":
			return actualizarDatos(peticion);
		case "CAMBIAR_PASSWORD":
			return cambiarPassword(peticion);
		case "ACTUALIZAR_NIVEL":
			return actualizarNivel(peticion);
		case "BORRAR_USUARIO":
			return borrarUsuario(peticion);
		case "OBTENER_USUARIOS":
			return obtenerUsuarios();
		case "OBTENER_WORKOUTS":
			return obtenerWorkouts();
		case "OBTENER_HISTORICO":
			return obtenerHistorico(peticion);
		case "GUARDAR_HISTORICO":
			return guardarHistorico(peticion);
		case "BORRAR_HISTORICO":
			return borrarHistorico(peticion);
		default:
			JsonObject respuesta = crearRespuesta(false);
			respuesta.addProperty("mensaje", "Acción desconocida: " + accion);
			return respuesta;
		}
	}

	/**
	 * Respuesta con solo "ok": {"ok":true} o {"ok":false}.
	 */
	private JsonObject crearRespuesta(boolean ok) {
		JsonObject respuesta = new JsonObject();
		respuesta.addProperty("ok", ok);
		return respuesta;
	}

	// Ayudas para leer los datos de la peticion
	private String leerTexto(JsonObject peticion, String campo) {
		return peticion.get(campo).getAsString();
	}

	private int leerEntero(JsonObject peticion, String campo) {
		return peticion.get(campo).getAsInt();
	}

	// ---------------------------------------------------------------
	// Usuarios
	// ---------------------------------------------------------------

	// Respuesta: {"ok":true,"usuario":{...}} o {"ok":false}
	private JsonObject login(JsonObject peticion) {
		Usuario usuario = gestionBD.validarLogin(leerTexto(peticion, "email"), leerTexto(peticion, "password"));
		JsonObject respuesta = crearRespuesta(usuario != null);
		if (usuario != null) {
			// objeto Usuario -> JSON
			respuesta.add("usuario", gson.toJsonTree(usuario));
		}
		return respuesta;
	}

	// Respuesta: {"ok":true,"existe":true/false}
	private JsonObject existeEmail(JsonObject peticion) {
		JsonObject respuesta = crearRespuesta(true);
		respuesta.addProperty("existe", gestionBD.existeEmail(leerTexto(peticion, "email")));
		return respuesta;
	}

	// Respuesta: {"ok":true} si se ha creado (false si el email ya existia)
	private JsonObject registro(JsonObject peticion) {
		LocalDate fecha = LocalDate.parse(leerTexto(peticion, "fechaNacimiento"));
		boolean creado = gestionBD.registrarUsuario(leerTexto(peticion, "nombre"), leerTexto(peticion, "apellidos"),
				leerTexto(peticion, "email"), leerTexto(peticion, "password"), fecha);
		return crearRespuesta(creado);
	}

	private JsonObject actualizarDatos(JsonObject peticion) {
		LocalDate fecha = LocalDate.parse(leerTexto(peticion, "fechaNacimiento"));
		boolean guardado = gestionBD.actualizarDatosUsuario(leerEntero(peticion, "id"), leerTexto(peticion, "nombre"),
				leerTexto(peticion, "apellidos"), fecha, leerTexto(peticion, "email"));
		return crearRespuesta(guardado);
	}

	// Respuesta: {"ok":true,"passwordCifrada":"$2a$12$..."}
	private JsonObject cambiarPassword(JsonObject peticion) {
		String cifrada = gestionBD.actualizarPasswordUsuario(leerEntero(peticion, "id"),
				leerTexto(peticion, "password"));
		JsonObject respuesta = crearRespuesta(cifrada != null);
		if (cifrada != null) {
			respuesta.addProperty("passwordCifrada", cifrada);
		}
		return respuesta;
	}

	private JsonObject actualizarNivel(JsonObject peticion) {
		boolean guardado = gestionBD.actualizarNivelUsuario(leerEntero(peticion, "id"), leerEntero(peticion, "nivel"));
		return crearRespuesta(guardado);
	}

	private JsonObject borrarUsuario(JsonObject peticion) {
		return crearRespuesta(gestionBD.borrarUsuario(leerEntero(peticion, "id")));
	}

	// Respuesta: {"ok":true,"usuarios":[{...},{...}]}
	private JsonObject obtenerUsuarios() {
		ArrayList<Usuario> usuarios = gestionBD.obtenerListadoUsuarios();
		JsonObject respuesta = crearRespuesta(usuarios != null);
		if (usuarios != null) {
			respuesta.add("usuarios", gson.toJsonTree(usuarios));
		}
		return respuesta;
	}

	// ---------------------------------------------------------------
	// Workouts e historico
	// ---------------------------------------------------------------

	// Respuesta: {"ok":true,"workouts":[{...},{...}]} (ok false si no hay)
	private JsonObject obtenerWorkouts() {
		ArrayList<Workout> workouts = gestionBD.obtenerWorkouts();
		JsonObject respuesta = crearRespuesta(!workouts.isEmpty());
		respuesta.add("workouts", gson.toJsonTree(workouts));
		return respuesta;
	}

	// Respuesta: {"ok":true,"historico":[{...},{...}]}
	private JsonObject obtenerHistorico(JsonObject peticion) {
		ArrayList<Historico> historico = gestionBD.obtenerHistoricoUsuario(leerEntero(peticion, "id"));
		JsonObject respuesta = crearRespuesta(true);
		respuesta.add("historico", gson.toJsonTree(historico));
		return respuesta;
	}

	// Respuesta: {"ok":true,"idHistorico":5} (el id con el que se ha guardado)
	private JsonObject guardarHistorico(JsonObject peticion) {
		// JSON -> objeto Historico
		Historico historico = gson.fromJson(peticion.get("historico"), Historico.class);
		boolean guardado = gestionBD.guardarHistorico(leerEntero(peticion, "id"), historico);
		JsonObject respuesta = crearRespuesta(guardado);
		if (guardado) {
			respuesta.addProperty("idHistorico", historico.getId());
		}
		return respuesta;
	}

	private JsonObject borrarHistorico(JsonObject peticion) {
		boolean borrado = gestionBD.borrarHistorico(leerEntero(peticion, "id"), leerEntero(peticion, "idHistorico"));
		return crearRespuesta(borrado);
	}
}
