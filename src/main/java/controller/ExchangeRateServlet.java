package controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import dao.CurrencyDao;
import dao.ExchangeRateDao;
import dto.*;
import exception.DaoException;
import exception.NotFoundException;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;

import static util.Validator.*;

@Slf4j
public class ExchangeRateServlet extends HttpServlet {
    private static final String INSTANCE_CURRENCY = "instanceCurrency";
    private static final String INSTANCE_EXCHANGE_RATE = "instanceExchangeRate";
    private static final String UPDATED_SUCCESSFULLY = "ExchangeRate updated successfully: {} - {} rate: {}";
    private static final char SEPARATOR = '/';
    private static final int INDEX_START_RATE = 5;
    private static final int CODE_LENGTH = 3;
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
        String baseCode = codes.baseCode();
        String targetCode = codes.targetCode();
        validateCode(baseCode);
        validateCode(targetCode);

        CurrencyDto currencyDtoBase = new CurrencyRequestDto(baseCode);
        CurrencyDto currencyDtoTarget = new CurrencyRequestDto(targetCode);
        Dto result;
        try {
            result = instanceExchangeRate.findExchangeRate(currencyDtoBase, currencyDtoTarget);
        } catch (DaoException e) {
            throw new NotFoundException(e.getMessage());
        }
        PrintWriter out = response.getWriter();
        response.setStatus(HttpServletResponse.SC_OK);
        mapper.writeValue(out, result);
    }

    @Override
    protected void doPatch(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String rate = getStringRate(request);
        Codes codes = getCodes(request);
        String baseCode = codes.baseCode();
        String targetCode = codes.targetCode();

        validateInputParameters(baseCode, targetCode, rate);
        BigDecimal newRate = new BigDecimal(rate);
        update(baseCode, targetCode, newRate);
        Dto result;
        CurrencyDto currencyDtoBase = new CurrencyRequestDto(baseCode);
        CurrencyDto currencyDtoTarget = new CurrencyRequestDto(targetCode);
        try {
            result = instanceExchangeRate.findExchangeRate(currencyDtoBase, currencyDtoTarget);
        } catch (DaoException e) {
            throw new NotFoundException(e.getMessage());
        }
        response.setStatus(HttpServletResponse.SC_OK);
        PrintWriter out = response.getWriter();
        mapper.writeValue(out, result);
    }

    private static String getStringRate(HttpServletRequest request) throws IOException {
        BufferedReader reader = request.getReader();
        StringBuilder body = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            body.append(line);
        }
        return body.substring(INDEX_START_RATE, body.length());
    }

    private static Codes getCodes(HttpServletRequest request) {
        String codes = request.getRequestURI();
        codes = codes.substring(codes.lastIndexOf(SEPARATOR) + 1).toUpperCase();
        String baseCode = codes.substring(0, CODE_LENGTH).toUpperCase();
        String targetCode = codes.substring(CODE_LENGTH).toUpperCase();
        return new Codes(baseCode, targetCode);
    }

    private void update(String baseCode, String targetCode, BigDecimal newRate) {
        CurrencyDto baseDto;
        CurrencyDto targetDto;
        try {
            CurrencyRequestDto currencyRequestDtoBase = new CurrencyRequestDto(baseCode);
            CurrencyRequestDto currencyRequestDtoTarget = new CurrencyRequestDto(targetCode);
            baseDto = instanceCurrency.findCurrencyByCode(currencyRequestDtoBase);
            targetDto = instanceCurrency.findCurrencyByCode(currencyRequestDtoTarget);
        } catch (DaoException e) {
            throw new NotFoundException(e.getMessage());
        }

        try {
            ExchangeRateDto exchangeRateDto = new ExchangeRateRequestDto(baseDto, targetDto, newRate);
            instanceExchangeRate.updateRate(exchangeRateDto);
            log.info(UPDATED_SUCCESSFULLY, baseCode, targetCode, newRate);
        } catch (DaoException e) {
            throw new NotFoundException(e.getMessage());
        }
    }
}
