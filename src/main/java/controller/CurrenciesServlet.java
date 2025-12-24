package controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import dao.CurrencyDao;
import exception.DaoException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Currency;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/currencies")
public class CurrenciesServlet extends HttpServlet {
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        List<Currency> currencies = CurrencyDao.getInstance().findAll();
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        mapper.writeValue(out, currencies);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        CurrencyDao currency = CurrencyDao.getInstance();
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        String code = request.getParameter("code").toUpperCase();
        String name = request.getParameter("name");
        String sign = request.getParameter("sign");
        if (code.isEmpty() || name.isEmpty() || sign.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        try {
            Currency saved = currency.save(new Currency(code, name, sign));
            response.setStatus(HttpServletResponse.SC_CREATED);
            mapper.writeValue(out, saved);
        } catch (DaoException e) {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
        }
    }
}
