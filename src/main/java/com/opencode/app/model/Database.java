package com.opencode.app.model;

import com.opencode.app.utils.Utils;

import javax.servlet.ServletContext;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Класс для работы с базой данных (HSQLDB).
 * Является одним из классов Модели приложения, построенного на основе архитектуры MVC.
 * Методы класса с помощью JDBC взаимодействуют с таблицами, записями базы данных.
 *
 * @version 1.0
 * @author Vladimir Kizelbashev
 */
public class Database {

    private String dbUrl;       // строка подключения к базе данных
    private String dbUserName;  // имя пользователя базы данных
    private String dbPassword;  // пароль пользователя базы данных

    // Объект для ведения лога исключений
    private static Logger logger = Logger.getLogger(Database.class.getName());

    /**
     * Метод инициализирует параметры подключения к базе данных.
     * @param context Контекст сервлета
     */
    public void init(ServletContext context) {
        // Чтение параметров контекста сервлета
        dbUrl = context.getInitParameter("dbUrl");
        dbUserName = context.getInitParameter("dbUserName");
        dbPassword = context.getInitParameter("dbPassword");
    }

    /**
     * Метод инициализирует параметры подключения к базе данных.
     * @param dbUrl Строка подключения к базе данных
     * @param dbUserName Имя пользователя базы данных
     * @param dbPassword Пароль пользователя базы данных
     */
    public void init(String dbUrl, String dbUserName, String dbPassword) {
        this.dbUrl = dbUrl;
        this.dbUserName = dbUserName;
        this.dbPassword = dbPassword;
    }

    /**
     * Метод выполняет авторизацию пользователя в приложении игры.
     * @param userName Имя пользователя
     * @param password Пароль пользователя
     * @return Объект пользователя приложения, соответствующий имени и паролю,
     *         либо {@code null}, если пользователь не найден
     */
    public User login(String userName, String password) {
        String sql = "SELECT * FROM users WHERE user_name = ? AND password = ?";
        User user = null;
        try (Connection conn = DriverManager.getConnection(dbUrl, dbUserName, dbPassword);
             PreparedStatement st = conn.prepareStatement(sql)) {
            st.setString(1, userName);
            st.setString(2, Utils.getMD5Hash(password));
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    user = new User();
                    user.setId(rs.getLong("id"));
                    user.setFirstName(rs.getString("first_name"));
                    user.setLastName(rs.getString("last_name"));
                    user.setUserName(rs.getString("user_name"));
                    user.setPassword(rs.getString("password"));
                }
            } catch (SQLException ex) {
                throw ex;
            }
        } catch (Exception ex) {
            user = null;
            logger.severe(ex.getMessage());
        }
        return user;
    }

    /**
     * Метод выполняет регистрацию нового пользователя в приложении игры.
     * @param user Пользователь - объект класса {@link User} с инициализированными полями
     *             (кроме поля {@link User#/id}, которое метод определяет после вставки записи в базу данных)
     * @return Объект пользователя игры, добавленного в базу данных,
     *         либо {@code null}, если пользователь с заданным именем (логином) уже существует
     */
    public User register(User user) {
        String sql = "INSERT INTO users (first_name, last_name, user_name, password) values (?, ?, ?, ?);";
        try (Connection conn = DriverManager.getConnection(dbUrl, dbUserName, dbPassword);
             PreparedStatement st = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            st.setString(1, user.getFirstName());
            st.setString(2, user.getLastName());
            st.setString(3, user.getUserName());
            st.setString(4, Utils.getMD5Hash(user.getPassword()));
            if (st.executeUpdate() == 1) {
                try (ResultSet rs = st.getGeneratedKeys()) {
                    rs.next();
                    user.setId(rs.getLong(1));
                } catch (SQLException ex) {
                    throw ex;
                }
            } else {
                user = null;
            }
        } catch (Exception ex) {
            user = null;
            logger.severe(ex.getMessage());
        }
        return user;
    }

    /**
     * Метод возвращает рейтинг пользователей в виде списка значений класса {@link RatingItem}.
     * Рейтинг пользователей рассчитывается на основе среднего количества попыток до угадывания числа.
     * Элементы списка расположены в порядке возрастания среднего количества попыток {@link RatingItem#/avgAttempts}.
     * @return Список рейтинга пользователей
     */
    public List<RatingItem> getRating() {
        String sql = "SELECT u.user_name, AVG(g.attempts * 1.0) AS avg_attempts, COUNT(*) AS games_count " +
                "FROM users u, games g " +
                "WHERE u.id = g.user_id " +
                "GROUP BY u.id, u.user_name " +
                "ORDER BY avg_attempts, u.user_name;";
        List<RatingItem> rating = new ArrayList<RatingItem>();
        try (Connection conn = DriverManager.getConnection(dbUrl, dbUserName, dbPassword);
             PreparedStatement st = conn.prepareStatement(sql);
             ResultSet rs = st.executeQuery()) {
            while (rs.next()) {
                RatingItem item = new RatingItem();
                item.setUserName(rs.getString("user_name"));
                item.setAvgAttempts(rs.getDouble("avg_attempts"));
                item.setGamesCount(rs.getInt("games_count"));
                rating.add(item);
            }
        } catch (SQLException ex) {
            rating = null;
            logger.severe(ex.getMessage());
        }
        return rating;
    }

    /**
     * Метод сохраняет данные завершённой пользователем игры в базе данных.
     * Это необходимо для ведения рейтинга пользователей.
     * @param user Пользователь игры
     * @param attempts Количество попыток до угадывания числа
     */
    public void addGame(User user, int attempts) {
        String sql = "INSERT INTO games (user_id, attempts) VALUES (?, ?);";
        try (Connection conn = DriverManager.getConnection(dbUrl, dbUserName, dbPassword);
             PreparedStatement st = conn.prepareStatement(sql)) {
            st.setLong(1, user.getId());
            st.setInt(2, attempts);
            st.executeUpdate();
        } catch (SQLException ex) {
            logger.severe(ex.getMessage());
        }
    }
}
