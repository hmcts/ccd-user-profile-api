package uk.gov.hmcts.ccd.auth;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.auth.checker.core.service.Service;
import uk.gov.hmcts.reform.auth.parser.idam.core.service.token.ServiceTokenInvalidException;
import uk.gov.hmcts.reform.auth.parser.idam.core.service.token.ServiceTokenParser;
import uk.gov.hmcts.reform.auth.parser.idam.core.service.token.ServiceTokenParsingException;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.auth.AuthCheckerFilter.AUTHORISATION;

public class AuthCheckerFilterTest {

    @Mock
    private ServiceTokenParser serviceTokenParser;

    @Mock
    private AuthenticationManager authenticationManager;

    private AuthCheckerFilter filter;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        this.filter = new AuthCheckerFilter(serviceTokenParser, authenticationManager);
    }

    @Test
    public void testAuthorizeService() {
        ReflectionTestUtils.setField(filter, "authorisedSServices", Arrays.asList("test"));

        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getHeader(AUTHORISATION)).thenReturn("token");

        when(serviceTokenParser.parse(anyString())).thenReturn("test");

        Service service = this.filter.authorizeService(req);

        assertEquals("test", service.getPrincipal());

        verify(serviceTokenParser, times(1)).parse(anyString());
    }

    @Test
    public void testNoServices() {
        Throwable t = assertThrows(IllegalArgumentException.class, () -> this.filter.authorizeService(null));
        assertEquals("Must have at least one service defined",  t.getMessage());
    }

    @Test
    public void testNoToken() {
        ReflectionTestUtils.setField(filter, "authorisedSServices", Arrays.asList("test"));

        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getHeader(AUTHORISATION)).thenReturn(null);

        assertNull(this.filter.authorizeService(req));
    }

    @Test
    public void testHandleServiceTokenInvalidException() {
        ReflectionTestUtils.setField(filter, "authorisedSServices", Arrays.asList("test"));

        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getHeader(AUTHORISATION)).thenReturn("token");

        when(serviceTokenParser.parse(anyString())).thenThrow(new ServiceTokenInvalidException());

        assertNull(this.filter.authorizeService(req));
    }

    @Test
    public void testHandleServiceTokenParsingException() {
        ReflectionTestUtils.setField(filter, "authorisedSServices", Arrays.asList("test"));

        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getHeader(AUTHORISATION)).thenReturn("token");

        when(serviceTokenParser.parse(anyString())).thenThrow(new ServiceTokenParsingException());

        assertNull(this.filter.authorizeService(req));
    }

    @Test
    public void testInvalidService() {
        ReflectionTestUtils.setField(filter, "authorisedSServices", Arrays.asList("test"));

        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getHeader(AUTHORISATION)).thenReturn("token");

        when(serviceTokenParser.parse(anyString())).thenReturn("invalid");

        assertNull(this.filter.authorizeService(req));
    }

}
