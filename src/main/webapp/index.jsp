<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<!DOCTYPE html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Главная</title>
    <style>
        <%@include file="/css/styles.css"%>
    </style>
</head>
<body>
    <%@ include file="/WEB-INF/jsp/header.jsp" %>
    <div align="center">
        <form name="index-form" class="index-form" method="POST" action="controller">
            <div class="form-message">
                <b>Добро пожаловать в игру «Быки и коровы»!</b>
                <p>
                Чтобы начать игру нужно войти в систему, используя Вашу учётную запись.
                Если у Вас ещё нет учётной записи, то зарегистрируйтесь по ссылке ниже
                или используйте тестовую (имя пользователя = user, пароль = user).
                </p>
            </div>
            <button type="submit" name="action" value="show-login" class="btn-link-ind">Вход</button>
            <br>
            <br>
            <button type="submit" name="action" value="show-register" class="btn-link-ind">Регистрация</button>
        </form>
    </div>
    <%@ include file="/WEB-INF/jsp/footer.jsp" %>
</body>
</html>
