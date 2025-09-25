package com.kshrd.jpahibernate02_homework;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(title = "Hibernate / JPA Homework", version = "1.0", description = "This Spring Boot application provides CRUD operations for managing products using JPA EntityManager. It supports product creation, updating, deletion, retrieval, and pagination."))
public class JpaHibernate02HomeworkApplication {

    public static void main(String[] args) {
        SpringApplication.run(JpaHibernate02HomeworkApplication.class, args);
    }

}
