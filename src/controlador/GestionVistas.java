package controlador;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import modelo.Ejercicio;
import modelo.Historico;
import modelo.Serie;
import modelo.Usuario;
import modelo.Workout;
import vista.DialogoCuentaAtras;
import vista.DialogoSalir;
import vista.VentanaAjustes;
import vista.VentanaDetalleWorkout;
import vista.VentanaHistorico;
import vista.VentanaInicio;
import vista.VentanaLogin;
import vista.VentanaPrincipal;
import vista.VentanaRegistro;
import vista.VentanaResumen;
import vista.VentanaWorkoutEnCurso;
import vista.VentanaWorkouts;

/**
 * Controlador de la aplicacion: abre las ventanas, escucha sus botones,
 * comprueba los datos y hace los calculos.
 *
 * Los datos NO los coge de Firebase: se los pide al servidor con la clase
 * Cliente (socket + JSON). Si el servidor no contesta, trabaja "sin conexion"
 * con los ficheros de backup (GestorFicheros).
 */
public class GestionVistas {

	// Tiempo que se ve el check antes de empezar la carga (ms)
	private static final int TIEMPO_CHECK = 1500;

	// Textos de la carga falsa: 4 textos x 1250 ms = 5 segundos
	private static final String[] TEXTOS_CARGA = { "Cargando datos del usuario...", "Cargando workouts...",
			"Cargando histórico...", "Casi terminado..." };
	private static final int TIEMPO_POR_TEXTO = 1250;

	// Usuario que ha hecho login (del servidor, del backup o el admin)
	private static Usuario usuarioRegistrado = new Usuario();

	// true si se ha entrado sin conexion con el servidor (login con el backup
	// usuarios.dat o con admin/admin): los datos salen de los ficheros de backup
	// y no se puede guardar nada en el servidor
	private static boolean modoSinConexion = false;

	// Workouts cargados despues del login (todos, sin filtrar)
	private static ArrayList<Workout> listaWorkouts = new ArrayList<Workout>();
	// Workouts que se ven ahora en la tabla (con el filtro aplicado)
	private static ArrayList<Workout> workoutsMostrados = new ArrayList<Workout>();

	// Lo pone a true el hilo de carga cuando termina. "volatile" para que el
	// Timer de la ventana (otro hilo) vea el cambio enseguida.
	private static volatile boolean datosCargados = false;

	// Nivel maximo de un usuario (0 a 4)
	private static final int NIVEL_MAXIMO = 4;
	// Formato de la fecha del ultimo workout en la home
	private static final DateTimeFormatter FORMATO_ULTIMO_WORKOUT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

	private static VentanaInicio ventanaInicio;
	private static VentanaLogin ventanaLogin;
	private static VentanaRegistro ventanaRegistro;
	private static VentanaPrincipal ventanaPrincipal;
	private static VentanaWorkouts ventanaWorkouts;
	private static VentanaDetalleWorkout ventanaDetalle;

	// registro
	private static final int LONGITUD_MINIMA_PASSWORD = 6;
	// Tiempo que se ve el mensaje verde antes de volver al login (ms)
	private static final int TIEMPO_REGISTRO_CORRECTO = 1500;
	// "uuuu" en vez de "yyyy" porque con STRICT "yyyy" no funciona bien
	private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/uuuu")
			.withResolverStyle(ResolverStyle.STRICT);
	private static Timer timerCarga;
	private static int indiceCarga;

	// Para hablar con el servidor (socket + JSON)
	private static Cliente cliente = new Cliente();
	// Para leer los ficheros de backup cuando no hay conexion
	private static GestorFicheros gestorFicheros = new GestorFicheros();

	// Usuario generico para entrar sin servidor y sin backup (por ejemplo si el
	// antivirus del instituto lo bloquea todo). No se busca en ningun sitio.
	private static final String ADMIN_USUARIO = "admin";
	private static final String ADMIN_PASSWORD = "admin";

	public GestionVistas() {

	}

	// ---------------------------------------------------------------
	// Ventana de inicio
	// ---------------------------------------------------------------

	public void abrirInicio() {
		// Nada mas arrancar se hace el backup (si hay conexion con el
		// servidor; si no, se usara el que ya hay para entrar sin conexion)
		lanzarBackup();

		ventanaInicio = new VentanaInicio();

		ventanaInicio.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				irALogin();
			}
		});

		ventanaInicio.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				irALogin();
			}
		});

		ventanaInicio.setVisible(true);
	}

	private static void irALogin() {
		ventanaInicio.cerrar();
		abrirLogin();
	}

	// ---------------------------------------------------------------
	// Ventana de login
	// ---------------------------------------------------------------

	private static void abrirLogin() {
		ventanaLogin = new VentanaLogin();

		// Boton de entrar (tambien se pulsa con Enter)
		ventanaLogin.getBtnEntrar().addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				comprobarLogin();
			}
		});

		ventanaLogin.getBtnRegistrar().addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				abrirRegistro();
				ventanaLogin.cerrar();
			}
		});

		ventanaLogin.setVisible(true);
	}

	// ---------------------------------------------------------------
	// Ventana de registro
	// ---------------------------------------------------------------

	private static void abrirRegistro() {
		ventanaRegistro = new VentanaRegistro();

		// Crear cuenta (tambien se pulsa con Enter)
		ventanaRegistro.getBtnCrearCuenta().addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				comprobarRegistro();
			}
		});

		// Volver al login sin registrarse
		ventanaRegistro.getBtnVolver().addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				abrirLogin();
				ventanaRegistro.cerrar();
			}
		});

		ventanaRegistro.setVisible(true);
	}

	/**
	 * Comprueba los campos del registro en orden y muestra el primer error
	 * que encuentre.
	 */
	private static void comprobarRegistro() {
		String nombre = ventanaRegistro.getNombre().trim();
		String apellidos = ventanaRegistro.getApellidos().trim();
		String email = ventanaRegistro.getEmail().trim();
		String fecha = ventanaRegistro.getFechaNacimiento();
		String password = ventanaRegistro.getPassword();
		String repetirPassword = ventanaRegistro.getRepetirPassword();

		ventanaRegistro.limpiarErrores();

		// 1. Campos obligatorios: se marcan en rojo todos los vacios
		boolean hayVacios = false;
		if (nombre.isEmpty()) {
			ventanaRegistro.marcarError(ventanaRegistro.getTxtNombre());
			hayVacios = true;
		}
		if (apellidos.isEmpty()) {
			ventanaRegistro.marcarError(ventanaRegistro.getTxtApellidos());
			hayVacios = true;
		}
		if (email.isEmpty()) {
			ventanaRegistro.marcarError(ventanaRegistro.getTxtEmail());
			hayVacios = true;
		}
		// La fecha vacia se ve como "__/__/____"
		if (fecha.equals("__/__/____")) {
			ventanaRegistro.marcarError(ventanaRegistro.getTxtFechaNacimiento());
			hayVacios = true;
		}
		if (password.isEmpty()) {
			ventanaRegistro.marcarError(ventanaRegistro.getTxtPassword());
			hayVacios = true;
		}
		if (repetirPassword.isEmpty()) {
			ventanaRegistro.marcarError(ventanaRegistro.getTxtRepetirPassword());
			hayVacios = true;
		}
		if (hayVacios) {
			ventanaRegistro.mostrarError("Rellena todos los campos");
			return;
		}

		// 2. Email con formato basico: algo@algo.algo
		if (!esEmailValido(email)) {
			ventanaRegistro.marcarError(ventanaRegistro.getTxtEmail());
			ventanaRegistro.mostrarError("El email no es válido");
			ventanaRegistro.enfocar(ventanaRegistro.getTxtEmail());
			return;
		}

		// 3. Fecha de nacimiento: que exista y que no sea futura
		LocalDate fechaNacimiento = null;
		try {
			fechaNacimiento = LocalDate.parse(fecha, FORMATO_FECHA);
		} catch (DateTimeParseException e) {
			ventanaRegistro.marcarError(ventanaRegistro.getTxtFechaNacimiento());
			ventanaRegistro.mostrarError("La fecha no es válida (dd/mm/aaaa)");
			ventanaRegistro.enfocar(ventanaRegistro.getTxtFechaNacimiento());
			return;
		}
		if (!fechaNacimiento.isBefore(LocalDate.now())) {
			ventanaRegistro.marcarError(ventanaRegistro.getTxtFechaNacimiento());
			ventanaRegistro.mostrarError("La fecha de nacimiento no puede ser futura");
			ventanaRegistro.enfocar(ventanaRegistro.getTxtFechaNacimiento());
			return;
		}

		// 4. Contrasena con una longitud minima
		if (password.length() < LONGITUD_MINIMA_PASSWORD) {
			ventanaRegistro.marcarError(ventanaRegistro.getTxtPassword());
			ventanaRegistro
					.mostrarError("La contraseña debe tener al menos " + LONGITUD_MINIMA_PASSWORD + " caracteres");
			ventanaRegistro.vaciarPasswords();
			ventanaRegistro.enfocar(ventanaRegistro.getTxtPassword());
			return;
		}

		// 5. Las dos contrasenas tienen que ser iguales
		if (!password.equals(repetirPassword)) {
			ventanaRegistro.marcarError(ventanaRegistro.getTxtPassword());
			ventanaRegistro.marcarError(ventanaRegistro.getTxtRepetirPassword());
			ventanaRegistro.mostrarError("Las contraseñas no coinciden");
			ventanaRegistro.vaciarPasswords();
			ventanaRegistro.enfocar(ventanaRegistro.getTxtPassword());
			return;
		}

		// 6. Para crear la cuenta hace falta el servidor
		if (!cliente.hayConexion()) {
			ventanaRegistro.mostrarError("No hay conexión con el servidor. Inténtalo más tarde");
			return;
		}

		// 7. El email no puede ser de otra cuenta
		email = email.toLowerCase();
		if (cliente.existeEmail(email)) {
			ventanaRegistro.marcarError(ventanaRegistro.getTxtEmail());
			ventanaRegistro.mostrarError("Ese email ya está registrado");
			ventanaRegistro.enfocar(ventanaRegistro.getTxtEmail());
			return;
		}

		// Todo correcto: el servidor crea el usuario (nivel 0) en Firebase
		if (!cliente.registrarUsuario(nombre, apellidos, email, password, fechaNacimiento)) {
			ventanaRegistro.mostrarError("No se ha podido crear la cuenta. Inténtalo de nuevo");
			return;
		}
		System.out.println("Registro correcto: " + email);
		lanzarBackup();
		ventanaRegistro.mostrarRegistroCorrecto("Cuenta creada. Ya puedes iniciar sesión");

		// El mensaje se ve un momento y despues se vuelve al login con el email puesto
		final String emailRegistrado = email;
		Timer timerVolver = new Timer(TIEMPO_REGISTRO_CORRECTO, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				abrirLogin();
				ventanaLogin.setUsuario(emailRegistrado);
				ventanaLogin.enfocarPassword();
				ventanaRegistro.cerrar();
			}
		});
		timerVolver.setRepeats(false);
		timerVolver.start();
	}

	/**
	 * Comprobacion sencilla del email: una sola @, algo antes, y despues de
	 * la @ un punto que no este ni al principio ni al final.
	 */
	private static boolean esEmailValido(String email) {
		int posArroba = email.indexOf('@');
		if (posArroba <= 0 || posArroba != email.lastIndexOf('@')) {
			return false;
		}
		String dominio = email.substring(posArroba + 1);
		int posPunto = dominio.lastIndexOf('.');
		if (posPunto <= 0 || posPunto == dominio.length() - 1) {
			return false;
		}
		if (email.contains(" ")) {
			return false;
		}
		return true;
	}

	/**
	 * Comprueba los campos del login y muestra el error que toque.
	 */
	private static void comprobarLogin() {

		String usuario = ventanaLogin.getUsuario().trim();
		String password = ventanaLogin.getPassword();

		// Se quitan los errores anteriores antes de volver a comprobar
		ventanaLogin.limpiarErrores();

		if (usuario.isEmpty() && password.isEmpty()) {
			ventanaLogin.marcarErrorUsuario();
			ventanaLogin.marcarErrorPassword();
			ventanaLogin.mostrarError("Introduce el usuario y la contraseña");
			ventanaLogin.enfocarUsuario();
		} else if (usuario.isEmpty()) {
			ventanaLogin.marcarErrorUsuario();
			ventanaLogin.mostrarError("El usuario no puede estar vacío");
			ventanaLogin.enfocarUsuario();
		} else if (password.isEmpty()) {
			ventanaLogin.marcarErrorPassword();
			ventanaLogin.mostrarError("La contraseña no puede estar vacía");
			ventanaLogin.enfocarPassword();
		} else if (esUsuarioAdmin(usuario, password)) {
			// Usuario generico admin/admin: entra SIN servidor (por si el
			// antivirus del instituto bloquea la conexion)
			usuarioRegistrado = crearUsuarioAdmin();
			modoSinConexion = true;
			System.out.println("Login admin (modo sin conexión)");
			entrarEnLaAplicacion();
		} else if (cliente.hayConexion()) {
			// CON conexion: el servidor comprueba el email y la contraseña en Firebase
			usuarioRegistrado = cliente.validarLogin(usuario.toLowerCase(), password);

			if (usuarioRegistrado != null) {
				modoSinConexion = false;
				System.out.println("Login correcto");
				// Si hizo workouts sin conexion, se suben ahora al servidor
				// (si sube alguno, se rehace el backup)
				subirPendientes();
				entrarEnLaAplicacion();
			} else {
				ventanaLogin.marcarErrorPassword();
				ventanaLogin.mostrarError("El email o la contraseña no son correctos");
				ventanaLogin.enfocarPassword();
			}
		} else {
			// SIN conexion: se busca el usuario en el backup (usuarios.dat)
			usuarioRegistrado = gestorFicheros.validarLoginSinConexion(usuario, password);

			if (usuarioRegistrado != null) {
				modoSinConexion = true;
				System.out.println("Login sin conexión (backup)");
				entrarEnLaAplicacion();
			} else {
				ventanaLogin.marcarErrorPassword();
				ventanaLogin.mostrarError("Sin conexión: email o contraseña no válidos");
				ventanaLogin.enfocarPassword();
			}
		}
	}

	/**
	 * true si ha escrito el usuario y la contraseña del admin generico.
	 */
	private static boolean esUsuarioAdmin(String usuario, String password) {
		return ADMIN_USUARIO.equals(usuario) && ADMIN_PASSWORD.equals(password);
	}

	/**
	 * Usuario admin: nivel maximo (4) para poder ver todos los workouts.
	 */
	private static Usuario crearUsuarioAdmin() {
		return new Usuario(0, "Admin", "Sin conexión", ADMIN_USUARIO, ADMIN_PASSWORD, LocalDate.of(2000, 1, 1),
				NIVEL_MAXIMO, new ArrayList<Historico>());
	}

	/**
	 * Login correcto: se ve el check un momento y despues empieza la carga.
	 */
	private static void entrarEnLaAplicacion() {
		ventanaLogin.mostrarLoginCorrecto();

		Timer timerEspera = new Timer(TIEMPO_CHECK, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				empezarCargaFalsa();
			}
		});
		timerEspera.setRepeats(false);
		timerEspera.start();
	}

	// ---------------------------------------------------------------
	// Carga despues del login (unos 5 segundos como minimo)
	// Mientras se ven los textos de carga, un hilo aparte trae el historico
	// y los workouts. Si el hilo tarda mas, se espera en "Casi terminado...".
	// ---------------------------------------------------------------

	private static void empezarCargaFalsa() {
		indiceCarga = 0;
		ventanaLogin.mostrarCarga(TEXTOS_CARGA[indiceCarga]);
		cargarDatosEnSegundoPlano();

		timerCarga = new Timer(TIEMPO_POR_TEXTO, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				indiceCarga++;
				if (indiceCarga < TEXTOS_CARGA.length) {
					ventanaLogin.setTextoCarga(TEXTOS_CARGA[indiceCarga]);
				} else if (datosCargados) {
					timerCarga.stop();
					terminarCarga();
				}
				// Si los datos aun no han llegado, el timer sigue y se vuelve a
				// comprobar en la siguiente vuelta
			}
		});
		timerCarga.start();
	}

	/**
	 * Hilo que carga los datos sin congelar la ventana (el servidor tarda unos
	 * segundos en leer Firebase). Sin conexion se leen los ficheros de backup:
	 * workouts.json y el historico de historico.json.
	 */
	private static void cargarDatosEnSegundoPlano() {
		datosCargados = false;

		Thread hiloCarga = new Thread(new Runnable() {
			@Override
			public void run() {
				ArrayList<Workout> workouts;
				if (modoSinConexion) {
					// Historico = el del backup JSON + los workouts hechos sin conexion
					ArrayList<Historico> historico = gestorFicheros
							.obtenerHistoricoSinConexion(usuarioRegistrado.getId());
					historico.addAll(gestorFicheros.leerPendientes(usuarioRegistrado.getId()));
					usuarioRegistrado.setHistorico(historico);
					workouts = gestorFicheros.obtenerWorkoutsSinConexion();
				} else {
					usuarioRegistrado.setHistorico(cliente.obtenerHistoricoUsuario(usuarioRegistrado.getId()));
					workouts = cliente.obtenerWorkouts();
					// Si el servidor no ha devuelto nada, se usan los del backup
					if (workouts.isEmpty()) {
						System.out.println("Sin workouts del servidor: se usan los del backup");
						workouts = gestorFicheros.obtenerWorkoutsSinConexion();
					}
				}
				listaWorkouts = workouts;
				// WORKOUT DE PRUEBA: se anade el primero de la lista
				if (MOSTRAR_WORKOUT_PRUEBA) {
					listaWorkouts.add(0, crearWorkoutPrueba());
				}
				datosCargados = true;
			}
		});
		hiloCarga.start();
	}

	/**
	 * Primero se abre la ventana principal y despues se cierra el login, asi no se
	 * ve el escritorio entre las dos ventanas.
	 */
	private static void terminarCarga() {
		System.out.println("Carga terminada");
		abrirPrincipal();
		ventanaLogin.cerrar();
	}

	// ---------------------------------------------------------------
	// Ventana principal (home)
	// ---------------------------------------------------------------

	private static void abrirPrincipal() {
		ventanaPrincipal = new VentanaPrincipal();

		// Datos reales del usuario que ha hecho login
		ponerDatosUsuario();

		ventanaPrincipal.getBtnWorkouts().addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				abrirWorkouts();
				ventanaPrincipal.cerrar();
			}
		});

		ventanaPrincipal.getBtnHistorial().addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				abrirHistorico(false);
				ventanaPrincipal.cerrar();
			}
		});

		ventanaPrincipal.getBtnAjustes().addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				abrirAjustes();
				ventanaPrincipal.cerrar();
			}
		});

		// Cerrar sesion: se vuelve al login
		ventanaPrincipal.getBtnCerrarSesion().addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				abrirLogin();
				ventanaPrincipal.cerrar();
			}
		});

		ventanaPrincipal.mostrar();
	}

	/**
	 * Pasa a la home el nombre, el nivel, el tiempo total y los workouts hechos del
	 * usuario que ha hecho login.
	 */
	private static void ponerDatosUsuario() {
		ventanaPrincipal.setNombreUsuario(usuarioRegistrado.getNombre());

		// Nivel actual y siguiente (null si ya esta en el maximo)
		int nivel = usuarioRegistrado.getNivel();
		String siguiente = null;
		if (nivel < NIVEL_MAXIMO) {
			siguiente = obtenerNombreNivel(nivel + 1);
		}
		ventanaPrincipal.setNivel(obtenerNombreNivel(nivel), siguiente);

		// Historico: tiempo total, cuantos workouts y la fecha del ultimo
		ArrayList<Historico> historico = usuarioRegistrado.getHistorico();
		if (historico == null) {
			historico = new ArrayList<Historico>();
		}

		int segundosTotales = 0;
		LocalDate ultimaFecha = null;
		for (int i = 0; i < historico.size(); i++) {
			Historico hist = historico.get(i);
			segundosTotales = segundosTotales + convertirASegundos(hist.getTiempoTotal());
			if (hist.getFecha() != null && (ultimaFecha == null || hist.getFecha().isAfter(ultimaFecha))) {
				ultimaFecha = hist.getFecha();
			}
		}

		ventanaPrincipal.setTiempoTotal(formatearTiempoTotal(segundosTotales));

		String textoUltimo = null;
		if (ultimaFecha != null) {
			textoUltimo = ultimaFecha.format(FORMATO_ULTIMO_WORKOUT);
		}
		ventanaPrincipal.setEstadisticas(historico.size(), textoUltimo);
	}

	// ---------------------------------------------------------------
	// Ventana de ajustes (datos del usuario y contraseña)
	// ---------------------------------------------------------------

	private static VentanaAjustes ventanaAjustes;

	// ====================================================================
	// PANEL: AJUSTES (abrir la ventana y sus botones)
	// ====================================================================
	private static void abrirAjustes() {
		ventanaAjustes = new VentanaAjustes();
		ventanaAjustes.setUsuario(usuarioRegistrado.getNombre(), obtenerNombreNivel(usuarioRegistrado.getNivel()));
		ponerDatosEnAjustes();

		// Pestaña DATOS DEL USUARIO: se vuelven a poner los datos guardados
		// (si habia cambiado algo sin guardar, se pierde)
		ventanaAjustes.getBtnMenuDatos().addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ventanaAjustes.mostrarDatos();
				ponerDatosEnAjustes();
			}
		});

		ventanaAjustes.getBtnMenuPassword().addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ventanaAjustes.mostrarPassword();
			}
		});

		// GUARDAR (tambien se pulsa con Enter): guarda lo de la pestaña que se ve
		ventanaAjustes.getBtnGuardar().addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (ventanaAjustes.estaEnDatos()) {
					guardarDatosUsuario();
				} else {
					guardarPassword();
				}
			}
		});

		// SALIR: vuelve a la home (con el nombre nuevo si lo ha cambiado)
		ventanaAjustes.getBtnSalir().addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				abrirPrincipal();
				ventanaAjustes.cerrar();
			}
		});

		// ELIMINAR CUENTA: primero se pide confirmacion
		ventanaAjustes.getBtnEliminarCuenta().addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				preguntarEliminarCuenta();
			}
		});

		ventanaAjustes.setVisible(true);
	}

	/**
	 * Pone en la pestaña de datos lo que tiene el usuario ahora. La fecha
	 * se pasa a texto "dd/mm/aaaa" para el campo con mascara.
	 */
	private static void ponerDatosEnAjustes() {
		String fecha = "";
		if (usuarioRegistrado.getFechaNacimiento() != null) {
			fecha = usuarioRegistrado.getFechaNacimiento().format(FORMATO_FECHA);
		}
		ventanaAjustes.setDatos(usuarioRegistrado.getNombre(), usuarioRegistrado.getApellidos(), fecha,
				usuarioRegistrado.getEmail());
	}

	// ====================================================================
	// PANEL: GUARDAR DATOS DEL USUARIO (mismas comprobaciones que el registro)
	// ====================================================================
	/**
	 * Comprueba los campos en orden y muestra el primer error que encuentre.
	 * Si todo esta bien lo guarda en Firebase y en usuarioRegistrado.
	 */
	private static void guardarDatosUsuario() {
		String nombre = ventanaAjustes.getNombre().trim();
		String apellidos = ventanaAjustes.getApellidos().trim();
		String fecha = ventanaAjustes.getFechaNacimiento();
		String email = ventanaAjustes.getEmail().trim().toLowerCase();

		ventanaAjustes.limpiarErrores();

		// 1. Campos obligatorios: se marcan en rojo todos los vacios
		boolean hayVacios = false;
		if (nombre.isEmpty()) {
			ventanaAjustes.marcarError(ventanaAjustes.getTxtNombre());
			hayVacios = true;
		}
		if (apellidos.isEmpty()) {
			ventanaAjustes.marcarError(ventanaAjustes.getTxtApellidos());
			hayVacios = true;
		}
		// La fecha vacia se ve como "__/__/____"
		if (fecha.equals("__/__/____")) {
			ventanaAjustes.marcarError(ventanaAjustes.getTxtFechaNacimiento());
			hayVacios = true;
		}
		if (email.isEmpty()) {
			ventanaAjustes.marcarError(ventanaAjustes.getTxtEmail());
			hayVacios = true;
		}
		if (hayVacios) {
			ventanaAjustes.mostrarError("Rellena todos los campos");
			return;
		}

		// 2. Fecha de nacimiento: que exista y que no sea futura
		LocalDate fechaNacimiento = null;
		try {
			fechaNacimiento = LocalDate.parse(fecha, FORMATO_FECHA);
		} catch (DateTimeParseException e) {
			ventanaAjustes.marcarError(ventanaAjustes.getTxtFechaNacimiento());
			ventanaAjustes.mostrarError("La fecha no es válida (dd/mm/aaaa)");
			ventanaAjustes.enfocar(ventanaAjustes.getTxtFechaNacimiento());
			return;
		}
		if (!fechaNacimiento.isBefore(LocalDate.now())) {
			ventanaAjustes.marcarError(ventanaAjustes.getTxtFechaNacimiento());
			ventanaAjustes.mostrarError("La fecha de nacimiento no puede ser futura");
			ventanaAjustes.enfocar(ventanaAjustes.getTxtFechaNacimiento());
			return;
		}

		// 3. Email con formato basico: algo@algo.algo
		if (!esEmailValido(email)) {
			ventanaAjustes.marcarError(ventanaAjustes.getTxtEmail());
			ventanaAjustes.mostrarError("El email no es válido");
			ventanaAjustes.enfocar(ventanaAjustes.getTxtEmail());
			return;
		}

		// 4. Sin conexion con el servidor no se puede guardar nada
		if (modoSinConexion) {
			ventanaAjustes.mostrarError("Modo sin conexión: no se pueden guardar los cambios");
			return;
		}

		// 5. Si ha cambiado el email, que no lo tenga ya otro usuario
		if (!email.equalsIgnoreCase(usuarioRegistrado.getEmail()) && cliente.existeEmail(email)) {
			ventanaAjustes.marcarError(ventanaAjustes.getTxtEmail());
			ventanaAjustes.mostrarError("Ese email ya lo usa otra cuenta");
			ventanaAjustes.enfocar(ventanaAjustes.getTxtEmail());
			return;
		}

		// Todo correcto: primero se guarda en el servidor (Firebase) y, si sale
		// bien, en el usuario de la aplicacion (asi los dos tienen lo mismo)
		boolean guardado = cliente.actualizarDatosUsuario(usuarioRegistrado.getId(), nombre, apellidos,
				fechaNacimiento, email);
		if (!guardado) {
			ventanaAjustes.mostrarError("No se han podido guardar los datos. Inténtalo de nuevo");
			return;
		}

		usuarioRegistrado.setNombre(nombre);
		usuarioRegistrado.setApellidos(apellidos);
		usuarioRegistrado.setFechaNacimiento(fechaNacimiento);
		usuarioRegistrado.setEmail(email);
		lanzarBackup();

		ventanaAjustes.setUsuario(nombre, obtenerNombreNivel(usuarioRegistrado.getNivel()));
		ventanaAjustes.mostrarCorrecto("Datos guardados");
	}

	// ====================================================================
	// PANEL: CAMBIAR CONTRASEÑA
	// ====================================================================
	/**
	 * Comprueba los tres campos en orden y muestra el primer error que
	 * encuentre. Si todo esta bien cambia la contraseña en Firebase.
	 */
	private static void guardarPassword() {
		String actual = ventanaAjustes.getPasswordActual();
		String nueva = ventanaAjustes.getPasswordNueva();
		String repetir = ventanaAjustes.getRepetirPassword();

		ventanaAjustes.limpiarErrores();

		// 1. Campos obligatorios
		boolean hayVacios = false;
		if (actual.isEmpty()) {
			ventanaAjustes.marcarError(ventanaAjustes.getTxtPasswordActual());
			hayVacios = true;
		}
		if (nueva.isEmpty()) {
			ventanaAjustes.marcarError(ventanaAjustes.getTxtPasswordNueva());
			hayVacios = true;
		}
		if (repetir.isEmpty()) {
			ventanaAjustes.marcarError(ventanaAjustes.getTxtRepetirPassword());
			hayVacios = true;
		}
		if (hayVacios) {
			ventanaAjustes.mostrarError("Rellena todos los campos");
			return;
		}

		// 2. La nueva con una longitud minima (la misma que en el registro)
		if (nueva.length() < LONGITUD_MINIMA_PASSWORD) {
			ventanaAjustes.marcarError(ventanaAjustes.getTxtPasswordNueva());
			ventanaAjustes.mostrarError(
					"La contraseña nueva debe tener al menos " + LONGITUD_MINIMA_PASSWORD + " caracteres");
			ventanaAjustes.enfocar(ventanaAjustes.getTxtPasswordNueva());
			return;
		}

		// 3. La nueva y la repetida tienen que ser iguales
		if (!nueva.equals(repetir)) {
			ventanaAjustes.marcarError(ventanaAjustes.getTxtPasswordNueva());
			ventanaAjustes.marcarError(ventanaAjustes.getTxtRepetirPassword());
			ventanaAjustes.mostrarError("Las contraseñas nuevas no coinciden");
			ventanaAjustes.enfocar(ventanaAjustes.getTxtPasswordNueva());
			return;
		}

		// 4. La nueva no puede ser igual que la actual
		if (nueva.equals(actual)) {
			ventanaAjustes.marcarError(ventanaAjustes.getTxtPasswordNueva());
			ventanaAjustes.mostrarError("La contraseña nueva tiene que ser distinta de la actual");
			ventanaAjustes.enfocar(ventanaAjustes.getTxtPasswordNueva());
			return;
		}

		// 5. Sin conexion con el servidor no se puede guardar nada
		if (modoSinConexion) {
			ventanaAjustes.mostrarError("Modo sin conexión: no se pueden guardar los cambios");
			return;
		}

		// 6. La actual tiene que ser la del usuario: se comprueba en el servidor
		// igual que en el login (en Firebase esta cifrada)
		if (cliente.validarLogin(usuarioRegistrado.getEmail(), actual) == null) {
			ventanaAjustes.marcarError(ventanaAjustes.getTxtPasswordActual());
			ventanaAjustes.mostrarError("La contraseña actual no es correcta");
			ventanaAjustes.vaciarPasswords();
			ventanaAjustes.enfocar(ventanaAjustes.getTxtPasswordActual());
			return;
		}

		// Todo correcto: el servidor la guarda cifrada y tambien el usuario
		String cifrada = cliente.actualizarPasswordUsuario(usuarioRegistrado.getId(), nueva);
		if (cifrada == null) {
			ventanaAjustes.mostrarError("No se ha podido cambiar la contraseña. Inténtalo de nuevo");
			return;
		}
		usuarioRegistrado.setPassword(cifrada);
		lanzarBackup();

		ventanaAjustes.vaciarPasswords();
		ventanaAjustes.mostrarCorrecto("Contraseña cambiada");
	}

	// ====================================================================
	// PANEL: ELIMINAR CUENTA (aviso y borrado)
	// ====================================================================
	/**
	 * Sale el aviso con el fondo borroso (DialogoSalir con el boton rojo).
	 * CANCELAR lo cierra sin hacer nada. ELIMINAR borra la cuenta.
	 */
	private static void preguntarEliminarCuenta() {
		ventanaAjustes.limpiarErrores();

		// Sin servidor no se puede borrar (y el admin no es un usuario real)
		if (modoSinConexion) {
			ventanaAjustes.mostrarError("Modo sin conexión: no se puede eliminar la cuenta");
			return;
		}

		final DialogoSalir dialogo = new DialogoSalir(ventanaAjustes, "¿Eliminar tu cuenta?",
				"Se borrarán tu cuenta y todo tu histórico. Esta acción no se puede deshacer.", "ELIMINAR", true);

		dialogo.getBtnCancelar().addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dialogo.cerrar();
			}
		});

		dialogo.getBtnSalir().addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dialogo.cerrar();
				eliminarCuenta();
			}
		});

		dialogo.setVisible(true);
	}

	/**
	 * El servidor borra el usuario y su historico de Firebase. Si sale bien se
	 * rehace el backup (para que el usuario borrado ya no pueda entrar sin
	 * conexion) y se vuelve al login.
	 */
	private static void eliminarCuenta() {
		if (!cliente.borrarUsuario(usuarioRegistrado.getId())) {
			ventanaAjustes.mostrarError("No se ha podido eliminar la cuenta. Inténtalo de nuevo");
			return;
		}
		System.out.println("Cuenta eliminada: " + usuarioRegistrado.getEmail());
		// Si tenia workouts sin subir, se borran (lista vacia = se borra el fichero)
		gestorFicheros.guardarPendientes(usuarioRegistrado.getId(), new ArrayList<Historico>());
		lanzarBackup();
		usuarioRegistrado = new Usuario();

		abrirLogin();
		ventanaAjustes.cerrar();
	}

	// ---------------------------------------------------------------
	// Ventana de workouts
	// ---------------------------------------------------------------

	private static void abrirWorkouts() {
		ventanaWorkouts = new VentanaWorkouts();
		ventanaWorkouts.setModoSinConexion(modoSinConexion);
		ventanaWorkouts.setUsuario(usuarioRegistrado.getNombre(), usuarioRegistrado.getNivel(),
				obtenerNombreNivel(usuarioRegistrado.getNivel()));

		// Filtro: "TODOS" (hasta su nivel) y despues cada nivel de 0 al del usuario
		int nivelUsuario = usuarioRegistrado.getNivel();
		String[] opciones = new String[nivelUsuario + 2];
		opciones[0] = "TODOS";
		for (int i = 0; i <= nivelUsuario; i++) {
			opciones[i + 1] = obtenerNombreNivel(i).toUpperCase();
		}
		ventanaWorkouts.setOpcionesNivel(opciones);
		mostrarWorkoutsFiltrados();

		// Al cambiar el filtro se vuelve a rellenar la tabla
		ventanaWorkouts.getComboNivel().addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mostrarWorkoutsFiltrados();
			}
		});

		// VER WORKOUT solo se activa si hay una fila elegida
		ventanaWorkouts.getTabla().getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				boolean hayFila = ventanaWorkouts.getFilaSeleccionada() >= 0;
				ventanaWorkouts.getBtnVerWorkout().setEnabled(hayFila);
			}
		});

		ventanaWorkouts.getBtnVerWorkout().addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int fila = ventanaWorkouts.getFilaSeleccionada();
				if (fila >= 0 && fila < workoutsMostrados.size()) {
					Workout elegido = workoutsMostrados.get(fila);
					abrirDetalleWorkout(elegido);
					ventanaWorkouts.cerrar();
				}
			}
		});

		// ATRAS: vuelve a la home
		ventanaWorkouts.getBtnAtras().addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				abrirPrincipal();
				ventanaWorkouts.cerrar();
			}
		});

		// HISTORICO: abre el historico (VOLVER vuelve aqui)
		ventanaWorkouts.getBtnHistorico().addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				abrirHistorico(true);
				ventanaWorkouts.cerrar();
			}
		});

		ventanaWorkouts.setVisible(true);
	}

	/**
	 * Aplica el filtro elegido y pasa las filas a la tabla. TODOS = workouts de su
	 * nivel y de los inferiores. Un nivel = solo ese nivel.
	 */
	private static void mostrarWorkoutsFiltrados() {
		int opcion = ventanaWorkouts.getComboNivel().getSelectedIndex();
		int nivelUsuario = usuarioRegistrado.getNivel();

		workoutsMostrados = new ArrayList<Workout>();
		for (int i = 0; i < listaWorkouts.size(); i++) {
			Workout workout = listaWorkouts.get(i);
			if (opcion <= 0) {
				if (workout.getNivel() <= nivelUsuario) {
					workoutsMostrados.add(workout);
				}
			} else if (workout.getNivel() == opcion - 1) {
				workoutsMostrados.add(workout);
			}
		}

		String[][] filas = new String[workoutsMostrados.size()][5];
		for (int i = 0; i < workoutsMostrados.size(); i++) {
			Workout workout = workoutsMostrados.get(i);
			filas[i][0] = workout.getNombre();
			filas[i][1] = obtenerNombreNivel(workout.getNivel());
			filas[i][2] = Integer.toString(contarEjercicios(workout));
			filas[i][3] = formatearDuracion(calcularDuracionWorkout(workout));
			// Columna Vídeo: el icono solo sale si el workout tiene enlace
			if (workout.getVideoURL() != null && !workout.getVideoURL().isEmpty()) {
				filas[i][4] = VentanaWorkouts.CON_VIDEO;
			} else {
				filas[i][4] = "";
			}
		}
		ventanaWorkouts.cargarWorkouts(filas);
	}

	// ---------------------------------------------------------------
	// Ventana del historico
	// ---------------------------------------------------------------

	private static VentanaHistorico ventanaHistorico;
	// Los workouts en el mismo orden que las filas de la tabla (los mas
	// recientes primero): la fila 3 de la tabla es historicoEnTabla.get(3)
	private static ArrayList<Historico> historicoEnTabla = new ArrayList<Historico>();

	// ====================================================================
	// PANEL: HISTORICO (abrir la ventana con los workouts hechos)
	// ====================================================================
	/**
	 * desdeWorkouts = true si se ha abierto desde la ventana de Workouts
	 * (VOLVER vuelve alli) y false si es desde la home (VOLVER va a la home).
	 */
	private static void abrirHistorico(final boolean desdeWorkouts) {
		ventanaHistorico = new VentanaHistorico();
		ventanaHistorico.setModoSinConexion(modoSinConexion);
		ventanaHistorico.setUsuario(usuarioRegistrado.getNombre(), usuarioRegistrado.getNivel(),
				obtenerNombreNivel(usuarioRegistrado.getNivel()));
		ventanaHistorico.cargarHistorico(crearFilasHistorico());

		// Clic en la X del final de una fila: borrar ese workout del historico
		// (sin conexion la ventana no enseña las X y devuelve -1)
		ventanaHistorico.getTabla().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int fila = ventanaHistorico.getFilaBorrar(e);
				if (fila >= 0) {
					preguntarBorrarHistorico(historicoEnTabla.get(fila));
				}
			}
		});

		ventanaHistorico.getBtnVolver().addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (desdeWorkouts) {
					abrirWorkouts();
				} else {
					abrirPrincipal();
				}
				ventanaHistorico.cerrar();
			}
		});

		ventanaHistorico.setVisible(true);
	}

	/**
	 * Una fila por workout hecho, primero los mas recientes:
	 * { workout, nivel, fecha, tiempo total, tiempo previsto, completado }.
	 * El tiempo previsto NO se guarda: se calcula ahora con las series y los
	 * descansos del workout (igual que en Detalle Workout).
	 */
	private static String[][] crearFilasHistorico() {
		ArrayList<Historico> historico = new ArrayList<Historico>();
		if (usuarioRegistrado.getHistorico() != null) {
			// Copia para ordenar sin cambiar la lista del usuario
			historico.addAll(usuarioRegistrado.getHistorico());
		}
		ordenarHistoricoRecientesPrimero(historico);
		historicoEnTabla = historico;

		String[][] filas = new String[historico.size()][6];
		for (int i = 0; i < historico.size(); i++) {
			Historico hist = historico.get(i);
			filas[i][0] = hist.getNombre();
			filas[i][1] = obtenerNombreNivel(hist.getNivel());
			if (hist.getFecha() != null) {
				filas[i][2] = hist.getFecha().format(FORMATO_ULTIMO_WORKOUT);
			} else {
				filas[i][2] = "--";
			}
			filas[i][3] = hist.getTiempoTotal();
			filas[i][4] = calcularTiempoPrevisto(hist);
			filas[i][5] = hist.getPorcentaje() + "%";
		}
		return filas;
	}

	// ====================================================================
	// PANEL: BORRAR UN WORKOUT DEL HISTORICO (la X de cada fila)
	// ====================================================================
	/**
	 * Sale el aviso con el fondo borroso (DialogoSalir con el boton rojo).
	 * CANCELAR lo cierra sin hacer nada. BORRAR lo borra.
	 */
	private static void preguntarBorrarHistorico(final Historico hist) {
		String fecha = "--";
		if (hist.getFecha() != null) {
			fecha = hist.getFecha().format(FORMATO_ULTIMO_WORKOUT);
		}
		final DialogoSalir dialogo = new DialogoSalir(ventanaHistorico, "¿Borrar del histórico?",
				"Se borrará " + hist.getNombre() + " del " + fecha + ". Esta acción no se puede deshacer.", "BORRAR",
				true);

		dialogo.getBtnCancelar().addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dialogo.cerrar();
			}
		});

		dialogo.getBtnSalir().addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dialogo.cerrar();
				borrarDelHistorico(hist);
			}
		});

		dialogo.setVisible(true);
	}

	/**
	 * El servidor lo borra de Firebase. Si sale bien se quita de la lista del
	 * usuario (asi la home tambien lo deja de contar) y se recarga la tabla.
	 * Si tiene id 0 nunca llego al servidor (esta en pendientes_<id>.json):
	 * se quita de ese fichero para que no se suba en el proximo login.
	 */
	private static void borrarDelHistorico(Historico hist) {
		int idUsuario = usuarioRegistrado.getId();
		if (hist.getId() == 0) {
			ArrayList<Historico> pendientes = gestorFicheros.leerPendientes(idUsuario);
			pendientes.remove(hist);
			gestorFicheros.guardarPendientes(idUsuario, pendientes);
		} else if (cliente.borrarHistorico(idUsuario, hist.getId())) {
			lanzarBackup();
		} else {
			ventanaHistorico.mostrarError("No se ha podido borrar");
			return;
		}
		usuarioRegistrado.getHistorico().remove(hist);
		ventanaHistorico.cargarHistorico(crearFilasHistorico());
	}

	/**
	 * Tiempo previsto del workout del historico: se busca el workout por su
	 * nombre y se calcula su duracion (series + descansos). Si ya no existe,
	 * el que traia guardado el historico (o "--:--").
	 */
	private static String calcularTiempoPrevisto(Historico hist) {
		for (int i = 0; i < listaWorkouts.size(); i++) {
			Workout workout = listaWorkouts.get(i);
			if (workout.getNombre() != null && workout.getNombre().equalsIgnoreCase(hist.getNombre())) {
				return formatearMinutosSegundos(calcularDuracionWorkout(workout));
			}
		}
		if (hist.getTiempoPrevisto() != null && !hist.getTiempoPrevisto().isEmpty()) {
			return hist.getTiempoPrevisto();
		}
		return "--:--";
	}

	/**
	 * Ordena por fecha, la mas reciente primero (metodo de la burbuja). Los que
	 * no tienen fecha van al final.
	 */
	private static void ordenarHistoricoRecientesPrimero(ArrayList<Historico> lista) {
		for (int i = 0; i < lista.size() - 1; i++) {
			for (int j = 0; j < lista.size() - 1 - i; j++) {
				if (esMasAntiguo(lista.get(j), lista.get(j + 1))) {
					Historico aux = lista.get(j);
					lista.set(j, lista.get(j + 1));
					lista.set(j + 1, aux);
				}
			}
		}
	}

	/**
	 * true si "a" es mas antiguo que "b" (tiene que ir detras).
	 */
	private static boolean esMasAntiguo(Historico a, Historico b) {
		if (a.getFecha() == null) {
			return b.getFecha() != null;
		}
		return b.getFecha() != null && a.getFecha().isBefore(b.getFecha());
	}

	// ---------------------------------------------------------------
	// Ventana de detalle de un workout
	// ---------------------------------------------------------------

	private static void abrirDetalleWorkout(final Workout workout) {
		ventanaDetalle = new VentanaDetalleWorkout();
		ventanaDetalle.setUsuario(usuarioRegistrado.getNombre(), usuarioRegistrado.getNivel(),
				obtenerNombreNivel(usuarioRegistrado.getNivel()));

		ventanaDetalle.setTitulo(workout.getNombre());
		ventanaDetalle.setNivel(workout.getNivel(), obtenerNombreNivel(workout.getNivel()));
		ventanaDetalle.setDuracion(formatearMinutosSegundos(calcularDuracionWorkout(workout)),
				contarEjercicios(workout));

		// Una fila por ejercicio: n°, nombre, series, descanso, duracion y estado
		ArrayList<Ejercicio> ejercicios = workout.getEjercicio();
		if (ejercicios == null) {
			ejercicios = new ArrayList<Ejercicio>();
		}
		String[][] filas = new String[ejercicios.size()][6];
		for (int i = 0; i < ejercicios.size(); i++) {
			Ejercicio ejercicio = ejercicios.get(i);
			int numSeries = 0;
			if (ejercicio.getSerie() != null) {
				numSeries = ejercicio.getSerie().size();
			}
			filas[i][0] = Integer.toString(i + 1);
			filas[i][1] = ejercicio.getNombre();
			filas[i][2] = Integer.toString(numSeries);
			filas[i][3] = ejercicio.getDescanso() + " s";
			filas[i][4] = formatearMinutosSegundos(calcularDuracionEjercicio(ejercicio));
			// De momento no se guarda que ejercicios se han hecho: todos pendientes
			filas[i][5] = VentanaDetalleWorkout.ESTADO_PENDIENTE;
		}
		ventanaDetalle.cargarEjercicios(filas);

		// Siguiente = el primer ejercicio pendiente (ahora siempre el primero)
		if (ejercicios.isEmpty()) {
			ventanaDetalle.setSiguiente(null);
		} else {
			ventanaDetalle.setSiguiente("1 - " + ejercicios.get(0).getNombre());
		}

		// VER VIDEO: abre el enlace en el navegador
		ventanaDetalle.getBtnVerVideo().setEnabled(workout.getVideoURL() != null && !workout.getVideoURL().isEmpty());
		ventanaDetalle.getBtnVerVideo().addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					Desktop.getDesktop().browse(new URI(workout.getVideoURL()));
				} catch (Exception ex) {
					System.out.println("No se ha podido abrir el video: " + ex.getMessage());
				}
			}
		});

		// CONTINUAR: empieza el workout
		ventanaDetalle.getBtnContinuar().addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				abrirWorkoutEnCurso(workout);
				ventanaDetalle.cerrar();
			}
		});

		// VOLVER: vuelve a la lista de workouts
		ventanaDetalle.getBtnVolver().addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				abrirWorkouts();
				ventanaDetalle.cerrar();
			}
		});

		ventanaDetalle.setVisible(true);
	}

	// ---------------------------------------------------------------
	// Ventana de workout en curso
	// Hay cuatro cronometros independientes, cada uno en su propio hilo
	// (clase Cronometro, que extiende Thread):
	//  - cronoWorkout:   cuenta hacia ARRIBA el tiempo de todo el workout
	//  - cronoEjercicio: cuenta hacia ARRIBA el tiempo del ejercicio
	//  - cronoSerie:     cuenta hacia ABAJO, primero 5 segundos para
	//                    prepararse y despues el tiempo de la serie
	//  - cronoDescanso:  cuenta hacia ABAJO el descanso entre series
	// Orden de un ejercicio: preparacion -> serie 1 -> descanso ->
	// serie 2 -> descanso -> ... -> ultima serie (despues de la ultima
	// serie no hay descanso: el ejercicio ha terminado). Con
	// CUENTA_ATRAS_EN_CADA_SERIE = true tambien hay preparacion antes de
	// la serie 2, 3...
	// ---------------------------------------------------------------

	// Lo que hace el boton grande en cada momento
	private static final int BOTON_INICIAR = 0;
	private static final int BOTON_PAUSA = 1;
	private static final int BOTON_REANUDAR = 2;
	private static final int BOTON_SIGUIENTE = 3;
	private static final int BOTON_RESUMEN = 4;

	// Lo que se esta contando ahora mismo en la serie
	private static final int FASE_PREPARACION = 0;
	private static final int FASE_SERIE = 1;
	private static final int FASE_DESCANSO = 2;

	// Cuenta atras para que el usuario se prepare antes de la serie
	private static final int SEGUNDOS_PREPARACION = 5;
	// false = la cuenta atras solo sale antes de la PRIMERA serie de cada
	// ejercicio (al pulsar INICIAR); despues de cada descanso la serie
	// siguiente empieza directamente. true = sale antes de CADA serie.
	private static final boolean CUENTA_ATRAS_EN_CADA_SERIE = false;

	private static VentanaWorkoutEnCurso ventanaEnCurso;
	private static Workout workoutEnCurso;
	private static int indiceEjercicio; // ejercicio que se esta haciendo (0 = el primero)
	private static int indiceSerie; // serie que se esta haciendo (0 = la primera)
	private static int fase; // FASE_PREPARACION, FASE_SERIE o FASE_DESCANSO
	private static int accionBoton; // BOTON_INICIAR, BOTON_PAUSA...
	private static Cronometro cronoWorkout;
	private static Cronometro cronoEjercicio;
	private static Cronometro cronoSerie;
	private static Cronometro cronoDescanso;
	// Cuenta atras grande de los 5 segundos (null cuando no se ve)
	private static DialogoCuentaAtras dialogoPreparacion;

	// Datos para el resumen del final
	private static int ejerciciosCompletados;
	private static int[] segundosPorEjercicio; // tiempo de cada ejercicio (0 = no empezado)

	// ====================================================================
	// PANEL: WORKOUT EN CURSO (abrir la ventana)
	// ====================================================================
	private static void abrirWorkoutEnCurso(Workout workout) {
		workoutEnCurso = workout;
		indiceEjercicio = 0;
		ejerciciosCompletados = 0;
		segundosPorEjercicio = new int[workout.getEjercicio().size()];

		ventanaEnCurso = new VentanaWorkoutEnCurso();
		ventanaEnCurso.setUsuario(usuarioRegistrado.getNombre(), usuarioRegistrado.getNivel(),
				obtenerNombreNivel(usuarioRegistrado.getNivel()));
		crearCronometros();
		cargarEjercicio();

		ventanaEnCurso.getBtnAccion().addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				pulsarBotonAccion();
			}
		});

		ventanaEnCurso.getBtnSalir().addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				preguntarSalir();
			}
		});

		ventanaEnCurso.setVisible(true);
	}

	// ====================================================================
	// PANEL: DATOS DEL EJERCICIO Y SERIES (cargar un ejercicio)
	// ====================================================================
	/**
	 * Pone en la ventana el ejercicio numero indiceEjercicio: textos, tarjetas
	 * de las series, el siguiente ejercicio y el cronometro del ejercicio a
	 * cero. El del workout no se toca (sigue contando). Se espera a INICIAR.
	 */
	private static void cargarEjercicio() {
		Ejercicio ejercicio = obtenerEjercicioActual();
		ArrayList<Serie> series = obtenerSeries(ejercicio);

		indiceSerie = 0;
		fase = FASE_PREPARACION;

		ventanaEnCurso.setSubtitulo(workoutEnCurso.getNombre() + " - Ejercicio " + (indiceEjercicio + 1) + " de "
				+ workoutEnCurso.getEjercicio().size());
		ventanaEnCurso.setEjercicio(ejercicio.getNombre(), ejercicio.getDescripcion());

		// Una tarjeta por serie
		String[] nombres = new String[series.size()];
		String[] detalles = new String[series.size()];
		String[] fotos = new String[series.size()];
		for (int i = 0; i < series.size(); i++) {
			Serie serie = series.get(i);
			nombres[i] = serie.getNombre();
			if (serie.getRepeticiones() == 1) {
				detalles[i] = "1 repetición - " + serie.getTiempo() + " s";
			} else {
				detalles[i] = serie.getRepeticiones() + " repeticiones - " + serie.getTiempo() + " s";
			}
			fotos[i] = prepararUrlFoto(serie.getFoto());
		}
		ventanaEnCurso.cargarSeries(nombres, detalles, fotos);

		// Siguiente ejercicio (null si este es el ultimo)
		if (indiceEjercicio < workoutEnCurso.getEjercicio().size() - 1) {
			Ejercicio siguiente = workoutEnCurso.getEjercicio().get(indiceEjercicio + 1);
			ventanaEnCurso.setSiguiente((indiceEjercicio + 2) + " - " + siguiente.getNombre());
		} else {
			ventanaEnCurso.setSiguiente(null);
		}

		// El cronometro del ejercicio vuelve a cero; las cajas con sus tiempos
		cronoEjercicio.setSegundos(0);
		ventanaEnCurso.setTiempoEjercicio(formatearMinutosSegundos(0));
		if (!series.isEmpty()) {
			ventanaEnCurso.setTiempoSerie("SERIE 1 - QUEDA", formatearMinutosSegundos(series.get(0).getTiempo()));
		}
		ventanaEnCurso.setTiempoDescanso("DESCANSO", formatearMinutosSegundos(ejercicio.getDescanso()));
		ventanaEnCurso.setDescansoActivo(false);

		cambiarBoton(BOTON_INICIAR);
	}

	private static Ejercicio obtenerEjercicioActual() {
		return workoutEnCurso.getEjercicio().get(indiceEjercicio);
	}

	/**
	 * Series del ejercicio (una lista vacia si no tiene).
	 */
	private static ArrayList<Serie> obtenerSeries(Ejercicio ejercicio) {
		if (ejercicio.getSerie() == null) {
			return new ArrayList<Serie>();
		}
		return ejercicio.getSerie();
	}

	private static Serie obtenerSerieActual() {
		return obtenerSeries(obtenerEjercicioActual()).get(indiceSerie);
	}

	/**
	 * Las fotos de las series son de placehold.co, por ejemplo:
	 * https://placehold.co/600x400?text=Sentadilla
	 * Sin nada mas, esa pagina devuelve la imagen en formato SVG, y Java
	 * (ImageIcon) no sabe leer SVG. Poniendo ".png" delante del "?" la
	 * devuelve en PNG: https://placehold.co/600x400.png?text=Sentadilla
	 * Las demas URLs se dejan como estan.
	 */
	private static String prepararUrlFoto(String foto) {
		if (foto != null && foto.contains("placehold.co") && !foto.contains(".png")) {
			return foto.replace("?", ".png?");
		}
		return foto;
	}

	// ====================================================================
	// PANEL: CRONOMETROS (crear los hilos y lo que pasa cada segundo)
	// ====================================================================
	/**
	 * Crea los cuatro cronometros y arranca sus hilos con start(). Empiezan
	 * parados: cada uno se pone en marcha con empezar().
	 */
	private static void crearCronometros() {
		cronoWorkout = new Cronometro(false);
		cronoEjercicio = new Cronometro(false);
		cronoSerie = new Cronometro(true);
		cronoDescanso = new Cronometro(true);

		cronoWorkout.start();
		cronoEjercicio.start();
		cronoSerie.start();
		cronoDescanso.start();
	}

	/**
	 * Acaba los cuatro hilos (al salir de la ventana del workout).
	 */
	private static void terminarCronometros() {
		cronoWorkout.terminar();
		cronoEjercicio.terminar();
		cronoSerie.terminar();
		cronoDescanso.terminar();
		cerrarCuentaAtras();
	}

	/**
	 * Lo llama el hilo de un cronometro cada vez que pasa un segundo.
	 * Las ventanas de Swing solo se deben cambiar desde el hilo de Swing:
	 * SwingUtilities.invokeLater le manda este trabajo a ese hilo.
	 */
	public static void avisarSegundo(final Cronometro crono) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				actualizarCronometro(crono);
			}
		});
	}

	/**
	 * Escribe en la ventana el tiempo del cronometro que ha avisado y, si una
	 * cuenta atras ha llegado a 0, pasa a lo siguiente.
	 */
	private static void actualizarCronometro(Cronometro crono) {
		// Si ya se ha salido del workout, el aviso ya no sirve
		if (crono.estaTerminado()) {
			return;
		}

		String tiempo = formatearMinutosSegundos(crono.getSegundos());
		if (crono == cronoWorkout) {
			ventanaEnCurso.setTiempoWorkout(tiempo);
		} else if (crono == cronoEjercicio) {
			ventanaEnCurso.setTiempoEjercicio(tiempo);
		} else if (crono == cronoSerie) {
			mostrarCuentaAtrasSerie();
			if (crono.getSegundos() == 0) {
				terminarCuentaAtrasSerie();
			}
		} else if (crono == cronoDescanso) {
			ventanaEnCurso.setTiempoDescanso("DESCANSO - QUEDA", tiempo);
			if (crono.getSegundos() == 0) {
				terminarDescanso();
			}
		}
	}

	// ====================================================================
	// PANEL: CUENTAS ATRAS (preparacion, serie y descanso)
	// ====================================================================
	/**
	 * Escribe la cuenta atras de la serie: la de preparacion o la de la serie.
	 */
	private static void mostrarCuentaAtrasSerie() {
		String tiempo = formatearMinutosSegundos(cronoSerie.getSegundos());
		if (fase == FASE_PREPARACION) {
			ventanaEnCurso.setTiempoSerie("SERIE " + (indiceSerie + 1) + " - PREPARADO", tiempo);
			// Y el numero grande del centro de la pantalla
			if (dialogoPreparacion != null) {
				dialogoPreparacion.setNumero(cronoSerie.getSegundos());
			}
		} else {
			ventanaEnCurso.setTiempoSerie("SERIE " + (indiceSerie + 1) + " - QUEDA", tiempo);
		}
	}

	/**
	 * Empieza la cuenta de 5 segundos antes de la serie indiceSerie. Se ve en
	 * grande en el centro de la pantalla (DialogoCuentaAtras, con el fondo
	 * borroso). La cuenta la lleva cronoSerie, igual que la de la serie.
	 */
	private static void empezarPreparacion() {
		fase = FASE_PREPARACION;
		ventanaEnCurso.setEstadoSerie(indiceSerie, VentanaWorkoutEnCurso.SERIE_ACTUAL);
		ventanaEnCurso.setDescansoActivo(false);
		cronoSerie.setSegundos(SEGUNDOS_PREPARACION);

		// Texto de abajo: "Sentadilla libre · SERIE 1 DE 2 · 10 repeticiones"
		int totalSeries = obtenerSeries(obtenerEjercicioActual()).size();
		String texto = obtenerEjercicioActual().getNombre() + " · SERIE " + (indiceSerie + 1) + " DE " + totalSeries
				+ " · " + obtenerSerieActual().getRepeticiones() + " repeticiones";
		dialogoPreparacion = new DialogoCuentaAtras(ventanaEnCurso, texto);
		mostrarCuentaAtrasSerie();
		dialogoPreparacion.setVisible(true);

		cronoSerie.empezar();
	}

	/**
	 * Quita la cuenta atras grande (si se esta viendo).
	 */
	private static void cerrarCuentaAtras() {
		if (dialogoPreparacion != null) {
			dialogoPreparacion.cerrar();
			dialogoPreparacion = null;
		}
	}

	/**
	 * La cuenta atras de la serie ha llegado a 0:
	 *  - si era la preparacion: empieza la serie con su tiempo
	 *  - si era la serie: se marca como hecha y empieza el descanso (o se
	 *    termina el ejercicio si era la ultima serie)
	 */
	private static void terminarCuentaAtrasSerie() {
		if (fase == FASE_PREPARACION) {
			// Fin de los 5 segundos: se quita el numero grande y empieza la serie
			cerrarCuentaAtras();
			empezarSerie();
		} else {
			ventanaEnCurso.setEstadoSerie(indiceSerie, VentanaWorkoutEnCurso.SERIE_HECHA);
			if (indiceSerie == obtenerSeries(obtenerEjercicioActual()).size() - 1) {
				terminarEjercicio();
			} else {
				empezarDescanso();
			}
		}
	}

	/**
	 * Empieza la serie indiceSerie: cronoSerie cuenta atras su tiempo.
	 */
	private static void empezarSerie() {
		fase = FASE_SERIE;
		ventanaEnCurso.setEstadoSerie(indiceSerie, VentanaWorkoutEnCurso.SERIE_ACTUAL);
		ventanaEnCurso.setDescansoActivo(false);
		cronoSerie.setSegundos(obtenerSerieActual().getTiempo());
		mostrarCuentaAtrasSerie();
		cronoSerie.empezar();
	}

	/**
	 * Empieza la cuenta atras del descanso (el tiempo de descanso es del
	 * ejercicio, el mismo para todas sus series).
	 */
	private static void empezarDescanso() {
		fase = FASE_DESCANSO;
		int descanso = obtenerEjercicioActual().getDescanso();
		cronoDescanso.setSegundos(descanso);
		ventanaEnCurso.setTiempoDescanso("DESCANSO - QUEDA", formatearMinutosSegundos(descanso));
		ventanaEnCurso.setDescansoActivo(true);
		cronoDescanso.empezar();
	}

	/**
	 * El descanso ha terminado: la caja enseña el tiempo que se ha usado para
	 * descansar y empieza la siguiente serie (con la cuenta atras de 5
	 * segundos antes solo si CUENTA_ATRAS_EN_CADA_SERIE es true).
	 */
	private static void terminarDescanso() {
		int descanso = obtenerEjercicioActual().getDescanso();
		ventanaEnCurso.setTiempoDescanso("DESCANSO - USADO", formatearMinutosSegundos(descanso));
		indiceSerie++;
		if (CUENTA_ATRAS_EN_CADA_SERIE) {
			empezarPreparacion();
		} else {
			empezarSerie();
		}
	}

	/**
	 * Todas las series hechas: se para el cronometro del ejercicio, se guarda
	 * su tiempo y el boton pasa a SIGUIENTE. Si era el ultimo ejercicio
	 * tambien se para el del workout y el boton pasa a VER RESUMEN.
	 */
	private static void terminarEjercicio() {
		cronoEjercicio.pausar();
		cronoSerie.pausar();
		cronoDescanso.pausar();
		ejerciciosCompletados++;
		segundosPorEjercicio[indiceEjercicio] = cronoEjercicio.getSegundos();

		if (indiceEjercicio < workoutEnCurso.getEjercicio().size() - 1) {
			cambiarBoton(BOTON_SIGUIENTE);
		} else {
			cronoWorkout.pausar();
			cambiarBoton(BOTON_RESUMEN);
		}
	}

	// ====================================================================
	// PANEL: PAUSAR Y REANUDAR (los cronometros se paran a la vez)
	// ====================================================================
	/**
	 * PAUSA (o el aviso de salir): se paran los cuatro cronometros.
	 */
	private static void pausarCronometros() {
		cronoWorkout.pausar();
		cronoEjercicio.pausar();
		cronoSerie.pausar();
		cronoDescanso.pausar();
	}

	/**
	 * REANUDAR: vuelven a contar el del workout, el del ejercicio y la cuenta
	 * atras que estaba contando (la de la serie o la del descanso).
	 */
	private static void reanudarCronometros() {
		cronoWorkout.empezar();
		cronoEjercicio.empezar();
		if (fase == FASE_DESCANSO) {
			cronoDescanso.empezar();
		} else {
			cronoSerie.empezar();
		}
	}

	// ====================================================================
	// PANEL: BOTON INICIAR / PAUSA / REANUDAR / SIGUIENTE / VER RESUMEN
	// ====================================================================
	/**
	 * El boton grande hace una cosa distinta segun accionBoton.
	 */
	private static void pulsarBotonAccion() {
		if (accionBoton == BOTON_INICIAR) {
			// Un ejercicio sin series se da por hecho
			if (obtenerSeries(obtenerEjercicioActual()).isEmpty()) {
				terminarEjercicio();
				return;
			}
			// El del workout empieza con el primer ejercicio (despues ya esta en marcha)
			cronoWorkout.empezar();
			cronoEjercicio.empezar();
			empezarPreparacion();
			cambiarBoton(BOTON_PAUSA);
		} else if (accionBoton == BOTON_PAUSA) {
			pausarCronometros();
			cambiarBoton(BOTON_REANUDAR);
		} else if (accionBoton == BOTON_REANUDAR) {
			reanudarCronometros();
			cambiarBoton(BOTON_PAUSA);
		} else if (accionBoton == BOTON_SIGUIENTE) {
			indiceEjercicio++;
			cargarEjercicio();
		} else if (accionBoton == BOTON_RESUMEN) {
			abrirResumen();
		}
	}

	/**
	 * Guarda lo que hace ahora el boton y cambia su texto y color.
	 */
	private static void cambiarBoton(int accion) {
		accionBoton = accion;
		if (accion == BOTON_INICIAR) {
			ventanaEnCurso.mostrarBotonIniciar();
		} else if (accion == BOTON_PAUSA) {
			ventanaEnCurso.mostrarBotonPausa();
		} else if (accion == BOTON_REANUDAR) {
			ventanaEnCurso.mostrarBotonReanudar();
		} else if (accion == BOTON_SIGUIENTE) {
			ventanaEnCurso.mostrarBotonSiguiente();
		} else if (accion == BOTON_RESUMEN) {
			ventanaEnCurso.mostrarBotonResumen();
		}
	}

	// ====================================================================
	// PANEL: AVISO AL PULSAR SALIR (DialogoSalir con el fondo borroso)
	// ====================================================================
	/**
	 * Los cronometros se paran mientras sale el aviso. CANCELAR vuelve a poner
	 * en marcha los que estaban contando. SALIR termina el workout.
	 */
	private static void preguntarSalir() {
		// Si ya ha terminado todo no hay nada que perder: directo al resumen
		if (accionBoton == BOTON_RESUMEN) {
			abrirResumen();
			return;
		}

		// Se apunta que estaba contando para dejarlo igual si cancela
		final boolean workoutEnMarcha = cronoWorkout.estaEnMarcha();
		final boolean ejercicioEnMarcha = cronoEjercicio.estaEnMarcha();
		pausarCronometros();

		final DialogoSalir dialogo = new DialogoSalir(ventanaEnCurso, "Llevas el " + calcularPorcentaje()
				+ "% del workout. Se guardará en tu histórico y los ejercicios que faltan quedarán sin completar.");

		dialogo.getBtnCancelar().addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dialogo.cerrar();
				if (ejercicioEnMarcha) {
					reanudarCronometros();
				} else if (workoutEnMarcha) {
					// Entre ejercicios solo cuenta el del workout
					cronoWorkout.empezar();
				}
			}
		});

		dialogo.getBtnSalir().addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dialogo.cerrar();
				// El tiempo del ejercicio a medias tambien cuenta (si ya estaba
				// terminado, su tiempo ya se guardo en terminarEjercicio)
				if (accionBoton != BOTON_SIGUIENTE) {
					segundosPorEjercicio[indiceEjercicio] = cronoEjercicio.getSegundos();
				}
				abrirResumen();
			}
		});

		dialogo.setVisible(true);
	}

	// ---------------------------------------------------------------
	// Ventana de resumen del workout
	// ---------------------------------------------------------------

	private static VentanaResumen ventanaResumen;

	// ====================================================================
	// PANEL: RESUMEN DEL WORKOUT (abrir la ventana con los datos)
	// ====================================================================
	/**
	 * Porcentaje = ejercicios completados / ejercicios del workout * 100 (un
	 * ejercicio a medias no cuenta como completado, pero su tiempo si).
	 */
	private static int calcularPorcentaje() {
		int totalEjercicios = workoutEnCurso.getEjercicio().size();
		if (totalEjercicios == 0) {
			return 0;
		}
		return ejerciciosCompletados * 100 / totalEjercicios;
	}

	/**
	 * Se guarda el workout en el historico (con su porcentaje) y se abre el
	 * resumen con los tiempos, los ejercicios y el mensaje motivacional.
	 */
	private static void abrirResumen() {
		terminarCronometros();

		ArrayList<Ejercicio> ejercicios = workoutEnCurso.getEjercicio();
		int totalEjercicios = ejercicios.size();
		int porcentaje = calcularPorcentaje();
		boolean completado = porcentaje == 100;

		// Tiempo total = lo que marca el cronometro del workout
		int segundosTotales = cronoWorkout.getSegundos();

		boolean subeDeNivel = guardarEnHistorico(porcentaje, segundosTotales);

		ventanaResumen = new VentanaResumen();
		if (completado) {
			ventanaResumen.setTitulo("¡Workout completado!");
		} else {
			ventanaResumen.setTitulo("Workout terminado");
		}
		ventanaResumen.setDatosWorkout(workoutEnCurso.getNombre(), workoutEnCurso.getNivel(),
				obtenerNombreNivel(workoutEnCurso.getNivel()), LocalDate.now().format(FORMATO_ULTIMO_WORKOUT));
		ventanaResumen.setDatos(formatearMinutosSegundos(segundosTotales),
				formatearMinutosSegundos(calcularDuracionWorkout(workoutEnCurso)),
				ejerciciosCompletados + " / " + totalEjercicios, porcentaje + "%");

		// Lista de ejercicios: los completados son los primeros (se hacen en orden)
		String[] nombres = new String[totalEjercicios];
		String[] tiempos = new String[totalEjercicios];
		boolean[] hechos = new boolean[totalEjercicios];
		for (int i = 0; i < totalEjercicios; i++) {
			nombres[i] = ejercicios.get(i).getNombre();
			hechos[i] = i < ejerciciosCompletados;
			if (segundosPorEjercicio[i] > 0) {
				tiempos[i] = formatearMinutosSegundos(segundosPorEjercicio[i]);
			} else {
				tiempos[i] = "--:--";
			}
		}
		ventanaResumen.cargarEjercicios(nombres, tiempos, hechos);

		ponerMensajeMotivacional(completado, subeDeNivel);

		// VOLVER: a la lista de workouts
		ventanaResumen.getBtnVolver().addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				abrirWorkouts();
				ventanaResumen.cerrar();
			}
		});

		ventanaResumen.setVisible(true);
		ventanaEnCurso.cerrar();
	}

	// ====================================================================
	// PANEL: GUARDAR EN EL HISTORICO Y SUBIR DE NIVEL
	// ====================================================================
	/**
	 * Guarda el workout en el historico del usuario: en la lista de la
	 * aplicacion (para que la home y el historico lo enseñen ya) y en el
	 * servidor. El tiempo previsto no se guarda (se calcula al enseñarlo).
	 * Sin conexion (o si el servidor falla) se guarda en pendientes_<id>.json
	 * y se sube en el proximo login con conexion (subirPendientes).
	 * Si lo ha completado al 100% y era de su nivel, sube al siguiente nivel.
	 * Devuelve true si ha subido de nivel.
	 */
	private static boolean guardarEnHistorico(int porcentaje, int segundosTotales) {
		// WORKOUT DE PRUEBA: no se guarda en el historico
		if (workoutEnCurso.getNivel() == NIVEL_PRUEBA) {
			return false;
		}
		// Si sale sin haber empezado (cronometro a 0) no hay nada que guardar
		if (segundosTotales == 0) {
			return false;
		}

		Historico historico = new Historico(0, workoutEnCurso.getNombre(), workoutEnCurso.getNivel(),
				LocalDate.now(), formatearMinutosSegundos(segundosTotales), null, porcentaje);

		if (usuarioRegistrado.getHistorico() == null) {
			usuarioRegistrado.setHistorico(new ArrayList<Historico>());
		}
		usuarioRegistrado.getHistorico().add(historico);

		// El admin no es un usuario de verdad (su id es 0): no se guarda
		if (usuarioRegistrado.getId() == 0) {
			return false;
		}

		// Sin conexion (o si el servidor falla): se guarda en el ordenador y se
		// sube en el proximo login con conexion. Asi no se pierde.
		if (modoSinConexion || !cliente.guardarHistorico(usuarioRegistrado.getId(), historico)) {
			gestorFicheros.anadirPendiente(usuarioRegistrado.getId(), historico);
			System.out.println("Sin conexión: el workout se guarda en el ordenador (pendiente de subir)");
			return false;
		}

		boolean subeDeNivel = subirNivelSiToca(workoutEnCurso.getNivel(), porcentaje);
		// Un solo backup para el historico y el nivel nuevo
		lanzarBackup();
		return subeDeNivel;
	}

	/**
	 * Subir de nivel: si ha completado al 100% un workout de SU nivel (y no
	 * esta en el maximo), el servidor le pone el nivel siguiente.
	 * Devuelve true si ha subido.
	 */
	private static boolean subirNivelSiToca(int nivelWorkout, int porcentaje) {
		int nivel = usuarioRegistrado.getNivel();
		if (porcentaje == 100 && nivelWorkout == nivel && nivel < NIVEL_MAXIMO) {
			if (cliente.actualizarNivelUsuario(usuarioRegistrado.getId(), nivel + 1)) {
				usuarioRegistrado.setNivel(nivel + 1);
				System.out.println("Sube al nivel " + obtenerNombreNivel(nivel + 1));
				return true;
			}
		}
		return false;
	}

	/**
	 * Al hacer login CON conexion: los workouts que hizo sin conexion
	 * (pendientes_<id>.json) se mandan al servidor en el orden en que los hizo,
	 * y se sube de nivel igual que si los hubiera hecho con conexion. Los que
	 * no se puedan mandar se quedan en el fichero para el siguiente login.
	 */
	private static void subirPendientes() {
		int id = usuarioRegistrado.getId();
		ArrayList<Historico> pendientes = gestorFicheros.leerPendientes(id);
		if (pendientes.isEmpty()) {
			return;
		}

		ArrayList<Historico> sinSubir = new ArrayList<Historico>();
		for (int i = 0; i < pendientes.size(); i++) {
			Historico hist = pendientes.get(i);
			if (cliente.guardarHistorico(id, hist)) {
				subirNivelSiToca(hist.getNivel(), hist.getPorcentaje());
			} else {
				sinSubir.add(hist);
			}
		}
		// Se quedan solo los que han fallado (si no queda ninguno, se borra el fichero)
		gestorFicheros.guardarPendientes(id, sinSubir);
		System.out.println("Workouts sin conexión subidos al servidor: " + (pendientes.size() - sinSubir.size())
				+ " de " + pendientes.size());
		// Si se ha subido alguno, el servidor ha cambiado: nuevo backup
		if (sinSubir.size() < pendientes.size()) {
			lanzarBackup();
		}
	}

	// ---------------------------------------------------------------
	// Backup en un proceso secundario
	// ---------------------------------------------------------------

	// true mientras hay un backup haciendose
	private static boolean backupEnMarcha = false;
	// true si se ha cambiado algo mientras se hacia el backup: al terminar
	// hay que hacer otro (para que tenga tambien ese cambio)
	private static boolean repetirBackup = false;

	// ====================================================================
	// PANEL: LANZAR EL PROCESO DE BACKUP (ProcessBuilder)
	// ====================================================================
	/**
	 * Se llama al arrancar la aplicacion y despues de CADA cambio que se
	 * guarda en el servidor (registro, datos, contraseña, historico, nivel,
	 * borrar historico, borrar cuenta).
	 *
	 * Arranca el backup en OTRO proceso con ProcessBuilder: la misma
	 * aplicacion (app.InicioApp) con el argumento "backup", que llama a
	 * ProcesoBackup.hacerBackup(). Un hilo comprueba antes si hay conexion
	 * (sin servidor no hay datos que copiar: se deja el backup que hay), lee
	 * lo que escribe el proceso (su mensaje con el resultado) y espera a que
	 * termine, asi la ventana no se congela.
	 *
	 * Nunca hay dos backups a la vez (escribirian los mismos ficheros): si ya
	 * hay uno, se apunta que hay que repetirlo y se hace al terminar.
	 * "synchronized": lo pueden llamar a la vez el hilo de Swing y el hilo
	 * del backup (terminarBackup), asi lo hacen de uno en uno.
	 */
	private static synchronized void lanzarBackup() {
		if (backupEnMarcha) {
			repetirBackup = true;
			return;
		}
		backupEnMarcha = true;

		Thread hiloBackup = new Thread(new Runnable() {
			@Override
			public void run() {
				if (!cliente.hayConexion()) {
					System.out.println("[Backup] Sin conexión con el servidor: se usa el backup que ya hay");
					terminarBackup();
					return;
				}
				try {
					ProcessBuilder pb = new ProcessBuilder(crearComandoBackup());
					// Los errores del proceso salen por el mismo sitio que los mensajes
					pb.redirectErrorStream(true);
					Process proceso = pb.start();

					// Lo que el proceso escribe con System.out lo leemos aqui
					BufferedReader lector = new BufferedReader(
							new InputStreamReader(proceso.getInputStream(), StandardCharsets.UTF_8));
					String linea;
					while ((linea = lector.readLine()) != null) {
						System.out.println("[Backup] " + linea);
					}

					// waitFor espera a que el proceso termine
					proceso.waitFor();
					System.out.println("[Backup] Proceso terminado");
				} catch (Exception e) {
					System.out.println("[Backup] No se ha podido lanzar el proceso: " + e.getMessage());
				}
				terminarBackup();
			}
		});
		hiloBackup.start();
	}

	/**
	 * El backup ha terminado. Si mientras tanto se ha cambiado algo, se hace
	 * otro.
	 */
	private static synchronized void terminarBackup() {
		backupEnMarcha = false;
		if (repetirBackup) {
			repetirBackup = false;
			lanzarBackup();
		}
	}

	/**
	 * Comando para arrancar el proceso, como si se escribiera en la consola:
	 * java -Dstdout.encoding=UTF-8 -cp <librerias> app.InicioApp backup
	 * Se usa el mismo java y las mismas librerias con los que se esta
	 * ejecutando la aplicacion (las da System.getProperty).
	 */
	private static ArrayList<String> crearComandoBackup() {
		String java = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";

		// Librerias y clases del proyecto. Si la aplicacion se ha arrancado como
		// modulo, una parte esta en el "module path": se juntan las dos.
		String librerias = System.getProperty("java.class.path");
		String modulePath = System.getProperty("jdk.module.path");
		if (modulePath != null) {
			librerias = modulePath + File.pathSeparator + librerias;
		}

		ArrayList<String> comando = new ArrayList<String>();
		comando.add(java);
		// Para que las tildes del mensaje lleguen bien
		comando.add("-Dstdout.encoding=UTF-8");
		comando.add("-Dstderr.encoding=UTF-8");
		comando.add("-cp");
		comando.add(librerias);
		comando.add("app.InicioApp");
		// Con este argumento el main hace el backup en vez de abrir las ventanas
		comando.add("backup");
		return comando;
	}

	// ====================================================================
	// PANEL: MENSAJE MOTIVACIONAL (frases al azar)
	// ====================================================================

	// El titulo es: comienzo + nombre del usuario + "!" (ej: "¡Gran trabajo, Alexis!")
	private static final String[] TITULOS_COMPLETADO = { "¡Gran trabajo, ", "¡Enhorabuena, ", "¡Así se hace, ",
			"¡Bien hecho, ", "¡Imparable, ", "¡Lo has clavado, ", "¡Qué nivel, ", "¡Brutal, " };

	private static final String[] TITULOS_INCOMPLETO = { "¡Buen intento, ", "¡Ánimo, ", "¡No te rindas, ",
			"¡Cada paso cuenta, ", "¡Sigue así, " };

	private static final String[] MENSAJES_COMPLETADO = {
			"Has completado todos los ejercicios. Sigue así y pronto subirás de nivel.",
			"Workout terminado al 100%. La constancia es lo que marca la diferencia.",
			"Hoy has cumplido. Descansa bien, que mañana toca volver a darlo todo.",
			"Cada workout completado te acerca a tu objetivo. ¡A por el siguiente!",
			"Lo difícil era empezar y lo has terminado. Eso es disciplina.",
			"Tu cuerpo te lo agradecerá. Hidrátate y recupera energía.",
			"Un workout más en tu historial. Los resultados llegan con la repetición.",
			"Has superado todos los ejercicios sin dejarte ninguno. ¡Enhorabuena!",
			"El esfuerzo de hoy es la fuerza de mañana. Sigue con este ritmo.",
			"Otro paso hacia tu mejor versión. ¡No pares ahora!",
			"Entrenar cuando no apetece es lo que te hace fuerte. ¡Gran sesión!",
			"Constancia, esfuerzo y ganas: hoy has tenido las tres." };

	private static final String[] MENSAJES_INCOMPLETO = {
			"No lo has terminado, pero lo que has hecho ya cuenta. La próxima vez, un poco más.",
			"Cada ejercicio suma. Vuelve pronto y termina lo que has empezado.",
			"Hoy no ha sido el día completo, pero has entrenado. Eso es lo importante.",
			"Escucha a tu cuerpo y descansa. El workout seguirá aquí esperándote.",
			"Un mal día no borra todo tu progreso. Mañana, a por todas.",
			"Lo importante es no rendirse. La próxima vez llegarás más lejos.",
			"Has dado el primer paso. Ahora toca volver y completarlo entero." };

	private static Random aleatorio = new Random();

	/**
	 * Elige un titulo y un mensaje al azar (unos si ha completado el workout y
	 * otros si ha salido antes) y los pone en la caja negra del resumen.
	 * Si ha subido de nivel, el mensaje es el del nivel nuevo.
	 */
	private static void ponerMensajeMotivacional(boolean completado, boolean subeDeNivel) {
		if (subeDeNivel) {
			String nivelNuevo = obtenerNombreNivel(usuarioRegistrado.getNivel());
			ventanaResumen.setMensaje("¡Nivel " + nivelNuevo + ", " + usuarioRegistrado.getNombre() + "!",
					"Has completado un workout de tu nivel y has subido a " + nivelNuevo
							+ ". Ya tienes nuevos workouts desbloqueados.");
			return;
		}

		String[] titulos;
		String[] mensajes;
		if (completado) {
			titulos = TITULOS_COMPLETADO;
			mensajes = MENSAJES_COMPLETADO;
		} else {
			titulos = TITULOS_INCOMPLETO;
			mensajes = MENSAJES_INCOMPLETO;
		}

		// nextInt(n) devuelve un numero al azar de 0 a n-1 (una posicion del array)
		String titulo = titulos[aleatorio.nextInt(titulos.length)] + usuarioRegistrado.getNombre() + "!";
		String mensaje = mensajes[aleatorio.nextInt(mensajes.length)];
		ventanaResumen.setMensaje(titulo, mensaje);
	}

	// ====================================================================
	// WORKOUT DE PRUEBA "Custom" (SOLO PARA PROBAR, SE PUEDE BORRAR)
	// Un workout de 1 ejercicio con 1 serie de 3 segundos, para llegar al
	// resumen rapido. Sale el primero en la lista (filtro TODOS).
	// Para quitarlo: poner MOSTRAR_WORKOUT_PRUEBA = false. Para borrarlo del
	// todo: borrar este bloque y las lineas marcadas con "WORKOUT DE PRUEBA"
	// (aqui, en obtenerNombreNivel, en cargarDatosEnSegundoPlano y en
	// guardarEnHistorico), y en
	// VentanaWorkouts el "else if" de "custom" y la imagen nivel_custom.png.
	// ====================================================================
	private static final boolean MOSTRAR_WORKOUT_PRUEBA = true;
	private static final int NIVEL_PRUEBA = -1;

	private static Workout crearWorkoutPrueba() {
		ArrayList<Serie> series = new ArrayList<Serie>();
		series.add(new Serie(1, "Serie 1", 1, 3, "", 1));

		ArrayList<Ejercicio> ejercicios = new ArrayList<Ejercicio>();
		ejercicios.add(new Ejercicio(1, "Ejercicio de prueba",
				"Solo dura 3 segundos. Sirve para llegar rápido al resumen.", 1, 0, series));

		return new Workout(0, "Prueba rápida", NIVEL_PRUEBA, "", 1, ejercicios);
	}

	// ---------------------------------------------------------------
	// Metodos de ayuda (niveles y tiempos)
	// ---------------------------------------------------------------

	// Nombre de cada nivel: la posicion del array es el numero de nivel (0 a 4)
	private static final String[] NOMBRES_NIVEL = { "Novato", "Principiante", "Intermedio", "Avanzado", "Experto" };

	/**
	 * 0 Novato, 1 Principiante, 2 Intermedio, 3 Avanzado, 4 Experto.
	 */
	private static String obtenerNombreNivel(int nivel) {
		// WORKOUT DE PRUEBA: su nivel se llama "Custom"
		if (nivel == NIVEL_PRUEBA) {
			return "Custom";
		}
		if (nivel < 0) {
			nivel = 0;
		} else if (nivel > NIVEL_MAXIMO) {
			nivel = NIVEL_MAXIMO;
		}
		return NOMBRES_NIVEL[nivel];
	}

	/**
	 * Numero de ejercicios: el de la lista, o numEjercicios si la lista esta vacia.
	 */
	private static int contarEjercicios(Workout workout) {
		if (workout.getEjercicio() != null && !workout.getEjercicio().isEmpty()) {
			return workout.getEjercicio().size();
		}
		return workout.getNumEjercicios();
	}

	/**
	 * Duracion prevista del workout: la suma de la de sus ejercicios. En segundos.
	 */
	private static int calcularDuracionWorkout(Workout workout) {
		int total = 0;
		ArrayList<Ejercicio> ejercicios = workout.getEjercicio();
		if (ejercicios == null) {
			return 0;
		}
		for (int i = 0; i < ejercicios.size(); i++) {
			total = total + calcularDuracionEjercicio(ejercicios.get(i));
		}
		return total;
	}

	/**
	 * Duracion prevista de un ejercicio: tiempo de cada serie + un descanso (el
	 * del ejercicio) por cada serie. En segundos.
	 */
	private static int calcularDuracionEjercicio(Ejercicio ejercicio) {
		int total = 0;
		ArrayList<Serie> series = ejercicio.getSerie();
		if (series != null) {
			for (int i = 0; i < series.size(); i++) {
				total = total + series.get(i).getTiempo() + ejercicio.getDescanso();
			}
		}
		return total;
	}

	/**
	 * 720 -> "12:00", 95 -> "01:35" (como en el diseño del detalle).
	 */
	private static String formatearMinutosSegundos(int segundos) {
		return String.format("%02d:%02d", segundos / 60, segundos % 60);
	}

	/**
	 * 600 -> "10m", 595 -> "9m 55s" (corto para que quepa en la columna).
	 */
	private static String formatearDuracion(int segundos) {
		int minutos = segundos / 60;
		int resto = segundos % 60;
		if (resto == 0) {
			return minutos + "m";
		}
		return minutos + "m " + resto + "s";
	}

	/**
	 * Tiempo total de la home: 0 -> "0 min", 1500 -> "25 min", 4800 -> "1h 20min".
	 */
	private static String formatearTiempoTotal(int segundos) {
		int minutosTotales = segundos / 60;
		int horas = minutosTotales / 60;
		int minutos = minutosTotales % 60;
		if (horas == 0) {
			return minutos + " min";
		}
		return horas + "h " + minutos + "min";
	}

	/**
	 * El historico guarda el tiempo como texto "mm:ss" (o "hh:mm:ss"). "22:57" ->
	 * 1377 segundos. Si no se puede leer, 0.
	 */
	private static int convertirASegundos(String tiempo) {
		if (tiempo == null || tiempo.isEmpty()) {
			return 0;
		}
		String[] partes = tiempo.trim().split(":");
		int segundos = 0;
		try {
			for (int i = 0; i < partes.length; i++) {
				segundos = segundos * 60 + Integer.parseInt(partes[i]);
			}
		} catch (NumberFormatException e) {
			return 0;
		}
		return segundos;
	}
}
