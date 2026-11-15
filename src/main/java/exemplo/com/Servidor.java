package exemplo.com;
import java.io.*;
import java.net.*;
import java.util.*;

public class Servidor {
    static PrintWriter[] play = new PrintWriter[5];
    static int vez = 0;
    static final int VIDAS_INICIAIS = 5;
    static final boolean ultimo_mao = true;
    Baralho baralho;
    List<Integer> mesaPlayer = new ArrayList<>();
    List<String> mesaCard = new ArrayList<>();
    int[] palpit = new int[5];
    int[] vida = new int[5];
    int[] vazas = new int[5];
    List<List<String>> mao = new ArrayList<>();
    boolean[] fora = new boolean[5];
    int rodada, first, ativo, cartasRodadas, palpiteRealizado;
    boolean fasePalpite;

    public Servidor() {
        for (int i = 0; i < 5; i++)
            mao.add(new ArrayList<>());
    }

    public void ServidorIniciar() throws IOException {
        ServerSocket serverSocket = new ServerSocket(8080);

        for (int i = 0; i < 5; i++) {
            Socket socket = serverSocket.accept();
            PrintWriter ext = new PrintWriter(socket.getOutputStream(), true);

            synchronized (play) {
                play[i] = ext;
            }

            ext.println("ID|" + i);

            int ID_play = i;
            new Thread(() -> PlayTratar(socket, ID_play)).start();
        }

        iniciarPartida();
    }

    public void PlayTratar(Socket socket, int i) {
        try {
            BufferedReader ent = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String msg;
            while ((msg = ent.readLine()) != null) {
                System.out.println("jogador " + i + " enviou " + msg);
                synchronized (play) {
                    processo(i, msg);
                }
            }
        } catch (IOException e) {
            System.out.println("jogador " + i + " desconectou.");
        } finally {
            synchronized (play) {
                if (!fora[i]) {
                    fora[i] = true;
                    ativo--;
                }
            }
        }
    }

    public void msgGeral(String msg) {
        synchronized (play) {
            for (int i = 0; i < play.length; i++) {
                if (play[i] != null)
                    play[i].println(msg);
            }
        }
    }



    private void iniciarPartida() {
        synchronized (play) {
            ativo = 5;
            rodada = 1;
            first = 0;
            for (int i = 0; i < 5; i++) {
                vida[i] = VIDAS_INICIAIS;
                fora[i] = false;
            }
            novaRodada();
        }
    }

    private void novaRodada() {
        cartasRodadas = ((rodada - 1) % 8) + 1;
        baralho = new Baralho();
        mesaPlayer.clear();
        mesaCard.clear();

        for (int i = 0; i < 5; i++) {
            mao.get(i).clear();
            palpit[i] = -1;
            vazas[i] = 0;
        }

        for (int volta = 0; volta < cartasRodadas; volta++)
            for (int i = 0; i < 5; i++)
                if (!fora[i])
                    mao.get(i).add(baralho.compra());


        for (int i = 0; i < 5; i++)
            if (!fora[i] && play[i] != null)
                play[i].println("MAO|" + String.join(",", mao.get(i)));

        msgGeral("RODADA|" + rodada + "|" + cartasRodadas);
        fasePalpite = true;
        palpiteRealizado = 0;
        vez = first;
        msgGeral("VEZ|" + vez);
    }



    private void processo(int i, String mensagem) {
        if (fora[i]) return;

        if (i != vez) {
            play[i].println("ERRO|nao e sua vez");
            return;
        }

        String[] p = mensagem.split("\\|");
        try {
            if (fasePalpite && p[0].equals("PALPITE"))
                palpite(i, Integer.parseInt(p[1]));
            else if (!fasePalpite && p[0].equals("JOGA"))
                jogar(i, p[1]);
            else
                play[i].println("ERRO|fora de fase");

        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            play[i].println("ERRO|mensagem invalida");
        }
    }



    private void palpite(int i, int v) {
        if (v < 0 || v > cartasRodadas) {
            play[i].println("ERRO|palpite de 0 a " + cartasRodadas);
            return;
        }
        boolean ultimo = (palpiteRealizado == ativo - 1);

        if (ultimo_mao && ultimo && somaPalpites() + v == cartasRodadas) {
            play[i].println("ERRO|o ultimo nao pode fechar a conta");
            return;
        }

        palpit[i] = v;
        palpiteRealizado++;
        msgGeral("PALPITE|" + i + "|" + v);

        if (palpiteRealizado >= ativo) {
            fasePalpite = false;
            vez = first;
            msgGeral("FASE|JOGADA");
        } else {
            vez = proximo(i);
        }
        msgGeral("VEZ|" + vez);
    }

    private void jogar(int i, String card) {
        if (!mao.get(i).remove(card)) {
            play[i].println("ERRO|carta invalida");
            return;
        }

        mesaPlayer.add(i);
        mesaCard.add(card);
        msgGeral("JOGA|" + i + "|" + card);

        if (mesaPlayer.size() >= ativo) {
            int win = vencedorDaVaza();
            vazas[win]++;
            msgGeral("VAZA|" + win);
            mesaPlayer.clear();
            mesaCard.clear();

            if (mao.get(win).isEmpty())
                fimRodada();
            else {
                vez = win;
                msgGeral("VEZ|" + vez);
            }
        } else {
            vez = proximo(i);
            msgGeral("VEZ|" + vez);
        }
    }

    private int vencedorDaVaza() {
        int m = -1;
        int win = -1;
        for (int i = 0; i < mesaCard.size(); i++) {
            int f = Baralho.nivel(mesaCard.get(i));
            if (f > m) {
                m = f;
                win = mesaPlayer.get(i);
            }
        }
        return win;
    }

    private void fimRodada() {
        List<String> placar = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            if (!fora[i]) {
                int perdeu = Math.abs(palpit[i] - vazas[i]);
                vida[i] -= perdeu;
                if (vida[i] <= 0) {
                    vida[i] = 0;
                    fora[i] = true;
                    ativo--;
                }
            }
            placar.add(i + ":" + vida[i]);
        }
        msgGeral("VIDAS|" + String.join(",", placar));

        if (ativo <= 1) {
            int win = -1;
            for (int i = 0; i < 5; i++)
                if (!fora[i])
                    win = i;
            msgGeral("FIM|" + win);
            return;
        }

        rodada++;
        first = proximo(first);
        novaRodada();
    }

    private int proximo(int a) {
        do
            a = (a + 1) % 5;
        while (fora[a]);
        return a;
    }

    private int somaPalpites() {
        int soma = 0;
        for (int i = 0; i < 5; i++)
            if (!fora[i] && palpit[i] >= 0)
                soma += palpit[i];
        return soma;
    }
}
