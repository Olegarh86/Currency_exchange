package controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import dao.CurrencyDao;
import dto.CurrencyDto;
import dto.CurrencyRequestDto;
import dto.Dto;
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
import java.util.List;

import static util.Validator.validateCurrency;

@Slf4j
public class CurrenciesServlet extends HttpServlet {
    private static final String NAME_PARAMETER = "name";
    private static final String CODE_PARAMETER = "code";
    private static final String SIGN_PARAMETER = "sign";
    private static final String INSTANCE_CURRENCY = "instanceCurrency";
    private static final String CREATE_SUCCESSFUL = "New currency create successful: {}";
    private CurrencyDao instanceCurrency;
    private final ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    @Override
    public void init() {
        ServletContext servletContext = getServletContext();
        this.instanceCurrency = (CurrencyDao) servletContext.getAttribute(INSTANCE_CURRENCY);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        List<Dto> result;
        try {
            result = instanceCurrency.findAll();
        } catch (DaoException e) {
            throw new NotFoundException(e.getMessage());
        }
        PrintWriter out = response.getWriter();
        response.setStatus(HttpServletResponse.SC_OK);
        mapper.writeValue(out, result);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String name = request.getParameter(NAME_PARAMETER);
        String code = request.getParameter(CODE_PARAMETER).toUpperCase();
        String sign = request.getParameter(SIGN_PARAMETER);

        validateCurrency(code, name);
        CurrencyDto currencyRequestDto = new CurrencyRequestDto(name, code, sign);
        Dto result;
        try {
            instanceCurrency.save(currencyRequestDto);
            result = instanceCurrency.findCurrencyByCode(currencyRequestDto);
            log.info(CREATE_SUCCESSFUL, result);
        } catch (DaoException e) {
            throw new AlreadyExistException(e.getMessage());
        }
        PrintWriter out = response.getWriter();
        response.setStatus(HttpServletResponse.SC_CREATED);
        mapper.writeValue(out, result);
    }
}
