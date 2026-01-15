package controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import dao.CurrencyDao;
import dao.ExchangeRateDao;
import dto.Codes;
import dto.ExchangeRateResponseDto;
import exception.DaoException;
import exception.NotFoundException;
import jakarta.servlet.ServletContext;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.UpdateRate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;

import static dao.util.Validator.*;

@WebServlet(value = "/exchangeRate/*", name = "ExchangeRateServlet")
public class ExchangeRateServlet extends HttpServlet {
    private static final String INSTANCE_CURRENCY = "instanceCurrency";
    private static final String INSTANCE_EXCHANGE_RATE = "instanceExchangeRate";
    private ExchangeRateDao instanceExchangeRate;
    private CurrencyDao instanceCurrency;
    private final ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    @Override
    public void init() {
        ServletContext servletContext = getServletContext();
        this.instanceCurrency = (CurrencyDao) servletContext.getAttribute(INSTANCE_CURRENCY);
        this.instanceExchangeRate = (ExchangeRateDao) servletContext.getAttribute(INSTANCE_EXCHANGE_RATE);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Codes codes = getCodes(request);
        validateCode(codes.baseCode());
        validateCode(codes.targetCode());

        ExchangeRateResponseDto exchangeRateDto;
        try {
            exchangeRateDto = instanceExchangeRate.findRateByCodes(codes.baseCode(), codes.targetCode());
        } catch (DaoException e) {
            throw new NotFoundException(e.getMessage());
        }
        PrintWriter out = response.getWriter();
        response.setStatus(HttpServletResponse.SC_OK);
        mapper.writeValue(out, exchangeRateDto);
    }

    @Override
    protected void doPatch(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String rate = getStringRate(request);
        Codes codes = getCodes(request);
        validateInputParameters(codes.baseCode(), codes.targetCode(), rate);
        BigDecimal newRate = new BigDecimal(rate);

        UpdateRate updateRate = new UpdateRate(instanceCurrency, instanceExchangeRate, codes, newRate);
        updateRate.update();
        ExchangeRateResponseDto exchangeRateDto;
        try {
            exchangeRateDto = instanceExchangeRate.findRateByCodes(codes.baseCode(), codes.targetCode());
        } catch (DaoException e) {
            throw new NotFoundException(e.getMessage());
        }
        response.setStatus(HttpServletResponse.SC_OK);
        PrintWriter out = response.getWriter();
        mapper.writeValue(out, exchangeRateDto);
    }

    private static String getStringRate(HttpServletRequest request) throws IOException {
        BufferedReader reader = request.getReader();
        StringBuilder body = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            body.append(line);
        }
        return body.substring(5, body.length());
    }

    private static Codes getCodes(HttpServletRequest request) {
        String codes = request.getRequestURI();
        codes = codes.substring(codes.lastIndexOf('/') + 1).toUpperCase();
        String baseCode = codes.substring(0, 3).toUpperCase();
        String targetCode = codes.substring(3).toUpperCase();
        return new Codes(baseCode, targetCode);
    }
}
