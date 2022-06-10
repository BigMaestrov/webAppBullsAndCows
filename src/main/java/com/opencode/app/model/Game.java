package com.opencode.app.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Класс реализует логику игры "Быки и коровы".
 */
public class Game {

    private User user;
    private String secret;
    private boolean guessed;
    private List<GameProgress> moves;

    public Game() {
        startNew();
    }

    /**
     * Метод запуска игры.
     */
    public void startNew() {
        guessed = false;
        moves = new ArrayList<GameProgress>();
        Random random = new Random();
        int rnd;
        while (!isNumberMatch(rnd = random.nextInt(9900) + 100));
        secret = String.format("%04d", rnd);
    }

    /**
     * Проверка пользовательского ввода.
     */
    public boolean isNumberMatch(int num) {
        String str = String.format("%04d", num);
        if (str.length() == 4 && str.matches("(?!.*(.).*\\1)\\d{4}")) {
            return true;
        }
        return false;
    }

    /**
     * Метод сравнивает полученную от пользователя строку с загаданной
     */
    public int checkGuess(String guess) {
        int bulls = 0;
        int cows = 0;
        if (guess.length() == 4 &&
                guess.matches("(?!.*(.).*\\1)\\d{4}")) {
            for (int i = 0; i < 4; i++) {
                if (guess.charAt(i) == secret.charAt(i)) {
                    bulls++;
                } else if (secret.contains(String.valueOf(guess.charAt(i)))) {
                    cows++;
                }
            }
            if (bulls == 4) {
                guessed = true;
            }
            // Добавление хода игры в список ходов
            // для вывода последовательности на страницу
            GameProgress move = new GameProgress();
            move.setNumber(guess);
            move.setBulls(String.valueOf(bulls));
            move.setCows(String.valueOf(cows));
            moves.add(move);
        }
        return bulls;
    }

    /**
     * Метод возвращает строку загаданного четырёхзначного числа.
     */
    public String getSecretNumber() {
        return secret;
    }

    /**
     * Метод устанавливает строку загаданного четырёхзначного числа.
     */
    public void setSecretNumber(String secret) {
        this.secret = secret;
    }

    /**
     * Метод возвращает флаг (статус) того, угадано число или нет.
     */
    public boolean isNumberGuessed() {
        return guessed;
    }

    /**
     * Метод возвращает список ходов игры.
     */
    public List<GameProgress> getMoves() {
        return moves;
    }

    /**
     * Метод возвращает пользователя игры.
     * @return Пользователь игры
     */
    public User getUser() {
        return user;
    }

    /**
     * Метод устанавливает пользователя игры.
     */
    public void setUser(User user) {
        this.user = user;
    }
}
