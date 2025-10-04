
Andres Felipe Echeverri Giraldo  2266244
Kevin Daniel Berrio Ramirez  2266277


# Batalla Yu-Gi-Oh!

Un pequeño juego en Java que simula un duelo entre el jugador y la máquina usando cartas del universo Yu-Gi-Oh!, obtenidas en tiempo real desde la API pública de **YGOProDeck**.  
El jugador puede elegir atacar o defender con sus cartas, y el resultado de cada ronda se determina comparando los valores de ATK y DEF.

---

## 🧩 Instrucciones de ejecución

1. **Clonar el repositorio desde GitHub:**
   ```bash
   git clone https://github.com/Danniel06-b/Yugi-Oh.git
   ```
2. Entrar a la carpeta del proyecto:
   ```bash
   src/BatallaYugiOh/batallaYugiOh.java
   ```
   Y ejecutar
3. Abrir el proyecto en tu IDE preferido (**IntelliJ IDEA**, **Eclipse** o **NetBeans**).
4. Asegurarte de tener **Java 17 o superior** instalado.
5. Verificar las dependencias necesarias:
   - `org.json` (para manejo de JSON)
   - `java.net.http` (ya viene incluido desde Java 11)
6. Ejecutar la clase principal:
   ```
   BatallaYuGiOh.batallaYuGiOh
   ```
7. En la ventana del juego:
   - Pulsa el botón **CARTAS** para cargar cartas aleatorias desde la API.
   - Luego pulsa **INICIAR DUELO** para comenzar.
   - Usa los botones **ATACAR** o **DEFENDER** según tu estrategia.

---

## ⚙️ Breve explicación de diseño

El juego está construido con **Java Swing** para la interfaz gráfica y utiliza un diseño orientado a objetos simple.  
Cada carta está representada por una clase `Card`, que guarda su nombre, puntos de ataque, defensa y la URL de su imagen.  
La clase principal (`batallaYuGiOh`) maneja toda la lógica del duelo, incluyendo los turnos, la resolución de ataques y la comunicación con la API mediante la clase `YgoApiClient`.  

El flujo del programa sigue un esquema por turnos:
- El jugador y la máquina alternan acciones (atacar o defender).
- Cada interacción se resuelve comparando valores de ATK y DEF.
- El juego finaliza cuando alguno gana dos rondas o se juegan tres.

---

## 🖼️ Dependencias externas
- **API**: [https://db.ygoprodeck.com/api/v7/cardinfo.php](https://db.ygoprodeck.com/api/v7/cardinfo.php)
- **Librería JSON**: `org.json` para el procesamiento de respuestas de la API.

- Foto general de la app, se recomienda usarlo en pantalla completa
- <img width="1918" height="1078" alt="1" src="https://github.com/user-attachments/assets/774978a0-e862-4f42-ab60-f58f428967b0" />

- Foto con cartas en mano, solicitarlas en el botón cartas y luego iniciar duelo para empezar
- <img width="1918" height="1078" alt="Captura de pantalla 2025-10-04 163742" src="https://github.com/user-attachments/assets/893935b0-f943-4de1-899b-18cee30f059b" />

-Foto Anuncio ganador
<img width="1918" height="1078" alt="3" src="https://github.com/user-attachments/assets/0071e8c0-7bbc-4478-910c-f9bb895940f8" />

---
