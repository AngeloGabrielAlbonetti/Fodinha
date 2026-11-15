package exemplo.com;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Main {
    Servidor servidor = new Servidor();
    Cliente cliente = new Cliente();
    List<Process> filhos = new CopyOnWriteArrayList<>();
    JFrame janela;
    boolean tituloDefinido = false;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().create());
    }

    public void create() {
        boolean primeira = portaLivre();

        new Thread(() -> {
            try {
                if (primeira) {
                    Thread t = new Thread(() -> {
                        try {
                            servidor.ServidorIniciar();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                    t.setDaemon(true);
                    t.start();
                }

                conectar();

                if (primeira) {
                    for (int i = 1; i < 5; i++) {
                        filhos.add(abrirJanela());
                        Thread.sleep(500);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        criarJanela();
    }

    private void criarJanela() {
        janela = new JFrame("Fodinha");
        janela.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        janela.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });
        janela.add(new Jogo(cliente));
        janela.pack();
        janela.setLocationByPlatform(true);
        janela.setVisible(true);

        new javax.swing.Timer(100, e -> {
            if (!tituloDefinido && cliente.meuId >= 0) {
                janela.setTitle("Fodinha - Jogador " + cliente.meuId);
                tituloDefinido = true;
            }
        }).start();
    }

    private boolean portaLivre() {
        try (ServerSocket s = new ServerSocket(8080)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private void conectar() throws InterruptedException {
        for (int t = 0; t < 10; t++) {
            try {
                cliente.Iniciar();
                return;
            } catch (IOException e) {
                Thread.sleep(300);
            }
        }
    }

    private Process abrirJanela() throws IOException {
        String exe = ProcessHandle.current().info().command().orElse("");
        String nome = new File(exe).getName().toLowerCase();
        if (nome.endsWith(".exe") && !nome.equals("java.exe") && !nome.equals("javaw.exe"))
            return new ProcessBuilder(exe).inheritIO().start();

        String java = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
        String principal = System.getProperty("sun.java.command").split(" ")[0];

        ProcessBuilder pb;
        if (principal.endsWith(".jar"))
            pb = new ProcessBuilder(java, "-jar", principal);
        else
            pb = new ProcessBuilder(java, "-cp", System.getProperty("java.class.path"), principal);

        return pb.inheritIO().start();
    }

    public void dispose() {
        for (Process p : filhos) p.destroy();
        System.exit(0);
    }
}