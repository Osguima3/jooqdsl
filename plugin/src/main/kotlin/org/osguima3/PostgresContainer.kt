package org.osguima3

import org.testcontainers.containers.PostgreSQLContainer


class PostgresContainer constructor(imageVersion: String) : PostgreSQLContainer<PostgresContainer>(imageVersion)
