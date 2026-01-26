package com.budgetpro.infrastructure.catalogo.adapter;

import com.budgetpro.domain.catalogo.exception.CatalogNotFoundException;
import com.budgetpro.domain.catalogo.exception.CatalogServiceException;
import com.budgetpro.domain.catalogo.model.RecursoSearchCriteria;
import com.budgetpro.domain.catalogo.model.RecursoSnapshot;
import com.budgetpro.domain.shared.model.TipoRecurso;
import com.budgetpro.infrastructure.catalogo.cache.CatalogCache;
import com.budgetpro.infrastructure.catalogo.observability.CatalogEventLogger;
import com.budgetpro.infrastructure.catalogo.observability.CatalogMetrics;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.ExpectedCount.times;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"NullAway", "null"})
class CapecoApiAdapterTest {

    private static final String BASE_URL = "http://capeco.test";
    private static final String API_KEY = "api-key";

    @Mock
    private CatalogCache catalogCache;

    @Mock
    private CatalogMetrics catalogMetrics;

    @Mock
    private CatalogEventLogger catalogEventLogger;

    @Test
    void fetchRecurso_debeMapearRespuesta() {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.createServer(restTemplate);
        CapecoApiAdapter adapter = new CapecoApiAdapter(restTemplate, BASE_URL, API_KEY, catalogCache, catalogMetrics, catalogEventLogger);

        server.expect(requestTo(BASE_URL + "/recursos/MAT-001"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("X-API-Key", API_KEY))
                .andRespond(withSuccess(
                        "{\"external_id\":\"MAT-001\",\"nombre\":\"CEMENTO\",\"tipo\":\"MATERIAL\",\"unidad\":\"BOL\",\"precio\":25.50,\"activo\":true}",
                        MediaType.APPLICATION_JSON));

        RecursoSnapshot snapshot = adapter.fetchRecurso("MAT-001", "CAPECO");

        assertEquals("MAT-001", snapshot.externalId());
        assertEquals(TipoRecurso.MATERIAL, snapshot.tipo());
        server.verify();
    }

    @Test
    void fetchRecurso_404_debeLanzarNotFound() {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.createServer(restTemplate);
        CapecoApiAdapter adapter = new CapecoApiAdapter(restTemplate, BASE_URL, API_KEY, catalogCache, catalogMetrics, catalogEventLogger);

        server.expect(requestTo(BASE_URL + "/recursos/NO-EXISTE"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(NOT_FOUND));

        assertThrows(CatalogNotFoundException.class,
                () -> adapter.fetchRecurso("NO-EXISTE", "CAPECO"));
        server.verify();
    }

    @Test
    void searchRecursos_debeFiltrarDesdeApi() {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.createServer(restTemplate);
        CapecoApiAdapter adapter = new CapecoApiAdapter(restTemplate, BASE_URL, API_KEY, catalogCache, catalogMetrics, catalogEventLogger);

        String url = BASE_URL + "/recursos?catalogSource=CAPECO&query=cemento&tipo=MATERIAL&unidad=BOL&limit=1&offset=0";
        server.expect(requestTo(url))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        "[{\"external_id\":\"MAT-001\",\"nombre\":\"CEMENTO\",\"tipo\":\"MATERIAL\",\"unidad\":\"BOL\",\"precio\":25.50}]",
                        MediaType.APPLICATION_JSON));

        RecursoSearchCriteria criteria = RecursoSearchCriteria.builder()
                .query("cemento")
                .tipo(TipoRecurso.MATERIAL)
                .unidad("BOL")
                .limit(1)
                .offset(0)
                .build();

        List<RecursoSnapshot> results = adapter.searchRecursos(criteria, "CAPECO");
        assertEquals(1, results.size());
        assertEquals("MAT-001", results.get(0).externalId());
        server.verify();
    }

    @Test
    void fetchRecurso_reintentos_debenLanzarServiceException() {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.createServer(restTemplate);
        CapecoApiAdapter adapter = new CapecoApiAdapter(restTemplate, BASE_URL, API_KEY, catalogCache, catalogMetrics, catalogEventLogger);

        server.expect(times(3), requestTo(BASE_URL + "/recursos/MAT-500"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withServerError());

        assertThrows(CatalogServiceException.class,
                () -> adapter.fetchRecurso("MAT-500", "CAPECO"));
        server.verify();
    }

    @Test
    void isRecursoActive_debeEvaluarEstado() {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.createServer(restTemplate);
        CapecoApiAdapter adapter = new CapecoApiAdapter(restTemplate, BASE_URL, API_KEY, catalogCache, catalogMetrics, catalogEventLogger);

        server.expect(requestTo(BASE_URL + "/recursos/MAT-002"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        "{\"external_id\":\"MAT-002\",\"nombre\":\"ACERO\",\"tipo\":\"MATERIAL\",\"unidad\":\"KG\",\"precio\":4.20,\"activo\":false}",
                        MediaType.APPLICATION_JSON));

        assertTrue(!adapter.isRecursoActive("MAT-002", "CAPECO"));
        server.verify();
    }
}
