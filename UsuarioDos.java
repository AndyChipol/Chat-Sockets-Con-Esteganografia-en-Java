
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

/**
 *
 * @author Andres cruz chipol
 */
public class UsuarioDos extends javax.swing.JFrame {

   
    Socket socket = null;
    Socket socketImagen = null;
    BufferedReader leer = null;
    PrintWriter escribir = null;
    PrintWriter img = null;
    BufferedReader leerImagen = null;    
    
    String linkImagen = null;
    public UsuarioDos() {
        initComponents();
        Thread mainImagen = new Thread(new Runnable(){
            public void run(){
                try {
                    socketImagen = new Socket("localhost",9886);                    
                    escribeImagen();
                    leerImagen();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        
        Thread main1 = new Thread(new Runnable(){
            public void run(){
                try {
                    socket = new Socket("localhost",9800);
                    leer();
                    escribe();
                    escribeEncriptar();
                    escribeDesencriptar();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        mainImagen.start();
        main1.start();
    }

    public void leerImagen(){
        Thread hilo = new Thread(new Runnable(){
            public void run(){
                try {
                    leerImagen =  new BufferedReader(new InputStreamReader(socketImagen.getInputStream()));
                    while(true){
                        
                        
                        String link = leerImagen.readLine();
                        System.out.println(link);
                        //Mostrar imagen en Jlabel
                        //
                        Image img = new ImageIcon(link).getImage();
                        Image img2 = getScaledImage(img, 200, 200);
                        
                        jLabel1.setIcon(new ImageIcon(img2));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                
            } 
        });
        hilo.start();
    }
        
    public void leer(){
        Thread hilo = new Thread(new Runnable(){
            public void run(){
                try {
                    //Obtener la entdada del socket
                    leer = new BufferedReader(new InputStreamReader(socket.getInputStream()));    
                    
                    while (true) {                
                       String mensajeRecibe =  leer.readLine();
                        String nuevoMensaje = recibeMensaje(mensajeRecibe);
                        chat.append( "\n"+"Usuario 1: " + nuevoMensaje); 
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } 
        });
        hilo.start();
    }
    
    public void escribe(){
        Thread hilo = new Thread(new Runnable(){
            public void run(){
                try {
                    escribir = new PrintWriter(socket.getOutputStream(),true);
                    enviar.addActionListener(new ActionListener(){
                        @Override
                        public void actionPerformed(ActionEvent ae) {
                            chat.append("\n"+ "Usuario 2: "+mensajeNormal.getText());
                            escribir.println(enviaMensaje(mensajeNormal.getText()));
                            mensajeNormal.setText("");
                        }
                    });
                   
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        hilo.start();
    }
    
    public void escribeImagen(){
        Thread hilo = new Thread(new Runnable(){
            public void run(){
                try {
                    img =  new PrintWriter(socketImagen.getOutputStream(),true);                    
                    imgEnviar.addActionListener(new ActionListener(){
                        @Override
                        public void actionPerformed(ActionEvent ae) {
                            estego(linkImagen,imgTexto.getText());
                            img.println("C:\\Users\\andy_\\Desktop\\UsuarioDos.png");
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        hilo.start();
    }
    
    
    
    /*Esteganografia*/
    public static void estego(String link,String prueba){
        try{
            File imagen = new File(link);  
            System.out.print(link);
            BufferedImage img = ImageIO.read(imagen);
            //get image width and height
            int width = img.getWidth();
            int height = img.getHeight();
            //prueba = "Hola mis queridos amigos";
            //longituDeTexto = prueba.length();
            String binarioTexto = "";
            binarioTexto = asciiBinario(prueba);
            //System.out.println("Texto Binario:" + binarioTexto);        
        int contadorTexto = 0;
        int lenBinarioTexto = binarioTexto.length();
        
        for(int y = 0; y < height; y++){
          for(int x = 0; x < width; x++){
            int p = img.getRGB(x, y);
            Color c = new Color (p);
            int R = c.getRed();
            int G = c.getGreen();
            int B = c.getBlue();
            String binarioR = String.format("%8s", Integer.toBinaryString(R & 0xFF)).replace(' ', '0');
            String binarioG = String.format("%8s", Integer.toBinaryString(G & 0xFF)).replace(' ', '0');
            String binarioB = String.format("%8s", Integer.toBinaryString(B & 0xFF)).replace(' ', '0');
           
            int lenR = binarioR.length();
            int lenG = binarioG.length();
            int lenB = binarioB.length();
            
            if((contadorTexto - lenBinarioTexto) != 0){
                String a = ""+binarioTexto.charAt(contadorTexto);
                binarioR = binarioR.replaceFirst(".$",a);
                ++contadorTexto;
            }
            if((contadorTexto - lenBinarioTexto) != 0){
                String a = ""+binarioTexto.charAt(contadorTexto);
                binarioG = binarioG.replaceFirst(".$",a);
                ++contadorTexto;
            }
            if((contadorTexto - lenBinarioTexto) != 0){
                String a = ""+binarioTexto.charAt(contadorTexto);
                binarioB = binarioB.replaceFirst(".$",a);
                ++contadorTexto;
            }
            R = Integer.parseInt(binarioR, 2);
            G = Integer.parseInt(binarioG, 2);
            B = Integer.parseInt(binarioB, 2);
            
            Color d = new Color(R,G,B);
            p = d.getRGB();
            //System.out.print(p);
            img.setRGB(x, y, p);
          }
        }
            ImageIO.write(img,"png",new File("C:\\Users\\andy_\\Desktop\\UsuarioDos.png"));
            System.out.println("Listo");
        }catch(IOException e){}
       
    }
    
    /**/
    
     public static String visualizar(int numLet){
        String binarioEstego = "";
         try{
            File imagen = new File("C:\\Users\\andy_\\Desktop\\UsuarioUno.png");    
            BufferedImage img = ImageIO.read(imagen);
            //get image width and height
            int width = img.getWidth();
            int height = img.getHeight();
            String prueba = "Hola mis queridos amigos";
            String binarioTexto = "";
            binarioTexto = asciiBinario(prueba);
            //System.out.println("Texto Binario:" + binarioTexto);
        int contadorTexto = 0;
        int lenBinarioTexto = binarioTexto.length();
        int numerosLetra = numLet * 8; //50 letras
        
        for(int y = 0; y < height; y++){
          for(int x = 0; x < width; x++){
            int p = img.getRGB(x, y);
            Color c = new Color (p);
            
            int R = c.getRed();
            int G = c.getGreen();
            int B = c.getBlue();
            
            String binarioR = String.format("%8s", Integer.toBinaryString(R & 0xFF)).replace(' ', '0');
            String binarioG = String.format("%8s", Integer.toBinaryString(G & 0xFF)).replace(' ', '0');
            String binarioB = String.format("%8s", Integer.toBinaryString(B & 0xFF)).replace(' ', '0'); 
           
            binarioEstego += binarioR.charAt(binarioR.length() - 1);
            numerosLetra = numerosLetra - 1;
            if(numerosLetra == 0){
                break;
            }
            binarioEstego += binarioG.charAt(binarioG.length() - 1);
            numerosLetra = numerosLetra - 1;
            if(numerosLetra == 0){
                break;
            }
            binarioEstego += binarioB.charAt(binarioB.length() - 1);
            numerosLetra = numerosLetra - 1;
            if(numerosLetra == 0){
                break;
            }
            System.out.println(numerosLetra);
        }
            
            break;
          }
        
           
        }catch(IOException e){}       
        return binarioAscci(binarioEstego);
    }
    
    
    private Image getScaledImage(Image srcImg, int w, int h){
        BufferedImage resizedImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = resizedImg.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(srcImg, 0, 0, w, h, null);
        g2.dispose();

        return resizedImg;
    }
    
    public static String enviaMensaje(String mensaje){
        String texto = "";
        for (int i = 0; i < mensaje.length(); i++) {
            texto +=  String.valueOf( ((int)(mensaje.charAt(i))))+ ",";    
        }
        return texto;
    }

    public static String recibeMensaje(String mensaje){
        String texto = "";
        StringTokenizer tokens= new StringTokenizer(mensaje, ",");
        int ascii;
        while(tokens.hasMoreTokens()){
             ascii = (Integer.valueOf(tokens.nextToken()));
             texto += (char)ascii;
        }
        return texto;
    }

    public void escribeEncriptar(){

        Thread hilo = new Thread(new Runnable(){
            @Override
            public void run() {
                try {
                    escribir = new PrintWriter(socket.getOutputStream(),true);
                    enviarCifrado.addActionListener(new ActionListener(){
                                char a = '0';
                                char b = '1';
                                String patron = ""+ a+b+b+a+b+a+b;
                        @Override
                        public void actionPerformed(ActionEvent ae) {
                            chat.append("\n"+"Usuario 1: "+desMiniEncriptar(msnLlano.getText(),llave1.getText(),llave2.getText(),llave3.getText()));
                            escribir.println(enviaMensaje(desMiniEncriptar(msnLlano.getText(),llave1.getText(),llave2.getText(),llave3.getText())));
                            msnLlano.setText("");
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        hilo.start();
    }
    
    public void escribeDesencriptar(){
        Thread hilo = new Thread(new Runnable(){
            @Override
            public void run() {
                try {
                    descifrar.addActionListener(new ActionListener(){
                                char a = '1';
                                char b = '0';
                                String patron2 = ""+ a+a+b+a+a+a+b;
                        @Override
                        public void actionPerformed(ActionEvent ae) {
                            txtDescifrado.setText(desMiniDescencriptar(txtDescifrado.getText(),llave6.getText(),llave5.getText(),llave4.getText()));                            
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        hilo.start();
    }

    
    public static String desMiniDescencriptar(String mensaje, String llave1, String llave2, String llave3){
        String texto = "";

    
        String transposicion2 = desencriptrar(llave3, mensaje);
        String xor1  = rellenoDescencriptar(transposicion2, llave2);
        
        String transposicion1 = desencriptrar(llave1, xor1);
        
        texto = transposicion1;
        return texto;
    }

    public static String desMiniEncriptar(String mensaje, String llave1, String llave2, String llave3){
        int divMsj = 2;
        String texto = "";
        HillMessage msj = new HillMessage(
            mensaje,
            divMsj,
            llave1
        );

        
        String transposicion1 = encriptrar(msj);
        String xor1  = rellenoEncriptar(transposicion1, llave2);

        HillMessage msj2 = new HillMessage(
            xor1,
            divMsj,
            llave3
        );

        String transposicion2 = encriptrar(msj2);
        
        texto = transposicion2;

        return texto;
    }
        
    public static String configuracion(String textoLlano, String patron){
        String texto = "";
        texto = xorOperador(asciiBinario(textoLlano), patron);
        return texto;
     }
 
    public static String binarioAscci(String binario){
         String texto = "";
         int lon = binario.length();
         int suma = 0;
         for(int i = 0; i < lon; i+=8){
             suma = 0;
             int contador = 7;
             for(int j = i; j < i+8; j++){
                 if(binario.charAt(j) == '1'){
                     suma = suma + (int)Math.pow(2,contador);
                 }
                 --contador;
             }
             texto = texto + (char)suma;
         }
         return texto;
     }
 
    public static String xorOperador(String prueba,String patron){
         String xor = "";
         int lon = prueba.length();
         int lonPatron = patron.length();
         int contador = 0;
 
         for(int i =0; i < lon; i++){
            // System.out.print(prueba.charAt(i) +" " + patron.charAt(contador) + " = ");
             if(prueba.charAt(i) == patron.charAt(contador)){
                 xor  = xor + "0";
                 //System.out.print("0\n");
             }
             else{
                 xor = xor + "1";
                 //System.out.print("1\n");
             }
             if(contador == lonPatron -1 ){
                 contador = 0;
             }
             ++contador;
         }
         return binarioAscci(xor);
     }
 
    public static String intBinario(int ascii) {
         if (ascii <= 0) {
             return "0";
         }
         ascii = ascii % 256; 
         String binario = "";
         while (ascii > 0) {
             short remainder = (short) (ascii % 2);
             ascii = ascii / 2;
             binario = String.valueOf(remainder) + binario;
         }
         if(binario.length() < 8){
             int contador = 8 - binario.length();
             for(int i = 0; i < contador;i++){
                 binario = "0" + binario;
             }
         }
         return binario;
     }
 
    public static String asciiBinario(String texto) {
         String binario = "";
         for (int i = 0; i < texto.length(); i++) {
             char caracter = texto.charAt(i);
             int ascii = (int) caracter;
             String binarioDec = intBinario(ascii);
             binario += binarioDec;
         }
         return binario;
     }
 
    public static boolean verificarClave(String clave){
         String claveReal = "110000111100001111000011110000111100001111000011110000111100001111000011";
         String bin = "";
         bin = asciiBinario(clave);
         String claveProcesada = "";
         claveProcesada =  cajas(bin);
         return claveReal.equals(claveProcesada);
    }
 
    // SPSP
     public static String cajas(String texto){
         String textoBin = "";
         int longitud = texto.length();
         String prueba = "";
         for (int i = 0; i < longitud; i+=8) {
             for (int j = 0; j < 8; j++) {
                 prueba = prueba + texto.charAt(j);
             }
            textoBin = textoBin + permutaicionDos(sustitucionDos(permutaicionUno(sustitucionUno(prueba))));
            //System.out.println(textoBin);
            prueba = "";
         }
 
         return textoBin;
     }

     public static String cajasDes(String texto){
        String textoBin = "";
        int longitud = texto.length();
        String prueba = "";
        for (int i = 0; i < longitud; i+=8) {
            for (int j = 0; j < 8; j++) {
                prueba = prueba + texto.charAt(j);
            }
           textoBin = textoBin + sustitucionUnoDes(permutaicionUnoDes(sustitucionDosDes(permutaicionDosDes(prueba))));
           //System.out.println(textoBin);
           prueba = "";
        }

        return textoBin;
    }


    public static String permutaicionUnoDes(String binario){
        String texto = "";
        
        char s0 = binario.charAt(0);
        char s1 = binario.charAt(1);
        char s2 = binario.charAt(2);
        char s3 = binario.charAt(3);
        char s4 = binario.charAt(4);
        char s5 = binario.charAt(5);
        char s6 = binario.charAt(6);
        char s7 = binario.charAt(7);
        // 3 1 0 5 6 7 4 2
        texto = texto + s3 + s1 + s0 + s5 + s6 + s7 + s4 + s2;
        return texto;
    }

    public static String permutaicionUno(String binario){
         String texto = "";
         
         char s0 = binario.charAt(0);
         char s1 = binario.charAt(1);
         char s2 = binario.charAt(2);
         char s3 = binario.charAt(3);
         char s4 = binario.charAt(4);
         char s5 = binario.charAt(5);
         char s6 = binario.charAt(6);
         char s7 = binario.charAt(7);

         char ss0 = s2;
         char ss1 = s1;
         char ss2 = s7;
         char ss3 = s0;
         char ss4 = s6;
         char ss5 = s3;
         char ss6 = s4;
         char ss7 = s5;
         
         texto = texto + ss0 + ss1 + ss2 + ss3 + ss4 + ss5 + ss6 + ss7;
         return texto;
     }
 
     public static String sustitucionUno(String binario){
         String texto = "";
         char s0 = binario.charAt(0);
         char s1 = binario.charAt(1);
         char s2 = binario.charAt(2);
         char s3 = binario.charAt(3);
         char s4 = binario.charAt(4);
         char s5 = binario.charAt(5);
         char s6 = binario.charAt(6);
         char s7 = binario.charAt(7);
 
         texto = texto + s3 + s6 + s0 + s7 + s1 + s2 + s4 + s5  ; 
         
         return texto;
     }
 
     public static String sustitucionUnoDes(String binario){
        String texto = "";
        char s0 = binario.charAt(0);
        char s1 = binario.charAt(1);
        char s2 = binario.charAt(2);
        char s3 = binario.charAt(3);
        char s4 = binario.charAt(4);
        char s5 = binario.charAt(5);
        char s6 = binario.charAt(6);
        char s7 = binario.charAt(7);
        // s2 s4 s5 s0 s6 s7 s1 s3
        texto = texto + s2 + s4 + s5 + s0 + s6 + s7 + s1 + s3  ; 

        return texto;
    }
     
    public static String permutaicionDos(String binario){
         String texto = "";
         char s0 = binario.charAt(0);
         char s1 = binario.charAt(1);
         char s2 = binario.charAt(2);
         char s3 = binario.charAt(3);
         char s4 = binario.charAt(4);
         char s5 = binario.charAt(5);
         char s6 = binario.charAt(6);
         char s7 = binario.charAt(7);
 
         char ss0 = s3 ;
         char ss1 = s6;
         char ss2 = s0;
         char ss3 = s7;
         char ss4 = s1;
         char ss5 = s2;
         char ss6 = s4;
         char ss7 = s5;

         texto = texto + ss0 + ss1 + ss2 + ss3 + ss4 + ss5 + ss6 + ss7;
         return texto;
     }

    public static String permutaicionDosDes(String binario){
        String texto = "";
        char s0 = binario.charAt(0);
        char s1 = binario.charAt(1);
        char s2 = binario.charAt(2);
        char s3 = binario.charAt(3);
        char s4 = binario.charAt(4);
        char s5 = binario.charAt(5);
        char s6 = binario.charAt(6);
        char s7 = binario.charAt(7);
         // s2 s4 s5 s0 s6 s7 s1 s3
        texto = texto + s2 + s4 + s5 + s0 + s6 + s7 + s1 + s3;
        return texto;
    }
 
    public static String sustitucionDos(String binario){
         String texto = "";
 
         char s0 = binario.charAt(0);
         char s1 = binario.charAt(1);
         char s2 = binario.charAt(2);
         char s3 = binario.charAt(3);
         char s4 = binario.charAt(4);
         char s5 = binario.charAt(5);
         char s6 = binario.charAt(6);
         char s7 = binario.charAt(7);
         
         texto = texto+ s7 + s4 + s2 + s1 + s6 + s3 + s5 + s0; 
 
         return texto;
     }
 
    public static String sustitucionDosDes(String binario){
        String texto = "";

        char s0 = binario.charAt(0);
        char s1 = binario.charAt(1);
        char s2 = binario.charAt(2);
        char s3 = binario.charAt(3);
        char s4 = binario.charAt(4);
        char s5 = binario.charAt(5);
        char s6 = binario.charAt(6);
        char s7 = binario.charAt(7);
        // s7 s3 s2 s5 s1 s6 s4 s0
        texto = texto+ s7 + s3 + s2 + s5 + s1 + s6 + s4 + s0; 

        return texto;
    }
    // XOR 
    public static String rellenoEncriptar(String mensaje, String llave){
        int lonMensaje = mensaje.length();
        int lonLlave = llave.length();
        String mensajeCifrado = ""; 
        int contador = 0;
        for (int i = 0; i < lonMensaje; i++) {
            mensajeCifrado = mensajeCifrado + (char)(mensaje.charAt(i) ^ llave.charAt(contador));
           // System.out.print((mensaje.charAt(i) ^ llave.charAt(contador)) + " ");
           // System.out.print(mensaje.charAt(i)  + " ");
           // System.out.print(llave.charAt(contador) + " ");
           // System.out.println();
            contador++;
            if(contador == lonLlave){
                contador = 0;
            }
        }
        return mensajeCifrado;
    }

    public static String rellenoDescencriptar(String mensaje, String llave){
        String mensajeDescifrado = "";
        int lonMensaje = mensaje.length();
        int lonLlave = llave.length();
        int contador = 0;

        for (int i = 0; i < lonMensaje; i++) {
            mensajeDescifrado = mensajeDescifrado + (char)(mensaje.charAt(i)^llave.charAt(contador));
            contador++;
            if(contador == lonLlave){
                contador = 0;
            }
        }

        return mensajeDescifrado;
    }


    // Transposicion
    /*
              Clave sera NxN 
        msj = new HillMessage("libni que bonita estas",4,"agua");
        encriptrar(msj);
        Nuestro alfabeto es el ASCII
        DIVIDIR textoClaro en bloques de longitud N=2
        String mensaje = "Hola mis amigos como estan?";
        //String mensaje = "hola amigos";
        System.out.print(mensaje.length());
        //String llave = "wed932as5";
        //String llave = "a 6W";        String llave = "nprt";
        //sllave = "☻♦◘►";
        String llave = "bfjn";
        //int divMsj = (int)Math.sqrt(llave.length());
    
    
    */    


    public static String encriptrar(HillMessage msj){
        // System.out.println("\nLLave K:");
         int keyNxN[][] = getKey(msj.llave);
        // System.out.println("\nMatriz M:");
         int matrizM[][] = getMessage(msj.textoClaro,msj.longitudN);
         int n = msj.longitudN;
         int n3 = (int) Math.sqrt(msj.llave.length());
         int n2 = (int)Math.ceil((float)msj.textoClaro.length()/n);
         int matrizC[][] = new int [n][(int)Math.ceil((float)msj.textoClaro.length()/n)];
         String mensaje = "";
         int contadorRelleno = 0 ;
         int lenMensaje = msj.textoClaro.length();
 
         int matrizCifrada[] = new int [n*n2];
         int suma = 0;
         int contadork = 0;
         for(int i = 0; i < n2; i++){ 
             for(contadork = 0; contadork < n; contadork++){
                 suma = 0;
                 for(int j=0; j < n; j++){                    
                     //System.out.print(matrizM[j][i] + " ->");
                     //System.out.println(keyNxN[contadork][j]+ "*"+matrizM[j][i] + "="+ keyNxN[contadork][j] * matrizM[j][i]);
                     suma += keyNxN[contadork][j] * matrizM[j][i] ;
                 } 
                 //System.out.println("Suma:" + suma + " Modulo-255= " + (suma % 255) + " Simbolo:" +(char)(suma % 255));
                 mensaje =  mensaje + (char)(suma % 255);
                 //System.out.println(mensaje);
             }
             
             suma = 0;
         }
         return mensaje;
     }
 
    public static String desencriptrar(String llave, String mensaje){
         //Matriz = la llave inversa por C  modulo |A|
         //System.out.println("");
         int [][] matrizMensaje = getMessage(mensaje,2);
         //System.out.println("");
         int [][] key = getKey(llave);
         int [][] keyInversa = inversa(key);
         String texto = "";
         for (int i = 0; i < mensaje.length()/2; i++) {
             texto = texto + ((char)((keyInversa[0][0] * matrizMensaje[0][i] + keyInversa[0][1] * matrizMensaje[1][i])%255));
             texto = texto + ((char)((keyInversa[1][0] * matrizMensaje[0][i] + keyInversa[1][1] * matrizMensaje[1][i])%255));
         }
         return texto;
     }
 
    public static int[][] inversa(int[][] matriz){
         int [][] resultado = new int[2][2];
         resultado[0][0] = matriz[1][1];
         resultado[0][1] = matriz[0][1] * -1;
         resultado[1][0] = matriz[1][0] * -1;
         resultado[1][1] = matriz[0][0];
         int determinante = (matriz[0][0]*matriz[1][1]) - (matriz[0][1]*matriz[1][0]);
         int escalar = 256/determinante;
         //System.out.println("Determinante:"+determinante);
 
         for (int i = 0; i < resultado.length; i++) {
             for (int j = 0; j < resultado.length; j++) {
                 resultado[i][j] = resultado[i][j]*escalar;
             }
         }
         for (int i = 0; i < resultado.length; i++) {
             for (int j = 0; j < resultado.length; j++) {
               //  System.out.print(resultado[i][j] + " ");
             }
            // System.out.println("");
         }
 
 
         for (int i = 0; i < resultado.length; i++) {
             for (int j = 0; j < resultado.length; j++) {
                 int mod = resultado[i][j];
                 if(mod >= 0){
                     resultado[i][j] = resultado[i][j]%255;
                 }else{
                     int div = resultado[i][j]/255;
                     int mul = (div*-1) * 255;
                     resultado[i][j] =( resultado[i][j] + mul ) + 255;
                 }
             }
         }
 
         for (int i = 0; i < resultado.length; i++) {
             for (int j = 0; j < resultado.length; j++) {
                 //System.out.print(resultado[i][j] + " ");
             }
            // System.out.println("");
         }
 
         return resultado;
     }
 
    public static int [][] getMessage(String mensaje,int n) {
         //CREACION DE LA MATRIZ M 
         int n2 = (int)Math.ceil((float)mensaje.length()/n);
         //System.out.println("n2 = "+n2);
         int lenMensaje = mensaje.length();
         int contadorRelleno = 0 ;
         int matrizMensaje[][] = new int [n][n2];
         //RELLENAR LA MATRIZ CON ESPACIOS
         for (int i = 0; i < n; i++) 
             for (int j = 0; j < n2; j++) 
                 matrizMensaje[i][j] = (int)' ';
         //RELLENAR LA MATRIZ CON EL MENSAJE
         for (int j = 0; j < n2; j++) {
             for (int i = 0; i < n; i++) {
                 if(contadorRelleno < lenMensaje){
                     matrizMensaje[i][j] = mensaje.charAt(contadorRelleno);
                     contadorRelleno++;  
                 }
             }
         }
         //Visualizar la matriz
         for (int i = 0; i < n; i++) {
             for (int j = 0; j < n2; j++) {
                 //System.out.print(matrizMensaje[i][j]+" ");
             }
             //System.out.println("");
         }
         //System.out.println("");
         for (int i = 0; i < n; i++) {
             for (int j = 0; j < n2; j++) {
                 //System.out.print((char)matrizMensaje[i][j]+" ");
             }
             //System.out.println("");
         }
         return matrizMensaje;
     }
     //La llave tiene que tener NxN si no no acepta la clave
     // 2x2 = 4, 1x1 = 1, 3x3 = 9, 4x4 = 16, 5x5 = 25
    public static int[][] getKey(String llave){
         int n =(int) Math.sqrt(llave.length());
         int llaveMatriz[][] = new int[n][n];
         int contador = 0;
         //Convertir Matriz
         for (int i = 0; i < n; i++) {
             for (int j = 0; j < n; j++) {
                 llaveMatriz[i][j] = llave.charAt(contador);              
                 contador++;
             }
 
         }
         //Visualizar matriz
         for (int i = 0; i < n; i++) {
             for (int j = 0; j < n; j++) {
                 //System.out.print(llaveMatriz[i][j] +" ");
             }
             //System.out.println("");
         }
         
         for (int i = 0; i < n; i++) {
             for (int j = 0; j < n; j++) {
                // System.out.print((char)llaveMatriz[i][j] +" ");
             }
             //System.out.println("");
         }
         return llaveMatriz;
     }
 

    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel4 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        chat = new javax.swing.JTextArea();
        jTabbedPane2 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        mensajeNormal = new javax.swing.JTextArea();
        enviar = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        enviarCifrado = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        msnLlano = new javax.swing.JTextArea();
        jScrollPane5 = new javax.swing.JScrollPane();
        llave1 = new javax.swing.JTextPane();
        jScrollPane6 = new javax.swing.JScrollPane();
        llave3 = new javax.swing.JTextPane();
        jScrollPane7 = new javax.swing.JScrollPane();
        llave2 = new javax.swing.JTextPane();
        jPanel3 = new javax.swing.JPanel();
        descifrar = new javax.swing.JButton();
        jScrollPane4 = new javax.swing.JScrollPane();
        txtDescifrado = new javax.swing.JTextArea();
        jScrollPane8 = new javax.swing.JScrollPane();
        llave4 = new javax.swing.JTextPane();
        jScrollPane9 = new javax.swing.JScrollPane();
        llave5 = new javax.swing.JTextPane();
        jScrollPane10 = new javax.swing.JScrollPane();
        llave6 = new javax.swing.JTextPane();
        jPanel5 = new javax.swing.JPanel();
        imgEnviar = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        jScrollPane11 = new javax.swing.JScrollPane();
        imgTexto = new javax.swing.JTextArea();
        jScrollPane12 = new javax.swing.JScrollPane();
        longitud = new javax.swing.JTextPane();
        estego = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Usuario 2");
        setBackground(new java.awt.Color(255, 255, 255));
        setResizable(false);

        jPanel4.setBackground(new java.awt.Color(255, 255, 255));

        jScrollPane1.setBorder(null);

        chat.setEditable(false);
        chat.setColumns(20);
        chat.setFont(new java.awt.Font("Yu Gothic UI Semilight", 0, 18)); // NOI18N
        chat.setLineWrap(true);
        chat.setRows(5);
        chat.setBorder(null);
        jScrollPane1.setViewportView(chat);

        jTabbedPane2.setBackground(new java.awt.Color(255, 255, 255));
        jTabbedPane2.setToolTipText("");
        jTabbedPane2.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jTabbedPane2.setFont(new java.awt.Font("Yu Gothic UI Semibold", 1, 12)); // NOI18N
        jTabbedPane2.setOpaque(true);
        jTabbedPane2.setPreferredSize(new java.awt.Dimension(417, 224));

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jPanel1.setPreferredSize(new java.awt.Dimension(415, 195));

        jScrollPane2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 255, 102), 2));

        mensajeNormal.setColumns(20);
        mensajeNormal.setFont(new java.awt.Font("Yu Gothic UI", 0, 12)); // NOI18N
        mensajeNormal.setLineWrap(true);
        mensajeNormal.setRows(5);
        mensajeNormal.setBorder(null);
        jScrollPane2.setViewportView(mensajeNormal);

        enviar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/btnOficial.png"))); // NOI18N
        enviar.setBorder(null);
        enviar.setBorderPainted(false);
        enviar.setContentAreaFilled(false);
        enviar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(enviar)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 590, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 20, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap(40, Short.MAX_VALUE)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(enviar)
                .addGap(24, 24, 24))
        );

        jTabbedPane2.addTab("Normal", jPanel1);

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));

        enviarCifrado.setFont(new java.awt.Font("Yu Gothic UI", 0, 11)); // NOI18N
        enviarCifrado.setIcon(new javax.swing.ImageIcon(getClass().getResource("/btnOficial.png"))); // NOI18N
        enviarCifrado.setBorder(null);
        enviarCifrado.setContentAreaFilled(false);
        enviarCifrado.setFocusPainted(false);

        jScrollPane3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 255, 105), 2));

        msnLlano.setColumns(20);
        msnLlano.setFont(new java.awt.Font("Yu Gothic UI", 0, 12)); // NOI18N
        msnLlano.setLineWrap(true);
        msnLlano.setRows(5);
        msnLlano.setPreferredSize(new java.awt.Dimension(220, 80));
        jScrollPane3.setViewportView(msnLlano);

        jScrollPane5.setViewportView(llave1);

        jScrollPane6.setViewportView(llave3);

        jScrollPane7.setViewportView(llave2);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 370, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 51, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(enviarCifrado, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 127, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane5, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(52, 52, 52))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 22, Short.MAX_VALUE)
                        .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(enviarCifrado))
                    .addComponent(jScrollPane3))
                .addGap(26, 26, 26))
        );

        jTabbedPane2.addTab("Cifrar", jPanel2);

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));

        descifrar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Componente 6 – 1.png"))); // NOI18N
        descifrar.setBorder(null);
        descifrar.setBorderPainted(false);
        descifrar.setContentAreaFilled(false);
        descifrar.setFocusPainted(false);
        descifrar.setFocusable(false);
        descifrar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                descifrarActionPerformed(evt);
            }
        });

        jScrollPane4.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 255, 105), 2));

        txtDescifrado.setColumns(20);
        txtDescifrado.setFont(new java.awt.Font("Yu Gothic UI", 0, 12)); // NOI18N
        txtDescifrado.setLineWrap(true);
        txtDescifrado.setRows(5);
        jScrollPane4.setViewportView(txtDescifrado);

        jScrollPane8.setViewportView(llave4);

        jScrollPane9.setViewportView(llave5);

        jScrollPane10.setViewportView(llave6);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(39, 39, 39)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 415, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(13, 13, 13)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(descifrar, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 139, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jScrollPane10, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jScrollPane9, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jScrollPane8, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(23, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(26, 26, 26)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(descifrar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jScrollPane10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jScrollPane9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jScrollPane8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 158, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(29, Short.MAX_VALUE))
        );

        jTabbedPane2.addTab("Descifrar", jPanel3);

        jPanel5.setBackground(new java.awt.Color(255, 255, 255));

        imgEnviar.setBackground(new java.awt.Color(102, 255, 102));
        imgEnviar.setText("Enviar Imagen");
        imgEnviar.setBorder(null);
        imgEnviar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                imgEnviarActionPerformed(evt);
            }
        });

        jButton1.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jButton1.setText("Buscar Imagen");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jScrollPane11.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 255, 105), 2));

        imgTexto.setColumns(20);
        imgTexto.setFont(new java.awt.Font("Yu Gothic UI", 0, 12)); // NOI18N
        imgTexto.setLineWrap(true);
        imgTexto.setRows(5);
        imgTexto.setPreferredSize(new java.awt.Dimension(220, 80));
        jScrollPane11.setViewportView(imgTexto);

        jScrollPane12.setViewportView(longitud);

        estego.setBackground(new java.awt.Color(102, 255, 102));
        estego.setText("Visualizar Texto");
        estego.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                estegoActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jButton1)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jScrollPane11, javax.swing.GroupLayout.PREFERRED_SIZE, 329, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 29, Short.MAX_VALUE)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addComponent(jScrollPane12, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(estego, javax.swing.GroupLayout.PREFERRED_SIZE, 127, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(imgEnviar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(26, 26, 26))))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap(24, Short.MAX_VALUE)
                .addComponent(jButton1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane11, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                        .addComponent(imgEnviar, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(estego, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                                .addComponent(jScrollPane12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(3, 3, 3)))))
                .addGap(22, 22, 22))
        );

        jTabbedPane2.addTab("Imagenes", jPanel5);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 372, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(22, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane1)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jTabbedPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 243, Short.MAX_VALUE))
        );

        jTabbedPane2.getAccessibleContext().setAccessibleName("Descifrar");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void descifrarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_descifrarActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_descifrarActionPerformed

    private void imgEnviarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_imgEnviarActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_imgEnviarActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        JFileChooser chooser = new JFileChooser();
        chooser.showOpenDialog(null);
        File f = chooser.getSelectedFile();
        String filename = f.getAbsolutePath();
        linkImagen = filename;
        Image img= new ImageIcon(filename).getImage();
        Image img2 = getScaledImage(img, 200, 200);
        jLabel1.setIcon(new ImageIcon(img2));
    }//GEN-LAST:event_jButton1ActionPerformed

    private void estegoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_estegoActionPerformed
        imgTexto.setText(visualizar(Integer.parseInt(longitud.getText())));
    }//GEN-LAST:event_estegoActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Windows".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(UsuarioDos.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(UsuarioDos.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(UsuarioDos.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(UsuarioDos.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
new UsuarioDos().setVisible(true);
        /*
        String clave = JOptionPane.showInputDialog("Usuario 2 Introduce Clave:");
        
        if(verificarClave(clave))
            new UsuarioDos().setVisible(true);
        else 
            JOptionPane.showMessageDialog(null, "Acceso Denegado");
        */

    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    public javax.swing.JTextArea chat;
    public javax.swing.JButton descifrar;
    public javax.swing.JButton enviar;
    public javax.swing.JButton enviarCifrado;
    public javax.swing.JButton estego;
    public javax.swing.JButton imgEnviar;
    public javax.swing.JTextArea imgTexto;
    private javax.swing.JButton jButton1;
    public javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane10;
    private javax.swing.JScrollPane jScrollPane11;
    private javax.swing.JScrollPane jScrollPane12;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JScrollPane jScrollPane8;
    private javax.swing.JScrollPane jScrollPane9;
    private javax.swing.JTabbedPane jTabbedPane2;
    public javax.swing.JTextPane llave1;
    public javax.swing.JTextPane llave2;
    public javax.swing.JTextPane llave3;
    public javax.swing.JTextPane llave4;
    public javax.swing.JTextPane llave5;
    public javax.swing.JTextPane llave6;
    public javax.swing.JTextPane longitud;
    public javax.swing.JTextArea mensajeNormal;
    public javax.swing.JTextArea msnLlano;
    public javax.swing.JTextArea txtDescifrado;
    // End of variables declaration//GEN-END:variables
}

