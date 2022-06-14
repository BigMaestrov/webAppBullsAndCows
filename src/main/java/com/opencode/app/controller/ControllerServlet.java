package com.opencode.app.controller;

import com.opencode.app.model.Database;
import com.opencode.app.model.Game;
import com.opencode.app.model.User;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Класс представляет собой Контроллер приложения, построенного на основе архитектуры MVC.
 * Он осуществляет диспетчеризацию поступающих запросов, реализуя логику приложения.
 * Сервлет создаёт экземпляры классов Модели и в зависимости от параметров поступающих
 * клиентских запросов перенаправляет запросы на соответствующие jsp-страницы (Виды).
 * В видах осуществляется ввод/вывод данных приложения.
 */
public class ControllerServlet extends HttpServlet {

    private static String INDEX = "/index.jsp";                     // главная страница
    private static String LOGIN = "/WEB-INF/jsp/login.jsp";         // авторизация
    private static String REGISTER = "/WEB-INF/jsp/register.jsp";   // регистрация
    private static String GAME = "/WEB-INF/jsp/game.jsp";           // игра (игра, рейтинг, правила)
    private static String ERROR = "/WEB-INF/jsp/error.jsp";         // ошибка 404

    private Database db = new Database();                           // объект для работы с базой данных

    private static Logger logger = Logger.getLogger(ControllerServlet.class.getName()); // для ведения лога

    /**
     * Метод инициализирует сервлет (а также объект-bean для доступа к базе данных)
     * параметрами, считанными из дескриптора развёртывания приложения (web.xml).
     * @param config Параметры инициализации
     */
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        ServletContext context = getServletContext();
        db.init(context);
        context.setAttribute("dbBean", db);
    }

    /**
     * Метод обрабатывает поступающие от клиентов GET-запросы.
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Метод обрабатывает поступающие от клиентов POST-запросы.
     */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Метод обрабатывает поступающие запросы, перенаправляемые ему от методов
     */
    public void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            // Получение сессии пользователя
            HttpSession session = request.getSession(true);

            // Чтение запрашиваемого параметра (действия)
            String action = request.getParameter("action");

            // URL по умолчанию (главная страница)
            String url = INDEX;

            // Формирование URL и инициализация объектов
            // в зависимости от запроса
            if (action != null) {
                switch (action) {
                    case "show-login":
                        url = LOGIN;
                        break;
                    case "show-register":
                        url = REGISTER;
                        break;
                    case "show-index":
                        url = INDEX;
                        break;
                    // Авторизация пользователя
                    case "login":
                        String userName = request.getParameter("username");
                        String password = request.getParameter("password");
                        User loginUser = db.login(userName, password);
                        if (loginUser != null) {
                            url = GAME;
                            Game game = new Game();
                            game.setUser(loginUser);
                            session.setAttribute("game", game);
                        } else {
                            url = LOGIN;
                            request.setAttribute("loginMessage", "Пользователь не найден");
                        }
                        break;
                    // Регистрация нового пользователя
                    case "register":
                        User regUser = new User();
                        regUser.setFirstName(request.getParameter("firstname"));
                        regUser.setLastName(request.getParameter("lastname"));
                        regUser.setUserName(request.getParameter("username"));
                        regUser.setPassword(request.getParameter("password"));
                        regUser = db.register(regUser);
                        if (regUser != null) {
                            url = GAME;
                            Game game = new Game();
                            game.setUser(regUser);
                            session.setAttribute("game", game);
                        } else {
                            url = REGISTER;
                            request.setAttribute("registerMessage", "Пользователь уже существует");
                        }
                        break;
                    case "show-game":
                        url = GAME;
                        break;
                    case "show-rating":
                        url = GAME;
                        request.setAttribute("view", "rating");
                        break;
                    case "show-rules":
                        url = GAME;
                        request.setAttribute("view", "rules");
                        break;
                    // Проверка введённого пользователем числа
                    case "check-number":
                        url = GAME;
                        Game game = (Game) session.getAttribute("game");
                        if (game != null) {
                            if (game.isNumberGuessed()) {
                                game.startNew();
                            } else {
                                String guess = request.getParameter("guess-text");
                                game.checkGuess(guess);
                                if (game.isNumberGuessed()) {
                                    User gameUser = game.getUser();
                                    int attempts = game.getMoves().size();
                                    db.addGame(gameUser, attempts);
                                }
                            }
                        }
                        break;
                    // Запуск новой игры
                    case "new-game":
                        url = GAME;
                        Game exGame = (Game) session.getAttribute("game");
                        if (exGame != null) {
                            exGame.startNew();
                        }
                        break;
                    // Завершение сессии
                    case "logout":
                        url = INDEX;
                        session.invalidate();
                        break;
                    default:
                        url = ERROR;
                }
            }

            // Перенаправление запроса
            RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(url);
            dispatcher.forward(request, response);

        } catch (Exception ex) {
            logger.severe(ex.getMessage());
        }
    }
}
