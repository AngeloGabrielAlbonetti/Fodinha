package exemplo.com;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Baralho {
    static final String[] forca = {"4", "5", "6", "7", "Q", "J", "K", "A", "2", "3"};
    static final char[] naipes = {'O', 'E', 'C', 'P'};
    static final String[] manilhas = {"7O", "AE", "7C", "4P"};
    private final List<String> cartas = new ArrayList<>();

    public Baralho() {
        for (char naipe : naipes)
            for (String rank : forca)
                cartas.add(rank + naipe);

        Collections.shuffle(cartas);
    }

    public static int indice(String[] strings, String string) {
        for (int i = 0; i < strings.length; i++)
            if (strings[i].equals(string))
                return i;
        return -1;
    }

    public String compra() {
        return cartas.remove(0);
    }

    public static boolean ehManilha(String carta) {
        return indice(manilhas, carta) >= 0;
    }

    public static int nivel(String carta) {
        int t = indice(manilhas, carta);
        if (t >= 0)
            return 100 + t;
        String rank = carta.substring(0, carta.length() - 1);
        return indice(forca, rank);
    }
}
