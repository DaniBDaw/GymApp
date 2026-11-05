package vista;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

/**
 * JPanel que pinta una imagen de fondo a su tamano original (sin estirarla).
 * El tamano del panel es el de la primera imagen.
 * Lo usan todas las ventanas como contentPane.
 */
public class PanelFondo extends JPanel {

	private static final long serialVersionUID = 1L;

	private Image imagenFondo;

	public PanelFondo(String ruta) {
		setLayout(null);
		setBackground(Estilos.COLOR_FONDO);

		ImageIcon icono = UtilidadesVista.cargarImagen(ruta);
		if (icono != null) {
			imagenFondo = icono.getImage();
			setPreferredSize(new Dimension(icono.getIconWidth(), icono.getIconHeight()));
		} else {
			// Si no se encuentra la imagen se usa un fondo blanco de 900x600
			setPreferredSize(new Dimension(900, 600));
		}
	}

	/**
	 * Cambia la imagen de fondo (por ejemplo al fondo de error).
	 * El tamano del panel no cambia.
	 */
	public void setImagen(String ruta) {
		ImageIcon icono = UtilidadesVista.cargarImagen(ruta);
		if (icono != null) {
			imagenFondo = icono.getImage();
		} else {
			imagenFondo = null;
		}
		repaint();
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (imagenFondo != null) {
			g.drawImage(imagenFondo, 0, 0, this);
		}
	}
}
