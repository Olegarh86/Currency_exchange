package controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import dao.CurrencyDao;
import dao.ExchangeRateDao;
import dao.util.DBConnector;
import dto.FindExchangeRateByIdDto;
import exception.DaoException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Currency;
import model.ExchangeRate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;

@WebServlet("/exchangeRate/*")
public class ExchangeRateServlet extends HttpServlet {
    private final ObjectMapper mapper = new ObjectMapper();
    private final DBConnector connector = new DBConnector();
    private final Connection connection = connector.getConnection();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        ExchangeRateDao instance = ExchangeRateDao.getInstance();
        String requestURI = request.getRequestURI();
        requestURI = requestURI.substring(requestURI.lastIndexOf('/') + 1).toUpperCase();
        if (requestURI.length() != 6) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }

        String baseCurrency = requestURI.substring(0, 3).toUpperCase();
        String targetCurrency = requestURI.substring(3).toUpperCase();
        try {
            ExchangeRate exchangeRate = instance.findRate(connection, new FindExchangeRateByIdDto(baseCurrency,
                    targetCurrency));
            int baseCurrencyId = exchangeRate.getBaseCurrencyId();
            int targetCurrencyId = exchangeRate.getTargetCurrencyId();
            CurrencyDao currencyDao = CurrencyDao.getInstance();
            Currency base = currencyDao.findById(connection, baseCurrencyId);
            Currency target = currencyDao.findById(connection, targetCurrencyId);
            ExchangeRate exchangeRate1 = new ExchangeRate(exchangeRate.getId(), base, target, exchangeRate.getRate());
            mapper.writeValue(out, exchangeRate1);
        } catch (DaoException e) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    protected void doPatch(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        BufferedReader reader = request.getReader();
        StringBuilder body = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            body.append(line);
        }
        String value = body.substring(5, body.length());

        ExchangeRateDao instance = ExchangeRateDao.getInstance();
        CurrencyDao currencyDao = CurrencyDao.getInstance();
        String requestURI = request.getRequestURI();
        int baseId;
        int targetId;
        requestURI = requestURI.substring(requestURI.lastIndexOf('/') + 1).toUpperCase();
        String baseCurrency = requestURI.substring(0, 3).toUpperCase();
        String targetCurrency = requestURI.substring(3).toUpperCase();
        try {
            baseId = currencyDao.findIdByCode(connection, baseCurrency);
            targetId = currencyDao.findIdByCode(connection, targetCurrency);
        } catch (DaoException e) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        BigDecimal rate;
        try {
            rate = new BigDecimal(value);

            if (rate.compareTo(BigDecimal.ZERO) <= 0) {
                throw new NumberFormatException();
            }

        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            instance.updateRate(connection, baseId, targetId, rate);
        } catch (DaoException e) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        try {
            ExchangeRate exchangeRate = instance.findRate(connection, new FindExchangeRateByIdDto(baseCurrency,
                    targetCurrency));
            mapper.writeValue(out, exchangeRate);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }

    }
}
