package controller.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;

import java.io.IOException;

@WebFilter(servletNames = {"CurrenciesServlet", "CurrencyServlet", "ExchangeRateServlet",
        "ExchangeRatesServlet", "ExchangeServlet"})
public class EncodingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        servletResponse.setContentType("application/json;charset=UTF-8");
        filterChain.doFilter(servletRequest, servletResponse);
    }
}
