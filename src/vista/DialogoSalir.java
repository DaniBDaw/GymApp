package vista;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

/**
 * Aviso que sale al pulsar SALIR en el workout en curso. Es un JDialog sin
 * bordes del mismo tamano que la ventana y justo encima de ella. De fondo
 * tiene una foto borrosa de la ventana y en el centro una caja blanca con
 * CANCELAR y SALIR. Mientras esta abierto no se puede tocar la ventana de
 * detras (modal). Los botones los escucha el controlador.
 * Tambien se usa para confirmar ELIMINAR CUENTA en Ajustes (segundo
 * constructor: otro titulo, otro texto y el boton en rojo).
 */
public class DialogoSalir extends JDialog {

	private static final long serialVersionUID = 1L;

	// Capa oscura encima de la foto (el cuarto numero es la transparencia)
	private static final Color COLOR_OSCURECER = new Color(0, 0, 0, 70);

	// Caja blanca del centro
	private static final int CAJA_ANCHO = 440;
	private static final int CAJA_ALTO = 230;

	private static final Font FUENTE_TITULO = new Font(Estilos.FAMILIA_FUENTE, Font.BOLD, 28);

	private JButton btnCancelar;
	private JButton btnSalir;

	/**
	 * Aviso de salir del workout: "¿Salir del workout?" con CANCELAR y SALIR.
	 */
	public DialogoSalir(JFrame ventana, String mensaje) {
		this(ventana, "¿Salir del workout?", mensaje, "SALIR", false);
	}

	/**
	 * Aviso con otro titulo y otro texto en el boton de confirmar (el de la
	 * derecha). botonRojo = true lo pinta en rojo (para borrar cosas).
	 * Ejemplo: new DialogoSalir(ventana, "¿Eliminar tu cuenta?", "...", "ELIMINAR", true)
	 */
	public DialogoSalir(JFrame ventana, String titulo, String mensaje, String textoBoton, boolean botonRojo) {
		super(ventana, true);
		setUndecorated(true);

		// Mismo tamano y posicion que el contenido de la ventana de detras
		Container contenido = ventana.getContentPane();
		setSize(contenido.getSize());
		setLocation(contenido.getLocationOnScreen());

		PanelBorroso panel = new PanelBorroso(UtilidadesVista.crearFotoBorrosa(contenido));
		setContentPane(panel);

		crearCaja(panel, titulo, mensaje, textoBoton, botonRojo);
	}

	// ====================================================================
	// PANEL: CAJA BLANCA DEL CENTRO (titulo, mensaje y botones)
	// ====================================================================
	private void crearCaja(JPanel panel, String titulo, String mensaje, String textoBoton, boolean botonRojo) {
		JPanel caja = new CajaBlanca();
		caja.setLayout(null);
		caja.setBounds((getWidth() - CAJA_ANCHO) / 2, (getHeight() - CAJA_ALTO) / 2, CAJA_ANCHO, CAJA_ALTO);
		panel.add(caja);

		JLabel lblTitulo = new JLabel(titulo);
		lblTitulo.setFont(FUENTE_TITULO);
		lblTitulo.setForeground(Estilos.COLOR_CAJA);
		lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
		lblTitulo.setBounds(0, 26, CAJA_ANCHO, 40);
		caja.add(lblTitulo);

		// JTextArea para que el mensaje pueda ocupar dos lineas
		JTextArea txtMensaje = new JTextArea(mensaje);
		txtMensaje.setFont(Estilos.FUENTE_MONO);
		txtMensaje.setForeground(Estilos.COLOR_TEXTO_SECUNDARIO);
		txtMensaje.setLineWrap(true);
		txtMensaje.setWrapStyleWord(true);
		txtMensaje.setEditable(false);
		txtMensaje.setFocusable(false);
		txtMensaje.setOpaque(false);
		txtMensaje.setBounds(40, 80, CAJA_ANCHO - 80, 60);
		caja.add(txtMensaje);

		btnCancelar = new BotonDialogo("CANCELAR", false, false);
		btnCancelar.setBounds(40, 158, 170, 46);
		caja.add(btnCancelar);

		btnSalir = new BotonDialogo(textoBoton, true, botonRojo);
		btnSalir.setBounds(CAJA_ANCHO - 40 - 170, 158, 170, 46);
		caja.add(btnSalir);
	}

	public void cerrar() {
		dispose();
	}

	// ---------------------------------------------------------------
	// Getters
	// ---------------------------------------------------------------

	public JButton getBtnCancelar() {
		return btnCancelar;
	}

	public JButton getBtnSalir() {
		return btnSalir;
	}

	// ---------------------------------------------------------------
	// Componentes dibujados a mano
	// ---------------------------------------------------------------

	// ====================================================================
	// DIBUJO: FONDO BORROSO (la foto borrosa + capa oscura)
	// ====================================================================
	private class PanelBorroso extends JPanel {

		private static final long serialVersionUID = 1L;

		private BufferedImage fotoBorrosa;

		public PanelBorroso(BufferedImage fotoBorrosa) {
			this.fotoBorrosa = fotoBorrosa;
			setLayout(null);
		}

		@Override
		protected void paintComponent(Graphics g) {
			g.drawImage(fotoBorrosa, 0, 0, null);
			g.setColor(COLOR_OSCURECER);
			g.fillRect(0, 0, getWidth(), getHeight());
		}
	}

	// ====================================================================
	// DIBUJO: CAJA BLANCA REDONDEADA
	// ====================================================================
	private class CajaBlanca extends JPanel {

		private static final long serialVersionUID = 1L;

		public CajaBlanca() {
			setOpaque(false);
		}

		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setColor(Estilos.COLOR_FONDO);
			g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
			g2.dispose();
		}
	}

	// ====================================================================
	// DIBUJO: BOTONES CANCELAR / SALIR
	// ====================================================================
	/**
	 * oscuro = true: negro con texto blanco. false: blanco con borde negro.
	 * rojo = true: rojo con texto blanco (ELIMINAR). Con el raton encima se
	 * pone mas oscuro / gris.
	 */
	private class BotonDialogo extends JButton {

		private static final long serialVersionUID = 1L;

		private boolean oscuro;
		private boolean rojo;

		public BotonDialogo(String texto, boolean oscuro, boolean rojo) {
			super(texto);
			this.oscuro = oscuro;
			this.rojo = rojo;
			setFont(Estilos.FUENTE_BOTON);
			setContentAreaFilled(false);
			setBorderPainted(false);
			setFocusPainted(false);
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
				g2.setColor(Estilos.COLOR_CAJA);
				g2.setStroke(new BasicStroke(1.5f));
				g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, Estilos.ARCO_BOTON, Estilos.ARCO_BOTON);
			}
			g2.dispose();
			super.paintComponent(g);
		}
	}
}
