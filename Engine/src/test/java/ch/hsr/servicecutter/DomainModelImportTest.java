package ch.hsr.servicecutter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.client.RestTemplate;

import ch.hsr.servicecutter.importer.api.EntityRelationDiagram;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = EngineServiceAppication.class)
@IntegrationTest("server.port=0")
@WebAppConfiguration
public class DomainModelImportTest {
	@Value("${local.server.port}")
	private int port;
	private RestTemplate restTemplate = new TestRestTemplate();

	@Test
	public void modelImport() throws IOException, URISyntaxException {
		EntityRelationDiagram input = IntegrationTestHelper.readFromFile("test_domain_model.json", EntityRelationDiagram.class);

		HttpEntity<EntityRelationDiagram> request = IntegrationTestHelper.createHttpRequestWithPostObj(input);
		ResponseEntity<Map<String, Object>> entity = this.restTemplate.exchange("http://localhost:" + this.port + "/engine/import", HttpMethod.POST, request,
				new ParameterizedTypeReference<Map<String, Object>>() {
				});

		assertEquals(HttpStatus.OK, entity.getStatusCode());

		assertNotNull(entity.getBody().get("id"));
		assertTrue(((String) entity.getBody().get("message")).startsWith("userSystem "));
	}
}
