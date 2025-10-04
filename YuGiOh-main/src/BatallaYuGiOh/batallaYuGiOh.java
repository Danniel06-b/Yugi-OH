package BatallaYuGiOh;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.net.http.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import org.json.*;

public class batallaYuGiOh {
    // --- Componentes del GUI ---
    // Estas son las etiquetas, campos de texto y botones del panel principal.

    private JLabel imagen1, imagen2, imagen3, imagen4, imagen5, imagen6;
    private JTextField campoNombreCarta1, campoAtk1, campoDef1;
    private JTextField campoNombreCarta2, campoAtk2, campoDef2;
    private JTextField campoNombreCarta3, campoAtk3, campoDef3;
    private JTextField campoNombreCarta4, CampoAtk4, CampoDef4;
    private JTextField campoNombreCarta5, campoAtk5, campoDef5;
    private JTextField campoNombreCarta6, campoAtk6, campoDef6;
    private JButton CARTASButton, INICIARDUELOButton;
    private JPanel mainPanel;
    private JScrollPane Scroll;
    private JButton ATACARButton4, ATACARButton5, ATACARButton6;
    private JButton DEFENDERButton4, DEFENDERButton5, DEFENDERButton6;

    // Área de texto para mostrar lo que va pasando durante el duelo

    private final JTextArea logArea = new JTextArea();

    // Clase interna que representa una carta del juego
    private static class Card {
        String name;
        int atk;
        int def;
        String imageUrl;
        boolean usada = false;          // si la carta ya se utilizó en batalla
        boolean enDefensa = false;      // si está colocada en modo defensa

        Card(String name, int atk, int def, String imageUrl) {
            this.name = name;
            this.atk = atk;
            this.def = def;
            this.imageUrl = imageUrl;
        }
    }

    // Conexión a la API de YGOProDeck para obtener cartas aleatorias
    private static class YgoApiClient {
        private final HttpClient client = HttpClient.newHttpClient();
        private final String baseUrl = "https://db.ygoprodeck.com/api/v7/cardinfo.php";
        private final Random rnd = new Random();


        // Devuelve una carta monstruo aleatoria desde la API
        Card fetchRandomMonsterCard() throws Exception {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(java.net.URI.create(baseUrl))
                    .GET()
                    .build();
            HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
            JSONObject obj = new JSONObject(resp.body());
            JSONArray data = obj.getJSONArray("data");

            // Se filtran solo las cartas que son de tipo "Monster"
            List<JSONObject> monsters = new ArrayList<>();
            for (int i = 0; i < data.length(); i++) {
                JSONObject cardJson = data.getJSONObject(i);
                if (cardJson.optString("type", "").contains("Monster")) {
                    monsters.add(cardJson);
                }
            }
            // Escoge una carta aleatoria del listado
            JSONObject cardJson = monsters.get(rnd.nextInt(monsters.size()));
            String name = cardJson.optString("name", "Unknown");
            int atk = cardJson.optInt("atk", 0);
            int def = cardJson.optInt("def", 0);
            String imageUrl = cardJson.getJSONArray("card_images")
                    .getJSONObject(0)
                    .getString("image_url");
            return new Card(name, atk, def, imageUrl);
        }
    }

    //    Estado del juego
    private final YgoApiClient api = new YgoApiClient();
    private final Card[] playerSlots = new Card[3];
    private final Card[] machineSlots = new Card[3];

    private int scoreJugador = 0, scoreMaquina = 0, roundsPlayed = 0;
    private boolean dueloActivo = false;
    private boolean turnoJugador;

    // Cartas en campo o defensa
    private Card cartaEnCampoMaquina = null;       // carta que la máquina puso como atacante esta ronda (si hay)
    private Card cartaEnDefensaMaquina = null;     // carta que la máquina tiene en defensa (preservada o puesta)
    private Card cartaEnDefensaJugador = null;     // carta que el jugador tiene en defensa (preservada o puesta)

    // Banderas para saber si se defendió en la ronda actual
    private boolean jugadorDefendioEstaRonda = false;
    private boolean maquinaDefendioEstaRonda = false;

    public batallaYuGiOh() {
        // Configuración inicial de la interfaz
        logArea.setEditable(false);
        Scroll.setViewportView(logArea);
        Scroll.setPreferredSize(new Dimension(400, 200));

        imagen1.setText(""); imagen1.setOpaque(false);
        imagen2.setText(""); imagen2.setOpaque(false);
        imagen3.setText(""); imagen3.setOpaque(false);
        imagen4.setText(""); imagen4.setOpaque(false);
        imagen5.setText(""); imagen5.setOpaque(false);
        imagen6.setText(""); imagen6.setOpaque(false);

        JTextField[] campos = {
                campoNombreCarta1, campoAtk1, campoDef1,
                campoNombreCarta2, campoAtk2, campoDef2,
                campoNombreCarta3, campoAtk3, campoDef3,
                campoNombreCarta4, CampoAtk4, CampoDef4,
                campoNombreCarta5, campoAtk5, campoDef5,
                campoNombreCarta6, campoAtk6, campoDef6
        };
        for (JTextField t : campos) if (t != null) t.setEditable(false);

        // Botón CARTAS
        // Carga cartas nuevas para el jugador y la máquina

        CARTASButton.addActionListener(e -> {
            try {
                resetGameState();
                for (int i = 0; i < 3; i++) {
                    playerSlots[i] = api.fetchRandomMonsterCard();
                    machineSlots[i] = api.fetchRandomMonsterCard();
                }
                mostrarCartas();
                // Asignar listeners a los botones de ataque y defensa del jugador

                ATACARButton4.addActionListener(ev -> accionJugador(0, true));
                ATACARButton5.addActionListener(ev -> accionJugador(1, true));
                ATACARButton6.addActionListener(ev -> accionJugador(2, true));

                DEFENDERButton4.addActionListener(ev -> accionJugador(0, false));
                DEFENDERButton5.addActionListener(ev -> accionJugador(1, false));
                DEFENDERButton6.addActionListener(ev -> accionJugador(2, false));

                habilitarBotonesJugador(false);
                appendLog("Cartas cargadas. Pulsa INICIAR DUELO.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(mainPanel, "Error: " + ex.getMessage());
            }
        });

        // Botón INICIAR DUELO
        INICIARDUELOButton.addActionListener(e -> {
            if (playerSlots[0] == null) {
                JOptionPane.showMessageDialog(mainPanel, "Primero carga las cartas.");
                return;
            }
            resetRoundFlags(); // aseguramos flags limpios al iniciar
            dueloActivo = true;
            turnoJugador = new Random().nextBoolean();   // el turno se elige al azar

            if (turnoJugador) {
                appendLog("===== Turno del Jugador =====");
                habilitarBotonesJugador(true);
            } else {
                appendLog("===== Turno de la Máquina =====");
                turnoMaquina();
            }
        });
    }


    // Resetea todo el estado del duelo
    private void resetGameState() {
        scoreJugador = scoreMaquina = roundsPlayed = 0;
        dueloActivo = false;
        cartaEnCampoMaquina = null;
        cartaEnDefensaMaquina = null;
        cartaEnDefensaJugador = null;
        resetRoundFlags();
        for (int i = 0; i < 3; i++) {
            playerSlots[i] = null;
            machineSlots[i] = null;
        }
        logArea.setText("");
    }

    private void resetRoundFlags() {
        jugadorDefendioEstaRonda = false;
        maquinaDefendioEstaRonda = false;
    }

    private void appendLog(String text) {
        logArea.append(text + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    // --- Mostrar cartas en la interfaz ---

    private void mostrarCartas() throws Exception {
        setCardData(campoNombreCarta1, campoAtk1, campoDef1, imagen1, machineSlots[0]);
        setCardData(campoNombreCarta2, campoAtk2, campoDef2, imagen2, machineSlots[1]);
        setCardData(campoNombreCarta3, campoAtk3, campoDef3, imagen3, machineSlots[2]);

        setCardData(campoNombreCarta4, CampoAtk4, CampoDef4, imagen4, playerSlots[0]);
        setCardData(campoNombreCarta5, campoAtk5, campoDef5, imagen5, playerSlots[1]);
        setCardData(campoNombreCarta6, campoAtk6, campoDef6, imagen6, playerSlots[2]);
    }

    private void setCardData(JTextField nombre, JTextField atk, JTextField def, JLabel lbl, Card c) throws Exception {
        if (c == null) return;
        nombre.setText(c.name);
        atk.setText("ATK: " + c.atk);
        def.setText("DEF: " + c.def);
        lbl.setIcon(new ImageIcon(new ImageIcon(new URL(c.imageUrl))
                .getImage().getScaledInstance(120, 180, Image.SCALE_SMOOTH)));
    }

    // --- LÓGICA DEL JUGADOR ---
    // Dependiendo si ataca o defiende, se resuelve la jugada.

    private void accionJugador(int index, boolean atacar) {
        if (!dueloActivo || !turnoJugador) return;
        Card j = playerSlots[index];
        if (j == null || j.usada) return;

        if (atacar) {
            // El jugador ataca
            jugadorDefendioEstaRonda = false;
            // 1. Si la máquina tenía un atacante en campo
            j.enDefensa = false;

            // Caso 1: hay un atacante de la máquina en campo -> ATK vs ATK
            if (cartaEnCampoMaquina != null) {
                String ganador = resolverAtaque(j, cartaEnCampoMaquina, true);
                appendLog("Jugador ataca con " + j.name + " (ATK:" + j.atk + ")"
                        + " vs Máquina atacó con " + cartaEnCampoMaquina.name + " (ATK:" + cartaEnCampoMaquina.atk + ")"
                        + " -> Ganador: " + ganador);
                // gastar ambas cartas
                j.usada = true;
                cartaEnCampoMaquina.usada = true;
                cartaEnCampoMaquina = null;
                roundsPlayed++;
                deshabilitarBotones(index);
            }
            // 2. Si la máquina defendió
            else if (maquinaDefendioEstaRonda && cartaEnDefensaMaquina != null) {
                String ganador = resolverAtaque(j, cartaEnDefensaMaquina, true);
                appendLog("Jugador ataca con " + j.name + " (ATK:" + j.atk + ")"
                        + " vs Máquina defendió con " + cartaEnDefensaMaquina.name + " (DEF:" + cartaEnDefensaMaquina.def + ")"
                        + " -> Ganador: " + ganador);
                j.usada = true;
                cartaEnDefensaMaquina.usada = true;
                cartaEnDefensaMaquina = null;
                maquinaDefendioEstaRonda = false;
                roundsPlayed++;
                deshabilitarBotones(index);
            }
            // 3. Si la máquina no hizo nada, se elige defensa aleatoria
            else {
                Card defensaM = elegirCartaMaquina(false);
                if (defensaM == null) {
                    scoreJugador++;
                    appendLog("Jugador ataca, pero la máquina no tenía carta → punto para el jugador.");
                    j.usada = true;
                    deshabilitarBotones(index);
                } else {
                    String ganador = resolverAtaque(j, defensaM, true);
                    appendLog("Jugador ataca con " + j.name + " (ATK:" + j.atk + ")"
                            + " vs Máquina defendió con " + defensaM.name + " (DEF:" + defensaM.def + ")"
                            + " -> Ganador: " + ganador);
                    j.usada = true;
                    defensaM.usada = true;
                    deshabilitarBotones(index);
                }
                roundsPlayed++;
            }
        } else {
            // El jugador decide defender
            j.enDefensa = true;
            cartaEnDefensaJugador = j;
            jugadorDefendioEstaRonda = true;
            appendLog("El jugador se pone en DEFENSA con " + j.name + " (DEF:" + j.def + ")");
            // Si la máquina ya había atacado, se resuelve inmediatamente
            if (cartaEnCampoMaquina != null) {
                String ganador = resolverAtaque(cartaEnCampoMaquina, j, false);
                appendLog("Máquina atacó con " + cartaEnCampoMaquina.name + " (ATK:" + cartaEnCampoMaquina.atk + ")"
                        + " vs Jugador defendió con " + j.name + " (DEF:" + j.def + ")"
                        + " -> Ganador: " + ganador);
                // ambas cartas se gastan al resolverse ATK vs DEF
                cartaEnCampoMaquina.usada = true;
                j.usada = true;
                cartaEnCampoMaquina = null;
                cartaEnDefensaJugador = null;
                jugadorDefendioEstaRonda = false;
                roundsPlayed++;
                deshabilitarBotones(index);
            }
        }

        appendLog("Marcador -> Jugador: " + scoreJugador + " | Máquina: " + scoreMaquina);
        if (verificarFinDuelo()) return;

        turnoJugador = false;
        appendLog("===== Turno de la Máquina =====");
        turnoMaquina();
    }
    // Desactiva botones cuando la carta ya fue usada

    private void deshabilitarBotones(int index) {
        switch (index) {
            case 0 -> { ATACARButton4.setEnabled(false); DEFENDERButton4.setEnabled(false); }
            case 1 -> { ATACARButton5.setEnabled(false); DEFENDERButton5.setEnabled(false); }
            case 2 -> { ATACARButton6.setEnabled(false); DEFENDERButton6.setEnabled(false); }
        }
    }

    // --- TURNO DE LA MÁQUINA ---

    private void turnoMaquina() {
        if (!dueloActivo) return;
        Random rnd = new Random();
        boolean atacar = rnd.nextBoolean();

        if (atacar) {
            // Ataque de la máquina

            Card m = elegirCartaMaquina(true);
            if (m == null) {
                appendLog("La máquina intentó atacar pero no tiene cartas.");
                if (verificarFinDuelo()) return;
                turnoJugador = true;
                appendLog("===== Turno del Jugador =====");
                habilitarBotonesJugador(true);
                return;
            }
            // Defensa de la máquina
            if (m == cartaEnDefensaMaquina) {
                cartaEnDefensaMaquina = null; // deja de ser DEFENSA preservada
                maquinaDefendioEstaRonda = false;
            }
            cartaEnCampoMaquina = m;
            m.enDefensa = false;
            appendLog("Máquina ataca con: " + m.name + " (ATK:" + m.atk + ")");
            turnoJugador = true;
            appendLog("===== Turno del Jugador =====");
            habilitarBotonesJugador(true);
        } else {
            Card defensaM = elegirCartaMaquina(false);
            if (defensaM == null) {
                appendLog("La máquina intentó defender pero no tiene cartas.");
            } else {
                // poner carta como defensa (preservada o defensa actual)
                cartaEnDefensaMaquina = defensaM;
                cartaEnDefensaMaquina.enDefensa = true;
                maquinaDefendioEstaRonda = true;
                appendLog("Máquina defiende con: " + defensaM.name + " (DEF:" + defensaM.def + ")");
                // Solo si el jugador puso defensa EN ESTA ronda consideramos DEF vs DEF
                if (jugadorDefendioEstaRonda) {
                    appendLog("Ambos jugadores pusieron cartas en DEFENSA → se conservan para la siguiente ronda.");
                    jugadorDefendioEstaRonda = false;
                    maquinaDefendioEstaRonda = false;
                }
            }
            turnoJugador = true;
            appendLog("===== Turno del Jugador =====");
            habilitarBotonesJugador(true);
        }
    }

    // Devuelve una carta disponible para la máquina

    private Card elegirCartaMaquina(boolean atacar) {
        List<Card> disponibles = new ArrayList<>();
        for (Card c : machineSlots) {
            if (c == null) continue;
            if (c.usada) continue;
            if (c == cartaEnCampoMaquina) continue; // no volver a elegir la misma carta atacante no resuelta
            disponibles.add(c);
        }
        if (disponibles.isEmpty()) return null;
        return disponibles.get(new Random().nextInt(disponibles.size()));
    }

    // --- RESOLUCIÓN DE ATAQUES ---

    private String resolverAtaque(Card atacante, Card defensor, boolean atacanteEsJugador) {
        // Si el defensor está en modo defensa usamos DEF, si no usamos ATK
        int valorDefensor = defensor.enDefensa ? defensor.def : defensor.atk;

        if (atacante.atk > valorDefensor) {
            if (atacanteEsJugador) { scoreJugador++; return "Jugador"; }
            else { scoreMaquina++; return "Máquina"; }
        } else if (atacante.atk < valorDefensor) {
            if (atacanteEsJugador) { scoreMaquina++; return "Máquina"; }
            else { scoreJugador++; return "Jugador"; }
        } else {
            // Empate: tratamos como empate (ambas cartas se gastan según el flujo que las llame)
            return "Empate";
        }
    }
    // Comprueba si el duelo terminó

    private boolean verificarFinDuelo() {
        if (scoreJugador == 2 || scoreMaquina == 2 || roundsPlayed >= 3) {
            dueloActivo = false;
            String ganador = (scoreJugador > scoreMaquina) ? "Jugador" :
                    (scoreMaquina > scoreJugador ? "Máquina" : "Empate");
            appendLog("¡Duelo terminado! Ganador: " + ganador);
            JOptionPane.showMessageDialog(mainPanel, "Ganador: " + ganador,
                    "Resultado del duelo", JOptionPane.INFORMATION_MESSAGE);
            habilitarBotonesJugador(false);
            return true;
        }
        return false;
    }
    // Activa o desactiva los botones del jugador según si las cartas están disponibles

    private void habilitarBotonesJugador(boolean enabled) {
        if (playerSlots[0] != null && !playerSlots[0].usada) {
            ATACARButton4.setEnabled(enabled);
            DEFENDERButton4.setEnabled(enabled);
        }
        if (playerSlots[1] != null && !playerSlots[1].usada) {
            ATACARButton5.setEnabled(enabled);
            DEFENDERButton5.setEnabled(enabled);
        }
        if (playerSlots[2] != null && !playerSlots[2].usada) {
            ATACARButton6.setEnabled(enabled);
            DEFENDERButton6.setEnabled(enabled);
        }
    }

    // ------------------ MAIN ------------------
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Batalla Yu-Gi-Oh!");
            frame.setContentPane(new batallaYuGiOh().mainPanel);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.pack();
            frame.setVisible(true);
        });
    }
}
