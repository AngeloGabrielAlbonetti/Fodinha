package io.github.angelogabrielalbonetti.fodinha;
import java.io.*;
import java.net.*;


public class Servidor {
    static PrintWriter[] play = new PrintWriter[5];
    static int contador = 0;

    public void ServidorIniciar() throws IOException{
        ServerSocket serverSocket = new ServerSocket(750);


        for (int i = 0; i < 5; i++) {
            Socket socket = serverSocket.accept();
            PrintWriter ext = new PrintWriter(socket.getOutputStream(), true);

            play[i] = ext;
        }
    }
}
