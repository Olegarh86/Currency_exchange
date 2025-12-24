package controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import dao.CurrencyDao;
import dao.ExchangeRateDao;
import exception.DaoException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Currency;
import model.ExchangeRate;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/exchangeRates")
public class ExchangeRatesServlet extends HttpServlet {
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        ExchangeRateDao instance = ExchangeRateDao.getInstance();
        List<ExchangeRate> exchangeRates = instance.findAll();
        List<ExchangeRate> exchangeRates1 = new ArrayList<>();
        for (ExchangeRate exchangeRate : exchangeRates) {
            CurrencyDao currencyDao = CurrencyDao.getInstance();
            Currency base = currencyDao.findById(exchangeRate.getBaseCurrencyId());
            Currency target = currencyDao.findById(exchangeRate.getTargetCurrencyId());
            exchangeRates1.add(new ExchangeRate (exchangeRate.getId(), base, target, exchangeRate.getRate()));
        }
        mapper.writeValue(out, exchangeRates1);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        ExchangeRateDao instance = ExchangeRateDao.getInstance();
        CurrencyDao currencyDao = CurrencyDao.getInstance();
        String codeBaseCurrency = request.getParameter("baseCurrencyCode").toUpperCase();
        String codeTargetCurrency = request.getParameter("targetCurrencyCode").toUpperCase();
        BigDecimal rate = new BigDecimal(request.getParameter("rate"));
        if (rate.compareTo(BigDecimal.ZERO) > 0 && !codeBaseCurrency.isEmpty() && !codeTargetCurrency.isEmpty()) {
            try {
                int idBaseCurrency = currencyDao.findIdByCode(codeBaseCurrency);
                int idTargetCurrency = currencyDao.findIdByCode(codeTargetCurrency);
                Currency baseCurrency = currencyDao.findById(idBaseCurrency);
                Currency targetCurrency = currencyDao.findById(idTargetCurrency);
                ExchangeRate exchangeRate = instance.save(new ExchangeRate(idBaseCurrency, idTargetCurrency,
                        baseCurrency, targetCurrency, rate));

                response.setStatus(HttpServletResponse.SC_CREATED);
                mapper.writeValue(out, exchangeRate);
            } catch (DaoException e) {
                response.setStatus(HttpServletResponse.SC_CONFLICT);
                mapper.writeValue(out, "Валютная пара с таким кодом уже существует");
            }
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            mapper.writeValue(out, "Отсутствует нужное поле формы");
        }
//  its for testing with tomcat
//        response.setContentType("application/json;charset=UTF-8");
//        PrintWriter out = response.getWriter();
//        ExchangeRateDao instance = ExchangeRateDao.getInstance();
//        CurrencyDao currencyDao = CurrencyDao.getInstance();
//        Currency baseCurrency = null;
//        Currency targetCurrency = null;
//        int baseCurrencyId;
//        int targetCurrencyId;
//        BigDecimal rate;
//        try {
//            baseCurrencyId = Integer.parseInt(request.getParameter("baseCurrencyCode"));
//            targetCurrencyId = Integer.parseInt(request.getParameter("targetCurrencyCode"));
//            rate = new BigDecimal(request.getParameter("rate"));
//        } catch (NumberFormatException e) {
//            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
//            return;
//        }
//
//        try {
//            baseCurrency = currencyDao.findById(baseCurrencyId);
//            targetCurrency = currencyDao.findById(targetCurrencyId);
//        } catch (DaoException e) {
//            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
//        }
//
//        if (rate.compareTo(BigDecimal.ZERO) > 0) {
//
//            try {
//                ExchangeRate exchangeRate = instance.save(new ExchangeRate(baseCurrencyId, targetCurrencyId, rate));
//                exchangeRate.setBaseCurrency(baseCurrency);
//                exchangeRate.setTargetCurrency(targetCurrency);
//                response.setStatus(HttpServletResponse.SC_CREATED);
//                mapper.writeValue(out, exchangeRate);
//            } catch (DaoException e) {
//                response.setStatus(HttpServletResponse.SC_CONFLICT);
//                mapper.writeValue(out, "Валютная пара с таким кодом уже существует");
//            }
//        }
    }

}

