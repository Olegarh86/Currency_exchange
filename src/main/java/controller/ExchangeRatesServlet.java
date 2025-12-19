package controller;

import dao.CurrencyDao;
import dao.ExchangeRateDao;
import exception.DaoException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.ExchangeRate;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.List;

@WebServlet("/api/exchangeRates")
public class ExchangeRatesServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ExchangeRateDao instance = ExchangeRateDao.getInstance();
        List<ExchangeRate> exchangeRates = instance.findAll();
        PrintWriter out = response.getWriter();
        response.setContentType("text/json;charset=UTF-8");
        out.println(exchangeRates);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ExchangeRateDao instance = ExchangeRateDao.getInstance();
        CurrencyDao currencyDao = CurrencyDao.getInstance();
        int baseCurrencyId;
        int targetCurrencyId;
        BigDecimal rate;
        try {
            baseCurrencyId = Integer.parseInt(request.getParameter("baseCurrencyCode"));
            targetCurrencyId = Integer.parseInt(request.getParameter("targetCurrencyCode"));
            rate = new BigDecimal(request.getParameter("rate"));
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            currencyDao.findById(baseCurrencyId);
            currencyDao.findById(targetCurrencyId);
        } catch (DaoException e) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }

        PrintWriter out = response.getWriter();
        if (rate.compareTo(BigDecimal.ZERO) > 0) {

            try {
                ExchangeRate exchangeRate = instance.save(new ExchangeRate(baseCurrencyId, targetCurrencyId, rate));
                response.setContentType("text/json;charset=UTF-8");
                response.setStatus(HttpServletResponse.SC_CREATED);
                out.println(exchangeRate);
            } catch (DaoException e) {
                response.setStatus(HttpServletResponse.SC_CONFLICT);
            }
        }
    }
}

