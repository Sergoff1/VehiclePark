plugins {
    id("java")
    id("war")
}

group = "ru.lessons.my"
version = "1.0-SNAPSHOT"

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<Test> {
    defaultCharacterEncoding = "UTF-8"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework:spring-webmvc:6.2.8")
    compileOnly("jakarta.servlet:jakarta.servlet-api:6.1.0")

    implementation("com.fasterxml.jackson.core:jackson-databind:2.19.0")

    implementation("org.springframework.security:spring-security-web:6.5.2")
    implementation("org.springframework.security:spring-security-config:6.5.2")
    implementation("org.springframework.security:spring-security-oauth2-resource-server:6.5.2")
    implementation("org.springframework.security:spring-security-oauth2-jose:6.5.2")
    testImplementation("org.springframework.security:spring-security-test:6.5.2")

    implementation("org.thymeleaf:thymeleaf-spring6:3.1.3.RELEASE")

    implementation("ch.qos.logback:logback-classic:1.5.18")

    implementation("org.postgresql:postgresql:42.7.7")
    implementation("org.springframework:spring-orm:6.2.8")
    implementation("org.hibernate.orm:hibernate-core:7.0.3.Final")
    implementation("org.hibernate.validator:hibernate-validator:9.0.1.Final")

    compileOnly("org.projectlombok:lombok:1.18.38")
    annotationProcessor("org.projectlombok:lombok:1.18.38")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.springframework:spring-test:6.2.8")
    testImplementation("jakarta.servlet:jakarta.servlet-api:6.1.0")
    testImplementation("com.h2database:h2:2.3.232")
    testImplementation("org.glassfish:jakarta.el:4.0.2")
    testImplementation("org.hamcrest:hamcrest:3.0")
    testImplementation("com.jayway.jsonpath:json-path:2.9.0")
}

tasks.test {
    useJUnitPlatform()
}
