package com.catalyst.user.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import jakarta.persistence.Entity;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

/**
 * Architecture tests for user-service enforcing Hexagonal Architecture rules.
 *
 * <p>These tests guarantee that the dependency rules are respected at all times:
 * Infrastructure → Application → Domain (never the reverse).
 *
 * @author Catalyst Team
 * @since 0.1.0
 */
@DisplayName("Hexagonal Architecture — user-service")
class HexagonalArchitectureTest {

    private static final String BASE_PACKAGE = "com.catalyst.user";

    private static final String DOMAIN_LAYER       = BASE_PACKAGE + ".domain..";
    private static final String APPLICATION_LAYER  = BASE_PACKAGE + ".application..";
    private static final String INFRASTRUCTURE_LAYER = BASE_PACKAGE + ".infrastructure..";

    private static JavaClasses importedClasses;

    @BeforeAll
    static void importClasses() {
        importedClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages(BASE_PACKAGE);
    }

    // ── Layer Dependency Rules ────────────────────────────────────────────────

    @Nested
    @DisplayName("Layer Dependencies")
    class LayerDependencies {

        @Test
        @DisplayName("layeredArchitecture_whenEnforced_thenInfrastructureMayNotBeAccessedByApplicationOrDomain")
        void layeredArchitecture_whenEnforced_thenInfrastructureMayNotBeAccessedByApplicationOrDomain() {
            ArchRule rule = layeredArchitecture()
                    .consideringAllDependencies()
                    .layer("Domain").definedBy(DOMAIN_LAYER)
                    .layer("Application").definedBy(APPLICATION_LAYER)
                    .layer("Infrastructure").definedBy(INFRASTRUCTURE_LAYER)

                    .whereLayer("Domain").mayNotAccessAnyLayer()
                    .whereLayer("Application").mayOnlyAccessLayers("Domain")
                    .whereLayer("Infrastructure").mayOnlyAccessLayers("Application", "Domain");

            rule.check(importedClasses);
        }

        @Test
        @DisplayName("domainLayer_whenChecked_thenMayNotDependOnApplicationOrInfrastructure")
        void domainLayer_whenChecked_thenMayNotDependOnApplicationOrInfrastructure() {
            ArchRule rule = noClasses()
                    .that().resideInAPackage(DOMAIN_LAYER)
                    .should().dependOnClassesThat()
                    .resideInAnyPackage(APPLICATION_LAYER, INFRASTRUCTURE_LAYER);

            rule.check(importedClasses);
        }

        @Test
        @DisplayName("applicationLayer_whenChecked_thenMayNotDependOnInfrastructure")
        void applicationLayer_whenChecked_thenMayNotDependOnInfrastructure() {
            ArchRule rule = noClasses()
                    .that().resideInAPackage(APPLICATION_LAYER)
                    .should().dependOnClassesThat()
                    .resideInAPackage(INFRASTRUCTURE_LAYER);

            rule.check(importedClasses);
        }
    }

    // ── Domain Layer Rules ────────────────────────────────────────────────────

    @Nested
    @DisplayName("Domain Layer")
    class DomainLayer {

        @Test
        @DisplayName("domainClasses_whenChecked_thenHaveNoSpringAnnotations")
        void domainClasses_whenChecked_thenHaveNoSpringAnnotations() {
            ArchRule rule = noClasses()
                    .that().resideInAPackage(DOMAIN_LAYER)
                    .should().beAnnotatedWith(Service.class)
                    .orShould().beAnnotatedWith(Component.class)
                    .orShould().beAnnotatedWith(Repository.class)
                    .orShould().beAnnotatedWith(Entity.class)
                    .because("Domain layer must be framework-agnostic — no Spring or JPA annotations allowed");

            rule.check(importedClasses);
        }

        @Test
        @DisplayName("domainClasses_whenChecked_thenHaveNoSpringImports")
        void domainClasses_whenChecked_thenHaveNoSpringImports() {
            ArchRule rule = noClasses()
                    .that().resideInAPackage(DOMAIN_LAYER)
                    .should().dependOnClassesThat()
                    .resideInAnyPackage("org.springframework..")
                    .because("Domain layer must be framework-agnostic — no Spring imports allowed");

            rule.check(importedClasses);
        }

        @Test
        @DisplayName("domainClasses_whenChecked_thenHaveNoJpaImports")
        void domainClasses_whenChecked_thenHaveNoJpaImports() {
            ArchRule rule = noClasses()
                    .that().resideInAPackage(DOMAIN_LAYER)
                    .should().dependOnClassesThat()
                    .resideInAnyPackage("jakarta.persistence..")
                    .because("Domain layer must be framework-agnostic — no JPA annotations allowed");

            rule.check(importedClasses);
        }
    }

    // ── Application Layer Rules ───────────────────────────────────────────────

    @Nested
    @DisplayName("Application Layer")
    class ApplicationLayer {

        @Test
        @DisplayName("applicationLayer_whenChecked_thenHasNoRestControllers")
        void applicationLayer_whenChecked_thenHasNoRestControllers() {
            ArchRule rule = noClasses()
                    .that().resideInAPackage(APPLICATION_LAYER)
                    .should().beAnnotatedWith(RestController.class)
                    .because("Controllers belong in infrastructure layer, not application layer");

            rule.check(importedClasses);
        }

        @Test
        @DisplayName("applicationLayer_whenChecked_thenHasNoJpaEntities")
        void applicationLayer_whenChecked_thenHasNoJpaEntities() {
            ArchRule rule = noClasses()
                    .that().resideInAPackage(APPLICATION_LAYER)
                    .should().beAnnotatedWith(Entity.class)
                    .because("JPA entities belong in infrastructure layer, not application layer");

            rule.check(importedClasses);
        }

        @Test
        @DisplayName("inputPorts_whenChecked_thenAreInterfaces")
        void inputPorts_whenChecked_thenAreInterfaces() {
            ArchRule rule = classes()
                    .that().resideInAPackage(BASE_PACKAGE + ".application.ports.input..")
                    .should().beInterfaces()
                    .because("Input ports (use cases) must be interfaces to allow multiple implementations and easy testing");

            rule.check(importedClasses);
        }

        @Test
        @DisplayName("outputPorts_whenChecked_thenAreInterfaces")
        void outputPorts_whenChecked_thenAreInterfaces() {
            ArchRule rule = classes()
                    .that().resideInAPackage(BASE_PACKAGE + ".application.ports.output..")
                    .should().beInterfaces()
                    .because("Output ports must be interfaces — implementations live in infrastructure");

            rule.check(importedClasses);
        }
    }

    // ── Infrastructure Layer Rules ────────────────────────────────────────────

    @Nested
    @DisplayName("Infrastructure Layer")
    class InfrastructureLayer {

        @Test
        @DisplayName("restControllers_whenChecked_thenResideInInfrastructureWebPackage")
        void restControllers_whenChecked_thenResideInInfrastructureWebPackage() {
            ArchRule rule = classes()
                    .that().areAnnotatedWith(RestController.class)
                    .should().resideInAPackage(BASE_PACKAGE + ".infrastructure.web..")
                    .because("REST controllers must live in infrastructure.web package");

            rule.check(importedClasses);
        }

        @Test
        @DisplayName("jpaEntities_whenChecked_thenResideInInfrastructurePersistencePackage")
        void jpaEntities_whenChecked_thenResideInInfrastructurePersistencePackage() {
            ArchRule rule = classes()
                    .that().areAnnotatedWith(Entity.class)
                    .should().resideInAPackage(BASE_PACKAGE + ".infrastructure.persistence..")
                    .because("JPA entities must live in infrastructure.persistence package");

            rule.check(importedClasses);
        }
    }

    // ── Naming Convention Rules ───────────────────────────────────────────────

    @Nested
    @DisplayName("Naming Conventions")
    class NamingConventions {

        @Test
        @DisplayName("useCaseInterfaces_whenChecked_thenEndWithUseCase")
        void useCaseInterfaces_whenChecked_thenEndWithUseCase() {
            ArchRule rule = classes()
                    .that().resideInAPackage(BASE_PACKAGE + ".application.ports.input..")
                    .and().areInterfaces()
                    .should().haveSimpleNameEndingWith("UseCase")
                    .because("Input port interfaces (use cases) must be named VerbNounUseCase per project conventions");

            rule.check(importedClasses);
        }

        @Test
        @DisplayName("outputPortInterfaces_whenChecked_thenEndWithPortOrRepository")
        void outputPortInterfaces_whenChecked_thenEndWithPortOrRepository() {
            ArchRule rule = classes()
                    .that().resideInAPackage(BASE_PACKAGE + ".application.ports.output..")
                    .and().areInterfaces()
                    .should().haveSimpleNameEndingWith("Port")
                    .orShould().haveSimpleNameEndingWith("Repository")
                    .orShould().haveSimpleNameEndingWith("Publisher")
                    .orShould().haveSimpleNameEndingWith("Encoder")
                    .because("Output port interfaces must be named NounPort, NounRepository, NounPublisher, or NounEncoder");

            rule.check(importedClasses);
        }
    }
}
