package controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dao.JdbcCurrencyDao;
import dao.JdbcExchangeRateDao;
import dto.*;
import exception.AlreadyExistException;
import exception.NotFoundException;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import mapper.CurrencyMapper;
import model.Currency;
import model.ExchangeRate;
import service.Exchange;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static util.Validator.*;

@Slf4j
public class ExchangeRatesServlet extends HttpServlet {
    private static final String INSTANCE_CURRENCY = "instanceCurrency";
    private static final String INSTANCE_EXCHANGE_RATE = "instanceExchangeRate";
    private static final String BASE_CODE_PARAMETER = "baseCurrencyCode";
    private static final String TARGET_CODE_PARAMETER = "targetCurrencyCode";
    private static final String RATE_PARAMETER = "rate";
    private static final String EXCHANGE_RATE = "exchange rate ";
    private static final String SAVED_SUCCESSFULLY = "ExchangeRate saved successfully: {}";
    private static final String NOT_FOUND = "Exchange rates not found";
    private static final String SPLITTER = " - ";
    private JdbcCurrencyDao instanceCurrency;
    private JdbcExchangeRateDao instanceExchangeRate;
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void init() {
        ServletContext servletContext = getServletContext();
        this.instanceCurrency = (JdbcCurrencyDao) servletContext.getAttribute(INSTANCE_CURRENCY);
        this.instanceExchangeRate = (JdbcExchangeRateDao) servletContext.getAttribute(INSTANCE_EXCHANGE_RATE);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        List<ExchangeRate> result = instanceExchangeRate.findAll().orElseThrow(() -> new NotFoundException(NOT_FOUND));
        List<ExchangeRateResponseDto> exchangeRatesDto = result.stream().
                map(CurrencyMapper.INSTANCE::exchangeRateToDto).
                toList();
        response.setStatus(HttpServletResponse.SC_OK);
        mapper.writeValue(response.getWriter(), exchangeRatesDto);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String baseCode = request.getParameter(BASE_CODE_PARAMETER).toUpperCase();
        String targetCode = request.getParameter(TARGET_CODE_PARAMETER).toUpperCase();
        String rateString = request.getParameter(RATE_PARAMETER);

        validateInputParameters(baseCode, targetCode, rateString);

        Currency baseCurrency = instanceCurrency.findByCode(baseCode).
                orElseThrow(() -> new NotFoundException(baseCode));
        Currency targetCurrency = instanceCurrency.findByCode(targetCode).
                orElseThrow(() -> new NotFoundException(targetCode));

        Optional<ExchangeRate> mayBeExchangeRate =
                new Exchange(instanceCurrency, instanceExchangeRate).findExchangeRate(baseCode, targetCode);
        if (mayBeExchangeRate.isPresent()) {
            throw new AlreadyExistException(EXCHANGE_RATE + baseCode + SPLITTER + targetCode);
        }

        BigDecimal rate = new BigDecimal(rateString);
        ExchangeRate exchangeRate =
                CurrencyMapper.INSTANCE.currenciesWithRateToExchangeRate(baseCurrency, targetCurrency, rate);

        Long id = instanceExchangeRate.save(exchangeRate);

        ExchangeRate exchangeRateResult = new ExchangeRate(id, baseCurrency, targetCurrency, rate);
        log.info(SAVED_SUCCESSFULLY, exchangeRateResult);

        response.setStatus(HttpServletResponse.SC_CREATED);
        mapper.writeValue(response.getWriter(), CurrencyMapper.INSTANCE.exchangeRateToDto(exchangeRateResult));
    }
}

