package controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dao.JdbcCurrencyDao;
import dto.CurrencyRequestDto;
import dto.CurrencyResponseDto;
import exception.NotFoundException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import mapper.CurrencyMapper;
import model.Currency;

import java.io.IOException;
import java.util.List;

import static util.Validator.validateCurrency;

@Slf4j
public class CurrenciesServlet extends HttpServlet {
    private static final String NAME_PARAMETER = "name";
    private static final String CODE_PARAMETER = "code";
    private static final String SIGN_PARAMETER = "sign";
    private static final String CREATE_SUCCESSFUL = "New currency create successful: {}";
    private static final String NOT_FOUND = "Currencies not found: ";
    private final JdbcCurrencyDao instanceCurrency = new JdbcCurrencyDao();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        List<Currency> result = instanceCurrency.findAll().orElseThrow(() -> new NotFoundException(NOT_FOUND));
        List<CurrencyResponseDto> currencyResponseDto = result.stream().
                map(CurrencyMapper.INSTANCE::currencyToDto).
                toList();
        response.setStatus(HttpServletResponse.SC_OK);
        mapper.writeValue(response.getWriter(), currencyResponseDto);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String name = request.getParameter(NAME_PARAMETER);
        String code = request.getParameter(CODE_PARAMETER).toUpperCase();
        String sign = request.getParameter(SIGN_PARAMETER);

        validateCurrency(code, name, sign);

        CurrencyRequestDto requestDto = new CurrencyRequestDto(name, code, sign);
        Long id = instanceCurrency.save(CurrencyMapper.INSTANCE.dtoToCurrency(requestDto));
        CurrencyResponseDto result = CurrencyMapper.INSTANCE.requestDtoToResponseDto(id, requestDto);
        log.info(CREATE_SUCCESSFUL, result);
        response.setStatus(HttpServletResponse.SC_CREATED);
        mapper.writeValue(response.getWriter(), result);
    }
}
