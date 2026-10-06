package io.github.angelogabrielalbonetti.fodinha;
import java.io.*;
import java.net.*;
import java.util.*;


public class Servidor {
    static PrintWriter[] play = new PrintWriter[5];
    static int contador = 0;

    public void ServidorIniciar() throws IOException{
        ServerSocket serverSocket = new ServerSocket(8080);


        for (int i = 0; i < 5; i++) {
            Socket socket = serverSocket.accept();
            PrintWriter ext = new PrintWriter(socket.getOutputStream(), true);

            play[i] = ext;

            int ID_play =i;
            new Thread(() -> PlayTratar(socket, ID_play)).start();

        }
        msgGeral("vez 0");
    }

    public void PlayTratar(Socket socket, int i){
        try {
            BufferedReader ent = new BufferedReader(new InputStreamReader(socket.getInputStream() ));
            String msg;
            while ((msg = ent.readLine()) != null){
                System.out.println("jogador" + i +"envidou" + msg);
                synchronized (this){
                    contador = (contador + 1) % 5;
                    msgGeral(msg);
                    msgGeral("VEZ" + contador);
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void msgGeral(String msg){
        synchronized (play){
            for (int i = 0; i < play.length && play[i] != null; i++) {
                play[i].println(msg);
            }
        }
    }
}
