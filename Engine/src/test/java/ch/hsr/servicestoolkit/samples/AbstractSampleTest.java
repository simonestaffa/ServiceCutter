package ch.hsr.servicestoolkit.samples;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import ch.hsr.servicestoolkit.EngineServiceAppication;
import ch.hsr.servicestoolkit.IntegrationTestHelper;
import ch.hsr.servicestoolkit.UrlHelper;
import ch.hsr.servicestoolkit.importer.api.DomainModel;
import ch.hsr.servicestoolkit.importer.api.UserRepresentationContainer;
import ch.hsr.servicestoolkit.solver.SolverConfiguration;
import ch.hsr.servicestoolkit.solver.SolverResult;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = EngineServiceAppication.class)
@IntegrationTest("server.port=0")
@WebAppConfiguration
public abstract class AbstractSampleTest {

	@Value("${local.server.port}")
	private int port;
	private RestTemplate restTemplate = new TestRestTemplate();
	private Logger log = LoggerFactory.getLogger(AbstractSampleTest.class);

	@Test
	public void sample() throws UnsupportedEncodingException, URISyntaxException, IOException {
		Integer modelId = createModelOnApi();

		uploadUserRepresentations(modelId);

		solveModel(modelId);
	}

	private void uploadUserRepresentations(final Integer modelId) throws URISyntaxException, UnsupportedEncodingException, IOException {
		UserRepresentationContainer userRepContainer = IntegrationTestHelper.readFromFile(getRepresentationsFile(), UserRepresentationContainer.class);

		log.info("read user Representations: {}", userRepContainer);

		HttpEntity<UserRepresentationContainer> request = IntegrationTestHelper.createHttpRequestWithPostObj(userRepContainer);
		String path = UrlHelper.userRepresentations(modelId, port);
		log.info("store user representations on {}", path);

		ResponseEntity<Void> entity = this.restTemplate.exchange(path, HttpMethod.POST, request, new ParameterizedTypeReference<Void>() {
		});

		assertEquals(HttpStatus.NO_CONTENT, entity.getStatusCode());
	}

	private void solveModel(final Integer modelId) {
		SolverConfiguration config = new SolverConfiguration();
		HttpEntity<SolverConfiguration> request = IntegrationTestHelper.createHttpRequestWithPostObj(config);
		ResponseEntity<SolverResult> solverResponse = this.restTemplate.exchange(UrlHelper.solve(modelId, port), HttpMethod.POST, request,
				new ParameterizedTypeReference<SolverResult>() {
				});

		assertEquals(HttpStatus.OK, solverResponse.getStatusCode());

		log.info("found services {}", solverResponse.getBody().getServices());
	}

	private Integer createModelOnApi() throws URISyntaxException, UnsupportedEncodingException, IOException {
		DomainModel input = IntegrationTestHelper.readFromFile(getModelFile(), DomainModel.class);

		HttpEntity<DomainModel> request = IntegrationTestHelper.createHttpRequestWithPostObj(input);
		ResponseEntity<Map<String, Object>> entity = this.restTemplate.exchange(UrlHelper.importDomain(port), HttpMethod.POST, request,
				new ParameterizedTypeReference<Map<String, Object>>() {
				});

		assertEquals(HttpStatus.OK, entity.getStatusCode());
		Integer modelId = (Integer) entity.getBody().get("id");
		assertNotNull(modelId);
		assertTrue(((String) entity.getBody().get("message")).startsWith("model "));
		return modelId;
	}

	protected abstract String getModelFile();

	protected abstract String getRepresentationsFile();

}