package controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import dao.CurrencyDao;
import dao.ExchangeRateDao;
import dto.Codes;
import dto.ExchangeRateResponseDto;
import dto.CurrenciesResponseDto;
import exception.DaoException;
import exception.NotFoundException;
import jakarta.servlet.ServletException;
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
    private final ExchangeRateDao exchangeRateInstance = ExchangeRateDao.getInstance();
    private final ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Codes codes = getCodes(request);
        validateCode(codes.baseCode());
        validateCode(codes.targetCode());

        ExchangeRateResponseDto exchangeRateDto;
        try {
            exchangeRateDto = exchangeRateInstance.findRateByCodes(codes.baseCode(), codes.targetCode());
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

        UpdateRate updateRate = new UpdateRate(exchangeRateInstance, codes, newRate);
        updateRate.update();
        ExchangeRateResponseDto exchangeRateDto;
        try {
            exchangeRateDto = exchangeRateInstance.findRateByCodes(codes.baseCode(), codes.targetCode());
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
