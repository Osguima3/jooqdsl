package org.osguima3.jooqdsl.plugin.container

import org.testcontainers.containers.PostgreSQLContainer

class PostgresContainer(imageVersion: String) : PostgreSQLContainer<PostgresContainer>(imageVersion)
