package exemplo.com;
import javax.imageio.ImageIO;
import javax.swing.JPanel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Jogo extends JPanel {
    static final float W = 800f;
    static final float H = 450f;
    static final float CARTA_W = 70f, CARTA_H = 140f;
    static final float MAO_DIREITA = 790f;
    static final float MAO_Y = 30f;
    static final float MESA_W = 56f, MESA_H = 112f;
    static final float MESA_Y = 160f;
    static final float VIDAS_X = 620f, VIDAS_Y = 42f;

    static final Color VERMELHO = new Color(0.80f, 0.10f, 0.12f);
    static final Color PRETO = new Color(0.08f, 0.08f, 0.10f);
    static final Color PAPEL = new Color(0.97f, 0.95f, 0.88f);
    static final Color PAPEL_MANILHA = new Color(1f, 0.96f, 0.78f);
    static final Color BORDA = new Color(0.12f, 0.12f, 0.14f);
    static final Color BORDA_MANILHA = new Color(0.90f, 0.68f, 0.08f);
    static final Color AMARELO = new Color(255, 255, 0);
    static final Color SALMAO = new Color(250, 128, 114);

    static final String ARQUIVO_FUNDO = "/fundo.png";

    static final String[] OUROS = {
            "...X...",
            "..XXX..",
            ".XXXXX.",
            "XXXXXXX",
            ".XXXXX.",
            "..XXX..",
            "...X..."
    };
    static final String[] ESPADAS = {
            "...X...",
            "..XXX..",
            ".XXXXX.",
            "XXXXXXX",
            "XXXXXXX",
            "...X...",
            "..XXX.."
    };
    static final String[] COPAS = {
            ".XX.XX.",
            "XXXXXXX",
            "XXXXXXX",
            ".XXXXX.",
            "..XXX..",
            "...X..."
    };
    static final String[] PAUS = {
            "..XXX..",
            ".XXXXX.",
            "..XXX..",
            "XXXXXXX",
            "XXXXXXX",
            "XX.X.XX",
            "...X...",
            "..XXX.."
    };

    final Cliente cliente;
    BufferedImage fundo;
    Font font = new Font(Font.SANS_SERIF, Font.PLAIN, 14);
    Font fonteCarta = new Font(Font.SANS_SERIF, Font.BOLD, 20);
    Graphics2D g;
    List<Rectangle2D.Float> retMao = new ArrayList<>();
    List<Rectangle2D.Float> retPalpite = new ArrayList<>();
    List<String> maoDesenhada = new ArrayList<>();

    float escala = 1f, offX = 0f, offY = 0f;

    public Jogo(Cliente cliente) {
        this.cliente = cliente;
        setPreferredSize(new Dimension((int) W, (int) H));
        setBackground(Color.BLACK);
        setDoubleBuffered(true);
        carregarFundo();

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                tratarToque(e.getX(), e.getY());
            }
        });
        new javax.swing.Timer(16, e -> repaint()).start();
    }

    private void carregarFundo() {
        try (var in = Jogo.class.getResourceAsStream(ARQUIVO_FUNDO)) {
            fundo = ImageIO.read(in);
        } catch (Exception e) {
            System.out.println("Nao consegui carregar o fundo: " + e.getMessage());
        }
    }
    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        escala = Math.min(getWidth() / W, getHeight() / H);
        offX = (getWidth() - W * escala) / 2f;
        offY = (getHeight() - H * escala) / 2f;
        g.translate(offX, offY);
        g.scale(escala, escala);
        g.clipRect(0, 0, (int) W, (int) H);

        desenharFundo();
        desenharOutrosJogadores();
        desenharStatus();
        desenharMesa();
        desenharMao();
        desenharPalpite();
        desenharMinhasVidas();

        g.dispose();
    }

    private void desenharFundo() {
        if (fundo == null) {
            caixa(0, 0, W, H, new Color(0.05f, 0.25f, 0.12f));
            return;
        }
        float esc = Math.max(W / fundo.getWidth(), H / fundo.getHeight());
        float bw = fundo.getWidth() * esc;
        float bh = fundo.getHeight() * esc;
        imagem(fundo, (W - bw) / 2, (H - bh) / 2, bw, bh);
    }

    private void desenharNaipe(char naipe, float x, float yTopo, float celula) {
        String[] forma = formaNaipe(naipe);
        Color cor = (naipe == 'O' || naipe == 'C') ? VERMELHO : PRETO;
        for (int r = 0; r < forma.length; r++) {
            for (int c = 0; c < forma[r].length(); c++) {
                if (forma[r].charAt(c) == 'X')
                    caixa(x + c * celula, yTopo - (r + 1) * celula, celula, celula, cor);
            }
        }
    }

    private String[] formaNaipe(char naipe) {
        return switch (naipe) {
            case 'O' -> OUROS;
            case 'E' -> ESPADAS;
            case 'C' -> COPAS;
            default -> PAUS;
        };
    }

    private void desenharCarta(String codigo, float x, float y, float w, float h) {
        if (codigo == null || codigo.length() < 2) return;
        String rank = codigo.substring(0, codigo.length() - 1);
        char naipe = codigo.charAt(codigo.length() - 1);
        boolean manilha = Baralho.ehManilha(codigo);
        float s = w / CARTA_W;
        float borda = (manilha ? 3f : 2f) * s;
        Color corTexto = (naipe == 'O' || naipe == 'C') ? VERMELHO : PRETO;

        caixa(x, y, w, h, manilha ? BORDA_MANILHA : BORDA);
        caixa(x + borda, y + borda, w - 2 * borda, h - 2 * borda, manilha ? PAPEL_MANILHA : PAPEL);

        Font fonteAntiga = font;
        font = fonteCarta.deriveFont(19.5f * s);
        texto(rank, x + 6 * s, y + h - 5 * s, corTexto);
        textoDireita(rank, x + 4 * s, y + 18 * s, w - 10 * s, corTexto);
        font = fonteAntiga;

        float pequeno = 2f * s;
        desenharNaipe(naipe, x + 6 * s, y + h - 22 * s, pequeno);
        desenharNaipe(naipe, x + w - 6 * s - 7 * pequeno, y + 36 * s, pequeno);

        float grande = 5f * s;
        int linhas = formaNaipe(naipe).length;
        desenharNaipe(naipe, x + (w - 7 * grande) / 2f, y + h / 2f + linhas * grande / 2f, grande);
    }

    private void desenharOutrosJogadores() {
        int meu = cliente.meuId;
        int vez = cliente.vez;
        int[] vidas = cliente.getVidas();
        int[] palpites = cliente.getPalpites();
        int[] vazas = cliente.getVazas();

        int slot = 0;
        for (int j = 0; j < Cliente.TOTAL && slot < 4; j++) {
            if (j == meu) continue;
            float x = 10 + slot * 190f;
            float y = H - 68f;

            Color bg = (j == vez)
                    ? new Color(0.9f, 0.75f, 0.1f, 0.85f)
                    : new Color(0f, 0f, 0f, 0.6f);
            caixa(x, y, 182f, 60f, bg);

            Color cor = (j == vez) ? Color.BLACK : Color.WHITE;
            texto("Jogador " + j + (vidas[j] <= 0 ? " (fora)" : ""), x + 6, y + 54, cor);
            texto("Vidas: " + vidas[j], x + 6, y + 38, cor);
            String pal = palpites[j] >= 0 ? String.valueOf(palpites[j]) : "-";
            texto("Palp: " + pal + "  Vazas: " + vazas[j], x + 6, y + 22, cor);
            slot++;
        }
    }

    private void desenharStatus() {
        String texto;
        Color cor = Color.WHITE;

        if (cliente.vencedor != -2) {
            texto = cliente.vencedor == cliente.meuId
                    ? "VOCE VENCEU!"
                    : "Fim! Vencedor: jogador " + cliente.vencedor;
            cor = AMARELO;
        } else if (cliente.meuId < 0) {
            texto = "Conectando...";
        } else if (cliente.minhaVez()) {
            texto = "Rodada " + cliente.rodada + "  SUA VEZ - "
                    + (cliente.fasePalpite ? "escolha o palpite" : "jogue uma carta");
            cor = AMARELO;
        } else {
            texto = "Rodada " + cliente.rodada + "  vez do jogador " + cliente.vez;
        }

        caixa(W / 2 - 230, H - 102, 460, 26, new Color(0f, 0f, 0f, 0.6f));
        textoCentro(texto, W / 2 - 230, H - 82, 460, cor);

        String erro = cliente.erro;
        if (!erro.isEmpty())
            textoCentro(erro, W / 2 - 230, H - 110, 460, SALMAO);
    }

    private void desenharMesa() {
        List<String[]> mesa = cliente.getMesa();
        int n = mesa.size();
        if (n == 0) return;
        float total = n * (MESA_W + 10);
        float x0 = (W - total) / 2;
        for (int i = 0; i < n; i++) {
            float x = x0 + i * (MESA_W + 10);
            desenharCarta(mesa.get(i)[1], x, MESA_Y, MESA_W, MESA_H);
            texto("J" + mesa.get(i)[0], x + 16, MESA_Y - 4, Color.WHITE);
        }
    }

    private void desenharMao() {
        maoDesenhada = cliente.getMao();
        retMao.clear();
        int n = maoDesenhada.size();
        if (n == 0) return;

        boolean podeJogar = cliente.minhaVez() && !cliente.fasePalpite && cliente.vencedor == -2;
        float passo = Math.min(CARTA_W + 6, 500f / n);
        float x0 = MAO_DIREITA - (passo * (n - 1) + CARTA_W);

        for (int i = 0; i < n; i++) {
            float x = x0 + i * passo;
            float y = MAO_Y + (podeJogar ? 12 : 0);
            desenharCarta(maoDesenhada.get(i), x, y, CARTA_W, CARTA_H);
            retMao.add(new Rectangle2D.Float(x, y, CARTA_W, CARTA_H));
        }
    }

    private void desenharPalpite() {
        retPalpite.clear();
        if (!(cliente.fasePalpite && cliente.minhaVez()
                && cliente.cartasRodada > 0 && cliente.vencedor == -2)) return;

        int opcoes = cliente.cartasRodada + 1;
        float total = opcoes * 52f;
        float x0 = (W - total) / 2;
        textoCentro("Faz quantas?", W / 2 - 110, MESA_Y + 72, 220, Color.WHITE);
        for (int v = 0; v < opcoes; v++) {
            float x = x0 + v * 52f;
            Rectangle2D.Float r = new Rectangle2D.Float(x, MESA_Y + 10, 46, 36);
            caixa(r.x, r.y, r.width, r.height, new Color(0.95f, 0.9f, 0.7f));
            textoCentro(String.valueOf(v), r.x, r.y + 24, r.width, Color.BLACK);
            retPalpite.add(r);
        }
    }

    private void desenharMinhasVidas() {
        int meu = cliente.meuId;
        if (meu < 0) return;
        int[] vidas = cliente.getVidas();
        int[] palpites = cliente.getPalpites();
        int[] vazas = cliente.getVazas();

        caixa(VIDAS_X - 8, VIDAS_Y - 30, 188, 56, new Color(0f, 0f, 0f, 0.65f));
        texto("Voce (jogador " + meu + ")", VIDAS_X, VIDAS_Y + 18, Color.WHITE);
        texto("Vidas: " + vidas[meu], VIDAS_X, VIDAS_Y + 2, Color.WHITE);
        String pal = palpites[meu] >= 0 ? String.valueOf(palpites[meu]) : "-";
        texto("Palp: " + pal + "  Vazas: " + vazas[meu], VIDAS_X, VIDAS_Y - 14, Color.WHITE);
    }

    void tratarToque(int pixelX, int pixelY) {
        if (!cliente.minhaVez() || cliente.vencedor != -2) return;

        float lx = (pixelX - offX) / escala;
        float ly = H - (pixelY - offY) / escala;   // inverte o eixo Y (origem embaixo)

        if (cliente.fasePalpite) {
            for (int v = 0; v < retPalpite.size(); v++) {
                if (retPalpite.get(v).contains(lx, ly)) {
                    cliente.envMsg("PALPITE|" + v);
                    return;
                }
            }
        } else {
            for (int i = retMao.size() - 1; i >= 0; i--) {
                if (retMao.get(i).contains(lx, ly)) {
                    cliente.envMsg("JOGA|" + maoDesenhada.get(i));
                    return;
                }
            }
        }
    }

    private void caixa(float x, float y, float w, float h, Color cor) {
        g.setColor(cor);
        g.fill(new Rectangle2D.Float(x, H - y - h, w, h));
    }

    private void imagem(BufferedImage img, float x, float y, float w, float h) {
        g.drawImage(img, Math.round(x), Math.round(H - y - h), Math.round(w), Math.round(h), null);
    }


    private void texto(String s, float x, float yTopo, Color cor) {
        g.setFont(font);
        g.setColor(cor);
        g.drawString(s, x, baseline(yTopo));
    }

    private void textoCentro(String s, float x, float yTopo, float largura, Color cor) {
        g.setFont(font);
        float tw = g.getFontMetrics().stringWidth(s);
        texto(s, x + (largura - tw) / 2f, yTopo, cor);
    }

    private void textoDireita(String s, float x, float yTopo, float largura, Color cor) {
        g.setFont(font);
        float tw = g.getFontMetrics().stringWidth(s);
        texto(s, x + largura - tw, yTopo, cor);
    }

    private float baseline(float yTopo) {
        return H - yTopo + font.getSize2D() * 0.72f;
    }
}
