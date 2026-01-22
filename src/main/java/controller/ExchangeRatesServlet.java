package controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import dao.CurrencyDao;
import dao.ExchangeRateDao;
import dto.*;
import exception.AlreadyExistException;
import exception.DaoException;
import exception.NotFoundException;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.List;

import static util.Validator.validateInputParameters;

@Slf4j
public class ExchangeRatesServlet extends HttpServlet {
    private static final String INSTANCE_CURRENCY = "instanceCurrency";
    private static final String INSTANCE_EXCHANGE_RATE = "instanceExchangeRate";
    private static final String BASE_CODE_PARAMETER = "baseCurrencyCode";
    private static final String TARGET_CODE_PARAMETER = "targetCurrencyCode";
    private static final String RATE_PARAMETER = "rate";
    private static final String SAVED_SUCCESSFULLY = "ExchangeRate saved successfully: {}";
    private CurrencyDao instanceCurrency;
    private ExchangeRateDao instanceExchangeRate;
    private final ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    @Override
    public void init() {
        ServletContext servletContext = getServletContext();
        this.instanceCurrency = (CurrencyDao) servletContext.getAttribute(INSTANCE_CURRENCY);
        this.instanceExchangeRate = (ExchangeRateDao) servletContext.getAttribute(INSTANCE_EXCHANGE_RATE);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        List<ExchangeRateDto> result;
        try {
            result = instanceExchangeRate.findAll();
        } catch (DaoException e) {
            throw new RuntimeException(e);
        }
        PrintWriter out = response.getWriter();
        response.setStatus(HttpServletResponse.SC_OK);
        mapper.writeValue(out, result);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String baseCode = request.getParameter(BASE_CODE_PARAMETER).toUpperCase();
        String targetCode = request.getParameter(TARGET_CODE_PARAMETER).toUpperCase();
        String rateString = request.getParameter(RATE_PARAMETER);

        validateInputParameters(baseCode, targetCode, rateString);
        BigDecimal rate = new BigDecimal(rateString);

        CurrencyDto baseCurrency;
        CurrencyDto targetCurrency;
        CurrencyDto currencyRequestDtoBase = new CurrencyRequestDto(baseCode);
        CurrencyDto currencyRequestDtoTarget = new CurrencyRequestDto(targetCode);

        try {
            baseCurrency = instanceCurrency.findCurrencyByCode(currencyRequestDtoBase);
            targetCurrency = instanceCurrency.findCurrencyByCode(currencyRequestDtoTarget);
        } catch (DaoException e) {
            throw new NotFoundException(e.getMessage());
        }
        instanceExchangeRate.validateRatesExistence(currencyRequestDtoBase, currencyRequestDtoTarget);
        ExchangeRateDto responseDto;

        try {
            instanceExchangeRate.save(new ExchangeRateRequestDto(baseCurrency, targetCurrency, rate));
            responseDto = instanceExchangeRate.findExchangeRate(currencyRequestDtoBase, currencyRequestDtoTarget);
            log.info(SAVED_SUCCESSFULLY, responseDto);
        } catch (DaoException e) {
            throw new AlreadyExistException(e.getMessage());
        }
        PrintWriter out = response.getWriter();
        response.setStatus(HttpServletResponse.SC_CREATED);
        mapper.writeValue(out, responseDto);
    }
}

