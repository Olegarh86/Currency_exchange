package controller;

import dao.CurrencyDao;
import dao.ExchangeRateDao;
import dto.FindExchangeRateByIdDto;
import exception.DaoException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.ExchangeRate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.List;

@WebServlet("/api/exchangeRate/*")
public class ExchangeRateServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        ExchangeRateDao instance = ExchangeRateDao.getInstance();
        String requestURI = request.getRequestURI();
        requestURI = requestURI.substring(requestURI.lastIndexOf('/') + 1).toUpperCase();
        if (requestURI.length() != 6) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
        String baseCurrency = requestURI.substring(0, 3).toUpperCase();
        String targetCurrency = requestURI.substring(3).toUpperCase();
        try {
            List<ExchangeRate> exchangeRates = instance.findPair(new FindExchangeRateByIdDto(baseCurrency, targetCurrency));
            response.setContentType("application/json");
            out.print(exchangeRates);
        } catch (DaoException e) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    protected void doPatch(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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
            baseId = currencyDao.findIdByCode(baseCurrency);
            targetId = currencyDao.findIdByCode(targetCurrency);
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

        instance.updateRate(baseId, targetId, rate);
        List<ExchangeRate> exchangeRate = instance.findPair(new FindExchangeRateByIdDto(baseCurrency, targetCurrency));
        out.print(exchangeRate);
    }
}
