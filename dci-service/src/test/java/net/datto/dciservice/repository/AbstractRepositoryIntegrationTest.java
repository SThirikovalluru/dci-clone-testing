package net.datto.dciservice.repository;

import net.datto.dciservice.configuration.AwsLocalstackConfiguration;
import net.datto.dciservice.configuration.RepositoryTestConfiguration;
import net.datto.dciservice.configuration.SpringTestConfig;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;

import java.util.concurrent.TimeUnit;

import static org.testcontainers.containers.localstack.LocalStackContainer.Service.DYNAMODB;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.SQS;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.SSM;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;
@ActiveProfiles("localstack")
@Import({SpringTestConfig.class, AwsLocalstackConfiguration.class, RepositoryTestConfiguration.class})
@Testcontainers
public class AbstractRepositoryIntegrationTest {
    private static final String DYNAMO_DB_DATA_PATH = "/dynamodb";
    private static final String SCRIPTS_PATH = "/scripts";
    private static final String CONTAINER_INIT_READY_PATH = "/etc/localstack/init/ready.d/";
    private static final int NUMBER_OF_TABLES = 3;

    public static final LocalStackContainer LOCAL_STACK_CONTAINER = new LocalStackContainer(DockerImageName.parse("localstack/localstack:2.2.0"))
            .withServices(DYNAMODB, SSM, SQS)
            .withClasspathResourceMapping(DYNAMO_DB_DATA_PATH, DYNAMO_DB_DATA_PATH, BindMode.READ_ONLY)
            .withCopyToContainer(MountableFile.forClasspathResource(SCRIPTS_PATH, 775), CONTAINER_INIT_READY_PATH);

    static DynamoDbAsyncClient dynamoDbAsyncClient;

    @BeforeAll
    public static void setUp() {
        LOCAL_STACK_CONTAINER.start();

        dynamoDbAsyncClient = DynamoDbAsyncClient.builder()
                .region(Region.of(LOCAL_STACK_CONTAINER.getRegion()))
                .endpointOverride(LOCAL_STACK_CONTAINER.getEndpointOverride(DYNAMODB))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(LOCAL_STACK_CONTAINER.getAccessKey(), LOCAL_STACK_CONTAINER.getSecretKey())))
                .build();

        await()
                .atMost(5L, TimeUnit.MINUTES)
                .until(() -> dynamoDbAsyncClient.listTables().get().tableNames().size() == NUMBER_OF_TABLES);
    }
}
