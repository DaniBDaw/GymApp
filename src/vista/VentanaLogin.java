package vista;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.Border;

/**
 * Ventana de login.
 * Solo es la parte visual: la comprobacion del usuario y la contrasena
 * la hace el controlador, que usa los getters y los metodos de error.
 */
public class VentanaLogin extends JFrame {

	private static final long serialVersionUID = 1L;

	private static final String RUTA_FONDO = "images/general_background1.jpg";
	private static final String RUTA_FONDO_ERROR = "images/geenral_errorBackground.jpg";

	// Posicion y tamano de la caja negra del login
	private static final int CAJA_X = 258;
	private static final int CAJA_Y = 104;
	private static final int CAJA_ANCHO = 384;
	private static final int CAJA_ALTO = 392;
	// Redondeo de las esquinas (30 = radio de 15 px)
	private static final int ARCO_CAJA = 30;

	// Imagen que sale en la caja cuando el login es correcto (384x392).
	// Es un PNG que ya tiene las esquinas redondeadas igual que la caja
	private static final String RUTA_LOGIN_CORRECTO = "images/login_correct.png";
	// Aparicion suave: cada 20 ms se sube la opacidad un 5% (unos 400 ms)
	private static final int TIEMPO_PASO_FUNDIDO = 20;
	private static final float PASO_FUNDIDO = 0.05f;

	// Circulo de carga: tamano y posicion (centrado donde estaba la caja)
	private static final int TAMANO_CARGANDO = 64;

	// Iconos
	private static final String RUTA_LOGO_BLANCO = "images/iconos/logo_blanco.png";
	private static final String RUTA_ICONO_ENTRAR = "images/iconos/entrar.png";

	// Posicion y tamano de los campos dentro de la caja
	private static final int CAMPO_X = 63;
	private static final int CAMPO_ANCHO = 258;
	private static final int CAMPO_ALTO = 34;

	private PanelFondo panelFondo;
	private PanelCaja panelCaja;
	private JLabel lblFecha;
	private JLabel lblLogo;
	private JLabel lblTitulo;
	private JLabel lblUsuario;
	private JTextField txtUsuario;
	private JLabel lblPassword;
	private JPasswordField txtPassword;
	private JButton btnRegistrar;
	private JButton btnEntrar;
	private JLabel lblError;
	private PanelCargando panelCargando;
	private JLabel lblCarga;
	private Timer timerFundido;
	private Timer timerDesvanecer;

	public VentanaLogin() {
		setTitle("GymApp - Login");
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
		crearError();
		crearCarga();

		// Al pulsar Enter en cualquier campo se pulsa el boton de entrar
		getRootPane().setDefaultButton(btnEntrar);

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
		lblFecha.setBounds(0, 45, panelFondo.getPreferredSize().width, 24);
		panelFondo.add(lblFecha);
	}

	// ====================================================================
	// PANEL: CAJA NEGRA DEL LOGIN (logo, titulo, campos y botones)
	// ====================================================================
	private void crearCaja() {
		panelCaja = new PanelCaja();
		panelCaja.setBounds(CAJA_X, CAJA_Y, CAJA_ANCHO, CAJA_ALTO);
		panelFondo.add(panelCaja);

		// Logo pequeno en blanco, centrado encima del titulo
		lblLogo = new JLabel(UtilidadesVista.cargarImagen(RUTA_LOGO_BLANCO));
		lblLogo.setHorizontalAlignment(SwingConstants.CENTER);
		lblLogo.setBounds(0, 26, CAJA_ANCHO, 24);
		panelCaja.add(lblLogo);

		lblTitulo = new JLabel("LOGIN");
		lblTitulo.setFont(Estilos.FUENTE_TITULO_LOGIN);
		lblTitulo.setForeground(Estilos.COLOR_TEXTO_CAJA);
		lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
		lblTitulo.setBounds(0, 50, CAJA_ANCHO, 64);
		panelCaja.add(lblTitulo);

		lblUsuario = crearEtiquetaCampo("USUARIO");
		lblUsuario.setBounds(0, 144, CAJA_ANCHO, 22);
		panelCaja.add(lblUsuario);

		txtUsuario = new JTextField();
		darEstiloCampo(txtUsuario);
		txtUsuario.setBounds(CAMPO_X, 168, CAMPO_ANCHO, CAMPO_ALTO);
		panelCaja.add(txtUsuario);

		lblPassword = crearEtiquetaCampo("CONTRASEÑA");
		lblPassword.setBounds(0, 222, CAJA_ANCHO, 22);
		panelCaja.add(lblPassword);

		txtPassword = new JPasswordField();
		txtPassword.setEchoChar('•');
		darEstiloCampo(txtPassword);
		txtPassword.setBounds(CAMPO_X, 246, CAMPO_ANCHO, CAMPO_ALTO);
		panelCaja.add(txtPassword);

		btnRegistrar = new BotonClaro("REGISTRAR");
		btnRegistrar.setBounds(CAMPO_X, 312, 118, 40);
		panelCaja.add(btnRegistrar);

		btnEntrar = new BotonClaro("");
		btnEntrar.setIcon(UtilidadesVista.cargarImagen(RUTA_ICONO_ENTRAR));
		btnEntrar.setToolTipText("Entrar");
		btnEntrar.setBounds(CAMPO_X + 128, 312, CAMPO_ANCHO - 128, 40);
		panelCaja.add(btnEntrar);
	}

	// ====================================================================
	// PANEL: MENSAJE DE ERROR (debajo de la caja)
	// ====================================================================
	private void crearError() {
		lblError = new JLabel(" ");
		lblError.setFont(Estilos.FUENTE_MONO_PEQUENA);
		lblError.setForeground(Estilos.COLOR_ERROR);
		lblError.setHorizontalAlignment(SwingConstants.CENTER);
		lblError.setBounds(0, CAJA_Y + CAJA_ALTO + 14, panelFondo.getPreferredSize().width, 22);
		lblError.setVisible(false);
		panelFondo.add(lblError);
	}

	// ====================================================================
	// PANEL: CIRCULO DE CARGA (sale despues del login correcto)
	// ====================================================================
	/**
	 * Circulo de carga y texto de debajo. Empiezan ocultos.
	 */
	private void crearCarga() {
		int anchoVentana = panelFondo.getPreferredSize().width;
		int centroCajaY = CAJA_Y + CAJA_ALTO / 2;

		panelCargando = new PanelCargando();
		panelCargando.setBounds((anchoVentana - TAMANO_CARGANDO) / 2, centroCajaY - TAMANO_CARGANDO / 2 - 20,
				TAMANO_CARGANDO, TAMANO_CARGANDO);
		panelCargando.setVisible(false);
		panelFondo.add(panelCargando);

		lblCarga = new JLabel(" ");
		lblCarga.setFont(Estilos.FUENTE_MONO);
		lblCarga.setForeground(Estilos.COLOR_TEXTO_SECUNDARIO);
		lblCarga.setHorizontalAlignment(SwingConstants.CENTER);
		lblCarga.setBounds(0, centroCajaY + TAMANO_CARGANDO / 2, anchoVentana, 24);
		lblCarga.setVisible(false);
		panelFondo.add(lblCarga);
	}

	// ====================================================================
	// AYUDA: ETIQUETAS Y ESTILO DE LOS CAMPOS DE LA CAJA
	// ====================================================================
	private JLabel crearEtiquetaCampo(String texto) {
		JLabel etiqueta = new JLabel(texto);
		etiqueta.setFont(Estilos.FUENTE_MONO_NEGRITA);
		etiqueta.setForeground(Estilos.COLOR_TEXTO_CAJA);
		etiqueta.setHorizontalAlignment(SwingConstants.CENTER);
		return etiqueta;
	}

	/**
	 * Campo negro con borde blanco y texto blanco, como en el diseno.
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
		lblError.setText(mensaje);
		lblError.setVisible(true);
		panelFondo.setImagen(RUTA_FONDO_ERROR);
	}

	/**
	 * Quita el mensaje, los bordes rojos y vuelve al fondo normal.
	 */
	public void limpiarErrores() {
		lblError.setText(" ");
		lblError.setVisible(false);
		panelFondo.setImagen(RUTA_FONDO);
		txtUsuario.setBorder(crearBordeCampo(Estilos.COLOR_TEXTO_CAJA));
		txtPassword.setBorder(crearBordeCampo(Estilos.COLOR_TEXTO_CAJA));
	}

	public void marcarErrorUsuario() {
		txtUsuario.setBorder(crearBordeCampo(Estilos.COLOR_ERROR));
	}

	public void marcarErrorPassword() {
		txtPassword.setBorder(crearBordeCampo(Estilos.COLOR_ERROR));
	}

	/**
	 * Escribe un texto en el campo de usuario (por ejemplo el email recien
	 * registrado, para que solo falte la contrasena).
	 */
	public void setUsuario(String usuario) {
		txtUsuario.setText(usuario);
	}

	/**
	 * Pone el cursor de escritura en el campo de usuario.
	 */
	public void enfocarUsuario() {
		txtUsuario.requestFocusInWindow();
	}

	/**
	 * Pone el cursor de escritura en el campo de contrasena.
	 */
	public void enfocarPassword() {
		txtPassword.requestFocusInWindow();
	}

	/**
	 * Login correcto: desaparece todo lo de dentro de la caja y aparece
	 * poco a poco la imagen login_correct.png.
	 */
	public void mostrarLoginCorrecto() {
		limpiarErrores();

		// Ocultar todo lo que hay dentro de la caja
		Component[] componentes = panelCaja.getComponents();
		for (int i = 0; i < componentes.length; i++) {
			componentes[i].setVisible(false);
		}

		// Que Enter ya no haga nada
		getRootPane().setDefaultButton(null);

		ImageIcon imagen = UtilidadesVista.cargarImagen(RUTA_LOGIN_CORRECTO);
		if (imagen == null) {
			return;
		}
		panelCaja.setImagen(imagen.getImage());

		// Aparicion suave de la imagen (solo visual)
		timerFundido = new Timer(TIEMPO_PASO_FUNDIDO, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				float opacidad = panelCaja.getOpacidad() + PASO_FUNDIDO;
				if (opacidad >= 1f) {
					opacidad = 1f;
					timerFundido.stop();
				}
				panelCaja.setOpacidad(opacidad);
			}
		});
		timerFundido.start();
	}

	/**
	 * Muestra el circulo de carga con el texto indicado debajo.
	 * El circulo esta DETRAS de la caja (la caja se anadio antes y se pinta
	 * encima), asi que al desaparecer la caja poco a poco se va viendo.
	 */
	public void mostrarCarga(String texto) {
		lblCarga.setText(texto);
		panelCargando.setVisible(true);
		lblCarga.setVisible(true);
		panelCargando.empezar();

		if (timerFundido != null) {
			timerFundido.stop();
		}

		// Desaparicion suave de la caja (solo visual)
		timerDesvanecer = new Timer(TIEMPO_PASO_FUNDIDO, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				float opacidad = panelCaja.getOpacidadCaja() - PASO_FUNDIDO;
				if (opacidad <= 0f) {
					timerDesvanecer.stop();
					panelCaja.setVisible(false);
				} else {
					panelCaja.setOpacidadCaja(opacidad);
				}
			}
		});
		timerDesvanecer.start();
	}

	/**
	 * Cambia el texto de debajo del circulo de carga.
	 */
	public void setTextoCarga(String texto) {
		lblCarga.setText(texto);
	}

	public void cerrar() {
		if (timerFundido != null) {
			timerFundido.stop();
		}
		if (timerDesvanecer != null) {
			timerDesvanecer.stop();
		}
		panelCargando.parar();
		dispose();
	}

	// ---------------------------------------------------------------
	// Getters
	// ---------------------------------------------------------------

	public String getUsuario() {
		return txtUsuario.getText();
	}

	public String getPassword() {
		return new String(txtPassword.getPassword());
	}

	public JButton getBtnEntrar() {
		return btnEntrar;
	}

	public JButton getBtnRegistrar() {
		return btnRegistrar;
	}

	// ---------------------------------------------------------------
	// Componentes dibujados a mano
	// ---------------------------------------------------------------

	// ====================================================================
	// DIBUJO: CAJA NEGRA REDONDEADA (y la imagen de login correcto)
	// ====================================================================
	/**
	 * Caja negra con las esquinas redondeadas donde va el formulario.
	 * Si tiene imagen (login correcto) la pinta encima con la opacidad indicada.
	 */
	private class PanelCaja extends JPanel {

		private static final long serialVersionUID = 1L;

		private Image imagen;
		// Opacidad de la imagen del check (0 = invisible, 1 = entera)
		private float opacidad;
		// Opacidad de toda la caja, para hacerla desaparecer al final
		private float opacidadCaja;

		public PanelCaja() {
			setLayout(null);
			setOpaque(false);
			opacidadCaja = 1f;
		}

		public float getOpacidadCaja() {
			return opacidadCaja;
		}

		public void setOpacidadCaja(float opacidadCaja) {
			this.opacidadCaja = opacidadCaja;
			repaint();
		}

		public void setImagen(Image imagen) {
			this.imagen = imagen;
			this.opacidad = 0f;
			repaint();
		}

		public float getOpacidad() {
			return opacidad;
		}

		public void setOpacidad(float opacidad) {
			this.opacidad = opacidad;
			repaint();
		}

		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			// Caja negra
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacidadCaja));
			g2.setColor(Estilos.COLOR_CAJA);
			g2.fillRoundRect(0, 0, getWidth(), getHeight(), ARCO_CAJA, ARCO_CAJA);

			// Imagen de login correcto encima, cada vez mas visible
			if (imagen != null) {
				g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacidad * opacidadCaja));
				g2.drawImage(imagen, 0, 0, null);
			}
			g2.dispose();
		}
	}

	// ====================================================================
	// DIBUJO: BOTON BLANCO DE LA CAJA (ENTRAR / REGISTRARSE)
	// ====================================================================
	/**
	 * Boton blanco con texto negro y las esquinas redondeadas (las mismas
	 * que los demas botones). Se pone gris claro al pasar el raton.
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
			// Sin foco: el tabulador solo pasa por los campos y Enter entra
			setFocusable(false);
			setCursor(new Cursor(Cursor.HAND_CURSOR));
		}

		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			if (getModel().isRollover() || getModel().isPressed()) {
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
