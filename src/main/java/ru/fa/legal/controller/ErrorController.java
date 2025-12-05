package ru.fa.legal.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Контроллер для обработки ошибок
 */
@Controller
class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        Object exception = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);

        if (status != null) {
            Integer statusCode = Integer.valueOf(status.toString());
            model.addAttribute("statusCode", statusCode);
            model.addAttribute("errorMessage", message);

            if (statusCode == HttpStatus.NOT_FOUND.value()) {
                model.addAttribute("errorTitle", "Страница не найдена");
                model.addAttribute("errorDescription", "Запрашиваемая страница не существует");
            } else if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                model.addAttribute("errorTitle", "Внутренняя ошибка сервера");
                model.addAttribute("errorDescription", "Произошла ошибка при обработке запроса");

                if (exception != null) {
                    Exception ex = (Exception) exception;
                    model.addAttribute("exceptionMessage", ex.getMessage());
                    model.addAttribute("exceptionClass", ex.getClass().getName());
                }
            } else if (statusCode == HttpStatus.FORBIDDEN.value()) {
                model.addAttribute("errorTitle", "Доступ запрещен");
                model.addAttribute("errorDescription", "У вас нет прав для доступа к этому ресурсу");
            } else {
                model.addAttribute("errorTitle", "Ошибка " + statusCode);
                model.addAttribute("errorDescription", "Произошла неизвестная ошибка");
            }
        }

        return "error";
    }
}