package vista;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.text.ParseException;

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
 * Ventana de registro de un usuario nuevo.
 * Solo es la parte visual: las comprobaciones (campos vacios, email,
 * contrasenas iguales, fecha...) las hace el controlador, que usa los
 * getters y los metodos de error.
 */
public class VentanaRegistro extends JFrame {

	private static final long serialVersionUID = 1L;

	private static final String RUTA_FONDO = "images/general_background1.jpg";
	private static final String RUTA_FONDO_ERROR = "images/geenral_errorBackground.jpg";
	private static final String RUTA_LOGO_BLANCO = "images/iconos/logo_blanco.png";

	// Posicion y tamano de la caja negra (mas ancha que la del login
	// porque los campos van en dos columnas)
	private static final int CAJA_X = 150;
	private static final int CAJA_Y = 84;
	private static final int CAJA_ANCHO = 600;
	private static final int CAJA_ALTO = 420;
	private static final int ARCO_CAJA = 30;

	// Dos columnas de campos dentro de la caja
	private static final int COLUMNA_1_X = 48;
	private static final int COLUMNA_2_X = 318;
	private static final int CAMPO_ANCHO = 234;
	private static final int CAMPO_ALTO = 34;

	// Altura de cada fila (la etiqueta va 24 px encima del campo)
	private static final int FILA_1_Y = 148;
	private static final int FILA_2_Y = 222;
	private static final int FILA_3_Y = 296;
	private static final int SEPARACION_ETIQUETA = 24;

	private static final int BOTONES_Y = 356;
	private static final int BOTONES_ALTO = 40;

	private PanelFondo panelFondo;
	private PanelCaja panelCaja;
	private JLabel lblFecha;
	private JTextField txtNombre;
	private JTextField txtApellidos;
	private JTextField txtEmail;
	private JFormattedTextField txtFechaNacimiento;
	private JPasswordField txtPassword;
	private JPasswordField txtRepetirPassword;
	private JButton btnVolver;
	private JButton btnCrearCuenta;
	private JLabel lblMensaje;

	public VentanaRegistro() {
		setTitle("GymApp - Registro");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(false);

		if (Estilos.ICONO_APP != null) {
			setIconImage(Estilos.ICONO_APP);
		}
		setCursor(Estilos.CURSOR_APP);

		panelFondo = new PanelFondo(RUTA_FONDO);
		setContentPane(panelFondo);

		crearFecha();
		crearCaja();
		crearMensaje();

		// Al pulsar Enter en cualquier campo se pulsa "Crear cuenta"
		getRootPane().setDefaultButton(btnCrearCuenta);

		pack();
		setLocationRelativeTo(null);
	}

	// ---------------------------------------------------------------
	// Creacion de componentes
	// ---------------------------------------------------------------

	// ====================================================================
	// PANEL: FECHA (arriba, centrada)
	// ====================================================================
	private void crearFecha() {
		lblFecha = new JLabel(UtilidadesVista.obtenerFechaHoy());
		lblFecha.setFont(Estilos.FUENTE_MONO);
		lblFecha.setForeground(Estilos.COLOR_TEXTO_SECUNDARIO);
		lblFecha.setHorizontalAlignment(SwingConstants.CENTER);
		lblFecha.setBounds(0, 40, panelFondo.getPreferredSize().width, 24);
		panelFondo.add(lblFecha);
	}

	// ====================================================================
	// PANEL: CAJA NEGRA DEL REGISTRO (titulo, campos en dos columnas y botones)
	// ====================================================================
	private void crearCaja() {
		panelCaja = new PanelCaja();
		panelCaja.setBounds(CAJA_X, CAJA_Y, CAJA_ANCHO, CAJA_ALTO);
		panelFondo.add(panelCaja);

		JLabel lblLogo = new JLabel(UtilidadesVista.cargarImagen(RUTA_LOGO_BLANCO));
		lblLogo.setHorizontalAlignment(SwingConstants.CENTER);
		lblLogo.setBounds(0, 24, CAJA_ANCHO, 24);
		panelCaja.add(lblLogo);

		JLabel lblTitulo = new JLabel("REGISTRO");
		lblTitulo.setFont(Estilos.FUENTE_TITULO_LOGIN);
		lblTitulo.setForeground(Estilos.COLOR_TEXTO_CAJA);
		lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
		lblTitulo.setBounds(0, 48, CAJA_ANCHO, 64);
		panelCaja.add(lblTitulo);

		// Se anaden fila por fila para que el tabulador vaya en este orden:
		// nombre -> apellidos -> email -> fecha -> contrasena -> repetir

		// Fila 1: nombre y apellidos
		txtNombre = new JTextField();
		anadirCampo("NOMBRE", txtNombre, COLUMNA_1_X, FILA_1_Y);

		txtApellidos = new JTextField();
		anadirCampo("APELLIDOS", txtApellidos, COLUMNA_2_X, FILA_1_Y);

		// Fila 2: email y fecha de nacimiento
		txtEmail = new JTextField();
		anadirCampo("EMAIL", txtEmail, COLUMNA_1_X, FILA_2_Y);

		txtFechaNacimiento = crearCampoFecha();
		anadirCampo("FECHA NACIMIENTO", txtFechaNacimiento, COLUMNA_2_X, FILA_2_Y);

		// Fila 3: contrasena y repetir contrasena
		txtPassword = new JPasswordField();
		txtPassword.setEchoChar('•');
		anadirCampo("CONTRASEÑA", txtPassword, COLUMNA_1_X, FILA_3_Y);

		txtRepetirPassword = new JPasswordField();
		txtRepetirPassword.setEchoChar('•');
		anadirCampo("REPETIR CONTRASEÑA", txtRepetirPassword, COLUMNA_2_X, FILA_3_Y);

		// Botones
		btnVolver = new BotonClaro("VOLVER");
		btnVolver.setBounds(COLUMNA_1_X, BOTONES_Y, CAMPO_ANCHO, BOTONES_ALTO);
		panelCaja.add(btnVolver);

		btnCrearCuenta = new BotonClaro("CREAR CUENTA");
		btnCrearCuenta.setFont(Estilos.FUENTE_MONO_NEGRITA);
		btnCrearCuenta.setBounds(COLUMNA_2_X, BOTONES_Y, CAMPO_ANCHO, BOTONES_ALTO);
		panelCaja.add(btnCrearCuenta);
	}

	// ====================================================================
	// PANEL: MENSAJE DE ERROR / REGISTRO CORRECTO (debajo de la caja)
	// ====================================================================
	/**
	 * Mensaje debajo de la caja: rojo para errores y verde cuando el
	 * registro sale bien. Empieza oculto.
	 */
	private void crearMensaje() {
		lblMensaje = new JLabel(" ");
		lblMensaje.setFont(Estilos.FUENTE_MONO_PEQUENA);
		lblMensaje.setForeground(Estilos.COLOR_ERROR);
		lblMensaje.setHorizontalAlignment(SwingConstants.CENTER);
		lblMensaje.setBounds(0, CAJA_Y + CAJA_ALTO + 14, panelFondo.getPreferredSize().width, 22);
		lblMensaje.setVisible(false);
		panelFondo.add(lblMensaje);
	}

	// ====================================================================
	// AYUDA: CAMPOS DE LA CAJA (etiqueta, fecha con mascara y estilo)
	// ====================================================================
	/**
	 * Anade la etiqueta (centrada encima) y el campo en la posicion indicada.
	 */
	private void anadirCampo(String texto, JTextField campo, int x, int y) {
		JLabel etiqueta = new JLabel(texto);
		etiqueta.setFont(Estilos.FUENTE_MONO_NEGRITA);
		etiqueta.setForeground(Estilos.COLOR_TEXTO_CAJA);
		etiqueta.setHorizontalAlignment(SwingConstants.CENTER);
		etiqueta.setBounds(x, y - SEPARACION_ETIQUETA, CAMPO_ANCHO, 22);
		panelCaja.add(etiqueta);

		darEstiloCampo(campo);
		campo.setBounds(x, y, CAMPO_ANCHO, CAMPO_ALTO);
		panelCaja.add(campo);
	}

	/**
	 * Campo de fecha con mascara dd/mm/aaaa: solo deja escribir numeros
	 * y las barras ya estan puestas.
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
	 * Campo negro con borde blanco y texto blanco, igual que en el login.
	 */
	private void darEstiloCampo(JTextField campo) {
		campo.setFont(Estilos.FUENTE_MONO);
		campo.setBackground(Estilos.COLOR_CAJA);
		campo.setForeground(Estilos.COLOR_TEXTO_CAJA);
		campo.setCaretColor(Estilos.COLOR_TEXTO_CAJA);
		campo.setBorder(crearBordeCampo(Estilos.COLOR_TEXTO_CAJA));
	}

	/**
	 * Borde redondeado de 3 px del color indicado (ya trae el margen
	 * interior para el texto).
	 */
	private Border crearBordeCampo(Color color) {
		return new BordeRedondeado(color, 3);
	}

	// ---------------------------------------------------------------
	// Metodos para mostrar y quitar errores (los usa el controlador)
	// ---------------------------------------------------------------

	/**
	 * Muestra el mensaje en rojo debajo de la caja y pone el fondo de error.
	 */
	public void mostrarError(String mensaje) {
		lblMensaje.setForeground(Estilos.COLOR_ERROR);
		lblMensaje.setText(mensaje);
		lblMensaje.setVisible(true);
		panelFondo.setImagen(RUTA_FONDO_ERROR);
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

		JTextField[] campos = { txtNombre, txtApellidos, txtEmail, txtFechaNacimiento, txtPassword,
				txtRepetirPassword };
		for (int i = 0; i < campos.length; i++) {
			campos[i].setBorder(crearBordeCampo(Estilos.COLOR_TEXTO_CAJA));
		}
	}

	/**
	 * Registro correcto: mensaje en verde y los botones desactivados
	 * para que no se pueda pulsar dos veces.
	 */
	public void mostrarRegistroCorrecto(String mensaje) {
		limpiarErrores();
		lblMensaje.setForeground(Estilos.COLOR_NIVEL_PRINCIPIANTE);
		lblMensaje.setText(mensaje);
		lblMensaje.setVisible(true);
		btnCrearCuenta.setEnabled(false);
		btnVolver.setEnabled(false);
	}

	/**
	 * Borra las dos contrasenas (por ejemplo cuando no coinciden).
	 */
	public void vaciarPasswords() {
		txtPassword.setText("");
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

	public String getEmail() {
		return txtEmail.getText();
	}

	/**
	 * Devuelve la fecha tal cual se ve: "dd/mm/aaaa".
	 * Si no esta completa tiene guiones bajos (ej: "14/05/____").
	 */
	public String getFechaNacimiento() {
		return txtFechaNacimiento.getText();
	}

	public String getPassword() {
		return new String(txtPassword.getPassword());
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

	public JTextField getTxtEmail() {
		return txtEmail;
	}

	public JFormattedTextField getTxtFechaNacimiento() {
		return txtFechaNacimiento;
	}

	public JPasswordField getTxtPassword() {
		return txtPassword;
	}

	public JPasswordField getTxtRepetirPassword() {
		return txtRepetirPassword;
	}

	public JButton getBtnVolver() {
		return btnVolver;
	}

	public JButton getBtnCrearCuenta() {
		return btnCrearCuenta;
	}

	// ---------------------------------------------------------------
	// Componentes dibujados a mano
	// ---------------------------------------------------------------

	// ====================================================================
	// DIBUJO: CAJA NEGRA REDONDEADA
	// ====================================================================
	/**
	 * Caja negra con las esquinas redondeadas donde va el formulario.
	 */
	private class PanelCaja extends JPanel {

		private static final long serialVersionUID = 1L;

		public PanelCaja() {
			setLayout(null);
			setOpaque(false);
		}

		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setColor(Estilos.COLOR_CAJA);
			g2.fillRoundRect(0, 0, getWidth(), getHeight(), ARCO_CAJA, ARCO_CAJA);
			g2.dispose();
		}
	}

	// ====================================================================
	// DIBUJO: BOTON BLANCO DE LA CAJA (VOLVER / CREAR CUENTA)
	// ====================================================================
	/**
	 * Boton blanco con texto negro y las esquinas redondeadas. Se pone gris
	 * claro al pasar el raton (igual que el del login).
	 */
	private class BotonClaro extends JButton {

		private static final long serialVersionUID = 1L;

		public BotonClaro(String texto) {
			super(texto);
			setFont(Estilos.FUENTE_MONO);
			setForeground(Estilos.COLOR_CAJA);
			setContentAreaFilled(false);
			setBorderPainted(false);
			setFocusPainted(false);
			// Sin foco: el tabulador solo pasa por los campos y Enter crea la cuenta
			setFocusable(false);
			setCursor(new Cursor(Cursor.HAND_CURSOR));
		}

		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			if (!isEnabled()) {
				g2.setColor(Estilos.COLOR_BOTON_CLARO_HOVER);
			} else if (getModel().isRollover() || getModel().isPressed()) {
				g2.setColor(Estilos.COLOR_BOTON_CLARO_HOVER);
			} else {
				g2.setColor(Estilos.COLOR_BOTON_CLARO);
			}
			g2.fillRoundRect(0, 0, getWidth(), getHeight(), Estilos.ARCO_BOTON, Estilos.ARCO_BOTON);
			g2.dispose();
			super.paintComponent(g);
		}
	}
}
