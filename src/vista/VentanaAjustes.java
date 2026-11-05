package vista;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.ParseException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.text.MaskFormatter;

/**
 * Ventana de ajustes del usuario (diseño "Ajustes").
 * A la izquierda, en la parte negra, el menu con las dos pestañas
 * (DATOS DEL USUARIO y CONTRASEÑA) y el nombre y nivel del usuario.
 * A la derecha el formulario de la pestaña elegida y los botones.
 * Solo es la parte visual: el controlador pone los datos, cambia de
 * pestaña y comprueba los campos al pulsar GUARDAR.
 */
public class VentanaAjustes extends JFrame {

	private static final long serialVersionUID = 1L;

	private static final String RUTA_FONDO = "images/ajustes_bg.jpg";
	private static final String RUTA_FONDO_ERROR = "images/ajustes_bg_incorrect.jpg";
	// El mismo logo que la cabecera de la home (logo_negro.png) pero en blanco
	private static final String RUTA_LOGO_BLANCO = "images/iconos/logo_blanco_cabecera.png";

	// Menu de la izquierda (las dos pestañas)
	private static final int MENU_X = 26;
	private static final int MENU_DATOS_Y = 188;
	private static final int MENU_PASSWORD_Y = 234;
	private static final int MENU_ANCHO = 190;
	private static final int MENU_ALTO = 22;

	// Formulario de la derecha
	private static final int FORMULARIO_X = 327;
	private static final int FORMULARIO_ANCHO = 499;

	// Los campos van dentro de un panel por pestaña. Estas posiciones son
	// dentro de ese panel (la x de la columna 2 es 593 - 327 = 266)
	private static final int PANEL_CAMPOS_Y = 166;
	private static final int PANEL_CAMPOS_ALTO = 150;
	private static final int COLUMNA_1_X = 0;
	private static final int COLUMNA_2_X = 266;
	private static final int FILA_1_Y = 26;
	private static final int FILA_2_Y = 110;
	private static final int CAMPO_ANCHO = 233;
	private static final int CAMPO_ALTO = 37;
	// La etiqueta gris va 20 px encima del campo
	private static final int SEPARACION_ETIQUETA = 20;

	// Botones GUARDAR y SALIR (todo el ancho del formulario)
	private static final int GUARDAR_Y = 347;
	private static final int SALIR_Y = 397;
	private static final int BOTON_ALTO = 38;

	// Boton rojo ELIMINAR CUENTA (en la parte negra, debajo de las pestañas)
	private static final int ELIMINAR_Y = 440;
	private static final int ELIMINAR_ANCHO = 180;

	// Colores y fuentes de esta ventana
	private static final Color COLOR_ETIQUETA = new Color(125, 125, 125);
	private static final Font FUENTE_TITULO_MENU = new Font(Estilos.FAMILIA_FUENTE, Font.BOLD, 44);
	private static final Font FUENTE_TITULO = new Font(Estilos.FAMILIA_FUENTE, Font.BOLD, 38);
	private static final Font FUENTE_CAMPO = Estilos.FUENTE_MONO.deriveFont(Font.PLAIN, 16f);

	private PanelFondo panelFondo;

	// Menu
	private JButton btnMenuDatos;
	private JButton btnMenuPassword;
	private JLabel lblPunto;
	private JLabel lblNombreUsuario;
	private JLabel lblIconoNivel;
	private JLabel lblNivel;

	// Formulario
	private JLabel lblTitulo;
	private JPanel panelDatos;
	private JPanel panelPassword;
	private JTextField txtNombre;
	private JTextField txtApellidos;
	private JFormattedTextField txtFechaNacimiento;
	private JTextField txtEmail;
	private JPasswordField txtPasswordActual;
	private JPasswordField txtPasswordNueva;
	private JPasswordField txtRepetirPassword;
	private JButton btnGuardar;
	private JButton btnSalir;
	private JButton btnEliminarCuenta;
	private JLabel lblMensaje;

	public VentanaAjustes() {
		setTitle("GymApp - Ajustes");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(false);

		if (Estilos.ICONO_APP != null) {
			setIconImage(Estilos.ICONO_APP);
		}
		setCursor(Estilos.CURSOR_APP);

		panelFondo = new PanelFondo(RUTA_FONDO);
		setContentPane(panelFondo);

		crearMenu();
		crearUsuario();
		crearTitulo();
		crearPanelDatos();
		crearPanelPassword();
		crearBotones();
		crearMensaje();

		// Al pulsar Enter en cualquier campo se pulsa GUARDAR
		getRootPane().setDefaultButton(btnGuardar);

		// Se empieza en la pestaña de datos del usuario
		mostrarDatos();

		pack();
		setLocationRelativeTo(null);
	}

	// ---------------------------------------------------------------
	// Creacion de componentes
	// ---------------------------------------------------------------

	// ====================================================================
	// PANEL: MENU DE LA IZQUIERDA (logo, 'Ajustes' y las dos pestañas)
	// ====================================================================
	/**
	 * Logo + "GymApp" en blanco, el titulo "Ajustes" y las dos pestañas.
	 * El punto blanco se pone a la izquierda de la pestaña elegida.
	 */
	private void crearMenu() {
		JLabel lblLogo = new JLabel(UtilidadesVista.cargarImagen(RUTA_LOGO_BLANCO));
		lblLogo.setBounds(24, 14, 32, 24);
		panelFondo.add(lblLogo);

		JLabel lblNombreApp = new JLabel("GymApp");
		lblNombreApp.setFont(Estilos.FUENTE_LOGO_TEXTO);
		lblNombreApp.setForeground(Estilos.COLOR_TEXTO_CAJA);
		lblNombreApp.setBounds(62, 11, 120, 30);
		panelFondo.add(lblNombreApp);

		JLabel lblAjustes = new JLabel("Ajustes");
		lblAjustes.setFont(FUENTE_TITULO_MENU);
		lblAjustes.setForeground(Estilos.COLOR_TEXTO_CAJA);
		lblAjustes.setBounds(MENU_X, 78, 200, 60);
		panelFondo.add(lblAjustes);

		lblPunto = new JLabel("•");
		lblPunto.setFont(Estilos.FUENTE_MONO);
		lblPunto.setForeground(Estilos.COLOR_TEXTO_CAJA);
		panelFondo.add(lblPunto);

		btnMenuDatos = crearBotonMenu("DATOS DEL USUARIO");
		btnMenuDatos.setBounds(MENU_X, MENU_DATOS_Y, MENU_ANCHO, MENU_ALTO);
		panelFondo.add(btnMenuDatos);

		btnMenuPassword = crearBotonMenu("CONTRASEÑA");
		btnMenuPassword.setBounds(MENU_X, MENU_PASSWORD_Y, MENU_ANCHO, MENU_ALTO);
		panelFondo.add(btnMenuPassword);
	}

	// ====================================================================
	// PANEL: USUARIO (nombre y nivel, abajo a la izquierda)
	// ====================================================================
	/**
	 * Nombre del usuario y debajo "Nivel" + icono + nombre del nivel con su
	 * color. Los textos los pone setUsuario.
	 */
	private void crearUsuario() {
		lblNombreUsuario = new JLabel();
		lblNombreUsuario.setFont(Estilos.FUENTE_MONO);
		lblNombreUsuario.setForeground(Estilos.COLOR_TEXTO_CAJA);
		lblNombreUsuario.setBounds(15, 521, 190, 22);
		panelFondo.add(lblNombreUsuario);

		JLabel lblTextoNivel = new JLabel("Nivel");
		lblTextoNivel.setFont(Estilos.FUENTE_MONO_PEQUENA);
		lblTextoNivel.setForeground(Estilos.COLOR_TEXTO_CAJA);
		lblTextoNivel.setBounds(15, 546, 45, 22);
		panelFondo.add(lblTextoNivel);

		lblIconoNivel = new JLabel();
		lblIconoNivel.setBounds(65, 546, 16, 22);
		panelFondo.add(lblIconoNivel);

		lblNivel = new JLabel();
		lblNivel.setFont(Estilos.FUENTE_MONO_PEQUENA);
		lblNivel.setBounds(83, 546, 130, 22);
		panelFondo.add(lblNivel);
	}

	// ====================================================================
	// PANEL: TITULO DE LA PESTAÑA ('Datos del Usuario' / 'Contraseña')
	// ====================================================================
	private void crearTitulo() {
		lblTitulo = new JLabel();
		lblTitulo.setFont(FUENTE_TITULO);
		lblTitulo.setForeground(Estilos.COLOR_CAJA);
		lblTitulo.setBounds(FORMULARIO_X, 80, FORMULARIO_ANCHO, 56);
		panelFondo.add(lblTitulo);
	}

	// ====================================================================
	// PANEL: PESTAÑA DATOS DEL USUARIO (nombre, apellidos, fecha y email)
	// ====================================================================
	private void crearPanelDatos() {
		panelDatos = crearPanelCampos();

		// Se anaden en este orden para que el tabulador vaya:
		// nombre -> apellidos -> fecha -> email
		txtNombre = new JTextField();
		anadirCampo(panelDatos, "NOMBRE", txtNombre, COLUMNA_1_X, FILA_1_Y);

		txtApellidos = new JTextField();
		anadirCampo(panelDatos, "APELLIDOS", txtApellidos, COLUMNA_2_X, FILA_1_Y);

		txtFechaNacimiento = crearCampoFecha();
		anadirCampo(panelDatos, "FECHA DE NACIMIENTO", txtFechaNacimiento, COLUMNA_1_X, FILA_2_Y);

		txtEmail = new JTextField();
		anadirCampo(panelDatos, "EMAIL", txtEmail, COLUMNA_2_X, FILA_2_Y);
	}

	// ====================================================================
	// PANEL: PESTAÑA CONTRASEÑA (actual, nueva y repetir)
	// ====================================================================
	private void crearPanelPassword() {
		panelPassword = crearPanelCampos();

		txtPasswordActual = new JPasswordField();
		txtPasswordActual.setEchoChar('•');
		anadirCampo(panelPassword, "CONTRASEÑA ACTUAL", txtPasswordActual, COLUMNA_1_X, FILA_1_Y);

		txtPasswordNueva = new JPasswordField();
		txtPasswordNueva.setEchoChar('•');
		anadirCampo(panelPassword, "NUEVA CONTRASEÑA", txtPasswordNueva, COLUMNA_1_X, FILA_2_Y);

		txtRepetirPassword = new JPasswordField();
		txtRepetirPassword.setEchoChar('•');
		anadirCampo(panelPassword, "REPETIR CONTRASEÑA", txtRepetirPassword, COLUMNA_2_X, FILA_2_Y);
	}

	// ====================================================================
	// PANEL: BOTONES GUARDAR, SALIR Y ELIMINAR CUENTA
	// ====================================================================
	/**
	 * GUARDAR (negro) y SALIR (blanco con borde negro). Los dos sirven para
	 * las dos pestañas. ELIMINAR CUENTA (rojo) va a la izquierda, en la parte
	 * negra: antes de borrar, el controlador pide confirmacion.
	 */
	private void crearBotones() {
		btnGuardar = new BotonRedondeado("GUARDAR", true, false);
		btnGuardar.setBounds(FORMULARIO_X, GUARDAR_Y, FORMULARIO_ANCHO, BOTON_ALTO);
		panelFondo.add(btnGuardar);

		btnSalir = new BotonRedondeado("SALIR", false, false);
		btnSalir.setBounds(FORMULARIO_X, SALIR_Y, FORMULARIO_ANCHO, BOTON_ALTO);
		panelFondo.add(btnSalir);

		btnEliminarCuenta = new BotonRedondeado("ELIMINAR CUENTA", true, true);
		// Sin el margen interior de Java: si no, el texto no cabe y sale "ELIMINAR CU..."
		btnEliminarCuenta.setMargin(new Insets(0, 0, 0, 0));
		btnEliminarCuenta.setBounds(MENU_X, ELIMINAR_Y, ELIMINAR_ANCHO, BOTON_ALTO);
		panelFondo.add(btnEliminarCuenta);
	}

	// ====================================================================
	// PANEL: MENSAJE DE ERROR / GUARDADO (debajo de SALIR)
	// ====================================================================
	/**
	 * Mensaje debajo de los botones: rojo para errores y verde cuando se
	 * guarda bien. Empieza oculto.
	 */
	private void crearMensaje() {
		lblMensaje = new JLabel(" ");
		lblMensaje.setFont(Estilos.FUENTE_MONO_PEQUENA);
		lblMensaje.setForeground(Estilos.COLOR_ERROR);
		lblMensaje.setHorizontalAlignment(SwingConstants.CENTER);
		lblMensaje.setBounds(FORMULARIO_X, SALIR_Y + BOTON_ALTO + 12, FORMULARIO_ANCHO, 22);
		lblMensaje.setVisible(false);
		panelFondo.add(lblMensaje);
	}

	// ====================================================================
	// AYUDA: BOTONES DEL MENU, PANELES Y CAMPOS
	// ====================================================================
	/**
	 * Boton del menu: solo se ve el texto blanco. Con el raton encima el
	 * texto se pone gris.
	 */
	private JButton crearBotonMenu(String texto) {
		final JButton boton = new JButton(texto);
		boton.setFont(Estilos.FUENTE_MONO);
		boton.setForeground(Estilos.COLOR_TEXTO_CAJA);
		boton.setHorizontalAlignment(SwingConstants.LEFT);
		boton.setBorder(BorderFactory.createEmptyBorder());
		boton.setContentAreaFilled(false);
		boton.setBorderPainted(false);
		boton.setFocusPainted(false);
		boton.setFocusable(false);
		boton.setCursor(new Cursor(Cursor.HAND_CURSOR));

		boton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				boton.setForeground(Estilos.COLOR_TEXTO_TARJETA);
			}

			@Override
			public void mouseExited(MouseEvent e) {
				boton.setForeground(Estilos.COLOR_TEXTO_CAJA);
			}
		});
		return boton;
	}

	/**
	 * Panel transparente donde van los campos de una pestaña. Asi para
	 * cambiar de pestaña solo hay que ocultar un panel y enseñar el otro.
	 */
	private JPanel crearPanelCampos() {
		JPanel panel = new JPanel();
		panel.setLayout(null);
		panel.setOpaque(false);
		panel.setBounds(FORMULARIO_X, PANEL_CAMPOS_Y, FORMULARIO_ANCHO, PANEL_CAMPOS_ALTO);
		panelFondo.add(panel);
		return panel;
	}

	/**
	 * Anade la etiqueta gris (encima) y el campo en la posicion indicada.
	 */
	private void anadirCampo(JPanel panel, String texto, JTextField campo, int x, int y) {
		JLabel etiqueta = new JLabel(texto);
		etiqueta.setFont(Estilos.FUENTE_MONO_PEQUENA);
		etiqueta.setForeground(COLOR_ETIQUETA);
		etiqueta.setBounds(x, y - SEPARACION_ETIQUETA, CAMPO_ANCHO, 18);
		panel.add(etiqueta);

		campo.setFont(FUENTE_CAMPO);
		campo.setBackground(Estilos.COLOR_FONDO);
		campo.setForeground(Estilos.COLOR_CAJA);
		campo.setCaretColor(Estilos.COLOR_CAJA);
		campo.setBorder(crearBordeCampo(Estilos.COLOR_CAJA));
		campo.setBounds(x, y, CAMPO_ANCHO, CAMPO_ALTO);
		panel.add(campo);
	}

	/**
	 * Campo de fecha con mascara dd/mm/aaaa (igual que en el registro).
	 */
	private JFormattedTextField crearCampoFecha() {
		JFormattedTextField campo;
		try {
			MaskFormatter mascara = new MaskFormatter("##/##/####");
			mascara.setPlaceholderCharacter('_');
			campo = new JFormattedTextField(mascara);
		} catch (ParseException e) {
			// No deberia pasar porque la mascara esta bien escrita
			campo = new JFormattedTextField();
		}
		// Si el usuario sale del campo sin terminar la fecha, no se borra lo escrito
		campo.setFocusLostBehavior(JFormattedTextField.PERSIST);
		campo.setToolTipText("Formato: dd/mm/aaaa");
		return campo;
	}

	/**
	 * Borde redondeado de 2 px del color indicado (ya trae el margen
	 * interior para el texto).
	 */
	private Border crearBordeCampo(Color color) {
		return new BordeRedondeado(color, 2);
	}

	/**
	 * Color de cada nivel. Si llega un nivel que no conoce, el de Intermedio.
	 */
	private Color obtenerColorNivel(String nivel) {
		if (nivel.equalsIgnoreCase("Novato")) {
			return Estilos.COLOR_NIVEL_NOVATO;
		} else if (nivel.equalsIgnoreCase("Principiante")) {
			return Estilos.COLOR_NIVEL_PRINCIPIANTE;
		} else if (nivel.equalsIgnoreCase("Avanzado")) {
			return Estilos.COLOR_NIVEL_AVANZADO;
		} else if (nivel.equalsIgnoreCase("Experto")) {
			return Estilos.COLOR_NIVEL_EXPERTO;
		}
		return Estilos.COLOR_NIVEL_INTERMEDIO;
	}

	// ---------------------------------------------------------------
	// Metodos que usa el controlador
	// ---------------------------------------------------------------

	/**
	 * Enseña la pestaña DATOS DEL USUARIO y quita los errores.
	 */
	public void mostrarDatos() {
		limpiarErrores();
		panelDatos.setVisible(true);
		panelPassword.setVisible(false);
		lblTitulo.setText("Datos del Usuario");
		lblPunto.setBounds(MENU_X - 15, MENU_DATOS_Y, 12, MENU_ALTO);
		txtNombre.requestFocusInWindow();
	}

	/**
	 * Enseña la pestaña CONTRASEÑA con los campos vacios y quita los errores.
	 */
	public void mostrarPassword() {
		limpiarErrores();
		vaciarPasswords();
		panelDatos.setVisible(false);
		panelPassword.setVisible(true);
		lblTitulo.setText("Contraseña");
		lblPunto.setBounds(MENU_X - 15, MENU_PASSWORD_Y, 12, MENU_ALTO);
		txtPasswordActual.requestFocusInWindow();
	}

	/**
	 * true si se esta viendo la pestaña de datos, false si es la de contraseña.
	 */
	public boolean estaEnDatos() {
		return panelDatos.isVisible();
	}

	/**
	 * Nombre y nivel de abajo a la izquierda. Ejemplo: setUsuario("Alexis",
	 * "Principiante").
	 */
	public void setUsuario(String nombre, String nivel) {
		lblNombreUsuario.setText(nombre);
		lblNivel.setText(nivel);
		lblNivel.setForeground(obtenerColorNivel(nivel));
		lblIconoNivel.setIcon(UtilidadesVista.cargarImagen("images/iconos/nivel_" + nivel.toLowerCase() + ".png"));
	}

	/**
	 * Rellena la pestaña de datos. La fecha va como "dd/mm/aaaa".
	 */
	public void setDatos(String nombre, String apellidos, String fecha, String email) {
		txtNombre.setText(nombre);
		txtApellidos.setText(apellidos);
		txtFechaNacimiento.setText(fecha);
		txtEmail.setText(email);
	}

	/**
	 * Muestra el mensaje en rojo debajo de los botones y pone el fondo de error.
	 */
	public void mostrarError(String mensaje) {
		lblMensaje.setForeground(Estilos.COLOR_ERROR);
		lblMensaje.setText(mensaje);
		lblMensaje.setVisible(true);
		panelFondo.setImagen(RUTA_FONDO_ERROR);
	}

	/**
	 * Mensaje en verde (por ejemplo "Datos guardados") y el fondo normal.
	 */
	public void mostrarCorrecto(String mensaje) {
		limpiarErrores();
		lblMensaje.setForeground(Estilos.COLOR_NIVEL_PRINCIPIANTE);
		lblMensaje.setText(mensaje);
		lblMensaje.setVisible(true);
	}

	/**
	 * Pone el borde rojo en el campo indicado.
	 * Ejemplo desde el controlador: ventana.marcarError(ventana.getTxtEmail());
	 */
	public void marcarError(JTextField campo) {
		campo.setBorder(crearBordeCampo(Estilos.COLOR_ERROR));
	}

	/**
	 * Quita el mensaje, los bordes rojos y vuelve al fondo normal.
	 */
	public void limpiarErrores() {
		lblMensaje.setText(" ");
		lblMensaje.setVisible(false);
		panelFondo.setImagen(RUTA_FONDO);

		JTextField[] campos = { txtNombre, txtApellidos, txtFechaNacimiento, txtEmail, txtPasswordActual,
				txtPasswordNueva, txtRepetirPassword };
		for (int i = 0; i < campos.length; i++) {
			campos[i].setBorder(crearBordeCampo(Estilos.COLOR_CAJA));
		}
	}

	/**
	 * Borra los tres campos de la pestaña de contraseña.
	 */
	public void vaciarPasswords() {
		txtPasswordActual.setText("");
		txtPasswordNueva.setText("");
		txtRepetirPassword.setText("");
	}

	/**
	 * Pone el cursor de escritura en el campo indicado.
	 */
	public void enfocar(JTextField campo) {
		campo.requestFocusInWindow();
	}

	public void cerrar() {
		dispose();
	}

	// ---------------------------------------------------------------
	// Getters
	// ---------------------------------------------------------------

	public String getNombre() {
		return txtNombre.getText();
	}

	public String getApellidos() {
		return txtApellidos.getText();
	}

	/**
	 * Devuelve la fecha tal cual se ve: "dd/mm/aaaa".
	 * Si no esta completa tiene guiones bajos (ej: "14/05/____").
	 */
	public String getFechaNacimiento() {
		return txtFechaNacimiento.getText();
	}

	public String getEmail() {
		return txtEmail.getText();
	}

	public String getPasswordActual() {
		return new String(txtPasswordActual.getPassword());
	}

	public String getPasswordNueva() {
		return new String(txtPasswordNueva.getPassword());
	}

	public String getRepetirPassword() {
		return new String(txtRepetirPassword.getPassword());
	}

	public JTextField getTxtNombre() {
		return txtNombre;
	}

	public JTextField getTxtApellidos() {
		return txtApellidos;
	}

	public JFormattedTextField getTxtFechaNacimiento() {
		return txtFechaNacimiento;
	}

	public JTextField getTxtEmail() {
		return txtEmail;
	}

	public JPasswordField getTxtPasswordActual() {
		return txtPasswordActual;
	}

	public JPasswordField getTxtPasswordNueva() {
		return txtPasswordNueva;
	}

	public JPasswordField getTxtRepetirPassword() {
		return txtRepetirPassword;
	}

	public JButton getBtnMenuDatos() {
		return btnMenuDatos;
	}

	public JButton getBtnMenuPassword() {
		return btnMenuPassword;
	}

	public JButton getBtnGuardar() {
		return btnGuardar;
	}

	public JButton getBtnSalir() {
		return btnSalir;
	}

	public JButton getBtnEliminarCuenta() {
		return btnEliminarCuenta;
	}

	// ---------------------------------------------------------------
	// Componentes dibujados a mano
	// ---------------------------------------------------------------

	// ====================================================================
	// DIBUJO: BOTONES REDONDEADOS (GUARDAR / SALIR / ELIMINAR CUENTA)
	// ====================================================================
	/**
	 * Boton con las esquinas redondeadas: negro con texto blanco (oscuro =
	 * true) o blanco con borde negro. Con el raton encima se pone gris.
	 * rojo = true: rojo con texto blanco (ELIMINAR CUENTA).
	 * Igual que los botones de las otras ventanas.
	 */
	private class BotonRedondeado extends JButton {

		private static final long serialVersionUID = 1L;

		private boolean oscuro;
		private boolean rojo;

		public BotonRedondeado(String texto, boolean oscuro, boolean rojo) {
			super(texto);
			this.oscuro = oscuro;
			this.rojo = rojo;
			setFont(Estilos.FUENTE_BOTON);
			setContentAreaFilled(false);
			setBorderPainted(false);
			setFocusPainted(false);
			// Sin foco: el tabulador solo pasa por los campos y Enter guarda
			setFocusable(false);
			setCursor(new Cursor(Cursor.HAND_CURSOR));
			if (oscuro) {
				setForeground(Estilos.COLOR_TEXTO_CAJA);
			} else {
				setForeground(Estilos.COLOR_CAJA);
			}
		}

		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			boolean encima = getModel().isRollover() || getModel().isPressed();

			if (rojo) {
				if (encima) {
					g2.setColor(Estilos.COLOR_ERROR_HOVER);
				} else {
					g2.setColor(Estilos.COLOR_ERROR);
				}
				g2.fillRoundRect(0, 0, getWidth(), getHeight(), Estilos.ARCO_BOTON, Estilos.ARCO_BOTON);
			} else if (oscuro) {
				if (encima) {
					g2.setColor(Estilos.COLOR_BOTON_OSCURO_HOVER);
				} else {
					g2.setColor(Estilos.COLOR_BOTON_OSCURO);
				}
				g2.fillRoundRect(0, 0, getWidth(), getHeight(), Estilos.ARCO_BOTON, Estilos.ARCO_BOTON);
			} else {
				if (encima) {
					g2.setColor(Estilos.COLOR_BOTON_CLARO_HOVER);
				} else {
					g2.setColor(Estilos.COLOR_FONDO);
				}
				g2.fillRoundRect(0, 0, getWidth(), getHeight(), Estilos.ARCO_BOTON, Estilos.ARCO_BOTON);
				// Borde negro de 1.5 px (BasicStroke es el grosor de la linea)
				g2.setColor(Estilos.COLOR_CAJA);
				g2.setStroke(new BasicStroke(1.5f));
				g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, Estilos.ARCO_BOTON, Estilos.ARCO_BOTON);
			}
			g2.dispose();
			super.paintComponent(g);
		}
	}
}
