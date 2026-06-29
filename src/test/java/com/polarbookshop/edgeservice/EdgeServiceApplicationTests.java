package com.polarbookshop.edgeservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest(
	webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@Testcontainers
class EdgeServiceApplicationTests {

	private static final int REDIS_PORT = 6379;
	private static final int KEYCLOAK_PORT = 8080;

	@Container
	static GenericContainer<?> redis = new GenericContainer<>( DockerImageName.parse( "redis:7.0" ) )
		.withExposedPorts( REDIS_PORT );

	@DynamicPropertySource
	static void redisProperties( DynamicPropertyRegistry registry ) {
		registry.add( "spring.data.redis.host", () -> redis.getHost() );
		registry.add( "spring.data.redis.port", () -> redis.getMappedPort( REDIS_PORT ) );
	}

	@Container
	static final GenericContainer<?> keycloak = new GenericContainer<>( DockerImageName.parse( "quay.io/keycloak/keycloak:26.1" ) )
		.withExposedPorts( KEYCLOAK_PORT )
		// Instructs Keycloak to create a realm and client via CLI variables on start
		.withCommand( "start-dev" )
		.waitingFor( Wait.forHttp( "/" ).forPort( KEYCLOAK_PORT ) );

	@DynamicPropertySource
	static void oidcProperties( DynamicPropertyRegistry registry ) {
		String oidcIssuerUrl = String.format(
			"http://%s:%d/realms/master",
			keycloak.getHost(),
			keycloak.getMappedPort( KEYCLOAK_PORT )
		);

		registry.add( "spring.security.oauth2.client.provider.keycloak.issuer-uri", () -> oidcIssuerUrl );
	}

	@Test
	void contextLoads() {
	}
}
