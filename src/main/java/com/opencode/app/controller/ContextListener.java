package com.opencode.app.controller;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

/**
 * Класс предназначен для инициализации приложения сразу после инициализации контейнера сервлетов.
 * Он является слушателем событий жизненного цикла контекста сервлета
 */
public class ContextListener implements ServletContextListener {

    // Объект для ведения лога исключений
    private static Logger logger = Logger.getLogger(ContextListener.class.getName());

    /**
     * Метод вызывается после инициализации контейнера сервлетов.
     * Он выполняет инициализацию приложения до начала обработки запросов.
     */
    @Override
    public void contextInitialized(ServletContextEvent event) {
        ServletContext context = event.getServletContext();
        String rootPath = context.getRealPath("/");
        System.setProperty("webroot", rootPath);
        initDatabase(context);
    }

    /**
     * Метод выполняется при закрытии контекста сервлета.
     */
    @Override
    public void contextDestroyed(ServletContextEvent event) {
    }

    /**
     * Метод инициализации базы данных (HSQLDB).
     * Он считывает параметры подключения к базе данных из дескриптора развёртывания приложения,
     * загружает класс JDBC-драйвера, подключается к базе данных и создаёт таблицы и записи.
     */
    private void initDatabase(ServletContext context) {
        try {
            // Чтение параметров контекста из файла web.xml
            // (параметров подключения к базе данных)
            String driver = context.getInitParameter("dbDriver");
            String url = context.getInitParameter("dbUrl");
            String username = context.getInitParameter("dbUserName");
            String password = context.getInitParameter("dbPassword");

            // Загрузка класса JDBC-драйвера
            Class.forName(driver);

            // Подключение к базе данных
            try (Connection conn = DriverManager.getConnection(url, username, password);
                 Statement st = conn.createStatement()) {

                // Создание таблицы "Пользователи"
                st.execute("CREATE TABLE IF NOT EXISTS users (" +
                        "id BIGINT GENERATED BY DEFAULT AS IDENTITY "+
                        "(START WITH 1, INCREMENT BY 1) NOT NULL PRIMARY KEY, " +
                        "first_name NVARCHAR(32) NOT NULL, " +          // имя
                        "last_name NVARCHAR(32) NOT NULL, " +           // фамилия
                        "user_name NVARCHAR(32) NOT NULL UNIQUE, " +    // имя пользователя
                        "password NVARCHAR(32) NOT NULL);");            // хеш пароля

                // Создание таблицы "Игры" (завершённые пользователями игры)
                st.execute("CREATE TABLE IF NOT EXISTS games (" +
                        "id BIGINT GENERATED BY DEFAULT AS IDENTITY " +
                        "(START WITH 1, INCREMENT BY 1) NOT NULL PRIMARY KEY, " +
                        "user_id BIGINT NOT NULL, " +                   // ID пользователя
                        "attempts INT NOT NULL, " +                     // количество попыток до угадывания числа
                        "CONSTRAINT fk_user_game FOREIGN KEY (user_id) REFERENCES users(id));");

                try {
                    // Вставка тестовых записей
                    // Пользователи
                    st.execute("INSERT INTO users (id, first_name, last_name, user_name, password) " +
                            "VALUES (1, 'Name', 'Surname', 'user', 'ee11cbb19052e40b07aac0ca060c23ee');");

                    st.execute("INSERT INTO users (id, first_name, last_name, user_name, password) " +
                            "VALUES (2, 'Keanu', 'Reeves', 'neo', 'cb59608fced567a14b13a6e5c5c8a1d2');");

                    st.execute("INSERT INTO users (id, first_name, last_name, user_name, password) " +
                            "VALUES (3, 'Laurence', 'Fishburne', 'morpheus', 'eb35c17c47b8ea645be204aba44cae3d');");

                    st.execute("INSERT INTO users (id, first_name, last_name, user_name, password) " +
                            "VALUES (4, 'Carrie-Anne', 'Moss', 'trinity', 'ee2055701c742262f93d083f854350b9');");

                    // Игры
                    st.execute("INSERT INTO games (id, user_id, attempts) VALUES (1, 1, 8);");
                    st.execute("INSERT INTO games (id, user_id, attempts) VALUES (2, 1, 10);");
                    st.execute("INSERT INTO games (id, user_id, attempts) VALUES (3, 1, 12);");

                    st.execute("INSERT INTO games (id, user_id, attempts) VALUES (4, 2, 7);");

                    st.execute("INSERT INTO games (id, user_id, attempts) VALUES (5, 3, 7);");
                    st.execute("INSERT INTO games (id, user_id, attempts) VALUES (6, 3, 8);");

                    st.execute("INSERT INTO games (id, user_id, attempts) VALUES (7, 4, 7);");
                    st.execute("INSERT INTO games (id, user_id, attempts) VALUES (8, 4, 8);");

                } catch (SQLException ex) {
                    throw new SQLException("The record already exists");
                }

            } catch (SQLException ex) {
                throw ex;
            }

        } catch (Exception ex) {
            logger.severe(ex.getMessage());
        }
    }
}
