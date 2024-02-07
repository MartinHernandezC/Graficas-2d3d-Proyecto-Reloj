import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.io.File;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class Reloj extends JFrame implements Runnable {
    private Thread hilo;

    private JPanel area;
    
    private JPanel detalles;
    private JLabel detalles_hora;
    private javax.swing.Timer t;
    
    private int[] hora = new int[3];
    private int tamano;

    private static final int margen = 100;
    private static final float tresPi = (float)(3.0 * Math.PI);
    private static final float rad = (float)(Math.PI / 30.0);
    
    private int xCentro;
    private int yCentro;

    private Image buffer;
    private Image cara;

    File tick = new File("tick2.wav");
    
    public Reloj() {
        super("Proyecto Reloj");

        //Tomar hora del sistema y guardarla en array hora[]
        Calendar now = Calendar.getInstance();
        hora[0] = now.get(Calendar.HOUR);
        hora[1] = now.get(Calendar.MINUTE);
        hora[2] = now.get(Calendar.SECOND) + 1; 

        
        area = new JPanel();
        getContentPane().add(area, BorderLayout.CENTER);
        area.setBackground(new Color(0.0f, 0.0f, 0.0f, 0.5f));
        
        detalles = new JPanel(new FlowLayout(FlowLayout.CENTER));
        detalles_hora = new JLabel("00:00:00", JLabel.CENTER);
        detalles.add(detalles_hora);
        getContentPane().add(detalles, BorderLayout.SOUTH);
        

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 625);
        setVisible(true);

        JLabel background;
        setSize(600,600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        ImageIcon img = new ImageIcon("reloj.jpg");
        background = new JLabel("",img,JLabel.CENTER);
        this.add(background);

        //Timer cada segundo
        t = new javax.swing.Timer(1000,
              new ActionListener() {
                  public void actionPerformed(ActionEvent e) {
                      actualizar();
                  }
              });

        t.start();
        detalles_hora.setText (String.format("%02d", hora[0]) + ":" + String.format("%02d", hora[1]) + ":" + String.format("%02d", hora[2]));
        pintarReloj();
    }

    public void actualizar(){
        if (hora[2] < 60){
            hora[2]++;
        } else if (hora[1] < 60){
            hora[1]++;
            hora[2] = 0;
        } else if (hora[1] < 12) {
            hora[0]++;
            hora[1] = 0;
            hora[2] = 0;
        }

        detalles_hora.setText (String.format("%02d", hora[0]) + ":" + String.format("%02d", hora[1]) + ":" + String.format("%02d", hora[2]));

        Graphics2D g2 = (Graphics2D)area.getGraphics();
        buffer = area.createImage(area.getWidth(), area.getHeight());
        buffer.getGraphics().drawImage(cara, 0, 0, this);
        pintarManecillas(buffer.getGraphics());

        g2.drawImage(buffer, 0, 0, this);
        sonido(tick);
    }

    public void pintarReloj() {
        Graphics2D g2 = (Graphics2D)area.getGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        tamano = 600 - 2*margen;
        xCentro = tamano/2 + margen;
        yCentro = tamano/2 + margen;
        
        cara = area.createImage(area.getWidth(), area.getHeight());
        pintarCara(cara.getGraphics());

        buffer = area.createImage(area.getWidth(), area.getHeight());
        buffer.getGraphics().drawImage(cara, 0, 0, this);
        pintarManecillas(buffer.getGraphics());

        g2.setComposite(AlphaComposite.Src);
        g2.drawImage(buffer, 0, 0, this);
        sonido(tick);
    }

    private void pintarCara(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;

        g.setColor(Color.black);
        g.fillOval(margen, margen, tamano, tamano);

        g2.setColor(Color.gray);
        g2.setStroke(new BasicStroke(4));
        g2.drawOval(margen-2, margen-2, tamano+4, tamano+4);

        g2.setColor(Color.white);
        g2.setStroke(new BasicStroke(2));
        g2.drawOval(margen, margen, tamano, tamano);

        for (int seg = 0; seg<60; seg++) {
            int inicio;
            if (seg%5 == 0) {
                inicio = tamano/2-20;
            } else {
                inicio = tamano/2-10;
            }
            dibujarLinea(g, 2, Color.white, xCentro, yCentro, rad*seg, inicio , tamano/2);
        }

        Font font = new Font("Dialog", Font.BOLD, 16);
        g.setFont(font);
        g.drawString( "12", 290, 145 );
        g.drawString( "1", 378, 167 );
        g.drawString( "2", 436, 223 );
        g.drawString( "3", 460, 305 );
        g.drawString( "4", 436, 386 );
        g.drawString( "5", 381, 446 );
        g.drawString( "6", 297, 470 );
        g.drawString( "7", 218, 446 );
        g.drawString( "8", 158, 386 );
        g.drawString( "9", 135, 305 );
        g.drawString( "10", 158, 223 );
        g.drawString( "11", 208, 167 );
    }

    private void pintarManecillas(Graphics g) {
        int radio = tamano/2 - 50;
        int radioHora = (radio + 50)/2;

        float fsegundos = hora[2];
        float fminutos = (float)(hora[1] + fsegundos/60.0);
        float fhours = (float)(hora[0] + fminutos/60.0);

        float anguloHora = tresPi - (5 * rad * fhours);
        dibujarLinea(g, 4, Color.red, xCentro, yCentro, anguloHora, 0, radioHora);

        float anguloMinutero = tresPi - (rad * fminutos);
        dibujarLinea(g, 4, Color.green, xCentro, yCentro, anguloMinutero, 0, radio);

        float anguloSegundero = tresPi - (rad * fsegundos);
        dibujarLinea(g, 2, Color.yellow, xCentro, yCentro, anguloSegundero, 0, radio);
    }

    private void dibujarLinea(Graphics g, int grosor, Color color, int x, int y, double angulo, int minRadius, int maxRadius) {
        Graphics2D g2 = (Graphics2D)g;
        
        float sine   = (float)Math.sin(angulo);
        float cosine = (float)Math.cos(angulo);

        int dxmin = (int)(minRadius * sine);
        int dymin = (int)(minRadius * cosine);

        int dxmax = (int)(maxRadius * sine);
        int dymax = (int)(maxRadius * cosine);
        g2.setColor(color);
        g2.setStroke(new BasicStroke(grosor));
        g2.drawLine(x+dxmin, y+dymin, x+dxmax, y+dymax);
    }

    static void sonido(File Sound){
        try{
            Clip clip = AudioSystem.getClip();
            clip.open(AudioSystem.getAudioInputStream(Sound));
            clip.start();
        } catch (Exception e){
            System.out.println("Error Audio");
        }
    }

    public static void main(String[] args){
        new Reloj();
    }
}