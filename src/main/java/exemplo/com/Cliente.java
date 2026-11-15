package exemplo.com;
import java.io.*;
import java.net.*;
import java.util.*;

public class Cliente {
    static final int TOTAL = 5;
    Socket socket;
    BufferedReader et;
    PrintWriter ext;
    volatile int meuId = -1;
    volatile int vez = -1;
    volatile int rodada = 0;
    volatile int cartasRodada = 0;
    volatile boolean fasePalpite = true;
    volatile int vencedor = -2;
    volatile String erro = "";

    private final List<String> mao = new ArrayList<>();
    private final List<String[]> mesa = new ArrayList<>();
    private boolean limparMesa = false;
    private final int[] vidas = {5, 5, 5, 5, 5};
    private final int[] palpites = {-1, -1, -1, -1, -1};
    private final int[] vazas = new int[TOTAL];

    public void Iniciar() throws IOException {
        socket = new Socket("localhost", 8080);
        et = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        ext = new PrintWriter(socket.getOutputStream(), true);

        Thread t = new Thread(() -> {
            try {
                String msg;
                while ((msg = et.readLine()) != null)
                    tratarMensagem(msg);
            } catch (IOException e) {
                System.out.println("Conexao encerrada.");
            }
        });
        t.setDaemon(true);
        t.start();
    }

    private synchronized void tratarMensagem(String msg) {
        System.out.println("[Jogador " + meuId + "] " + msg);
        String[] p = msg.split("\\|");
        try {
            switch (p[0]) {
                case "ID":      meuId = Integer.parseInt(p[1]); break;
                case "VEZ":     vez = Integer.parseInt(p[1]); erro = ""; break;
                case "MAO":     mao.clear(); mao.addAll(Arrays.asList(p[1].split(","))); break;
                case "RODADA":
                    rodada = Integer.parseInt(p[1]);
                    cartasRodada = Integer.parseInt(p[2]);
                    fasePalpite = true;
                    Arrays.fill(palpites, -1);
                    Arrays.fill(vazas, 0);
                    mesa.clear(); limparMesa = false;
                    break;
                case "PALPITE":
                    palpites[Integer.parseInt(p[1])] = Integer.parseInt(p[2]);
                    break;
                case "JOGA": {
                    if (limparMesa) { mesa.clear(); limparMesa = false; }
                    int id = Integer.parseInt(p[1]);
                    mesa.add(new String[]{p[1], p[2]});
                    if (id == meuId) mao.remove(p[2]);
                    break;
                }
                case "FASE":
                    fasePalpite = false;
                    break;

                case "VAZA":
                    vazas[Integer.parseInt(p[1])]++;
                    limparMesa = true;
                    break;
                case "VIDAS":
                    for (String par : p[1].split(",")) {
                        String[] kv = par.split(":");
                        vidas[Integer.parseInt(kv[0])] = Integer.parseInt(kv[1]);
                    }
                    break;
                case "FIM":     vencedor = Integer.parseInt(p[1]); break;
                case "ERRO":    erro = p.length > 1 ? p[1] : "erro"; break;
                default: break;
            }
        } catch (Exception e) {
            System.out.println("Mensagem fora do protocolo " + msg);
        }
    }

    public synchronized List<String> getMao() {
        return new ArrayList<>(mao);
    }

    public synchronized List<String[]> getMesa() {
        return new ArrayList<>(mesa);
    }
    public synchronized int[] getVidas() {
        return vidas.clone();
    }
    public synchronized int[] getPalpites() {
        return palpites.clone();
    }
    public synchronized int[] getVazas() {
        return vazas.clone();
    }

    public boolean minhaVez() {
        return meuId >= 0 && meuId == vez;
    }

    public void envMsg(String msg) {
        if (ext != null)
            ext.println(msg);
    }
}
