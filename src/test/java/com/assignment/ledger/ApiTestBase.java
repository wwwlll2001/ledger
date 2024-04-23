package com.assignment.ledger;

import io.restassured.RestAssured;
import io.restassured.parsing.Parser;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.DeleteTopicsResult;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.data.elasticsearch.NoSuchIndexException;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.core.query.StringQuery;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@Slf4j
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = LedgerApplication.class)
public class ApiTestBase {

    @LocalServerPort
    private int port;

    @Autowired
    Flyway flyway;

    @Autowired
    ElasticsearchTemplate elasticsearchTemplate;

    public static MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.0.28")
            .withDatabaseName("ledger")
            .withUsername("root")
            .withPassword("root")
            .withReuse(true);
    public static KafkaContainer kafkaContainer =
          new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.2")).withReuse(true);

    public static ElasticsearchContainer elasticsearchContainer =
                             new ElasticsearchContainer("elasticsearch:7.14.0").withReuse(true)
                                     .waitingFor(Wait.forHttp("/").forPort(9200).forStatusCode(200));
    @DynamicPropertySource
    static void setDataSourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mysqlContainer::getUsername);
        registry.add("spring.datasource.password", mysqlContainer::getPassword);

        registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);

        registry.add("spring.data.elasticsearch.client.reactive.endpoints",
                                                                     () -> elasticsearchContainer.getHttpHostAddress());
        registry.add("spring.data.elasticsearch.client.reactive.use-ssl", () -> "false");
    }

    @BeforeEach
    public void setUp() throws IOException, InterruptedException {
        RestAssured.port = port;
        RestAssured.basePath = "/";
        RestAssured.defaultParser = Parser.JSON;

        clean();
    }

    @BeforeAll
    public static void beforeAllSetUp() {
        mysqlContainer.start();
        kafkaContainer.start();
        elasticsearchContainer.start();
    }

    private void clean() throws IOException, InterruptedException {
        mysqlClean();
        elasticsearchClean();
//        kafkaClean();
    }

    private void elasticsearchClean() {
        clearIndex("transactions");
        clearIndex("wallet_change_history");
        clearIndex("history_transaction");
    }

    private void clearIndex(String indexName) {
        IndexCoordinates indexCoordinates = IndexCoordinates.of(indexName);
        Query query = new StringQuery("{\"match_all\": {}}");
        try {
            elasticsearchTemplate.delete(query, Object.class, indexCoordinates);
        } catch (NoSuchIndexException e) {
            //ignore no such index exception
        }
    }

    private void mysqlClean() {
        flyway.clean();
        flyway.migrate();
    }

    private void kafkaCleanV1() throws IOException, InterruptedException {
        Container.ExecResult execResult = kafkaContainer.execInContainer(
                "sh", "-c",
                "kafka-topics --delete --topic '.*' --bootstrap-server " + kafkaContainer.getBootstrapServers()
        );

        if (execResult.getExitCode() != 0) {
            throw new RuntimeException("Failed to delete topics, error: " + execResult.getStderr());
        }
    }

    //not work
    private void kafkaClean() {
        Properties config = new Properties();
        config.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers());
//        config.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, "30000");
        try (AdminClient admin = AdminClient.create(config)) {

            // Delete topic
            ListTopicsResult listTopicsResult = admin.listTopics();
            listTopicsResult.names().whenComplete((topicNames, throwable) -> {
                System.out.println("deleting + " + topicNames);
                DeleteTopicsResult deleteResult = admin.deleteTopics(topicNames);
                try {
                    deleteResult.all().get();
                } catch (InterruptedException | ExecutionException e) {
                    log.error(e.getMessage(), e);
                    throw new RuntimeException(e);
                }
            });
        }
    }
}
